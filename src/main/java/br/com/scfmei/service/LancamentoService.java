package br.com.scfmei.service;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private ContaService contaService;


    @Transactional(readOnly = true)
    public List<Lancamento> buscarTodos() {
        return lancamentoRepository.findAll();
    }

    /**
     * Salva um lançamento, tratando tanto a criação de um novo quanto a atualização de um existente.
     * A lógica de atualização de saldo está contida aqui.
     */
    @Transactional
    public Lancamento salvar(Lancamento lancamento) {
        if (lancamento.getId() != null) {
            // Lógica de EDIÇÃO: Se o lançamento já existe, precisamos reverter o valor antigo antes de aplicar o novo.
            Lancamento lancamentoExistente = buscarPorId(lancamento.getId())
                    .orElseThrow(() -> new RuntimeException("Lançamento a ser editado não encontrado!"));
            reverterLancamentoNaConta(lancamentoExistente);
        }

        aplicarLancamentoNaConta(lancamento);
        return lancamentoRepository.save(lancamento);
    }

    @Transactional(readOnly = true)
    public Optional<Lancamento> buscarPorId(Long id) {
        return lancamentoRepository.findById(id);
    }

    /**
     * Exclui um lançamento e reverte o impacto que ele teve no saldo da conta.
     */
    @Transactional
    public void excluirPorId(Long id) {
        Lancamento lancamento = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Lançamento a ser excluído não encontrado!"));

        reverterLancamentoNaConta(lancamento);
        lancamentoRepository.deleteById(id);
    }

    // --- Métodos Privados de Ajuda ---

    private void aplicarLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        } else { // SAIDA
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        }
        contaService.salvar(conta);
    }

    private void reverterLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            // Para reverter uma entrada, nós subtraímos o valor
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        } else { // SAIDA
            // Para reverter uma saída, nós somamos o valor de volta
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        }
        contaService.salvar(conta);
    }
//    public List<Lancamento> buscarPorMesEAno(int ano, int mes) {
//        return lancamentoRepository.findByAnoAndMes(ano, mes);
//    }

    public List<Lancamento> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return lancamentoRepository.findByDataBetweenOrderByDataDesc(dataInicio, dataFim);
    }
}