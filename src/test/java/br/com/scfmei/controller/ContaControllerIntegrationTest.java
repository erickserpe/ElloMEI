package br.com.scfmei.controller;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.ContaRepository;
import br.com.scfmei.repository.UsuarioRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Testes de integração para a classe ContaController.
 * 
 * Esta classe testa o fluxo end-to-end desde a requisição HTTP até a interação com o banco de dados,
 * validando dois cenários críticos:
 * 1. A criação bem-sucedida de uma nova Conta via requisição POST
 * 2. A aplicação das regras de segurança, impedindo que um usuário acesse a Conta de outro usuário
 * 
 * Os testes utilizam:
 * - @SpringBootTest: Carrega o contexto completo da aplicação
 * - @AutoConfigureMockMvc: Configura MockMvc para simulação de requisições HTTP
 * - @ActiveProfiles("test"): Ativa o perfil de teste com banco H2 em memória
 * - @WithMockUser: Simula usuários autenticados para testes de segurança
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Ativa o application-test.properties
class ContaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario userA;
    private Usuario userB;

    @BeforeEach
    void setUp() {
        // Limpa os repositórios antes de cada teste para garantir isolamento
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Cria dois utilizadores diferentes para testar a segurança
        userA = new Usuario();
        userA.setUsername("userA");
        userA.setPassword(passwordEncoder.encode("password"));
        userA.setRoles("USER");
        userA.setNomeCompleto("User A");
        userA.setCpf("11144477735"); // CPF válido para testes
        userA.setRazaoSocial("Empresa A LTDA");
        userA.setNomeFantasia("Empresa A");
        userA.setCnpj("11222333000181"); // CNPJ válido para testes
        userA = usuarioRepository.save(userA);

        userB = new Usuario();
        userB.setUsername("userB");
        userB.setPassword(passwordEncoder.encode("password"));
        userB.setRoles("USER");
        userB.setNomeCompleto("User B");
        userB.setCpf("22233344456"); // CPF válido para testes
        userB.setRazaoSocial("Empresa B LTDA");
        userB.setNomeFantasia("Empresa B");
        userB.setCnpj("22333444000195"); // CNPJ válido para testes
        userB = usuarioRepository.save(userB);
    }

    @Test
    @WithMockUser(username = "userA") // Simula que o 'userA' está logado
    void deveCriarNovaContaComSucesso() throws Exception {
        // Cenário (Arrange)
        String nomeConta = "Conta Teste";
        String tipo = "Banco";
        BigDecimal saldoInicial = new BigDecimal("500.00");

        // Ação (Act) & Verificação da Resposta HTTP (Assert)
        mockMvc.perform(post("/contas")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nomeConta", nomeConta)
                .param("tipo", tipo)
                .param("saldoInicial", saldoInicial.toString())
                .with(csrf())) // Adiciona o token CSRF necessário pelo Spring Security
                .andExpect(status().is3xxRedirection()) // Espera um redirecionamento (status 302)
                .andExpect(redirectedUrl("/contas")); // Verifica se redireciona para a lista de contas

        // Verificação do Estado da Base de Dados (Assert)
        List<Conta> contasDoUserA = contaRepository.findByUsuario(userA);
        assertEquals(1, contasDoUserA.size(), "Deve haver exatamente uma conta criada para o userA");
        
        Conta contaCriada = contasDoUserA.get(0);
        assertEquals(nomeConta, contaCriada.getNomeConta(), "Nome da conta deve ser igual ao enviado");
        assertEquals(tipo, contaCriada.getTipo(), "Tipo da conta deve ser igual ao enviado");
        // Compara usando compareTo para BigDecimals para evitar problemas de precisão
        assertEquals(0, saldoInicial.compareTo(contaCriada.getSaldoInicial()), 
                "Saldo inicial deve ser igual ao enviado");
        assertEquals(0, saldoInicial.compareTo(contaCriada.getSaldoAtual()), 
                "Saldo atual deve ser inicializado com o saldo inicial");
        assertEquals(userA.getId(), contaCriada.getUsuario().getId(), 
                "Conta deve estar associada ao usuário correto");
    }

    @Test
    @WithMockUser(username = "userB") // Simula que o 'userB' está logado
    void naoDevePermitirAcessoAContaDeOutroUsuario() throws Exception {
        // Cenário (Arrange): A conta pertence ao userA
        Conta contaDoUserA = new Conta();
        contaDoUserA.setNomeConta("Conta do User A");
        contaDoUserA.setTipo("Privada");
        contaDoUserA.setSaldoInicial(new BigDecimal("1000.00"));
        contaDoUserA.setSaldoAtual(new BigDecimal("1000.00"));
        contaDoUserA.setUsuario(userA);
        contaDoUserA = contaRepository.save(contaDoUserA);

        // Ação (Act) & Verificação da Resposta HTTP (Assert)
        // O userB tenta aceder à página de edição da conta do userA
        mockMvc.perform(get("/contas/editar/" + contaDoUserA.getId()))
                .andExpect(status().isForbidden()); // Espera um erro 403 Forbidden
    }

    @Test
    @WithMockUser(username = "userA") // Simula que o 'userA' está logado
    void devePermitirAcessoAPropriaContaParaEdicao() throws Exception {
        // Cenário (Arrange): A conta pertence ao userA
        Conta contaDoUserA = new Conta();
        contaDoUserA.setNomeConta("Conta do User A");
        contaDoUserA.setTipo("Corrente");
        contaDoUserA.setSaldoInicial(new BigDecimal("2000.00"));
        contaDoUserA.setSaldoAtual(new BigDecimal("2500.00"));
        contaDoUserA.setUsuario(userA);
        contaDoUserA = contaRepository.save(contaDoUserA);

        // Ação (Act) & Verificação da Resposta HTTP (Assert)
        // O userA acessa sua própria conta para edição
        mockMvc.perform(get("/contas/editar/" + contaDoUserA.getId()))
                .andExpect(status().isOk()); // Espera sucesso (status 200)
    }

    @Test
    @WithMockUser(username = "userA") // Simula que o 'userA' está logado
    void deveListarApenasContasDoUsuarioLogado() throws Exception {
        // Cenário (Arrange): Criar contas para ambos os usuários
        Conta contaUserA1 = new Conta();
        contaUserA1.setNomeConta("Conta A1");
        contaUserA1.setTipo("Poupança");
        contaUserA1.setSaldoInicial(new BigDecimal("1000.00"));
        contaUserA1.setSaldoAtual(new BigDecimal("1000.00"));
        contaUserA1.setUsuario(userA);
        contaRepository.save(contaUserA1);

        Conta contaUserA2 = new Conta();
        contaUserA2.setNomeConta("Conta A2");
        contaUserA2.setTipo("Corrente");
        contaUserA2.setSaldoInicial(new BigDecimal("2000.00"));
        contaUserA2.setSaldoAtual(new BigDecimal("2000.00"));
        contaUserA2.setUsuario(userA);
        contaRepository.save(contaUserA2);

        Conta contaUserB = new Conta();
        contaUserB.setNomeConta("Conta B");
        contaUserB.setTipo("Investimento");
        contaUserB.setSaldoInicial(new BigDecimal("5000.00"));
        contaUserB.setSaldoAtual(new BigDecimal("5000.00"));
        contaUserB.setUsuario(userB);
        contaRepository.save(contaUserB);

        // Ação (Act) & Verificação da Resposta HTTP (Assert)
        mockMvc.perform(get("/contas"))
                .andExpect(status().isOk()); // Espera sucesso (status 200)

        // Verificação do Estado da Base de Dados (Assert)
        List<Conta> contasDoUserA = contaRepository.findByUsuario(userA);
        List<Conta> contasDoUserB = contaRepository.findByUsuario(userB);
        
        assertEquals(2, contasDoUserA.size(), "UserA deve ter exatamente 2 contas");
        assertEquals(1, contasDoUserB.size(), "UserB deve ter exatamente 1 conta");
        
        // Verifica que as contas do userA são realmente dele
        contasDoUserA.forEach(conta -> 
            assertEquals(userA.getId(), conta.getUsuario().getId(), 
                "Todas as contas do userA devem pertencer a ele"));
    }

    @Test
    @WithMockUser(username = "userA") // Simula que o 'userA' está logado
    void deveValidarCamposObrigatoriosAoCriarConta() throws Exception {
        // Cenário (Arrange): Tentar criar conta sem nome (campo obrigatório)
        
        // Ação (Act) & Verificação da Resposta HTTP (Assert)
        mockMvc.perform(post("/contas")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nomeConta", "") // Nome vazio (inválido)
                .param("tipo", "Banco")
                .param("saldoInicial", "500.00")
                .with(csrf()))
                .andExpect(status().isOk()); // Retorna para o formulário com erros (status 200)

        // Verificação do Estado da Base de Dados (Assert)
        List<Conta> contasDoUserA = contaRepository.findByUsuario(userA);
        assertEquals(0, contasDoUserA.size(), "Não deve haver contas criadas quando há erros de validação");
    }
}
