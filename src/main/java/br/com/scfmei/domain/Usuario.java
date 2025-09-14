package br.com.scfmei.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDate;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dados de Acesso
    @Column(unique = true)
    private String username; // Ou email
    private String password;

    // Dados Pessoais
    private String nomeCompleto;
    private String cpf;

    // Dados da Empresa (MEI)
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;

    private String roles; // Papéis/Permissões do usuário
    private LocalDate dataAberturaMei;
    // Getters e Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public LocalDate getDataAberturaMei() {
        return dataAberturaMei;
    }
    public void setDataAberturaMei(LocalDate dataAberturaMei) {
        this.dataAberturaMei = dataAberturaMei;
    }
}