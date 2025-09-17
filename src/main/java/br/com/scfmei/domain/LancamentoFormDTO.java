package br.com.scfmei.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LancamentoFormDTO {
    private String grupoOperacao;
    private String descricao;
    private LocalDate data;
    private TipoLancamento tipo;
    private CategoriaDespesa categoriaDespesa;
    private Contato contato;
    private Boolean comNotaFiscal;

    // O campo para os comprovantes que já existe
    private List<Comprovante> comprovantes = new ArrayList<>();

    private List<PagamentoDTO> pagamentos = new ArrayList<>();

    public LancamentoFormDTO() {
        this.pagamentos.add(new PagamentoDTO());
    }

    // --- Getters e Setters ---

    public String getGrupoOperacao() { return grupoOperacao; }
    public void setGrupoOperacao(String grupoOperacao) { this.grupoOperacao = grupoOperacao; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }
    public CategoriaDespesa getCategoriaDespesa() { return categoriaDespesa; }
    public void setCategoriaDespesa(CategoriaDespesa categoriaDespesa) { this.categoriaDespesa = categoriaDespesa; }
    public Contato getContato() { return contato; }
    public void setContato(Contato contato) { this.contato = contato; }public Boolean getComNotaFiscal() { return comNotaFiscal; }
    public void setComNotaFiscal(Boolean comNotaFiscal) { this.comNotaFiscal = comNotaFiscal; }
    public List<PagamentoDTO> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<PagamentoDTO> pagamentos) { this.pagamentos = pagamentos; }

    // --- MÉTODOS QUE FALTAVAM ---
    public List<Comprovante> getComprovantes() {
        return comprovantes;
    }
    public void setComprovantes(List<Comprovante> comprovantes) {
        this.comprovantes = comprovantes;
    }
}