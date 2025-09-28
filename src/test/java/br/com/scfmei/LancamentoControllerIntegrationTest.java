package br.com.scfmei;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.StatusLancamento;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.ContaRepository;
import br.com.scfmei.repository.LancamentoRepository;
import br.com.scfmei.repository.UsuarioRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Garante que cada teste é executado numa transação que sofre rollback no final
class LancamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario testUser;
    private Conta testConta;

    @BeforeEach
    void setUp() {
        // Limpa os dados para garantir isolamento
        lancamentoRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Cria um utilizador e uma conta para os testes
        testUser = new Usuario();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRoles("USER");
        testUser.setNomeCompleto("Test User");
        testUser.setCpf("11144477735"); // CPF válido para testes
        testUser.setRazaoSocial("Test Company LTDA");
        testUser.setNomeFantasia("Test Company");
        testUser.setCnpj("11222333000181"); // CNPJ válido para testes
        usuarioRepository.save(testUser);

        testConta = new Conta();
        testConta.setNomeConta("Conta Principal");
        testConta.setTipo("Corrente");
        testConta.setSaldoInicial(new BigDecimal("1000.00"));
        testConta.setSaldoAtual(new BigDecimal("1000.00"));
        testConta.setUsuario(testUser);
        contaRepository.save(testConta);
    }

    @Test
    @WithMockUser(username = "testuser") // Simula que 'testuser' está logado
    void deveCriarLancamentoDeSaidaEAtualizarSaldoDaConta() throws Exception {
        // Cenário (Arrange)
        String descricaoLancamento = "Compra de material de escritório";
        BigDecimal valorLancamento = new BigDecimal("150.55");

        // Ação (Act) - Simula o envio do formulário
        mockMvc.perform(post("/lancamentos")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("descricao", descricaoLancamento)
                .param("data", LocalDate.now().toString())
                .param("tipo", TipoLancamento.SAIDA.toString())
                .param("status", StatusLancamento.PAGO.toString())
                .param("pagamentos[0].conta", testConta.getId().toString())
                .param("pagamentos[0].valor", valorLancamento.toString())
                .with(csrf())) // Adiciona o token CSRF
                .andExpect(status().is3xxRedirection()) // Verifica se a resposta foi um redirect
                .andExpect(redirectedUrl("/lancamentos")); // Verifica se redirecionou para a página correta

        // Verificação do Estado da Base de Dados (Assert)
        Optional<Conta> contaAtualizadaOptional = contaRepository.findById(testConta.getId());
        assertTrue(contaAtualizadaOptional.isPresent(), "A conta deveria existir no banco de dados.");

        Conta contaAtualizada = contaAtualizadaOptional.get();
        BigDecimal saldoEsperado = new BigDecimal("849.45"); // 1000.00 - 150.55

        // Compara os BigDecimals com compareTo para verificar a exatidão
        assertEquals(0, saldoEsperado.compareTo(contaAtualizada.getSaldoAtual()),
                "O saldo da conta não foi atualizado corretamente após o lançamento de saída.");

        assertEquals(1, lancamentoRepository.count(), "Um lançamento deveria ter sido criado.");
    }
}
