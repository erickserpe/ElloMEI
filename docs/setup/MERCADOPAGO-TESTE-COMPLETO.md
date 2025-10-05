# 🧪 Mercado Pago - Guia de Teste Completo

**Objetivo:** Testar toda a integração do Mercado Pago no ElloMEI

---

## 📋 **PRÉ-REQUISITOS**

Antes de começar, certifique-se de que:

- [ ] Aplicação está rodando (`docker compose ps`)
- [ ] Credenciais de TESTE configuradas no `.env`
- [ ] Você tem um usuário cadastrado no sistema

---

## 🎯 **CENÁRIOS DE TESTE**

### **1. Teste de Upgrade - Cartão Aprovado** ✅

**Objetivo:** Simular upgrade bem-sucedido com cartão de crédito

**Passos:**

1. **Acessar sistema**
   ```
   http://localhost:8080
   ```

2. **Fazer login**
   - Use seu usuário cadastrado

3. **Ir para Upgrade**
   - Clique em "Assinaturas" no menu
   - Ou acesse: `http://localhost:8080/assinatura/upgrade`

4. **Clicar em "Fazer Upgrade Agora"**
   - Você será redirecionado para o checkout do Mercado Pago

5. **Preencher dados do cartão de TESTE**
   ```
   Número: 5031 4332 1540 6351
   CVV: 123
   Validade: 11/25
   Nome: APRO
   CPF: 123.456.789-01
   Email: test_user@testuser.com
   ```

6. **Confirmar pagamento**
   - Clique em "Pagar"

7. **Verificar redirecionamento**
   - Você deve ser redirecionado para: `/assinatura/pagamento/sucesso`
   - Deve ver mensagem de sucesso

8. **Verificar assinatura**
   - Acesse: `http://localhost:8080/assinatura`
   - Status deve ser: **ATIVA**
   - Plano deve ser: **PRO**

9. **Verificar histórico**
   - Acesse: `http://localhost:8080/assinatura/historico`
   - Deve aparecer o pagamento de R$ 29,90

**Resultado esperado:** ✅ Upgrade realizado com sucesso

---

### **2. Teste de Upgrade - Cartão Recusado** ❌

**Objetivo:** Simular pagamento recusado

**Passos:**

1. **Acessar upgrade**
   ```
   http://localhost:8080/assinatura/upgrade
   ```

2. **Clicar em "Fazer Upgrade Agora"**

3. **Preencher dados do cartão RECUSADO**
   ```
   Número: 5031 4332 1540 6351
   CVV: 123
   Validade: 11/25
   Nome: OTHE
   CPF: 123.456.789-01
   Email: test_user@testuser.com
   ```

4. **Confirmar pagamento**

5. **Verificar redirecionamento**
   - Você deve ser redirecionado para: `/assinatura/pagamento/falha`
   - Deve ver mensagem de erro

6. **Verificar assinatura**
   - Acesse: `http://localhost:8080/assinatura`
   - Status deve continuar: **FREE**

**Resultado esperado:** ❌ Pagamento recusado corretamente

---

### **3. Teste de Webhook - Pagamento Aprovado** 🔔

**Objetivo:** Testar notificação automática do Mercado Pago

**Passos:**

1. **Fazer upgrade com cartão aprovado** (Teste 1)

2. **Aguardar 5-10 segundos**

3. **Verificar logs da aplicação**
   ```bash
   docker compose logs app --tail=100 | grep -i "webhook\|payment"
   ```

4. **Procurar por:**
   ```
   INFO  MercadoPagoWebhookController - Webhook recebido do Mercado Pago
   INFO  MercadoPagoWebhookController - Processando notificação de pagamento
   INFO  MercadoPagoWebhookController - Pagamento aprovado
   INFO  MercadoPagoWebhookController - Assinatura criada via webhook
   INFO  EmailService - Email de pagamento aprovado enviado
   ```

5. **Verificar email**
   - Verifique se recebeu email de "Pagamento Aprovado"

**Resultado esperado:** 🔔 Webhook recebido e processado

---

### **4. Teste de PIX** 💰

**Objetivo:** Gerar QR Code PIX para pagamento

**Passos:**

1. **Fazer requisição para criar PIX**
   ```bash
   curl -X POST http://localhost:8080/assinatura/upgrade/pix \
     -H "Cookie: JSESSIONID=SEU_SESSION_ID" \
     -v
   ```

2. **Copiar QR Code retornado**
   - Será uma string longa começando com `00020126...`

3. **Escanear QR Code**
   - Use app do banco (em ambiente de teste)
   - Ou use: https://qrcode.tec-it.com/pt/

4. **Verificar valor**
   - Deve ser R$ 29,90

**Resultado esperado:** 💰 QR Code PIX gerado

---

### **5. Teste de Cancelamento** 🚫

**Objetivo:** Cancelar assinatura PRO

**Passos:**

1. **Ter assinatura PRO ativa** (Teste 1)

2. **Acessar gerenciamento**
   ```
   http://localhost:8080/assinatura
   ```

3. **Clicar em "Cancelar Assinatura"**

4. **Confirmar cancelamento**

5. **Verificar status**
   - Status deve mudar para: **CANCELADA**
   - Plano deve voltar para: **FREE**

6. **Verificar histórico**
   - Acesse: `http://localhost:8080/assinatura/historico`
   - Deve aparecer registro de cancelamento

**Resultado esperado:** 🚫 Assinatura cancelada

---

### **6. Teste de Limite FREE** 🔒

**Objetivo:** Verificar bloqueio após 20 lançamentos

**Passos:**

1. **Ter plano FREE**

2. **Criar 20 lançamentos**
   - Acesse: `http://localhost:8080/lancamentos/novo/entrada`
   - Crie 20 lançamentos de entrada

3. **Tentar criar 21º lançamento**

4. **Verificar bloqueio**
   - Deve aparecer mensagem: "Limite de lançamentos atingido"
   - Deve sugerir upgrade para PRO

5. **Fazer upgrade para PRO**

6. **Criar mais lançamentos**
   - Agora deve permitir ilimitado

**Resultado esperado:** 🔒 Limite respeitado

---

## 🎭 **CARTÕES DE TESTE**

### **Cartões Aprovados** ✅

```
Número: 5031 4332 1540 6351
Nome: APRO
Resultado: Aprovado
```

```
Número: 4235 6477 2802 5682
Nome: APRO
Resultado: Aprovado
```

### **Cartões Recusados** ❌

```
Número: 5031 4332 1540 6351
Nome: OTHE
Resultado: Recusado (outro erro)
```

```
Número: 5031 4332 1540 6351
Nome: CALL
Resultado: Recusado (chamar autorizador)
```

```
Número: 5031 4332 1540 6351
Nome: FUND
Resultado: Recusado (fundos insuficientes)
```

```
Número: 5031 4332 1540 6351
Nome: SECU
Resultado: Recusado (código de segurança)
```

```
Número: 5031 4332 1540 6351
Nome: EXPI
Resultado: Recusado (data de expiração)
```

### **Cartões Pendentes** ⏳

```
Número: 5031 4332 1540 6351
Nome: CONT
Resultado: Pendente (em análise)
```

**Mais cartões:** https://www.mercadopago.com.br/developers/pt/docs/checkout-api/testing

---

## 📊 **VERIFICAÇÕES**

### **1. Verificar Logs**

```bash
# Ver todos os logs
docker compose logs app --tail=200

# Ver apenas logs do Mercado Pago
docker compose logs app | grep -i "mercado\|payment\|webhook"

# Ver logs em tempo real
docker compose logs app -f
```

### **2. Verificar Banco de Dados**

```bash
# Conectar ao MySQL
docker exec -it ellomei-mysql mysql -u scf_user -p scf_mei_db

# Ver assinaturas
SELECT id, usuario_id, plano, status, data_inicio, data_fim 
FROM assinaturas 
ORDER BY id DESC 
LIMIT 10;

# Ver histórico de pagamentos
SELECT id, usuario_id, valor, status, data_pagamento 
FROM historico_pagamento 
ORDER BY id DESC 
LIMIT 10;

# Sair
exit;
```

### **3. Verificar no Mercado Pago**

1. Acesse: https://www.mercadopago.com.br/activities
2. Veja transações de teste
3. Verifique status dos pagamentos

---

## 🐛 **TROUBLESHOOTING**

### **Erro: "Invalid access token"**

```bash
# Verificar credenciais
grep MERCADOPAGO .env

# Devem começar com TEST-
# Reiniciar aplicação
docker compose restart app
```

### **Checkout não abre**

```bash
# Verificar logs
docker compose logs app --tail=50

# Verificar se aplicação está rodando
docker compose ps

# Reiniciar
docker compose restart app
```

### **Webhook não chega**

```bash
# Em desenvolvimento, webhooks podem não funcionar
# Pois o Mercado Pago precisa de URL pública

# Solução: Testar manualmente
curl -X POST http://localhost:8080/api/webhooks/mercadopago \
  -H "Content-Type: application/json" \
  -d '{
    "type": "payment",
    "data": {
      "id": "123456789"
    }
  }'
```

### **Email não chega**

```bash
# Verificar configuração de email
grep MAIL .env

# Ver logs de email
docker compose logs app | grep -i "email\|mail"

# Email pode estar em spam
```

---

## ✅ **CHECKLIST DE TESTES**

- [ ] Upgrade com cartão aprovado
- [ ] Upgrade com cartão recusado
- [ ] Webhook de pagamento aprovado
- [ ] Geração de QR Code PIX
- [ ] Cancelamento de assinatura
- [ ] Limite de lançamentos FREE
- [ ] Histórico de pagamentos
- [ ] Email de pagamento aprovado
- [ ] Email de pagamento recusado
- [ ] Logs sem erros

**Todos marcados?** 🎉 **Mercado Pago funcionando perfeitamente!**

---

## 📚 **PRÓXIMOS PASSOS**

Após testar tudo em desenvolvimento:

1. **Obter credenciais de PRODUÇÃO**
   - Veja: `docs/setup/MERCADOPAGO-QUICK-START.md`

2. **Configurar SSL/HTTPS**
   - Obrigatório para produção
   - Veja: `docs/setup/ssl-https.md`

3. **Configurar Webhooks**
   - URL pública necessária
   - Veja: `docs/setup/mercadopago.md`

4. **Fazer deploy**
   - Veja: `docs/security/PRODUCTION-SECURITY.md`

---

**Última atualização:** 05/10/2025  
**Tempo de teste:** ~30 minutos  
**Dificuldade:** ⭐⭐⭐☆☆ (Médio)

