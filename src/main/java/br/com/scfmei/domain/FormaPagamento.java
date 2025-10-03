package br.com.scfmei.domain;

/**
 * Enum que representa as formas de pagamento aceitas no sistema.
 * 
 * Formas disponíveis:
 * - CARTAO_CREDITO: Pagamento recorrente via cartão de crédito
 * - PIX: Pagamento via PIX (QR Code)
 * - BOLETO: Pagamento via boleto bancário
 * 
 * Integração com Mercado Pago:
 * - CARTAO_CREDITO: payment_method_id = "credit_card"
 * - PIX: payment_method_id = "pix"
 * - BOLETO: payment_method_id = "bolbradesco"
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public enum FormaPagamento {
    
    /**
     * Pagamento recorrente via cartão de crédito.
     * Renovação automática mensal.
     */
    CARTAO_CREDITO("Cartão de Crédito", "credit_card", true),
    
    /**
     * Pagamento via PIX.
     * Pagamento instantâneo, mas não recorrente.
     */
    PIX("PIX", "pix", false),
    
    /**
     * Pagamento via boleto bancário.
     * Pagamento não recorrente, vencimento em 3 dias.
     */
    BOLETO("Boleto Bancário", "bolbradesco", false);
    
    private final String descricao;
    private final String mercadoPagoId;
    private final boolean recorrente;
    
    FormaPagamento(String descricao, String mercadoPagoId, boolean recorrente) {
        this.descricao = descricao;
        this.mercadoPagoId = mercadoPagoId;
        this.recorrente = recorrente;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public String getMercadoPagoId() {
        return mercadoPagoId;
    }
    
    public boolean isRecorrente() {
        return recorrente;
    }
}

