# 🔐 Guia de Segurança para Produção - ElloMEI

**Data:** 05/10/2025  
**Objetivo:** Configurar o ElloMEI de forma segura em ambiente de produção

---

## 📋 **CHECKLIST PRÉ-DEPLOY**

Antes de fazer deploy em produção, verifique:

- [ ] Todas as senhas foram alteradas
- [ ] Tokens de API foram gerados para produção
- [ ] SSL/HTTPS está configurado
- [ ] Firewall está configurado
- [ ] Backups automáticos estão configurados
- [ ] Monitoramento está ativo
- [ ] Logs estão sendo coletados
- [ ] Variáveis de ambiente estão configuradas

---

## 🔑 **1. CREDENCIAIS DE PRODUÇÃO**

### **MySQL**

```bash
# Gerar senha forte (Linux/Mac)
openssl rand -base64 32

# Ou use um gerador online confiável
# https://passwordsgenerator.net/
```

**Configurar no `.env` de produção:**

```bash
MYSQL_DATABASE=ellomei_prod
MYSQL_USER=ellomei_user
MYSQL_PASSWORD=SuaSenhaForteDe32Caracteres!@#$
MYSQL_ROOT_PASSWORD=OutraSenhaForteDe32Caracteres!@#$
```

**Recomendações:**
- ✅ Mínimo 16 caracteres (recomendado: 32)
- ✅ Letras maiúsculas e minúsculas
- ✅ Números
- ✅ Caracteres especiais
- ❌ Não use palavras do dicionário
- ❌ Não use informações pessoais

---

### **Email (Google Workspace)**

**Passo 1: Criar conta de serviço**

1. Crie um email específico: `noreply@ellomei.com`
2. Não use seu email pessoal em produção

**Passo 2: Gerar Senha de App**

1. Acesse: https://myaccount.google.com/apppasswords
2. Selecione "Outro (nome personalizado)"
3. Digite: "ElloMEI Production"
4. Copie a senha de 16 caracteres

**Configurar no `.env` de produção:**

```bash
MAIL_USERNAME=noreply@ellomei.com
MAIL_PASSWORD=abcdefghijklmnop
MAIL_FROM_NAME=Ello MEI - Sistema de Controle Financeiro
```

---

### **Mercado Pago**

**Passo 1: Criar aplicação de produção**

1. Acesse: https://www.mercadopago.com.br/developers/panel/app
2. Crie uma nova aplicação
3. Ative o modo PRODUÇÃO

**Passo 2: Obter credenciais de produção**

1. Copie o **Access Token** (começa com `APP_USR-`)
2. Copie a **Public Key** (começa com `APP_USR-`)

**Configurar no `.env` de produção:**

```bash
MERCADOPAGO_ACCESS_TOKEN=APP_USR-1234567890123456-123456-abcdef1234567890abcdef1234567890-123456789
MERCADOPAGO_PUBLIC_KEY=APP_USR-abcdef12-3456-7890-abcd-ef1234567890
MERCADOPAGO_WEBHOOK_SECRET=sua_chave_secreta_webhook
```

---

### **SSL/HTTPS**

**Opção 1: Let's Encrypt (Recomendado - Grátis)**

```bash
# Instalar Certbot
sudo apt-get install certbot

# Gerar certificado
sudo certbot certonly --standalone -d ellomei.com -d www.ellomei.com

# Converter para PKCS12 (formato Java)
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/ellomei.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/ellomei.com/privkey.pem \
  -out keystore.p12 \
  -name ellomei \
  -passout pass:SuaSenhaDoKeystore
```

**Opção 2: Certificado Comercial**

Compre de uma CA confiável (DigiCert, GlobalSign, etc.)

**Configurar no `.env` de produção:**

```bash
SSL_ENABLED=true
SSL_KEYSTORE_PATH=/path/to/keystore.p12
SSL_KEYSTORE_PASSWORD=SuaSenhaDoKeystore
SSL_KEYSTORE_TYPE=PKCS12
SSL_KEY_ALIAS=ellomei
```

---

## 🛡️ **2. CONFIGURAÇÕES DE SEGURANÇA**

### **Firewall**

```bash
# Permitir apenas portas necessárias
sudo ufw allow 80/tcp    # HTTP (redirecionar para HTTPS)
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 22/tcp    # SSH (apenas de IPs confiáveis)

# Bloquear porta do MySQL externamente
sudo ufw deny 3306/tcp

# Ativar firewall
sudo ufw enable
```

---

### **Docker em Produção**

**Arquivo: `docker-compose.prod.yml`**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - ellomei-network
    # NÃO expor porta 3306 externamente
    # ports:
    #   - "3306:3306"
    restart: always

  app:
    build: .
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      # Todas as outras variáveis do .env
    ports:
      - "443:8443"  # HTTPS
      - "80:8080"   # HTTP (redirecionar para HTTPS)
    depends_on:
      - mysql
    networks:
      - ellomei-network
    restart: always

networks:
  ellomei-network:
    driver: bridge

volumes:
  mysql_data:
```

---

### **Variáveis de Ambiente em Produção**

**Opção 1: Arquivo `.env` no servidor**

```bash
# No servidor de produção
cd /opt/ellomei
nano .env

# Cole as configurações de produção
# NUNCA commite este arquivo no Git!
```

**Opção 2: Secrets do Docker Swarm**

```bash
# Criar secrets
echo "senha_mysql" | docker secret create mysql_password -
echo "senha_email" | docker secret create mail_password -

# Usar no docker-compose
services:
  app:
    secrets:
      - mysql_password
      - mail_password
```

**Opção 3: AWS Secrets Manager / Azure Key Vault**

Para ambientes cloud, use serviços de gerenciamento de secrets.

---

## 📊 **3. MONITORAMENTO E LOGS**

### **Logs de Segurança**

```bash
# Configurar no application-prod.properties
logging.level.org.springframework.security=INFO
logging.level.br.com.ellomei=INFO
logging.file.name=/var/log/ellomei/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

---

### **Alertas**

Configure alertas para:
- ✅ Tentativas de login falhadas
- ✅ Erros de pagamento
- ✅ Uso de CPU/memória alto
- ✅ Disco cheio
- ✅ Certificado SSL expirando

---

## 🔄 **4. BACKUPS**

### **Backup Automático do Banco**

```bash
# Crontab para backup diário às 2h da manhã
0 2 * * * /opt/ellomei/scripts/backup-database.sh
```

**Configurar no `.env`:**

```bash
BACKUP_DIR=/opt/ellomei/backups
BACKUP_RETENTION_DAYS=30
```

---

### **Backup de Uploads**

```bash
# Backup de arquivos enviados
rsync -avz /opt/ellomei/uploads/ /backup/ellomei/uploads/
```

---

## 🚀 **5. DEPLOY SEGURO**

### **Checklist de Deploy**

1. **Antes do Deploy:**
   - [ ] Código revisado
   - [ ] Testes passando
   - [ ] Vulnerabilidades verificadas
   - [ ] Dependências atualizadas

2. **Durante o Deploy:**
   - [ ] Backup do banco de dados
   - [ ] Backup dos arquivos
   - [ ] Deploy em horário de baixo tráfego
   - [ ] Monitoramento ativo

3. **Após o Deploy:**
   - [ ] Verificar logs
   - [ ] Testar funcionalidades críticas
   - [ ] Verificar métricas
   - [ ] Confirmar backups

---

## 🔍 **6. AUDITORIA DE SEGURANÇA**

### **Verificações Mensais**

```bash
# 1. Verificar atualizações de segurança
docker pull mysql:8.0
docker pull openjdk:17-jdk-slim

# 2. Verificar logs de acesso
tail -f /var/log/ellomei/application.log | grep "WARN\|ERROR"

# 3. Verificar uso de recursos
docker stats

# 4. Verificar certificado SSL
openssl s_client -connect ellomei.com:443 -servername ellomei.com
```

---

### **Rotação de Credenciais**

**A cada 90 dias:**
- [ ] Rotacionar senha do MySQL
- [ ] Rotacionar senha de email
- [ ] Rotacionar tokens do Mercado Pago
- [ ] Rotacionar senha do keystore SSL

---

## 🆘 **7. RESPOSTA A INCIDENTES**

### **Se Credenciais Forem Expostas:**

1. **Imediato (< 1 hora):**
   - Rotacionar TODAS as credenciais
   - Verificar logs de acesso
   - Bloquear IPs suspeitos

2. **Curto Prazo (< 24 horas):**
   - Investigar extensão do vazamento
   - Notificar usuários afetados
   - Documentar incidente

3. **Longo Prazo (< 1 semana):**
   - Implementar melhorias de segurança
   - Revisar processos
   - Treinar equipe

---

## ✅ **CONCLUSÃO**

Segurança é um processo contínuo. Revise este guia regularmente e mantenha-se atualizado sobre melhores práticas.

**Lembre-se:**
- ✅ Nunca commite credenciais no Git
- ✅ Use senhas fortes e únicas
- ✅ Mantenha sistemas atualizados
- ✅ Monitore constantemente
- ✅ Faça backups regulares

---

**Última atualização:** 05/10/2025  
**Responsável:** Augment Agent  
**Status:** ✅ Pronto para produção

