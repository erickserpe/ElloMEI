package br.com.scfmei.repository;

import br.com.scfmei.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método para o Spring Security encontrar um usuário pelo seu nome de login
    Optional<Usuario> findByUsername(String username);
}