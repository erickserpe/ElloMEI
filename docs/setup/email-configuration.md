# 📧 Configuração de Email - Google Workspace

Este guia mostra como configurar o envio de emails usando **Google Workspace** (Gmail Corporativo) no SCF-MEI.

---

## 📋 Índice

1. [Pré-requisitos](#pré-requisitos)
2. [Configuração Rápida (5 minutos)](#configuração-rápida-5-minutos)
3. [Gerar Senha de App](#gerar-senha-de-app)
4. [Configurar Variáveis de Ambiente](#configurar-variáveis-de-ambiente)
5. [Testar Envio de Email](#testar-envio-de-email)
6. [Emails Implementados](#emails-implementados)
7. [Exemplos de Configuração](#exemplos-de-configuração)
8. [Troubleshooting](#troubleshooting)

---

## 🎯 Pré-requisitos

- ✅ Conta do **Google Workspace** ativa
- ✅ Acesso administrativo à conta
- ✅ Verificação em 2 etapas habilitada

---

## ⚡ Configuração Rápida (5 minutos)

### **PASSO 1: Gerar Senha de App do Google**

1. Acesse: https://myaccount.google.com/apppasswords
2. Faça login com sua conta do **Google Workspace**
3. Se pedir para habilitar verificação em 2 etapas, faça isso primeiro
4. Clique em **"Selecionar app"** → **"Outro (nome personalizado)"**
5. Digite: `SCF-MEI Email Service`
6. Clique em **"Gerar"**
7. **COPIE A SENHA** (16 caracteres)

**Exemplo:**
```
abcd efgh ijkl mnop  ← Como o Google mostra (COM espaços)
abcdefghijklmnop     ← Como você deve usar (SEM espaços)
```

---

### **PASSO 2: Editar o arquivo `.env`**

```bash
cd /home/es_luan/IdeaProjects/SCF-MEI
nano .env
```

**Cole estas configurações:**

```bash
# ========================================
# EMAIL - Google Workspace
# ========================================

# Email do remetente (seu domínio corporativo)
MAIL_USERNAME=noreply@ellomei.com

# Senha de App do Google (16 caracteres, SEM ESPAÇOS)
MAIL_PASSWORD=sua_senha_de_app_aqui

# Nome do remetente (aparece no email)
MAIL_FROM_NAME=Ello MEI - Sistema de Controle Financeiro

# URL base da aplicação (para links nos emails)
APP_BASE_URL=http://localhost:8080
```

**Salve:** `Ctrl + O` → `Enter` → `Ctrl + X`

---

### **PASSO 3: Reiniciar a Aplicação**

```bash
docker compose down
docker compose up -d
```

---

## 🔧 Gerar Senha de App

### **1. Habilitar Verificação em 2 Etapas**

Se ainda não tiver habilitado:

1. Acesse: https://myaccount.google.com/security
2. Clique em **"Verificação em duas etapas"**
3. Siga as instruções para configurar

### **2. Gerar Senha de App**

1. Acesse: https://myaccount.google.com/apppasswords
2. Faça login com sua conta do Google Workspace
3. Clique em **"Selecionar app"** → **"Outro (nome personalizado)"**
4. Digite: `SCF-MEI Email Service`
5. Clique em **"Gerar"**
6. **COPIE A SENHA** (16 caracteres sem espaços)

**⚠️ IMPORTANTE:**
- A senha é mostrada **UMA VEZ APENAS**
- Copie e guarde em local seguro
- Remova os espaços ao colar no `.env`

---

## 🔐 Configurar Variáveis de Ambiente

### **Arquivo `.env` (Desenvolvimento)**

```bash
# ========================================
# EMAIL - Google Workspace
# ========================================

MAIL_USERNAME=noreply@ellomei.com
MAIL_PASSWORD=abcdefghijklmnop
MAIL_FROM_NAME=Ello MEI - Sistema de Controle Financeiro
APP_BASE_URL=http://localhost:8080
```

### **Arquivo `.env.prod` (Produção)**

```bash
# ========================================
# EMAIL - Google Workspace (PRODUÇÃO)
# ========================================

MAIL_USERNAME=noreply@seudominio.com.br
MAIL_PASSWORD=sua_senha_de_app_producao
MAIL_FROM_NAME=Seu Sistema - Nome Oficial
APP_BASE_URL=https://seudominio.com.br
```

---

## 🧪 Testar Envio de Email

### **Opção 1: Script de Teste Rápido**

```bash
./testar-email.sh
```

### **Opção 2: Criar Novo Usuário**

1. Acesse: http://localhost:8080/registro
2. Preencha o formulário
3. Clique em **"Cadastrar"**
4. Verifique seu email (caixa de entrada ou spam)

### **Opção 3: Recuperação de Senha**

1. Acesse: http://localhost:8080/recuperar-senha
2. Digite seu email
3. Clique em **"Enviar Instruções"**
4. Verifique seu email

---

## 📨 Emails Implementados

| Email | Quando é Enviado | Template |
|-------|------------------|----------|
| **Boas-Vindas** | Após cadastro de novo usuário | HTML profissional |
| **Recuperação de Senha** | Ao solicitar reset de senha | HTML com link seguro |
| **Pagamento Aprovado** | Webhook Mercado Pago - aprovado | HTML com detalhes |
| **Pagamento Pendente** | Webhook Mercado Pago - pendente | HTML com instruções |
| **Falha de Pagamento** | Webhook Mercado Pago - falha | HTML com suporte |

---

## 📝 Exemplos de Configuração

### **Cenário 1: Google Workspace (Recomendado)**

```bash
MAIL_USERNAME=noreply@seudominio.com.br
MAIL_PASSWORD=abcdefghijklmnop
MAIL_FROM_NAME=SCF-MEI - Sistema de Controle Financeiro
APP_BASE_URL=https://seudominio.com.br
```

**Vantagens:**
- ✅ Alta taxa de entrega
- ✅ Não cai em spam
- ✅ Suporte profissional
- ✅ Domínio personalizado

### **Cenário 2: Gmail Pessoal (Desenvolvimento)**

```bash
MAIL_USERNAME=seuemail@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
MAIL_FROM_NAME=SCF-MEI Dev
APP_BASE_URL=http://localhost:8080
```

**Limitações:**
- ⚠️ Limite de 500 emails/dia
- ⚠️ Pode cair em spam
- ⚠️ Não recomendado para produção

---

## 🔍 Troubleshooting

### **Problema 1: Email não está sendo enviado**

**Verificar logs:**
```bash
docker compose logs app | grep -i "email\|mail"
```

**Possíveis causas:**
- ❌ Senha de app incorreta
- ❌ Verificação em 2 etapas não habilitada
- ❌ Email bloqueado pelo Google

**Solução:**
1. Verifique se a senha está correta (sem espaços)
2. Confirme que a verificação em 2 etapas está ativa
3. Gere uma nova senha de app

### **Problema 2: Email cai em spam**

**Soluções:**
- ✅ Use Google Workspace (domínio próprio)
- ✅ Configure SPF, DKIM e DMARC no DNS
- ✅ Evite palavras como "grátis", "promoção" no assunto

### **Problema 3: Erro "Authentication failed"**

**Verificar:**
```bash
# Ver variáveis de ambiente
docker compose exec app env | grep MAIL
```

**Solução:**
1. Confirme que `MAIL_USERNAME` é o email completo
2. Confirme que `MAIL_PASSWORD` é a senha de app (não a senha da conta)
3. Remova espaços da senha

### **Problema 4: Erro "Connection timeout"**

**Verificar firewall:**
```bash
# Testar conexão SMTP
telnet smtp.gmail.com 587
```

**Solução:**
- Verifique se a porta 587 está aberta
- Confirme que não há firewall bloqueando

---

## 📊 Verificar Status

### **Logs da Aplicação**

```bash
# Ver últimos 50 logs
docker compose logs app --tail=50

# Filtrar apenas emails
docker compose logs app | grep "Email"

# Acompanhar em tempo real
docker compose logs app -f
```

### **Logs Esperados (Sucesso)**

```
✅ Email de boas-vindas enviado para: usuario@exemplo.com
✅ Email de recuperação enviado para: usuario@exemplo.com
```

### **Logs de Erro**

```
❌ Erro ao enviar email: Authentication failed
❌ Erro ao enviar email: Connection timeout
```

---

## 🎯 Checklist Final

- [ ] Verificação em 2 etapas habilitada
- [ ] Senha de app gerada
- [ ] Arquivo `.env` configurado
- [ ] Senha sem espaços
- [ ] Aplicação reiniciada
- [ ] Email de teste enviado
- [ ] Email recebido (verificar spam)

---

## 📞 Suporte

Se ainda tiver problemas:

1. Verifique os logs: `docker compose logs app --tail=100`
2. Confirme as variáveis: `docker compose exec app env | grep MAIL`
3. Teste a conexão SMTP: `telnet smtp.gmail.com 587`

---

**Documentação atualizada em:** 05/10/2025  
**Versão:** 2.0 (Consolidada)

