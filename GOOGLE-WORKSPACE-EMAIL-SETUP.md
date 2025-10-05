# 📧 Configuração de Email - Google Workspace

Este guia mostra como configurar o envio de emails usando **Google Workspace** (Gmail Corporativo) no SCF-MEI.

---

## 📋 Índice

1. [Pré-requisitos](#pré-requisitos)
2. [Configuração do Google Workspace](#configuração-do-google-workspace)
3. [Gerar Senha de App](#gerar-senha-de-app)
4. [Configurar Variáveis de Ambiente](#configurar-variáveis-de-ambiente)
5. [Testar Envio de Email](#testar-envio-de-email)
6. [Emails Implementados](#emails-implementados)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Pré-requisitos

- ✅ Conta do **Google Workspace** ativa
- ✅ Acesso administrativo à conta
- ✅ Verificação em 2 etapas habilitada

---

## 🔧 Configuração do Google Workspace

### **1. Habilitar Verificação em 2 Etapas**

A verificação em 2 etapas é **obrigatória** para gerar senhas de app.

1. Acesse: https://myaccount.google.com/security
2. Clique em **"Verificação em duas etapas"**
3. Siga as instruções para habilitar
4. Escolha o método (SMS, app autenticador, etc.)

---

## 🔑 Gerar Senha de App

### **Passo 1: Acessar Senhas de App**

1. Acesse: https://myaccount.google.com/apppasswords
2. Faça login com sua conta do Google Workspace
3. Você verá a página "Senhas de app"

### **Passo 2: Criar Nova Senha de App**

1. Clique em **"Selecionar app"**
2. Escolha **"Outro (nome personalizado)"**
3. Digite: `SCF-MEI Email Service`
4. Clique em **"Gerar"**

### **Passo 3: Copiar a Senha**

1. O Google mostrará uma senha de **16 caracteres**
2. **COPIE ESTA SENHA** (você não poderá vê-la novamente!)
3. Exemplo: `abcd efgh ijkl mnop` (sem espaços)
4. Guarde em local seguro

---

## ⚙️ Configurar Variáveis de Ambiente

### **1. Editar o arquivo `.env`**

Se você ainda não tem o arquivo `.env`, crie a partir do exemplo:

```bash
cp .env.example .env
```

### **2. Configurar as variáveis de email**

Edite o arquivo `.env` e configure:

```bash
# ========================================
# EMAIL - Configurações de Envio de Email
# ========================================

# Email do remetente (seu email do Google Workspace)
# Exemplo: noreply@seudominio.com.br
MAIL_USERNAME=noreply@seudominio.com.br

# Senha de App do Google (16 caracteres, SEM ESPAÇOS)
# Exemplo: abcdefghijklmnop
MAIL_PASSWORD=abcdefghijklmnop

# Nome do remetente (aparece no email)
MAIL_FROM_NAME=SCF-MEI - Sistema de Controle Financeiro
```

### **3. Configurar URL base da aplicação**

```bash
# URL base da aplicação (para links nos emails)
# Desenvolvimento: http://localhost:8080
# Produção: https://seudominio.com.br
APP_BASE_URL=https://seudominio.com.br
```

---

## 🧪 Testar Envio de Email

### **1. Reiniciar a aplicação**

```bash
./docker-start.sh restart
```

### **2. Testar recuperação de senha**

1. Acesse: http://localhost:8080/recuperar-senha
2. Digite um email válido
3. Clique em "Enviar"
4. Verifique a caixa de entrada do email

### **3. Verificar logs**

```bash
docker compose logs app | grep -i email
```

Você deve ver:

```
✅ Email de recuperação de senha enviado para: usuario@exemplo.com
```

---

## 📧 Emails Implementados

O sistema envia os seguintes emails automaticamente:

### **1. Recuperação de Senha** 🔐

**Quando:** Usuário solicita recuperação de senha

**Conteúdo:**
- Link para redefinir senha
- Token com validade de 1 hora
- Instruções de segurança

**Template:** HTML responsivo com design profissional

---

### **2. Boas-Vindas** 🎉

**Quando:** Novo usuário se registra

**Conteúdo:**
- Mensagem de boas-vindas
- Recursos do plano FREE
- Link para upgrade PRO
- Link para dashboard

**Template:** HTML responsivo com gradiente roxo

---

### **3. Pagamento Aprovado** ✅

**Quando:** Pagamento da assinatura é aprovado

**Conteúdo:**
- Confirmação de pagamento
- Detalhes da assinatura (plano, valor, próxima cobrança)
- ID da transação
- Recursos do plano PRO
- Link para dashboard

**Template:** HTML responsivo com gradiente verde

---

### **4. Pagamento Pendente** ⏳

**Quando:** Pagamento está aguardando confirmação

**Conteúdo:**
- Status do pagamento
- Detalhes da transação
- Instruções por forma de pagamento (Boleto, Pix, Cartão)
- Tempo estimado de confirmação
- Link para ver status

**Template:** HTML responsivo com gradiente laranja

---

### **5. Falha de Pagamento** ❌

**Quando:** Pagamento falha ou é recusado

**Conteúdo:**
- Notificação de falha
- Motivo da falha
- Instruções para resolver
- Prazo de 7 dias para regularizar
- Link para atualizar forma de pagamento

**Template:** HTML responsivo com gradiente vermelho

---

### **6. Upgrade de Plano** 🚀

**Quando:** Usuário faz upgrade para PRO

**Conteúdo:**
- Confirmação de upgrade
- Recursos do plano PRO
- Detalhes da assinatura
- Link para dashboard

---

### **7. Cancelamento de Assinatura** 😢

**Quando:** Usuário cancela assinatura PRO

**Conteúdo:**
- Confirmação de cancelamento
- Motivo do cancelamento
- Limitações do plano FREE
- Link para reativar

---

## 🎨 Características dos Templates

Todos os emails têm:

- ✅ **Design Responsivo** - Funciona em desktop e mobile
- ✅ **HTML Profissional** - Código limpo e semântico
- ✅ **Gradientes Modernos** - Visual atraente
- ✅ **Cores por Tipo** - Verde (sucesso), Laranja (aviso), Vermelho (erro)
- ✅ **Botões de Ação** - CTAs claros e destacados
- ✅ **Tabelas de Informação** - Dados organizados
- ✅ **Encoding UTF-8** - Suporte a caracteres especiais
- ✅ **Footer Padrão** - Informações da empresa

---

## 🔍 Troubleshooting

### **Problema: Email não está sendo enviado**

**Solução:**

1. Verifique se a senha de app está correta (sem espaços)
2. Verifique se a verificação em 2 etapas está habilitada
3. Verifique os logs da aplicação:

```bash
docker compose logs app | grep -i email
```

---

### **Problema: Erro "Authentication failed"**

**Solução:**

1. Gere uma nova senha de app
2. Copie a senha SEM ESPAÇOS
3. Atualize o arquivo `.env`
4. Reinicie a aplicação

---

### **Problema: Email cai no spam**

**Solução:**

1. Configure SPF, DKIM e DMARC no seu domínio
2. Use um email corporativo (não use `noreply@gmail.com`)
3. Adicione o remetente à lista de contatos
4. Peça aos usuários para marcarem como "Não é spam"

---

### **Problema: Erro "Connection timeout"**

**Solução:**

1. Verifique se a porta 587 está aberta no firewall
2. Verifique se o servidor tem acesso à internet
3. Tente usar a porta 465 (SSL) em vez de 587 (TLS)

Edite `application.properties`:

```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```

---

### **Problema: Email demora muito para enviar**

**Solução:**

1. Aumente os timeouts em `application.properties`:

```properties
spring.mail.properties.mail.smtp.connectiontimeout=10000
spring.mail.properties.mail.smtp.timeout=10000
spring.mail.properties.mail.smtp.writetimeout=10000
```

2. Verifique a latência da rede
3. Considere usar um serviço de email assíncrono

---

## 📊 Monitoramento de Emails

### **Ver logs de emails enviados**

```bash
docker compose logs app | grep "Email.*enviado"
```

### **Ver erros de email**

```bash
docker compose logs app | grep "Erro ao enviar email"
```

### **Habilitar debug do JavaMail**

Edite `application.properties`:

```properties
spring.mail.properties.mail.debug=true
```

Reinicie a aplicação e veja logs detalhados.

---

## 🚀 Próximos Passos

1. ✅ Configure SPF, DKIM e DMARC no seu domínio
2. ✅ Teste todos os tipos de email
3. ✅ Configure monitoramento de taxa de entrega
4. ✅ Implemente fila de emails (opcional)
5. ✅ Configure templates personalizados (opcional)

---

## 📚 Recursos Adicionais

- [Google Workspace - Senhas de App](https://support.google.com/accounts/answer/185833)
- [JavaMail API Documentation](https://javaee.github.io/javamail/)
- [Spring Boot Mail](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)

---

## 💡 Dicas de Segurança

1. ✅ **NUNCA** commite o arquivo `.env` no Git
2. ✅ Use senhas de app diferentes para cada aplicação
3. ✅ Revogue senhas de app não utilizadas
4. ✅ Monitore atividade suspeita na conta
5. ✅ Use HTTPS em produção
6. ✅ Valide emails antes de enviar
7. ✅ Implemente rate limiting para evitar spam

---

**Configuração concluída! 🎉**

Agora você pode enviar emails profissionais usando Google Workspace.

