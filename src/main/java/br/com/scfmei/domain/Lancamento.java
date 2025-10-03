// src/main/java/br/com/scfmei/domain/Lancamento.java
package br.com.scfmei.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "usuario_id = :tenantId")
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String grupoOperacao;

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
    @JoinColumn(name = "contato_id")
    private Contato contato;

    private Boolean comNotaFiscal;

    @OneToMany(mappedBy = "lancamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comprovante> comprovantes = new ArrayList<>();

    @NotNull(message = "Status é obrigatório.")
    @Enumerated(EnumType.STRING)
    private StatusLancamento status;

    // --- NOVA ADIÇÃO ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    // -------------------


    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGrupoOperacao() { return grupoOperacao; }
    public void setGrupoOperacao(String grupoOperacao) { this.grupoOperacao = grupoOperacao; }
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
    public Contato getContato() { return contato; }
    public void setContato(Contato contato) { this.contato = contato; }
    public Boolean getComNotaFiscal() { return comNotaFiscal; }
    public void setComNotaFiscal(Boolean comNotaFiscal) { this.comNotaFiscal = comNotaFiscal; }
    public List<Comprovante> getComprovantes() { return comprovantes; }
    public void setComprovantes(List<Comprovante> comprovantes) { this.comprovantes = comprovantes; }
    public StatusLancamento getStatus() { return status; }
    public void setStatus(StatusLancamento status) { this.status = status; }

    // --- NOVOS GETTERS E SETTERS ---
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    // -----------------------------
}