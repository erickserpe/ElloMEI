// src/main/java/br/com/scfmei/controller/ContaController.java
package br.com.scfmei.controller;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // Importante para segurança
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/contas")
public class ContaController {

    @Autowired private ContaService contaService;
    @Autowired private UsuarioRepository usuarioRepository;

    // Função auxiliar para pegar o usuário logado
    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado no banco de dados."));
    }

    @GetMapping
    public String listarContas(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        List<Conta> contas = contaService.buscarTodasPorUsuario(usuario);
        model.addAttribute("listaDeContas", contas);
        return "contas";
    }

    @GetMapping("/novo")
    public String mostrarFormularioDeCadastro(Model model) {
        model.addAttribute("conta", new Conta());
        return "form-conta";
    }

    @PostMapping
    public String salvarConta(@Valid @ModelAttribute("conta") Conta conta, BindingResult result, Principal principal) {
        if (result.hasErrors()) {
            return "form-conta";
        }
        Usuario usuario = getUsuarioLogado(principal);
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