package br.com.scfmei.service;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Transactional(readOnly = true)
    public List<Lancamento> buscarTodos() {
        return lancamentoRepository.findAll();
    }

    @Transactional
    public Lancamento salvar(Lancamento lancamento) {
        return lancamentoRepository.save(lancamento);
    }

    // Futuramente, adicionaremos os outros métodos aqui (buscarPorId, excluirPorId)
}