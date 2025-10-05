package br.com.ellomei.domain;

import jakarta.persistence.*;

@Entity
public class Comprovante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pathArquivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lancamento_id")
    private Lancamento lancamento;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPathArquivo() { return pathArquivo; }
    public void setPathArquivo(String pathArquivo) { this.pathArquivo = pathArquivo; }
    public Lancamento getLancamento() { return lancamento; }
    public void setLancamento(Lancamento lancamento) { this.lancamento = lancamento; }
}