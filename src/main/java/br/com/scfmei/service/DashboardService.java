package br.com.scfmei.service;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.ContaRepository;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;


    public BigDecimal getSaldoTotal(Usuario usuario, Long contaId) {
        if (contaId != null) {
            return contaRepository.findById(contaId)
                    .map(Conta::getSaldoAtual)
                    .orElse(BigDecimal.ZERO);
        }

        return contaRepository.findByUsuario(usuario).stream()
                .map(Conta::getSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public BigDecimal getTotalEntradas(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, Usuario usuario) {
        if (dataInicio == null || dataFim == null) {
            YearMonth mesAtual = YearMonth.now();
            dataInicio = mesAtual.atDay(1);
            dataFim = mesAtual.atEndOfMonth();
        }
        List<Lancamento> entradas = lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, contatoId, TipoLancamento.ENTRADA, null, null, null, null, usuario);
        return entradas.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<ChartData> getDespesasPorCategoria(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, Long categoriaId, StatusLancamento status, Usuario usuario) {
        if (dataInicio == null || dataFim == null) {
            YearMonth mesAtual = YearMonth.now();
            dataInicio = mesAtual.atDay(1);
            dataFim = mesAtual.atEndOfMonth();
        }

        return lancamentoRepository.findDespesasPorCategoriaComFiltros(dataInicio, dataFim, contaId, contatoId, categoriaId, status, usuario);
    }

    public Map<String, List<?>> getFluxoDeCaixaUltimos12Meses(Usuario usuario) {
        Map<String, List<?>> resultado = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> entradas = new ArrayList<>();
        List<BigDecimal> saidas = new ArrayList<>();

        LocalDate hoje = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy");

        for (int i = 11; i >= 0; i--) {
            YearMonth mes = YearMonth.from(hoje.minusMonths(i));
            labels.add(mes.format(formatter));

            LocalDate inicioDoMes = mes.atDay(1);
            LocalDate fimDoMes = mes.atEndOfMonth();


            BigDecimal totalEntradas = lancamentoRepository.findComFiltros(inicioDoMes, fimDoMes, null, null, TipoLancamento.ENTRADA, null, null, null, StatusLancamento.PAGO, usuario)
                    .stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSaidas = lancamentoRepository.findComFiltros(inicioDoMes, fimDoMes, null, null, TipoLancamento.SAIDA, null, null, null, StatusLancamento.PAGO, usuario)
                    .stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

            entradas.add(totalEntradas);
            saidas.add(totalSaidas);
        }

        resultado.put("labels", labels);
        resultado.put("entradas", entradas);
        resultado.put("saidas", saidas);
        return resultado;
    }

    public BigDecimal getFaturamentoOficial(int ano, Usuario usuario) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        List<Lancamento> entradas = lancamentoRepository.findComFiltros(inicioDoAno, fimDoAno, null, null, TipoLancamento.ENTRADA, null, null, null, null, usuario);
        return entradas.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFaturamentoBancario(int ano, Usuario usuario) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        BigDecimal total = lancamentoRepository.sumEntradasBancariasNoPeriodo(inicioDoAno, fimDoAno, usuario);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getFaturamentoBancarioMesAtual(Usuario usuario) {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        BigDecimal total = lancamentoRepository.sumEntradasBancariasNoPeriodo(inicioDoMes, fimDoMes, usuario);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getMetaFaturamentoBaseadoEmCustos(int ano, Usuario usuario) {
        LocalDate inicioDoAno = LocalDate.of(ano, 1, 1);
        LocalDate fimDoAno = LocalDate.of(ano, 12, 31);
        List<Lancamento> comprasComNota = lancamentoRepository.findComFiltros(inicioDoAno, fimDoAno, null, null, TipoLancamento.SAIDA, null, true, null, null, usuario);
        BigDecimal totalComprasComNota = comprasComNota.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalComprasComNota.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return totalComprasComNota.divide(new BigDecimal("0.8"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMetaFaturamentoBaseadoEmCustosMensal(Usuario usuario) {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        List<Lancamento> comprasComNota = lancamentoRepository.findComFiltros(inicioDoMes, fimDoMes, null, null, TipoLancamento.SAIDA, null, true, null, null, usuario);
        BigDecimal totalComprasComNota = comprasComNota.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalComprasComNota.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return totalComprasComNota.divide(new BigDecimal("0.8"), 2, RoundingMode.HALF_UP);
    }
}