package br.com.ellomei.repository;

import br.com.ellomei.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para gerenciar roles (papéis/permissões) do sistema.
 * 
 * Permite buscar roles por nome para associação com usuários.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Busca uma role pelo nome.
     * 
     * @param nome Nome da role (ex: "ROLE_USER", "ROLE_ADMIN")
     * @return Optional contendo a role se encontrada
     */
    Optional<Role> findByNome(String nome);
}

