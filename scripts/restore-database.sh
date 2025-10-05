#!/bin/bash

# ===================================
# Script de Restore do Banco de Dados
# ===================================
# Restaura backup do MySQL
#
# Uso:
#   ./restore-database.sh [arquivo_backup.sql.gz]
#
# Se nenhum arquivo for especificado, lista backups disponíveis
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
MYSQL_CONTAINER="${MYSQL_CONTAINER_NAME:-scf-mei-mysql}"

# Credenciais do banco
DB_NAME="${MYSQL_DATABASE:-scf_mei_db}"
DB_USER="${MYSQL_USER:-scf_user}"
DB_PASSWORD="${MYSQL_PASSWORD}"

# Arquivo de backup (argumento)
BACKUP_FILE="$1"

# ===================================
# FUNÇÕES
# ===================================

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

error() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1" >&2
}

list_backups() {
    log "📋 Backups disponíveis em $BACKUP_DIR:"
    log "================================================"
    
    local count=0
    find "$BACKUP_DIR" -name "scf_mei_backup_*.sql.gz" -type f -printf "%T@ %p\n" | \
    sort -rn | \
    while read timestamp filepath; do
        ((count++))
        filename=$(basename "$filepath")
        filesize=$(du -h "$filepath" | cut -f1)
        filedate=$(date -d "@${timestamp%.*}" "+%Y-%m-%d %H:%M:%S")
        log "   $count. $filename ($filesize) - $filedate"
    done
    
    if [ $count -eq 0 ]; then
        log "   Nenhum backup encontrado"
    fi
    
    log "================================================"
    log ""
    log "Uso: ./restore-database.sh <arquivo_backup.sql.gz>"
    log "Exemplo: ./restore-database.sh backups/scf_mei_backup_20250104_020000.sql.gz"
}

# ===================================
# VALIDAÇÕES
# ===================================

log "🔄 Iniciando restore do banco de dados SCF-MEI"
log "================================================"

# Se nenhum arquivo foi especificado, listar backups
if [ -z "$BACKUP_FILE" ]; then
    list_backups
    exit 0
fi

# Verificar se arquivo existe
if [ ! -f "$BACKUP_FILE" ]; then
    error "Arquivo de backup não encontrado: $BACKUP_FILE"
    log ""
    list_backups
    exit 1
fi

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
# CONFIRMAÇÃO
# ===================================

BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
log ""
log "⚠️  ATENÇÃO: Esta operação irá SOBRESCREVER o banco de dados atual!"
log ""
log "   Arquivo: $(basename "$BACKUP_FILE")"
log "   Tamanho: $BACKUP_SIZE"
log "   Database: $DB_NAME"
log "   Container: $MYSQL_CONTAINER"
log ""
read -p "   Deseja continuar? (digite 'SIM' para confirmar): " -r
echo ""

if [ "$REPLY" != "SIM" ]; then
    log "❌ Operação cancelada pelo usuário"
    exit 0
fi

# ===================================
# EXECUTAR RESTORE
# ===================================

log "🔄 Restaurando backup..."

# Descompactar e restaurar
if gunzip -c "$BACKUP_FILE" | docker exec -i "$MYSQL_CONTAINER" mysql \
    -u "$DB_USER" \
    -p"$DB_PASSWORD" \
    2>/dev/null; then
    
    log "✅ Restore concluído com sucesso!"
else
    error "Falha ao restaurar backup!"
    exit 1
fi

# ===================================
# VERIFICAÇÃO
# ===================================

log ""
log "🔍 Verificando restore..."

# Contar tabelas
TABLE_COUNT=$(docker exec "$MYSQL_CONTAINER" mysql \
    -u "$DB_USER" \
    -p"$DB_PASSWORD" \
    -D "$DB_NAME" \
    -e "SHOW TABLES;" \
    2>/dev/null | wc -l)

# Subtrair 1 (header)
TABLE_COUNT=$((TABLE_COUNT - 1))

if [ $TABLE_COUNT -gt 0 ]; then
    log "✅ Verificação OK: $TABLE_COUNT tabela(s) encontrada(s)"
else
    error "Verificação falhou: Nenhuma tabela encontrada!"
    exit 1
fi

# ===================================
# RESUMO
# ===================================

log ""
log "================================================"
log "✅ RESTORE CONCLUÍDO COM SUCESSO!"
log "================================================"
log "   Arquivo: $(basename "$BACKUP_FILE")"
log "   Database: $DB_NAME"
log "   Tabelas: $TABLE_COUNT"
log "================================================"
log ""
log "⚠️  IMPORTANTE: Reinicie a aplicação para garantir consistência"
log "   ./docker-start.sh restart"
log ""

exit 0

