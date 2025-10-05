# 🔧 SPRING PROFILES - SCF-MEI

Este documento explica como usar os diferentes perfis (profiles) do Spring Boot na aplicação SCF-MEI.

---

## 📋 **PERFIS DISPONÍVEIS**

### **1. `dev` - Desenvolvimento** 🛠️

**Quando usar:**
- Desenvolvimento local
- Testes manuais
- Debug de problemas

**Características:**
- ✅ Logs detalhados (DEBUG)
- ✅ SQL visível no console
- ✅ Hibernate gerencia schema (ddl-auto=update)
- ✅ Flyway desabilitado
- ✅ DevTools habilitado (restart automático)
- ✅ Actuator expõe todos os endpoints
- ✅ Cache de templates desabilitado
- ❌ Compressão desabilitada

**Arquivo:** `src/main/resources/application-dev.properties`

---

### **2. `prod` - Produção** 🚀

**Quando usar:**
- Ambiente de produção
- Staging/homologação

**Características:**
- ✅ Logs otimizados (WARN/INFO)
- ✅ SQL oculto (segurança)
- ✅ Hibernate apenas valida schema (ddl-auto=validate)
- ✅ Flyway habilitado (migrações controladas)
- ✅ DevTools desabilitado
- ✅ Actuator expõe apenas /health
- ✅ Cache de templates habilitado
- ✅ Compressão GZIP habilitada
- ✅ Pool de conexões otimizado
- ✅ Logs salvos em arquivo

**Arquivo:** `src/main/resources/application-prod.properties`

---

## 🚀 **COMO USAR**

### **Opção 1: Variável de Ambiente (Recomendado)**

**Desenvolvimento:**
```bash
export SPRING_PROFILES_ACTIVE=dev
./docker-start.sh start
```

**Produção:**
```bash
export SPRING_PROFILES_ACTIVE=prod
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

### **Opção 2: Arquivo `.env`**

Edite o arquivo `.env`:

```bash
# Desenvolvimento
SPRING_PROFILES_ACTIVE=dev

# Produção
SPRING_PROFILES_ACTIVE=prod
```

Depois reinicie:
```bash
./docker-start.sh restart
```

---

### **Opção 3: Docker Compose**

**Desenvolvimento (padrão):**
```bash
docker compose up -d
```

**Produção:**
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

### **Opção 4: Linha de Comando (sem Docker)**

```bash
# Desenvolvimento
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Produção
java -jar -Dspring.profiles.active=prod target/SCF-MEI-0.0.1-SNAPSHOT.jar
```

---

## 📊 **COMPARAÇÃO DE PERFIS**

| Configuração | `dev` | `prod` |
|--------------|-------|--------|
| **Logs** | DEBUG | WARN/INFO |
| **SQL no console** | ✅ Sim | ❌ Não |
| **ddl-auto** | update | validate |
| **Flyway** | ❌ Desabilitado | ✅ Habilitado |
| **DevTools** | ✅ Habilitado | ❌ Desabilitado |
| **Actuator** | Todos endpoints | Apenas /health |
| **Compressão** | ❌ Não | ✅ Sim (GZIP) |
| **Cache templates** | ❌ Não | ✅ Sim |
| **Pool conexões** | 5 max | 20 max |
| **Arquivo de log** | ❌ Não | ✅ Sim |
| **Usuário Docker** | root | spring (não-root) |

---

## 🔍 **VERIFICAR PERFIL ATIVO**

### **Método 1: Logs da aplicação**

Ao iniciar, a aplicação mostra:
```
The following 1 profile is active: "dev"
```

### **Método 2: Endpoint do Actuator**

**Desenvolvimento:**
```bash
curl http://localhost:8080/actuator/env | grep "activeProfiles"
```

**Produção:**
```bash
# Actuator está desabilitado em produção (segurança)
# Verifique os logs:
docker compose logs app | grep "profile"
```

### **Método 3: Logs do Docker**

```bash
docker compose logs app | grep "profile"
```

---

## ⚙️ **CONFIGURAÇÕES POR PERFIL**

### **Desenvolvimento (`dev`)**

<augment_code_snippet path="src/main/resources/application-dev.properties" mode="EXCERPT">
````properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.flyway.enabled=false
logging.level.br.com.scfmei=DEBUG
````
</augment_code_snippet>

### **Produção (`prod`)**

<augment_code_snippet path="src/main/resources/application-prod.properties" mode="EXCERPT">
````properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.flyway.enabled=true
logging.level.br.com.scfmei=INFO
````
</augment_code_snippet>

---

## 🛡️ **SEGURANÇA EM PRODUÇÃO**

### **Antes de usar `prod`:**

1. **✅ Configure senhas fortes no `.env`**
   ```bash
   MYSQL_ROOT_PASSWORD=SuaSenhaForte123!@#
   MYSQL_PASSWORD=OutraSenhaForte456!@#
   ```

2. **✅ Configure SSL/HTTPS**
   - Use Nginx com Let's Encrypt
   - Ou use Cloudflare

3. **✅ Crie as migrations do Flyway**
   ```bash
   mkdir -p src/main/resources/db/migration
   # Crie: V1__initial_schema.sql
   ```

4. **✅ Crie o diretório de logs**
   ```bash
   mkdir -p ./logs
   chmod 755 ./logs
   ```

5. **✅ Configure credenciais reais**
   - Mercado Pago (produção)
   - Email (Gmail ou SendGrid)

---

## 📝 **BOAS PRÁTICAS**

### **✅ FAÇA:**

- Use `dev` para desenvolvimento local
- Use `prod` para produção e staging
- Configure `.env` com credenciais corretas
- Teste em staging antes de produção
- Monitore logs em produção

### **❌ NÃO FAÇA:**

- Nunca use `dev` em produção
- Nunca use `ddl-auto=update` em produção
- Nunca exponha todos os endpoints do Actuator em produção
- Nunca commite o arquivo `.env` no Git
- Nunca use senhas fracas em produção

---

## 🔧 **TROUBLESHOOTING**

### **Problema: Perfil não está sendo aplicado**

**Solução:**
```bash
# Verificar variável de ambiente
echo $SPRING_PROFILES_ACTIVE

# Verificar arquivo .env
cat .env | grep SPRING_PROFILES_ACTIVE

# Forçar perfil no docker-compose
docker compose down
export SPRING_PROFILES_ACTIVE=prod
docker compose up -d
```

### **Problema: Flyway falhando em produção**

**Solução:**
```bash
# Criar baseline se o banco já existir
# Adicione ao application-prod.properties:
spring.flyway.baseline-on-migrate=true
```

### **Problema: Logs não aparecem em arquivo**

**Solução:**
```bash
# Criar diretório de logs
mkdir -p ./logs
chmod 755 ./logs

# Verificar permissões no Docker
docker exec scf-mei-app ls -la /var/log/scf-mei
```

---

## 📚 **PRÓXIMOS PASSOS**

1. **Testar perfil `dev`:**
   ```bash
   export SPRING_PROFILES_ACTIVE=dev
   ./docker-start.sh restart
   ```

2. **Criar migrations do Flyway** (para usar `prod`)

3. **Configurar ambiente de staging** (usar `prod`)

4. **Configurar monitoramento** (Prometheus + Grafana)

---

**Dúvidas?** Consulte a documentação oficial do Spring Boot:
- https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles

