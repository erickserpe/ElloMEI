// src/main/java/br/com/ellomei/domain/Contato.java
package br.com.ellomei.domain;

import br.com.ellomei.validation.anotations.CPF;
import br.com.ellomei.validation.anotations.CNPJ;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;

@Entity
@Filter(name = "tenantFilter", condition = "usuario_id = :tenantId")
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O tipo de contato é obrigatório.")
    @Enumerated(EnumType.STRING)
    private TipoContato tipoContato;

    // Campos para Pessoa Física
    private String nomeCompleto;
    @CPF
    private String cpf;

    // Campos para Pessoa Jurídica
    private String razaoSocial;
    private String nomeFantasia;
    @CNPJ
    private String cnpj;

    // --- NOVA ADIÇÃO ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    // -------------------

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoContato getTipoContato() { return tipoContato; }
    public void setTipoContato(TipoContato tipoContato) { this.tipoContato = tipoContato; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    // --- NOVOS GETTERS E SETTERS ---
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    // -----------------------------

    @Transient
    public String getNomeExibicao() {
        if (this.tipoContato == TipoContato.PESSOA_JURIDICA && this.nomeFantasia != null && !this.nomeFantasia.isBlank()) {
            return this.nomeFantasia;
        }
        return this.nomeCompleto;
    }
}