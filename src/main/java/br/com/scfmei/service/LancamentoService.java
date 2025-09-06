package br.com.scfmei.service;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    // --- MUDANÇA 1: Injetamos o SERVIÇO de Contas, e não o repositório ---
    @Autowired
    private ContaService contaService;


    @Transactional(readOnly = true)
    public List<Lancamento> buscarTodos() {
        return lancamentoRepository.findAll();
    }

    // --- MÉTODO SALVAR CORRIGIDO ---
    @Transactional
    public Lancamento salvar(Lancamento lancamento) {
        // 1. Pede ao SERVIÇO de Contas para buscar a conta pelo ID.
        //    .orElseThrow() é uma forma segura de lidar com o Optional: se não achar a conta, ele para com um erro.
        Conta conta = contaService.buscarPorId(lancamento.getConta().getId())
                .orElseThrow(() -> new RuntimeException("Conta não encontrada para o lançamento!"));

        // 2. Verificamos o tipo do lançamento para saber se somamos ou subtraímos.
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        } else { // Se for SAIDA
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        }

        // 3. Pede ao SERVIÇO de Contas para salvar a conta com o saldo já atualizado.
        contaService.salvar(conta);

        // 4. Finalmente, salvamos o próprio lançamento.
        return lancamentoRepository.save(lancamento);
    }


    @Transactional(readOnly = true)
    public Optional<Lancamento> buscarPorId(Long id) {
        return lancamentoRepository.findById(id);
    }

    @Transactional
    public void excluirPorId(Long id) {
        // Futuramente, teremos que adicionar a lógica para reverter a atualização do saldo aqui.
        lancamentoRepository.deleteById(id);
    }
}