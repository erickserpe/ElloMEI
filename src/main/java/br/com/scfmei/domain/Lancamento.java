package br.com.scfmei.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descrição é obrigatória.")
    @Size(min = 3, max = 100, message = "Descrição deve ter entre 3 e 100 caracteres.")
    private String descricao;

    @NotNull(message = "Valor é obrigatório.")
    @Positive(message = "Valor deve ser positivo.")
    private BigDecimal valor;

    @NotNull(message = "Data é obrigatória.")
    private LocalDate data;

    @NotNull(message = "Tipo é obrigatório.")
    @Enumerated(EnumType.STRING)
    private TipoLancamento tipo;

    @NotNull(message = "Conta é obrigatória.")
    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "categoria_despesa_id")
    private CategoriaDespesa categoriaDespesa;

    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    private String comprovantePath;

    // --- ATRIBUTO QUE ESTAVA CAUSANDO O ERRO ---
    private Boolean comNotaFiscal;


    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }
    public Conta getConta() { return conta; }
    public void setConta(Conta conta) { this.conta = conta; }
    public CategoriaDespesa getCategoriaDespesa() { return categoriaDespesa; }
    public void setCategoriaDespesa(CategoriaDespesa categoriaDespesa) { this.categoriaDespesa = categoriaDespesa; }
    public Pessoa getPessoa() { return pessoa; }
    public void setPessoa(Pessoa pessoa) { this.pessoa = pessoa; }
    public String getComprovantePath() { return comprovantePath; }
    public void setComprovantePath(String comprovantePath) { this.comprovantePath = comprovantePath; }

    // --- GETTER E SETTER PARA O NOVO CAMPO ---
    public Boolean getComNotaFiscal() {
        return comNotaFiscal;
    }
    public void setComNotaFiscal(Boolean comNotaFiscal) {
        this.comNotaFiscal = comNotaFiscal;
    }
}