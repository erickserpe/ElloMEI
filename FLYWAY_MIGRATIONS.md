# 🦅 FLYWAY MIGRATIONS - SCF-MEI

Este documento explica o sistema de migrações de banco de dados usando Flyway.

---

## 📋 **ÍNDICE**

1. [O que é Flyway?](#-o-que-é-flyway)
2. [Por que usar Flyway?](#-por-que-usar-flyway)
3. [Como Funciona](#-como-funciona)
4. [Estrutura de Migrations](#-estrutura-de-migrations)
5. [Convenções de Nomenclatura](#-convenções-de-nomenclatura)
6. [Migrations Existentes](#-migrations-existentes)
7. [Criar Nova Migration](#-criar-nova-migration)
8. [Configuração](#-configuração)
9. [Comandos Úteis](#-comandos-úteis)

---

## 🤔 **O QUE É FLYWAY?**

Flyway é uma ferramenta de versionamento de banco de dados que aplica migrations (migrações) de forma automática e controlada.

**Analogia:**
Assim como o Git versiona código, o Flyway versiona o schema do banco de dados.

**Conceito:**
- Cada migration é um arquivo SQL numerado (V1, V2, V3...)
- Flyway rastreia quais migrations já foram aplicadas
- Novas migrations são aplicadas automaticamente no startup
- Impossível aplicar a mesma migration duas vezes

---

## 🎯 **POR QUE USAR FLYWAY?**

### **1. Versionamento de Schema** 📚
**Problema:** Sem Flyway, o schema é gerenciado pelo Hibernate (ddl-auto=update).

**Problemas do ddl-auto=update:**
- ❌ Não cria índices customizados
- ❌ Não remove colunas antigas
- ❌ Não faz alterações complexas
- ❌ Pode corromper dados em produção
- ❌ Sem histórico de mudanças

**Solução com Flyway:**
- ✅ Controle total sobre o schema
- ✅ Histórico completo de mudanças
- ✅ Migrations testadas antes de produção
- ✅ Rollback possível (com migrations reversas)
- ✅ Seguro para produção

---

### **2. Ambientes Consistentes** 🔄
**Problema:** Dev, staging e produção com schemas diferentes.

**Solução:** Flyway garante que todos os ambientes tenham o mesmo schema.

---

### **3. Deploy Seguro** 🛡️
**Problema:** Deploy manual de SQL pode falhar ou ser esquecido.

**Solução:** Flyway aplica migrations automaticamente no startup.

---

### **4. Colaboração em Equipe** 👥
**Problema:** Múltiplos desenvolvedores alterando o schema.

**Solução:** Migrations versionadas evitam conflitos.

---

## ⚙️ **COMO FUNCIONA**

### **Fluxo de Execução:**

```
1. Aplicação inicia
2. Flyway verifica tabela flyway_schema_history
3. Compara migrations no código vs banco
4. Aplica migrations pendentes em ordem
5. Registra migrations aplicadas
6. Aplicação continua normalmente
```

### **Tabela de Controle:**

Flyway cria automaticamente a tabela `flyway_schema_history`:

```sql
SELECT * FROM flyway_schema_history;
```

| installed_rank | version | description | type | script | checksum | installed_by | installed_on | execution_time | success |
|----------------|---------|-------------|------|--------|----------|--------------|--------------|----------------|---------|
| 1 | 1 | initial schema | SQL | V1__initial_schema.sql | 123456789 | scf_user | 2025-10-04 20:00:00 | 1234 | 1 |
| 2 | 2 | add email column | SQL | V2__add_email_column.sql | 987654321 | scf_user | 2025-10-05 10:00:00 | 56 | 1 |

---

## 📁 **ESTRUTURA DE MIGRATIONS**

```
src/main/resources/db/migration/
├── V1__initial_schema.sql          # Migration inicial (schema completo)
├── V2__add_email_to_usuario.sql    # Adiciona coluna email
├── V3__create_roles_table.sql      # Cria tabela de roles
└── V4__add_indexes.sql             # Adiciona índices de performance
```

**Regras:**
- ✅ Arquivos em `src/main/resources/db/migration/`
- ✅ Nome: `V{versão}__{descrição}.sql`
- ✅ Versão: Número sequencial (1, 2, 3...)
- ✅ Descrição: Snake_case (palavras separadas por _)
- ✅ Extensão: `.sql`

---

## 📝 **CONVENÇÕES DE NOMENCLATURA**

### **Formato:**
```
V{versão}__{descrição}.sql
```

### **Exemplos Válidos:**
```
V1__initial_schema.sql
V2__add_email_column.sql
V3__create_roles_table.sql
V4__add_indexes_for_performance.sql
V5__alter_usuario_add_phone.sql
V10__seed_initial_data.sql
V11__drop_old_table.sql
```

### **Exemplos Inválidos:**
```
❌ v1__initial_schema.sql           (v minúsculo)
❌ V1_initial_schema.sql             (apenas 1 underscore)
❌ V1__initial-schema.sql            (hífen em vez de underscore)
❌ V1__Initial_Schema.sql            (CamelCase)
❌ migration_1.sql                   (sem prefixo V)
❌ V1.sql                            (sem descrição)
```

---

## 📦 **MIGRATIONS EXISTENTES**

### **V1__initial_schema.sql**

**Descrição:** Cria o schema inicial completo do SCF-MEI.

**Tabelas criadas:**
- `usuario` - Usuários do sistema (MEIs)
- `role` - Roles de permissão
- `usuario_roles` - Relacionamento Many-to-Many
- `assinaturas` - Assinaturas dos planos
- `historico_pagamentos` - Histórico de pagamentos
- `categoria_despesa` - Categorias de despesas
- `contato` - Contatos (clientes/fornecedores)
- `conta` - Contas bancárias
- `lancamento` - Lançamentos financeiros
- `comprovante` - Comprovantes anexados
- `password_reset_tokens` - Tokens de recuperação de senha

**Índices criados:**
- `idx_assinaturas_usuario_status` - Performance em buscas de assinaturas
- `idx_lancamento_data` - Performance em buscas por data
- `idx_lancamento_usuario_data` - Performance em dashboard
- `idx_password_reset_expiracao` - Limpeza de tokens expirados

**Dados iniciais (seed):**
- Roles: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_PRO`

---

## ➕ **CRIAR NOVA MIGRATION**

### **Passo 1: Determinar a próxima versão**

```bash
# Ver última migration
ls -1 src/main/resources/db/migration/ | tail -1
# Saída: V1__initial_schema.sql

# Próxima versão: V2
```

---

### **Passo 2: Criar arquivo**

```bash
# Exemplo: Adicionar coluna telefone ao usuário
touch src/main/resources/db/migration/V2__add_phone_to_usuario.sql
```

---

### **Passo 3: Escrever SQL**

```sql
-- ===================================
-- Flyway Migration V2: Add Phone to Usuario
-- ===================================
-- Descrição: Adiciona coluna de telefone à tabela usuario
-- Autor: SCF-MEI Team
-- Data: 2025-10-04
-- ===================================

-- Adicionar coluna telefone
ALTER TABLE usuario 
ADD COLUMN telefone VARCHAR(20) DEFAULT NULL AFTER email;

-- Adicionar índice (opcional)
CREATE INDEX idx_usuario_telefone ON usuario(telefone);

-- ===================================
-- Fim da Migration V2
-- ===================================
```

---

### **Passo 4: Testar localmente**

```bash
# Parar aplicação
./docker-start.sh stop

# Iniciar (Flyway aplicará a migration)
./docker-start.sh start

# Verificar logs
docker compose logs app | grep Flyway
```

**Saída esperada:**
```
Flyway Community Edition 10.x.x by Redgate
Database: jdbc:mysql://mysql:3306/scf_mei_db (MySQL 8.0)
Successfully validated 2 migrations (execution time 00:00.012s)
Current version of schema `scf_mei_db`: 1
Migrating schema `scf_mei_db` to version "2 - add phone to usuario"
Successfully applied 1 migration to schema `scf_mei_db`, now at version v2 (execution time 00:00.045s)
```

---

### **Passo 5: Verificar no banco**

```bash
# Conectar ao MySQL
docker exec -it scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db

# Verificar coluna
DESCRIBE usuario;

# Verificar histórico do Flyway
SELECT * FROM flyway_schema_history;
```

---

## 🔧 **CONFIGURAÇÃO**

### **application-dev.properties:**

```properties
# Flyway DESABILITADO em desenvolvimento
# Hibernate gerencia o schema (ddl-auto=update)
spring.flyway.enabled=false
spring.jpa.hibernate.ddl-auto=update
```

**Por quê?**
- Em dev, queremos agilidade
- Hibernate cria/atualiza tabelas automaticamente
- Não precisamos criar migrations para cada teste

---

### **application-prod.properties:**

```properties
# Flyway HABILITADO em produção
# Hibernate apenas valida o schema (ddl-auto=validate)
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```

**Por quê?**
- Em prod, queremos segurança
- Flyway controla todas as mudanças
- Hibernate não pode alterar o schema
- Migrations são testadas antes do deploy

---

### **Configurações Adicionais:**

```properties
# Baseline (primeira execução em banco existente)
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0

# Localização das migrations
spring.flyway.locations=classpath:db/migration

# Validar checksums (detectar alterações em migrations antigas)
spring.flyway.validate-on-migrate=true

# Placeholder substitution (usar variáveis)
spring.flyway.placeholder-replacement=true
spring.flyway.placeholders.tablespace=scf_mei_tablespace
```

---

## 🛠️ **COMANDOS ÚTEIS**

### **Ver histórico de migrations:**

```sql
SELECT 
    installed_rank,
    version,
    description,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

---

### **Forçar re-execução (CUIDADO!):**

```sql
-- APENAS EM DESENVOLVIMENTO!
-- Remove histórico do Flyway (não remove tabelas)
DELETE FROM flyway_schema_history;

-- Reiniciar aplicação para re-aplicar migrations
```

---

### **Baseline em banco existente:**

Se você já tem um banco de dados em produção e quer começar a usar Flyway:

```properties
# application-prod.properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```

Isso marca a versão 1 como já aplicada, e Flyway aplicará apenas V2, V3, etc.

---

## ✅ **BOAS PRÁTICAS**

### **1. Nunca altere migrations aplicadas** ❌
```sql
❌ ERRADO: Editar V1__initial_schema.sql depois de aplicado
✅ CERTO: Criar V2__fix_initial_schema.sql
```

**Por quê?**
- Flyway valida checksums
- Alteração causa erro de validação
- Outros ambientes já aplicaram a versão antiga

---

### **2. Teste migrations antes de produção** ✅
```bash
# Testar em dev
./docker-start.sh restart

# Testar em staging
SPRING_PROFILES_ACTIVE=prod ./docker-start.sh restart

# Só então fazer deploy em produção
```

---

### **3. Migrations devem ser idempotentes** ✅
```sql
✅ CERTO:
CREATE TABLE IF NOT EXISTS usuario (...);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS email VARCHAR(255);

❌ ERRADO:
CREATE TABLE usuario (...);  -- Falha se já existe
ALTER TABLE usuario ADD COLUMN email VARCHAR(255);  -- Falha se já existe
```

---

### **4. Use transações** ✅
```sql
-- Flyway executa cada migration em uma transação
-- Se algo falhar, tudo é revertido
START TRANSACTION;
  ALTER TABLE usuario ADD COLUMN telefone VARCHAR(20);
  UPDATE usuario SET telefone = '0000-0000' WHERE telefone IS NULL;
COMMIT;
```

---

### **5. Documente migrations complexas** ✅
```sql
-- ===================================
-- Migration V5: Migrar dados de plano
-- ===================================
-- ATENÇÃO: Esta migration pode demorar em bancos grandes!
-- Estimativa: 1 segundo por 1000 usuários
-- ===================================

UPDATE usuario SET plano = 'FREE' WHERE plano IS NULL;
```

---

## 🎉 **RESUMO**

Com Flyway implementado:

- ✅ **Schema versionado** (V1, V2, V3...)
- ✅ **Histórico completo** (flyway_schema_history)
- ✅ **Deploy automático** (migrations aplicadas no startup)
- ✅ **Seguro para produção** (ddl-auto=validate)
- ✅ **Rollback possível** (migrations reversas)
- ✅ **Colaboração facilitada** (sem conflitos de schema)
- ✅ **Pronto para produção** ✨

---

**Dúvidas?** Consulte a documentação oficial:
- https://flywaydb.org/documentation/

