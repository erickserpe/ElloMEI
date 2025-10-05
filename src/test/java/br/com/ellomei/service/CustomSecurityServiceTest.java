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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários leves para a classe CustomSecurityService.
 *
 * Esta classe testa a lógica crítica de segurança que controla o acesso aos recursos
 * da aplicação, garantindo que usuários só possam acessar seus próprios dados.
 *
 * Foca apenas nos casos essenciais para manter os testes leves.
 */
@ExtendWith(MockitoExtension.class)
class CustomSecurityServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CustomSecurityService customSecurityService;

    @BeforeEach
    void setUp() {
        // Configuração do contexto de segurança
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void devePermitirAcessoAPropriaContaDoUsuario() {
        // Cenário (Arrange)
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario_logado");

        Conta conta = new Conta();
        conta.setId(100L);
        conta.setUsuario(usuario);

        when(authentication.getName()).thenReturn("usuario_logado");
        when(contaRepository.findById(100L)).thenReturn(Optional.of(conta));

        // Ação (Act)
        boolean podeAcessar = customSecurityService.isContaOwner(100L);

        // Verificação (Assert)
        assertTrue(podeAcessar, "O usuário deveria poder acessar sua própria conta");
    }

    @Test
    void deveNegarAcessoAContaDeOutroUsuario() {
        // Cenário (Arrange)
        Usuario outroUsuario = new Usuario();
        outroUsuario.setUsername("outro_usuario");

        Conta conta = new Conta();
        conta.setId(200L);
        conta.setUsuario(outroUsuario);

        when(authentication.getName()).thenReturn("usuario_logado");
        when(contaRepository.findById(200L)).thenReturn(Optional.of(conta));

        // Ação (Act)
        boolean podeAcessar = customSecurityService.isContaOwner(200L);

        // Verificação (Assert)
        assertFalse(podeAcessar, "O usuário NÃO deveria poder acessar conta de outro usuário");
    }
}
