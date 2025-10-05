package br.com.ellomei.repository;

import br.com.ellomei.domain.HistoricoPagamento;
import br.com.ellomei.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para a entidade HistoricoPagamento.
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Repository
public interface HistoricoPagamentoRepository extends JpaRepository<HistoricoPagamento, Long> {
    
    /**
     * Busca todos os pagamentos de um usuário, ordenados por data (mais recente primeiro).
     */
    List<HistoricoPagamento> findByUsuarioOrderByDataPagamentoDesc(Usuario usuario);
    
    /**
     * Busca todos os pagamentos de um usuário com paginação.
     */
    Page<HistoricoPagamento> findByUsuarioOrderByDataPagamentoDesc(Usuario usuario, Pageable pageable);
    
    /**
     * Busca pagamento pelo ID externo (Mercado Pago).
     */
    Optional<HistoricoPagamento> findByIdPagamentoExterno(String idPagamentoExterno);
    
    /**
     * Busca pagamentos aprovados de um usuário.
     */
    @Query("SELECT h FROM HistoricoPagamento h WHERE h.usuario = :usuario AND h.status = 'approved' ORDER BY h.dataPagamento DESC")
    List<HistoricoPagamento> findPagamentosAprovados(@Param("usuario") Usuario usuario);
    
    /**
     * Conta total de pagamentos aprovados de um usuário.
     */
    @Query("SELECT COUNT(h) FROM HistoricoPagamento h WHERE h.usuario = :usuario AND h.status = 'approved'")
    Long countPagamentosAprovados(@Param("usuario") Usuario usuario);
    
    /**
     * Soma total gasto por um usuário (apenas pagamentos aprovados).
     */
    @Query("SELECT COALESCE(SUM(h.valor), 0) FROM HistoricoPagamento h WHERE h.usuario = :usuario AND h.status = 'approved'")
    java.math.BigDecimal calcularTotalGasto(@Param("usuario") Usuario usuario);
}

