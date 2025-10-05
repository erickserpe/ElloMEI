package br.com.ellomei.domain;

import jakarta.persistence.*;

/**
 * Entidade que representa uma role (papel/permissão) no sistema.
 * 
 * Permite um gerenciamento de permissões mais flexível e granular,
 * substituindo o campo String roles por um relacionamento ManyToMany.
 * 
 * Exemplos de roles:
 * - ROLE_USER: Usuário comum do sistema
 * - ROLE_ADMIN: Administrador com permissões especiais
 * - ROLE_PREMIUM: Usuário com plano premium (futuro)
 */
@Entity
@Table(name = "role")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome da role seguindo a convenção do Spring Security.
     * Deve começar com "ROLE_" (ex: ROLE_USER, ROLE_ADMIN).
     */
    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    // --- Construtores ---

    public Role() {
    }

    public Role(String nome) {
        this.nome = nome;
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // --- equals, hashCode e toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return nome != null && nome.equals(role.nome);
    }

    @Override
    public int hashCode() {
        return nome != null ? nome.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}

