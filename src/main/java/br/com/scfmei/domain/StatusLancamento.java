package br.com.scfmei.domain;

public enum StatusLancamento {
    // Status for both income and expenses
    PAGO("Pago"),

    // Status specific to expenses
    A_PAGAR("A Pagar"),

    // Status specific to income
    A_RECEBER("A Receber");

    private final String descricao;

    StatusLancamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Returns status options appropriate for income transactions
     */
    public static StatusLancamento[] getIncomeStatuses() {
        return new StatusLancamento[]{PAGO, A_RECEBER};
    }

    /**
     * Returns status options appropriate for expense transactions
     */
    public static StatusLancamento[] getExpenseStatuses() {
        return new StatusLancamento[]{PAGO, A_PAGAR};
    }
}