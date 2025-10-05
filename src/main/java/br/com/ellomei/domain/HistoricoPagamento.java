package br.com.ellomei.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa o histórico de pagamentos de um usuário.
 * 
 * Armazena todos os pagamentos realizados (aprovados, recusados, reembolsados)
 * para fins de auditoria e transparência.
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "historico_pagamentos")
public class HistoricoPagamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assinatura_id")
    private Assinatura assinatura;
    
    /**
     * ID do pagamento no Mercado Pago.
     */
    @Column(name = "id_pagamento_externo")
    private String idPagamentoExterno;
    
    /**
     * Status do pagamento.
     * Valores possíveis: approved, rejected, refunded, pending, cancelled
     */
    @Column(nullable = false, length = 50)
    private String status;
    
    /**
     * Descrição do pagamento.
     */
    @Column(length = 500)
    private String descricao;
    
    /**
     * Valor do pagamento.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    /**
     * Forma de pagamento utilizada.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 50)
    private FormaPagamento formaPagamento;
    
    /**
     * Data e hora do pagamento.
     */
    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;
    
    /**
     * Motivo da recusa (se aplicável).
     */
    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;
    
    /**
     * Dados adicionais do pagamento (JSON).
     */
    @Column(name = "dados_adicionais", columnDefinition = "TEXT")
    private String dadosAdicionais;
    
    // ==================== CONSTRUCTORS ====================
    
    public HistoricoPagamento() {
    }
    
    public HistoricoPagamento(Usuario usuario, Assinatura assinatura, String idPagamentoExterno,
                             String status, String descricao, BigDecimal valor, 
                             FormaPagamento formaPagamento) {
        this.usuario = usuario;
        this.assinatura = assinatura;
        this.idPagamentoExterno = idPagamentoExterno;
        this.status = status;
        this.descricao = descricao;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.dataPagamento = LocalDateTime.now();
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * Verifica se o pagamento foi aprovado.
     */
    public boolean isAprovado() {
        return "approved".equals(status);
    }
    
    /**
     * Verifica se o pagamento foi recusado.
     */
    public boolean isRecusado() {
        return "rejected".equals(status);
    }
    
    /**
     * Verifica se o pagamento foi reembolsado.
     */
    public boolean isReembolsado() {
        return "refunded".equals(status);
    }
    
    /**
     * Verifica se o pagamento está pendente.
     */
    public boolean isPendente() {
        return "pending".equals(status);
    }
    
    /**
     * Retorna a cor do badge baseada no status.
     */
    public String getCorBadge() {
        return switch (status) {
            case "approved" -> "success";
            case "rejected", "cancelled" -> "danger";
            case "refunded" -> "warning";
            case "pending" -> "info";
            default -> "secondary";
        };
    }
    
    /**
     * Retorna o ícone baseado no status.
     */
    public String getIcone() {
        return switch (status) {
            case "approved" -> "bi-check-circle-fill";
            case "rejected", "cancelled" -> "bi-x-circle-fill";
            case "refunded" -> "bi-arrow-counterclockwise";
            case "pending" -> "bi-clock-history";
            default -> "bi-question-circle";
        };
    }
    
    /**
     * Retorna o status traduzido.
     */
    public String getStatusTraduzido() {
        return switch (status) {
            case "approved" -> "Aprovado";
            case "rejected" -> "Recusado";
            case "refunded" -> "Reembolsado";
            case "pending" -> "Pendente";
            case "cancelled" -> "Cancelado";
            default -> status;
        };
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Assinatura getAssinatura() {
        return assinatura;
    }
    
    public void setAssinatura(Assinatura assinatura) {
        this.assinatura = assinatura;
    }
    
    public String getIdPagamentoExterno() {
        return idPagamentoExterno;
    }
    
    public void setIdPagamentoExterno(String idPagamentoExterno) {
        this.idPagamentoExterno = idPagamentoExterno;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }
    
    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
    
    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }
    
    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
    
    public String getMotivoRecusa() {
        return motivoRecusa;
    }
    
    public void setMotivoRecusa(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa;
    }
    
    public String getDadosAdicionais() {
        return dadosAdicionais;
    }
    
    public void setDadosAdicionais(String dadosAdicionais) {
        this.dadosAdicionais = dadosAdicionais;
    }
}

