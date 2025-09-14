package br.com.scfmei.repository;

import br.com.scfmei.domain.Contato; // Deve usar Contato
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> { // Deve usar Contato
}