package br.com.scfmei.service;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.TipoLancamento;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.ComprovanteRepository;
import br.com.scfmei.repository.LancamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe LancamentoService.
 * 
 * Esta classe testa a lógica financeira crítica do LancamentoService, focando nos métodos
 * responsáveis por alterar o saldo das contas: aplicarLancamentoNaConta e reverterLancamentoNaConta.
 * 
 * Os testes verificam:
 * 1. Se uma despesa (SAIDA) subtrai corretamente do saldo da conta
 * 2. Se uma receita (ENTRADA) adiciona corretamente ao saldo da conta
 * 3. Se reverter uma despesa restaura corretamente o saldo da conta
 * 4. Se reverter uma receita subtrai corretamente do saldo da conta
 */
@ExtendWith(MockitoExtension.class)
class LancamentoServiceTest {

    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private ComprovanteRepository comprovanteRepository;

    @Mock
    private FileStorageService fileStorageService;

    // Usamos @Spy no ContaService porque precisamos chamar um método real dele (`salvar`)
    // dentro de outro método do LancamentoService que estamos a testar.
    @Spy
    private ContaService contaService;

    // @InjectMocks injeta os mocks e spies acima no nosso serviço principal.
    @InjectMocks
    private LancamentoService lancamentoService;

    private Usuario usuario;
    private Conta conta;

    @BeforeEach
    void setUp() {
        // Configuração que será executada antes de cada teste
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");

        conta = new Conta();
        conta.setId(1L);
        conta.setUsuario(usuario);
        conta.setNomeConta("Conta Teste");
        conta.setTipo("Banco");
        conta.setSaldoInicial(new BigDecimal("1000.00"));
        conta.setSaldoAtual(new BigDecimal("1000.00"));

        // Como contaService é um @Spy, precisamos configurar seu comportamento.
        // Configuramos o spy para retornar a própria conta quando salvar() for chamado,
        // permitindo que testemos apenas a lógica de alteração de saldo.
        doReturn(conta).when(contaService).salvar(any(Conta.class), any(Usuario.class));
    }

    /**
     * Método auxiliar para criar um lançamento de teste
     */
    private Lancamento criarLancamento(TipoLancamento tipo, BigDecimal valor) {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(1L);
        lancamento.setTipo(tipo);
        lancamento.setValor(valor);
        lancamento.setConta(conta);
        lancamento.setUsuario(usuario);
        lancamento.setData(LocalDate.now());
        lancamento.setDescricao("Lançamento de teste");
        return lancamento;
    }

    @Test
    void deveDebitarSaldoAoAplicarLancamentoDeSaida() {
        // Cenário (Arrange)
        Lancamento lancamentoSaida = criarLancamento(TipoLancamento.SAIDA, new BigDecimal("200.00"));

        // Ação (Act)
        // Usamos ReflectionTestUtils para invocar o método privado para o teste
        ReflectionTestUtils.invokeMethod(lancamentoService, "aplicarLancamentoNaConta", lancamentoSaida);

        // Verificação (Assert)
        // O saldo inicial era 1000. Após uma saída de 200, deve ser 800.
        assertEquals(new BigDecimal("800.00"), conta.getSaldoAtual());
        // Verifica se o método `salvar` do contaService foi chamado uma vez.
        verify(contaService, times(1)).salvar(conta, usuario);
    }

    @Test
    void deveCreditarSaldoAoAplicarLancamentoDeEntrada() {
        // Cenário (Arrange)
        Lancamento lancamentoEntrada = criarLancamento(TipoLancamento.ENTRADA, new BigDecimal("300.00"));

        // Ação (Act)
        ReflectionTestUtils.invokeMethod(lancamentoService, "aplicarLancamentoNaConta", lancamentoEntrada);

        // Verificação (Assert)
        // O saldo inicial era 1000. Após uma entrada de 300, deve ser 1300.
        assertEquals(new BigDecimal("1300.00"), conta.getSaldoAtual());
        verify(contaService, times(1)).salvar(conta, usuario);
    }

    @Test
    void deveCreditarSaldoAoReverterLancamentoDeSaida() {
        // Cenário (Arrange)
        Lancamento lancamentoSaida = criarLancamento(TipoLancamento.SAIDA, new BigDecimal("150.00"));

        // Ação (Act)
        ReflectionTestUtils.invokeMethod(lancamentoService, "reverterLancamentoNaConta", lancamentoSaida);

        // Verificação (Assert)
        // O saldo inicial era 1000. Ao reverter uma saída de 150, o valor deve ser somado de volta.
        assertEquals(new BigDecimal("1150.00"), conta.getSaldoAtual());
        verify(contaService, times(1)).salvar(conta, usuario);
    }

    @Test
    void deveDebitarSaldoAoReverterLancamentoDeEntrada() {
        // Cenário (Arrange)
        Lancamento lancamentoEntrada = criarLancamento(TipoLancamento.ENTRADA, new BigDecimal("400.00"));

        // Ação (Act)
        ReflectionTestUtils.invokeMethod(lancamentoService, "reverterLancamentoNaConta", lancamentoEntrada);

        // Verificação (Assert)
        // O saldo inicial era 1000. Ao reverter uma entrada de 400, o valor deve ser subtraído.
        assertEquals(new BigDecimal("600.00"), conta.getSaldoAtual());
        verify(contaService, times(1)).salvar(conta, usuario);
    }

    @Test
    void deveManterPrecisaoDecimalEmOperacoesFinanceiras() {
        // Cenário (Arrange) - Testando com valores decimais precisos
        Lancamento lancamentoSaida = criarLancamento(TipoLancamento.SAIDA, new BigDecimal("123.45"));
        Lancamento lancamentoEntrada = criarLancamento(TipoLancamento.ENTRADA, new BigDecimal("67.89"));

        // Ação (Act) - Aplicar saída primeiro
        ReflectionTestUtils.invokeMethod(lancamentoService, "aplicarLancamentoNaConta", lancamentoSaida);
        
        // Verificação intermediária
        assertEquals(new BigDecimal("876.55"), conta.getSaldoAtual());

        // Ação (Act) - Aplicar entrada depois
        ReflectionTestUtils.invokeMethod(lancamentoService, "aplicarLancamentoNaConta", lancamentoEntrada);

        // Verificação (Assert)
        // 1000.00 - 123.45 + 67.89 = 944.44
        assertEquals(new BigDecimal("944.44"), conta.getSaldoAtual());
        verify(contaService, times(2)).salvar(conta, usuario);
    }

    @Test
    void deveManterConsistenciaAoAplicarEReverterLancamento() {
        // Cenário (Arrange)
        BigDecimal saldoInicial = conta.getSaldoAtual();
        Lancamento lancamento = criarLancamento(TipoLancamento.SAIDA, new BigDecimal("250.00"));

        // Ação (Act) - Aplicar lançamento
        ReflectionTestUtils.invokeMethod(lancamentoService, "aplicarLancamentoNaConta", lancamento);
        
        // Verificação intermediária
        assertEquals(new BigDecimal("750.00"), conta.getSaldoAtual());

        // Ação (Act) - Reverter lançamento
        ReflectionTestUtils.invokeMethod(lancamentoService, "reverterLancamentoNaConta", lancamento);

        // Verificação (Assert)
        // Após aplicar e reverter, o saldo deve voltar ao valor inicial
        assertEquals(saldoInicial, conta.getSaldoAtual());
        verify(contaService, times(2)).salvar(conta, usuario);
    }
}
