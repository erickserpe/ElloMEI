# 🧪 GUIA DE TESTES - VALIDAÇÕES SCF-MEI

## 🎯 Objetivo

Este guia contém todos os testes que você deve realizar para validar as implementações de validação de cadastro.

---

## 🚀 PASSO 1: Verificar Aplicação Rodando

```bash
# Verificar se a aplicação está rodando
curl -s http://localhost:8080/actuator/health

# Deve retornar: {"status":"UP"}
```

**Se não estiver rodando:**
```bash
cd /home/es_luan/IdeaProjects/SCF-MEI
docker compose up -d
sleep 30
```

---

## 🔐 TESTE 1: Validação de Senha Forte

### ❌ Testes que DEVEM FALHAR

Acesse: http://localhost:8080/registro

#### Teste 1.1: Senha muito curta
- **Username:** `teste1`
- **Email:** `teste1@gmail.com`
- **Password:** `Abc1!`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Menos de 8 caracteres

#### Teste 1.2: Senha sem maiúscula
- **Username:** `teste2`
- **Email:** `teste2@gmail.com`
- **Password:** `abcd1234!`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Falta letra maiúscula

#### Teste 1.3: Senha sem minúscula
- **Username:** `teste3`
- **Email:** `teste3@gmail.com`
- **Password:** `ABCD1234!`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Falta letra minúscula

#### Teste 1.4: Senha sem número
- **Username:** `teste4`
- **Email:** `teste4@gmail.com`
- **Password:** `Abcdefgh!`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Falta número

#### Teste 1.5: Senha sem caractere especial
- **Username:** `teste5`
- **Email:** `teste5@gmail.com`
- **Password:** `Abcd1234`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Falta caractere especial

#### Teste 1.6: Senha comum (123456)
- **Username:** `teste6`
- **Email:** `teste6@gmail.com`
- **Password:** `123456`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Senha muito comum

#### Teste 1.7: Senha comum (password)
- **Username:** `teste7`
- **Email:** `teste7@gmail.com`
- **Password:** `password`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Senha muito comum

#### Teste 1.8: Senha com sequência (123)
- **Username:** `teste8`
- **Email:** `teste8@gmail.com`
- **Password:** `Abc123!@#`
- **Resultado Esperado:** ❌ Erro: "A senha não atende aos requisitos de segurança"
- **Motivo:** Contém sequência numérica 123

### ✅ Testes que DEVEM PASSAR

#### Teste 1.9: Senha forte válida
- **Username:** `teste9`
- **Email:** `teste9@gmail.com`
- **Password:** `M3iS3nh@F0rt3`
- **Resultado Esperado:** ✅ Cadastro realizado com sucesso
- **Verificar:** Email de boas-vindas enviado

#### Teste 1.10: Outra senha forte válida
- **Username:** `teste10`
- **Email:** `teste10@gmail.com`
- **Password:** `S3cur3P@ssw0rd!`
- **Resultado Esperado:** ✅ Cadastro realizado com sucesso
- **Verificar:** Email de boas-vindas enviado

---

## 👤 TESTE 2: Validação de Username Único

### ❌ Testes que DEVEM FALHAR

#### Teste 2.1: Username duplicado
1. **Primeiro cadastro:**
   - Username: `joaosilva`
   - Email: `joao@gmail.com`
   - Password: `S3nh@F0rt3!`
   - Resultado: ✅ Sucesso

2. **Segundo cadastro (duplicado):**
   - Username: `joaosilva` (mesmo username)
   - Email: `joao2@gmail.com` (email diferente)
   - Password: `S3nh@F0rt3!`
   - **Resultado Esperado:** ❌ Erro: "O nome de usuário 'joaosilva' já está em uso. Por favor, escolha outro."

#### Teste 2.2: Username muito curto
- **Username:** `ab`
- **Email:** `teste@gmail.com`
- **Password:** `S3nh@F0rt3!`
- **Resultado Esperado:** ❌ Erro: "O nome de usuário deve ter entre 3 e 50 caracteres"

#### Teste 2.3: Username muito longo
- **Username:** `usuario_muito_longo_que_ultrapassa_cinquenta_caracteres_no_total`
- **Email:** `teste@gmail.com`
- **Password:** `S3nh@F0rt3!`
- **Resultado Esperado:** ❌ Erro: "O nome de usuário deve ter entre 3 e 50 caracteres"

---

## 📧 TESTE 3: Validação de Email Único

### ❌ Testes que DEVEM FALHAR

#### Teste 3.1: Email duplicado
1. **Primeiro cadastro:**
   - Username: `maria`
   - Email: `maria@gmail.com`
   - Password: `S3nh@F0rt3!`
   - Resultado: ✅ Sucesso

2. **Segundo cadastro (duplicado):**
   - Username: `maria2` (username diferente)
   - Email: `maria@gmail.com` (mesmo email)
   - Password: `S3nh@F0rt3!`
   - **Resultado Esperado:** ❌ Erro: "O email 'maria@gmail.com' já está cadastrado. Use outro email ou recupere sua senha."

---

## 🌐 TESTE 4: Validação Real de Email (DNS/MX)

### ❌ Testes que DEVEM FALHAR

#### Teste 4.1: Email com formato inválido
- **Username:** `teste_email1`
- **Email:** `email_invalido_sem_arroba`
- **Password:** `S3nh@F0rt3!`
- **Resultado Esperado:** ❌ Erro: "Formato de email inválido"

#### Teste 4.2: Email com domínio inexistente
- **Username:** `teste_email2`
- **Email:** `usuario@dominioqueNaoExiste12345.com`
- **Password:** `S3nh@F0rt3!`
- **Resultado Esperado:** ❌ Erro: "Email inválido ou inexistente"
- **Motivo:** Domínio não existe (sem registros DNS)

### ✅ Testes que DEVEM PASSAR

#### Teste 4.3: Email Gmail válido
- **Username:** `teste_gmail`
- **Email:** `teste_scfmei@gmail.com`
- **Password:** `S3nh@F0rt3!`
- **Resultado Esperado:** ✅ Cadastro realizado com sucesso
- **Motivo:** Gmail tem registros MX válidos

#### Teste 4.4: Email Outlook válido
- **Username:** `teste_outlook`
- **Email:** `teste_scfmei@outlook.com`
- **Password:** `S3nh@F0rt3!`
- **Resultado Esperado:** ✅ Cadastro realizado com sucesso
- **Motivo:** Outlook tem registros MX válidos

---

## 📨 TESTE 5: Envio de E-mails

### ✅ Teste 5.1: Email de Boas-Vindas

1. **Criar novo usuário:**
   - Username: `novousuario`
   - Email: `serpetabordaerickluan@gmail.com`
   - Password: `S3nh@F0rt3!`

2. **Verificar logs:**
   ```bash
   docker compose logs app --tail=50 | grep "Email de boas-vindas"
   ```
   
3. **Resultado Esperado:**
   - ✅ Log: "✅ Email de boas-vindas enviado para: serpetabordaerickluan@gmail.com"
   - ✅ Email recebido na caixa de entrada (ou spam)

### ✅ Teste 5.2: Email de Recuperação de Senha

1. **Acessar:** http://localhost:8080/recuperar-senha

2. **Preencher:**
   - Email: `serpetabordaerickluan@gmail.com`

3. **Clicar em:** "Enviar Instruções"

4. **Resultado Esperado:**
   - ✅ Mensagem na tela: "✅ Instruções de recuperação enviadas! Verifique seu email: serpetabordaerickluan@gmail.com"
   - ✅ Email recebido com link de recuperação

---

## 🎨 TESTE 6: Interface do Usuário

### ✅ Teste 6.1: Mensagens de Erro Visíveis

1. **Tentar cadastrar com senha fraca:**
   - Username: `teste_ui`
   - Email: `teste@gmail.com`
   - Password: `123456`

2. **Resultado Esperado:**
   - ✅ Alert vermelho no topo: "Por favor, corrija os erros no formulário."
   - ✅ Campo de senha fica vermelho (borda vermelha)
   - ✅ Mensagem abaixo do campo: "A senha não atende aos requisitos de segurança"

### ✅ Teste 6.2: Dicas de Preenchimento

1. **Acessar:** http://localhost:8080/registro

2. **Resultado Esperado:**
   - ✅ Abaixo do campo Username: "Entre 3 e 50 caracteres"
   - ✅ Abaixo do campo Email: "Será usado para login e recuperação de senha"
   - ✅ Abaixo do campo Password: "Mínimo 8 caracteres, com maiúsculas, minúsculas, números e caracteres especiais (!@#$%&*)"

### ✅ Teste 6.3: Mensagem de Sucesso

1. **Cadastrar usuário válido:**
   - Username: `sucesso_teste`
   - Email: `sucesso@gmail.com`
   - Password: `S3nh@F0rt3!`

2. **Resultado Esperado:**
   - ✅ Redirecionado para `/login`
   - ✅ Alert verde: "🎉 Cadastro realizado com sucesso! Verifique seu email para confirmar o cadastro e faça login."

---

## 📊 TESTE 7: Verificar Logs

### Verificar logs da aplicação

```bash
docker compose logs app --tail=100 | grep -E "📝|✅|⚠️|❌"
```

### Logs esperados para cadastro bem-sucedido:

```
📝 Iniciando cadastro de novo usuário: teste9
✅ Username 'teste9' disponível
✅ Email 'teste9@gmail.com' disponível
✅ Usuário 'teste9' cadastrado com sucesso!
✅ Email de boas-vindas enviado para: teste9@gmail.com
```

### Logs esperados para username duplicado:

```
📝 Iniciando cadastro de novo usuário: joaosilva
⚠️ Tentativa de cadastro com username duplicado: joaosilva
```

### Logs esperados para email duplicado:

```
📝 Iniciando cadastro de novo usuário: maria2
✅ Username 'maria2' disponível
⚠️ Tentativa de cadastro com email duplicado: maria@gmail.com
```

---

## ✅ CHECKLIST DE TESTES

Marque cada teste conforme for realizando:

### Validação de Senha
- [ ] Senha muito curta (< 8 caracteres) - DEVE FALHAR
- [ ] Senha sem maiúscula - DEVE FALHAR
- [ ] Senha sem minúscula - DEVE FALHAR
- [ ] Senha sem número - DEVE FALHAR
- [ ] Senha sem caractere especial - DEVE FALHAR
- [ ] Senha comum (123456) - DEVE FALHAR
- [ ] Senha comum (password) - DEVE FALHAR
- [ ] Senha com sequência (123) - DEVE FALHAR
- [ ] Senha forte válida - DEVE PASSAR
- [ ] Outra senha forte válida - DEVE PASSAR

### Validação de Username
- [ ] Username duplicado - DEVE FALHAR
- [ ] Username muito curto (< 3 chars) - DEVE FALHAR
- [ ] Username muito longo (> 50 chars) - DEVE FALHAR
- [ ] Username válido e único - DEVE PASSAR

### Validação de Email
- [ ] Email duplicado - DEVE FALHAR
- [ ] Email com formato inválido - DEVE FALHAR
- [ ] Email com domínio inexistente - DEVE FALHAR
- [ ] Email Gmail válido - DEVE PASSAR
- [ ] Email Outlook válido - DEVE PASSAR

### Envio de E-mails
- [ ] Email de boas-vindas enviado
- [ ] Email de recuperação de senha enviado
- [ ] Mensagem de confirmação exibida na tela

### Interface do Usuário
- [ ] Mensagens de erro visíveis
- [ ] Campos ficam vermelhos quando inválidos
- [ ] Dicas de preenchimento aparecem
- [ ] Mensagem de sucesso após cadastro

### Logs
- [ ] Logs de cadastro bem-sucedido
- [ ] Logs de username duplicado
- [ ] Logs de email duplicado
- [ ] Logs de email enviado

---

## 🐛 TROUBLESHOOTING

### Problema: Aplicação não está rodando

**Solução:**
```bash
cd /home/es_luan/IdeaProjects/SCF-MEI
docker compose down
docker compose build app
docker compose up -d
sleep 30
docker compose logs app --tail=50
```

### Problema: Email não está sendo enviado

**Verificar configuração:**
```bash
docker compose logs app | grep -i "mail"
```

**Verificar variáveis de ambiente:**
```bash
docker compose exec app env | grep MAIL
```

### Problema: Validação não está funcionando

**Verificar se validators foram carregados:**
```bash
docker compose logs app | grep -i "validator"
```

### Problema: Erro 500 ao cadastrar

**Verificar logs de erro:**
```bash
docker compose logs app --tail=100 | grep -A 10 "Exception\|Error"
```

---

## 📞 SUPORTE

Se algum teste falhar:
1. Verifique os logs: `docker compose logs app --tail=100`
2. Verifique se a aplicação está rodando: `curl http://localhost:8080/actuator/health`
3. Consulte o arquivo `VALIDACOES-IMPLEMENTADAS.md` para detalhes da implementação

---

**Boa sorte com os testes! 🚀**

