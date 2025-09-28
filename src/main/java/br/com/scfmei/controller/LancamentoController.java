package br.com.scfmei.controller;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private ContaService contaService;
    @Autowired private CategoriaDespesaService categoriaService;
    @Autowired private ContatoService contatoService;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado."));
    }

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
            Principal principal, Model model) {

        Usuario usuario = getUsuarioLogado(principal);

        // Busca os lançamentos agrupados com base nos filtros
        List<LancamentoGrupoDTO> lancamentosAgrupados = lancamentoService.buscarComFiltrosAgrupados(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario);
        model.addAttribute("listaDeLancamentos", lancamentosAgrupados);

        // Calcula os totais para os KPIs da página
        BigDecimal totalEntradas = lancamentosAgrupados.stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA && l.getStatus() == StatusLancamento.PAGO)
                .map(LancamentoGrupoDTO::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = lancamentosAgrupados.stream()
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
    public String mostrarFormularioDeNovaEntrada(Model model, Principal principal) {
        carregarDadosDoFormulario(model, getUsuarioLogado(principal));
        LancamentoFormDTO lancamentoForm = new LancamentoFormDTO();
        lancamentoForm.setTipo(TipoLancamento.ENTRADA);
        model.addAttribute("lancamentoForm", lancamentoForm);
        return "form-lancamento-entrada";
    }

    @GetMapping("/novo/saida")
    public String mostrarFormularioDeNovaSaida(Model model, Principal principal) {
        carregarDadosDoFormulario(model, getUsuarioLogado(principal));
        LancamentoFormDTO lancamentoForm = new LancamentoFormDTO();
        lancamentoForm.setTipo(TipoLancamento.SAIDA);
        model.addAttribute("lancamentoForm", lancamentoForm);
        return "form-lancamento-saida";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
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
                                   @RequestParam(value = "comprovanteFiles", required = false) MultipartFile[] comprovanteFiles, Principal principal) {
        lancamentoService.salvarOuAtualizarOperacao(lancamentoForm, comprovanteFiles, getUsuarioLogado(principal));
        return "redirect:/lancamentos";
    }

    @DeleteMapping("/excluir/{id}")
    public String excluirLancamento(@PathVariable Long id, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        lancamentoService.excluirOperacao(id, usuario);
        return "redirect:/lancamentos";
    }

    @DeleteMapping("/comprovante/{comprovanteId}")
    public ResponseEntity<Void> excluirComprovante(@PathVariable Long comprovanteId, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        lancamentoService.excluirComprovante(comprovanteId, usuario);
        return ResponseEntity.ok().build();
    }
}