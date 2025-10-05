# 🔒 SSL/HTTPS - CONFIGURAÇÃO COMPLETA

Este documento explica como configurar SSL/HTTPS no ElloMEI para desenvolvimento e produção.

---

## 📋 **ÍNDICE**

1. [Por que usar HTTPS?](#-por-que-usar-https)
2. [Desenvolvimento - Certificado Auto-Assinado](#-desenvolvimento---certificado-auto-assinado)
3. [Produção - Let's Encrypt](#-produção---lets-encrypt)
4. [Configuração](#-configuração)
5. [Testes](#-testes)
6. [Troubleshooting](#-troubleshooting)

---

## 🤔 **POR QUE USAR HTTPS?**

### **1. Segurança** 🔒
- ✅ Criptografa dados entre cliente e servidor
- ✅ Protege senhas, dados pessoais e financeiros
- ✅ Previne ataques Man-in-the-Middle (MITM)
- ✅ Previne interceptação de cookies de sessão

### **2. Compliance** 📋
- ✅ **PCI DSS**: Obrigatório para processar pagamentos
- ✅ **LGPD**: Proteção de dados pessoais
- ✅ **Mercado Pago**: Exige HTTPS para webhooks em produção

### **3. SEO e Confiança** 🌐
- ✅ Google prioriza sites HTTPS no ranking
- ✅ Navegadores marcam HTTP como "Não seguro"
- ✅ Usuários confiam mais em sites com cadeado verde

### **4. Funcionalidades Modernas** ⚡
- ✅ Service Workers (PWA) exigem HTTPS
- ✅ Geolocalização exige HTTPS
- ✅ Notificações Push exigem HTTPS
- ✅ HTTP/2 exige HTTPS

---

## 🛠️ **DESENVOLVIMENTO - CERTIFICADO AUTO-ASSINADO**

Para desenvolvimento local, use um certificado auto-assinado.

### **PASSO 1: Gerar Certificado**

```bash
# Executar script de geração
./generate-ssl-cert.sh
```

**O que o script faz:**
1. Gera um keystore PKCS12 (`keystore.p12`)
2. Cria um certificado auto-assinado válido por 365 dias
3. Configura SAN (Subject Alternative Names) para localhost

**Saída esperada:**
```
🔐 Gerando Certificado SSL Auto-Assinado para ElloMEI
======================================================

📝 Configurações:
   - Arquivo: keystore.p12
   - Senha: scfmei2025
   - Alias: scfmei
   - Validade: 365 dias
   - Domínio: localhost

🔨 Gerando certificado...

✅ Certificado gerado com sucesso!

📁 Arquivo gerado: keystore.p12
```

---

### **PASSO 2: Configurar .env**

Edite o arquivo `.env`:

```bash
# Habilitar SSL
SSL_ENABLED=true

# Porta HTTPS
SSL_PORT=8443

# Caminho do keystore
SSL_KEYSTORE_PATH=keystore.p12

# Senha do keystore
SSL_KEYSTORE_PASSWORD=scfmei2025

# Tipo do keystore
SSL_KEYSTORE_TYPE=PKCS12

# Alias da chave
SSL_KEY_ALIAS=scfmei
```

---

### **PASSO 3: Reiniciar Aplicação**

```bash
./docker-start.sh restart
```

---

### **PASSO 4: Acessar via HTTPS**

Abra o navegador em:
```
https://localhost:8443
```

**⚠️ AVISO DE SEGURANÇA:**
O navegador mostrará um aviso porque o certificado é auto-assinado.

**Como proceder:**
- **Chrome**: Clique em "Avançado" → "Prosseguir para localhost (não seguro)"
- **Firefox**: Clique em "Avançado" → "Aceitar o risco e continuar"
- **Edge**: Clique em "Avançado" → "Continuar para localhost (não seguro)"

**Isso é normal em desenvolvimento!** ✅

---

## 🌐 **PRODUÇÃO - LET'S ENCRYPT**

Para produção, use um certificado de uma CA confiável (Let's Encrypt é gratuito).

### **OPÇÃO 1: Nginx Reverse Proxy (Recomendado)**

Use Nginx como proxy reverso para gerenciar SSL.

#### **1. Instalar Certbot**

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install certbot python3-certbot-nginx

# CentOS/RHEL
sudo yum install certbot python3-certbot-nginx
```

#### **2. Obter Certificado**

```bash
# Substituir seudominio.com.br pelo seu domínio real
sudo certbot --nginx -d seudominio.com.br -d www.seudominio.com.br
```

**Certbot irá:**
1. Validar que você é dono do domínio
2. Gerar certificado SSL
3. Configurar Nginx automaticamente
4. Configurar renovação automática

#### **3. Configurar Nginx**

Edite `/etc/nginx/sites-available/ellomei`:

```nginx
server {
    listen 80;
    server_name seudominio.com.br www.seudominio.com.br;
    
    # Redirecionar HTTP para HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name seudominio.com.br www.seudominio.com.br;
    
    # Certificados SSL (gerenciados pelo Certbot)
    ssl_certificate /etc/letsencrypt/live/seudominio.com.br/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/seudominio.com.br/privkey.pem;
    
    # Configurações SSL modernas
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-GCM-SHA256';
    ssl_prefer_server_ciphers on;
    
    # HSTS (força HTTPS por 1 ano)
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # Outros headers de segurança
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Proxy para aplicação Spring Boot
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### **4. Ativar Configuração**

```bash
# Criar link simbólico
sudo ln -s /etc/nginx/sites-available/ellomei /etc/nginx/sites-enabled/

# Testar configuração
sudo nginx -t

# Reiniciar Nginx
sudo systemctl restart nginx
```

#### **5. Renovação Automática**

Certbot configura renovação automática via cron. Testar:

```bash
# Testar renovação (dry-run)
sudo certbot renew --dry-run
```

---

### **OPÇÃO 2: SSL Direto no Spring Boot**

Se não quiser usar Nginx, configure SSL diretamente no Spring Boot.

#### **1. Obter Certificado**

```bash
# Obter certificado com Certbot (standalone)
sudo certbot certonly --standalone -d seudominio.com.br
```

#### **2. Converter para PKCS12**

Let's Encrypt gera certificados em formato PEM. Converter para PKCS12:

```bash
# Converter certificado
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/seudominio.com.br/fullchain.pem \
  -inkey /etc/letsencrypt/live/seudominio.com.br/privkey.pem \
  -out /app/ssl/certificate.p12 \
  -name scfmei \
  -passout pass:SUA_SENHA_FORTE_AQUI
```

#### **3. Configurar .env**

```bash
SSL_ENABLED=true
SSL_PORT=8443
SSL_KEYSTORE_PATH=/app/ssl/certificate.p12
SSL_KEYSTORE_PASSWORD=SUA_SENHA_FORTE_AQUI
SSL_KEYSTORE_TYPE=PKCS12
SSL_KEY_ALIAS=scfmei
```

#### **4. Renovação Automática**

Criar script de renovação `/usr/local/bin/renew-ssl.sh`:

```bash
#!/bin/bash
certbot renew --quiet
openssl pkcs12 -export \
  -in /etc/letsencrypt/live/seudominio.com.br/fullchain.pem \
  -inkey /etc/letsencrypt/live/seudominio.com.br/privkey.pem \
  -out /app/ssl/certificate.p12 \
  -name scfmei \
  -passout pass:SUA_SENHA_FORTE_AQUI
systemctl restart ellomei
```

Adicionar ao cron:

```bash
# Executar todo dia às 3h da manhã
0 3 * * * /usr/local/bin/renew-ssl.sh
```

---

## ⚙️ **CONFIGURAÇÃO**

### **Variáveis de Ambiente (.env)**

```bash
# Habilitar SSL (true/false)
SSL_ENABLED=true

# Porta HTTPS (padrão: 8443)
SSL_PORT=8443

# Caminho do keystore
# Dev: keystore.p12
# Prod: /app/ssl/certificate.p12
SSL_KEYSTORE_PATH=keystore.p12

# Senha do keystore
SSL_KEYSTORE_PASSWORD=scfmei2025

# Tipo do keystore (PKCS12 ou JKS)
SSL_KEYSTORE_TYPE=PKCS12

# Alias da chave
SSL_KEY_ALIAS=scfmei
```

### **Atualizar APP_BASE_URL**

```bash
# Desenvolvimento
APP_BASE_URL=https://localhost:8443

# Produção
APP_BASE_URL=https://seudominio.com.br
```

---

## 🧪 **TESTES**

### **Testar HTTPS Localmente**

```bash
# Verificar se porta 8443 está aberta
curl -k https://localhost:8443

# Verificar certificado
openssl s_client -connect localhost:8443 -showcerts
```

### **Testar Redirecionamento HTTP → HTTPS**

```bash
# Deve redirecionar para HTTPS
curl -I http://localhost:8080
```

### **Testar Headers de Segurança**

```bash
# Verificar HSTS e outros headers
curl -I https://localhost:8443
```

**Headers esperados:**
```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
```

---

## 🔧 **TROUBLESHOOTING**

### **Erro: "Keystore not found"**

```
Caused by: java.io.FileNotFoundException: keystore.p12
```

**Solução:**
```bash
# Gerar keystore
./generate-ssl-cert.sh

# Verificar se existe
ls -la keystore.p12
```

---

### **Erro: "Incorrect password"**

```
Caused by: java.io.IOException: keystore password was incorrect
```

**Solução:**
```bash
# Verificar senha no .env
grep SSL_KEYSTORE_PASSWORD .env

# Deve ser: scfmei2025 (ou a senha que você definiu)
```

---

### **Navegador: "Sua conexão não é particular"**

**Causa:** Certificado auto-assinado (desenvolvimento).

**Solução:** Clique em "Avançado" → "Prosseguir" (seguro em dev).

---

### **Mercado Pago: "Webhook URL must be HTTPS"**

**Causa:** Mercado Pago exige HTTPS em produção.

**Solução:**
1. Configure SSL conforme este guia
2. Atualize `APP_BASE_URL` para HTTPS
3. Reconfigure webhook no painel do Mercado Pago

---

## ✅ **CHECKLIST DE PRODUÇÃO**

Antes de fazer deploy em produção:

- [ ] Certificado SSL de CA confiável (Let's Encrypt)
- [ ] `SSL_ENABLED=true` no .env
- [ ] `APP_BASE_URL` com HTTPS
- [ ] HSTS habilitado
- [ ] Redirecionamento HTTP → HTTPS
- [ ] Renovação automática configurada
- [ ] Firewall permite porta 443
- [ ] Testar em https://www.ssllabs.com/ssltest/

---

## 🎉 **RESUMO**

**Desenvolvimento:**
```bash
./generate-ssl-cert.sh
# Editar .env: SSL_ENABLED=true
./docker-start.sh restart
# Acessar: https://localhost:8443
```

**Produção (Nginx):**
```bash
sudo certbot --nginx -d seudominio.com.br
# Configurar Nginx como proxy reverso
# Acessar: https://seudominio.com.br
```

**Produção (Spring Boot direto):**
```bash
sudo certbot certonly --standalone -d seudominio.com.br
# Converter para PKCS12
# Configurar .env com caminho do certificado
# Acessar: https://seudominio.com.br:8443
```

---

**Dúvidas?** Consulte:
- Let's Encrypt: https://letsencrypt.org/
- SSL Labs: https://www.ssllabs.com/ssltest/

