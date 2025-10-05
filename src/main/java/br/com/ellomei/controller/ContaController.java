// src/main/java/br/com/ellomei/controller/ContaController.java
package br.com.ellomei.controller;

import br.com.ellomei.config.security.CurrentUser;
import br.com.ellomei.domain.Conta;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contas")
public class ContaController {

    @Autowired private ContaService contaService;

    @GetMapping
    public String listarContas(Model model, @CurrentUser Usuario usuario, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Conta> contasPage = contaService.buscarTodasPorUsuario(usuario, pageable);
        model.addAttribute("contasPage", contasPage);
        // Mantém compatibilidade com HTML antigo
        model.addAttribute("listaDeContas", contasPage.getContent());
        return "contas";
    }

    @GetMapping("/novo")
    public String mostrarFormularioDeCadastro(Model model) {
        model.addAttribute("conta", new Conta());
        return "form-conta";
    }

    @PostMapping
    public String salvarConta(@Valid @ModelAttribute("conta") Conta conta, BindingResult result, @CurrentUser Usuario usuario) {
        if (result.hasErrors()) {
            return "form-conta";
        }
        contaService.salvar(conta, usuario);
        return "redirect:/contas";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        Conta conta = contaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        model.addAttribute("conta", conta);
        return "form-conta";
    }

    @GetMapping("/excluir/{id}")
    public String excluirConta(@PathVariable Long id) {
        contaService.excluirPorId(id);
        return "redirect:/contas";
    }
}