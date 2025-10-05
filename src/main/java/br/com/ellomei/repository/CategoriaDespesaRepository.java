package br.com.ellomei.repository;

import br.com.ellomei.domain.CategoriaDespesa;
import br.com.ellomei.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaDespesaRepository extends JpaRepository<CategoriaDespesa, Long> {

    // Busca todas as categorias associadas a um determinado usuário (sem paginação - legado)
    List<CategoriaDespesa> findByUsuario(Usuario usuario);

    // Busca todas as categorias associadas a um determinado usuário (com paginação)
    Page<CategoriaDespesa> findByUsuario(Usuario usuario, Pageable pageable);

}