package br.com.ellomei.dto;

import br.com.ellomei.domain.PlanoAssinatura;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO que representa as métricas de uso de um usuário.
 * 
 * Usado para exibir no dashboard:
 * - Quantos lançamentos foram usados no mês
 * - Qual o limite do plano
 * - Percentual de uso
 * - Dias restantes no mês
 * - Alertas de proximidade do limite
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public class UsageMetricsDTO {
    
    private PlanoAssinatura plano;
    private int lancamentosUsados;
    private int lancamentosLimite;
    private BigDecimal percentualUso;
    private int diasRestantesNoMes;
    private boolean proximoDoLimite;
    private boolean limiteExcedido;
    private String mensagemAlerta;
    
    // ==================== CONSTRUCTORS ====================
    
    public UsageMetricsDTO() {
    }
    
    public UsageMetricsDTO(PlanoAssinatura plano, int lancamentosUsados, 
                          int lancamentosLimite, int diasRestantesNoMes) {
        this.plano = plano;
        this.lancamentosUsados = lancamentosUsados;
        this.lancamentosLimite = lancamentosLimite;
        this.diasRestantesNoMes = diasRestantesNoMes;
        
        // Calcular percentual de uso
        if (lancamentosLimite > 0) {
            this.percentualUso = BigDecimal.valueOf(lancamentosUsados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(lancamentosLimite), 2, RoundingMode.HALF_UP);
        } else {
            this.percentualUso = BigDecimal.ZERO;
        }
        
        // Verificar se está próximo do limite (80% ou mais)
        this.proximoDoLimite = percentualUso.compareTo(BigDecimal.valueOf(80)) >= 0;
        
        // Verificar se excedeu o limite
        this.limiteExcedido = lancamentosUsados >= lancamentosLimite && lancamentosLimite > 0;
        
        // Gerar mensagem de alerta
        gerarMensagemAlerta();
    }
    
    /**
     * Gera mensagem de alerta baseada no uso.
     */
    private void gerarMensagemAlerta() {
        if (plano == PlanoAssinatura.PRO) {
            this.mensagemAlerta = null; // PRO não tem limites
        } else if (limiteExcedido) {
            this.mensagemAlerta = "⚠️ Você atingiu o limite de " + lancamentosLimite + 
                " lançamentos do plano FREE. Faça upgrade para o plano PRO!";
        } else if (proximoDoLimite) {
            int restantes = lancamentosLimite - lancamentosUsados;
            this.mensagemAlerta = "⚠️ Atenção! Você tem apenas " + restantes + 
                " lançamentos restantes neste mês. Considere fazer upgrade para o plano PRO.";
        } else {
            this.mensagemAlerta = null;
        }
    }
    
    /**
     * Retorna a cor da barra de progresso baseada no uso.
     */
    public String getCorBarraProgresso() {
        if (plano == PlanoAssinatura.PRO) {
            return "success"; // Verde
        } else if (limiteExcedido) {
            return "danger"; // Vermelho
        } else if (proximoDoLimite) {
            return "warning"; // Amarelo
        } else {
            return "primary"; // Azul
        }
    }
    
    /**
     * Retorna o ícone apropriado baseado no status.
     */
    public String getIconeStatus() {
        if (plano == PlanoAssinatura.PRO) {
            return "bi-infinity"; // Infinito
        } else if (limiteExcedido) {
            return "bi-exclamation-triangle-fill"; // Alerta
        } else if (proximoDoLimite) {
            return "bi-exclamation-circle-fill"; // Atenção
        } else {
            return "bi-check-circle-fill"; // OK
        }
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    public PlanoAssinatura getPlano() {
        return plano;
    }
    
    public void setPlano(PlanoAssinatura plano) {
        this.plano = plano;
    }
    
    public int getLancamentosUsados() {
        return lancamentosUsados;
    }
    
    public void setLancamentosUsados(int lancamentosUsados) {
        this.lancamentosUsados = lancamentosUsados;
    }
    
    public int getLancamentosLimite() {
        return lancamentosLimite;
    }
    
    public void setLancamentosLimite(int lancamentosLimite) {
        this.lancamentosLimite = lancamentosLimite;
    }
    
    public BigDecimal getPercentualUso() {
        return percentualUso;
    }
    
    public void setPercentualUso(BigDecimal percentualUso) {
        this.percentualUso = percentualUso;
    }
    
    public int getDiasRestantesNoMes() {
        return diasRestantesNoMes;
    }
    
    public void setDiasRestantesNoMes(int diasRestantesNoMes) {
        this.diasRestantesNoMes = diasRestantesNoMes;
    }
    
    public boolean isProximoDoLimite() {
        return proximoDoLimite;
    }
    
    public void setProximoDoLimite(boolean proximoDoLimite) {
        this.proximoDoLimite = proximoDoLimite;
    }
    
    public boolean isLimiteExcedido() {
        return limiteExcedido;
    }
    
    public void setLimiteExcedido(boolean limiteExcedido) {
        this.limiteExcedido = limiteExcedido;
    }
    
    public String getMensagemAlerta() {
        return mensagemAlerta;
    }
    
    public void setMensagemAlerta(String mensagemAlerta) {
        this.mensagemAlerta = mensagemAlerta;
    }
}

