package br.com.ellomei;

import static br.com.scfmei.TestHelper.*;

import br.com.ellomei.domain.PlanoAssinatura;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.domain.Role;
import br.com.ellomei.event.ReportGenerationRequestedEvent;
import br.com.ellomei.repository.UsuarioRepository;
import br.com.ellomei.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de Integração para Geração Assíncrona de Relatórios.
 * 
 * Este teste verifica que o padrão de Eventos de Domínio está funcionando
 * corretamente para a geração de relatórios em background.
 * 
 * Fluxo testado:
 * 1. Controller recebe requisição de relatório
 * 2. Controller publica evento ReportGenerationRequestedEvent
 * 3. Controller retorna HTTP 202 (Accepted) imediatamente
 * 4. Listener processa o evento em background
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReportGenerationEventTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Usuario usuarioTeste;

    @BeforeEach
    void setUp() {
        // Limpa usuários de teste anteriores
        usuarioRepository.deleteAll();

        // Cria usuário de teste
        usuarioTeste = new Usuario();
        usuarioTeste.setUsername("reportuser");
        usuarioTeste.setPassword(passwordEncoder.encode("senha123"));
        usuarioTeste.setRoles(TestHelper.createUserRole(roleRepository));
        usuarioTeste.setPlano(PlanoAssinatura.PRO);
        usuarioTeste = usuarioRepository.save(usuarioTeste);
    }

    /**
     * Teste 1: Verifica que o evento pode ser criado com o Builder.
     */
    @Test
    void devePermitirCriarEventoComBuilder() {
        // Arrange & Act
        ReportGenerationRequestedEvent event = ReportGenerationRequestedEvent.builder()
                .source(this)
                .tipoRelatorio("FATURAMENTO_DINAMICO")
                .tipoVisao("OFICIAL")
                .dataInicio(LocalDate.of(2025, 1, 1))
                .dataFim(LocalDate.of(2025, 12, 31))
                .usuario(usuarioTeste)
                .build();

        // Assert
        assertNotNull(event);
        assertEquals("FATURAMENTO_DINAMICO", event.getTipoRelatorio());
        assertEquals("OFICIAL", event.getTipoVisao());
        assertEquals(usuarioTeste, event.getUsuario());
        assertEquals(LocalDate.of(2025, 1, 1), event.getDataInicio());
        assertEquals(LocalDate.of(2025, 12, 31), event.getDataFim());
    }

    /**
     * Teste 2: Verifica que o Builder valida campos obrigatórios.
     */
    @Test
    void deveValidarCamposObrigatoriosNoBuilder() {
        // Arrange & Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            ReportGenerationRequestedEvent.builder()
                    .tipoRelatorio("FATURAMENTO_DINAMICO")
                    // Falta source e usuario
                    .build();
        });
    }

    /**
     * Teste 3: Verifica que o endpoint retorna HTTP 202 (Accepted).
     */
    @Test
    @WithMockUser(username = "reportuser")
    void deveRetornarHttp202QuandoSolicitarRelatorio() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/relatorios/faturamento/dinamico/pdf")
                        .param("tipoVisao", "OFICIAL")
                        .param("dataInicio", "2025-01-01")
                        .param("dataFim", "2025-12-31"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Seu relatório está sendo processado. Você será notificado quando estiver pronto."))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.tipoRelatorio").value("FATURAMENTO_DINAMICO"))
                .andExpect(jsonPath("$.tipoVisao").value("OFICIAL"));
    }

    /**
     * Teste 4: Verifica que o endpoint de compras com nota retorna HTTP 202.
     */
    @Test
    @WithMockUser(username = "reportuser")
    void deveRetornarHttp202ParaRelatorioComprasComNota() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/relatorios/compras-com-nota/pdf")
                        .param("dataInicio", "2025-01-01")
                        .param("dataFim", "2025-12-31"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Seu relatório está sendo processado. Você será notificado quando estiver pronto."))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.tipoRelatorio").value("COMPRAS_COM_NOTA"));
    }

    /**
     * Teste 5: Verifica que o endpoint de lançamentos retorna HTTP 202.
     */
    @Test
    @WithMockUser(username = "reportuser")
    void deveRetornarHttp202ParaRelatorioLancamentos() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/relatorios/lancamentos/pdf")
                        .param("dataInicio", "2025-01-01")
                        .param("dataFim", "2025-12-31"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Seu relatório está sendo processado. Você será notificado quando estiver pronto."))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.tipoRelatorio").value("LANCAMENTOS"));
    }

    /**
     * Teste 6: Verifica que o evento pode ser publicado manualmente.
     */
    @Test
    void devePermitirPublicarEventoManualmente() {
        // Arrange
        ReportGenerationRequestedEvent event = ReportGenerationRequestedEvent.builder()
                .source(this)
                .tipoRelatorio("LANCAMENTOS")
                .dataInicio(LocalDate.of(2025, 1, 1))
                .dataFim(LocalDate.of(2025, 12, 31))
                .usuario(usuarioTeste)
                .build();

        // Act - Publica o evento
        assertDoesNotThrow(() -> {
            eventPublisher.publishEvent(event);
        });

        // Assert
        // O evento foi publicado com sucesso
        // O listener será chamado em background (verificar logs)
        System.out.println("✅ Evento publicado com sucesso!");
        System.out.println("✅ Verifique os logs para ver a mensagem do listener!");
    }

    /**
     * Teste 7: Verifica que todos os parâmetros são preservados no evento.
     */
    @Test
    void devePreservarTodosParametrosNoEvento() {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 1, 1);
        LocalDate dataFim = LocalDate.of(2025, 12, 31);

        // Act
        ReportGenerationRequestedEvent event = ReportGenerationRequestedEvent.builder()
                .source(this)
                .tipoRelatorio("FATURAMENTO_DINAMICO")
                .tipoVisao("OFICIAL")
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .contaId(1L)
                .contatoId(2L)
                .categoriaId(3L)
                .descricao("Teste")
                .usuario(usuarioTeste)
                .build();

        // Assert
        assertEquals("FATURAMENTO_DINAMICO", event.getTipoRelatorio());
        assertEquals("OFICIAL", event.getTipoVisao());
        assertEquals(dataInicio, event.getDataInicio());
        assertEquals(dataFim, event.getDataFim());
        assertEquals(1L, event.getContaId());
        assertEquals(2L, event.getContatoId());
        assertEquals(3L, event.getCategoriaId());
        assertEquals("Teste", event.getDescricao());
        assertEquals(usuarioTeste, event.getUsuario());
    }
}

