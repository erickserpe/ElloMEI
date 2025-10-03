// src/main/java/br/com/scfmei/service/ContatoService.java
package br.com.scfmei.service;

import br.com.scfmei.domain.Contato;
import br.com.scfmei.domain.Usuario; // Importe Usuario
import br.com.scfmei.repository.ContatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContatoService {

    @Autowired
    private ContatoRepository contatoRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "contatosPorUsuario", key = "#usuario.id")
    public List<Contato> buscarTodosPorUsuario(Usuario usuario) {
        // This log will only appear the FIRST time the method is called for a user
        System.out.println("Buscando contatos do banco de dados para o usuário: " + usuario.getId());
        return contatoRepository.findByUsuario(usuario);
    }

    @Transactional
    @CacheEvict(value = "contatosPorUsuario", key = "#usuario.id")
    public Contato salvar(Contato contato, Usuario usuario) {
        contato.setUsuario(usuario); // Associa o contato ao usuário logado
        System.out.println("Cache de contatos invalidado para o usuário: " + usuario.getId());
        return contatoRepository.save(contato);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@customSecurityService.isContatoOwner(#id)")
    public Optional<Contato> buscarPorId(Long id) {
        return contatoRepository.findById(id);
    }

    @Transactional
    @PreAuthorize("@customSecurityService.isContatoOwner(#id)")
    public void excluirPorId(Long id) {
        // First, get the contact to obtain the user for cache eviction
        Optional<Contato> contato = contatoRepository.findById(id);
        if (contato.isPresent()) {
            Long usuarioId = contato.get().getUsuario().getId();
            contatoRepository.deleteById(id);
            // Manually evict cache after deletion
            evictContatoCache(usuarioId);
        } else {
            contatoRepository.deleteById(id);
        }
    }

    @CacheEvict(value = "contatosPorUsuario", key = "#usuarioId")
    public void evictContatoCache(Long usuarioId) {
        System.out.println("Cache de contatos invalidado para o usuário: " + usuarioId);
    }
}