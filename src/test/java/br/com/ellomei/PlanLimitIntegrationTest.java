package br.com.ellomei;

import static br.com.scfmei.TestHelper.*;

import br.com.ellomei.domain.*;
import br.com.ellomei.exception.PlanLimitExceededException;
import br.com.ellomei.repository.ContaRepository;
import br.com.ellomei.repository.LancamentoRepository;
import br.com.ellomei.repository.UsuarioRepository;
import br.com.ellomei.repository.RoleRepository;
import br.com.ellomei.service.LancamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

/**
 * Teste de Integração para Limites de Plano.
 * 
 * Este teste verifica que o aspecto PlanLimitAspect está funcionando corretamente
 * e que usuários do plano FREE não podem criar mais de 20 lançamentos por mês.
 * 
 * Se este teste falhar, significa que os limites de plano não estão sendo aplicados.
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PlanLimitIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LancamentoService lancamentoService;
    @Autowired private LancamentoRepository lancamentoRepository;
    @Autowired private ContaRepository contaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario userFree;
    private Usuario userPro;
    private Conta conta;

    @BeforeEach
    void setUp() {
        // Limpa o banco de dados
        lancamentoRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Cria usuário FREE
        userFree = new Usuario();
        userFree.setUsername("userFree");
        userFree.setPassword(passwordEncoder.encode("password"));
        userFree.setRoles(TestHelper.createUserRole(roleRepository));
        userFree.setPlano(PlanoAssinatura.FREE);
        usuarioRepository.save(userFree);

        // Cria usuário PRO
        userPro = new Usuario();
        userPro.setUsername("userPro");
        userPro.setPassword(passwordEncoder.encode("password"));
        userPro.setRoles(TestHelper.createUserRole(roleRepository));
        userPro.setPlano(PlanoAssinatura.PRO);
        usuarioRepository.save(userPro);

        // Cria uma conta para os testes
        conta = new Conta();
        conta.setNomeConta("Conta Teste");
        conta.setSaldoInicial(BigDecimal.valueOf(1000));
        conta.setSaldoAtual(BigDecimal.valueOf(1000));
        conta.setUsuario(userFree);
        contaRepository.save(conta);
    }

    /**
     * Teste 1: Verifica que usuário FREE pode criar até 20 lançamentos.
     * 
     * Este teste cria 20 lançamentos e verifica que todos foram criados com sucesso.
     */
    @Test
    @WithMockUser("userFree")
    void usuarioFree_deveCriarAte20Lancamentos_semErro() {
        // Arrange & Act: Cria 20 lançamentos
        for (int i = 1; i <= 20; i++) {
            LancamentoFormDTO form = criarLancamentoForm("Lançamento " + i);
            
            // Não deve lançar exceção
            assertDoesNotThrow(() -> {
                lancamentoService.salvarOuAtualizarOperacao(form, null, userFree);
            }, "Deveria permitir criar até 20 lançamentos no plano FREE");
        }

        // Assert: Verifica que 20 lançamentos foram criados
        long count = lancamentoRepository.findComFiltros(
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
            null, null, null, null, null, null, null, userFree
        ).stream()
            .map(l -> l.getGrupoOperacao() != null ? l.getGrupoOperacao() : l.getId().toString())
            .distinct()
            .count();

        assertEquals(20, count, "Deveria ter exatamente 20 lançamentos");
    }

    /**
     * Teste 2: Verifica que usuário FREE NÃO pode criar o 21º lançamento.
     * 
     * Este é o teste mais importante! Ele verifica que o limite está sendo aplicado.
     */
    @Test
    @WithMockUser("userFree")
    void usuarioFree_naoDeveCriar21Lancamento_deveLancarExcecao() {
        // Arrange: Cria 20 lançamentos (atingindo o limite)
        for (int i = 1; i <= 20; i++) {
            LancamentoFormDTO form = criarLancamentoForm("Lançamento " + i);
            lancamentoService.salvarOuAtualizarOperacao(form, null, userFree);
        }

        // Act & Assert: Tenta criar o 21º lançamento
        LancamentoFormDTO form21 = criarLancamentoForm("Lançamento 21");
        
        PlanLimitExceededException exception = assertThrows(
            PlanLimitExceededException.class,
            () -> lancamentoService.salvarOuAtualizarOperacao(form21, null, userFree),
            "Deveria lançar PlanLimitExceededException ao tentar criar o 21º lançamento"
        );

        // Verifica a mensagem da exceção
        assertTrue(
            exception.getMessage().contains("Limite de 20 lançamentos mensais"),
            "A mensagem da exceção deveria mencionar o limite de 20 lançamentos"
        );
        assertTrue(
            exception.getMessage().contains("plano PRO"),
            "A mensagem da exceção deveria sugerir upgrade para o plano PRO"
        );
    }

    /**
     * Teste 3: Verifica que usuário PRO pode criar mais de 20 lançamentos.
     * 
     * Usuários PRO não têm limite de lançamentos.
     */
    @Test
    @WithMockUser("userPro")
    void usuarioPro_deveCriarMaisDe20Lancamentos_semErro() {
        // Arrange: Cria uma conta para o usuário PRO
        Conta contaPro = new Conta();
        contaPro.setNomeConta("Conta PRO");
        contaPro.setSaldoInicial(BigDecimal.valueOf(10000));
        contaPro.setSaldoAtual(BigDecimal.valueOf(10000));
        contaPro.setUsuario(userPro);
        contaRepository.save(contaPro);

        // Act: Cria 25 lançamentos (mais que o limite FREE)
        for (int i = 1; i <= 25; i++) {
            LancamentoFormDTO form = criarLancamentoFormParaUsuario("Lançamento PRO " + i, contaPro);
            
            // Não deve lançar exceção
            assertDoesNotThrow(() -> {
                lancamentoService.salvarOuAtualizarOperacao(form, null, userPro);
            }, "Usuário PRO deveria poder criar lançamentos ilimitados");
        }

        // Assert: Verifica que 25 lançamentos foram criados
        long count = lancamentoRepository.findComFiltros(
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
            null, null, null, null, null, null, null, userPro
        ).stream()
            .map(l -> l.getGrupoOperacao() != null ? l.getGrupoOperacao() : l.getId().toString())
            .distinct()
            .count();

        assertEquals(25, count, "Usuário PRO deveria ter 25 lançamentos");
    }

    /**
     * Teste 4: Verifica que edições de lançamentos NÃO contam para o limite.
     * 
     * Usuários FREE devem poder editar lançamentos existentes sem problemas.
     */
    @Test
    @WithMockUser("userFree")
    void usuarioFree_deveEditarLancamentos_semContarParaLimite() {
        // Arrange: Cria 20 lançamentos
        List<String> gruposOperacao = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            LancamentoFormDTO form = criarLancamentoForm("Lançamento " + i);
            lancamentoService.salvarOuAtualizarOperacao(form, null, userFree);
            
            // Pega o grupoOperacao do primeiro lançamento criado
            if (i == 1) {
                List<Lancamento> lancamentos = lancamentoRepository.findComFiltros(
                    LocalDate.now(), LocalDate.now(), null, null, null, null, null, "Lançamento 1", null, userFree
                );
                if (!lancamentos.isEmpty()) {
                    String grupo = lancamentos.get(0).getGrupoOperacao();
                    if (grupo != null) {
                        gruposOperacao.add(grupo);
                    }
                }
            }
        }

        // Act: Edita o primeiro lançamento (não deve contar para o limite)
        if (!gruposOperacao.isEmpty()) {
            LancamentoFormDTO formEdicao = criarLancamentoForm("Lançamento 1 Editado");
            formEdicao.setGrupoOperacao(gruposOperacao.get(0)); // Marca como edição
            
            // Não deve lançar exceção
            assertDoesNotThrow(() -> {
                lancamentoService.salvarOuAtualizarOperacao(formEdicao, null, userFree);
            }, "Edições não deveriam contar para o limite");
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Cria um formulário de lançamento para testes.
     */
    private LancamentoFormDTO criarLancamentoForm(String descricao) {
        return criarLancamentoFormParaUsuario(descricao, conta);
    }

    /**
     * Cria um formulário de lançamento para um usuário específico.
     */
    private LancamentoFormDTO criarLancamentoFormParaUsuario(String descricao, Conta contaParam) {
        LancamentoFormDTO form = new LancamentoFormDTO();
        form.setDescricao(descricao);
        form.setData(LocalDate.now());
        form.setTipo(TipoLancamento.ENTRADA);
        form.setStatus(StatusLancamento.PAGO);
        form.setComNotaFiscal(false);

        // Adiciona um pagamento
        PagamentoDTO pagamento = new PagamentoDTO();
        pagamento.setConta(contaParam.getId());
        pagamento.setValor(BigDecimal.TEN);

        List<PagamentoDTO> pagamentos = new ArrayList<>();
        pagamentos.add(pagamento);
        form.setPagamentos(pagamentos);

        return form;
    }

    /**
     * TESTE 5: Verifica que exceção é lançada quando limite é excedido via controller.
     *
     * Este teste simula uma chamada HTTP real usando MockMvc e verifica:
     * 1. Exceção PlanLimitExceededException é lançada
     * 2. GlobalExceptionHandler captura a exceção
     * 3. Resposta HTTP apropriada é retornada
     *
     * Nota: Como o controller retorna redirect (não JSON), vamos verificar
     * que a exceção é lançada e não capturada silenciosamente.
     */
    @Test
    @WithMockUser("userFreeApi")
    void usuarioFree_deveLancarExcecao_quandoTentaCriar21LancamentoViaController() throws Exception {
        // Arrange: Cria usuário FREE
        Usuario userFreeApi = new Usuario();
        userFreeApi.setUsername("userFreeApi");
        userFreeApi.setPassword(passwordEncoder.encode("senha123"));
        userFreeApi.setPlano(PlanoAssinatura.FREE);
        userFreeApi = usuarioRepository.save(userFreeApi);

        // Cria conta para o usuário
        Conta contaApi = new Conta();
        contaApi.setNomeConta("Conta API");
        contaApi.setSaldoAtual(BigDecimal.ZERO);
        contaApi.setUsuario(userFreeApi);
        contaApi = contaRepository.save(contaApi);

        // Cria 20 lançamentos (atingindo o limite)
        for (int i = 1; i <= 20; i++) {
            LancamentoFormDTO form = criarLancamentoFormParaUsuario("Lançamento API " + i, contaApi);
            lancamentoService.salvarOuAtualizarOperacao(form, null, userFreeApi);
        }

        // Act & Assert: Tenta criar o 21º lançamento via controller
        // Deve lançar PlanLimitExceededException
        mockMvc.perform(post("/lancamentos")
                .with(user("userFreeApi"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("descricao", "Lançamento 21 - Deve Falhar")
                .param("data", LocalDate.now().toString())
                .param("tipo", "ENTRADA")
                .param("status", "PAGO")
                .param("comNotaFiscal", "false")
                .param("pagamentos[0].conta", contaApi.getId().toString())
                .param("pagamentos[0].valor", "10.0"))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception, "Deveria ter lançado uma exceção");
                    assertTrue(exception instanceof PlanLimitExceededException,
                        "Deveria ser PlanLimitExceededException, mas foi: " + exception.getClass().getName());
                    assertTrue(exception.getMessage().contains("Limite de 20 lançamentos mensais"),
                        "Mensagem deveria conter 'Limite de 20 lançamentos mensais'");
                    assertTrue(exception.getMessage().contains("plano gratuito"),
                        "Mensagem deveria conter 'plano gratuito'");
                    assertTrue(exception.getMessage().contains("upgrade para o plano PRO"),
                        "Mensagem deveria conter 'upgrade para o plano PRO'");
                });
    }
}

