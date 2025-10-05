package br.com.ellomei.domain;

/**
 * Enum que representa os possíveis status de uma assinatura no sistema SaaS.
 * 
 * Status disponíveis:
 * - TRIAL: Período de teste gratuito (7-30 dias)
 * - ATIVA: Assinatura paga e ativa
 * - SUSPENSA: Assinatura suspensa por falta de pagamento
 * - CANCELADA: Assinatura cancelada pelo usuário
 * - EXPIRADA: Assinatura que expirou e não foi renovada
 * 
 * Fluxo típico:
 * TRIAL → ATIVA → (SUSPENSA) → CANCELADA/EXPIRADA
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
public enum StatusAssinatura {
    
    /**
     * Período de teste gratuito.
     * Usuário tem acesso completo ao plano PRO por tempo limitado.
     */
    TRIAL,
    
    /**
     * Assinatura ativa e paga.
     * Usuário tem acesso completo aos recursos do plano contratado.
     */
    ATIVA,
    
    /**
     * Assinatura suspensa por falta de pagamento.
     * Usuário tem acesso limitado (somente leitura) até regularizar.
     */
    SUSPENSA,
    
    /**
     * Assinatura cancelada pelo usuário.
     * Usuário volta para o plano FREE.
     */
    CANCELADA,
    
    /**
     * Assinatura expirada.
     * Não foi renovada após o período de suspensão.
     */
    EXPIRADA
}

