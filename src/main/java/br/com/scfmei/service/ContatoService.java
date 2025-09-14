package br.com.scfmei.service;

import br.com.scfmei.domain.Contato;
import br.com.scfmei.repository.ContatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContatoService { // Nome da classe atualizado

    @Autowired
    private ContatoRepository contatoRepository; // Usa o novo ContatoRepository

    @Transactional(readOnly = true)
    public List<Contato> buscarTodos() {
        return contatoRepository.findAll();
    }

    @Transactional
    public Contato salvar(Contato contato) {
        return contatoRepository.save(contato);
    }

    @Transactional(readOnly = true)
    public Optional<Contato> buscarPorId(Long id) {
        return contatoRepository.findById(id);
    }

    @Transactional
    public void excluirPorId(Long id) {
        contatoRepository.deleteById(id);
    }
}