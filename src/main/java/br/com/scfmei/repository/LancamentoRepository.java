package br.com.scfmei.repository;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    List<Lancamento> findByDataBetweenOrderByDataDesc(LocalDate dataInicio, LocalDate dataFim);

    @Query("SELECT SUM(l.valor) FROM Lancamento l " +
            "WHERE l.tipo = :tipo " +
            "AND l.data >= :inicioDoMes AND l.data <= :fimDoMes " +
            "AND (:contaId IS NULL OR l.conta.id = :contaId) " +
            "AND (:pessoaId IS NULL OR l.pessoa.id = :pessoaId)")
    BigDecimal calcularTotalComFiltros(
            @Param("tipo") TipoLancamento tipo,
            @Param("inicioDoMes") LocalDate inicioDoMes,
            @Param("fimDoMes") LocalDate fimDoMes,
            @Param("contaId") Long contaId,
            @Param("pessoaId") Long pessoaId
    );

    @Query("SELECT new br.com.scfmei.domain.ChartData(c.nome, SUM(l.valor)) " +
            "FROM Lancamento l JOIN l.categoriaDespesa c " +
            "WHERE l.tipo = 'SAIDA' " +
            "AND l.data >= :inicioDoMes AND l.data <= :fimDoMes " +
            "AND (:contaId IS NULL OR l.conta.id = :contaId) " +
            "AND (:pessoaId IS NULL OR l.pessoa.id = :pessoaId) " +
            "GROUP BY c.nome")
    List<ChartData> findDespesasPorCategoriaComFiltros(
            @Param("inicioDoMes") LocalDate inicioDoMes,
            @Param("fimDoMes") LocalDate fimDoMes,
            @Param("contaId") Long contaId,
            @Param("pessoaId") Long pessoaId
    );
}