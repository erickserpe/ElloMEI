package br.com.ellomei.repository;

import br.com.ellomei.domain.Comprovante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprovanteRepository extends JpaRepository<Comprovante, Long> {
}