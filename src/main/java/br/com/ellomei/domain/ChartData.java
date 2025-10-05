package br.com.ellomei.domain;

import java.math.BigDecimal;

// Data Transfer Object (DTO) para os dados do gráfico
public class ChartData {
    private String label; // Ex: "Supermercado"
    private BigDecimal value; // Ex: 500.00

    public ChartData(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }

    // Getters e Setters
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}