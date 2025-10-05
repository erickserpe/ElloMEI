# 💳 Mercado Pago - Guia Rápido de Configuração

**Tempo estimado:** 10 minutos  
**Objetivo:** Configurar Mercado Pago para aceitar pagamentos no ElloMEI

---

## 🎯 **O QUE VOCÊ VAI FAZER**

1. ✅ Criar conta no Mercado Pago (se não tiver)
2. ✅ Criar aplicação no painel de desenvolvedores
3. ✅ Copiar credenciais de teste
4. ✅ Configurar no ElloMEI
5. ✅ Testar pagamento

**Resultado:** Sistema funcionando com pagamentos de teste

---

## 📝 **PASSO 1: CRIAR CONTA NO MERCADO PAGO**

### **Se você JÁ TEM conta:**
- Pule para o [Passo 2](#-passo-2-criar-aplicação)

### **Se você NÃO TEM conta:**

1. Acesse: https://www.mercadopago.com.br/
2. Clique em **"Criar conta"**
3. Preencha:
   - Nome completo
   - Email
   - CPF
   - Senha
4. Confirme seu email
5. ✅ Pronto!

---

## 🚀 **PASSO 2: CRIAR APLICAÇÃO**

1. Acesse: https://www.mercadopago.com.br/developers/panel/app
2. Faça login
3. Clique em **"Criar aplicação"**
4. Preencha:
   - **Nome:** `ElloMEI`
   - **Descrição:** `Sistema de Controle Financeiro`
   - **Modelo de integração:** `Pagamentos online`
   - **Produto:** `Checkout Pro`
5. Clique em **"Criar aplicação"**
6. ✅ Aplicação criada!

---

## 🔑 **PASSO 3: COPIAR CREDENCIAIS DE TESTE**

1. Na página da aplicação, clique em **"Credenciais"**
2. Selecione a aba **"Credenciais de teste"**
3. Você verá duas credenciais:

### **Public Key** (Chave Pública)
```
TEST-abcd1234-5678-90ef-ghij-klmnopqrstuv
```
📋 **Copie** esta chave

### **Access Token** (Token de Acesso)
```
TEST-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789
```
📋 **Copie** este token

**⚠️ IMPORTANTE:**
- Credenciais de TESTE começam com `TEST-`
- Use apenas para desenvolvimento
- Não processam pagamentos reais

---

## ⚙️ **PASSO 4: CONFIGURAR NO ELLOMEI**

### **Abrir arquivo `.env`**

```bash
cd /home/es_luan/IdeaProjects/ElloMEI
nano .env
```

### **Localizar seção do Mercado Pago**

Procure por:
```bash
# ========================================
# MERCADO PAGO - Configurações de Pagamento
# ========================================
```

### **Colar suas credenciais**

Substitua os valores:

```bash
# Access Token de TESTE
MERCADOPAGO_ACCESS_TOKEN=TEST-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789

# Public Key de TESTE
MERCADOPAGO_PUBLIC_KEY=TEST-abcd1234-5678-90ef-ghij-klmnopqrstuv

# Webhook Secret (deixe vazio por enquanto)
MERCADOPAGO_WEBHOOK_SECRET=

# Base URL (HTTP em desenvolvimento)
APP_BASE_URL=http://localhost:8080
```

**Cole SUAS credenciais** que você copiou no Passo 3!

### **Salvar arquivo**

- Pressione `Ctrl + O` → `Enter` (salvar)
- Pressione `Ctrl + X` (sair)

---

## 🔄 **PASSO 5: REINICIAR APLICAÇÃO**

```bash
cd /home/es_luan/IdeaProjects/ElloMEI
docker compose down
docker compose up -d
```

Aguarde ~30 segundos para a aplicação iniciar.

---

## 🧪 **PASSO 6: TESTAR PAGAMENTO**

### **1. Acessar sistema**

Abra o navegador: http://localhost:8080

### **2. Fazer login**

Use seu usuário cadastrado.

### **3. Ir para Assinaturas**

Clique em **"Assinaturas"** no menu.

### **4. Assinar Plano PRO**

1. Clique em **"Assinar Plano PRO"**
2. Você será redirecionado para o checkout do Mercado Pago

### **5. Preencher dados de teste**

Use estes dados de **TESTE**:

**Cartão de Crédito (Aprovado):**
```
Número: 5031 4332 1540 6351
CVV: 123
Validade: 11/25
Nome: APRO
CPF: 123.456.789-01
```

**Email:**
```
test_user_123456@testuser.com
```

### **6. Confirmar pagamento**

1. Clique em **"Pagar"**
2. Aguarde processamento
3. Você será redirecionado para página de sucesso

### **7. Verificar assinatura**

1. Volte para **"Assinaturas"**
2. Você deve ver:
   - Status: **ATIVA**
   - Plano: **PRO**
   - Data de início
   - Próxima cobrança

✅ **Funcionou!** Mercado Pago está configurado!

---

## 🎉 **PRONTO!**

Seu ElloMEI agora aceita pagamentos via Mercado Pago!

**O que você configurou:**
- ✅ Conta no Mercado Pago
- ✅ Aplicação criada
- ✅ Credenciais de teste
- ✅ ElloMEI configurado
- ✅ Pagamento testado

---

## 📊 **VERIFICAR PAGAMENTOS**

### **No ElloMEI:**
- Acesse: http://localhost:8080/assinatura/historico
- Veja histórico de pagamentos

### **No Mercado Pago:**
- Acesse: https://www.mercadopago.com.br/activities
- Veja transações de teste

---

## 🔍 **TROUBLESHOOTING**

### **Erro: "Invalid access token"**

**Causa:** Credenciais incorretas

**Solução:**
1. Verifique se copiou corretamente
2. Credenciais devem começar com `TEST-`
3. Não pode ter espaços extras
4. Reinicie a aplicação após alterar

```bash
# Verificar credenciais
grep MERCADOPAGO .env

# Reiniciar
docker compose restart app
```

---

### **Erro: "Payment rejected"**

**Causa:** Dados do cartão incorretos

**Solução:**
Use exatamente estes dados:
```
Número: 5031 4332 1540 6351
CVV: 123
Validade: 11/25
Nome: APRO
```

**Mais cartões de teste:**
https://www.mercadopago.com.br/developers/pt/docs/checkout-api/testing

---

### **Página de checkout não abre**

**Causa:** Aplicação não está rodando

**Solução:**
```bash
# Verificar se está rodando
docker compose ps

# Ver logs
docker compose logs app --tail=50

# Reiniciar
docker compose restart app
```

---

## 🚀 **PRÓXIMOS PASSOS**

### **Para usar em PRODUÇÃO:**

1. **Verificar identidade no Mercado Pago**
   - Enviar documento
   - Configurar dados bancários

2. **Obter credenciais de PRODUÇÃO**
   - Começam com `APP_USR-`
   - Processam pagamentos reais

3. **Configurar SSL/HTTPS**
   - Obrigatório em produção
   - Veja: `docs/setup/ssl-https.md`

4. **Configurar Webhooks**
   - Receber notificações automáticas
   - Veja: `docs/setup/mercadopago.md`

**Documentação completa:** `docs/setup/mercadopago.md`

---

## 📞 **PRECISA DE AJUDA?**

### **Documentação:**
- Guia completo: `docs/setup/mercadopago.md`
- Mercado Pago: https://www.mercadopago.com.br/developers/pt/docs

### **Suporte:**
- FAQ: https://www.mercadopago.com.br/developers/pt/support
- Fórum: https://www.mercadopago.com.br/developers/pt/community

---

## ✅ **CHECKLIST**

- [ ] Conta criada no Mercado Pago
- [ ] Aplicação criada
- [ ] Credenciais de teste copiadas
- [ ] `.env` configurado
- [ ] Aplicação reiniciada
- [ ] Pagamento de teste realizado
- [ ] Assinatura ativa verificada

**Tudo marcado?** 🎉 **Parabéns! Mercado Pago configurado!**

---

**Última atualização:** 05/10/2025  
**Tempo de configuração:** ~10 minutos  
**Dificuldade:** ⭐⭐☆☆☆ (Fácil)

