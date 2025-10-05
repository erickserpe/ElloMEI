package br.com.ellomei.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma assinatura de plano no sistema SaaS.
 * 
 * Esta entidade gerencia o ciclo de vida completo de uma assinatura:
 * - Criação (trial ou paga)
 * - Renovação automática
 * - Suspensão por falta de pagamento
 * - Cancelamento
 * - Expiração
 * 
 * Relacionamentos:
 * - Cada assinatura pertence a um único usuário
 * - Um usuário pode ter múltiplas assinaturas (histórico)
 * - Apenas uma assinatura pode estar ATIVA por vez
 * 
 * Integração com Mercado Pago:
 * - idPagamentoExterno: ID da subscription no Mercado Pago
 * - idClienteExterno: ID do customer no Mercado Pago
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "assinaturas")
public class Assinatura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Usuário que possui esta assinatura.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull
    private Usuario usuario;
    
    /**
     * Plano contratado (FREE ou PRO).
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private PlanoAssinatura plano;
    
    /**
     * Status atual da assinatura.
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private StatusAssinatura status;
    
    /**
     * Data de início da assinatura.
     */
    @NotNull
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;
    
    /**
     * Data da próxima cobrança (renovação mensal).
     */
    @Column(name = "data_proxima_cobranca")
    private LocalDate dataProximaCobranca;
    
    /**
     * Data de expiração da assinatura.
     * Null para assinaturas recorrentes ativas.
     */
    @Column(name = "data_expiracao")
    private LocalDate dataExpiracao;
    
    /**
     * Data de cancelamento (se aplicável).
     */
    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;
    
    /**
     * Valor mensal da assinatura em reais.
     */
    @Positive
    @Column(name = "valor_mensal", precision = 10, scale = 2)
    private BigDecimal valorMensal;
    
    /**
     * Forma de pagamento escolhida.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 20)
    private FormaPagamento formaPagamento;
    
    /**
     * ID da subscription no Mercado Pago.
     * Usado para gerenciar renovações automáticas.
     */
    @Column(name = "id_pagamento_externo", length = 100)
    private String idPagamentoExterno;
    
    /**
     * ID do customer no Mercado Pago.
     * Usado para associar múltiplos pagamentos ao mesmo cliente.
     */
    @Column(name = "id_cliente_externo", length = 100)
    private String idClienteExterno;
    
    /**
     * Motivo do cancelamento (se aplicável).
     */
    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;
    
    /**
     * Indica se a assinatura está em período de trial.
     */
    @Column(name = "is_trial")
    private boolean isTrial = false;

    /**
     * Número de tentativas de pagamento falhadas.
     * Usado para controlar retries automáticos.
     */
    @Column(name = "tentativas_pagamento")
    private int tentativasPagamento = 0;

    /**
     * Data da última tentativa de pagamento.
     */
    @Column(name = "data_ultima_tentativa")
    private LocalDateTime dataUltimaTentativa;

    /**
     * Motivo da última falha de pagamento.
     */
    @Column(name = "motivo_falha_pagamento", length = 500)
    private String motivoFalhaPagamento;

    /**
     * Data de criação do registro.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data da última atualização do registro.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ==================== LIFECYCLE CALLBACKS ====================
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * Verifica se a assinatura está ativa.
     */
    public boolean isAtiva() {
        return status == StatusAssinatura.ATIVA;
    }
    
    /**
     * Verifica se a assinatura está em período de trial.
     */
    public boolean isEmTrial() {
        return status == StatusAssinatura.TRIAL || isTrial;
    }
    
    /**
     * Verifica se a assinatura expirou.
     */
    public boolean isExpirada() {
        if (dataExpiracao == null) {
            return false;
        }
        return LocalDate.now().isAfter(dataExpiracao);
    }
    
    /**
     * Verifica se a assinatura precisa ser renovada.
     */
    public boolean precisaRenovar() {
        if (dataProximaCobranca == null) {
            return false;
        }
        return LocalDate.now().isAfter(dataProximaCobranca) || 
               LocalDate.now().isEqual(dataProximaCobranca);
    }
    
    /**
     * Cancela a assinatura.
     */
    public void cancelar(String motivo) {
        this.status = StatusAssinatura.CANCELADA;
        this.dataCancelamento = LocalDateTime.now();
        this.motivoCancelamento = motivo;
        this.dataExpiracao = LocalDate.now();
    }
    
    /**
     * Suspende a assinatura por falta de pagamento.
     */
    public void suspender() {
        this.status = StatusAssinatura.SUSPENSA;
        // Dar 7 dias de prazo antes de expirar
        this.dataExpiracao = LocalDate.now().plusDays(7);
    }

    /**
     * Suspende a assinatura com motivo específico.
     */
    public void suspender(String motivoFalha) {
        this.status = StatusAssinatura.SUSPENSA;
        this.motivoFalhaPagamento = motivoFalha;
        this.dataExpiracao = LocalDate.now().plusDays(7);
        this.dataUltimaTentativa = LocalDateTime.now();
    }
    
    /**
     * Reativa a assinatura após pagamento.
     */
    public void reativar() {
        this.status = StatusAssinatura.ATIVA;
        this.dataExpiracao = null;
        // Próxima cobrança em 30 dias
        this.dataProximaCobranca = LocalDate.now().plusDays(30);
    }
    
    /**
     * Renova a assinatura para o próximo mês.
     */
    public void renovar() {
        this.dataProximaCobranca = LocalDate.now().plusDays(30);
        this.status = StatusAssinatura.ATIVA;
    }

    /**
     * Incrementa o contador de tentativas de pagamento.
     */
    public void incrementarTentativasPagamento() {
        this.tentativasPagamento++;
        this.dataUltimaTentativa = LocalDateTime.now();
    }

    /**
     * Reseta o contador de tentativas de pagamento.
     */
    public void resetarTentativasPagamento() {
        this.tentativasPagamento = 0;
        this.motivoFalhaPagamento = null;
        this.dataUltimaTentativa = null;
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
    
    public PlanoAssinatura getPlano() {
        return plano;
    }
    
    public void setPlano(PlanoAssinatura plano) {
        this.plano = plano;
    }
    
    public StatusAssinatura getStatus() {
        return status;
    }
    
    public void setStatus(StatusAssinatura status) {
        this.status = status;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public LocalDate getDataProximaCobranca() {
        return dataProximaCobranca;
    }
    
    public void setDataProximaCobranca(LocalDate dataProximaCobranca) {
        this.dataProximaCobranca = dataProximaCobranca;
    }
    
    public LocalDate getDataExpiracao() {
        return dataExpiracao;
    }
    
    public void setDataExpiracao(LocalDate dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }
    
    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }
    
    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }
    
    public BigDecimal getValorMensal() {
        return valorMensal;
    }
    
    public void setValorMensal(BigDecimal valorMensal) {
        this.valorMensal = valorMensal;
    }
    
    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }
    
    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
    
    public String getIdPagamentoExterno() {
        return idPagamentoExterno;
    }
    
    public void setIdPagamentoExterno(String idPagamentoExterno) {
        this.idPagamentoExterno = idPagamentoExterno;
    }
    
    public String getIdClienteExterno() {
        return idClienteExterno;
    }
    
    public void setIdClienteExterno(String idClienteExterno) {
        this.idClienteExterno = idClienteExterno;
    }
    
    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }
    
    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }
    
    public boolean isTrial() {
        return isTrial;
    }

    public void setTrial(boolean trial) {
        isTrial = trial;
    }

    public int getTentativasPagamento() {
        return tentativasPagamento;
    }

    public void setTentativasPagamento(int tentativasPagamento) {
        this.tentativasPagamento = tentativasPagamento;
    }

    public LocalDateTime getDataUltimaTentativa() {
        return dataUltimaTentativa;
    }

    public void setDataUltimaTentativa(LocalDateTime dataUltimaTentativa) {
        this.dataUltimaTentativa = dataUltimaTentativa;
    }

    public String getMotivoFalhaPagamento() {
        return motivoFalhaPagamento;
    }

    public void setMotivoFalhaPagamento(String motivoFalhaPagamento) {
        this.motivoFalhaPagamento = motivoFalhaPagamento;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

