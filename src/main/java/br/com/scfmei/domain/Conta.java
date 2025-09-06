package br.com.scfmei.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal; // IMPORTAÇÃO ADICIONADA

@Entity
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeConta;
    private String tipo;

    // --- CORRIGIDO AQUI ---
    private BigDecimal saldoInicial;
    private BigDecimal saldoAtual;

    public Conta() {
    }

    // Getters e Setters (também corrigidos)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeConta() {
        return nomeConta;
    }

    public void setNomeConta(String nomeConta) {
        this.nomeConta = nomeConta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // --- CORRIGIDO AQUI ---
    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    // --- CORRIGIDO AQUI ---
    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    // --- CORRIGIDO AQUI ---
    public BigDecimal getSaldoAtual() {
        return saldoAtual;
    }

    // --- CORRIGIDO AQUI ---
    public void setSaldoAtual(BigDecimal saldoAtual) {
        this.saldoAtual = saldoAtual;
    }
}