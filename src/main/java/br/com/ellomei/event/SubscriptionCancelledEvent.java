package br.com.ellomei.event;

import br.com.ellomei.domain.Assinatura;
import br.com.ellomei.domain.Usuario;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de domínio disparado quando uma assinatura é cancelada.
 * 
 * Este evento permite desacoplamento entre o cancelamento e
 * ações subsequentes como:
 * - Envio de e-mail de confirmação de cancelamento
 * - Pesquisa de satisfação (NPS)
 * - Atualização de métricas de churn
 * - Notificações para equipe de retenção
 * - Auditoria
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public class SubscriptionCancelledEvent extends ApplicationEvent {
    
    private final Usuario usuario;
    private final Assinatura assinatura;
    private final String motivo;
    
    public SubscriptionCancelledEvent(Object source, Usuario usuario, 
                                     Assinatura assinatura, String motivo) {
        super(source);
        this.usuario = usuario;
        this.assinatura = assinatura;
        this.motivo = motivo;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public Assinatura getAssinatura() {
        return assinatura;
    }
    
    public String getMotivo() {
        return motivo;
    }
}

