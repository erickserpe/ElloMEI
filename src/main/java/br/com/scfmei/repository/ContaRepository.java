package br.com.scfmei.repository;

import br.com.scfmei.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // "Carimbo" que identifica esta interface como um Repositório gerenciado pelo Spring
public interface ContaRepository extends JpaRepository<Conta, Long> {
    // A mágica acontece aqui! Não precisamos escrever nada.
}