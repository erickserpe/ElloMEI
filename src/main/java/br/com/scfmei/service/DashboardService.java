package br.com.scfmei.service;

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

    public BigDecimal getSaldoTotal() {
        List<Conta> contas = contaRepository.findAll();
        return contas.stream()
                .map(Conta::getSaldoAtual) // Pega o saldo atual de cada conta
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Soma todos os saldos, começando do zero
    }

    public BigDecimal getTotalEntradasMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        BigDecimal total = lancamentoRepository.calcularTotalPorTipoEPeriodo(TipoLancamento.ENTRADA, inicioDoMes, fimDoMes);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSaidasMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioDoMes = mesAtual.atDay(1);
        LocalDate fimDoMes = mesAtual.atEndOfMonth();
        BigDecimal total = lancamentoRepository.calcularTotalPorTipoEPeriodo(TipoLancamento.SAIDA, inicioDoMes, fimDoMes);
        return total != null ? total : BigDecimal.ZERO;
    }
}