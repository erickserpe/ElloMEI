package br.com.scfmei.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa um grupo de lançamentos (transação com múltiplos pagamentos)
 * para exibição na listagem principal
 */
public class LancamentoGrupoDTO {
    private Long id; // ID do primeiro lançamento do grupo (para edição/exclusão)
    private String grupoOperacao;
    private String descricao;
    private LocalDate data;
    private TipoLancamento tipo;
    private StatusLancamento status;
    private BigDecimal valorTotal; // Soma de todos os valores do grupo
    private String contasDescricao; // Descrição das contas envolvidas
    private CategoriaDespesa categoriaDespesa;
    private Contato contato;
    private Boolean comNotaFiscal;
    private List<Comprovante> comprovantes;

    // Construtores
    public LancamentoGrupoDTO() {}

    public LancamentoGrupoDTO(Long id, String grupoOperacao, String descricao, LocalDate data, 
                             TipoLancamento tipo, StatusLancamento status, BigDecimal valorTotal, 
                             String contasDescricao, CategoriaDespesa categoriaDespesa, 
                             Contato contato, Boolean comNotaFiscal, List<Comprovante> comprovantes) {
        this.id = id;
        this.grupoOperacao = grupoOperacao;
        this.descricao = descricao;
        this.data = data;
        this.tipo = tipo;
        this.status = status;
        this.valorTotal = valorTotal;
        this.contasDescricao = contasDescricao;
        this.categoriaDespesa = categoriaDespesa;
        this.contato = contato;
        this.comNotaFiscal = comNotaFiscal;
        this.comprovantes = comprovantes;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGrupoOperacao() { return grupoOperacao; }
    public void setGrupoOperacao(String grupoOperacao) { this.grupoOperacao = grupoOperacao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }

    public StatusLancamento getStatus() { return status; }
    public void setStatus(StatusLancamento status) { this.status = status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public String getContasDescricao() { return contasDescricao; }
    public void setContasDescricao(String contasDescricao) { this.contasDescricao = contasDescricao; }

    public CategoriaDespesa getCategoriaDespesa() { return categoriaDespesa; }
    public void setCategoriaDespesa(CategoriaDespesa categoriaDespesa) { this.categoriaDespesa = categoriaDespesa; }

    public Contato getContato() { return contato; }
    public void setContato(Contato contato) { this.contato = contato; }

    public Boolean getComNotaFiscal() { return comNotaFiscal; }
    public void setComNotaFiscal(Boolean comNotaFiscal) { this.comNotaFiscal = comNotaFiscal; }

    public List<Comprovante> getComprovantes() { return comprovantes; }
    public void setComprovantes(List<Comprovante> comprovantes) { this.comprovantes = comprovantes; }
}
