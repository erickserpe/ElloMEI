// src/main/java/br/com/scfmei/repository/ContatoRepository.java
package br.com.ellomei.repository;

import br.com.ellomei.domain.Contato;
import br.com.ellomei.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> {

    // Busca todos os contatos associados a um determinado usuário (sem paginação - legado)
    List<Contato> findByUsuario(Usuario usuario);

    // Busca todos os contatos associados a um determinado usuário (com paginação)
    Page<Contato> findByUsuario(Usuario usuario, Pageable pageable);

}