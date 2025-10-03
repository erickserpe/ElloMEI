package br.com.scfmei.event;

import br.com.scfmei.domain.Usuario;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de domínio disparado quando um novo usuário é registrado no sistema.
 * 
 * Este evento segue o padrão de Eventos de Domínio do Spring, permitindo
 * desacoplamento entre a lógica de criação de usuário e ações subsequentes
 * como envio de e-mails, notificações, auditoria, etc.
 * 
 * Benefícios:
 * - Desacoplamento: UsuarioService não precisa conhecer todas as ações pós-registro
 * - Extensibilidade: Novos listeners podem ser adicionados sem modificar o service
 * - Testabilidade: Cada listener pode ser testado independentemente
 * - Single Responsibility: Cada listener tem uma responsabilidade específica
 * 
 * Exemplo de uso futuro:
 * - Enviar e-mail de boas-vindas
 * - Criar registros de auditoria
 * - Enviar notificações push
 * - Integrar com sistemas externos (CRM, analytics, etc.)
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public class UserRegisteredEvent extends ApplicationEvent {
    
    private final Usuario usuario;

    /**
     * Cria um novo evento de registro de usuário.
     * 
     * @param source O objeto que publicou o evento (geralmente o UsuarioService)
     * @param usuario O usuário que foi registrado
     */
    public UserRegisteredEvent(Object source, Usuario usuario) {
        super(source);
        this.usuario = usuario;
    }

    /**
     * Retorna o usuário que foi registrado.
     * 
     * @return O usuário registrado
     */
    public Usuario getUsuario() {
        return usuario;
    }
}

