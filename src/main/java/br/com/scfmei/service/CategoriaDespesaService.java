package br.com.scfmei.service;

import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.CategoriaDespesaRepository;
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
import java.util.Optional;
import java.util.List;

@Service
public class CategoriaDespesaService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaDespesaService.class);

    @Autowired
    private CategoriaDespesaRepository categoriaDespesaRepository;

    // Método legado (sem paginação) - mantido para compatibilidade
    @Transactional(readOnly = true)
    @Cacheable(value = "categoriasPorUsuario", key = "#usuario.id")
    public List<CategoriaDespesa> buscarTodasPorUsuario(Usuario usuario) {
        // This log will only appear the FIRST time the method is called for a user
        logger.debug("Buscando categorias do banco de dados para o usuário: {}", usuario.getId());
        return categoriaDespesaRepository.findByUsuario(usuario);
    }

    // Novo método com paginação (sem cache por enquanto)
    @Transactional(readOnly = true)
    public Page<CategoriaDespesa> buscarTodasPorUsuario(Usuario usuario, Pageable pageable) {
        logger.debug("Buscando categorias paginadas do banco de dados para o usuário: {}", usuario.getId());
        return categoriaDespesaRepository.findByUsuario(usuario, pageable);
    }

    @Transactional
    @CacheEvict(value = "categoriasPorUsuario", key = "#usuario.id")
    public CategoriaDespesa salvar(CategoriaDespesa categoriaDespesa, Usuario usuario) {
        categoriaDespesa.setUsuario(usuario); // Associa a categoria ao usuário logado
        logger.debug("Cache de categorias invalidado para o usuário: {}", usuario.getId());
        return categoriaDespesaRepository.save(categoriaDespesa);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@customSecurityService.isCategoriaOwner(#id)")
    public Optional<CategoriaDespesa> buscarPorId(Long id) {
        return categoriaDespesaRepository.findById(id);
    }

    @Transactional
    @PreAuthorize("@customSecurityService.isCategoriaOwner(#id)")
    public void excluirPorId(Long id) {
        // First, get the category to obtain the user for cache eviction
        Optional<CategoriaDespesa> categoria = categoriaDespesaRepository.findById(id);
        if (categoria.isPresent()) {
            Long usuarioId = categoria.get().getUsuario().getId();
            categoriaDespesaRepository.deleteById(id);
            // Manually evict cache after deletion
            evictCategoriaCache(usuarioId);
        } else {
            categoriaDespesaRepository.deleteById(id);
        }
    }

    @CacheEvict(value = "categoriasPorUsuario", key = "#usuarioId")
    public void evictCategoriaCache(Long usuarioId) {
        logger.debug("Cache de categorias invalidado para o usuário: {}", usuarioId);
    }
}