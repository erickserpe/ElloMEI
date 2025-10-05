# 🔒 Checklist de Segurança - ElloMEI

**Data:** 05/10/2025  
**Objetivo:** Garantir que informações sensíveis não sejam expostas no GitHub

---

## ✅ **STATUS ATUAL**

### **O QUE ESTÁ SEGURO** ✅

1. ✅ **`.env` não está no Git** - Apenas `.env.example` está versionado
2. ✅ **`.gitignore` configurado** - Ignora `.env`, backups, logs, keystores
3. ✅ **Variáveis de ambiente** - Credenciais vêm de variáveis de ambiente
4. ✅ **Backups ignorados** - `*.sql`, `*.sql.gz` não vão para o Git

---

## ⚠️ **PROBLEMAS ENCONTRADOS**

### **1. Senhas Padrão em `application.properties`**

**Arquivo:** `src/main/resources/application.properties`

```properties
# ❌ PROBLEMA: Senha padrão hardcoded
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD:ellomei2025}
```

**Risco:** Se alguém não configurar a variável `SSL_KEYSTORE_PASSWORD`, a senha padrão `ellomei2025` será usada.

**Solução:** Remover valor padrão ou usar placeholder genérico.

---

### **2. Informações Sensíveis em `.env.example`**

**Arquivo:** `.env.example`

O arquivo `.env.example` deve conter apenas **PLACEHOLDERS**, nunca valores reais.

**Verificar:**
- ❌ Não deve ter senhas reais
- ❌ Não deve ter tokens reais
- ❌ Não deve ter emails reais
- ✅ Deve ter apenas exemplos genéricos

---

### **3. Credenciais em Comentários**

**Risco:** Às vezes desenvolvedores deixam credenciais em comentários.

**Verificar:**
- Comentários em arquivos `.properties`
- Comentários em arquivos `.yml`
- Comentários em código Java

---

## 🛡️ **AÇÕES CORRETIVAS**

### **AÇÃO 1: Atualizar `.env.example`**

Garantir que contém apenas placeholders:

```bash
# ❌ ERRADO
MYSQL_PASSWORD=5522
MAIL_PASSWORD=vjcwlklyzrfnysem

# ✅ CORRETO
MYSQL_PASSWORD=sua_senha_forte_aqui
MAIL_PASSWORD=sua_senha_de_app_do_google_aqui
```

---

### **AÇÃO 2: Remover Senhas Padrão**

**Antes:**
```properties
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD:ellomei2025}
```

**Depois:**
```properties
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD:changeme}
```

---

### **AÇÃO 3: Criar `.env.local` para Desenvolvimento**

Criar arquivo `.env.local` (ignorado pelo Git) para desenvolvimento local:

```bash
# .env.local - Credenciais locais (NÃO COMMITAR!)
MYSQL_PASSWORD=senha_dev_local
MAIL_PASSWORD=senha_email_dev
```

Adicionar ao `.gitignore`:
```
.env
.env.local
.env.*.local
```

---

### **AÇÃO 4: Rotacionar Credenciais Expostas**

Se alguma credencial foi commitada no Git, você DEVE rotacioná-la:

#### **Email (Google Workspace)**
1. Acesse: https://myaccount.google.com/apppasswords
2. Revogue a senha de app antiga
3. Gere uma nova senha de app
4. Atualize o `.env` local

#### **Mercado Pago**
1. Acesse: https://www.mercadopago.com.br/developers/panel/app
2. Revogue os tokens antigos
3. Gere novos tokens
4. Atualize o `.env` local

#### **MySQL**
1. Conecte ao MySQL
2. Altere a senha:
   ```sql
   ALTER USER 'scf_user'@'%' IDENTIFIED BY 'nova_senha_forte';
   FLUSH PRIVILEGES;
   ```
3. Atualize o `.env` local

---

## 🔍 **VERIFICAÇÃO DE SEGURANÇA**

### **Comando 1: Verificar se `.env` está no Git**

```bash
git ls-files | grep "\.env$"
```

**Resultado esperado:** Vazio (nenhum arquivo `.env`)

---

### **Comando 2: Verificar histórico do Git**

```bash
git log --all --full-history -- .env
```

**Resultado esperado:** Vazio (`.env` nunca foi commitado)

---

### **Comando 3: Buscar senhas hardcoded**

```bash
grep -r "password.*=" src/main/resources/*.properties | grep -v "\${" | grep -v "#"
```

**Resultado esperado:** Vazio (todas as senhas vêm de variáveis)

---

### **Comando 4: Buscar tokens hardcoded**

```bash
grep -r "token.*=" src/main/resources/*.properties | grep -v "\${" | grep -v "#"
```

**Resultado esperado:** Vazio (todos os tokens vêm de variáveis)

---

## 📋 **CHECKLIST FINAL**

Antes de fazer push para o GitHub, verifique:

- [ ] `.env` está no `.gitignore`
- [ ] `.env` NÃO está no Git (`git ls-files | grep .env`)
- [ ] `.env.example` contém apenas placeholders
- [ ] Nenhuma senha hardcoded em `.properties`
- [ ] Nenhum token hardcoded em `.properties`
- [ ] Keystores (`.p12`, `.jks`) estão no `.gitignore`
- [ ] Backups (`.sql`, `.sql.gz`) estão no `.gitignore`
- [ ] Logs não contêm informações sensíveis
- [ ] Credenciais de produção estão em variáveis de ambiente

---

## 🚀 **BOAS PRÁTICAS**

### **1. Nunca Commite:**
- ❌ Senhas
- ❌ Tokens de API
- ❌ Chaves privadas
- ❌ Certificados SSL
- ❌ Dados de produção
- ❌ Backups de banco de dados

### **2. Sempre Use:**
- ✅ Variáveis de ambiente
- ✅ Arquivos `.env` (ignorados pelo Git)
- ✅ Secrets do GitHub (para CI/CD)
- ✅ Vault ou AWS Secrets Manager (produção)

### **3. Para Produção:**
- ✅ Use senhas fortes (16+ caracteres)
- ✅ Rotacione credenciais regularmente
- ✅ Use HTTPS sempre
- ✅ Habilite autenticação de 2 fatores
- ✅ Monitore acessos suspeitos

---

## 🆘 **SE VOCÊ JÁ COMMITOU CREDENCIAIS**

### **Opção 1: Remover do Histórico (Recomendado)**

```bash
# Instalar BFG Repo-Cleaner
# https://rtyley.github.io/bfg-repo-cleaner/

# Remover arquivo do histórico
bfg --delete-files .env

# Limpar histórico
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push (CUIDADO!)
git push origin --force --all
```

### **Opção 2: Rotacionar Credenciais (Mais Simples)**

1. Rotacione TODAS as credenciais expostas
2. Atualize `.env` local com novas credenciais
3. Continue trabalhando normalmente
4. As credenciais antigas no Git ficam inúteis

---

## 📞 **CONTATOS DE EMERGÊNCIA**

Se você suspeitar que credenciais foram expostas:

1. **Google Workspace:** Revogue senhas de app imediatamente
2. **Mercado Pago:** Revogue tokens no painel de desenvolvedores
3. **MySQL:** Altere senhas via SQL
4. **GitHub:** Considere tornar o repositório privado temporariamente

---

## ✅ **CONCLUSÃO**

Seu projeto está **RELATIVAMENTE SEGURO**, mas precisa de alguns ajustes:

1. ✅ `.env` não está no Git (BOM!)
2. ⚠️ Senhas padrão em `application.properties` (CORRIGIR)
3. ⚠️ Verificar `.env.example` (REVISAR)

**Próximos passos:** Aplicar as ações corretivas listadas acima.

---

**Última atualização:** 05/10/2025  
**Responsável:** Augment Agent  
**Status:** ⚠️ Ação necessária

