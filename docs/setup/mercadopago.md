# 💳 MERCADO PAGO - CONFIGURAÇÃO COMPLETA

Este documento explica como configurar as credenciais do Mercado Pago para desenvolvimento e produção.

---

## 📋 **ÍNDICE**

1. [Visão Geral](#-visão-geral)
2. [Criar Conta no Mercado Pago](#-criar-conta-no-mercado-pago)
3. [Obter Credenciais de Teste](#-obter-credenciais-de-teste)
4. [Obter Credenciais de Produção](#-obter-credenciais-de-produção)
5. [Configurar no SCF-MEI](#-configurar-no-scf-mei)
6. [Configurar Webhooks](#-configurar-webhooks)
7. [Testar Integração](#-testar-integração)
8. [Troubleshooting](#-troubleshooting)

---

## 🤔 **VISÃO GERAL**

O SCF-MEI usa o Mercado Pago para processar pagamentos de assinaturas (Plano PRO).

**Credenciais necessárias:**
- **Access Token**: Token de acesso para API
- **Public Key**: Chave pública para checkout
- **Webhook Secret**: Chave secreta para validar webhooks (opcional)

**Ambientes:**
- **Teste**: Credenciais começam com `TEST-`
- **Produção**: Credenciais começam com `APP_USR-`

---

## 👤 **CRIAR CONTA NO MERCADO PAGO**

### **PASSO 1: Criar Conta**

1. Acesse: https://www.mercadopago.com.br/
2. Clique em "Criar conta"
3. Preencha seus dados:
   - Nome completo
   - Email
   - CPF
   - Senha
4. Confirme seu email

---

### **PASSO 2: Ativar Conta de Vendedor**

1. Faça login em: https://www.mercadopago.com.br/
2. Vá em "Seu negócio" → "Configurações"
3. Complete as informações da sua empresa:
   - Razão social
   - CNPJ (ou CPF se for MEI)
   - Endereço
   - Telefone
4. Aguarde aprovação (geralmente instantânea)

---

## 🧪 **OBTER CREDENCIAIS DE TESTE**

Credenciais de teste permitem testar a integração sem processar pagamentos reais.

### **PASSO 1: Acessar Painel de Desenvolvedores**

1. Acesse: https://www.mercadopago.com.br/developers/panel/app
2. Faça login com sua conta Mercado Pago
3. Clique em "Suas integrações"

---

### **PASSO 2: Criar Aplicação**

1. Clique em "Criar aplicação"
2. Preencha:
   - **Nome**: SCF-MEI (ou o nome que preferir)
   - **Descrição**: Sistema de Controle Financeiro para MEI
   - **Modelo de integração**: Pagamentos online
   - **Produto**: Checkout Pro
3. Clique em "Criar aplicação"

---

### **PASSO 3: Copiar Credenciais de Teste**

1. Na página da aplicação, vá em "Credenciais"
2. Selecione a aba "Credenciais de teste"
3. Copie:
   - **Public Key** (começa com `TEST-`)
   - **Access Token** (começa com `TEST-`)

**Exemplo:**
```
Public Key: TEST-abcd1234-5678-90ef-ghij-klmnopqrstuv
Access Token: TEST-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789
```

---

### **PASSO 4: Criar Usuários de Teste**

Para testar pagamentos, você precisa de usuários de teste (comprador e vendedor).

1. Vá em "Usuários de teste"
2. Clique em "Criar usuário de teste"
3. Preencha:
   - **País**: Brasil
   - **Tipo**: Comprador
4. Clique em "Criar"
5. Repita para criar um usuário vendedor

**Anote as credenciais dos usuários de teste!**

---

## 🚀 **OBTER CREDENCIAIS DE PRODUÇÃO**

Credenciais de produção processam pagamentos reais.

### **PASSO 1: Ativar Modo Produção**

1. Acesse: https://www.mercadopago.com.br/developers/panel/app
2. Selecione sua aplicação
3. Vá em "Credenciais"
4. Selecione a aba "Credenciais de produção"

---

### **PASSO 2: Completar Informações da Conta**

Antes de usar credenciais de produção, você precisa:

1. **Verificar identidade:**
   - Enviar foto do documento (RG ou CNH)
   - Selfie segurando o documento

2. **Configurar dados bancários:**
   - Banco
   - Agência
   - Conta corrente
   - Tipo de conta

3. **Aceitar termos:**
   - Termos de uso
   - Política de privacidade

---

### **PASSO 3: Copiar Credenciais de Produção**

1. Na aba "Credenciais de produção"
2. Copie:
   - **Public Key** (começa com `APP_USR-`)
   - **Access Token** (começa com `APP_USR-`)

**Exemplo:**
```
Public Key: APP_USR-abcd1234-5678-90ef-ghij-klmnopqrstuv
Access Token: APP_USR-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789
```

**⚠️ IMPORTANTE:**
- **NUNCA** commite credenciais de produção no Git!
- Guarde em local seguro (gerenciador de senhas)
- Use variáveis de ambiente

---

## ⚙️ **CONFIGURAR NO SCF-MEI**

### **DESENVOLVIMENTO (Credenciais de Teste)**

Edite o arquivo `.env`:

```bash
# ========================================
# MERCADO PAGO - Configurações de Pagamento
# ========================================

# Access Token de TESTE
MERCADOPAGO_ACCESS_TOKEN=TEST-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789

# Public Key de TESTE
MERCADOPAGO_PUBLIC_KEY=TEST-abcd1234-5678-90ef-ghij-klmnopqrstuv

# Webhook Secret (opcional)
MERCADOPAGO_WEBHOOK_SECRET=

# Base URL (HTTP em desenvolvimento)
APP_BASE_URL=http://localhost:8080
```

---

### **PRODUÇÃO (Credenciais Reais)**

Edite o arquivo `.env` (ou configure no servidor):

```bash
# ========================================
# MERCADO PAGO - Configurações de Pagamento
# ========================================

# Access Token de PRODUÇÃO
MERCADOPAGO_ACCESS_TOKEN=APP_USR-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789

# Public Key de PRODUÇÃO
MERCADOPAGO_PUBLIC_KEY=APP_USR-abcd1234-5678-90ef-ghij-klmnopqrstuv

# Webhook Secret (recomendado)
MERCADOPAGO_WEBHOOK_SECRET=sua-chave-secreta-aqui

# Base URL (HTTPS obrigatório!)
APP_BASE_URL=https://seudominio.com.br
```

**⚠️ SEGURANÇA:**
- Use HTTPS em produção (obrigatório)
- Configure webhook secret
- Nunca exponha credenciais

---

## 🔔 **CONFIGURAR WEBHOOKS**

Webhooks notificam sua aplicação sobre eventos de pagamento (aprovado, recusado, etc.).

### **PASSO 1: Configurar URL do Webhook**

1. Acesse: https://www.mercadopago.com.br/developers/panel/app
2. Selecione sua aplicação
3. Vá em "Webhooks"
4. Clique em "Configurar notificações"
5. Preencha:
   - **URL de produção**: `https://seudominio.com.br/api/mercadopago/webhook`
   - **Eventos**: Selecione "Pagamentos"
6. Clique em "Salvar"

**⚠️ IMPORTANTE:**
- URL deve ser HTTPS (obrigatório em produção)
- URL deve estar acessível publicamente
- Mercado Pago testará a URL antes de salvar

---

### **PASSO 2: Gerar Webhook Secret**

1. Na página de Webhooks
2. Clique em "Gerar secret"
3. Copie o secret gerado
4. Adicione ao `.env`:

```bash
MERCADOPAGO_WEBHOOK_SECRET=seu-secret-aqui
```

**Para que serve?**
O secret valida que a requisição realmente veio do Mercado Pago (segurança).

---

### **PASSO 3: Testar Webhook**

1. Na página de Webhooks
2. Clique em "Testar"
3. Mercado Pago enviará uma requisição de teste
4. Verifique os logs da aplicação:

```bash
docker compose logs app | grep "Webhook"
```

**Saída esperada:**
```
INFO  MercadoPagoWebhookController - Webhook recebido: payment.created
INFO  MercadoPagoWebhookController - Processando pagamento ID: 123456789
```

---

## 🧪 **TESTAR INTEGRAÇÃO**

### **Teste 1: Criar Assinatura**

1. Acesse: http://localhost:8080/assinaturas
2. Clique em "Assinar Plano PRO"
3. Preencha os dados do cartão de teste:

**Cartões de Teste (Aprovado):**
```
Número: 5031 4332 1540 6351
CVV: 123
Validade: 11/25
Nome: APRO (Approved)
CPF: 123.456.789-01
```

**Cartões de Teste (Recusado):**
```
Número: 5031 4332 1540 6351
CVV: 123
Validade: 11/25
Nome: OTHE (Other error)
CPF: 123.456.789-01
```

**Mais cartões de teste:**
https://www.mercadopago.com.br/developers/pt/docs/checkout-api/testing

---

### **Teste 2: Verificar Pagamento**

1. Após criar assinatura, verifique:
   - Status da assinatura: "ATIVA"
   - Histórico de pagamentos
   - Logs da aplicação

2. Verifique no painel do Mercado Pago:
   - https://www.mercadopago.com.br/activities
   - Deve aparecer o pagamento de teste

---

### **Teste 3: Webhook**

1. Simule um evento de pagamento
2. Verifique se o webhook foi recebido:

```bash
docker compose logs app | grep "Webhook"
```

3. Verifique se a assinatura foi atualizada

---

## 🔧 **TROUBLESHOOTING**

### **Erro: "Invalid access token"**

```
MercadoPagoException: Invalid access token
```

**Causas:**
- Access token incorreto
- Access token expirado
- Access token de teste em produção (ou vice-versa)

**Solução:**
```bash
# Verificar access token no .env
grep MERCADOPAGO_ACCESS_TOKEN .env

# Deve começar com:
# - TEST- (desenvolvimento)
# - APP_USR- (produção)

# Gerar novo access token no painel do Mercado Pago
```

---

### **Erro: "Webhook URL must be HTTPS"**

```
Error: The webhook URL must use HTTPS protocol
```

**Causa:** Mercado Pago exige HTTPS em produção.

**Solução:**
1. Configure SSL/HTTPS (veja `SSL_HTTPS_SETUP.md`)
2. Atualize `APP_BASE_URL` para HTTPS
3. Reconfigure webhook com URL HTTPS

---

### **Erro: "Payment rejected"**

```
Payment status: rejected
```

**Causas:**
- Cartão de teste incorreto
- Dados inválidos
- Limite de teste excedido

**Solução:**
```bash
# Usar cartões de teste oficiais
# https://www.mercadopago.com.br/developers/pt/docs/checkout-api/testing

# Verificar logs
docker compose logs app | grep "Payment"
```

---

### **Webhook não está sendo recebido**

**Causas:**
- URL incorreta
- Firewall bloqueando
- Aplicação não está rodando
- HTTPS não configurado

**Solução:**
```bash
# 1. Verificar se aplicação está rodando
curl http://localhost:8080/actuator/health

# 2. Verificar URL do webhook
# Deve ser acessível publicamente

# 3. Testar webhook manualmente
curl -X POST https://seudominio.com.br/api/mercadopago/webhook \
  -H "Content-Type: application/json" \
  -d '{"type":"payment","data":{"id":"123"}}'

# 4. Verificar logs
docker compose logs app | grep "Webhook"
```

---

## 📚 **RECURSOS ÚTEIS**

### **Documentação Oficial:**
- Guia de integração: https://www.mercadopago.com.br/developers/pt/docs
- API Reference: https://www.mercadopago.com.br/developers/pt/reference
- Cartões de teste: https://www.mercadopago.com.br/developers/pt/docs/checkout-api/testing

### **Painel do Mercado Pago:**
- Aplicações: https://www.mercadopago.com.br/developers/panel/app
- Atividades: https://www.mercadopago.com.br/activities
- Configurações: https://www.mercadopago.com.br/settings

### **Suporte:**
- FAQ: https://www.mercadopago.com.br/developers/pt/support
- Fórum: https://www.mercadopago.com.br/developers/pt/community

---

## ✅ **CHECKLIST**

### **Desenvolvimento:**
- [ ] Conta criada no Mercado Pago
- [ ] Aplicação criada no painel
- [ ] Credenciais de teste copiadas
- [ ] Usuários de teste criados
- [ ] `.env` configurado com credenciais de teste
- [ ] Teste de pagamento realizado
- [ ] Webhook testado (opcional em dev)

### **Produção:**
- [ ] Identidade verificada
- [ ] Dados bancários configurados
- [ ] Credenciais de produção copiadas
- [ ] `.env` configurado com credenciais de produção
- [ ] SSL/HTTPS configurado
- [ ] `APP_BASE_URL` com HTTPS
- [ ] Webhook configurado
- [ ] Webhook secret configurado
- [ ] Teste de pagamento real realizado
- [ ] Monitoramento configurado

---

## 🎉 **RESUMO**

**Desenvolvimento:**
```bash
# 1. Criar aplicação no Mercado Pago
# 2. Copiar credenciais de TESTE
# 3. Editar .env:
MERCADOPAGO_ACCESS_TOKEN=TEST-...
MERCADOPAGO_PUBLIC_KEY=TEST-...
APP_BASE_URL=http://localhost:8080

# 4. Reiniciar aplicação
./docker-start.sh restart

# 5. Testar com cartões de teste
```

**Produção:**
```bash
# 1. Verificar identidade no Mercado Pago
# 2. Configurar dados bancários
# 3. Copiar credenciais de PRODUÇÃO
# 4. Editar .env:
MERCADOPAGO_ACCESS_TOKEN=APP_USR-...
MERCADOPAGO_PUBLIC_KEY=APP_USR-...
MERCADOPAGO_WEBHOOK_SECRET=...
APP_BASE_URL=https://seudominio.com.br

# 5. Configurar SSL/HTTPS
# 6. Configurar webhook
# 7. Fazer deploy
# 8. Testar pagamento real
```

---

**Dúvidas?** Consulte a documentação oficial do Mercado Pago.

