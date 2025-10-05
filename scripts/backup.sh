#!/bin/bash

# ========================================
# ElloMEI - Script de Backup do Banco de Dados
# ========================================
#
# Este script cria backups automáticos do banco de dados MySQL
# rodando no Docker e gerencia a rotação de backups antigos.
#
# Uso:
#   ./backup.sh                    # Backup completo
#   ./backup.sh --restore <file>   # Restaurar backup
#   ./backup.sh --list             # Listar backups
#   ./backup.sh --clean            # Limpar backups antigos
#

set -e  # Para em caso de erro

# ========================================
# CONFIGURAÇÕES
# ========================================

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações do banco
MYSQL_CONTAINER="ellomei-mysql"
MYSQL_USER="scf_user"
MYSQL_PASSWORD="5522"
MYSQL_DATABASE="ellomei_db"
MYSQL_ROOT_PASSWORD="root_password"

# Configurações de backup
BACKUP_DIR="./backups"
BACKUP_RETENTION_DAYS=7
DATE_FORMAT=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="ellomei_backup_${DATE_FORMAT}.sql"

# ========================================
# FUNÇÕES AUXILIARES
# ========================================

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

# ========================================
# VERIFICAÇÕES
# ========================================

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker não está instalado!"
        exit 1
    fi
}

check_container() {
    if ! docker ps | grep -q "$MYSQL_CONTAINER"; then
        print_error "Container MySQL não está rodando!"
        print_info "Execute: docker compose up -d"
        exit 1
    fi
}

create_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        print_success "Diretório de backup criado: $BACKUP_DIR"
    fi
}

# ========================================
# FUNÇÕES DE BACKUP
# ========================================

create_backup() {
    print_header "CRIANDO BACKUP DO BANCO DE DADOS"
    
    check_docker
    check_container
    create_backup_dir
    
    print_info "Banco de dados: $MYSQL_DATABASE"
    print_info "Arquivo: $BACKUP_FILE"
    print_info "Diretório: $BACKUP_DIR"
    echo ""
    
    # Criar backup
    print_info "Exportando banco de dados..."
    docker exec $MYSQL_CONTAINER mysqldump \
        -u root \
        -p${MYSQL_ROOT_PASSWORD} \
        --databases $MYSQL_DATABASE \
        --add-drop-database \
        --routines \
        --triggers \
        --events \
        > "$BACKUP_DIR/$BACKUP_FILE"
    
    # Verificar se o backup foi criado
    if [ -f "$BACKUP_DIR/$BACKUP_FILE" ]; then
        BACKUP_SIZE=$(du -h "$BACKUP_DIR/$BACKUP_FILE" | cut -f1)
        print_success "Backup criado com sucesso!"
        print_info "Tamanho: $BACKUP_SIZE"
        print_info "Localização: $BACKUP_DIR/$BACKUP_FILE"
        
        # Comprimir backup
        print_info "Comprimindo backup..."
        gzip "$BACKUP_DIR/$BACKUP_FILE"
        COMPRESSED_SIZE=$(du -h "$BACKUP_DIR/$BACKUP_FILE.gz" | cut -f1)
        print_success "Backup comprimido: $COMPRESSED_SIZE"
        
        # Limpar backups antigos
        clean_old_backups
    else
        print_error "Falha ao criar backup!"
        exit 1
    fi
}

restore_backup() {
    local backup_file=$1
    
    print_header "RESTAURANDO BACKUP DO BANCO DE DADOS"
    
    if [ -z "$backup_file" ]; then
        print_error "Especifique o arquivo de backup!"
        print_info "Uso: ./backup.sh --restore <arquivo>"
        print_info "Liste os backups com: ./backup.sh --list"
        exit 1
    fi
    
    # Verificar se o arquivo existe
    if [ ! -f "$backup_file" ]; then
        # Tentar no diretório de backups
        if [ -f "$BACKUP_DIR/$backup_file" ]; then
            backup_file="$BACKUP_DIR/$backup_file"
        else
            print_error "Arquivo de backup não encontrado: $backup_file"
            exit 1
        fi
    fi
    
    check_docker
    check_container
    
    print_warning "ATENÇÃO: Esta operação irá SUBSTITUIR o banco de dados atual!"
    read -p "Deseja continuar? (s/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        print_info "Operação cancelada."
        exit 0
    fi
    
    print_info "Arquivo: $backup_file"
    echo ""
    
    # Descomprimir se necessário
    if [[ $backup_file == *.gz ]]; then
        print_info "Descomprimindo backup..."
        gunzip -c "$backup_file" > /tmp/restore_temp.sql
        backup_file="/tmp/restore_temp.sql"
    fi
    
    # Restaurar backup
    print_info "Restaurando banco de dados..."
    docker exec -i $MYSQL_CONTAINER mysql \
        -u root \
        -p${MYSQL_ROOT_PASSWORD} \
        < "$backup_file"
    
    # Limpar arquivo temporário
    if [ -f "/tmp/restore_temp.sql" ]; then
        rm /tmp/restore_temp.sql
    fi
    
    print_success "Backup restaurado com sucesso!"
    print_info "Reinicie a aplicação: docker compose restart app"
}

list_backups() {
    print_header "BACKUPS DISPONÍVEIS"
    
    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A $BACKUP_DIR 2>/dev/null)" ]; then
        print_warning "Nenhum backup encontrado em $BACKUP_DIR"
        exit 0
    fi
    
    echo -e "${BLUE}Data/Hora          Tamanho    Arquivo${NC}"
    echo "--------------------------------------------------------"
    
    for file in $(ls -t $BACKUP_DIR/*.sql.gz 2>/dev/null); do
        if [ -f "$file" ]; then
            filename=$(basename "$file")
            filesize=$(du -h "$file" | cut -f1)
            # Extrair data do nome do arquivo
            if [[ $filename =~ ellomei_backup_([0-9]{8})_([0-9]{6}) ]]; then
                date_part="${BASH_REMATCH[1]}"
                time_part="${BASH_REMATCH[2]}"
                formatted_date="${date_part:6:2}/${date_part:4:2}/${date_part:0:4} ${time_part:0:2}:${time_part:2:2}:${time_part:4:2}"
                echo -e "${formatted_date}  ${filesize}\t${filename}"
            else
                echo -e "N/A                ${filesize}\t${filename}"
            fi
        fi
    done
    
    echo ""
    total_backups=$(ls -1 $BACKUP_DIR/*.sql.gz 2>/dev/null | wc -l)
    total_size=$(du -sh $BACKUP_DIR 2>/dev/null | cut -f1)
    print_info "Total de backups: $total_backups"
    print_info "Espaço total: $total_size"
}

clean_old_backups() {
    print_info "Limpando backups antigos (mantendo últimos $BACKUP_RETENTION_DAYS dias)..."
    
    if [ ! -d "$BACKUP_DIR" ]; then
        return
    fi
    
    # Encontrar e remover backups antigos
    find "$BACKUP_DIR" -name "ellomei_backup_*.sql.gz" -type f -mtime +$BACKUP_RETENTION_DAYS -delete
    
    removed=$(find "$BACKUP_DIR" -name "ellomei_backup_*.sql.gz" -type f -mtime +$BACKUP_RETENTION_DAYS 2>/dev/null | wc -l)
    
    if [ $removed -gt 0 ]; then
        print_success "Removidos $removed backup(s) antigo(s)"
    else
        print_info "Nenhum backup antigo para remover"
    fi
}

# ========================================
# MENU PRINCIPAL
# ========================================

show_help() {
    echo "ElloMEI - Script de Backup do Banco de Dados"
    echo ""
    echo "Uso:"
    echo "  ./backup.sh                    Criar backup completo"
    echo "  ./backup.sh --restore <file>   Restaurar backup"
    echo "  ./backup.sh --list             Listar backups disponíveis"
    echo "  ./backup.sh --clean            Limpar backups antigos"
    echo "  ./backup.sh --help             Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  ./backup.sh"
    echo "  ./backup.sh --restore ellomei_backup_20251003_120000.sql.gz"
    echo "  ./backup.sh --list"
    echo ""
}

# ========================================
# PROCESSAMENTO DE ARGUMENTOS
# ========================================

case "${1:-}" in
    --restore)
        restore_backup "$2"
        ;;
    --list)
        list_backups
        ;;
    --clean)
        clean_old_backups
        ;;
    --help|-h)
        show_help
        ;;
    "")
        create_backup
        ;;
    *)
        print_error "Opção inválida: $1"
        show_help
        exit 1
        ;;
esac

echo ""

