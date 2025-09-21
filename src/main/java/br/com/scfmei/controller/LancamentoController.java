package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.LancamentoFormDTO;
import br.com.scfmei.domain.StatusLancamento;
import br.com.scfmei.service.CategoriaDespesaService;
import br.com.scfmei.service.ContaService;
import br.com.scfmei.service.LancamentoService;
import br.com.scfmei.service.ContatoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {

    @Autowired
    private LancamentoService lancamentoService;
    @Autowired
    private ContaService contaService;
    @Autowired
    private CategoriaDespesaService categoriaService;
    @Autowired
    private ContatoService contatoService;

    @GetMapping
    public String listarLancamentos(@RequestParam(required = false) LocalDate dataInicio,
                                    @RequestParam(required = false) LocalDate dataFim,
                                    @RequestParam(required = false) Long contaId,
                                    @RequestParam(required = false) Long contatoId,
                                    @RequestParam(required = false) StatusLancamento status, // <-- NOVO PARÂMETRO
                                    Model model) {

        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, contatoId, null, null, null, null, status); // <-- Passando para o service
        model.addAttribute("listaDeLancamentos", lancamentos);

        // ... (código para adicionar listas de contas e pessoas)

        // Devolve os filtros selecionados para a view
        model.addAttribute("dataInicioSel", dataInicio);
        model.addAttribute("dataFimSel", dataFim);
        model.addAttribute("contaIdSel", contaId);
        model.addAttribute("pessoaIdSel", contatoId);
        model.addAttribute("statusSel", status); // <-- DEVOLVE O STATUS SELECIONADO

        return "lancamentos";
    }

    @GetMapping("/novo")
    public String mostrarFormularioDeNovoLancamento(Model model) {
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
        model.addAttribute("listaDePessoas", contatoService.buscarTodos());
        model.addAttribute("lancamentoForm", new LancamentoFormDTO());
        return "form-lancamento";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
        model.addAttribute("listaDePessoas", contatoService.buscarTodos());
        LancamentoFormDTO formDTO = lancamentoService.carregarOperacaoParaEdicao(id);
        model.addAttribute("lancamentoForm", formDTO);
        return "form-lancamento";
    }

    @PostMapping
    public String salvarLancamento(@ModelAttribute("lancamentoForm") LancamentoFormDTO lancamentoForm,
                                   @RequestParam("comprovanteFile") MultipartFile comprovanteFile) {
        lancamentoService.salvarOuAtualizarOperacao(lancamentoForm, comprovanteFile);
        return "redirect:/lancamentos";
    }

    @GetMapping("/excluir/{id}")
    public String excluirLancamento(@PathVariable Long id) {
        lancamentoService.excluirOperacao(id);
        return "redirect:/lancamentos";
    }
}