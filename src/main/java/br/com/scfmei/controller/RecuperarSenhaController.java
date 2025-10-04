package br.com.scfmei.controller;

import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.UsuarioRepository;
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
     * 
     * IMPLEMENTAÇÃO SIMPLIFICADA: Em produção, envie um email com link único.
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
        
        // IMPLEMENTAÇÃO SIMPLIFICADA: Gera token mas não envia email
        // Em produção: envie email com link contendo o token
        String token = UUID.randomUUID().toString();
        
        // TODO: Salvar token em tabela com data de expiração
        // TODO: Enviar email com link: /recuperar-senha/redefinir?token={token}
        
        // Por enquanto, apenas loga o token (REMOVER EM PRODUÇÃO!)
        System.out.println("=== TOKEN DE RECUPERAÇÃO ===");
        System.out.println("Email: " + email);
        System.out.println("Token: " + token);
        System.out.println("Link: http://localhost:8080/recuperar-senha/redefinir?token=" + token);
        System.out.println("===========================");
        
        redirectAttributes.addFlashAttribute("mensagemSucesso", 
            "Se o email estiver cadastrado, você receberá instruções para redefinir sua senha.");
        redirectAttributes.addFlashAttribute("tokenDemo", token); // APENAS PARA DEMO
        
        return "redirect:/login";
    }

    /**
     * Exibe o formulário para redefinir a senha (com token).
     * 
     * IMPLEMENTAÇÃO SIMPLIFICADA: Não valida token.
     * Em produção: validar token e expiração.
     */
    @GetMapping("/recuperar-senha/redefinir")
    public String exibirFormularioRedefinicao(
            @RequestParam(value = "token", required = false) String token,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // TODO: Validar token e verificar expiração
        
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Token inválido ou expirado.");
            return "redirect:/login";
        }
        
        model.addAttribute("token", token);
        return "recuperar-senha/redefinir";
    }

    /**
     * Processa a redefinição de senha.
     * 
     * IMPLEMENTAÇÃO SIMPLIFICADA: Usa email diretamente.
     * Em produção: usar token para identificar usuário.
     */
    @PostMapping("/recuperar-senha/redefinir")
    public String redefinirSenha(
            @RequestParam("email") String email,
            @RequestParam("novaSenha") String novaSenha,
            @RequestParam("confirmarSenha") String confirmarSenha,
            RedirectAttributes redirectAttributes) {
        
        // Valida se as senhas coincidem
        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "As senhas não coincidem.");
            return "redirect:/recuperar-senha/redefinir?email=" + email;
        }

        // Valida tamanho mínimo da senha
        if (novaSenha.length() < 6) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A senha deve ter no mínimo 6 caracteres.");
            return "redirect:/recuperar-senha/redefinir?email=" + email;
        }

        // Busca usuário por email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();
        
        // Atualiza a senha
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        
        // TODO: Invalidar o token usado
        
        redirectAttributes.addFlashAttribute("mensagemSucesso", 
            "Senha redefinida com sucesso! Faça login com sua nova senha.");
        
        return "redirect:/login";
    }
}

