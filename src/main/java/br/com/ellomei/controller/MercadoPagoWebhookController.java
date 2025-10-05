package br.com.ellomei.controller;

import br.com.ellomei.domain.Assinatura;
import br.com.ellomei.domain.FormaPagamento;
import br.com.ellomei.domain.PlanoAssinatura;
import br.com.ellomei.domain.StatusAssinatura;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.AssinaturaRepository;
import br.com.ellomei.repository.UsuarioRepository;
import br.com.ellomei.service.AssinaturaService;
import br.com.ellomei.service.EmailService;
import br.com.ellomei.service.MercadoPagoService;
import com.mercadopago.resources.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para processar webhooks do Mercado Pago.
 * 
 * O Mercado Pago envia notificações para este endpoint quando:
 * - Um pagamento é aprovado
 * - Um pagamento é recusado
 * - Uma cobrança recorrente é processada
 * - Um chargeback é iniciado
 * 
 * Documentação:
 * https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/webhooks/mercadopago")
public class MercadoPagoWebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    @Value("${mercadopago.webhook.secret:}")
    private String webhookSecret;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private AssinaturaService assinaturaService;

    @Autowired
    private AssinaturaRepository assinaturaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;
    
    /**
     * Endpoint que recebe notificações do Mercado Pago.
     *
     * Tipos de notificação:
     * - payment: Notificação de pagamento
     * - plan: Notificação de plano de assinatura
     * - subscription: Notificação de assinatura
     * - invoice: Notificação de fatura
     *
     * @param body Corpo da notificação
     * @param xSignature Header x-signature para validação
     * @param xRequestId Header x-request-id
     * @return Status HTTP 200 (OK) ou 500 (Error)
     */
    @PostMapping
    public ResponseEntity<String> processarWebhook(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId) {

        logger.info("Webhook recebido do Mercado Pago: {}", body);
        logger.info("X-Signature: {}", xSignature);
        logger.info("X-Request-Id: {}", xRequestId);

        // Validar assinatura do webhook (se configurado)
        if (webhookSecret != null && !webhookSecret.isEmpty()) {
            if (!validarAssinaturaWebhook(body, xSignature, xRequestId)) {
                logger.error("Assinatura do webhook inválida! Possível tentativa de fraude.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }
        }
        
        try {
            String type = (String) body.get("type");
            
            if ("payment".equals(type)) {
                processarNotificacaoPagamento(body);
            } else if ("subscription".equals(type)) {
                processarNotificacaoAssinatura(body);
            } else {
                logger.warn("Tipo de notificação não tratado: {}", type);
            }
            
            return ResponseEntity.ok("OK");
            
        } catch (Exception e) {
            logger.error("Erro ao processar webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Processa notificação de pagamento.
     */
    private void processarNotificacaoPagamento(Map<String, Object> body) {
        try {
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            String paymentIdStr = (String) data.get("id");
            Long paymentId = Long.parseLong(paymentIdStr);
            
            logger.info("Processando notificação de pagamento: {}", paymentId);
            
            // Buscar pagamento no Mercado Pago
            Payment payment = mercadoPagoService.buscarPagamento(paymentId);
            
            String status = payment.getStatus();
            String externalReference = payment.getExternalReference();
            
            logger.info("Status do pagamento: {} - External Reference: {}", 
                       status, externalReference);
            
            if ("approved".equals(status)) {
                processarPagamentoAprovado(payment, externalReference);
            } else if ("pending".equals(status)) {
                processarPagamentoPendente(payment, externalReference);
            } else if ("rejected".equals(status)) {
                processarPagamentoRecusado(payment, externalReference);
            } else if ("refunded".equals(status)) {
                processarPagamentoReembolsado(payment, externalReference);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação de pagamento: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Processa pagamento aprovado.
     */
    private void processarPagamentoAprovado(Payment payment, String externalReference) {
        logger.info("Pagamento aprovado: {}", payment.getId());
        
        // Extrair ID do usuário da external reference
        // Formato: "USER_123_PLAN_PRO"
        if (externalReference != null && externalReference.startsWith("USER_")) {
            String[] parts = externalReference.split("_");
            Long usuarioId = Long.parseLong(parts[1]);
            String planoStr = parts[3];
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                PlanoAssinatura plano = PlanoAssinatura.valueOf(planoStr);
                
                // Verificar se já existe assinatura para este pagamento
                Optional<Assinatura> assinaturaExistente = 
                    assinaturaRepository.findByIdPagamentoExterno(payment.getId().toString());
                
                if (assinaturaExistente.isEmpty()) {
                    // Criar nova assinatura
                    Assinatura assinatura = assinaturaService.processarUpgrade(
                        usuario,
                        plano,
                        payment,
                        FormaPagamento.CARTAO_CREDITO
                    );

                    logger.info("Assinatura criada via webhook para usuário: {}",
                               usuario.getUsername());

                    // Enviar email de pagamento aprovado
                    try {
                        emailService.enviarEmailPagamentoAprovado(
                            usuario,
                            assinatura,
                            payment.getId().toString()
                        );
                        logger.info("Email de pagamento aprovado enviado para: {}",
                                   usuario.getUsername());
                    } catch (Exception e) {
                        logger.error("Erro ao enviar email de pagamento aprovado: {}",
                                    e.getMessage());
                    }
                } else {
                    logger.info("Assinatura já existe para este pagamento: {}",
                               payment.getId());
                }
            } else {
                logger.warn("Usuário não encontrado: {}", usuarioId);
            }
        }
    }
    
    /**
     * Processa pagamento pendente.
     */
    private void processarPagamentoPendente(Payment payment, String externalReference) {
        logger.info("Pagamento pendente: {} - Aguardando confirmação", payment.getId());

        // Extrair ID do usuário da external reference
        if (externalReference != null && externalReference.startsWith("USER_")) {
            String[] parts = externalReference.split("_");
            Long usuarioId = Long.parseLong(parts[1]);

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // Buscar assinatura ativa do usuário
                Optional<Assinatura> assinaturaOpt = assinaturaRepository.findByUsuarioAndStatus(
                    usuario,
                    StatusAssinatura.ATIVA
                );

                if (assinaturaOpt.isPresent()) {
                    Assinatura assinatura = assinaturaOpt.get();

                    // Enviar email de pagamento pendente
                    try {
                        emailService.enviarEmailPagamentoPendente(
                            usuario,
                            assinatura,
                            payment.getId().toString()
                        );
                        logger.info("Email de pagamento pendente enviado para: {}",
                                   usuario.getUsername());
                    } catch (Exception e) {
                        logger.error("Erro ao enviar email de pagamento pendente: {}",
                                    e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Processa pagamento recusado.
     */
    private void processarPagamentoRecusado(Payment payment, String externalReference) {
        logger.warn("Pagamento recusado: {} - Motivo: {}",
                   payment.getId(), payment.getStatusDetail());

        // Extrair ID do usuário da external reference
        if (externalReference != null && externalReference.startsWith("USER_")) {
            String[] parts = externalReference.split("_");
            Long usuarioId = Long.parseLong(parts[1]);

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // Buscar assinatura ativa do usuário
                Optional<Assinatura> assinaturaOpt = assinaturaRepository.findByUsuarioAndStatus(
                    usuario,
                    StatusAssinatura.ATIVA
                );

                if (assinaturaOpt.isPresent()) {
                    Assinatura assinatura = assinaturaOpt.get();

                    // Atualizar motivo da falha
                    assinatura.setMotivoFalhaPagamento(payment.getStatusDetail());
                    assinaturaRepository.save(assinatura);

                    // Enviar email de falha de pagamento
                    try {
                        emailService.enviarEmailFalhaPagamento(usuario, assinatura);
                        logger.info("Email de falha de pagamento enviado para: {}",
                                   usuario.getUsername());
                    } catch (Exception e) {
                        logger.error("Erro ao enviar email de falha de pagamento: {}",
                                    e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Processa pagamento reembolsado.
     */
    private void processarPagamentoReembolsado(Payment payment, String externalReference) {
        logger.warn("Pagamento reembolsado: {}", payment.getId());
        
        // Buscar assinatura pelo ID do pagamento
        Optional<Assinatura> assinaturaOpt = 
            assinaturaRepository.findByIdPagamentoExterno(payment.getId().toString());
        
        if (assinaturaOpt.isPresent()) {
            Assinatura assinatura = assinaturaOpt.get();
            Usuario usuario = assinatura.getUsuario();
            
            // Cancelar assinatura
            assinaturaService.cancelarAssinatura(usuario, "Pagamento reembolsado");
            
            logger.info("Assinatura cancelada devido a reembolso: {}", assinatura.getId());
        }
    }
    
    /**
     * Processa notificação de assinatura recorrente.
     */
    private void processarNotificacaoAssinatura(Map<String, Object> body) {
        logger.info("Processando notificação de assinatura: {}", body);

        // TODO: Implementar lógica de assinaturas recorrentes
        // Quando o Mercado Pago cobrar automaticamente a renovação mensal
    }

    /**
     * Valida a assinatura do webhook do Mercado Pago.
     *
     * Documentação:
     * https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks#editor_3
     *
     * @param body Corpo da notificação
     * @param xSignature Header x-signature
     * @param xRequestId Header x-request-id
     * @return true se a assinatura é válida, false caso contrário
     */
    private boolean validarAssinaturaWebhook(Map<String, Object> body, String xSignature, String xRequestId) {
        if (xSignature == null || xRequestId == null) {
            logger.warn("Headers de assinatura ausentes");
            return false;
        }

        try {
            // Extrair partes da assinatura
            // Formato: ts=timestamp,v1=hash
            String[] parts = xSignature.split(",");
            String timestamp = null;
            String hash = null;

            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    if ("ts".equals(keyValue[0])) {
                        timestamp = keyValue[1];
                    } else if ("v1".equals(keyValue[0])) {
                        hash = keyValue[1];
                    }
                }
            }

            if (timestamp == null || hash == null) {
                logger.warn("Formato de assinatura inválido");
                return false;
            }

            // Construir string para validação
            // Formato: id:request-id:ts:timestamp
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            String id = data != null ? String.valueOf(data.get("id")) : "";
            String manifest = "id:" + id + ";request-id:" + xRequestId + ";ts:" + timestamp;

            // Calcular HMAC SHA256
            String calculatedHash = calcularHMAC(manifest, webhookSecret);

            // Comparar hashes
            boolean valid = calculatedHash.equals(hash);

            if (!valid) {
                logger.error("Hash inválido! Esperado: {}, Recebido: {}", calculatedHash, hash);
            }

            return valid;

        } catch (Exception e) {
            logger.error("Erro ao validar assinatura do webhook: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calcula HMAC SHA256.
     */
    private String calcularHMAC(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}

