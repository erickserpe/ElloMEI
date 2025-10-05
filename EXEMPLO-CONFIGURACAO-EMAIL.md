# 📧 Exemplo de Configuração de Email

Este arquivo mostra exemplos práticos de como configurar o email no arquivo `.env`.

---

## 🎯 Cenário 1: Google Workspace (Recomendado para Produção)

### **Configuração no `.env`:**

```bash
# ========================================
# EMAIL - Google Workspace
# ========================================

# Email do remetente (seu domínio corporativo)
MAIL_USERNAME=noreply@seudominio.com.br

# Senha de App do Google (16 caracteres, SEM ESPAÇOS)
# Gere em: https://myaccount.google.com/apppasswords
MAIL_PASSWORD=abcdefghijklmnop

# Nome do remetente (aparece no email)
MAIL_FROM_NAME=SCF-MEI - Sistema de Controle Financeiro

# URL base da aplicação (para links nos emails)
APP_BASE_URL=https://seudominio.com.br
```

### **Vantagens:**
- ✅ Email profissional com seu domínio
- ✅ Alta taxa de entrega (não cai no spam)
- ✅ Confiável e seguro
- ✅ Suporte do Google

---

## 🎯 Cenário 2: Gmail Pessoal (Desenvolvimento/Testes)

### **Configuração no `.env`:**

```bash
# ========================================
# EMAIL - Gmail Pessoal
# ========================================

# Email do remetente (seu Gmail pessoal)
MAIL_USERNAME=seu.email@gmail.com

# Senha de App do Google (16 caracteres, SEM ESPAÇOS)
# Gere em: https://myaccount.google.com/apppasswords
MAIL_PASSWORD=abcdefghijklmnop

# Nome do remetente
MAIL_FROM_NAME=SCF-MEI - Sistema de Controle Financeiro

# URL base (desenvolvimento)
APP_BASE_URL=http://localhost:8080
```

### **Vantagens:**
- ✅ Fácil de configurar
- ✅ Grátis
- ✅ Bom para testes

### **Desvantagens:**
- ⚠️ Pode cair no spam
- ⚠️ Limite de envios por dia (500 emails)
- ⚠️ Não é profissional

---

## 🔑 Como Gerar Senha de App do Google

### **Passo 1: Habilitar Verificação em 2 Etapas**

1. Acesse: https://myaccount.google.com/security
2. Clique em **"Verificação em duas etapas"**
3. Siga as instruções para habilitar

### **Passo 2: Gerar Senha de App**

1. Acesse: https://myaccount.google.com/apppasswords
2. Faça login
3. Clique em **"Selecionar app"** → **"Outro (nome personalizado)"**
4. Digite: `SCF-MEI Email Service`
5. Clique em **"Gerar"**
6. **COPIE A SENHA** (16 caracteres)
7. Cole no `.env` **SEM ESPAÇOS**

**Exemplo de senha gerada:**
```
abcd efgh ijkl mnop  ← Com espaços (como o Google mostra)
abcdefghijklmnop     ← Sem espaços (como você deve usar)
```

---

## 🧪 Testar Configuração

### **1. Editar o `.env`**

```bash
nano .env
```

Cole as configurações acima.

### **2. Reiniciar a aplicação**

```bash
./docker-start.sh restart
```

### **3. Testar recuperação de senha**

1. Acesse: http://localhost:8080/recuperar-senha
2. Digite um email válido
3. Clique em "Enviar"
4. Verifique a caixa de entrada

### **4. Verificar logs**

```bash
docker compose logs app | grep -i "email.*enviado"
```

**Saída esperada:**
```
✅ Email de recuperação de senha enviado para: usuario@exemplo.com
```

---

## 📊 Tipos de Email Enviados Automaticamente

### **1. Recuperação de Senha** 🔐
- **Quando:** Usuário clica em "Esqueci minha senha"
- **Conteúdo:** Link para redefinir senha (válido por 1 hora)

### **2. Boas-Vindas** 🎉
- **Quando:** Novo usuário se registra
- **Conteúdo:** Mensagem de boas-vindas + recursos do plano FREE

### **3. Pagamento Aprovado** ✅
- **Quando:** Mercado Pago confirma pagamento
- **Conteúdo:** Confirmação + detalhes da assinatura + ID da transação

### **4. Pagamento Pendente** ⏳
- **Quando:** Pagamento aguardando confirmação (Boleto, Pix)
- **Conteúdo:** Status + instruções + tempo estimado

### **5. Falha de Pagamento** ❌
- **Quando:** Pagamento é recusado
- **Conteúdo:** Motivo da falha + instruções para resolver

### **6. Upgrade de Plano** 🚀
- **Quando:** Usuário faz upgrade para PRO
- **Conteúdo:** Confirmação + recursos PRO

### **7. Cancelamento** 😢
- **Quando:** Usuário cancela assinatura
- **Conteúdo:** Confirmação + motivo + como reativar

---

## 🎨 Características dos Emails

Todos os emails têm:

- ✅ **Design Responsivo** - Funciona em desktop e mobile
- ✅ **HTML Profissional** - Código limpo e semântico
- ✅ **Gradientes Modernos** - Visual atraente
- ✅ **Cores por Tipo:**
  - 🟢 Verde: Sucesso (pagamento aprovado)
  - 🟠 Laranja: Aviso (pagamento pendente)
  - 🔴 Vermelho: Erro (falha de pagamento)
  - 🟣 Roxo: Informação (boas-vindas, recuperação)
- ✅ **Botões de Ação** - CTAs claros
- ✅ **Tabelas de Informação** - Dados organizados
- ✅ **Encoding UTF-8** - Suporte a acentos e caracteres especiais

---

## 🔍 Troubleshooting

### **Problema: "Authentication failed"**

**Causa:** Senha de app incorreta ou verificação em 2 etapas não habilitada.

**Solução:**
1. Verifique se a verificação em 2 etapas está habilitada
2. Gere uma nova senha de app
3. Copie a senha **SEM ESPAÇOS**
4. Atualize o `.env`
5. Reinicie: `./docker-start.sh restart`

---

### **Problema: Email não está sendo enviado**

**Solução:**

1. Verifique os logs:
```bash
docker compose logs app | grep -i email
```

2. Verifique se o `MAIL_USERNAME` e `MAIL_PASSWORD` estão corretos no `.env`

3. Teste a conexão SMTP:
```bash
docker compose exec app sh -c 'telnet smtp.gmail.com 587'
```

---

### **Problema: Email cai no spam**

**Solução:**

1. **Use Google Workspace** em vez de Gmail pessoal
2. Configure **SPF, DKIM e DMARC** no seu domínio
3. Adicione o remetente à lista de contatos
4. Peça aos usuários para marcarem como "Não é spam"

---

### **Problema: "Connection timeout"**

**Solução:**

1. Verifique se a porta 587 está aberta no firewall
2. Tente usar a porta 465 (SSL):

Edite `src/main/resources/application.properties`:

```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```

---

## 📚 Recursos Adicionais

- [Google Workspace - Senhas de App](https://support.google.com/accounts/answer/185833)
- [JavaMail API](https://javaee.github.io/javamail/)
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

## 🚀 Exemplo Completo de `.env`

```bash
# ========================================
# SCF-MEI - Variáveis de Ambiente
# ========================================

# ========================================
# MYSQL
# ========================================
MYSQL_DATABASE=scf_mei_db
MYSQL_USER=scf_user
MYSQL_PASSWORD=SuaSenhaForte123!
MYSQL_ROOT_PASSWORD=RootSenhaForte456!
DB_HOST=mysql
MYSQL_PORT=3307

# ========================================
# SPRING BOOT
# ========================================
APP_PORT=8080
APP_BASE_URL=https://seudominio.com.br
SPRING_PROFILES_ACTIVE=prod

# ========================================
# EMAIL - Google Workspace
# ========================================
MAIL_USERNAME=noreply@seudominio.com.br
MAIL_PASSWORD=abcdefghijklmnop
MAIL_FROM_NAME=SCF-MEI - Sistema de Controle Financeiro

# ========================================
# MERCADO PAGO
# ========================================
MERCADOPAGO_ACCESS_TOKEN=APP_USR-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789
MERCADOPAGO_PUBLIC_KEY=APP_USR-abcdef12-3456-7890-abcd-ef1234567890
MERCADOPAGO_WEBHOOK_SECRET=sua-chave-secreta-aqui

# ========================================
# SSL/HTTPS
# ========================================
SSL_ENABLED=true
SSL_PORT=8443
SSL_KEYSTORE_PATH=/path/to/production-cert.p12
SSL_KEYSTORE_PASSWORD=SuaSenhaSSL789!
SSL_KEYSTORE_TYPE=PKCS12
SSL_KEY_ALIAS=scfmei
```

---

**Configuração concluída! 🎉**

Agora você pode enviar emails profissionais usando Google Workspace.

