package br.com.scfmei.service;

import br.com.scfmei.domain.Pessoa;
import br.com.scfmei.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        // Por enquanto, não temos regras de negócio complexas para salvar uma pessoa
        return pessoaRepository.save(pessoa);
    }
}