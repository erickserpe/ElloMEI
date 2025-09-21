package br.com.scfmei.controller;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public String listarLancamentos(@RequestParam(required = false) LocalDate dataInicio,
                                    @RequestParam(required = false) LocalDate dataFim,
                                    // ... outros request params
                                    Principal principal, Model model) {
        Usuario usuario = getUsuarioLogado(principal);
        List<Lancamento> lancamentos = lancamentoService.buscarComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario);
        model.addAttribute("listaDeLancamentos", lancamentos);

        // Carrega apenas os dados do usuário logado para os filtros
        model.addAttribute("listaDeContas", contaService.buscarTodasPorUsuario(usuario));
        model.addAttribute("listaDePessoas", contatoService.buscarTodosPorUsuario(usuario));

        // ... devolve filtros selecionados para a view
        return "lancamentos";
    }

    private void carregarDadosDoFormulario(Model model, Usuario usuario) {
        model.addAttribute("listaDeContas", contaService.buscarTodasPorUsuario(usuario));
        model.addAttribute("listaDeCategorias", categoriaService.buscarTodasPorUsuario(usuario));
        model.addAttribute("listaDePessoas", contatoService.buscarTodosPorUsuario(usuario));
    }

    @GetMapping("/novo")
    public String mostrarFormularioDeNovoLancamento(Model model, Principal principal) {
        carregarDadosDoFormulario(model, getUsuarioLogado(principal));
        model.addAttribute("lancamentoForm", new LancamentoFormDTO());
        return "form-lancamento";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        Lancamento lancamento = lancamentoService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));

        if(!lancamento.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso negado.");
        }

        carregarDadosDoFormulario(model, usuario);
        model.addAttribute("lancamentoForm", lancamentoService.carregarOperacaoParaEdicao(id));
        return "form-lancamento";
    }

    @PostMapping
    public String salvarLancamento(@ModelAttribute("lancamentoForm") LancamentoFormDTO lancamentoForm,
                                   @RequestParam("comprovanteFile") MultipartFile comprovanteFile, Principal principal) {
        lancamentoService.salvarOuAtualizarOperacao(lancamentoForm, comprovanteFile, getUsuarioLogado(principal));
        return "redirect:/lancamentos";
    }

    @GetMapping("/excluir/{id}")
    public String excluirLancamento(@PathVariable Long id, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        Lancamento lancamento = lancamentoService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));

        if(!lancamento.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso negado.");
        }

        lancamentoService.excluirOperacao(id, usuario);
        return "redirect:/lancamentos";
    }
}