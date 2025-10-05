package br.com.ellomei.config;

import br.com.ellomei.domain.LancamentoFormDTO;
import br.com.ellomei.domain.PlanoAssinatura;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.exception.PlanLimitExceededException;
import br.com.ellomei.repository.LancamentoRepository;
import br.com.ellomei.repository.UsuarioRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Aspecto Spring AOP que controla os limites de uso baseados no plano de assinatura.
 * 
 * Este aspecto intercepta a criação de novos lançamentos e verifica se o usuário
 * do plano FREE não excedeu o limite mensal de 20 lançamentos.
 * 
 * Plano FREE: Máximo de 20 lançamentos (grupos de operação) por mês
 * Plano PRO: Sem limites
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Aspect
@Component
public class PlanLimitAspect {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Intercepta a criação de novos lançamentos e verifica os limites do plano.
     * 
     * Este método só é executado quando um NOVO lançamento está sendo criado
     * (quando o grupoOperacao é nulo ou vazio). Edições de lançamentos existentes
     * não são contabilizadas para o limite.
     * 
     * @param form O formulário de lançamento sendo salvo
     * @throws PlanLimitExceededException Se o usuário FREE excedeu o limite de 20 lançamentos mensais
     */
    @Before("execution(* br.com.ellomei.service.LancamentoService.salvarOuAtualizarOperacao(..)) && args(form, ..)")
    public void checkLancamentoLimit(LancamentoFormDTO form) {
        // Se tem grupoOperacao preenchido, é uma edição, não conta para o limite
        if (form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank()) {
            return;
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

            // Apenas usuários do plano FREE têm limite
            if (usuario.getPlano() == PlanoAssinatura.FREE) {
                YearMonth mesAtual = YearMonth.now();
                LocalDate inicioDoMes = mesAtual.atDay(1);
                LocalDate fimDoMes = mesAtual.atEndOfMonth();

                // Conta quantos grupos de operação (lançamentos únicos) o usuário tem no mês atual
                long count = lancamentoRepository.findComFiltros(
                    inicioDoMes, 
                    fimDoMes, 
                    null,  // contaId
                    null,  // contatoId
                    null,  // tipo
                    null,  // categoriaId
                    null,  // comNotaFiscal
                    null,  // descricao
                    null,  // status
                    usuario
                ).stream()
                    .map(l -> l.getGrupoOperacao() != null ? l.getGrupoOperacao() : l.getId().toString())
                    .distinct()
                    .count();

                // Limite de 20 lançamentos mensais para o plano FREE
                if (count >= 20) {
                    throw new PlanLimitExceededException(
                        "Limite de 20 lançamentos mensais para o plano gratuito atingido. " +
                        "Faça upgrade para o plano PRO para lançamentos ilimitados."
                    );
                }
            }
        }
    }
}

