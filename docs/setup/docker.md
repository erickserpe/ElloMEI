# 🐳 ElloMEI - Configuração Completa com Docker

## 🎯 Tudo Rodando em Docker!

Este guia configura **TUDO** em Docker: MySQL + Aplicação Spring Boot.

---

## 📋 Pré-requisitos

Apenas Docker instalado e rodando:

```bash
docker --version
```

Se não tiver Docker instalado:

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Verificar
docker --version
```

---

## 🚀 Início Rápido (3 Comandos)

### Opção 1: Script Automático (RECOMENDADO)

```bash
chmod +x docker-start.sh
./docker-start.sh
```

### Opção 2: Manual

```bash
# 1. Baixar MySQL
docker pull mysql:8.0

# 2. Iniciar tudo
docker compose up --build -d

# 3. Ver logs
docker compose logs -f
```

---

## 📦 O que será criado?

### Containers:
- **ellomei-mysql**: Banco de dados MySQL 8.0
- **ellomei-app**: Aplicação Spring Boot

### Rede:
- **ellomei-network**: Rede bridge para comunicação entre containers

### Volumes:
- **mysql_data**: Dados persistentes do MySQL
- **./uploads**: Arquivos de upload da aplicação

---

## 🎮 Comandos Essenciais

### Gerenciamento Básico

```bash
# Iniciar tudo
docker compose up -d

# Parar tudo
docker compose down

# Reiniciar
docker compose restart

# Ver status
docker compose ps

# Parar e remover TUDO (incluindo volumes)
docker compose down -v
```

### Logs

```bash
# Logs de todos os containers
docker compose logs -f

# Logs apenas da aplicação
docker compose logs -f app

# Logs apenas do MySQL
docker compose logs -f mysql

# Últimas 100 linhas
docker compose logs --tail=100 app
```

### Reconstruir

```bash
# Reconstruir apenas a aplicação
docker compose up --build -d app

# Reconstruir tudo do zero
docker compose down
docker compose build --no-cache
docker compose up -d
```

---

## 🔧 Acessar Containers

### Aplicação Spring Boot

```bash
# Acessar shell do container
docker exec -it ellomei-app sh

# Ver logs em tempo real
docker logs -f ellomei-app
```

### MySQL

```bash
# Acessar MySQL CLI
docker exec -it ellomei-mysql mysql -u scf_user -p5522 ellomei_db

# Executar comando SQL direto
docker exec -it ellomei-mysql mysql -u scf_user -p5522 ellomei_db -e "SHOW TABLES;"

# Ver índices
docker exec -it ellomei-mysql mysql -u scf_user -p5522 ellomei_db -e "SHOW INDEX FROM lancamento;"
```

---

## 🗄️ Gerenciar Banco de Dados

### Backup

```bash
# Backup completo
docker exec ellomei-mysql mysqldump -u scf_user -p5522 ellomei_db > backup.sql

# Backup com data
docker exec ellomei-mysql mysqldump -u scf_user -p5522 ellomei_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restaurar

```bash
# Restaurar backup
docker exec -i ellomei-mysql mysql -u scf_user -p5522 ellomei_db < backup.sql
```

### Limpar Dados

```bash
# Parar containers
docker compose down

# Remover volume do MySQL (CUIDADO: apaga todos os dados!)
docker volume rm ellomei_mysql_data

# Iniciar novamente (banco vazio)
docker compose up -d
```

---

## 🐛 Troubleshooting

### Container não inicia

```bash
# Ver logs detalhados
docker compose logs app

# Ver últimas 50 linhas
docker compose logs --tail=50 app

# Verificar se MySQL está saudável
docker compose ps
```

### Erro de conexão com MySQL

```bash
# Verificar se MySQL está rodando
docker ps | grep mysql

# Reiniciar MySQL
docker compose restart mysql

# Aguardar MySQL ficar pronto
docker compose exec mysql mysqladmin ping -h localhost -u root -proot_password
```

### Porta 8080 ou 3306 já em uso

Edite `docker-compose.yml`:

```yaml
services:
  mysql:
    ports:
      - "3307:3306"  # MySQL na porta 3307
  app:
    ports:
      - "8081:8080"  # App na porta 8081
```

### Aplicação não conecta no MySQL

```bash
# Verificar rede
docker network inspect ellomei_ellomei-network

# Testar conexão do container da app
docker compose exec app ping mysql
```

### Limpar tudo e começar do zero

```bash
# Parar e remover tudo
docker compose down -v

# Remover imagens
docker rmi ellomei-ellomei-app
docker rmi mysql:8.0

# Limpar cache do Docker
docker system prune -a

# Iniciar novamente
./docker-start.sh
```

---

## 📊 Monitoramento

### Ver uso de recursos

```bash
# Estatísticas em tempo real
docker stats

# Apenas containers do projeto
docker stats ellomei-mysql ellomei-app
```

### Ver espaço em disco

```bash
# Espaço usado pelo Docker
docker system df

# Detalhado
docker system df -v
```

---

## 🔄 Desenvolvimento

### Fazer alterações no código

1. Edite o código fonte
2. Reconstrua o container:
   ```bash
   docker compose up --build -d app
   ```

### Executar testes

```bash
# Dentro do container
docker compose exec app sh
mvn test

# Ou direto
docker compose exec app mvn test
```

### Hot Reload (Desenvolvimento)

Para desenvolvimento com hot reload, use este `docker-compose.dev.yml`:

```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    volumes:
      - ./src:/app/src
      - ./target:/app/target
    environment:
      SPRING_DEVTOOLS_RESTART_ENABLED: true
```

Execute:
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

---

## 🚀 Deploy em Produção

### Construir imagem otimizada

```bash
docker build -t ellomei:latest .
```

### Executar em produção

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## 📈 Próximos Passos

1. ✅ Ambiente Docker configurado
2. 🔄 Configurar CI/CD com GitHub Actions
3. ☁️ Deploy em cloud (AWS, Azure, GCP)
4. 📊 Adicionar monitoramento (Prometheus, Grafana)
5. 🔐 Configurar HTTPS com Nginx reverse proxy

---

## 🎓 Dicas Importantes

### Performance

- Use volumes nomeados para dados persistentes
- Não monte `node_modules` ou `target` como volumes
- Use multi-stage builds (já configurado no Dockerfile)

### Segurança

- **NUNCA** use senhas padrão em produção
- Use Docker secrets para senhas
- Não exponha portas desnecessárias
- Mantenha imagens atualizadas

### Backup

- Faça backup regular do volume `mysql_data`
- Use `docker volume backup` ou ferramentas como Velero
- Teste restauração de backups regularmente

---

## 📚 Recursos Úteis

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)

---

## ✅ Checklist de Verificação

Após executar `./docker-start.sh`:

- [ ] Containers rodando: `docker compose ps`
- [ ] MySQL saudável: `docker compose exec mysql mysqladmin ping -h localhost -u root -proot_password`
- [ ] Aplicação acessível: http://localhost:8080
- [ ] Logs sem erros: `docker compose logs app`
- [ ] Banco de dados criado: `docker compose exec mysql mysql -u scf_user -p5522 -e "SHOW DATABASES;"`

---

## 🆘 Suporte

Se encontrar problemas:

1. Verifique os logs: `docker compose logs -f`
2. Verifique o status: `docker compose ps`
3. Teste a conexão: `docker compose exec app ping mysql`
4. Reinicie tudo: `docker compose restart`
5. Limpe e recomece: `docker compose down -v && ./docker-start.sh`

