package br.com.scfmei.repository;

import br.com.scfmei.domain.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.scfmei.domain.TipoLancamento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.scfmei.domain.ChartData;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    // --- MÉTODO NOVO ---
    @Query("SELECT SUM(l.valor) FROM Lancamento l WHERE l.tipo = :tipo AND l.data >= :inicioDoMes AND l.data <= :fimDoMes")
    BigDecimal calcularTotalPorTipoEPeriodo(
            @Param("tipo") TipoLancamento tipo,
            @Param("inicioDoMes") LocalDate inicioDoMes,
            @Param("fimDoMes") LocalDate fimDoMes
    );

    @Query("SELECT new br.com.scfmei.domain.ChartData(c.nome, SUM(l.valor)) " +
            "FROM Lancamento l JOIN l.categoriaDespesa c " +
            "WHERE l.tipo = 'SAIDA' AND l.data >= :inicioDoMes AND l.data <= :fimDoMes " +
            "GROUP BY c.nome")
    List<ChartData> findDespesasPorCategoriaNoPeriodo(
            @Param("inicioDoMes") LocalDate inicioDoMes,
            @Param("fimDoMes") LocalDate fimDoMes
    );
    // ... dentro da interface LancamentoRepository ...

//    @Query("SELECT l FROM Lancamento l WHERE YEAR(l.data) = :ano AND MONTH(l.data) = :mes ORDER BY l.data DESC")
//    List<Lancamento> findByAnoAndMes(@Param("ano") int ano, @Param("mes") int mes);

    // O Spring Data JPA cria a consulta automaticamente a partir do nome do método!
    List<Lancamento> findByDataBetweenOrderByDataDesc(LocalDate dataInicio, LocalDate dataFim);
}