#!/bin/bash

# ========================================
# SCF-MEI - Docker Environment Manager
# ========================================
#
# Uso:
#   ./docker-start.sh           # Iniciar ambiente
#   ./docker-start.sh start     # Iniciar ambiente
#   ./docker-start.sh stop      # Parar ambiente
#   ./docker-start.sh restart   # Reiniciar ambiente
#   ./docker-start.sh status    # Ver status
#   ./docker-start.sh logs      # Ver logs
#   ./docker-start.sh clean     # Limpar tudo
#   ./docker-start.sh backup    # Criar backup
#

set -e  # Para em caso de erro

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_header() { echo -e "${CYAN}$1${NC}"; }

# ========================================
# FUNÇÕES
# ========================================

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker não está rodando. Por favor, inicie o Docker primeiro."
        exit 1
    fi
    print_success "Docker está rodando"
}

start_environment() {
    print_header "🐳 SCF-MEI - Iniciando Ambiente Docker"
    echo "======================================"
    echo ""

    check_docker
    echo ""

    # Stop and remove existing containers
    print_info "Limpando containers antigos..."
    docker compose down 2>/dev/null || true

    echo ""
    print_info "📥 ETAPA 1/3: Baixando imagem MySQL (pode demorar na primeira vez)..."
    docker pull mysql:8.0

    echo ""
    print_info "🗄️  ETAPA 2/3: Iniciando MySQL..."
    docker compose up -d mysql

    echo ""
    print_info "⏳ Aguardando MySQL ficar pronto..."
    for i in {1..30}; do
        if docker compose exec -T mysql mysqladmin ping -h localhost -u root -proot_password &> /dev/null; then
            print_success "MySQL está pronto!"
            break
        fi
        echo -n "."
        sleep 2
    done
    echo ""

    echo ""
    print_info "🏗️  ETAPA 3/3: Construindo e iniciando aplicação..."
    docker compose up --build -d app

    echo ""
    print_info "⏳ Aguardando aplicação iniciar..."
    sleep 15

    # Check if containers are running
    if docker ps | grep -q scf-mei-mysql && docker ps | grep -q scf-mei-app; then
        echo ""
        echo "================================================"
        print_success "AMBIENTE DOCKER CONFIGURADO COM SUCESSO!"
        echo "================================================"
        echo ""
        show_status
        echo ""
        show_help_commands
        echo ""
        print_info "Aguarde mais alguns segundos para a aplicação terminar de iniciar..."
        echo ""
    else
        echo ""
        print_error "Erro ao iniciar containers. Verificando logs..."
        docker compose logs --tail=50
        exit 1
    fi
}

stop_environment() {
    print_header "🛑 Parando Ambiente Docker"
    echo ""

    check_docker

    print_info "Parando containers..."
    docker compose down

    print_success "Ambiente parado com sucesso!"
}

restart_environment() {
    print_header "🔄 Reiniciando Ambiente Docker"
    echo ""

    check_docker

    print_info "Reiniciando containers..."
    docker compose restart

    print_success "Ambiente reiniciado com sucesso!"
    echo ""
    show_status
}

show_status() {
    print_header "📊 Status dos Containers"
    docker compose ps
    echo ""
    echo "🌐 Aplicação: http://localhost:8080"
    echo "🗄️  MySQL:    localhost:3307 (externa) / 3306 (interna)"
}

show_logs() {
    print_header "📋 Logs da Aplicação"
    echo ""
    print_info "Pressione Ctrl+C para sair"
    echo ""
    docker compose logs -f app
}

clean_environment() {
    print_header "🧹 Limpando Ambiente Docker"
    echo ""

    print_warning "ATENÇÃO: Esta operação irá remover:"
    echo "  - Todos os containers"
    echo "  - Todos os volumes (dados do banco serão perdidos!)"
    echo "  - Imagens não utilizadas"
    echo ""
    read -p "Deseja continuar? (s/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        print_info "Operação cancelada."
        exit 0
    fi

    check_docker

    print_info "Parando containers..."
    docker compose down -v

    print_info "Removendo imagens..."
    docker rmi scf-mei-app 2>/dev/null || true

    print_info "Limpando sistema Docker..."
    docker system prune -f

    print_success "Ambiente limpo com sucesso!"
    print_info "Execute './docker-start.sh' para recriar o ambiente"
}

create_backup() {
    print_header "💾 Criando Backup"
    echo ""

    if [ ! -f "./backup.sh" ]; then
        print_error "Script de backup não encontrado!"
        exit 1
    fi

    ./backup.sh
}

show_help_commands() {
    echo "📝 Comandos úteis:"
    echo "   ./docker-start.sh status       # Ver status"
    echo "   ./docker-start.sh logs         # Ver logs"
    echo "   ./docker-start.sh restart      # Reiniciar"
    echo "   ./docker-start.sh stop         # Parar"
    echo "   ./docker-start.sh backup       # Criar backup"
    echo "   ./docker-start.sh clean        # Limpar tudo"
}

show_help() {
    echo "SCF-MEI - Docker Environment Manager"
    echo ""
    echo "Uso: ./docker-start.sh [comando]"
    echo ""
    echo "Comandos disponíveis:"
    echo "  start     Iniciar ambiente (padrão)"
    echo "  stop      Parar ambiente"
    echo "  restart   Reiniciar ambiente"
    echo "  status    Ver status dos containers"
    echo "  logs      Ver logs da aplicação"
    echo "  clean     Limpar tudo (containers, volumes, imagens)"
    echo "  backup    Criar backup do banco de dados"
    echo "  help      Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  ./docker-start.sh              # Iniciar ambiente"
    echo "  ./docker-start.sh status       # Ver status"
    echo "  ./docker-start.sh logs         # Ver logs"
    echo ""
}

# ========================================
# PROCESSAMENTO DE ARGUMENTOS
# ========================================

case "${1:-start}" in
    start)
        start_environment
        ;;
    stop)
        stop_environment
        ;;
    restart)
        restart_environment
        ;;
    status)
        check_docker
        show_status
        ;;
    logs)
        check_docker
        show_logs
        ;;
    clean)
        clean_environment
        ;;
    backup)
        create_backup
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Comando inválido: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

