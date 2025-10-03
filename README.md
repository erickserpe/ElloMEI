# 💰 SCF-MEI - Sistema de Controle Financeiro para MEI

Sistema completo de controle financeiro desenvolvido em **Spring Boot** para Microempreendedores Individuais (MEI).

---

## 🚀 Início Rápido

### Pré-requisito
- Docker instalado e rodando

### Iniciar o Sistema

```bash
chmod +x docker-start.sh
./docker-start.sh
```

Aguarde a mensagem de sucesso e acesse:
```
http://localhost:8080
```

**Pronto! Tudo está rodando em Docker.** 🎉

---

## 📋 Funcionalidades

- ✅ **Gestão de Lançamentos** - Receitas e despesas
- ✅ **Categorização** - Organize suas finanças
- ✅ **Contas Bancárias** - Múltiplas contas
- ✅ **Contatos** - Clientes e fornecedores
- ✅ **Comprovantes** - Upload de documentos
- ✅ **Relatórios** - Análise financeira
- ✅ **Multi-usuário** - Sistema de autenticação

---

## 🛠️ Tecnologias

### Backend
- **Java 17** - OpenJDK
- **Spring Boot 3.5.5** - Framework principal
- **Spring Data JPA** - Persistência
- **Hibernate** - ORM
- **MySQL 8.0** - Banco de dados
- **Maven 3.9** - Gerenciamento de dependências

### Infraestrutura
- **Docker** - Containerização
- **Docker Compose** - Orquestração
- **HikariCP** - Connection pooling

---

## 📚 Documentação

- **[Guia Rápido](QUICK-START.md)** - Comandos essenciais
- **[Configuração Docker Completa](DOCKER-SETUP.md)** - Guia detalhado
- **[Recursos Avançados](ADVANCED-FEATURES.md)** - Backup, variáveis de ambiente, boas práticas
- **[Relatório de Testes](LIGHTWEIGHT_TESTS_FINAL_REPORT.md)** - Testes implementados

---

## 🎯 Comandos Úteis

### Gerenciamento do Ambiente

```bash
# Ver status
./docker-start.sh status

# Ver logs em tempo real
./docker-start.sh logs

# Reiniciar
./docker-start.sh restart

# Parar
./docker-start.sh stop

# Limpar tudo
./docker-start.sh clean
```

### Backup e Restauração

```bash
# Criar backup
./backup.sh

# Listar backups
./backup.sh --list

# Restaurar backup
./backup.sh --restore <arquivo>
```

### Comandos Docker Diretos

```bash
# Ver status
docker compose ps

# Ver logs
docker compose logs -f app

# Acessar MySQL
docker exec -it scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db

# Parar tudo
docker compose down
```

---

## 🗄️ Estrutura do Banco de Dados

- **usuario** - Usuários do sistema
- **categoria_despesa** - Categorias de despesas
- **conta** - Contas bancárias
- **contato** - Clientes e fornecedores
- **lancamento** - Lançamentos financeiros
- **comprovante** - Comprovantes anexados

---

## 🔧 Configuração

### Portas
- **8080** - Aplicação Spring Boot
- **3307** - MySQL (externa)

### Credenciais MySQL
- **Database**: `scf_mei_db`
- **Usuário**: `scf_user`
- **Senha**: `5522`
- **Root Password**: `root_password`

### Volumes Docker
- **mysql_data** - Dados persistentes do MySQL
- **./uploads** - Arquivos enviados pela aplicação

---

## 🧪 Testes

```bash
# Rodar testes
docker compose run --rm app mvn test

# Rodar testes específicos
docker compose run --rm app mvn test -Dtest=NomeDoTeste
```

---

## 📦 Build Manual

Se precisar fazer build fora do Docker:

```bash
# Compilar
mvn clean package -DskipTests

# Rodar localmente (requer Java 17 e MySQL)
java -jar target/SCF-MEI-0.0.1-SNAPSHOT.jar
```

---

## 🐛 Troubleshooting

### Porta 3307 já em uso
```bash
# Parar MySQL local
sudo systemctl stop mysql

# Ou mudar a porta no docker-compose.yml
```

### Containers não iniciam
```bash
# Ver logs detalhados
docker compose logs

# Limpar tudo
docker compose down -v
docker system prune -f
./docker-start.sh
```

### Erro de conexão com banco
```bash
# Verificar se MySQL está saudável
docker compose ps

# Reiniciar apenas o MySQL
docker compose restart mysql
```

---

## 📝 Desenvolvimento

### Estrutura do Projeto
```
SCF-MEI/
├── src/
│   ├── main/
│   │   ├── java/br/com/scfmei/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/
│   │       ├── static/
│   │       └── templates/
│   └── test/
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

### Hot Reload (Desenvolvimento)
Para desenvolvimento com hot reload, monte o código fonte:

```yaml
# Adicione ao docker-compose.yml (serviço app)
volumes:
  - ./src:/app/src
  - ./pom.xml:/app/pom.xml
```

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto é de uso educacional.

---

## 👨‍💻 Autor

Desenvolvido para auxiliar Microempreendedores Individuais (MEI) no controle financeiro de seus negócios.

---

## 🆘 Suporte

Encontrou algum problema? 

1. Verifique a [documentação completa](DOCKER-SETUP.md)
2. Veja os logs: `docker compose logs -f`
3. Abra uma issue no repositório

---

**Feito com ❤️ usando Spring Boot e Docker**

