package br.com.ellomei.service;

import br.com.ellomei.domain.Conta;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.ContaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContaService {

    private static final Logger logger = LoggerFactory.getLogger(ContaService.class);

    @Autowired
    private ContaRepository contaRepository;

    // Método legado (sem paginação) - mantido para compatibilidade
    @Transactional(readOnly = true)
    @Cacheable(value = "contasPorUsuario", key = "#usuario.id")
    public List<Conta> buscarTodasPorUsuario(Usuario usuario) {
        // This log will only appear the FIRST time the method is called for a user
        logger.debug("Buscando contas do banco de dados para o usuário: {}", usuario.getId());
        return contaRepository.findByUsuario(usuario);
    }

    // Novo método com paginação (sem cache por enquanto)
    @Transactional(readOnly = true)
    public Page<Conta> buscarTodasPorUsuario(Usuario usuario, Pageable pageable) {
        logger.debug("Buscando contas paginadas do banco de dados para o usuário: {}", usuario.getId());
        return contaRepository.findByUsuario(usuario, pageable);
    }

    @Transactional
    @CacheEvict(value = "contasPorUsuario", key = "#usuario.id")
    public Conta salvar(Conta conta, Usuario usuario) {
        conta.setUsuario(usuario);

        if (conta.getId() == null) {
            conta.setSaldoAtual(conta.getSaldoInicial());
        }
        logger.debug("Cache de contas invalidado para o usuário: {}", usuario.getId());
        return contaRepository.save(conta);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@customSecurityService.isContaOwner(#id)")
    public Optional<Conta> buscarPorId(Long id) {
        return contaRepository.findById(id);
    }

    @Transactional
    @PreAuthorize("@customSecurityService.isContaOwner(#id)")
    public void excluirPorId(Long id) {
        // First, get the account to obtain the user for cache eviction
        Optional<Conta> conta = contaRepository.findById(id);
        if (conta.isPresent()) {
            Long usuarioId = conta.get().getUsuario().getId();
            contaRepository.deleteById(id);
            // Manually evict cache after deletion
            evictContaCache(usuarioId);
        } else {
            contaRepository.deleteById(id);
        }
    }

    @CacheEvict(value = "contasPorUsuario", key = "#usuarioId")
    public void evictContaCache(Long usuarioId) {
        logger.debug("Cache de contas invalidado para o usuário: {}", usuarioId);
    }
}