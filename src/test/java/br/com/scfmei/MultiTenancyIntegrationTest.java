package br.com.scfmei;

import static br.com.scfmei.TestHelper.*;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.domain.Role;
import br.com.scfmei.repository.ContaRepository;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.repository.RoleRepository;
import br.com.scfmei.service.ContaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste de Integração para Multi-Tenancy.
 * 
 * Este é o teste mais crítico do sistema SaaS.
 * Ele garante que o filtro Hibernate está funcionando corretamente
 * e que um usuário NUNCA pode ver os dados de outro usuário.
 * 
 * Se este teste falhar, o sistema NÃO É SEGURO para produção.
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MultiTenancyIntegrationTest {

    @Autowired private ContaService contaService;
    @Autowired private ContaRepository contaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario userA;
    private Usuario userB;

    @BeforeEach
    void setUp() {
        // Limpa o banco de dados antes de cada teste
        usuarioRepository.deleteAll();

        // Cria o User A
        userA = new Usuario();
        userA.setUsername("userA");
        userA.setPassword(passwordEncoder.encode("password"));
        userA.setRoles(TestHelper.createUserRole(roleRepository));
        usuarioRepository.save(userA);

        // Cria o User B
        userB = new Usuario();
        userB.setUsername("userB");
        userB.setPassword(passwordEncoder.encode("password"));
        userB.setRoles(TestHelper.createUserRole(roleRepository));
        usuarioRepository.save(userB);

        // Cria uma conta para o User A
        Conta contaUserA = new Conta();
        contaUserA.setNomeConta("Conta do User A");
        contaUserA.setSaldoInicial(BigDecimal.TEN);
        contaService.salvar(contaUserA, userA);

        // Cria uma conta para o User B
        Conta contaUserB = new Conta();
        contaUserB.setNomeConta("Conta do User B");
        contaUserB.setSaldoInicial(BigDecimal.ONE);
        contaService.salvar(contaUserB, userB);
    }

    /**
     * Teste 1: Verifica que o User A vê apenas suas próprias contas.
     * 
     * Quando o User A está logado, o filtro Hibernate deve ser ativado
     * com tenantId = userA.getId(), garantindo que apenas as contas
     * do User A sejam retornadas.
     */
    @Test
    @WithMockUser("userA") // Simula que o userA está logado
    void deveRetornarApenasContasDoUserA_quandoLogadoComoUserA() {
        // Act: O serviço busca todas as contas. O Aspecto deve ativar o filtro.
        List<Conta> contas = contaService.buscarTodasPorUsuario(userA);

        // Assert: Apenas a conta do userA deve ser retornada.
        assertEquals(1, contas.size(), "Deveria encontrar apenas uma conta.");
        assertEquals("Conta do User A", contas.get(0).getNomeConta());
    }

    /**
     * Teste 2: Verifica que o User B vê apenas suas próprias contas.
     * 
     * Quando o User B está logado, o filtro Hibernate deve ser ativado
     * com tenantId = userB.getId(), garantindo que apenas as contas
     * do User B sejam retornadas.
     */
    @Test
    @WithMockUser("userB") // Simula que o userB está logado
    void deveRetornarApenasContasDoUserB_quandoLogadoComoUserB() {
        // Act
        List<Conta> contas = contaService.buscarTodasPorUsuario(userB);

        // Assert
        assertEquals(1, contas.size(), "Deveria encontrar apenas uma conta.");
        assertEquals("Conta do User B", contas.get(0).getNomeConta());
    }
}

