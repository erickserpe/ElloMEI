package br.com.scfmei.controller;

import br.com.scfmei.domain.Assinatura;
import br.com.scfmei.domain.FormaPagamento;
import br.com.scfmei.domain.PlanoAssinatura;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.AssinaturaRepository;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.AssinaturaService;
import br.com.scfmei.service.MercadoPagoService;
import com.mercadopago.resources.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    @Autowired
    private MercadoPagoService mercadoPagoService;
    
    @Autowired
    private AssinaturaService assinaturaService;
    
    @Autowired
    private AssinaturaRepository assinaturaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
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
     * @return Status HTTP 200 (OK) ou 500 (Error)
     */
    @PostMapping
    public ResponseEntity<String> processarWebhook(@RequestBody Map<String, Object> body) {
        
        logger.info("Webhook recebido do Mercado Pago: {}", body);
        
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
                    assinaturaService.processarUpgrade(
                        usuario, 
                        plano, 
                        payment, 
                        FormaPagamento.CARTAO_CREDITO
                    );
                    
                    logger.info("Assinatura criada via webhook para usuário: {}", 
                               usuario.getUsername());
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
     * Processa pagamento recusado.
     */
    private void processarPagamentoRecusado(Payment payment, String externalReference) {
        logger.warn("Pagamento recusado: {} - Motivo: {}", 
                   payment.getId(), payment.getStatusDetail());
        
        // TODO: Enviar e-mail notificando falha no pagamento
        // TODO: Suspender assinatura se for renovação
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
}

