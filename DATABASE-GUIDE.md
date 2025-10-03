# 🗄️ SCF-MEI - Guia do Banco de Dados

**Como funciona o armazenamento de dados com Docker**

---

## ✅ **RESPOSTA RÁPIDA**

**SIM, seus dados são guardados permanentemente!**

O Docker usa **volumes persistentes** que mantêm seus dados mesmo quando você para, reinicia ou atualiza os containers.

---

## 📦 **1. ONDE OS DADOS FICAM GUARDADOS**

### Volume Docker Persistente

```yaml
# docker-compose.yml
volumes:
  mysql_data:
    driver: local
```

### Localização Física

```
/var/lib/docker/volumes/scf-mei_mysql_data/_data
```

Este diretório contém **TODOS os dados do MySQL**, incluindo:
- ✅ Tabelas do banco de dados
- ✅ Índices
- ✅ Configurações
- ✅ Logs de transações

---

## 🗂️ **2. ESTRUTURA DO BANCO DE DADOS**

### Tabelas Criadas

Seu banco de dados tem **6 tabelas**:

| Tabela | Tamanho | Descrição |
|--------|---------|-----------|
| **lancamento** | 0.08 MB | Lançamentos financeiros (receitas/despesas) |
| **categoria_despesa** | 0.03 MB | Categorias de despesas |
| **comprovante** | 0.03 MB | Comprovantes anexados |
| **conta** | 0.03 MB | Contas bancárias |
| **contato** | 0.03 MB | Clientes e fornecedores |
| **usuario** | 0.02 MB | Usuários do sistema |

### Verificar Tabelas

```bash
# Listar todas as tabelas
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "SHOW TABLES;"

# Ver estrutura de uma tabela
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "DESCRIBE lancamento;"

# Contar registros
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "SELECT COUNT(*) FROM lancamento;"
```

---

## 🔄 **3. PERSISTÊNCIA DE DADOS**

### ✅ Dados PERMANECEM quando você:

```bash
# Parar containers
./docker-start.sh stop
# ✅ Dados mantidos no volume

# Reiniciar containers
./docker-start.sh restart
# ✅ Dados carregados do volume

# Reiniciar o computador
sudo reboot
# ✅ Dados mantidos no volume

# Atualizar a aplicação
docker compose up --build -d
# ✅ Dados mantidos no volume

# Parar sem remover volumes
docker compose down
# ✅ Dados mantidos no volume
```

### ❌ Dados SÃO PERDIDOS quando você:

```bash
# Limpar tudo (inclui volumes)
./docker-start.sh clean
# ❌ REMOVE TUDO, incluindo dados!

# Parar e remover volumes
docker compose down -v
# ❌ Flag -v remove volumes!

# Remover volume manualmente
docker volume rm scf-mei_mysql_data
# ❌ Remove o volume com os dados!
```

---

## 🧪 **4. TESTE DE PERSISTÊNCIA**

Vamos fazer um teste prático:

### Passo 1: Criar um registro de teste

```bash
# Inserir um registro de teste
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "
INSERT INTO categoria_despesa (nome, descricao) 
VALUES ('TESTE_PERSISTENCIA', 'Teste de persistência de dados');
"

# Verificar que foi criado
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "
SELECT * FROM categoria_despesa WHERE nome = 'TESTE_PERSISTENCIA';
"
```

### Passo 2: Parar os containers

```bash
./docker-start.sh stop
```

### Passo 3: Iniciar novamente

```bash
./docker-start.sh start
```

### Passo 4: Verificar se o dado ainda existe

```bash
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "
SELECT * FROM categoria_despesa WHERE nome = 'TESTE_PERSISTENCIA';
"
```

**Resultado:** O registro ainda está lá! ✅

### Passo 5: Limpar o teste

```bash
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "
DELETE FROM categoria_despesa WHERE nome = 'TESTE_PERSISTENCIA';
"
```

---

## 💾 **5. BACKUP DOS DADOS**

### Por que fazer backup?

Mesmo com volumes persistentes, é importante fazer backups para:
- ✅ Proteger contra erros humanos
- ✅ Proteger contra falhas de hardware
- ✅ Migrar para outro servidor
- ✅ Testar em outro ambiente

### Como fazer backup

```bash
# Criar backup
./backup.sh

# Resultado:
# ✅ Arquivo criado: backups/scf_mei_backup_20251003_143022.sql.gz
```

### Como restaurar backup

```bash
# Listar backups disponíveis
./backup.sh --list

# Restaurar um backup específico
./backup.sh --restore scf_mei_backup_20251003_143022.sql.gz
```

---

## 📊 **6. MONITORAMENTO DO BANCO**

### Ver tamanho do banco de dados

```bash
# Tamanho total do banco
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'scf_mei_db'
GROUP BY table_schema;
"
```

### Ver tamanho de cada tabela

```bash
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)',
    table_rows AS 'Rows'
FROM information_schema.tables
WHERE table_schema = 'scf_mei_db'
ORDER BY (data_length + index_length) DESC;
"
```

### Ver número de registros

```bash
# Contar registros em todas as tabelas
docker exec scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db -e "
SELECT 'usuario' AS tabela, COUNT(*) AS total FROM usuario
UNION ALL
SELECT 'conta', COUNT(*) FROM conta
UNION ALL
SELECT 'categoria_despesa', COUNT(*) FROM categoria_despesa
UNION ALL
SELECT 'contato', COUNT(*) FROM contato
UNION ALL
SELECT 'lancamento', COUNT(*) FROM lancamento
UNION ALL
SELECT 'comprovante', COUNT(*) FROM comprovante;
"
```

---

## 🔍 **7. ACESSAR O BANCO DE DADOS**

### Via linha de comando

```bash
# Acessar MySQL interativo
docker exec -it scf-mei-mysql mysql -u scf_user -p5522 scf_mei_db

# Agora você pode executar SQL diretamente:
mysql> SHOW TABLES;
mysql> SELECT * FROM usuario;
mysql> exit;
```

### Via ferramenta gráfica (MySQL Workbench, DBeaver, etc.)

**Configurações de conexão:**
- **Host:** `localhost`
- **Porta:** `3307` (porta externa)
- **Usuário:** `scf_user`
- **Senha:** `5522`
- **Database:** `scf_mei_db`

### Via aplicação Spring Boot

A aplicação se conecta automaticamente usando as configurações do `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:mysql}:3306/scf_mei_db
spring.datasource.username=scf_user
spring.datasource.password=5522
```

---

## 🛡️ **8. SEGURANÇA DOS DADOS**

### Boas Práticas

#### 1. Fazer backups regulares

```bash
# Backup manual
./backup.sh

# Backup automático (cron)
# Adicionar ao crontab:
0 2 * * * cd /caminho/para/SCF-MEI && ./backup.sh
```

#### 2. Testar restauração

```bash
# Periodicamente, teste se consegue restaurar
./backup.sh --restore <backup_mais_recente>
```

#### 3. Guardar backups em local seguro

```bash
# Copiar backups para outro local
cp -r backups/ /mnt/backup_externo/scf-mei/

# Ou enviar para nuvem
# rsync -av backups/ usuario@servidor:/backups/scf-mei/
```

#### 4. Alterar senhas em produção

```bash
# Editar .env com senhas fortes
MYSQL_PASSWORD=senha_forte_aqui_123!@#
MYSQL_ROOT_PASSWORD=outra_senha_forte_456!@#
```

---

## 🔄 **9. MIGRAÇÃO DE DADOS**

### Migrar para outro computador

```bash
# No computador antigo:
# 1. Criar backup
./backup.sh

# 2. Copiar backup para novo computador
scp backups/scf_mei_backup_*.sql.gz usuario@novo-pc:/tmp/

# No computador novo:
# 1. Clonar repositório
git clone https://github.com/erickserpe/SCF-MEI.git
cd SCF-MEI

# 2. Iniciar ambiente
./docker-start.sh

# 3. Restaurar backup
./backup.sh --restore /tmp/scf_mei_backup_*.sql.gz

# 4. Reiniciar aplicação
./docker-start.sh restart
```

---

## 📁 **10. ARQUIVOS ENVIADOS (UPLOADS)**

Além do banco de dados, a aplicação também guarda arquivos enviados:

### Localização

```
/home/es_luan/IdeaProjects/SCF-MEI/uploads/
```

### Persistência

Os arquivos em `uploads/` são guardados **diretamente no seu computador**, não no Docker.

```yaml
# docker-compose.yml
volumes:
  - ./uploads:/app/uploads
```

Isso significa:
- ✅ Arquivos persistem mesmo parando containers
- ✅ Você pode acessar diretamente pelo sistema de arquivos
- ✅ Fácil fazer backup: `tar -czf uploads_backup.tar.gz uploads/`

### Backup de uploads

```bash
# Criar backup dos uploads
tar -czf uploads_backup_$(date +%Y%m%d_%H%M%S).tar.gz uploads/

# Restaurar uploads
tar -xzf uploads_backup_20251003_143022.tar.gz
```

---

## ❓ **11. PERGUNTAS FREQUENTES**

### Se eu parar o Docker, perco os dados?
**NÃO.** Os dados ficam no volume persistente.

### Se eu reiniciar o computador, perco os dados?
**NÃO.** Os dados ficam no volume persistente.

### Se eu atualizar a aplicação, perco os dados?
**NÃO.** Os dados ficam no volume persistente.

### Quando eu perco os dados?
Apenas quando você **explicitamente remove o volume**:
- `./docker-start.sh clean`
- `docker compose down -v`
- `docker volume rm scf-mei_mysql_data`

### Como garantir que nunca vou perder dados?
1. Fazer backups regulares: `./backup.sh`
2. Guardar backups em local seguro
3. Testar restauração periodicamente
4. Nunca usar `./docker-start.sh clean` sem backup

### Posso acessar o banco de dados de fora do Docker?
**SIM.** Use a porta `3307`:
- Host: `localhost`
- Porta: `3307`
- Usuário: `scf_user`
- Senha: `5522`

---

## 📝 **RESUMO**

✅ **Dados são guardados permanentemente** em volume Docker  
✅ **Localização:** `/var/lib/docker/volumes/scf-mei_mysql_data/_data`  
✅ **6 tabelas** criadas automaticamente pelo Hibernate  
✅ **Persistem** ao parar/reiniciar containers  
✅ **Backup fácil** com `./backup.sh`  
✅ **Restauração fácil** com `./backup.sh --restore`  
✅ **Uploads** guardados em `./uploads/`  
✅ **Acesso externo** via porta `3307`  

**Seus dados estão seguros!** 🔒

---

**Criado em:** 2025-10-03  
**Versão:** 1.0

