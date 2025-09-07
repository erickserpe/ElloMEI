package br.com.scfmei.controller;

import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.Pessoa;
import br.com.scfmei.service.CategoriaDespesaService;
import br.com.scfmei.service.ContaService;
import br.com.scfmei.service.LancamentoService;
import br.com.scfmei.service.PessoaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {

    // --- INJEÇÕES DE DEPENDÊNCIA (BOA PRÁTICA É DEIXÁ-LAS JUNTAS NO TOPO) ---
    @Autowired
    private LancamentoService lancamentoService;
    @Autowired
    private ContaService contaService;
    @Autowired
    private CategoriaDespesaService categoriaService;
    @Autowired
    private PessoaService pessoaService;


    // --- MÉTODOS DO CONTROLLER ---

    @GetMapping
    public String listarLancamentos(@RequestParam(required = false) LocalDate dataInicio,
                                    @RequestParam(required = false) LocalDate dataFim,
                                    Model model) {
        List<Lancamento> lancamentos;

        // Se as duas datas foram informadas, filtra a busca por período
        if (dataInicio != null && dataFim != null) {
            lancamentos = lancamentoService.buscarPorPeriodo(dataInicio, dataFim);
        } else {
            // Se não, busca todos os lançamentos
            lancamentos = lancamentoService.buscarTodos();
        }

        model.addAttribute("listaDeLancamentos", lancamentos);
        return "lancamentos";
    }

    @GetMapping("/novo")
    public String mostrarFormularioDeNovoLancamento(Model model) {
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());
        model.addAttribute("lancamento", new Lancamento());
        return "form-lancamento";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());

        Optional<Lancamento> lancamentoOpt = lancamentoService.buscarPorId(id);
        if (lancamentoOpt.isPresent()) {
            model.addAttribute("lancamento", lancamentoOpt.get());
        } else {
            return "redirect:/lancamentos";
        }
        return "form-lancamento";
    }

    // --- MÉTODO SALVAR ÚNICO E CORRETO, COM VALIDAÇÃO ---
    @PostMapping
    public String salvarLancamento(@Valid @ModelAttribute("lancamento") Lancamento lancamento, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Se houver erros, recarregamos as listas para os dropdowns e voltamos ao formulário
            model.addAttribute("listaDeContas", contaService.buscarTodas());
            model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
            model.addAttribute("listaDePessoas", pessoaService.buscarTodas());
            return "form-lancamento";
        }

        lancamentoService.salvar(lancamento);
        return "redirect:/lancamentos";
    }

    @GetMapping("/excluir/{id}")
    public String excluirLancamento(@PathVariable Long id) {
        lancamentoService.excluirPorId(id);
        return "redirect:/lancamentos";
    }
}