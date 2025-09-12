package br.com.scfmei.service;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Lancamento;
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
        List<Lancamento> entradas = lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, pessoaId, TipoLancamento.ENTRADA, null);
        return entradas.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSaidas(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId) {
        if (dataInicio == null || dataFim == null) {
            YearMonth mesAtual = YearMonth.now();
            dataInicio = mesAtual.atDay(1);
            dataFim = mesAtual.atEndOfMonth();
        }
        List<Lancamento> saidas = lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, pessoaId, TipoLancamento.SAIDA, null);
        return saidas.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<ChartData> getDespesasPorCategoria(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId) {
        if (dataInicio == null || dataFim == null) {
            YearMonth mesAtual = YearMonth.now();
            dataInicio = mesAtual.atDay(1);
            dataFim = mesAtual.atEndOfMonth();
        }
        return lancamentoRepository.findDespesasPorCategoriaComFiltros(dataInicio, dataFim, contaId, pessoaId);
    }

    public BigDecimal getFaturamentoOficial(int ano) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        List<Lancamento> entradas = lancamentoRepository.findComFiltros(inicioDoAno, fimDoAno, null, null, TipoLancamento.ENTRADA, null);
        return entradas.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFaturamentoBancario(int ano) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        BigDecimal total = lancamentoRepository.sumEntradasBancariasNoPeriodo(inicioDoAno, fimDoAno);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getFaturamentoBancarioMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        BigDecimal total = lancamentoRepository.sumEntradasBancariasNoPeriodo(inicioDoMes, fimDoMes);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getMetaFaturamentoBaseadoEmCustos(int ano) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        List<Lancamento> comprasComNota = lancamentoRepository.findComFiltros(inicioDoAno, fimDoAno, null, null, TipoLancamento.SAIDA, true);
        BigDecimal totalComprasComNota = comprasComNota.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalComprasComNota.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return totalComprasComNota.divide(new BigDecimal("0.8"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMetaFaturamentoBaseadoEmCustosMensal() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        List<Lancamento> comprasComNota = lancamentoRepository.findComFiltros(inicioDoMes, fimDoMes, null, null, TipoLancamento.SAIDA, true);
        BigDecimal totalComprasComNota = comprasComNota.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalComprasComNota.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return totalComprasComNota.divide(new BigDecimal("0.8"), 2, RoundingMode.HALF_UP);
    }
}