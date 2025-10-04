package br.com.scfmei;

import br.com.scfmei.domain.Role;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.RoleRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * Classe helper para auxiliar na criação de dados de teste.
 */
public class TestHelper {

    /**
     * Cria um Set de roles para testes.
     * 
     * @param roleRepository Repositório de roles
     * @param roleNames Nomes das roles (ex: "ROLE_USER", "ROLE_ADMIN")
     * @return Set de roles
     */
    public static Set<Role> createRoles(RoleRepository roleRepository, String... roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByNome(roleName)
                    .orElseGet(() -> {
                        Role newRole = new Role(roleName);
                        return roleRepository.save(newRole);
                    });
            roles.add(role);
        }
        return roles;
    }

    /**
     * Cria um Set com a role ROLE_USER para testes.
     * 
     * @param roleRepository Repositório de roles
     * @return Set contendo ROLE_USER
     */
    public static Set<Role> createUserRole(RoleRepository roleRepository) {
        return createRoles(roleRepository, "ROLE_USER");
    }
}

