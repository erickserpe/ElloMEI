package br.com.ellomei.controller;

import br.com.ellomei.domain.Usuario;
import br.com.ellomei.exception.UsuarioDuplicadoException;
import br.com.ellomei.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsável pelo registro de novos usuários.
 *
 * Implementa validações completas:
 * - Validação de formato (Bean Validation)
 * - Validação de senha forte
 * - Validação de email real (DNS/MX)
 * - Validação de duplicidade (username e email)
 *
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Controller
public class RegistroController {

    private static final Logger logger = LoggerFactory.getLogger(RegistroController.class);

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Exibe o formulário de registro.
     */
    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    /**
     * Processa o formulário de registro com validações completas.
     *
     * Validações aplicadas:
     * 1. Bean Validation (@Valid) - formato, tamanho, obrigatoriedade
     * 2. Senha forte - complexidade e segurança
     * 3. Email real - DNS e registros MX
     * 4. Duplicidade - username e email únicos
     */
    @PostMapping("/registro")
    public String registrarUsuario(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        logger.info("📝 Tentativa de registro de novo usuário: {}", usuario.getUsername());

        // Validação 1: Erros de Bean Validation (formato, tamanho, etc)
        if (result.hasErrors()) {
            logger.warn("⚠️ Erros de validação no formulário de registro:");
            for (FieldError error : result.getFieldErrors()) {
                logger.warn("  - Campo '{}': {}", error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("mensagemErro", "Por favor, corrija os erros no formulário.");
            return "registro";
        }

        try {
            // Validação 2: Duplicidade e salvamento
            usuarioService.salvar(usuario);

            logger.info("✅ Usuário {} registrado com sucesso!", usuario.getUsername());

            // Redireciona para o login com mensagem de sucesso
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                "🎉 Cadastro realizado com sucesso! Verifique seu email para confirmar o cadastro e faça login.");

            return "redirect:/login";

        } catch (UsuarioDuplicadoException e) {
            // Erro de duplicidade (username ou email já existe)
            logger.warn("⚠️ Erro de duplicidade: {}", e.getMessage());

            // Adiciona erro ao campo apropriado
            if (e.getMessage().contains("usuário")) {
                result.rejectValue("username", "error.usuario", e.getMessage());
            } else if (e.getMessage().contains("email")) {
                result.rejectValue("email", "error.usuario", e.getMessage());
            }

            model.addAttribute("mensagemErro", e.getMessage());
            return "registro";

        } catch (Exception e) {
            // Erro inesperado
            logger.error("❌ Erro inesperado ao registrar usuário: {}", e.getMessage(), e);

            model.addAttribute("mensagemErro",
                "Ocorreu um erro ao processar seu cadastro. Por favor, tente novamente.");
            return "registro";
        }
    }
}