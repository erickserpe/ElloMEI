package br.com.ellomei.controller;

import br.com.ellomei.config.security.CurrentUser;
import br.com.ellomei.domain.*;
import br.com.ellomei.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private ContaService contaService;
    @Autowired private CategoriaDespesaService categoriaService;
    @Autowired private ContatoService contatoService;

    @GetMapping
    public String listarLancamentos(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long contatoId, // Corrigido de pessoaId para contatoId
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean comNotaFiscal,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) StatusLancamento status,
            @PageableDefault(size = 20, sort = "data") Pageable pageable,
            @CurrentUser Usuario usuario, Model model) {

        // Busca os lançamentos agrupados com base nos filtros (com paginação)
        Page<LancamentoGrupoDTO> lancamentosAgrupadosPage = lancamentoService.buscarComFiltrosAgrupados(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario, pageable);
        model.addAttribute("lancamentosPage", lancamentosAgrupadosPage);
        // Mantém compatibilidade com HTML antigo
        model.addAttribute("listaDeLancamentos", lancamentosAgrupadosPage.getContent());

        // Calcula os totais para os KPIs da página (apenas da página atual)
        BigDecimal totalEntradas = lancamentosAgrupadosPage.getContent().stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA && l.getStatus() == StatusLancamento.PAGO)
                .map(LancamentoGrupoDTO::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = lancamentosAgrupadosPage.getContent().stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA && l.getStatus() == StatusLancamento.PAGO)
                .map(LancamentoGrupoDTO::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalEntradas", totalEntradas);
        model.addAttribute("totalSaidas", totalSaidas);

        // Carrega dados para os dropdowns dos filtros
        model.addAttribute("listaDeContas", contaService.buscarTodasPorUsuario(usuario));
        model.addAttribute("listaDePessoas", contatoService.buscarTodosPorUsuario(usuario));

        // Mantém os valores dos filtros selecionados na tela
        model.addAttribute("dataInicioSel", dataInicio);
        model.addAttribute("dataFimSel", dataFim);
        model.addAttribute("contaIdSel", contaId);
        model.addAttribute("contatoIdSel", contatoId);
        model.addAttribute("statusSel", status);

        return "lancamentos";
    }

    private void carregarDadosDoFormulario(Model model, Usuario usuario) {
        model.addAttribute("listaDeContas", contaService.buscarTodasPorUsuario(usuario));
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodasPorUsuario(usuario));
        model.addAttribute("listaDePessoas", contatoService.buscarTodosPorUsuario(usuario));
    }

    @GetMapping("/novo/entrada")
    public String mostrarFormularioDeNovaEntrada(Model model, @CurrentUser Usuario usuario) {
        carregarDadosDoFormulario(model, usuario);
        LancamentoFormDTO lancamentoForm = new LancamentoFormDTO();
        lancamentoForm.setTipo(TipoLancamento.ENTRADA);
        model.addAttribute("lancamentoForm", lancamentoForm);
        return "form-lancamento-entrada";
    }

    @GetMapping("/novo/saida")
    public String mostrarFormularioDeNovaSaida(Model model, @CurrentUser Usuario usuario) {
        carregarDadosDoFormulario(model, usuario);
        LancamentoFormDTO lancamentoForm = new LancamentoFormDTO();
        lancamentoForm.setTipo(TipoLancamento.SAIDA);
        model.addAttribute("lancamentoForm", lancamentoForm);
        return "form-lancamento-saida";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model, @CurrentUser Usuario usuario) {
        Lancamento lancamento = lancamentoService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));

        carregarDadosDoFormulario(model, usuario);
        LancamentoFormDTO lancamentoForm = lancamentoService.carregarOperacaoParaEdicao(id);
        model.addAttribute("lancamentoForm", lancamentoForm);

        // Route to the appropriate form based on transaction type
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            return "form-lancamento-entrada";
        } else {
            return "form-lancamento-saida";
        }
    }

    @PostMapping
    public String salvarLancamento(@ModelAttribute("lancamentoForm") LancamentoFormDTO lancamentoForm,
                                   @RequestParam(value = "comprovanteFiles", required = false) MultipartFile[] comprovanteFiles, @CurrentUser Usuario usuario) {
        lancamentoService.salvarOuAtualizarOperacao(lancamentoForm, comprovanteFiles, usuario);
        return "redirect:/lancamentos";
    }

    @DeleteMapping("/excluir/{id}")
    public String excluirLancamento(@PathVariable Long id, @CurrentUser Usuario usuario) {
        lancamentoService.excluirOperacao(id, usuario);
        return "redirect:/lancamentos";
    }

    @DeleteMapping("/comprovante/{comprovanteId}")
    public ResponseEntity<Void> excluirComprovante(@PathVariable Long comprovanteId, @CurrentUser Usuario usuario) {
        lancamentoService.excluirComprovante(comprovanteId, usuario);
        return ResponseEntity.ok().build();
    }
}