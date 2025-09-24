package br.com.scfmei.service;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.ComprovanteRepository;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public Optional<Lancamento> buscarPorId(Long id) { return lancamentoRepository.findById(id); }

    public void salvarOuAtualizarOperacao(LancamentoFormDTO form, MultipartFile comprovanteFile, Usuario usuario) {
        if (form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank()) {
            excluirOperacaoPorGrupo(form.getGrupoOperacao(), usuario);
        }
        salvarNovaOperacao(form, comprovanteFile, usuario);
    }

    private void salvarNovaOperacao(LancamentoFormDTO form, MultipartFile comprovanteFile, Usuario usuario) {
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
            lancamento.setContato(form.getContato());
            lancamento.setComNotaFiscal(form.getComNotaFiscal());
            lancamento.setGrupoOperacao(grupoOperacao);
            lancamento.setStatus(form.getStatus());
            lancamento.setUsuario(usuario);

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

            if (lancamentoSalvo.getStatus() == StatusLancamento.PAGO) {
                aplicarLancamentoNaConta(lancamentoSalvo);
            }
        }
    }

    @Transactional(readOnly = true)
    public LancamentoFormDTO carregarOperacaoParaEdicao(Long lancamentoId) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        String grupoOperacao = umLancamentoDoGrupo.getGrupoOperacao();
        if (grupoOperacao == null) { grupoOperacao = umLancamentoDoGrupo.getId().toString(); }
        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacaoAndUsuario(grupoOperacao, umLancamentoDoGrupo.getUsuario());
        if(lancamentosDoGrupo.isEmpty()){ lancamentosDoGrupo.add(umLancamentoDoGrupo); }

        LancamentoFormDTO form = new LancamentoFormDTO();
        form.setGrupoOperacao(umLancamentoDoGrupo.getGrupoOperacao());
        form.setDescricao(umLancamentoDoGrupo.getDescricao());
        form.setData(umLancamentoDoGrupo.getData());
        form.setTipo(umLancamentoDoGrupo.getTipo());
        form.setCategoriaDespesa(umLancamentoDoGrupo.getCategoriaDespesa());
        form.setContato(umLancamentoDoGrupo.getContato());
        form.setComNotaFiscal(umLancamentoDoGrupo.getComNotaFiscal());
        form.setComprovantes(umLancamentoDoGrupo.getComprovantes());
        form.setStatus(umLancamentoDoGrupo.getStatus());

        List<PagamentoDTO> pagamentos = lancamentosDoGrupo.stream().map(l -> {
            PagamentoDTO dto = new PagamentoDTO();
            dto.setConta(l.getConta().getId());
            dto.setValor(l.getValor());
            return dto;
        }).collect(Collectors.toList());
        form.setPagamentos(pagamentos);
        return form;
    }

    public void excluirOperacao(Long lancamentoId, Usuario usuario) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        if (!umLancamentoDoGrupo.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso negado.");
        }

        String grupoOperacao = umLancamentoDoGrupo.getGrupoOperacao();
        if (grupoOperacao == null || grupoOperacao.isBlank()) {
            if (umLancamentoDoGrupo.getStatus() == StatusLancamento.PAGO) {
                reverterLancamentoNaConta(umLancamentoDoGrupo);
            }
            lancamentoRepository.delete(umLancamentoDoGrupo);
        } else {
            excluirOperacaoPorGrupo(grupoOperacao, usuario);
        }
    }

    private void excluirOperacaoPorGrupo(String grupoOperacao, Usuario usuario) {
        if (grupoOperacao == null || grupoOperacao.isBlank()) return;
        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacaoAndUsuario(grupoOperacao, usuario);
        for (Lancamento lancamento : lancamentosDoGrupo) {
            if (lancamento.getStatus() == StatusLancamento.PAGO) {
                reverterLancamentoNaConta(lancamento);
            }
            lancamentoRepository.delete(lancamento);
        }
    }

    @Transactional(readOnly = true)
    public List<Lancamento> buscarComFiltros(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, TipoLancamento tipo, Long categoriaId, Boolean comNotaFiscal, String descricao, StatusLancamento status, Usuario usuario) {
        return lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario);
    }

    @Transactional(readOnly = true)
    public List<Lancamento> buscarContasAPagarPorUsuario(Usuario usuario) {
        return lancamentoRepository.findByStatusAndUsuarioOrderByDataAsc(StatusLancamento.A_PAGAR, usuario);
    }

    public void pagarConta(Long lancamentoId, Usuario usuario) {
        Lancamento lancamento = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        if (!lancamento.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso negado.");
        }

        if (lancamento.getStatus() == StatusLancamento.A_PAGAR) {
            lancamento.setStatus(StatusLancamento.PAGO);
            aplicarLancamentoNaConta(lancamento);
            lancamentoRepository.save(lancamento);
        }
    }

    private void aplicarLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        }
        // CORREÇÃO AQUI
        contaService.salvar(conta, lancamento.getUsuario());
    }

    private void reverterLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        }
        // CORREÇÃO AQUI
        contaService.salvar(conta, lancamento.getUsuario());
    }
}