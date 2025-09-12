package br.com.scfmei.repository;

import br.com.scfmei.domain.Comprovante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprovanteRepository extends JpaRepository<Comprovante, Long> {
}