package br.com.scfmei.service;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.ComprovanteRepository;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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

    @Transactional
    public void excluirComprovante(Long comprovanteId, Usuario usuario) {
        Comprovante comprovante = comprovanteRepository.findById(comprovanteId)
                .orElseThrow(() -> new RuntimeException("Comprovante não encontrado!"));

        // Security Check: Ensure the user owns the transaction associated with the attachment
        if (!comprovante.getLancamento().getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso negado para excluir este comprovante.");
        }

        // The @OneToMany(orphanRemoval=true) on Lancamento entity will handle the deletion
        comprovante.getLancamento().getComprovantes().remove(comprovante);
        comprovanteRepository.delete(comprovante);
    }

    public void salvarOuAtualizarOperacao(LancamentoFormDTO form, MultipartFile comprovanteFile, Usuario usuario) {
        String grupoOperacao = (form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank())
                ? form.getGrupoOperacao()
                : UUID.randomUUID().toString();

        // If it's an existing operation, we need to delete the old lancamentos before recreating them
        if (form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank()) {
            List<Lancamento> lancamentosAntigos = lancamentoRepository.findByGrupoOperacaoAndUsuario(form.getGrupoOperacao(), usuario);
            for (Lancamento lancamento : lancamentosAntigos) {
                if (lancamento.getStatus() == StatusLancamento.PAGO) {
                    reverterLancamentoNaConta(lancamento);
                }
            }
            lancamentoRepository.deleteAll(lancamentosAntigos);
        }

        // Save the new or updated lancamentos
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

            // Get existing attachments for this group operation before saving
            if (form.getGrupoOperacao() != null && form.getComprovantes() != null) {
                form.getComprovantes().forEach(comp -> {
                    comp.setLancamento(lancamento);
                    lancamento.getComprovantes().add(comp);
                });
            }

            Lancamento lancamentoSalvo = lancamentoRepository.save(lancamento);

            // If a new file was uploaded, add it to the list of attachments
            if (comprovanteFile != null && !comprovanteFile.isEmpty()) {
                List<String> comprovantePaths = fileStorageService.storeFile(comprovanteFile);
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
    public List<LancamentoGrupoDTO> buscarComFiltrosAgrupados(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, TipoLancamento tipo, Long categoriaId, Boolean comNotaFiscal, String descricao, StatusLancamento status, Usuario usuario) {
        List<Lancamento> lancamentos = lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario);

        // Agrupa os lançamentos por grupoOperacao (ou por ID se não tiver grupo)
        Map<String, List<Lancamento>> grupos = lancamentos.stream()
                .collect(Collectors.groupingBy(l ->
                    l.getGrupoOperacao() != null ? l.getGrupoOperacao() : l.getId().toString()
                ));

        // Converte cada grupo em um LancamentoGrupoDTO
        return grupos.values().stream()
                .map(this::criarLancamentoGrupoDTO)
                .sorted((g1, g2) -> {
                    // Ordena por data decrescente, depois por ID decrescente
                    int dateCompare = g2.getData().compareTo(g1.getData());
                    return dateCompare != 0 ? dateCompare : g2.getId().compareTo(g1.getId());
                })
                .collect(Collectors.toList());
    }

    private LancamentoGrupoDTO criarLancamentoGrupoDTO(List<Lancamento> lancamentosDoGrupo) {
        if (lancamentosDoGrupo.isEmpty()) return null;

        Lancamento primeiro = lancamentosDoGrupo.get(0);

        // Calcula o valor total do grupo
        BigDecimal valorTotal = lancamentosDoGrupo.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cria descrição das contas envolvidas
        String contasDescricao = lancamentosDoGrupo.stream()
                .map(l -> l.getConta().getNomeConta())
                .distinct()
                .collect(Collectors.joining(", "));

        // Se há apenas uma conta, mostra só o nome. Se há múltiplas, mostra "Múltiplas contas"
        if (lancamentosDoGrupo.size() > 1 && lancamentosDoGrupo.stream().map(l -> l.getConta().getId()).distinct().count() > 1) {
            contasDescricao = "Múltiplas contas (" + contasDescricao + ")";
        }

        return new LancamentoGrupoDTO(
                primeiro.getId(),
                primeiro.getGrupoOperacao(),
                primeiro.getDescricao(),
                primeiro.getData(),
                primeiro.getTipo(),
                primeiro.getStatus(),
                valorTotal,
                contasDescricao,
                primeiro.getCategoriaDespesa(),
                primeiro.getContato(),
                primeiro.getComNotaFiscal(),
                primeiro.getComprovantes()
        );
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