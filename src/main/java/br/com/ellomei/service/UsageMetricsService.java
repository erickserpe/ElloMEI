package br.com.ellomei.service;

import br.com.ellomei.domain.PlanoAssinatura;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.dto.UsageMetricsDTO;
import br.com.ellomei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Serviço responsável por calcular métricas de uso dos usuários.
 * 
 * Responsabilidades:
 * - Calcular quantos lançamentos foram usados no mês
 * - Calcular percentual de uso do plano
 * - Gerar alertas de proximidade do limite
 * - Fornecer dados para o dashboard de uso
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Service
public class UsageMetricsService {
    
    @Autowired
    private LancamentoRepository lancamentoRepository;
    
    // Limites por plano
    private static final int LIMITE_FREE = 20;
    private static final int LIMITE_PRO = Integer.MAX_VALUE; // Ilimitado
    
    /**
     * Calcula as métricas de uso de um usuário.
     * 
     * @param usuario Usuário para calcular métricas
     * @return DTO com métricas de uso
     */
    public UsageMetricsDTO calcularMetricas(Usuario usuario) {
        YearMonth mesAtual = YearMonth.now();
        
        // Contar lançamentos do mês atual
        int lancamentosUsados = contarLancamentosMes(usuario, mesAtual);
        
        // Obter limite do plano
        int lancamentosLimite = getLimiteLancamentos(usuario.getPlano());
        
        // Calcular dias restantes no mês
        int diasRestantes = calcularDiasRestantesNoMes();
        
        return new UsageMetricsDTO(
            usuario.getPlano(),
            lancamentosUsados,
            lancamentosLimite,
            diasRestantes
        );
    }
    
    /**
     * Conta quantos lançamentos o usuário criou no mês especificado.
     * 
     * Importante: Conta apenas lançamentos únicos (não duplica por grupo de operação).
     */
    private int contarLancamentosMes(Usuario usuario, YearMonth mes) {
        LocalDate inicioMes = mes.atDay(1);
        LocalDate fimMes = mes.atEndOfMonth();
        
        // Contar apenas lançamentos únicos (distintos por grupoOperacao)
        return lancamentoRepository.countDistinctGrupoOperacaoByUsuarioAndDataBetween(
            usuario,
            inicioMes,
            fimMes
        );
    }
    
    /**
     * Retorna o limite de lançamentos para um plano.
     */
    private int getLimiteLancamentos(PlanoAssinatura plano) {
        return switch (plano) {
            case FREE -> LIMITE_FREE;
            case PRO -> LIMITE_PRO;
        };
    }
    
    /**
     * Calcula quantos dias faltam para o fim do mês.
     */
    private int calcularDiasRestantesNoMes() {
        LocalDate hoje = LocalDate.now();
        LocalDate fimDoMes = YearMonth.now().atEndOfMonth();
        return (int) ChronoUnit.DAYS.between(hoje, fimDoMes) + 1;
    }
    
    /**
     * Verifica se o usuário está próximo do limite (80% ou mais).
     */
    public boolean isProximoDoLimite(Usuario usuario) {
        UsageMetricsDTO metrics = calcularMetricas(usuario);
        return metrics.isProximoDoLimite();
    }
    
    /**
     * Verifica se o usuário excedeu o limite.
     */
    public boolean isLimiteExcedido(Usuario usuario) {
        UsageMetricsDTO metrics = calcularMetricas(usuario);
        return metrics.isLimiteExcedido();
    }
}

