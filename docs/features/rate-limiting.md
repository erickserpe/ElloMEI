# 🛡️ RATE LIMITING - ElloMEI

Este documento explica o sistema de rate limiting implementado para proteger a aplicação contra abuso e ataques.

---

## 📋 **ÍNDICE**

1. [O que é Rate Limiting?](#-o-que-é-rate-limiting)
2. [Por que implementar?](#-por-que-implementar)
3. [Limites Configurados](#-limites-configurados)
4. [Como Funciona](#-como-funciona)
5. [Endpoints Protegidos](#-endpoints-protegidos)
6. [Resposta ao Cliente](#-resposta-ao-cliente)
7. [Configuração](#-configuração)
8. [Testes](#-testes)

---

## 🤔 **O QUE É RATE LIMITING?**

Rate limiting é uma técnica para controlar a taxa de requisições que um cliente pode fazer a um servidor em um determinado período de tempo.

**Analogia:**
Imagine uma torneira com um limitador de fluxo. Mesmo que você abra totalmente, a água sai em uma taxa controlada. Rate limiting faz o mesmo com requisições HTTP.

**Algoritmo usado:** Token Bucket
- Cada cliente tem um "balde" com tokens
- Cada requisição consome 1 token
- Tokens são reabastecidos ao longo do tempo
- Se não há tokens, a requisição é bloqueada

---

## 🎯 **POR QUE IMPLEMENTAR?**

### **1. Proteção contra Brute Force** 🔒
**Problema:** Atacante tenta adivinhar senhas fazendo milhares de tentativas de login.

**Solução:** Limitar login a 5 tentativas por minuto por IP.

**Resultado:** Atacante levaria anos para testar senhas comuns.

---

### **2. Proteção contra Spam** 📧
**Problema:** Atacante envia milhares de emails de recuperação de senha.

**Solução:** Limitar recuperação de senha a 3 tentativas por hora por IP.

**Resultado:** Impossível usar o sistema para spam.

---

### **3. Proteção contra DDoS** 🌊
**Problema:** Atacante faz milhares de requisições para derrubar o servidor.

**Solução:** Limitar requisições gerais a 100 por minuto por usuário.

**Resultado:** Servidor permanece estável mesmo sob ataque.

---

### **4. Economia de Recursos** 💰
**Problema:** Bots ou scripts mal feitos fazem requisições excessivas.

**Solução:** Rate limiting força uso responsável da API.

**Resultado:** Menor custo de infraestrutura.

---

## 📊 **LIMITES CONFIGURADOS**

### **1. Login** 🔐

| Parâmetro | Valor |
|-----------|-------|
| **Endpoint** | `POST /login` |
| **Limite** | 5 requisições |
| **Período** | 1 minuto |
| **Identificação** | IP do cliente |
| **Objetivo** | Prevenir brute force |

**Exemplo:**
```
Tentativa 1: ✅ OK
Tentativa 2: ✅ OK
Tentativa 3: ✅ OK
Tentativa 4: ✅ OK
Tentativa 5: ✅ OK
Tentativa 6: ❌ 429 Too Many Requests (aguarde 60s)
```

---

### **2. Recuperação de Senha** 📧

| Parâmetro | Valor |
|-----------|-------|
| **Endpoint** | `POST /recuperar-senha` |
| **Limite** | 3 requisições |
| **Período** | 1 hora |
| **Identificação** | IP do cliente |
| **Objetivo** | Prevenir spam de emails |

**Exemplo:**
```
Tentativa 1: ✅ OK (email enviado)
Tentativa 2: ✅ OK (email enviado)
Tentativa 3: ✅ OK (email enviado)
Tentativa 4: ❌ 429 Too Many Requests (aguarde 3600s)
```

---

### **3. Redefinir Senha** 🔑

| Parâmetro | Valor |
|-----------|-------|
| **Endpoint** | `POST /redefinir-senha/*` |
| **Limite** | 3 requisições |
| **Período** | 1 hora |
| **Identificação** | IP do cliente |
| **Objetivo** | Prevenir abuso |

---

### **4. Registro de Usuário** 👤

| Parâmetro | Valor |
|-----------|-------|
| **Endpoint** | `POST /register` |
| **Limite** | 5 requisições |
| **Período** | 1 minuto |
| **Identificação** | IP do cliente |
| **Objetivo** | Prevenir criação massiva de contas |

---

### **5. API Geral** 🌐

| Parâmetro | Valor |
|-----------|-------|
| **Endpoint** | `* /api/*` |
| **Limite** | 100 requisições |
| **Período** | 1 minuto |
| **Identificação** | Username (se autenticado) ou IP |
| **Objetivo** | Prevenir abuso de API |

---

## ⚙️ **COMO FUNCIONA**

### **Fluxo de Requisição:**

```
1. Cliente faz requisição → POST /login
2. RateLimitInterceptor intercepta
3. Verifica se endpoint tem rate limiting
4. Identifica cliente (IP ou username)
5. Busca bucket do cliente no cache
6. Tenta consumir 1 token do bucket
   ├─ Se tem token: ✅ Permite requisição
   └─ Se não tem: ❌ Bloqueia com HTTP 429
```

### **Algoritmo Token Bucket:**

```
Bucket (Login):
┌─────────────────────────┐
│ Capacidade: 5 tokens    │
│ Refill: 5 tokens/minuto │
│                         │
│ [●][●][●][●][●]        │ ← 5 tokens disponíveis
└─────────────────────────┘

Após 3 requisições:
┌─────────────────────────┐
│ [○][○][○][●][●]        │ ← 2 tokens restantes
└─────────────────────────┘

Após 1 minuto:
┌─────────────────────────┐
│ [●][●][●][●][●]        │ ← 5 tokens (reabastecido)
└─────────────────────────┘
```

---

## 🎯 **ENDPOINTS PROTEGIDOS**

| Endpoint | Método | Limite | Período | Identificação |
|----------|--------|--------|---------|---------------|
| `/login` | POST | 5 | 1 min | IP |
| `/recuperar-senha` | POST | 3 | 1 hora | IP |
| `/redefinir-senha/*` | POST | 3 | 1 hora | IP |
| `/register` | POST | 5 | 1 min | IP |
| `/api/*` | ALL | 100 | 1 min | Username/IP |

**Endpoints SEM rate limiting:**
- GET /login (página de login)
- GET /dashboard
- GET /lancamentos
- Recursos estáticos (/css, /js, /images)
- Webjars (/webjars)

---

## 📨 **RESPOSTA AO CLIENTE**

### **Requisição Permitida (200 OK):**

```http
HTTP/1.1 200 OK
X-Rate-Limit-Remaining: 4
Content-Type: application/json

{
  "success": true,
  "message": "Login realizado com sucesso"
}
```

**Headers:**
- `X-Rate-Limit-Remaining`: Tokens restantes

---

### **Requisição Bloqueada (429 Too Many Requests):**

```http
HTTP/1.1 429 Too Many Requests
X-Rate-Limit-Retry-After-Seconds: 60
Content-Type: application/json

{
  "error": "Too Many Requests",
  "message": "Você excedeu o limite de requisições. Tente novamente em 60 segundos.",
  "retryAfter": 60
}
```

**Headers:**
- `X-Rate-Limit-Retry-After-Seconds`: Segundos até poder tentar novamente

---

## 🔧 **CONFIGURAÇÃO**

### **Ajustar Limites:**

Edite `src/main/java/br/com/ellomei/config/RateLimitConfig.java`:

```java
// Login: 10 tentativas por minuto
private Bucket createLoginBucket() {
    Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
}

// Recuperação de senha: 5 tentativas por hora
private Bucket createPasswordResetBucket() {
    Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1)));
    return Bucket.builder().addLimit(limit).build();
}

// API: 200 requisições por minuto
private Bucket createApiBucket() {
    Bandwidth limit = Bandwidth.classic(200, Refill.greedy(200, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
}
```

---

### **Adicionar Novos Endpoints:**

Edite `src/main/java/br/com/ellomei/interceptor/RateLimitInterceptor.java`:

```java
private boolean shouldApplyRateLimit(String uri, String method) {
    // Adicionar novo endpoint
    if (uri.equals("/meu-endpoint") && method.equals("POST")) {
        return true;
    }
    
    // ... resto do código
}
```

---

### **Desabilitar Rate Limiting (Dev):**

Em `application-dev.properties`:

```properties
# Desabilitar rate limiting em desenvolvimento
rate-limiting.enabled=false
```

Depois, adicione verificação no interceptor:

```java
@Value("${rate-limiting.enabled:true}")
private boolean rateLimitingEnabled;

@Override
public boolean preHandle(...) {
    if (!rateLimitingEnabled) {
        return true; // Bypass
    }
    // ... resto do código
}
```

---

## 🧪 **TESTES**

### **Teste Manual com cURL:**

```bash
# Teste de login (5 tentativas permitidas)
for i in {1..6}; do
  echo "Tentativa $i:"
  curl -X POST http://localhost:8080/login \
    -d "username=test&password=test" \
    -i | grep -E "HTTP|X-Rate-Limit"
  echo ""
done
```

**Resultado esperado:**
```
Tentativa 1: HTTP/1.1 200 OK, X-Rate-Limit-Remaining: 4
Tentativa 2: HTTP/1.1 200 OK, X-Rate-Limit-Remaining: 3
Tentativa 3: HTTP/1.1 200 OK, X-Rate-Limit-Remaining: 2
Tentativa 4: HTTP/1.1 200 OK, X-Rate-Limit-Remaining: 1
Tentativa 5: HTTP/1.1 200 OK, X-Rate-Limit-Remaining: 0
Tentativa 6: HTTP/1.1 429 Too Many Requests, X-Rate-Limit-Retry-After-Seconds: 60
```

---

### **Teste de Recuperação de Senha:**

```bash
# Teste de recuperação (3 tentativas permitidas)
for i in {1..4}; do
  echo "Tentativa $i:"
  curl -X POST http://localhost:8080/recuperar-senha \
    -d "email=test@example.com" \
    -i | grep -E "HTTP|X-Rate-Limit"
  echo ""
done
```

---

## 📈 **MONITORAMENTO**

### **Ver Logs de Rate Limiting:**

```bash
# Ver requisições bloqueadas
docker compose logs app | grep "Rate limit EXCEDIDO"

# Ver requisições permitidas (apenas em dev)
docker compose logs app | grep "Rate limit OK"
```

**Exemplo de log:**
```
2025-10-04 20:00:00.123 WARN  RateLimitInterceptor - Rate limit EXCEDIDO para /login - IP: 192.168.1.100 - Retry after: 60s
```

---

## ✅ **BENEFÍCIOS**

### **Segurança** 🔒
- ✅ Proteção contra brute force
- ✅ Proteção contra spam
- ✅ Proteção contra DDoS
- ✅ Redução de superfície de ataque

### **Performance** ⚡
- ✅ Servidor permanece estável
- ✅ Recursos não são desperdiçados
- ✅ Cache eficiente (Caffeine)
- ✅ Baixo overhead (<1ms por requisição)

### **Compliance** 📋
- ✅ OWASP Top 10 (A07:2021 - Identification and Authentication Failures)
- ✅ PCI DSS (Requirement 8.1.6 - Limit repeated access attempts)
- ✅ LGPD (Segurança da informação)

---

## 🎉 **RESUMO**

Com rate limiting implementado:

- ✅ **Login protegido** (5 tentativas/min)
- ✅ **Recuperação de senha protegida** (3 tentativas/hora)
- ✅ **API protegida** (100 req/min)
- ✅ **Registro protegido** (5 tentativas/min)
- ✅ **Cache eficiente** (Caffeine)
- ✅ **Logs estruturados** (SLF4J)
- ✅ **Headers informativos** (X-Rate-Limit-*)
- ✅ **Pronto para produção** ✨

---

**Dúvidas?** Consulte a documentação do Bucket4j:
- https://bucket4j.com/

