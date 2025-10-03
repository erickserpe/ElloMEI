# 🚀 SCF-MEI - Recursos Avançados

Este documento descreve os recursos avançados adicionados ao projeto.

---

## 📋 Índice

1. [Gerenciador de Ambiente Docker](#gerenciador-de-ambiente-docker)
2. [Sistema de Backup](#sistema-de-backup)
3. [Variáveis de Ambiente](#variáveis-de-ambiente)
4. [Boas Práticas](#boas-práticas)

---

## 🐳 Gerenciador de Ambiente Docker

O script `docker-start.sh` foi aprimorado e agora funciona como um gerenciador completo do ambiente.

### Comandos Disponíveis

```bash
# Iniciar ambiente
./docker-start.sh
./docker-start.sh start

# Parar ambiente
./docker-start.sh stop

# Reiniciar ambiente
./docker-start.sh restart

# Ver status dos containers
./docker-start.sh status

# Ver logs em tempo real
./docker-start.sh logs

# Criar backup do banco de dados
./docker-start.sh backup

# Limpar tudo (containers, volumes, imagens)
./docker-start.sh clean

# Mostrar ajuda
./docker-start.sh help
```

### Exemplos de Uso

#### Iniciar o Ambiente
```bash
./docker-start.sh
```
Ou explicitamente:
```bash
./docker-start.sh start
```

#### Ver Status
```bash
./docker-start.sh status
```
Mostra:
- Status de cada container
- URLs de acesso
- Portas expostas

#### Ver Logs
```bash
./docker-start.sh logs
```
Mostra logs em tempo real. Pressione `Ctrl+C` para sair.

#### Reiniciar Aplicação
```bash
./docker-start.sh restart
```
Útil após mudanças de configuração.

#### Limpar Ambiente
```bash
./docker-start.sh clean
```
⚠️ **ATENÇÃO**: Remove tudo, incluindo dados do banco!

---

## 💾 Sistema de Backup

Script completo para backup e restauração do banco de dados MySQL.

### Comandos de Backup

```bash
# Criar backup
./backup.sh

# Listar backups disponíveis
./backup.sh --list

# Restaurar backup
./backup.sh --restore <arquivo>

# Limpar backups antigos
./backup.sh --clean

# Mostrar ajuda
./backup.sh --help
```

### Exemplos de Uso

#### Criar Backup
```bash
./backup.sh
```

Resultado:
```
✅ Backup criado com sucesso!
ℹ️  Tamanho: 2.3M
ℹ️  Localização: ./backups/scf_mei_backup_20251003_143022.sql.gz
```

#### Listar Backups
```bash
./backup.sh --list
```

Resultado:
```
Data/Hora          Tamanho    Arquivo
--------------------------------------------------------
03/10/2025 14:30:22  2.3M    scf_mei_backup_20251003_143022.sql.gz
02/10/2025 10:15:45  2.1M    scf_mei_backup_20251002_101545.sql.gz

ℹ️  Total de backups: 2
ℹ️  Espaço total: 4.4M
```

#### Restaurar Backup
```bash
./backup.sh --restore scf_mei_backup_20251003_143022.sql.gz
```

⚠️ **ATENÇÃO**: Isso irá substituir o banco de dados atual!

### Configurações de Backup

Edite o arquivo `backup.sh` para alterar:

```bash
# Diretório de backups
BACKUP_DIR="./backups"

# Dias de retenção (backups mais antigos são removidos)
BACKUP_RETENTION_DAYS=7
```

### Rotação Automática

Backups mais antigos que 7 dias são automaticamente removidos após cada novo backup.

Para alterar:
```bash
# Editar backup.sh
BACKUP_RETENTION_DAYS=30  # Manter por 30 dias
```

### Backup Manual do Diretório de Uploads

```bash
# Criar backup dos uploads
tar -czf uploads_backup_$(date +%Y%m%d_%H%M%S).tar.gz uploads/

# Restaurar uploads
tar -xzf uploads_backup_20251003_143022.tar.gz
```

---

## 🔧 Variáveis de Ambiente

O arquivo `.env.example` contém todas as variáveis de ambiente configuráveis.

### Configuração Inicial

```bash
# Copiar arquivo de exemplo
cp .env.example .env

# Editar com suas configurações
nano .env
```

### Variáveis Principais

#### MySQL
```bash
MYSQL_DATABASE=scf_mei_db
MYSQL_USER=scf_user
MYSQL_PASSWORD=5522
MYSQL_ROOT_PASSWORD=root_password
MYSQL_PORT=3307
```

#### Spring Boot
```bash
APP_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

#### JPA/Hibernate
```bash
# Primeira execução: use 'update'
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Após primeira execução: use 'validate'
# SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

#### Flyway
```bash
# Primeira execução: desabilitado
SPRING_FLYWAY_ENABLED=false

# Após primeira execução: habilitar
# SPRING_FLYWAY_ENABLED=true
```

### Usando Variáveis de Ambiente

Para usar as variáveis do `.env`, atualize o `docker-compose.yml`:

```yaml
services:
  mysql:
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-scf_mei_db}
      MYSQL_USER: ${MYSQL_USER:-scf_user}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-5522}
```

O formato `${VAR:-default}` usa o valor do `.env` ou o padrão se não existir.

---

## 📚 Boas Práticas

### 1. Desenvolvimento Local

```bash
# Iniciar ambiente
./docker-start.sh

# Ver logs durante desenvolvimento
./docker-start.sh logs

# Fazer backup antes de mudanças grandes
./backup.sh
```

### 2. Antes de Mudanças Importantes

```bash
# 1. Criar backup
./backup.sh

# 2. Fazer mudanças no código

# 3. Reiniciar para aplicar
./docker-start.sh restart

# 4. Se algo der errado, restaurar
./backup.sh --restore <arquivo>
```

### 3. Limpeza Periódica

```bash
# Limpar backups antigos
./backup.sh --clean

# Limpar imagens Docker não utilizadas
docker system prune -f

# Limpar tudo e recomeçar (cuidado!)
./docker-start.sh clean
./docker-start.sh start
```

### 4. Segurança

#### Nunca Commite Dados Sensíveis
```bash
# Arquivos já no .gitignore:
.env              # Variáveis de ambiente
backups/          # Backups do banco
uploads/          # Arquivos dos usuários (opcional)
logs/             # Logs da aplicação
```

#### Altere Senhas em Produção
```bash
# Edite .env com senhas fortes
MYSQL_PASSWORD=senha_forte_aqui_123!@#
MYSQL_ROOT_PASSWORD=outra_senha_forte_456!@#
```

#### Use HTTPS em Produção
Configure um proxy reverso (Nginx/Traefik) com SSL.

### 5. Monitoramento

#### Ver Uso de Recursos
```bash
# CPU e memória dos containers
docker stats

# Espaço em disco
docker system df

# Logs de erro
docker compose logs app | grep ERROR
```

#### Health Checks
```bash
# Verificar saúde do MySQL
docker compose exec mysql mysqladmin ping -h localhost -u root -proot_password

# Verificar aplicação
curl http://localhost:8080/actuator/health
```

---

## 🔄 Workflow Recomendado

### Desenvolvimento Diário

```bash
# 1. Iniciar ambiente
./docker-start.sh

# 2. Desenvolver código

# 3. Ver logs se necessário
./docker-start.sh logs

# 4. Reiniciar após mudanças
./docker-start.sh restart

# 5. Parar ao final do dia
./docker-start.sh stop
```

### Antes de Deploy

```bash
# 1. Criar backup
./backup.sh

# 2. Testar em ambiente limpo
./docker-start.sh clean
./docker-start.sh start

# 3. Executar testes
docker compose run --rm app mvn test

# 4. Se tudo OK, fazer deploy
```

### Recuperação de Desastres

```bash
# 1. Listar backups disponíveis
./backup.sh --list

# 2. Parar ambiente
./docker-start.sh stop

# 3. Limpar tudo
./docker-start.sh clean

# 4. Iniciar ambiente limpo
./docker-start.sh start

# 5. Restaurar backup
./backup.sh --restore <arquivo_mais_recente>

# 6. Reiniciar aplicação
./docker-start.sh restart
```

---

## 🆘 Troubleshooting

### Backup Falha

```bash
# Verificar se MySQL está rodando
docker compose ps

# Verificar logs do MySQL
docker compose logs mysql

# Tentar manualmente
docker exec scf-mei-mysql mysqldump -u root -proot_password scf_mei_db > backup_manual.sql
```

### Restauração Falha

```bash
# Verificar arquivo de backup
gunzip -t backups/scf_mei_backup_*.sql.gz

# Restaurar manualmente
gunzip -c backups/scf_mei_backup_*.sql.gz | docker exec -i scf-mei-mysql mysql -u root -proot_password
```

### Variáveis de Ambiente Não Funcionam

```bash
# Verificar se .env existe
ls -la .env

# Verificar sintaxe do .env (sem espaços ao redor do =)
cat .env

# Recriar containers para aplicar mudanças
docker compose down
docker compose up -d
```

---

## 📝 Arquivos Criados

- **`.env.example`** - Template de variáveis de ambiente
- **`backup.sh`** - Script de backup e restauração
- **`docker-start.sh`** (melhorado) - Gerenciador de ambiente
- **`ADVANCED-FEATURES.md`** - Esta documentação

---

## 🎯 Próximos Passos

1. ✅ Copiar `.env.example` para `.env`
2. ✅ Configurar variáveis no `.env`
3. ✅ Criar primeiro backup: `./backup.sh`
4. ✅ Testar restauração em ambiente de teste
5. ✅ Configurar backup automático (cron)

### Backup Automático com Cron

```bash
# Editar crontab
crontab -e

# Adicionar linha para backup diário às 2h da manhã
0 2 * * * cd /caminho/para/SCF-MEI && ./backup.sh >> /var/log/scf-mei-backup.log 2>&1
```

---

**Documentação criada em**: 2025-10-03  
**Versão**: 1.0

