// src/main/java/br/com/scfmei/repository/ContatoRepository.java
package br.com.scfmei.repository;

import br.com.scfmei.domain.Contato;
import br.com.scfmei.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> {


    List<Contato> findByUsuario(Usuario usuario);
    
}