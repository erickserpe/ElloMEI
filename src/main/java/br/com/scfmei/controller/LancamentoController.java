package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.service.LancamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.domain.Pessoa;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.service.ContaService;
import br.com.scfmei.service.CategoriaDespesaService;
import br.com.scfmei.service.PessoaService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {

    @Autowired
    private LancamentoService lancamentoService;

    @GetMapping
    public String listarLancamentos(Model model) {
        List<Lancamento> lancamentos = lancamentoService.buscarTodos();
        model.addAttribute("listaDeLancamentos", lancamentos);
        return "lancamentos"; // Procurará por lancamentos.html
    }
    @GetMapping("/novo")
    public String mostrarFormularioDeNovoLancamento(Model model) {
        // 1. Carrega as listas de dados para os dropdowns do formulário
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());

        // 2. Envia um objeto Lancamento vazio para conectar ao formulário
        model.addAttribute("lancamento", new Lancamento());

        return "form-lancamento"; // Retorna o nome do arquivo HTML do formulário
    }

    @PostMapping
    public String salvarLancamento(@ModelAttribute Lancamento lancamento) {
        // O Spring "monta" o objeto Lancamento com as referências corretas para nós!
        lancamentoService.salvar(lancamento);
        return "redirect:/lancamentos";
    }
    // Adicione estas injeções
    @Autowired
    private ContaService contaService;
    @Autowired
    private CategoriaDespesaService categoriaService;
    @Autowired
    private PessoaService pessoaService;
}