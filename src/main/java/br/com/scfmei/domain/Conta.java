package br.com.scfmei.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // Avisa ao JPA que esta classe representa uma tabela no banco de dados
public class Conta {

    @Id // Marca o campo 'id' como a chave primária da tabela
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Diz ao banco para gerar o valor do ID automaticamente
    private Long id;

    private String nomeConta; // Ex: "Sicoob", "Caixa Físico"
    private String tipo;      // Ex: "Conta Corrente", "Caixa"
    private Double saldoInicial;
    private Double saldoAtual;

    // Construtor padrão (obrigatório para o JPA)
    public Conta() {
    }

    // Getters e Setters (para acessar e modificar os atributos)
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

    public Double getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(Double saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public Double getSaldoAtual() {
        return saldoAtual;
    }

    public void setSaldoAtual(Double saldoAtual) {
        this.saldoAtual = saldoAtual;
    }
}