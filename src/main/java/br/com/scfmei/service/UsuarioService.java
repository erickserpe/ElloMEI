package br.com.scfmei.service;

import br.com.scfmei.domain.Usuario;
import br.com.scfmei.event.UserRegisteredEvent;
import br.com.scfmei.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela gestão de usuários.
 *
 * Este serviço implementa o padrão de Eventos de Domínio,
 * publicando eventos quando ações importantes ocorrem (ex: novo usuário registrado).
 *
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injetamos o codificador de senhas

    @Autowired
    private ApplicationEventPublisher eventPublisher; // Publicador de eventos de domínio

    /**
     * Salva um novo usuário no sistema.
     *
     * Este método:
     * 1. Criptografa a senha do usuário
     * 2. Define o papel padrão (USER)
     * 3. Salva o usuário no banco de dados
     * 4. Publica um evento UserRegisteredEvent para notificar outros componentes
     *
     * @param usuario O usuário a ser salvo
     * @return O usuário salvo com ID gerado
     */
    public Usuario salvar(Usuario usuario) {
        // Criptografa a senha antes de salvar no banco
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        // Define um papel padrão para novos usuários
        usuario.setRoles("USER");

        // Salva o usuário no banco de dados
        Usuario novoUsuario = usuarioRepository.save(usuario);

        // Publica o evento após salvar
        // Isso permite que outros componentes (listeners) reajam ao registro
        // sem que o UsuarioService precise conhecê-los
        eventPublisher.publishEvent(new UserRegisteredEvent(this, novoUsuario));

        return novoUsuario;
    }
}