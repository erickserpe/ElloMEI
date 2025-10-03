package br.com.scfmei.repository;

import br.com.scfmei.domain.Assinatura;
import br.com.scfmei.domain.StatusAssinatura;
import br.com.scfmei.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciar assinaturas no sistema.
 * 
 * Responsabilidades:
 * - Buscar assinatura ativa de um usuário
 * - Listar histórico de assinaturas
 * - Buscar assinaturas que precisam ser renovadas
 * - Buscar assinaturas suspensas/expiradas
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Repository
public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
    
    /**
     * Busca a assinatura ativa de um usuário.
     * Um usuário só pode ter uma assinatura ativa por vez.
     */
    Optional<Assinatura> findByUsuarioAndStatus(Usuario usuario, StatusAssinatura status);
    
    /**
     * Busca todas as assinaturas de um usuário (histórico).
     */
    List<Assinatura> findByUsuarioOrderByDataInicioDesc(Usuario usuario);
    
    /**
     * Busca assinatura pelo ID do pagamento externo (Mercado Pago).
     */
    Optional<Assinatura> findByIdPagamentoExterno(String idPagamentoExterno);
    
    /**
     * Busca assinaturas que precisam ser renovadas hoje.
     */
    @Query("SELECT a FROM Assinatura a WHERE a.status = 'ATIVA' " +
           "AND a.dataProximaCobranca <= :hoje " +
           "AND a.formaPagamento = 'CARTAO_CREDITO'")
    List<Assinatura> findAssinaturasParaRenovar(@Param("hoje") LocalDate hoje);
    
    /**
     * Busca assinaturas suspensas que expiraram.
     */
    @Query("SELECT a FROM Assinatura a WHERE a.status = 'SUSPENSA' " +
           "AND a.dataExpiracao <= :hoje")
    List<Assinatura> findAssinaturasSuspensasExpiradas(@Param("hoje") LocalDate hoje);
    
    /**
     * Busca assinaturas em trial que expiraram.
     */
    @Query("SELECT a FROM Assinatura a WHERE a.status = 'TRIAL' " +
           "AND a.dataExpiracao <= :hoje")
    List<Assinatura> findTrialsExpirados(@Param("hoje") LocalDate hoje);
    
    /**
     * Conta quantas assinaturas ativas existem (métrica de negócio).
     */
    long countByStatus(StatusAssinatura status);
    
    /**
     * Verifica se um usuário tem assinatura ativa.
     */
    boolean existsByUsuarioAndStatus(Usuario usuario, StatusAssinatura status);

    /**
     * Busca assinaturas suspensas que precisam de retry de pagamento.
     */
    @Query("SELECT a FROM Assinatura a WHERE a.status = :status " +
           "AND (a.dataUltimaTentativa IS NULL OR CAST(a.dataUltimaTentativa AS date) < :dataLimite) " +
           "AND a.tentativasPagamento < :maxTentativas")
    List<Assinatura> findAssinaturasParaRetry(
        @Param("status") StatusAssinatura status,
        @Param("dataLimite") LocalDate dataLimite,
        @Param("maxTentativas") int maxTentativas
    );
}

