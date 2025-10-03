package br.com.scfmei.listener;

import br.com.scfmei.event.UserRegisteredEvent;
import br.com.scfmei.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de registro de novos usuários.
 * 
 * Este componente escuta eventos UserRegisteredEvent e executa ações
 * relacionadas ao registro de novos usuários.
 * 
 * Atualmente, apenas registra um log da criação do usuário.
 * 
 * Futuras implementações podem incluir:
 * - Envio de e-mail de boas-vindas
 * - Criação de dados iniciais para o usuário (contas padrão, categorias, etc.)
 * - Notificações para administradores
 * - Integração com ferramentas de analytics
 * - Registro em sistemas de auditoria
 * 
 * Vantagens desta abordagem:
 * - O UsuarioService não precisa conhecer estas ações
 * - Novos listeners podem ser adicionados sem modificar código existente
 * - Cada listener pode ser testado independentemente
 * - Fácil ativar/desativar funcionalidades (comentar @Component)
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Component
public class UserRegistrationListener {

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationListener.class);

    @Autowired
    private EmailService emailService;

    /**
     * Manipula o evento de registro de novo usuário.
     *
     * Este método é chamado automaticamente pelo Spring sempre que
     * um UserRegisteredEvent é publicado.
     *
     * @param event O evento contendo informações do usuário registrado
     */
    @EventListener
    @Async
    public void handleUserRegistration(UserRegisteredEvent event) {
        String username = event.getUsuario().getUsername();
        String plano = event.getUsuario().getPlano().name();

        logger.info("========================================");
        logger.info("NOVO USUÁRIO REGISTRADO!");
        logger.info("Username: {}", username);
        logger.info("Plano: {}", plano);
        logger.info("========================================");

        // Enviar e-mail de boas-vindas
        emailService.enviarEmailBoasVindas(event.getUsuario());

        // TODO: Criar dados iniciais (conta padrão, categorias sugeridas)
        // TODO: Registrar em sistema de analytics
    }
}

