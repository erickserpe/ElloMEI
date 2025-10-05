# 💾 BACKUP AUTOMÁTICO DO BANCO DE DADOS

Este documento explica como fazer backup e restore do banco de dados MySQL do ElloMEI.

---

## 📋 **ÍNDICE**

1. [Visão Geral](#-visão-geral)
2. [Backup Manual](#-backup-manual)
3. [Backup Automático](#-backup-automático)
4. [Restore](#-restore)
5. [Boas Práticas](#-boas-práticas)
6. [Troubleshooting](#-troubleshooting)

---

## 🤔 **VISÃO GERAL**

O ElloMEI inclui scripts para backup e restore automático do MySQL:

**Recursos:**
- ✅ Backup completo do banco de dados
- ✅ Compressão automática (gzip)
- ✅ Rotação de backups antigos
- ✅ Restore simples
- ✅ Agendamento via cron

**Localização dos backups:**
```
./backups/
├── ellomei_backup_20250104_020000.sql.gz
├── ellomei_backup_20250103_020000.sql.gz
└── ellomei_backup_20250102_020000.sql.gz
```

---

## 💾 **BACKUP MANUAL**

### **PASSO 1: Executar Script de Backup**

```bash
./backup-database.sh
```

**Saída esperada:**
```
[2025-01-04 14:30:00] 🗄️  Iniciando backup do banco de dados ElloMEI
================================================
[2025-01-04 14:30:01] 💾 Criando backup do banco de dados...
   Database: ellomei_db
   Container: ellomei-mysql
   Arquivo: ellomei_backup_20250104_143000.sql.gz
[2025-01-04 14:30:05] ✅ Backup criado com sucesso!
   Tamanho: 2.3M
   Localização: ./backups/ellomei_backup_20250104_143000.sql.gz

[2025-01-04 14:30:05] 🔄 Executando rotação de backups...
   Retenção: 7 dias
[2025-01-04 14:30:05] ℹ️  Nenhum backup antigo para remover
   Backups mantidos: 3

[2025-01-04 14:30:05] 📋 Backups disponíveis:
================================================
   ellomei_backup_20250104_143000.sql.gz (2.3M) - 2025-01-04 14:30:00
   ellomei_backup_20250103_020000.sql.gz (2.1M) - 2025-01-03 02:00:00
   ellomei_backup_20250102_020000.sql.gz (2.0M) - 2025-01-02 02:00:00

================================================
✅ BACKUP CONCLUÍDO COM SUCESSO!
================================================
```

---

### **PASSO 2: Verificar Backup**

```bash
# Listar backups
ls -lh backups/

# Ver conteúdo do backup (sem descompactar)
gunzip -c backups/ellomei_backup_20250104_143000.sql.gz | head -20
```

---

## ⏰ **BACKUP AUTOMÁTICO**

### **OPÇÃO 1: Cron (Linux/Mac)**

#### **1. Editar crontab:**

```bash
crontab -e
```

#### **2. Adicionar linha:**

```bash
# Backup diário às 2h da manhã
0 2 * * * cd /caminho/para/ElloMEI && ./backup-database.sh >> /var/log/ellomei-backup.log 2>&1
```

**Exemplos de agendamento:**

```bash
# Todo dia às 2h da manhã
0 2 * * * /caminho/para/backup-database.sh

# A cada 6 horas
0 */6 * * * /caminho/para/backup-database.sh

# Todo domingo às 3h da manhã
0 3 * * 0 /caminho/para/backup-database.sh

# Todo dia útil às 23h
0 23 * * 1-5 /caminho/para/backup-database.sh
```

#### **3. Verificar cron:**

```bash
# Listar tarefas agendadas
crontab -l

# Ver logs do cron
tail -f /var/log/ellomei-backup.log
```

---

### **OPÇÃO 2: Systemd Timer (Linux)**

#### **1. Criar service:**

Criar arquivo `/etc/systemd/system/ellomei-backup.service`:

```ini
[Unit]
Description=ElloMEI Database Backup
After=docker.service

[Service]
Type=oneshot
User=seu-usuario
WorkingDirectory=/caminho/para/ElloMEI
ExecStart=/caminho/para/ElloMEI/backup-database.sh
StandardOutput=journal
StandardError=journal
```

#### **2. Criar timer:**

Criar arquivo `/etc/systemd/system/ellomei-backup.timer`:

```ini
[Unit]
Description=ElloMEI Database Backup Timer
Requires=ellomei-backup.service

[Timer]
OnCalendar=daily
OnCalendar=02:00
Persistent=true

[Install]
WantedBy=timers.target
```

#### **3. Ativar timer:**

```bash
# Recarregar systemd
sudo systemctl daemon-reload

# Ativar timer
sudo systemctl enable ellomei-backup.timer

# Iniciar timer
sudo systemctl start ellomei-backup.timer

# Verificar status
sudo systemctl status ellomei-backup.timer

# Ver próxima execução
systemctl list-timers ellomei-backup.timer
```

---

### **OPÇÃO 3: Docker Compose (Agendamento Interno)**

Adicionar serviço de backup ao `docker-compose.yml`:

```yaml
services:
  backup:
    image: alpine:latest
    container_name: ellomei-backup
    restart: unless-stopped
    volumes:
      - ./backups:/backups
      - ./backup-database.sh:/backup-database.sh:ro
      - ./.env:/app/.env:ro
    command: >
      sh -c "apk add --no-cache mysql-client &&
             while true; do
               /backup-database.sh;
               sleep 86400;
             done"
    depends_on:
      - mysql
    networks:
      - ellomei-network
```

---

## 🔄 **RESTORE**

### **PASSO 1: Listar Backups Disponíveis**

```bash
./restore-database.sh
```

**Saída:**
```
[2025-01-04 15:00:00] 🔄 Iniciando restore do banco de dados ElloMEI
================================================
[2025-01-04 15:00:00] 📋 Backups disponíveis em ./backups:
================================================
   1. ellomei_backup_20250104_143000.sql.gz (2.3M) - 2025-01-04 14:30:00
   2. ellomei_backup_20250103_020000.sql.gz (2.1M) - 2025-01-03 02:00:00
   3. ellomei_backup_20250102_020000.sql.gz (2.0M) - 2025-01-02 02:00:00
================================================

Uso: ./restore-database.sh <arquivo_backup.sql.gz>
Exemplo: ./restore-database.sh backups/ellomei_backup_20250104_143000.sql.gz
```

---

### **PASSO 2: Executar Restore**

```bash
./restore-database.sh backups/ellomei_backup_20250104_143000.sql.gz
```

**Saída:**
```
[2025-01-04 15:01:00] 🔄 Iniciando restore do banco de dados ElloMEI
================================================

⚠️  ATENÇÃO: Esta operação irá SOBRESCREVER o banco de dados atual!

   Arquivo: ellomei_backup_20250104_143000.sql.gz
   Tamanho: 2.3M
   Database: ellomei_db
   Container: ellomei-mysql

   Deseja continuar? (digite 'SIM' para confirmar): SIM

[2025-01-04 15:01:05] 🔄 Restaurando backup...
[2025-01-04 15:01:15] ✅ Restore concluído com sucesso!

[2025-01-04 15:01:15] 🔍 Verificando restore...
[2025-01-04 15:01:16] ✅ Verificação OK: 11 tabela(s) encontrada(s)

================================================
✅ RESTORE CONCLUÍDO COM SUCESSO!
================================================
   Arquivo: ellomei_backup_20250104_143000.sql.gz
   Database: ellomei_db
   Tabelas: 11
================================================

⚠️  IMPORTANTE: Reinicie a aplicação para garantir consistência
   ./docker-start.sh restart
```

---

### **PASSO 3: Reiniciar Aplicação**

```bash
./docker-start.sh restart
```

---

## 📚 **BOAS PRÁTICAS**

### **1. Retenção de Backups**

Configure retenção adequada no `.env`:

```bash
# Desenvolvimento: 7 dias
BACKUP_RETENTION_DAYS=7

# Produção: 30 dias
BACKUP_RETENTION_DAYS=30
```

---

### **2. Backup Offsite**

Envie backups para armazenamento externo:

```bash
# AWS S3
aws s3 sync ./backups/ s3://meu-bucket/ellomei-backups/

# Google Cloud Storage
gsutil rsync -r ./backups/ gs://meu-bucket/ellomei-backups/

# Rsync para servidor remoto
rsync -avz ./backups/ usuario@servidor:/backups/ellomei/
```

---

### **3. Testar Restore Regularmente**

```bash
# Criar backup de teste
./backup-database.sh

# Restaurar em ambiente de teste
./restore-database.sh backups/ellomei_backup_XXXXXXXX_XXXXXX.sql.gz

# Verificar se aplicação funciona
curl http://localhost:8080/actuator/health
```

---

### **4. Monitorar Backups**

Criar script de monitoramento:

```bash
#!/bin/bash
# check-backup.sh

BACKUP_DIR="./backups"
MAX_AGE_HOURS=26  # 26 horas (backup diário às 2h)

LATEST_BACKUP=$(find "$BACKUP_DIR" -name "ellomei_backup_*.sql.gz" -type f -printf "%T@ %p\n" | sort -rn | head -1 | cut -d' ' -f2)

if [ -z "$LATEST_BACKUP" ]; then
    echo "ERRO: Nenhum backup encontrado!"
    exit 1
fi

BACKUP_AGE_SECONDS=$(( $(date +%s) - $(stat -c %Y "$LATEST_BACKUP") ))
BACKUP_AGE_HOURS=$(( BACKUP_AGE_SECONDS / 3600 ))

if [ $BACKUP_AGE_HOURS -gt $MAX_AGE_HOURS ]; then
    echo "ALERTA: Último backup tem $BACKUP_AGE_HOURS horas!"
    exit 1
else
    echo "OK: Último backup tem $BACKUP_AGE_HOURS horas"
    exit 0
fi
```

---

## 🔧 **TROUBLESHOOTING**

### **Erro: "Container MySQL não encontrado"**

**Solução:**
```bash
# Verificar nome do container
docker ps -a | grep mysql

# Atualizar .env se necessário
MYSQL_CONTAINER_NAME=nome-correto-do-container
```

---

### **Erro: "Senha do banco de dados não configurada"**

**Solução:**
```bash
# Adicionar senha ao .env
echo "MYSQL_PASSWORD=sua-senha" >> .env
```

---

### **Backup muito grande**

**Solução:**
```bash
# Verificar tamanho do banco
docker exec ellomei-mysql mysql -u scf_user -p -e "
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'ellomei_db'
GROUP BY table_schema;"

# Limpar dados antigos se necessário
# (implementar limpeza de dados históricos)
```

---

## ✅ **CHECKLIST**

### **Configuração Inicial:**
- [ ] Scripts de backup/restore criados
- [ ] Diretório `backups/` criado
- [ ] Variáveis de ambiente configuradas
- [ ] Backup manual testado
- [ ] Restore testado

### **Produção:**
- [ ] Backup automático agendado (cron/systemd)
- [ ] Retenção configurada (30 dias)
- [ ] Backup offsite configurado
- [ ] Monitoramento de backups
- [ ] Teste de restore mensal
- [ ] Documentação de procedimentos

---

## 🎉 **RESUMO**

**Fazer backup manual:**
```bash
./backup-database.sh
```

**Restaurar backup:**
```bash
./restore-database.sh backups/ellomei_backup_XXXXXXXX_XXXXXX.sql.gz
```

**Agendar backup diário (cron):**
```bash
crontab -e
# Adicionar: 0 2 * * * cd /caminho/para/ElloMEI && ./backup-database.sh
```

---

**Dúvidas?** Consulte a documentação do MySQL.

