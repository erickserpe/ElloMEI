package br.com.scfmei.service;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository lancamentoRepository;
    @Autowired
    private ContaService contaService;
    @Autowired
    private FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<Lancamento> buscarTodos() {
        return lancamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Lancamento> buscarPorId(Long id) {
        return lancamentoRepository.findById(id);
    }

    @Transactional
    public void salvarOuAtualizarOperacao(LancamentoFormDTO form, MultipartFile comprovanteFile) {
        // Se o form já tem um grupoOperacao, significa que é uma edição.
        if (form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank()) {
            // Primeiro, excluímos a operação antiga (revertendo os saldos)
            excluirOperacaoPorGrupo(form.getGrupoOperacao());
        }

        // Agora, criamos a nova operação (seja ela nova ou a versão atualizada da antiga)
        salvarNovaOperacao(form, comprovanteFile);
    }

    private void salvarNovaOperacao(LancamentoFormDTO form, MultipartFile comprovanteFile) {
        String grupoOperacao = UUID.randomUUID().toString();
        String comprovantePath = null;

        if (comprovanteFile != null && !comprovanteFile.isEmpty()) {
            comprovantePath = fileStorageService.storeFile(comprovanteFile);
        } else if (form.getGrupoOperacao() != null) {
            // Se não enviou arquivo novo, mas é uma edição, mantém o caminho do comprovante antigo
            comprovantePath = form.getComprovantePath();
        }

        for (PagamentoDTO pagamento : form.getPagamentos()) {
            if (pagamento.getValor() == null || pagamento.getConta() == null) {
                continue;
            }
            Lancamento lancamento = new Lancamento();
            lancamento.setDescricao(form.getDescricao());
            lancamento.setData(form.getData());
            lancamento.setTipo(form.getTipo());
            lancamento.setCategoriaDespesa(form.getCategoriaDespesa());
            lancamento.setPessoa(form.getPessoa());
            lancamento.setComNotaFiscal(form.getComNotaFiscal());
            lancamento.setGrupoOperacao(grupoOperacao);
            lancamento.setComprovantePath(comprovantePath);
            Conta conta = contaService.buscarPorId(pagamento.getConta())
                    .orElseThrow(() -> new RuntimeException("Conta não encontrada!"));
            lancamento.setConta(conta);
            lancamento.setValor(pagamento.getValor());
            aplicarLancamentoNaConta(lancamento);
            lancamentoRepository.save(lancamento);
        }
    }

    @Transactional(readOnly = true)
    public LancamentoFormDTO carregarOperacaoParaEdicao(Long lancamentoId) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        String grupoOperacao = umLancamentoDoGrupo.getGrupoOperacao();

        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacao(grupoOperacao);

        LancamentoFormDTO form = new LancamentoFormDTO();
        form.setGrupoOperacao(umLancamentoDoGrupo.getGrupoOperacao());
        form.setDescricao(umLancamentoDoGrupo.getDescricao());
        form.setData(umLancamentoDoGrupo.getData());
        form.setTipo(umLancamentoDoGrupo.getTipo());
        form.setCategoriaDespesa(umLancamentoDoGrupo.getCategoriaDespesa());
        form.setPessoa(umLancamentoDoGrupo.getPessoa());
        form.setComNotaFiscal(umLancamentoDoGrupo.getComNotaFiscal());
        form.setComprovantePath(umLancamentoDoGrupo.getComprovantePath());

        List<PagamentoDTO> pagamentos = lancamentosDoGrupo.stream().map(l -> {
            PagamentoDTO dto = new PagamentoDTO();
            dto.setConta(l.getConta().getId());
            dto.setValor(l.getValor());
            return dto;
        }).collect(Collectors.toList());
        form.setPagamentos(pagamentos);

        return form;
    }

    @Transactional
    public void excluirOperacao(Long lancamentoId) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        excluirOperacaoPorGrupo(umLancamentoDoGrupo.getGrupoOperacao());
    }

    private void excluirOperacaoPorGrupo(String grupoOperacao) {
        if (grupoOperacao == null || grupoOperacao.isBlank()) {
            return;
        }
        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacao(grupoOperacao);
        for (Lancamento lancamento : lancamentosDoGrupo) {
            reverterLancamentoNaConta(lancamento);
            lancamentoRepository.delete(lancamento);
        }
    }

    public List<Lancamento> buscarComFiltros(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId) {
        if (dataInicio == null && dataFim == null && contaId == null && pessoaId == null) {
            return buscarTodos();
        }
        return lancamentoRepository.findComFiltrosCompletos(dataInicio, dataFim, contaId, pessoaId);
    }

    private void aplicarLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        }
        contaService.salvar(conta);
    }

    private void reverterLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        }
        contaService.salvar(conta);
    }
}