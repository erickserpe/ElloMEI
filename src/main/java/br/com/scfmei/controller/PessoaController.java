package br.com.scfmei.controller;

import br.com.scfmei.domain.Pessoa;
import br.com.scfmei.service.PessoaService;
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
    private PessoaService pessoaService;

    @GetMapping
    public String listarPessoas(Model model) {
        List<Pessoa> pessoas = pessoaService.buscarTodas();
        model.addAttribute("listaDePessoas", pessoas);
        return "pessoas";
    }

    @GetMapping("/nova")
    public String mostrarFormularioDeNovaPessoa(Model model) {
        model.addAttribute("pessoa", new Pessoa());
        return "form-pessoa";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        Optional<Pessoa> pessoaOpt = pessoaService.buscarPorId(id);
        if (pessoaOpt.isPresent()) {
            model.addAttribute("pessoa", pessoaOpt.get());
            return "form-pessoa";
        }
        return "redirect:/pessoas";
    }

    // --- ESTE É O ÚNICO MÉTODO @PostMapping QUE DEVEMOS TER ---
    @PostMapping
    public String salvarPessoa(@Valid @ModelAttribute("pessoa") Pessoa pessoa, BindingResult result) {
        if (result.hasErrors()) {
            return "form-pessoa";
        }
        pessoaService.salvar(pessoa);
        return "redirect:/pessoas";
    }

    @GetMapping("/excluir/{id}")
    public String excluirPessoa(@PathVariable Long id) {
        pessoaService.excluirPorId(id);
        return "redirect:/pessoas";
    }
}