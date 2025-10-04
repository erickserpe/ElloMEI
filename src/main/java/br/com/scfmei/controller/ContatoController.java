// src/main/java/br/com/scfmei/controller/ContatoController.java
package br.com.scfmei.controller;

import br.com.scfmei.config.security.CurrentUser;
import br.com.scfmei.domain.Contato;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.service.ContatoService;
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
@RequestMapping("/contatoes")
public class ContatoController {

    @Autowired private ContatoService contatoService;

    @GetMapping
    public String listarContatos(Model model, @CurrentUser Usuario usuario, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
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
    public String salvarContato(@Valid @ModelAttribute("contato") Contato contato, BindingResult result, @CurrentUser Usuario usuario) {
        if (result.hasErrors()) {
            return "form-contato";
        }
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