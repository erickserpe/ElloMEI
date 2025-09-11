package br.com.scfmei.domain;

import java.math.BigDecimal;

// DTO (Data Transfer Object) - um objeto simples para carregar dados do formulário
public class PagamentoDTO {
    private Long conta; // Usamos o mesmo nome do 'name' do nosso select: name="pagamentos[0].conta"
    private BigDecimal valor; // Usamos o mesmo nome do 'name' do nosso input: name="pagamentos[0].valor"

    // Getters e Setters
    public Long getConta() { return conta; }
    public void setConta(Long conta) { this.conta = conta; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}