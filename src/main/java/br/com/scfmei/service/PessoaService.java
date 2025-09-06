package br.com.scfmei.service;

import br.com.scfmei.domain.Pessoa;
import br.com.scfmei.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional(readOnly = true)
    public List<Pessoa> buscarTodas() {
        return pessoaRepository.findAll();
    }

    @Transactional
    public Pessoa salvar(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    // --- MÉTODO NOVO ---
    @Transactional(readOnly = true)
    public Optional<Pessoa> buscarPorId(Long id) {
        return pessoaRepository.findById(id);
    }

    // --- MÉTODO NOVO ---
    @Transactional
    public void excluirPorId(Long id) {
        pessoaRepository.deleteById(id);
    }
}