#!/bin/bash

# ===================================
# Script de Backup do Banco de Dados
# ===================================
# Cria backup do MySQL e faz rotação de backups antigos
#
# Uso:
#   ./backup-database.sh
#
# Agendamento (cron):
#   0 2 * * * /path/to/backup-database.sh
#   (Executa todo dia às 2h da manhã)
# ===================================

set -e

# ===================================
# CONFIGURAÇÕES
# ===================================

# Carregar variáveis do .env
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Diretório de backups
BACKUP_DIR="${BACKUP_DIR:-./backups}"

# Nome do container MySQL
MYSQL_CONTAINER="${MYSQL_CONTAINER_NAME:-ellomei-mysql}"

# Credenciais do banco
DB_NAME="${MYSQL_DATABASE:-ellomei_db}"
DB_USER="${MYSQL_USER:-scf_user}"
DB_PASSWORD="${MYSQL_PASSWORD}"

# Retenção de backups (dias)
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-7}"

# Timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DATE=$(date +"%Y-%m-%d %H:%M:%S")

# Nome do arquivo de backup
BACKUP_FILE="ellomei_backup_${TIMESTAMP}.sql"
BACKUP_FILE_GZ="${BACKUP_FILE}.gz"

# ===================================
# FUNÇÕES
# ===================================

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

error() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1" >&2
}

# ===================================
# VALIDAÇÕES
# ===================================

log "🗄️  Iniciando backup do banco de dados ElloMEI"
log "================================================"

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    error "Docker não está rodando!"
    exit 1
fi

# Verificar se container MySQL existe
if ! docker ps -a --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    error "Container MySQL '${MYSQL_CONTAINER}' não encontrado!"
    exit 1
fi

# Verificar se container MySQL está rodando
if ! docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    error "Container MySQL '${MYSQL_CONTAINER}' não está rodando!"
    exit 1
fi

# Verificar se senha do banco foi fornecida
if [ -z "$DB_PASSWORD" ]; then
    error "Senha do banco de dados não configurada!"
    error "Configure MYSQL_PASSWORD no arquivo .env"
    exit 1
fi

# ===================================
# CRIAR DIRETÓRIO DE BACKUPS
# ===================================

if [ ! -d "$BACKUP_DIR" ]; then
    log "📁 Criando diretório de backups: $BACKUP_DIR"
    mkdir -p "$BACKUP_DIR"
fi

# ===================================
# EXECUTAR BACKUP
# ===================================

log "💾 Criando backup do banco de dados..."
log "   Database: $DB_NAME"
log "   Container: $MYSQL_CONTAINER"
log "   Arquivo: $BACKUP_FILE_GZ"

# Executar mysqldump dentro do container
if docker exec "$MYSQL_CONTAINER" mysqldump \
    -u "$DB_USER" \
    -p"$DB_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --databases "$DB_NAME" \
    2>/dev/null | gzip > "$BACKUP_DIR/$BACKUP_FILE_GZ"; then
    
    # Verificar se arquivo foi criado
    if [ -f "$BACKUP_DIR/$BACKUP_FILE_GZ" ]; then
        BACKUP_SIZE=$(du -h "$BACKUP_DIR/$BACKUP_FILE_GZ" | cut -f1)
        log "✅ Backup criado com sucesso!"
        log "   Tamanho: $BACKUP_SIZE"
        log "   Localização: $BACKUP_DIR/$BACKUP_FILE_GZ"
    else
        error "Arquivo de backup não foi criado!"
        exit 1
    fi
else
    error "Falha ao criar backup!"
    exit 1
fi

# ===================================
# ROTAÇÃO DE BACKUPS
# ===================================

log ""
log "🔄 Executando rotação de backups..."
log "   Retenção: $RETENTION_DAYS dias"

# Contar backups antes da rotação
BACKUPS_BEFORE=$(find "$BACKUP_DIR" -name "ellomei_backup_*.sql.gz" | wc -l)

# Remover backups antigos
DELETED_COUNT=0
while IFS= read -r old_backup; do
    if [ -n "$old_backup" ]; then
        log "   🗑️  Removendo: $(basename "$old_backup")"
        rm -f "$old_backup"
        ((DELETED_COUNT++))
    fi
done < <(find "$BACKUP_DIR" -name "ellomei_backup_*.sql.gz" -type f -mtime +$RETENTION_DAYS)

# Contar backups após rotação
BACKUPS_AFTER=$(find "$BACKUP_DIR" -name "ellomei_backup_*.sql.gz" | wc -l)

if [ $DELETED_COUNT -gt 0 ]; then
    log "✅ Rotação concluída: $DELETED_COUNT backup(s) removido(s)"
else
    log "ℹ️  Nenhum backup antigo para remover"
fi

log "   Backups mantidos: $BACKUPS_AFTER"

# ===================================
# LISTAR BACKUPS
# ===================================

log ""
log "📋 Backups disponíveis:"
log "================================================"

if [ $BACKUPS_AFTER -gt 0 ]; then
    find "$BACKUP_DIR" -name "ellomei_backup_*.sql.gz" -type f -printf "%T@ %p\n" | \
    sort -rn | \
    while read timestamp filepath; do
        filename=$(basename "$filepath")
        filesize=$(du -h "$filepath" | cut -f1)
        filedate=$(date -d "@${timestamp%.*}" "+%Y-%m-%d %H:%M:%S")
        log "   $filename ($filesize) - $filedate"
    done
else
    log "   Nenhum backup encontrado"
fi

# ===================================
# RESUMO
# ===================================

log ""
log "================================================"
log "✅ BACKUP CONCLUÍDO COM SUCESSO!"
log "================================================"
log "   Data/Hora: $DATE"
log "   Arquivo: $BACKUP_FILE_GZ"
log "   Tamanho: $BACKUP_SIZE"
log "   Localização: $BACKUP_DIR"
log "   Backups mantidos: $BACKUPS_AFTER"
log "================================================"

exit 0

