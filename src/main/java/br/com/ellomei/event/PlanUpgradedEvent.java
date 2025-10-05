package br.com.ellomei.event;

import br.com.ellomei.domain.Assinatura;
import br.com.ellomei.domain.PlanoAssinatura;
import br.com.ellomei.domain.Usuario;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de domínio disparado quando um usuário faz upgrade de plano.
 * 
 * Este evento permite desacoplamento entre o processo de upgrade e
 * ações subsequentes como:
 * - Envio de e-mail de confirmação
 * - Atualização de métricas de negócio
 * - Notificações in-app
 * - Registro em analytics
 * - Auditoria
 * 
 * Fluxo típico:
 * 1. Usuário solicita upgrade (FREE → PRO)
 * 2. Pagamento é processado
 * 3. Assinatura é criada/atualizada
 * 4. Evento PlanUpgradedEvent é publicado
 * 5. Listeners reagem ao evento (e-mail, métricas, etc.)
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
public class PlanUpgradedEvent extends ApplicationEvent {
    
    private final Usuario usuario;
    private final PlanoAssinatura planoAnterior;
    private final PlanoAssinatura planoNovo;
    private final Assinatura assinatura;
    
    /**
     * Construtor do evento de upgrade de plano.
     * 
     * @param source Objeto que publicou o evento (geralmente o service)
     * @param usuario Usuário que fez o upgrade
     * @param planoAnterior Plano anterior do usuário
     * @param planoNovo Novo plano do usuário
     * @param assinatura Assinatura criada/atualizada
     */
    public PlanUpgradedEvent(Object source, Usuario usuario, 
                            PlanoAssinatura planoAnterior, 
                            PlanoAssinatura planoNovo,
                            Assinatura assinatura) {
        super(source);
        this.usuario = usuario;
        this.planoAnterior = planoAnterior;
        this.planoNovo = planoNovo;
        this.assinatura = assinatura;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public PlanoAssinatura getPlanoAnterior() {
        return planoAnterior;
    }
    
    public PlanoAssinatura getPlanoNovo() {
        return planoNovo;
    }
    
    public Assinatura getAssinatura() {
        return assinatura;
    }
    
    /**
     * Verifica se foi um upgrade (FREE → PRO).
     */
    public boolean isUpgrade() {
        return planoAnterior == PlanoAssinatura.FREE && 
               planoNovo == PlanoAssinatura.PRO;
    }
    
    /**
     * Verifica se foi um downgrade (PRO → FREE).
     */
    public boolean isDowngrade() {
        return planoAnterior == PlanoAssinatura.PRO && 
               planoNovo == PlanoAssinatura.FREE;
    }
}

