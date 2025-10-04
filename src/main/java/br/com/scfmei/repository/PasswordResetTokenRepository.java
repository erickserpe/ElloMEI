package br.com.scfmei.repository;

import br.com.scfmei.domain.PasswordResetToken;
import br.com.scfmei.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    List<PasswordResetToken> findByUsuario(Usuario usuario);
    
    // Limpar tokens expirados (pode ser usado em um job agendado)
    void deleteByDataExpiracaoBefore(LocalDateTime data);
}

