// src/main/java/br/com/scfmei/domain/CategoriaDespesa.java
package br.com.scfmei.domain;

import jakarta.persistence.*;

@Entity
public class CategoriaDespesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // --- NOVA ADIÇÃO ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    // -------------------

    // Construtor
    public CategoriaDespesa() {
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    // --- NOVOS GETTERS E SETTERS ---
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    // -----------------------------
}