package br.com.scfmei.controller;

import br.com.scfmei.domain.Contato;
import br.com.scfmei.service.ContatoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/contatoes") // URL base para todos os métodos deste controller
public class ContatoController {

    @Autowired
    private ContatoService contatoService;

    @GetMapping
    public String listarContatos(Model model) { // Nomenclatura atualizada
        List<Contato> contatos = contatoService.buscarTodos();
        model.addAttribute("listaDeContatos", contatos); // Nomenclatura do atributo atualizada
        return "contatos"; // Aponta para o arquivo contatos.html
    }

    @GetMapping("/nova")
    public String mostrarFormularioDeNovoContato(Model model) { // Nomenclatura atualizada
        model.addAttribute("contato", new Contato()); // Nomenclatura do objeto atualizada
        return "form-contato"; // Aponta para o arquivo form-contato.html
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        Optional<Contato> contatoOpt = contatoService.buscarPorId(id); // Nomenclatura da variável atualizada
        if (contatoOpt.isPresent()) {
            model.addAttribute("contato", contatoOpt.get()); // Nomenclatura do objeto atualizada
            return "form-contato"; // Aponta para o arquivo form-contato.html
        }
        return "redirect:/contatoes"; // Redirecionamento corrigido
    }

    @PostMapping
    public String salvarContato(@Valid @ModelAttribute("contato") Contato contato, BindingResult result) { // Nomenclatura atualizada
        if (result.hasErrors()) {
            return "form-contato"; // Aponta para o arquivo form-contato.html em caso de erro
        }
        contatoService.salvar(contato);
        return "redirect:/contatoes"; // Redirecionamento corrigido
    }

    @GetMapping("/excluir/{id}")
    public String excluirContato(@PathVariable Long id) { // Nomenclatura atualizada
        contatoService.excluirPorId(id);
        return "redirect:/contatoes"; // Redirecionamento corrigido
    }
}