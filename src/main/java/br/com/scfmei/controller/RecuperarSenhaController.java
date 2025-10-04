package br.com.scfmei.controller;

import br.com.scfmei.domain.PasswordResetToken;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.PasswordResetTokenRepository;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

/**
 * Controller responsável pela recuperação de senha.
 * 
 * NOTA: Esta é uma implementação simplificada para demonstração.
 * Em produção, você deve:
 * 1. Enviar email real com link de recuperação
 * 2. Usar tokens com expiração
 * 3. Armazenar tokens em tabela separada
 * 4. Implementar rate limiting para evitar abuso
 */
@Controller
public class RecuperarSenhaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Exibe o formulário para solicitar recuperação de senha.
     */
    @GetMapping("/recuperar-senha")
    public String exibirFormularioRecuperacao() {
        return "recuperar-senha/solicitar";
    }

    /**
     * Processa a solicitação de recuperação de senha.
     * Gera token, salva no banco e envia email.
     */
    @PostMapping("/recuperar-senha/solicitar")
    public String solicitarRecuperacao(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        // Por segurança, sempre mostra mensagem de sucesso mesmo se o email não existir
        // Isso evita que atacantes descubram quais emails estão cadastrados
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Se o email estiver cadastrado, você receberá instruções para redefinir sua senha.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();

        // Gera token único
        String tokenString = UUID.randomUUID().toString();

        // Cria e salva o token no banco de dados
        PasswordResetToken token = new PasswordResetToken(tokenString, usuario);
        tokenRepository.save(token);

        // Envia email com link de recuperação
        try {
            emailService.enviarEmailRecuperacaoSenha(
                email,
                usuario.getNomeCompleto() != null ? usuario.getNomeCompleto() : usuario.getUsername(),
                tokenString
            );
        } catch (Exception e) {
            // Se falhar ao enviar email, ainda mostra mensagem de sucesso por segurança
            // mas loga o erro
            System.err.println("Erro ao enviar email de recuperação: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso",
            "Se o email estiver cadastrado, você receberá instruções para redefinir sua senha.");

        return "redirect:/login";
    }

    /**
     * Exibe o formulário para redefinir a senha (com token).
     * Valida token e expiração.
     */
    @GetMapping("/recuperar-senha/redefinir")
    public String exibirFormularioRedefinicao(
            @RequestParam(value = "token", required = false) String token,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Token inválido ou expirado.");
            return "redirect:/login";
        }

        // Busca e valida o token
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Token inválido ou expirado.");
            return "redirect:/login";
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Verifica se o token é válido (não usado e não expirado)
        if (!resetToken.isValido()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                resetToken.isUsado() ? "Este token já foi utilizado." : "Este token expirou. Solicite uma nova recuperação de senha.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        model.addAttribute("email", resetToken.getUsuario().getEmail());
        return "recuperar-senha/redefinir";
    }

    /**
     * Processa a redefinição de senha usando token.
     */
    @PostMapping("/recuperar-senha/redefinir")
    public String redefinirSenha(
            @RequestParam("token") String tokenString,
            @RequestParam("novaSenha") String novaSenha,
            @RequestParam("confirmarSenha") String confirmarSenha,
            RedirectAttributes redirectAttributes) {

        // Valida se as senhas coincidem
        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "As senhas não coincidem.");
            return "redirect:/recuperar-senha/redefinir?token=" + tokenString;
        }

        // Valida tamanho mínimo da senha
        if (novaSenha.length() < 6) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A senha deve ter no mínimo 6 caracteres.");
            return "redirect:/recuperar-senha/redefinir?token=" + tokenString;
        }

        // Busca e valida o token
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenString);

        if (tokenOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Token inválido ou expirado.");
            return "redirect:/login";
        }

        PasswordResetToken token = tokenOpt.get();

        // Verifica se o token é válido
        if (!token.isValido()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                token.isUsado() ? "Este token já foi utilizado." : "Este token expirou. Solicite uma nova recuperação de senha.");
            return "redirect:/login";
        }

        Usuario usuario = token.getUsuario();

        // Atualiza a senha
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // Marca o token como usado
        token.setUsado(true);
        tokenRepository.save(token);

        redirectAttributes.addFlashAttribute("mensagemSucesso",
            "Senha redefinida com sucesso! Faça login com sua nova senha.");

        return "redirect:/login";
    }
}

