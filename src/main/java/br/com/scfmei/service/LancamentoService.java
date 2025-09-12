package br.com.scfmei.service;

import br.com.scfmei.domain.*;
        import br.com.scfmei.repository.ComprovanteRepository;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LancamentoService {

    @Autowired private LancamentoRepository lancamentoRepository;
    @Autowired private ComprovanteRepository comprovanteRepository;
    @Autowired private ContaService contaService;
    @Autowired private FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<Lancamento> buscarTodos() { return lancamentoRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Lancamento> buscarPorId(Long id) { return lancamentoRepository.findById(id); }

    public void salvarOuAtualizarOperacao(LancamentoFormDTO form, MultipartFile comprovanteFile) {
        if (form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank()) {
            excluirOperacaoPorGrupo(form.getGrupoOperacao());
        }
        salvarNovaOperacao(form, comprovanteFile);
    }

    private void salvarNovaOperacao(LancamentoFormDTO form, MultipartFile comprovanteFile) {
        String grupoOperacao = UUID.randomUUID().toString();
        List<String> comprovantePaths = new ArrayList<>();
        if (comprovanteFile != null && !comprovanteFile.isEmpty()) {
            comprovantePaths = fileStorageService.storeFile(comprovanteFile);
        } else if (form.getGrupoOperacao() != null && form.getComprovantes() != null && !form.getComprovantes().isEmpty()) {
            comprovantePaths = form.getComprovantes().stream().map(Comprovante::getPathArquivo).collect(Collectors.toList());
        }

        for (PagamentoDTO pagamento : form.getPagamentos()) {
            if (pagamento.getValor() == null || pagamento.getConta() == null) continue;
            Lancamento lancamento = new Lancamento();
            lancamento.setDescricao(form.getDescricao());
            lancamento.setData(form.getData());
            lancamento.setTipo(form.getTipo());
            lancamento.setCategoriaDespesa(form.getCategoriaDespesa());
            lancamento.setPessoa(form.getPessoa());
            lancamento.setComNotaFiscal(form.getComNotaFiscal());
            lancamento.setGrupoOperacao(grupoOperacao);
            Conta conta = contaService.buscarPorId(pagamento.getConta()).orElseThrow(() -> new RuntimeException("Conta não encontrada!"));
            lancamento.setConta(conta);
            lancamento.setValor(pagamento.getValor());
            Lancamento lancamentoSalvo = lancamentoRepository.save(lancamento);
            if (!comprovantePaths.isEmpty()) {
                for (String path : comprovantePaths) {
                    Comprovante comprovante = new Comprovante();
                    comprovante.setPathArquivo(path);
                    comprovante.setLancamento(lancamentoSalvo);
                    comprovanteRepository.save(comprovante);
                }
            }
            aplicarLancamentoNaConta(lancamentoSalvo);
        }
    }

    @Transactional(readOnly = true)
    public LancamentoFormDTO carregarOperacaoParaEdicao(Long lancamentoId) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        String grupoOperacao = umLancamentoDoGrupo.getGrupoOperacao();
        if (grupoOperacao == null) { grupoOperacao = umLancamentoDoGrupo.getId().toString(); }
        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacao(grupoOperacao);
        if(lancamentosDoGrupo.isEmpty()){ lancamentosDoGrupo.add(umLancamentoDoGrupo); }
        LancamentoFormDTO form = new LancamentoFormDTO();
        form.setGrupoOperacao(umLancamentoDoGrupo.getGrupoOperacao());
        form.setDescricao(umLancamentoDoGrupo.getDescricao());
        form.setData(umLancamentoDoGrupo.getData());
        form.setTipo(umLancamentoDoGrupo.getTipo());
        form.setCategoriaDespesa(umLancamentoDoGrupo.getCategoriaDespesa());
        form.setPessoa(umLancamentoDoGrupo.getPessoa());
        form.setComNotaFiscal(umLancamentoDoGrupo.getComNotaFiscal());
        form.setComprovantes(umLancamentoDoGrupo.getComprovantes());
        List<PagamentoDTO> pagamentos = lancamentosDoGrupo.stream().map(l -> {
            PagamentoDTO dto = new PagamentoDTO();
            dto.setConta(l.getConta().getId());
            dto.setValor(l.getValor());
            return dto;
        }).collect(Collectors.toList());
        form.setPagamentos(pagamentos);
        return form;
    }

    public void excluirOperacao(Long lancamentoId) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        String grupoOperacao = umLancamentoDoGrupo.getGrupoOperacao();
        if (grupoOperacao == null || grupoOperacao.isBlank()) {
            reverterLancamentoNaConta(umLancamentoDoGrupo);
            lancamentoRepository.delete(umLancamentoDoGrupo);
        } else {
            excluirOperacaoPorGrupo(grupoOperacao);
        }
    }

    private void excluirOperacaoPorGrupo(String grupoOperacao) {
        if (grupoOperacao == null || grupoOperacao.isBlank()) return;
        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacao(grupoOperacao);
        for (Lancamento lancamento : lancamentosDoGrupo) {
            reverterLancamentoNaConta(lancamento);
            lancamentoRepository.delete(lancamento);
        }
    }

    @Transactional(readOnly = true)
    public List<Lancamento> buscarComFiltros(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId, TipoLancamento tipo, Boolean comNotaFiscal) {
        return lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, pessoaId, tipo, comNotaFiscal);
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