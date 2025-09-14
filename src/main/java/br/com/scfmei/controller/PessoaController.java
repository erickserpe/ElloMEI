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
@RequestMapping("/pessoas")
public class PessoaController {

    @Autowired
    private ContatoService contatoService;

    @GetMapping
    public String listarPessoas(Model model) {
        List<Contato> contatoes = contatoService.buscarTodas();
        model.addAttribute("listaDePessoas", contatoes);
        return "pessoas";
    }

    @GetMapping("/nova")
    public String mostrarFormularioDeNovaPessoa(Model model) {
        model.addAttribute("pessoa", new Contato());
        return "form-pessoa";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        Optional<Contato> pessoaOpt = contatoService.buscarPorId(id);
        if (pessoaOpt.isPresent()) {
            model.addAttribute("pessoa", pessoaOpt.get());
            return "form-pessoa";
        }
        return "redirect:/pessoas";
    }

    // --- ESTE É O ÚNICO MÉTODO @PostMapping QUE DEVEMOS TER ---
    @PostMapping
    public String salvarPessoa(@Valid @ModelAttribute("pessoa") Contato contato, BindingResult result) {
        if (result.hasErrors()) {
            return "form-pessoa";
        }
        contatoService.salvar(contato);
        return "redirect:/pessoas";
    }

    @GetMapping("/excluir/{id}")
    public String excluirPessoa(@PathVariable Long id) {
        contatoService.excluirPorId(id);
        return "redirect:/pessoas";
    }
}