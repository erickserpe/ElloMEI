package br.com.ellomei.service;

import br.com.ellomei.domain.Role;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.event.UserRegisteredEvent;
import br.com.ellomei.exception.UsuarioDuplicadoException;
import br.com.ellomei.repository.RoleRepository;
import br.com.ellomei.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * Serviço responsável pela gestão de usuários.
 *
 * Este serviço implementa o padrão de Eventos de Domínio,
 * publicando eventos quando ações importantes ocorrem (ex: novo usuário registrado).
 *
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injetamos o codificador de senhas

    @Autowired
    private ApplicationEventPublisher eventPublisher; // Publicador de eventos de domínio

    /**
     * Salva um novo usuário no sistema.
     *
     * Este método:
     * 1. Valida se username e email são únicos
     * 2. Criptografa a senha do usuário
     * 3. Associa a role padrão ROLE_USER ao usuário
     * 4. Salva o usuário no banco de dados
     * 5. Publica um evento UserRegisteredEvent para notificar outros componentes
     *
     * @param usuario O usuário a ser salvo
     * @return O usuário salvo com ID gerado
     * @throws UsuarioDuplicadoException se username ou email já existirem
     */
    public Usuario salvar(Usuario usuario) {
        logger.info("📝 Iniciando cadastro de novo usuário: {}", usuario.getUsername());

        // Validação 1: Verifica se o username já existe
        validarUsernameUnico(usuario.getUsername());

        // Validação 2: Verifica se o email já existe
        validarEmailUnico(usuario.getEmail());

        // Criptografa a senha antes de salvar no banco
        String senhaOriginal = usuario.getPassword();
        usuario.setPassword(passwordEncoder.encode(senhaOriginal));
        logger.debug("🔒 Senha criptografada com sucesso");

        // Busca a role ROLE_USER no banco de dados
        Role userRole = roleRepository.findByNome("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Erro: Role ROLE_USER não encontrada no banco de dados."));

        // Associa a role ao usuário
        usuario.setRoles(Set.of(userRole));
        logger.debug("👤 Role ROLE_USER associada ao usuário");

        // Salva o usuário no banco de dados
        Usuario novoUsuario = usuarioRepository.save(usuario);
        logger.info("✅ Usuário {} cadastrado com sucesso! ID: {}", novoUsuario.getUsername(), novoUsuario.getId());

        // Publica o evento após salvar
        // Isso permite que outros componentes (listeners) reajam ao registro
        // sem que o UsuarioService precise conhecê-los
        eventPublisher.publishEvent(new UserRegisteredEvent(this, novoUsuario));
        logger.debug("📢 Evento UserRegisteredEvent publicado");

        return novoUsuario;
    }

    /**
     * Valida se o username é único no sistema.
     *
     * @param username Username a ser validado
     * @throws UsuarioDuplicadoException se o username já existir
     */
    private void validarUsernameUnico(String username) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByUsername(username);
        if (usuarioExistente.isPresent()) {
            logger.warn("⚠️ Tentativa de cadastro com username duplicado: {}", username);
            throw new UsuarioDuplicadoException(
                "O nome de usuário '" + username + "' já está em uso. Por favor, escolha outro."
            );
        }
        logger.debug("✅ Username {} está disponível", username);
    }

    /**
     * Valida se o email é único no sistema.
     *
     * @param email Email a ser validado
     * @throws UsuarioDuplicadoException se o email já existir
     */
    private void validarEmailUnico(String email) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);
        if (usuarioExistente.isPresent()) {
            logger.warn("⚠️ Tentativa de cadastro com email duplicado: {}", email);
            throw new UsuarioDuplicadoException(
                "O email '" + email + "' já está cadastrado. Use outro email ou recupere sua senha."
            );
        }
        logger.debug("✅ Email {} está disponível", email);
    }
}