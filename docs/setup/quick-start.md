# 🚀 ElloMEI - Guia Rápido Docker

## ⚡ Início em 3 Passos

### 1. Iniciar Ambiente

```bash
./docker-start.sh
```

### 2. Aguardar Inicialização

Aguarde a mensagem:
```
✅ AMBIENTE DOCKER CONFIGURADO COM SUCESSO!
```

### 3. Acessar Aplicação

```
http://localhost:8080
```

---

## 📋 Comandos Essenciais

### Ver Status

```bash
docker compose ps
```

### Ver Logs

```bash
# Todos os logs
docker compose logs -f

# Apenas aplicação
docker compose logs -f app

# Apenas MySQL
docker compose logs -f mysql
```

### Parar/Iniciar

```bash
# Parar tudo
docker compose down

# Iniciar tudo
docker compose up -d

# Reiniciar
docker compose restart
```

---

## 🗄️ Acessar Banco de Dados

```bash
# Via Docker
docker exec -it ellomei-mysql mysql -u scf_user -p5522 ellomei_db

# Comandos úteis SQL
SHOW TABLES;
SELECT * FROM usuario;
SELECT * FROM lancamento LIMIT 10;
SHOW INDEX FROM lancamento;
```

---

## 🔧 Desenvolvimento

### Fazer Alterações no Código

```bash
# 1. Edite o código
# 2. Reconstrua
docker compose up --build -d app

# 3. Veja os logs
docker compose logs -f app
```

### Executar Testes

```bash
docker compose exec app mvn test
```

---

## 🐛 Problemas Comuns

### Container não inicia

```bash
docker compose logs app
docker compose restart
```

### Limpar tudo e recomeçar

```bash
docker compose down -v
./docker-start.sh
```

### Porta em uso

Edite `docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Mude 8080 para 8081
```

---

## 📊 Informações do Ambiente

### Containers
- **ellomei-mysql**: MySQL 8.0 (porta 3306)
- **ellomei-app**: Spring Boot (porta 8080)

### Credenciais MySQL
- **Database**: ellomei_db
- **Usuário**: scf_user
- **Senha**: 5522
- **Root Password**: root_password

### Volumes
- **mysql_data**: Dados persistentes do MySQL
- **./uploads**: Arquivos de upload

---

## 🎯 Próximos Passos

1. ✅ Ambiente rodando
2. 📝 Criar primeiro usuário
3. 🧪 Testar funcionalidades
4. 📊 Verificar relatórios
5. 🔐 Configurar backup

---

## 📚 Documentação Completa

- **DOCKER-SETUP.md**: Guia completo Docker
- **README-DOCKER.md**: Documentação detalhada
- **SETUP-UBUNTU.md**: Instalação local (alternativa)

---

## 🆘 Ajuda

```bash
# Ver todos os containers
docker ps -a

# Ver uso de recursos
docker stats

# Limpar sistema Docker
docker system prune -a
```

