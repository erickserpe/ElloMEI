package br.com.scfmei.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O tipo de contato é obrigatório.")
    @Enumerated(EnumType.STRING)
    private TipoContato tipoContato;

    // Campos para Pessoa Física
    private String nomeCompleto;
    private String cpf;

    // Campos para Pessoa Jurídica
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;

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

    // Método útil para exibir o nome correto na interface
    @Transient // Diz ao JPA para ignorar este método, ele não é uma coluna
    public String getNomeExibicao() {
        if (this.tipoContato == TipoContato.PESSOA_JURIDICA && this.nomeFantasia != null && !this.nomeFantasia.isBlank()) {
            return this.nomeFantasia;
        }
        return this.nomeCompleto;
    }
}