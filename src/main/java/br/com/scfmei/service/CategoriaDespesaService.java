package br.com.scfmei.service;

import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.CategoriaDespesaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

@Service
public class CategoriaDespesaService {

    @Autowired
    private CategoriaDespesaRepository categoriaDespesaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDespesa> buscarTodasPorUsuario(Usuario usuario) {
        return categoriaDespesaRepository.findByUsuario(usuario);
    }

    @Transactional
    public CategoriaDespesa salvar(CategoriaDespesa categoriaDespesa, Usuario usuario) {
        categoriaDespesa.setUsuario(usuario); // Associa a categoria ao usuário logado
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
        categoriaDespesaRepository.deleteById(id);
    }
}