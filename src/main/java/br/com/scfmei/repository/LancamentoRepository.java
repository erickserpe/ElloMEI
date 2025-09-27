// src/main/java/br/com/scfmei/repository/LancamentoRepository.java
package br.com.scfmei.repository;

import br.com.scfmei.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    // Métodos já refatorados
    List<Lancamento> findByGrupoOperacaoAndUsuario(String grupoOperacao, Usuario usuario);
    List<Lancamento> findByStatusAndUsuarioOrderByDataAsc(StatusLancamento status, Usuario usuario);

    @Query("SELECT l FROM Lancamento l WHERE " +
            "(:dataInicio IS NULL OR l.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR l.data <= :dataFim) AND " +
            "(:contaId IS NULL OR l.conta.id = :contaId) AND " +
            "(:contatoId IS NULL OR l.contato.id = :contatoId) AND " +
            "(:tipo IS NULL OR l.tipo = :tipo) AND " +
            "(:categoriaId IS NULL OR l.categoriaDespesa.id = :categoriaId) AND " +
            "(:comNotaFiscal IS NULL OR l.comNotaFiscal = :comNotaFiscal) AND " +
            "(:descricao IS NULL OR l.descricao LIKE %:descricao%) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "l.usuario = :usuario " +
            "ORDER BY l.data DESC, l.id DESC")
    List<Lancamento> findComFiltros(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("contaId") Long contaId,
            @Param("contatoId") Long contatoId,
            @Param("tipo") TipoLancamento tipo,
            @Param("categoriaId") Long categoriaId,
            @Param("comNotaFiscal") Boolean comNotaFiscal,
            @Param("descricao") String descricao,
            @Param("status") StatusLancamento status,
            @Param("usuario") Usuario usuario
    );

    @Query("SELECT new br.com.scfmei.domain.ChartData(c.nome, SUM(l.valor)) " +
            "FROM Lancamento l JOIN l.categoriaDespesa c " +
            "WHERE l.tipo = 'SAIDA' " +
            "AND (:status IS NULL OR l.status = :status) " +
            "AND l.data >= :inicioDoMes AND l.data <= :fimDoMes " +
            "AND (:contaId IS NULL OR l.conta.id = :contaId) " +
            "AND (:contatoId IS NULL OR l.contato.id = :contatoId) " +
            "AND (:categoriaId IS NULL OR l.categoriaDespesa.id = :categoriaId) " +
            "AND l.usuario = :usuario " +
            "GROUP BY c.nome " +
            "ORDER BY SUM(l.valor) DESC")
    List<ChartData> findDespesasPorCategoriaComFiltros(
            @Param("inicioDoMes") LocalDate inicioDoMes,
            @Param("fimDoMes") LocalDate fimDoMes,
            @Param("contaId") Long contaId,
            @Param("contatoId") Long contatoId,
            @Param("categoriaId") Long categoriaId,
            @Param("status") StatusLancamento status,
            @Param("usuario") Usuario usuario
    );

    // --- CONSULTA ATUALIZADA ---
    @Query("SELECT SUM(l.valor) FROM Lancamento l JOIN l.conta c " +
            "WHERE l.tipo = 'ENTRADA' " +
            "AND c.tipo <> 'Caixa' " +
            "AND l.data >= :dataInicio AND l.data <= :dataFim " +
            "AND l.usuario = :usuario") // <-- FILTRO DE USUÁRIO ADICIONADO
    BigDecimal sumEntradasBancariasNoPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("usuario") Usuario usuario // <-- NOVO PARÂMETRO
    );
}