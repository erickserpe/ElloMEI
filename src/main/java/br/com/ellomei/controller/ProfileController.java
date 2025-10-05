package br.com.ellomei.controller;

import br.com.ellomei.config.security.CurrentUser;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.service.AssinaturaService;
import br.com.ellomei.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsável pelo gerenciamento do perfil do usuário.
 * 
 * Permite visualizar e editar dados pessoais, gerenciar assinatura
 * e configurações da conta.
 */
@Controller
@RequestMapping("/perfil")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AssinaturaService assinaturaService;

    /**
     * Exibe a página de perfil do usuário.
     * 
     * Mostra dados pessoais, informações da assinatura ativa e opções de configuração.
     * 
     * @param usuario Usuário autenticado (injetado via @CurrentUser)
     * @param model Modelo para passar dados para a view
     * @return Nome do template Thymeleaf
     */
    @GetMapping
    public String viewProfile(@CurrentUser Usuario usuario, Model model) {
        // Busca a assinatura ativa para exibir os detalhes
        assinaturaService.buscarAssinaturaAtiva(usuario).ifPresent(assinatura -> {
            model.addAttribute("assinaturaAtiva", assinatura);
        });

        model.addAttribute("usuario", usuario);
        model.addAttribute("paginaAtual", "perfil");
        return "perfil/index";
    }

    /**
     * Exibe o formulário de edição de dados pessoais.
     * 
     * @param usuario Usuário autenticado
     * @param model Modelo para passar dados para a view
     * @return Nome do template Thymeleaf
     */
    @GetMapping("/editar")
    public String editProfileForm(@CurrentUser Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("paginaAtual", "perfil");
        return "perfil/form";
    }

    /**
     * Processa a atualização dos dados pessoais do usuário.
     * 
     * Este método garante que apenas campos permitidos sejam atualizados,
     * evitando alterações maliciosas em campos sensíveis (id, password, roles, plano).
     * 
     * @param usuarioForm Dados do formulário
     * @param result Resultado da validação
     * @param usuarioLogado Usuário autenticado (do SecurityContext)
     * @param attributes Atributos para mensagens flash
     * @return Redirecionamento para a página de perfil
     */
    @PostMapping("/editar")
    public String updateProfile(
            @Valid @ModelAttribute("usuario") Usuario usuarioForm,
            BindingResult result,
            @CurrentUser Usuario usuarioLogado,
            RedirectAttributes attributes) {

        // Se houver erros de validação, retorna ao formulário
        if (result.hasErrors()) {
            return "perfil/form";
        }

        try {
            // Atualiza apenas os campos permitidos de forma segura
            profileService.atualizarDadosPessoais(usuarioLogado, usuarioForm);

            attributes.addFlashAttribute("mensagemSucesso", "Dados atualizados com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro ao atualizar dados: " + e.getMessage());
        }

        return "redirect:/perfil";
    }

    /**
     * Exibe o formulário de alteração de senha.
     * 
     * @param model Modelo para passar dados para a view
     * @return Nome do template Thymeleaf
     */
    @GetMapping("/alterar-senha")
    public String changePasswordForm(Model model) {
        model.addAttribute("paginaAtual", "perfil");
        return "perfil/alterar-senha";
    }

    /**
     * Processa a alteração de senha do usuário.
     * 
     * @param senhaAtual Senha atual para validação
     * @param novaSenha Nova senha a ser definida
     * @param confirmacaoSenha Confirmação da nova senha
     * @param usuarioLogado Usuário autenticado
     * @param attributes Atributos para mensagens flash
     * @return Redirecionamento para a página de perfil
     */
    @PostMapping("/alterar-senha")
    public String changePassword(
            @RequestParam("senhaAtual") String senhaAtual,
            @RequestParam("novaSenha") String novaSenha,
            @RequestParam("confirmacaoSenha") String confirmacaoSenha,
            @CurrentUser Usuario usuarioLogado,
            RedirectAttributes attributes) {

        // Valida se as senhas coincidem
        if (!novaSenha.equals(confirmacaoSenha)) {
            attributes.addFlashAttribute("mensagemErro", "A nova senha e a confirmação não coincidem.");
            return "redirect:/perfil/alterar-senha";
        }

        // Valida tamanho mínimo da senha
        if (novaSenha.length() < 6) {
            attributes.addFlashAttribute("mensagemErro", "A nova senha deve ter no mínimo 6 caracteres.");
            return "redirect:/perfil/alterar-senha";
        }

        // Tenta atualizar a senha
        boolean sucesso = profileService.atualizarSenha(usuarioLogado, senhaAtual, novaSenha);

        if (sucesso) {
            attributes.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso!");
            return "redirect:/perfil";
        } else {
            attributes.addFlashAttribute("mensagemErro", "Senha atual incorreta.");
            return "redirect:/perfil/alterar-senha";
        }
    }
}

