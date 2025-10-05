package br.com.ellomei.domain;

public enum TipoContato {
    PESSOA_FISICA("Contato Física"),
    PESSOA_JURIDICA("Contato Jurídica");

    private final String descricao;

    TipoContato(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}