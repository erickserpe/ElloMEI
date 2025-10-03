package br.com.scfmei.listener;

import br.com.scfmei.event.PlanUpgradedEvent;
import br.com.scfmei.event.SubscriptionCancelledEvent;
import br.com.scfmei.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos relacionados a assinaturas.
 * 
 * Este componente escuta eventos de upgrade, downgrade e cancelamento
 * de assinaturas e executa ações relacionadas.
 * 
 * Ações atuais:
 * - Logging detalhado de eventos
 * 
 * Futuras implementações:
 * - Envio de e-mails de confirmação
 * - Atualização de métricas de negócio
 * - Notificações in-app
 * - Integração com analytics (Google Analytics, Mixpanel)
 * - Registro em sistema de auditoria
 * - Envio de pesquisa NPS (Net Promoter Score)
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Component
public class SubscriptionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionEventListener.class);

    @Autowired
    private EmailService emailService;

    /**
     * Manipula evento de upgrade de plano.
     *
     * Este método é chamado automaticamente quando um usuário
     * faz upgrade de FREE para PRO.
     *
     * @param event Evento contendo informações do upgrade
     */
    @EventListener
    @Async
    public void handlePlanUpgraded(PlanUpgradedEvent event) {
        logger.info("========================================");
        logger.info("UPGRADE DE PLANO REALIZADO!");
        logger.info("Usuário: {}", event.getUsuario().getUsername());
        logger.info("Plano Anterior: {}", event.getPlanoAnterior());
        logger.info("Plano Novo: {}", event.getPlanoNovo());
        logger.info("Assinatura ID: {}", event.getAssinatura().getId());
        logger.info("Valor Mensal: R$ {}", event.getAssinatura().getValorMensal());
        logger.info("Forma de Pagamento: {}", event.getAssinatura().getFormaPagamento());
        logger.info("========================================");

        // Enviar e-mail de confirmação de upgrade
        emailService.enviarEmailUpgrade(event.getUsuario(), event.getAssinatura());

        // TODO: Registrar métrica de conversão
        // TODO: Enviar notificação in-app
        // TODO: Registrar em analytics

        if (event.isUpgrade()) {
            logger.info("✅ Conversão FREE → PRO realizada com sucesso!");
        }
    }
    
    /**
     * Manipula evento de cancelamento de assinatura.
     *
     * Este método é chamado automaticamente quando um usuário
     * cancela sua assinatura PRO.
     *
     * @param event Evento contendo informações do cancelamento
     */
    @EventListener
    @Async
    public void handleSubscriptionCancelled(SubscriptionCancelledEvent event) {
        logger.info("========================================");
        logger.info("ASSINATURA CANCELADA");
        logger.info("Usuário: {}", event.getUsuario().getUsername());
        logger.info("Plano Cancelado: {}", event.getAssinatura().getPlano());
        logger.info("Motivo: {}", event.getMotivo());
        logger.info("Data Cancelamento: {}", event.getAssinatura().getDataCancelamento());
        logger.info("========================================");

        // Enviar e-mail de confirmação de cancelamento
        emailService.enviarEmailCancelamento(event.getUsuario(), event.getMotivo());

        // TODO: Enviar pesquisa de satisfação (NPS)
        // TODO: Registrar métrica de churn
        // TODO: Notificar equipe de retenção
        // TODO: Oferecer desconto para retenção

        logger.warn("⚠️ Churn detectado - Usuário voltou para plano FREE");
    }
}

