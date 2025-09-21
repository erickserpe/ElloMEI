// src/main/java/br/com/scfmei/service/ContatoService.java
package br.com.scfmei.service;

import br.com.scfmei.domain.Contato;
import br.com.scfmei.domain.Usuario; // Importe Usuario
import br.com.scfmei.repository.ContatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContatoService {

    @Autowired
    private ContatoRepository contatoRepository;

    @Transactional(readOnly = true)
    public List<Contato> buscarTodosPorUsuario(Usuario usuario) {
        return contatoRepository.findByUsuario(usuario);
    }

    @Transactional
    public Contato salvar(Contato contato, Usuario usuario) {
        contato.setUsuario(usuario); // Associa o contato ao usuário logado
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