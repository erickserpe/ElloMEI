package br.com.scfmei.domain;

public enum StatusLancamento {
    PAGO("Pago"),
    A_PAGAR("A Pagar");

    private final String descricao;

    StatusLancamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}