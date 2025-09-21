package br.com.scfmei.repository;

import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaDespesaRepository extends JpaRepository<CategoriaDespesa, Long> {

    List<CategoriaDespesa> findByUsuario(Usuario usuario);

}