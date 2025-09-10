package br.com.scfmei.service;

import br.com.scfmei.domain.ChartData;
import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.repository.ContaRepository;
import br.com.scfmei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        // Se um ID de conta foi especificado, retorna o saldo apenas daquela conta
        if (contaId != null) {
            return contaRepository.findById(contaId)
                    .map(Conta::getSaldoAtual)
                    .orElse(BigDecimal.ZERO);
        }
        // Se não, soma o saldo de todas as contas
        return contaRepository.findAll().stream()
                .map(Conta::getSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalEntradas(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long pessoaId) {
        // Se as datas não forem fornecidas, usa o mês atual como padrão
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
        // Usamos a consulta que já existe, mas sem filtros de conta ou pessoa
        BigDecimal total = lancamentoRepository.calcularTotalComFiltros(TipoLancamento.ENTRADA, inicioDoMes, fimDoMes, null, null);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getFaturamentoAnoAtual() {
        int anoAtual = YearMonth.now().getYear();
        LocalDate inicioDoAno = LocalDate.of(anoAtual, 1, 1);
        LocalDate fimDoAno = LocalDate.of(anoAtual, 12, 31);
        // Usamos a mesma consulta, mas para o período do ano inteiro
        BigDecimal total = lancamentoRepository.calcularTotalComFiltros(TipoLancamento.ENTRADA, inicioDoAno, fimDoAno, null, null);
        return total != null ? total : BigDecimal.ZERO;
    }
}