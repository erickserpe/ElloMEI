package br.com.ellomei.interceptor;

import br.com.ellomei.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para aplicar rate limiting em endpoints específicos.
 * 
 * Protege contra:
 * - Brute force em login
 * - Spam em recuperação de senha
 * - Abuso de API
 * 
 * Retorna HTTP 429 (Too Many Requests) quando o limite é excedido.
 * 
 * @author SCF-MEI Team
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private RateLimitConfig rateLimitConfig;

    /**
     * Intercepta requisições antes de chegarem ao controller.
     * Aplica rate limiting baseado no endpoint.
     * 
     * @param request Requisição HTTP
     * @param response Resposta HTTP
     * @param handler Handler do Spring MVC
     * @return true se a requisição pode prosseguir, false se foi bloqueada
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIp(request);

        // Aplicar rate limiting apenas em endpoints específicos
        if (shouldApplyRateLimit(uri, method)) {
            Bucket bucket = resolveBucket(uri, clientIp, request);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                // Requisição permitida
                // Adicionar headers informativos
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                logger.debug("Rate limit OK para {} - IP: {} - Tokens restantes: {}", 
                    uri, clientIp, probe.getRemainingTokens());
                return true;
            } else {
                // Limite excedido - bloquear requisição
                long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
                response.setContentType("application/json");
                response.getWriter().write(String.format(
                    "{\"error\": \"Too Many Requests\", \"message\": \"Você excedeu o limite de requisições. Tente novamente em %d segundos.\", \"retryAfter\": %d}",
                    waitForRefill, waitForRefill
                ));

                logger.warn("Rate limit EXCEDIDO para {} - IP: {} - Retry after: {}s", 
                    uri, clientIp, waitForRefill);
                
                return false;
            }
        }

        // Endpoints sem rate limiting
        return true;
    }

    /**
     * Determina se o endpoint deve ter rate limiting aplicado.
     * 
     * @param uri URI da requisição
     * @param method Método HTTP
     * @return true se deve aplicar rate limiting
     */
    private boolean shouldApplyRateLimit(String uri, String method) {
        // Login (POST)
        if (uri.equals("/login") && method.equals("POST")) {
            return true;
        }

        // Recuperação de senha (POST)
        if (uri.equals("/recuperar-senha") && method.equals("POST")) {
            return true;
        }

        // Redefinir senha (POST)
        if (uri.startsWith("/redefinir-senha") && method.equals("POST")) {
            return true;
        }

        // Registro de usuário (POST)
        if (uri.equals("/register") && method.equals("POST")) {
            return true;
        }

        // APIs REST (se houver)
        if (uri.startsWith("/api/")) {
            return true;
        }

        return false;
    }

    /**
     * Resolve o bucket apropriado baseado no endpoint.
     * 
     * @param uri URI da requisição
     * @param clientIp IP do cliente
     * @param request Requisição HTTP
     * @return Bucket configurado
     */
    private Bucket resolveBucket(String uri, String clientIp, HttpServletRequest request) {
        // Login: limite por IP
        if (uri.equals("/login")) {
            return rateLimitConfig.resolveLoginBucket("login:" + clientIp);
        }

        // Recuperação de senha: limite por IP (mais restritivo)
        if (uri.equals("/recuperar-senha") || uri.startsWith("/redefinir-senha")) {
            return rateLimitConfig.resolvePasswordResetBucket("password-reset:" + clientIp);
        }

        // Registro: limite por IP
        if (uri.equals("/register")) {
            return rateLimitConfig.resolveLoginBucket("register:" + clientIp);
        }

        // API geral: limite por usuário (se autenticado) ou IP
        String username = request.getUserPrincipal() != null ? 
            request.getUserPrincipal().getName() : clientIp;
        return rateLimitConfig.resolveApiBucket("api:" + username);
    }

    /**
     * Extrai o IP real do cliente, considerando proxies e load balancers.
     * 
     * @param request Requisição HTTP
     * @return IP do cliente
     */
    private String getClientIp(HttpServletRequest request) {
        // Verificar headers de proxy
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For pode conter múltiplos IPs, pegar o primeiro
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback para IP direto
        return request.getRemoteAddr();
    }
}

