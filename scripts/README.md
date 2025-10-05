# 🔧 Scripts Utilitários - ElloMEI

Scripts auxiliares para gerenciamento e manutenção do sistema.

---

## 📋 Scripts Disponíveis

### 🐳 Docker

#### `docker-start.sh`
Script principal para gerenciar o ambiente Docker.

**Uso:**
```bash
# Iniciar aplicação
./scripts/docker-start.sh

# Ver status
./scripts/docker-start.sh status

# Ver logs
./scripts/docker-start.sh logs

# Reiniciar
./scripts/docker-start.sh restart

# Parar
./scripts/docker-start.sh stop

# Limpar tudo
./scripts/docker-start.sh clean
```

---

### 💾 Backup e Restore

#### `backup.sh`
Script completo de backup do banco de dados.

**Uso:**
```bash
# Criar backup
./scripts/backup.sh

# Listar backups
./scripts/backup.sh --list

# Restaurar backup
./scripts/backup.sh --restore <arquivo>
```

#### `backup-database.sh`
Script simplificado de backup.

**Uso:**
```bash
./scripts/backup-database.sh
```

#### `restore-database.sh`
Script de restauração de backup.

**Uso:**
```bash
./scripts/restore-database.sh <arquivo-backup.sql>
```

---

### 📧 Email

#### `testar-email.sh`
Script interativo para testar envio de email.

**Uso:**
```bash
./scripts/testar-email.sh
# Siga as instruções na tela
```

#### `testar-email-completo.sh`
Script automatizado de teste de email (com CSRF).

**Uso:**
```bash
./scripts/testar-email-completo.sh
```

**Nota:** Edite o script para alterar o email de teste.

---

### 🔒 SSL/HTTPS

#### `generate-ssl-cert.sh`
Gera certificados SSL auto-assinados para desenvolvimento.

**Uso:**
```bash
./scripts/generate-ssl-cert.sh
```

**Saída:**
- `ssl/keystore.p12` - Keystore PKCS12
- `ssl/certificate.crt` - Certificado
- `ssl/private.key` - Chave privada

---

## 🎯 Exemplos de Uso Comum

### Iniciar o Sistema
```bash
./scripts/docker-start.sh
```

### Fazer Backup Antes de Atualização
```bash
./scripts/backup.sh
```

### Testar Configuração de Email
```bash
./scripts/testar-email.sh
```

### Restaurar Backup Após Problema
```bash
./scripts/backup.sh --list
./scripts/backup.sh --restore backup-2025-10-05-120000.sql
```

---

## ⚠️ Observações

### Permissões
Todos os scripts precisam de permissão de execução:
```bash
chmod +x scripts/*.sh
```

### Dependências
- **Docker** e **Docker Compose** instalados
- **curl** para testes de email
- **mysql-client** para backup/restore (opcional, usa Docker)

---

## 📝 Manutenção

### Adicionar Novo Script

1. Crie o script na pasta `scripts/`
2. Adicione permissão de execução: `chmod +x scripts/seu-script.sh`
3. Documente neste README
4. Adicione comentários no script

### Padrão de Nomenclatura

- Use kebab-case: `nome-do-script.sh`
- Seja descritivo: `backup-database.sh` em vez de `bkp.sh`
- Agrupe por funcionalidade

---

**Última atualização:** 05/10/2025

