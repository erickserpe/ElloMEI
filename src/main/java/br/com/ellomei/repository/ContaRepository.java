// src/main/java/br/com/scfmei/repository/ContaRepository.java
package br.com.ellomei.repository;

import br.com.ellomei.domain.Conta;
import br.com.ellomei.domain.Usuario; // Importe a entidade Usuario
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Importe a classe List

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    // --- MÉTODOS DE BUSCA ---
    // Busca todas as contas associadas a um determinado usuário (sem paginação - legado)
    List<Conta> findByUsuario(Usuario usuario);

    // Busca todas as contas associadas a um determinado usuário (com paginação)
    Page<Conta> findByUsuario(Usuario usuario, Pageable pageable);
    // -------------------
}