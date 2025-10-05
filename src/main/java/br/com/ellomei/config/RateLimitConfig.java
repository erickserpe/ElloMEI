package br.com.ellomei.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Configuração de Rate Limiting usando Bucket4j.
 * 
 * Implementa o algoritmo Token Bucket para limitar requisições por IP/usuário.
 * 
 * Limites configurados:
 * - Login: 5 tentativas por minuto por IP
 * - API Geral: 100 requisições por minuto por usuário
 * - Recuperação de senha: 3 tentativas por hora por IP
 * 
 * @author SCF-MEI Team
 */
@Configuration
@EnableCaching
public class RateLimitConfig {

    /**
     * Cache para armazenar os buckets de rate limiting.
     * Usa Caffeine para performance e expiração automática.
     */
    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Configuração do CacheManager com Caffeine.
     * Expira entradas após 1 hora de inatividade.
     *
     * Registra todos os caches usados pela aplicação:
     * - rateLimitCache: Para rate limiting
     * - categoriasPorUsuario: Cache de categorias por usuário
     * - contatosPorUsuario: Cache de contatos por usuário
     * - contasPorUsuario: Cache de contas por usuário
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "rateLimitCache",
            "categoriasPorUsuario",
            "contatosPorUsuario",
            "contasPorUsuario"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(10000));
        return cacheManager;
    }

    /**
     * Cria ou recupera um bucket para login.
     * Limite: 5 tentativas por minuto.
     * 
     * @param key Chave única (geralmente IP do cliente)
     * @return Bucket configurado
     */
    public Bucket resolveLoginBucket(String key) {
        return cache.computeIfAbsent(key, k -> createLoginBucket());
    }

    /**
     * Cria ou recupera um bucket para API geral.
     * Limite: 100 requisições por minuto.
     * 
     * @param key Chave única (geralmente username ou IP)
     * @return Bucket configurado
     */
    public Bucket resolveApiBucket(String key) {
        return cache.computeIfAbsent(key, k -> createApiBucket());
    }

    /**
     * Cria ou recupera um bucket para recuperação de senha.
     * Limite: 3 tentativas por hora.
     * 
     * @param key Chave única (geralmente IP do cliente)
     * @return Bucket configurado
     */
    public Bucket resolvePasswordResetBucket(String key) {
        return cache.computeIfAbsent(key, k -> createPasswordResetBucket());
    }

    /**
     * Cria bucket para login.
     * Algoritmo Token Bucket:
     * - Capacidade: 5 tokens
     * - Refill: 5 tokens a cada 1 minuto
     * - Greedy refill: Todos os tokens são reabastecidos de uma vez
     */
    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Cria bucket para API geral.
     * Algoritmo Token Bucket:
     * - Capacidade: 100 tokens
     * - Refill: 100 tokens a cada 1 minuto
     * - Greedy refill: Todos os tokens são reabastecidos de uma vez
     */
    private Bucket createApiBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Cria bucket para recuperação de senha.
     * Algoritmo Token Bucket:
     * - Capacidade: 3 tokens
     * - Refill: 3 tokens a cada 1 hora
     * - Greedy refill: Todos os tokens são reabastecidos de uma vez
     */
    private Bucket createPasswordResetBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofHours(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Limpa o cache de rate limiting.
     * Útil para testes ou reset manual.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Remove uma entrada específica do cache.
     * 
     * @param key Chave a ser removida
     */
    public void removeFromCache(String key) {
        cache.remove(key);
    }
}

