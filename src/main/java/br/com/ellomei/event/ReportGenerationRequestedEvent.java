package br.com.ellomei.event;

import br.com.ellomei.domain.StatusLancamento;
import br.com.ellomei.domain.TipoLancamento;
import br.com.ellomei.domain.Usuario;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.Map;

/**
 * Evento de domínio disparado quando um relatório é solicitado.
 * 
 * Este evento segue o padrão de Eventos de Domínio do Spring, permitindo
 * desacoplamento completo entre a solicitação de relatório (controller) e
 * a geração efetiva do PDF (listener).
 * 
 * Benefícios:
 * - Desacoplamento: Controller não espera a geração do PDF
 * - Escalabilidade: Geração de PDF pode ser processada em background
 * - Resiliência: Falhas na geração não afetam a resposta HTTP
 * - Flexibilidade: Múltiplos listeners podem reagir ao evento
 * 
 * Fluxo:
 * 1. Usuário solicita relatório via HTTP
 * 2. Controller publica ReportGenerationRequestedEvent
 * 3. Controller retorna HTTP 202 (Accepted) imediatamente
 * 4. Listener processa o evento em background
 * 5. PDF é gerado e salvo (futuramente: notificação ao usuário)
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
public class ReportGenerationRequestedEvent extends ApplicationEvent {
    
    private final String tipoRelatorio;
    private final String tipoVisao;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final Long contaId;
    private final Long contatoId;
    private final TipoLancamento tipo;
    private final Long categoriaId;
    private final Boolean comNotaFiscal;
    private final String descricao;
    private final StatusLancamento status;
    private final Usuario usuario;
    private final Map<String, Object> variaveisAdicionais;

    /**
     * Construtor privado - use o Builder para criar instâncias.
     */
    private ReportGenerationRequestedEvent(Object source, Builder builder) {
        super(source);
        this.tipoRelatorio = builder.tipoRelatorio;
        this.tipoVisao = builder.tipoVisao;
        this.dataInicio = builder.dataInicio;
        this.dataFim = builder.dataFim;
        this.contaId = builder.contaId;
        this.contatoId = builder.contatoId;
        this.tipo = builder.tipo;
        this.categoriaId = builder.categoriaId;
        this.comNotaFiscal = builder.comNotaFiscal;
        this.descricao = builder.descricao;
        this.status = builder.status;
        this.usuario = builder.usuario;
        this.variaveisAdicionais = builder.variaveisAdicionais;
    }

    // Getters
    public String getTipoRelatorio() { return tipoRelatorio; }
    public String getTipoVisao() { return tipoVisao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public Long getContaId() { return contaId; }
    public Long getContatoId() { return contatoId; }
    public TipoLancamento getTipo() { return tipo; }
    public Long getCategoriaId() { return categoriaId; }
    public Boolean getComNotaFiscal() { return comNotaFiscal; }
    public String getDescricao() { return descricao; }
    public StatusLancamento getStatus() { return status; }
    public Usuario getUsuario() { return usuario; }
    public Map<String, Object> getVariaveisAdicionais() { return variaveisAdicionais; }

    /**
     * Builder para criar instâncias de ReportGenerationRequestedEvent.
     * 
     * Uso:
     * <pre>
     * ReportGenerationRequestedEvent event = ReportGenerationRequestedEvent.builder()
     *     .source(this)
     *     .tipoRelatorio("FATURAMENTO_DINAMICO")
     *     .tipoVisao("OFICIAL")
     *     .usuario(usuario)
     *     .dataInicio(dataInicio)
     *     .dataFim(dataFim)
     *     .build();
     * </pre>
     */
    public static class Builder {
        private Object source;
        private String tipoRelatorio;
        private String tipoVisao;
        private LocalDate dataInicio;
        private LocalDate dataFim;
        private Long contaId;
        private Long contatoId;
        private TipoLancamento tipo;
        private Long categoriaId;
        private Boolean comNotaFiscal;
        private String descricao;
        private StatusLancamento status;
        private Usuario usuario;
        private Map<String, Object> variaveisAdicionais;

        public Builder source(Object source) {
            this.source = source;
            return this;
        }

        public Builder tipoRelatorio(String tipoRelatorio) {
            this.tipoRelatorio = tipoRelatorio;
            return this;
        }

        public Builder tipoVisao(String tipoVisao) {
            this.tipoVisao = tipoVisao;
            return this;
        }

        public Builder dataInicio(LocalDate dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public Builder dataFim(LocalDate dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        public Builder contaId(Long contaId) {
            this.contaId = contaId;
            return this;
        }

        public Builder contatoId(Long contatoId) {
            this.contatoId = contatoId;
            return this;
        }

        public Builder tipo(TipoLancamento tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder categoriaId(Long categoriaId) {
            this.categoriaId = categoriaId;
            return this;
        }

        public Builder comNotaFiscal(Boolean comNotaFiscal) {
            this.comNotaFiscal = comNotaFiscal;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder status(StatusLancamento status) {
            this.status = status;
            return this;
        }

        public Builder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }

        public Builder variaveisAdicionais(Map<String, Object> variaveisAdicionais) {
            this.variaveisAdicionais = variaveisAdicionais;
            return this;
        }

        public ReportGenerationRequestedEvent build() {
            if (source == null) {
                throw new IllegalStateException("Source is required");
            }
            if (usuario == null) {
                throw new IllegalStateException("Usuario is required");
            }
            if (tipoRelatorio == null) {
                throw new IllegalStateException("TipoRelatorio is required");
            }
            return new ReportGenerationRequestedEvent(source, this);
        }
    }

    /**
     * Cria um novo builder para construir o evento.
     */
    public static Builder builder() {
        return new Builder();
    }
}

