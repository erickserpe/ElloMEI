# 🚀 CONFIGURAR EMAIL AGORA - Guia Rápido

Este é um guia rápido para você configurar o email do Google Workspace **AGORA**.

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

**Encontre estas linhas e edite:**

```bash
# ========================================
# EMAIL - Configurações de Envio de Email
# ========================================

# Email do remetente (seu email do Google Workspace)
MAIL_USERNAME=noreply@seudominio.com.br

# Senha de App do Google (16 caracteres, SEM ESPAÇOS)
MAIL_PASSWORD=abcdefghijklmnop

# Nome do remetente (aparece no email)
MAIL_FROM_NAME=SCF-MEI - Sistema de Controle Financeiro

# URL base da aplicação (para links nos emails)
APP_BASE_URL=https://seudominio.com.br
```

**Substitua:**
- `noreply@seudominio.com.br` → Seu email do Google Workspace
- `abcdefghijklmnop` → A senha de app que você copiou (SEM ESPAÇOS!)
- `https://seudominio.com.br` → URL da sua aplicação

**Salvar:**
- Pressione `Ctrl + O` (salvar)
- Pressione `Enter` (confirmar)
- Pressione `Ctrl + X` (sair)

---

### **PASSO 3: Reiniciar a aplicação**

```bash
./docker-start.sh restart
```

Aguarde ~15 segundos para a aplicação iniciar.

---

### **PASSO 4: Testar o envio de email**

#### **Opção 1: Testar recuperação de senha**

1. Abra o navegador: http://localhost:8080/recuperar-senha
2. Digite um email válido (pode ser o seu)
3. Clique em "Enviar"
4. Verifique a caixa de entrada do email

#### **Opção 2: Ver logs**

```bash
docker compose logs app | grep -i "email.*enviado"
```

**Saída esperada:**
```
✅ Email de recuperação de senha enviado para: usuario@exemplo.com
```

---

## ✅ Pronto!

Se você viu o email na caixa de entrada ou viu a mensagem de sucesso nos logs, **está funcionando!** 🎉

---

## 📧 Emails que Serão Enviados Automaticamente

Agora o sistema enviará emails automaticamente em:

1. **Recuperação de Senha** 🔐
   - Quando: Usuário clica em "Esqueci minha senha"
   - Template: Roxo com link para redefinir

2. **Boas-Vindas** 🎉
   - Quando: Novo usuário se registra
   - Template: Roxo com recursos do plano FREE

3. **Pagamento Aprovado** ✅
   - Quando: Mercado Pago confirma pagamento
   - Template: Verde com detalhes da assinatura

4. **Pagamento Pendente** ⏳
   - Quando: Pagamento aguardando confirmação
   - Template: Laranja com instruções

5. **Falha de Pagamento** ❌
   - Quando: Pagamento é recusado
   - Template: Vermelho com motivo e instruções

6. **Upgrade de Plano** 🚀
   - Quando: Usuário faz upgrade para PRO
   - Template: Roxo com recursos PRO

7. **Cancelamento** 😢
   - Quando: Usuário cancela assinatura
   - Template: Cinza com motivo

---

## 🎨 Como Ficam os Emails

Todos os emails têm:

- ✅ Design responsivo (funciona em celular e desktop)
- ✅ Cores modernas com gradientes
- ✅ Botões de ação destacados
- ✅ Informações organizadas em tabelas
- ✅ Footer profissional
- ✅ Suporte a acentos e caracteres especiais

**Exemplo de email de pagamento aprovado:**

```
┌─────────────────────────────────────┐
│   ✅ Pagamento Aprovado!            │  ← Header verde
├─────────────────────────────────────┤
│                                     │
│  Olá, João!                         │
│                                     │
│  🎉 Ótimas notícias! Seu pagamento  │
│  foi aprovado com sucesso.          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Plano:      PRO             │   │
│  │ Valor:      R$ 29,90        │   │
│  │ Pagamento:  Cartão Crédito  │   │
│  │ Próxima:    05/11/2025      │   │
│  └─────────────────────────────┘   │
│                                     │
│  Você já pode aproveitar:           │
│  🚀 Lançamentos ILIMITADOS          │
│  📊 Relatórios avançados            │
│  📈 Gráficos e análises             │
│  ⭐ Suporte prioritário             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Acessar Dashboard         │   │  ← Botão verde
│  └─────────────────────────────┘   │
│                                     │
│  Obrigado por ser PRO! 🎉          │
│                                     │
├─────────────────────────────────────┤
│  © 2024 SCF-MEI                     │  ← Footer
│  Este é um email automático         │
└─────────────────────────────────────┘
```

---

## 🔍 Troubleshooting Rápido

### **Problema: "Authentication failed"**

**Solução:**
1. Verifique se copiou a senha **SEM ESPAÇOS**
2. Verifique se a verificação em 2 etapas está habilitada
3. Gere uma nova senha de app
4. Atualize o `.env`
5. Reinicie: `./docker-start.sh restart`

---

### **Problema: Email não chega**

**Solução:**
1. Verifique a pasta de spam
2. Verifique os logs: `docker compose logs app | grep -i email`
3. Verifique se o `MAIL_USERNAME` está correto
4. Verifique se o `MAIL_PASSWORD` está correto (sem espaços!)

---

### **Problema: Email cai no spam**

**Solução:**
1. Use um email do Google Workspace (não Gmail pessoal)
2. Configure SPF, DKIM e DMARC no seu domínio
3. Adicione o remetente à lista de contatos
4. Marque como "Não é spam"

---

## 📚 Documentação Completa

Se precisar de mais detalhes, consulte:

- **GOOGLE-WORKSPACE-EMAIL-SETUP.md** - Guia completo de configuração
- **EXEMPLO-CONFIGURACAO-EMAIL.md** - Exemplos práticos

---

## 💡 Dicas Importantes

1. ✅ **NUNCA** commite o arquivo `.env` no Git
2. ✅ Use senhas de app diferentes para cada aplicação
3. ✅ Revogue senhas de app não utilizadas
4. ✅ Monitore atividade suspeita na conta
5. ✅ Em produção, use HTTPS
6. ✅ Configure SPF, DKIM e DMARC no seu domínio

---

## 🎯 Exemplo Completo de `.env`

```bash
# ========================================
# EMAIL - Google Workspace
# ========================================

# Seu email do Google Workspace
MAIL_USERNAME=noreply@meudominio.com.br

# Senha de app (16 caracteres, SEM ESPAÇOS)
MAIL_PASSWORD=abcdefghijklmnop

# Nome que aparece no email
MAIL_FROM_NAME=SCF-MEI - Sistema de Controle Financeiro

# URL da sua aplicação
APP_BASE_URL=https://meudominio.com.br
```

---

## 🚀 Próximos Passos

Depois de configurar o email:

1. ✅ Teste todos os tipos de email
2. ✅ Configure SPF, DKIM e DMARC (produção)
3. ✅ Monitore taxa de entrega
4. ✅ Personalize templates se necessário
5. ✅ Configure alertas de falha de envio

---

## 📊 Verificar se Está Funcionando

### **Comando 1: Ver logs de email**

```bash
docker compose logs app | grep -i email
```

### **Comando 2: Ver emails enviados**

```bash
docker compose logs app | grep "Email.*enviado"
```

### **Comando 3: Ver erros de email**

```bash
docker compose logs app | grep "Erro ao enviar email"
```

---

## 🎉 Tudo Pronto!

Agora você tem:

- ✅ Email profissional configurado
- ✅ Templates HTML bonitos
- ✅ Envio automático em 7 situações
- ✅ Integração com Mercado Pago
- ✅ Logs detalhados
- ✅ Documentação completa

**Seu sistema está pronto para produção! 🚀**

---

## 📞 Precisa de Ajuda?

Se tiver algum problema:

1. Verifique os logs: `docker compose logs app | grep -i email`
2. Consulte a documentação: `GOOGLE-WORKSPACE-EMAIL-SETUP.md`
3. Verifique o troubleshooting: `EXEMPLO-CONFIGURACAO-EMAIL.md`

---

**Configuração concluída! 📧✨**

Agora é só configurar suas credenciais e testar!

