package br.com.ellomei.service;

import br.com.ellomei.domain.Conta;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

/**
 * Testes unitários para a classe ContaService.
 * 
 * Esta é a primeira classe de teste da aplicação ElloMEI, focando na validação
 * da lógica de negócio principal do ContaService, especificamente o método salvar().
 * 
 * Os testes verificam:
 * 1. Se uma nova conta tem seu saldoAtual inicializado corretamente com o saldoInicial
 * 2. Se uma conta existente mantém seu saldoAtual quando outros detalhes são atualizados
 */
@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    // @Mock cria um objeto falso (mock) do nosso repositório.
    // Não vamos usar o banco de dados real nos testes.
    @Mock
    private ContaRepository contaRepository;

    // @InjectMocks cria uma instância real do ContaService, mas injeta
    // os mocks que criámos acima (neste caso, o contaRepository).
    @InjectMocks
    private ContaService contaService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Cria um utilizador padrão para ser usado em todos os testes
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
    }

    @Test
    void deveInicializarSaldoAtualAoSalvarNovaConta() {
        // Cenário (Arrange)
        Conta novaConta = new Conta();
        novaConta.setNomeConta("Conta Corrente");
        novaConta.setTipo("Banco");
        novaConta.setSaldoInicial(new BigDecimal("1000.00"));
        // Note que não definimos ID - isso indica que é uma nova conta

        // Configuramos o mock para retornar a própria conta quando o método save for chamado
        when(contaRepository.save(any(Conta.class))).thenReturn(novaConta);

        // Ação (Act)
        Conta contaSalva = contaService.salvar(novaConta, usuario);

        // Verificação (Assert)
        // Verificamos se o saldoAtual é igual ao saldoInicial, que é a regra de negócio
        assertEquals(new BigDecimal("1000.00"), contaSalva.getSaldoAtual());
        // Verificamos se o usuário foi definido corretamente
        assertEquals(usuario, contaSalva.getUsuario());
        // Verificamos se a conta foi retornada
        assertNotNull(contaSalva);
    }

    @Test
    void naoDeveAlterarSaldoAtualAoEditarContaExistente() {
        // Cenário (Arrange)
        Conta contaExistente = new Conta();
        contaExistente.setId(1L); // A presença do ID indica que a conta já existe
        contaExistente.setNomeConta("Conta Antiga");
        contaExistente.setTipo("Banco");
        contaExistente.setSaldoInicial(new BigDecimal("500.00"));
        contaExistente.setSaldoAtual(new BigDecimal("1234.56")); // Saldo atual diferente do inicial

        // Ação: O utilizador muda o nome da conta
        contaExistente.setNomeConta("Conta com Nome Novo");

        // Configuramos o mock para retornar a conta existente
        when(contaRepository.save(any(Conta.class))).thenReturn(contaExistente);

        // Ação (Act)
        Conta contaSalva = contaService.salvar(contaExistente, usuario);

        // Verificação (Assert)
        // Verificamos se o saldoAtual NÃO foi resetado para o saldoInicial
        assertEquals(new BigDecimal("1234.56"), contaSalva.getSaldoAtual());
        // Verificamos se o nome foi atualizado
        assertEquals("Conta com Nome Novo", contaSalva.getNomeConta());
        // Verificamos se o usuário foi definido corretamente
        assertEquals(usuario, contaSalva.getUsuario());
        // Verificamos se a conta foi retornada
        assertNotNull(contaSalva);
    }

    @Test
    void deveDefinirUsuarioCorretamenteAoSalvarConta() {
        // Cenário (Arrange)
        Conta novaConta = new Conta();
        novaConta.setNomeConta("Conta Poupança");
        novaConta.setTipo("Banco");
        novaConta.setSaldoInicial(new BigDecimal("2500.00"));

        when(contaRepository.save(any(Conta.class))).thenReturn(novaConta);

        // Ação (Act)
        Conta contaSalva = contaService.salvar(novaConta, usuario);

        // Verificação (Assert)
        // Verificamos se o usuário foi corretamente associado à conta
        assertEquals(usuario, contaSalva.getUsuario());
        assertEquals("testuser", contaSalva.getUsuario().getUsername());
        assertEquals(Long.valueOf(1L), contaSalva.getUsuario().getId());
    }
}
