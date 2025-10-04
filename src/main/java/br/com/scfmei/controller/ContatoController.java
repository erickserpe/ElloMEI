// src/main/java/br/com/scfmei/controller/ContatoController.java
package br.com.scfmei.controller;

import br.com.scfmei.domain.Contato;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.ContatoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/contatoes")
public class ContatoController {

    @Autowired private ContatoService contatoService;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado."));
    }

    @GetMapping
    public String listarContatos(Model model, Principal principal, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Usuario usuario = getUsuarioLogado(principal);
        Page<Contato> contatosPage = contatoService.buscarTodosPorUsuario(usuario, pageable);
        model.addAttribute("contatosPage", contatosPage);
        // Mantém compatibilidade com HTML antigo
        model.addAttribute("listaDeContatos", contatosPage.getContent());
        return "contatos";
    }

    @GetMapping("/nova")
    public String mostrarFormularioDeNovoContato(Model model) {
        model.addAttribute("contato", new Contato());
        return "form-contato";
    }

    @PostMapping
    public String salvarContato(@Valid @ModelAttribute("contato") Contato contato, BindingResult result, Principal principal) {
        if (result.hasErrors()) {
            return "form-contato";
        }
        Usuario usuario = getUsuarioLogado(principal);
        contatoService.salvar(contato, usuario);
        return "redirect:/contatoes";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        Contato contato = contatoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Contato não encontrado"));
        model.addAttribute("contato", contato);
        return "form-contato";
    }

    @GetMapping("/excluir/{id}")
    public String excluirContato(@PathVariable Long id) {
        contatoService.excluirPorId(id);
        return "redirect:/contatoes";
    }
}