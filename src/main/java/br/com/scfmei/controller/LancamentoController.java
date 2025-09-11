package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.LancamentoFormDTO;
import br.com.scfmei.service.CategoriaDespesaService;
import br.com.scfmei.service.ContaService;
import br.com.scfmei.service.LancamentoService;
import br.com.scfmei.service.PessoaService;
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
    private PessoaService pessoaService;

    @GetMapping
    public String listarLancamentos(@RequestParam(required = false) LocalDate dataInicio,
                                    @RequestParam(required = false) LocalDate dataFim,
                                    // Adicionando filtros que faltavam para consistência
                                    @RequestParam(required = false) Long contaId,
                                    @RequestParam(required = false) Long pessoaId,
                                    Model model) {

        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, pessoaId);
        model.addAttribute("listaDeLancamentos", lancamentos);

        // Adiciona listas para os dropdowns de filtro
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());

        // Devolve os filtros selecionados para a view
        model.addAttribute("dataInicioSel", dataInicio);
        model.addAttribute("dataFimSel", dataFim);
        model.addAttribute("contaIdSel", contaId);
        model.addAttribute("pessoaIdSel", pessoaId);

        return "lancamentos";
    }

    @GetMapping("/novo")
    public String mostrarFormularioDeNovoLancamento(Model model) {
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());
        model.addAttribute("lancamentoForm", new LancamentoFormDTO());
        return "form-lancamento";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        model.addAttribute("listaDeContas", contaService.buscarTodas());
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodas());
        model.addAttribute("listaDePessoas", pessoaService.buscarTodas());

        // Chama o novo serviço que prepara o DTO para edição
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