package br.com.scfmei.service;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.repository.ContaRepository;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    public BigDecimal getSaldoTotal(Long contaId) {
        if (contaId != null) {
            return contaRepository.findById(contaId)
                    .map(Conta::getSaldoAtual)
                    .orElse(BigDecimal.ZERO);
        }
        return contaRepository.findAll().stream()
                .map(Conta::getSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalEntradas(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId) {
        if (dataInicio == null || dataFim == null) {
            YearMonth mesAtual = YearMonth.now();
            dataInicio = mesAtual.atDay(1);
            dataFim = mesAtual.atEndOfMonth();
        }
        BigDecimal total = lancamentoRepository.calcularTotalComFiltros(TipoLancamento.ENTRADA, dataInicio, dataFim, contaId, pessoaId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSaidas(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId) {
        if (dataInicio == null || dataFim == null) {
            YearMonth mesAtual = YearMonth.now();
            dataInicio = mesAtual.atDay(1);
            dataFim = mesAtual.atEndOfMonth();
        }
        BigDecimal total = lancamentoRepository.calcularTotalComFiltros(TipoLancamento.SAIDA, dataInicio, dataFim, contaId, pessoaId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<ChartData> getDespesasPorCategoria(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId) {
        if (dataInicio == null || dataFim == null) {
            YearMonth mesAtual = YearMonth.now();
            dataInicio = mesAtual.atDay(1);
            dataFim = mesAtual.atEndOfMonth();
        }
        return lancamentoRepository.findDespesasPorCategoriaComFiltros(dataInicio, dataFim, contaId, pessoaId);
    }

    public BigDecimal getFaturamentoMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        BigDecimal total = lancamentoRepository.calcularTotalComFiltros(TipoLancamento.ENTRADA, inicioDoMes, fimDoMes, null, null);
        return total != null ? total : BigDecimal.ZERO;
    }

    // VISÃO 1: Faturamento Oficial MEI (Anual)
    public BigDecimal getFaturamentoOficial(int ano) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        BigDecimal total = lancamentoRepository.calcularTotalComFiltros(TipoLancamento.ENTRADA, inicioDoAno, fimDoAno, null, null);
        return total != null ? total : BigDecimal.ZERO;
    }

    // VISÃO 2: Faturamento Bancário (Anual)
    public BigDecimal getFaturamentoBancario(int ano) {
        // TODO: Implementar lógica de busca por tipo de conta.
        return BigDecimal.ZERO; // Valor temporário
    }

    // VISÃO 3: Meta de Faturamento (Baseado em Compras com Nota - Anual)
    public BigDecimal getMetaFaturamentoBaseadoEmCustos(int ano) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        BigDecimal totalComprasComNota = lancamentoRepository.sumSaidasComNotaNoPeriodo(inicioDoAno, fimDoAno);

        if (totalComprasComNota == null || totalComprasComNota.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return totalComprasComNota.divide(new BigDecimal("0.8"), 2, RoundingMode.HALF_UP);
    }
    public BigDecimal getFaturamentoBancarioMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        BigDecimal total = lancamentoRepository.sumEntradasBancariasNoPeriodo(inicioDoMes, fimDoMes);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getMetaFaturamentoBaseadoEmCustosMensal() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        BigDecimal totalComprasComNota = lancamentoRepository.sumSaidasComNotaNoPeriodo(inicioDoMes, fimDoMes);

        if (totalComprasComNota == null || totalComprasComNota.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return totalComprasComNota.divide(new BigDecimal("0.8"), 2, RoundingMode.HALF_UP);
    }
}