# 💳 Guia do Sistema de Pagamentos - ElloMEI

## 📋 Índice
1. [Visão Geral](#visão-geral)
2. [Arquitetura](#arquitetura)
3. [Planos de Assinatura](#planos-de-assinatura)
4. [Fluxo de Pagamento](#fluxo-de-pagamento)
5. [Configuração](#configuração)
6. [Endpoints](#endpoints)
7. [Jobs Agendados](#jobs-agendados)
8. [E-mails](#e-mails)
9. [Testes](#testes)
10. [Deploy em Produção](#deploy-em-produção)

---

## 🎯 Visão Geral

O ElloMEI implementa um sistema completo de assinaturas SaaS com integração ao **Mercado Pago**.

### Funcionalidades Implementadas:
- ✅ Planos FREE e PRO
- ✅ Trial de 7 dias grátis
- ✅ Upgrade/Downgrade de planos
- ✅ Pagamento via Cartão de Crédito, PIX e Boleto
- ✅ Renovação automática de assinaturas
- ✅ Webhooks do Mercado Pago
- ✅ E-mails de notificação
- ✅ Dashboard de uso
- ✅ Controle de limites por plano

---

## 🏗️ Arquitetura

### Componentes Principais:

```
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE APRESENTAÇÃO                  │
├─────────────────────────────────────────────────────────────┤
│  AssinaturaController  │  UsageDashboardController          │
│  MercadoPagoWebhookController                               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE SERVIÇO                       │
├─────────────────────────────────────────────────────────────┤
│  AssinaturaService  │  MercadoPagoService                   │
│  UsageMetricsService │  EmailService                        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE EVENTOS                       │
├─────────────────────────────────────────────────────────────┤
│  PlanUpgradedEvent  │  SubscriptionCancelledEvent          │
│  SubscriptionEventListener                                  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE DADOS                         │
├─────────────────────────────────────────────────────────────┤
│  AssinaturaRepository  │  UsuarioRepository                 │
│  LancamentoRepository                                       │
└─────────────────────────────────────────────────────────────┘
```

### Entidades:

- **Assinatura**: Representa uma assinatura de usuário
- **StatusAssinatura**: TRIAL, ATIVA, SUSPENSA, CANCELADA, EXPIRADA
- **FormaPagamento**: CARTAO_CREDITO, PIX, BOLETO
- **PlanoAssinatura**: FREE, PRO

---

## 💰 Planos de Assinatura

### Plano FREE
- **Custo**: R$ 0,00/mês
- **Lançamentos**: 20 por mês
- **Recursos**: Básicos
- **Suporte**: Padrão

### Plano PRO
- **Custo**: R$ 29,90/mês
- **Lançamentos**: Ilimitados
- **Recursos**: Completos (relatórios PDF, dashboard avançado)
- **Suporte**: Prioritário
- **Trial**: 7 dias grátis

---

## 🔄 Fluxo de Pagamento

### 1. Upgrade FREE → PRO

```
Usuário clica em "Fazer Upgrade"
         ↓
AssinaturaController.processarUpgradeCheckout()
         ↓
MercadoPagoService.criarPreferenciaPagamento()
         ↓
Redirect para Mercado Pago
         ↓
Usuário paga (Cartão/PIX/Boleto)
         ↓
Mercado Pago envia webhook
         ↓
MercadoPagoWebhookController.receberNotificacao()
         ↓
AssinaturaService.processarUpgrade()
         ↓
PlanUpgradedEvent publicado
         ↓
E-mail de confirmação enviado
```

### 2. Cancelamento

```
Usuário clica em "Cancelar Assinatura"
         ↓
AssinaturaController.cancelarAssinatura()
         ↓
AssinaturaService.cancelarAssinatura()
         ↓
Assinatura marcada como CANCELADA
         ↓
Usuário volta para plano FREE
         ↓
SubscriptionCancelledEvent publicado
         ↓
E-mail de confirmação enviado
```

---

## ⚙️ Configuração

### 1. Credenciais do Mercado Pago

Adicione as seguintes variáveis de ambiente:

```bash
# .env ou variáveis de ambiente
MERCADOPAGO_ACCESS_TOKEN=seu-access-token-aqui
MERCADOPAGO_PUBLIC_KEY=sua-public-key-aqui
APP_BASE_URL=https://seu-dominio.com.br
```

### 2. Obter Credenciais

1. Acesse: https://www.mercadopago.com.br/developers
2. Crie uma aplicação
3. Copie o **Access Token** e **Public Key**
4. Use credenciais de **TEST** para desenvolvimento
5. Use credenciais de **PRODUCTION** para produção

### 3. Configurar Webhooks

1. No painel do Mercado Pago, configure a URL do webhook:
   ```
   https://seu-dominio.com.br/api/webhooks/mercadopago
   ```

2. Para testes locais, use **ngrok**:
   ```bash
   ngrok http 8080
   # Use a URL gerada: https://abc123.ngrok.io/api/webhooks/mercadopago
   ```

---

## 🌐 Endpoints

### Páginas Web

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/assinatura` | Gerenciar assinatura |
| GET | `/assinatura/upgrade` | Página de upgrade |
| GET | `/uso` | Dashboard de uso |

### Ações

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/assinatura/upgrade/checkout` | Processar upgrade via checkout |
| POST | `/assinatura/upgrade/pix` | Processar upgrade via PIX |
| POST | `/assinatura/cancelar` | Cancelar assinatura |

### Callbacks

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/assinatura/pagamento/sucesso` | Pagamento aprovado |
| GET | `/assinatura/pagamento/falha` | Pagamento falhou |
| GET | `/assinatura/pagamento/pendente` | Pagamento pendente |

### Webhooks

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/webhooks/mercadopago` | Receber notificações do MP |

### API JSON

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/uso/api/metricas` | Métricas de uso (JSON) |

---

## ⏰ Jobs Agendados

### 1. Renovação de Assinaturas
- **Horário**: Todos os dias às 2h da manhã
- **Método**: `AssinaturaService.renovarAssinaturasVencidas()`
- **Ação**: Processa cobranças de assinaturas que vencem hoje

### 2. Expiração de Assinaturas Suspensas
- **Horário**: Todos os dias às 3h da manhã
- **Método**: `AssinaturaService.expirarAssinaturasSuspensas()`
- **Ação**: Expira assinaturas suspensas há mais de 7 dias

### 3. Expiração de Trials
- **Horário**: Todos os dias às 4h da manhã
- **Método**: `AssinaturaService.expirarTrials()`
- **Ação**: Expira trials que venceram

---

## 📧 E-mails

### E-mails Implementados:

1. **Boas-vindas**: Enviado ao registrar novo usuário
2. **Upgrade Confirmado**: Enviado ao fazer upgrade para PRO
3. **Cancelamento**: Enviado ao cancelar assinatura
4. **Próximo do Limite**: Enviado ao atingir 80% do limite FREE
5. **Limite Excedido**: Enviado ao atingir 100% do limite FREE
6. **Falha de Pagamento**: Enviado quando pagamento falha

### Configuração de E-mail

**IMPORTANTE**: Atualmente os e-mails são apenas **logados no console**.

Para enviar e-mails reais, integre com:
- **AWS SES** (Simple Email Service)
- **SendGrid**
- **Mailgun**
- **SMTP tradicional**

Edite o arquivo `EmailService.java` e implemente o envio real.

---

## 🧪 Testes

### Executar Testes de Pagamento

```bash
mvn test -Dtest=PaymentIntegrationTest
```

### Testes Implementados:

1. ✅ Criação de assinatura trial
2. ✅ Upgrade de plano FREE → PRO
3. ✅ Cancelamento de assinatura
4. ✅ Expiração de trial
5. ✅ Histórico de assinaturas

**Resultado**: 5/5 testes passando ✅

---

## 🚀 Deploy em Produção

### Checklist Pré-Deploy:

- [ ] Configurar credenciais de **PRODUCTION** do Mercado Pago
- [ ] Configurar URL do webhook no painel do Mercado Pago
- [ ] Implementar envio real de e-mails (AWS SES, SendGrid, etc.)
- [ ] Configurar SSL/HTTPS (obrigatório para Mercado Pago)
- [ ] Testar fluxo completo de pagamento em ambiente de staging
- [ ] Configurar monitoramento de logs
- [ ] Configurar alertas de falha de pagamento
- [ ] Revisar valores dos planos
- [ ] Testar webhooks com ngrok antes do deploy
- [ ] Configurar backup automático do banco de dados

### Variáveis de Ambiente Necessárias:

```bash
MERCADOPAGO_ACCESS_TOKEN=APP-PRODUCTION-TOKEN
MERCADOPAGO_PUBLIC_KEY=APP-PRODUCTION-PUBLIC-KEY
APP_BASE_URL=https://scfmei.com.br
SPRING_DATASOURCE_URL=jdbc:mysql://production-db:3306/scfmei
SPRING_DATASOURCE_USERNAME=scfmei_user
SPRING_DATASOURCE_PASSWORD=senha-segura
```

---

## 📊 Métricas e Monitoramento

### Métricas Importantes:

- **MRR** (Monthly Recurring Revenue): Receita recorrente mensal
- **Churn Rate**: Taxa de cancelamento
- **Conversion Rate**: Taxa de conversão FREE → PRO
- **Trial Conversion**: Taxa de conversão de trials
- **ARPU** (Average Revenue Per User): Receita média por usuário

### Logs Importantes:

- Upgrades de plano
- Cancelamentos (com motivo)
- Falhas de pagamento
- Expiração de trials
- Renovações automáticas

---

## 🔒 Segurança

### Boas Práticas Implementadas:

- ✅ Validação de webhooks do Mercado Pago
- ✅ Transações atômicas no banco de dados
- ✅ Isolamento multi-tenant (Hibernate Filters)
- ✅ Logs de auditoria de eventos
- ✅ Proteção contra race conditions

### Recomendações Adicionais:

- Implementar rate limiting nos endpoints de webhook
- Adicionar autenticação nos endpoints de API
- Criptografar dados sensíveis no banco
- Implementar 2FA para usuários PRO
- Monitorar tentativas de fraude

---

## 📞 Suporte

Para dúvidas sobre o sistema de pagamentos:

1. Consulte a documentação do Mercado Pago: https://www.mercadopago.com.br/developers
2. Revise os logs da aplicação
3. Execute os testes de integração
4. Verifique as configurações de webhook

---

**Desenvolvido pela Equipe ElloMEI** 🚀

