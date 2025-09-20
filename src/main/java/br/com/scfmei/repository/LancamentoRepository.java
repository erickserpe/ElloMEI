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

    List<Lancamento> findByGrupoOperacao(String grupoOperacao);

    @Query("SELECT l FROM Lancamento l WHERE " +
            "(:dataInicio IS NULL OR l.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR l.data <= :dataFim) AND " +
            "(:contaId IS NULL OR l.conta.id = :contaId) AND " +
            "(:contatoId IS NULL OR l.contato.id = :contatoId) AND " +
            "(:tipo IS NULL OR l.tipo = :tipo) AND " +
            "(:categoriaId IS NULL OR l.categoriaDespesa.id = :categoriaId) AND " +
            "(:comNotaFiscal IS NULL OR l.comNotaFiscal = :comNotaFiscal) AND " +
            "(:descricao IS NULL OR l.descricao LIKE %:descricao%) " + // <-- FILTRO NOVO
            "ORDER BY l.data DESC, l.id DESC")
    List<Lancamento> findComFiltros(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("contaId") Long contaId,
            @Param("contatoId") Long contatoId,
            @Param("tipo") TipoLancamento tipo,
            @Param("categoriaId") Long categoriaId,
            @Param("comNotaFiscal") Boolean comNotaFiscal,
            @Param("descricao") String descricao // <-- PARÂMETRO NOVO
    );

    @Query("SELECT new br.com.scfmei.domain.ChartData(c.nome, SUM(l.valor)) " +
            "FROM Lancamento l JOIN l.categoriaDespesa c " +
            "WHERE l.tipo = 'SAIDA' " +
            "AND l.data >= :inicioDoMes AND l.data <= :fimDoMes " +
            "AND (:contaId IS NULL OR l.conta.id = :contaId) " +
            "AND (:contatoId IS NULL OR l.contato.id = :contatoId) " +
            "GROUP BY c.nome")
    List<ChartData> findDespesasPorCategoriaComFiltros(
            @Param("inicioDoMes") LocalDate inicioDoMes,
            @Param("fimDoMes") LocalDate fimDoMes,
            @Param("contaId") Long contaId,
            @Param("contatoId") Long contatoId
    );

    @Query("SELECT SUM(l.valor) FROM Lancamento l JOIN l.conta c " +
            "WHERE l.tipo = 'ENTRADA' " +
            "AND c.tipo <> 'Caixa' " +
            "AND l.data >= :dataInicio AND l.data <= :dataFim")
    BigDecimal sumEntradasBancariasNoPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}