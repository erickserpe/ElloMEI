package br.com.ellomei.service;

import br.com.ellomei.domain.Assinatura;
import br.com.ellomei.domain.StatusAssinatura;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.AssinaturaRepository;
import com.mercadopago.resources.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável por gerenciar tentativas de pagamento falhadas.
 * 
 * Funcionalidades:
 * - Retry automático de pagamentos falhados
 * - Notificação de falhas persistentes
 * - Suspensão automática após múltiplas falhas
 * - Logs detalhados de tentativas
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Service
public class PaymentRetryService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentRetryService.class);
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_INTERVAL_HOURS = 24;
    
    @Autowired
    private AssinaturaRepository assinaturaRepository;
    
    @Autowired
    private MercadoPagoService mercadoPagoService;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Job agendado para processar retries de pagamentos falhados.
     * Executa diariamente às 3h da manhã.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void processarRetrysPendentes() {
        logger.info("Iniciando processamento de retries de pagamentos falhados...");
        
        try {
            // Buscar assinaturas com pagamento pendente de retry
            List<Assinatura> assinaturasParaRetry = buscarAssinaturasParaRetry();
            
            logger.info("Encontradas {} assinaturas para retry", assinaturasParaRetry.size());
            
            for (Assinatura assinatura : assinaturasParaRetry) {
                processarRetry(assinatura);
            }
            
            logger.info("Processamento de retries concluído");
            
        } catch (Exception e) {
            logger.error("Erro ao processar retries de pagamentos: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Busca assinaturas que precisam de retry de pagamento.
     */
    private List<Assinatura> buscarAssinaturasParaRetry() {
        // Buscar assinaturas suspensas que ainda não atingiram o limite de tentativas
        LocalDate dataLimite = LocalDate.now().minusDays(RETRY_INTERVAL_HOURS / 24);
        
        return assinaturaRepository.findAssinaturasParaRetry(
            StatusAssinatura.SUSPENSA,
            dataLimite,
            MAX_RETRY_ATTEMPTS
        );
    }
    
    /**
     * Processa retry de pagamento para uma assinatura.
     */
    @Transactional
    public void processarRetry(Assinatura assinatura) {
        Usuario usuario = assinatura.getUsuario();
        
        logger.info("Processando retry de pagamento - Assinatura: {} - Usuário: {} - Tentativa: {}", 
                   assinatura.getId(), usuario.getUsername(), assinatura.getTentativasPagamento() + 1);
        
        try {
            // Incrementar contador de tentativas
            assinatura.incrementarTentativasPagamento();
            
            // Tentar processar pagamento novamente
            Payment payment = tentarPagamento(assinatura);
            
            if (payment != null && "approved".equals(payment.getStatus())) {
                // Pagamento aprovado - reativar assinatura
                assinatura.reativar();
                assinatura.resetarTentativasPagamento();
                assinaturaRepository.save(assinatura);
                
                // Enviar e-mail de sucesso
                emailService.enviarEmailPagamentoRecuperado(usuario, assinatura);
                
                logger.info("Retry bem-sucedido - Assinatura reativada: {}", assinatura.getId());
                
            } else {
                // Pagamento ainda falhou
                assinaturaRepository.save(assinatura);
                
                if (assinatura.getTentativasPagamento() >= MAX_RETRY_ATTEMPTS) {
                    // Atingiu limite de tentativas - cancelar assinatura
                    assinatura.cancelar("Limite de tentativas de pagamento excedido");
                    assinaturaRepository.save(assinatura);
                    
                    // Enviar e-mail de cancelamento
                    emailService.enviarEmailPagamentoFalhaDefinitiva(usuario, assinatura);
                    
                    logger.warn("Limite de retries atingido - Assinatura cancelada: {}", assinatura.getId());
                    
                } else {
                    // Ainda há tentativas restantes
                    emailService.enviarEmailFalhaPagamento(usuario, assinatura, 
                        assinatura.getTentativasPagamento(), MAX_RETRY_ATTEMPTS);
                    
                    logger.info("Retry falhou - Tentativa {}/{} - Assinatura: {}", 
                               assinatura.getTentativasPagamento(), MAX_RETRY_ATTEMPTS, assinatura.getId());
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar retry - Assinatura: {} - Erro: {}", 
                        assinatura.getId(), e.getMessage(), e);
            
            // Registrar erro mas não propagar exceção
            assinaturaRepository.save(assinatura);
        }
    }
    
    /**
     * Tenta processar pagamento.
     */
    private Payment tentarPagamento(Assinatura assinatura) {
        try {
            // Aqui você implementaria a lógica de retry de pagamento
            // Por exemplo, tentando cobrar novamente no cartão de crédito salvo
            
            // Por enquanto, retornamos null (implementação futura)
            logger.info("Tentativa de pagamento para assinatura: {}", assinatura.getId());
            
            // TODO: Implementar integração com Mercado Pago para retry
            // Exemplo: mercadoPagoService.processarPagamentoRecorrente(assinatura);
            
            return null;
            
        } catch (Exception e) {
            logger.error("Erro ao tentar pagamento: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Processa falha de pagamento imediata (chamado pelo webhook).
     */
    @Transactional
    public void processarFalhaPagamento(Assinatura assinatura, String motivoFalha) {
        Usuario usuario = assinatura.getUsuario();
        
        logger.warn("Falha de pagamento detectada - Assinatura: {} - Motivo: {}", 
                   assinatura.getId(), motivoFalha);
        
        // Suspender assinatura
        assinatura.suspender(motivoFalha);
        assinatura.incrementarTentativasPagamento();
        assinaturaRepository.save(assinatura);
        
        // Enviar e-mail de notificação
        emailService.enviarEmailFalhaPagamento(usuario, assinatura, 
            assinatura.getTentativasPagamento(), MAX_RETRY_ATTEMPTS);
        
        logger.info("Assinatura suspensa - Retry automático será tentado em {} horas", 
                   RETRY_INTERVAL_HOURS);
    }
    
    /**
     * Verifica se uma assinatura pode ser reativada manualmente.
     */
    public boolean podeReativar(Assinatura assinatura) {
        return assinatura.getStatus() == StatusAssinatura.SUSPENSA 
            && assinatura.getTentativasPagamento() < MAX_RETRY_ATTEMPTS;
    }
    
    /**
     * Reativa assinatura manualmente após pagamento bem-sucedido.
     */
    @Transactional
    public void reativarAssinatura(Assinatura assinatura) {
        assinatura.reativar();
        assinatura.resetarTentativasPagamento();
        assinaturaRepository.save(assinatura);
        
        logger.info("Assinatura reativada manualmente: {}", assinatura.getId());
    }
}

