# 🔄 CI/CD - INTEGRAÇÃO E DEPLOY CONTÍNUO

Este documento explica o pipeline de CI/CD do ElloMEI usando GitHub Actions.

---

## 📋 **ÍNDICE**

1. [Visão Geral](#-visão-geral)
2. [Workflows Configurados](#-workflows-configurados)
3. [Como Funciona](#-como-funciona)
4. [Configurar Secrets](#-configurar-secrets)
5. [Deploy Manual](#-deploy-manual)
6. [Troubleshooting](#-troubleshooting)

---

## 🤔 **VISÃO GERAL**

O ElloMEI usa **GitHub Actions** para automação de CI/CD:

**CI (Continuous Integration):**
- ✅ Build automático
- ✅ Testes automatizados
- ✅ Análise de segurança
- ✅ Verificação de qualidade

**CD (Continuous Deployment):**
- ✅ Build de imagem Docker
- ✅ Push para registry
- ✅ Deploy automático (opcional)

---

## 🏗️ **WORKFLOWS CONFIGURADOS**

### **1. CI - Build and Test** (`.github/workflows/ci.yml`)

**Quando executa:**
- Push para `main` ou `develop`
- Pull Requests

**O que faz:**
1. ✅ Checkout do código
2. ✅ Setup Java 17
3. ✅ Cache de dependências Maven
4. ✅ Build com Maven
5. ✅ Executar testes
6. ✅ Gerar relatório de cobertura
7. ✅ Upload de artefatos (JAR)
8. ✅ Scan de segurança (Trivy)

**Duração:** ~5-10 minutos

---

### **2. Docker Build and Push** (`.github/workflows/docker.yml`)

**Quando executa:**
- Push para `main`
- Tags de release (`v*.*.*`)
- Manualmente (workflow_dispatch)

**O que faz:**
1. ✅ Checkout do código
2. ✅ Setup Docker Buildx
3. ✅ Login no GitHub Container Registry
4. ✅ Build da imagem Docker
5. ✅ Push para registry
6. ✅ Scan de segurança da imagem

**Duração:** ~10-15 minutos

**Imagem gerada:**
```
ghcr.io/<seu-usuario>/ellomei:latest
ghcr.io/<seu-usuario>/ellomei:v1.0.0
```

---

## 🔄 **COMO FUNCIONA**

### **Fluxo de Desenvolvimento**

```
┌─────────────────┐
│  Desenvolvedor  │
│   faz commit    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   GitHub Push   │
│  (main/develop) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  GitHub Actions │
│   CI Workflow   │
│                 │
│  1. Build       │
│  2. Tests       │
│  3. Security    │
└────────┬────────┘
         │
         ├─ ✅ Sucesso
         │
         ▼
┌─────────────────┐
│  Docker Build   │
│   Workflow      │
│                 │
│  1. Build image │
│  2. Push image  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Deploy (manual)│
│  ou automático  │
└─────────────────┘
```

---

### **Exemplo: Push para Main**

1. **Desenvolvedor faz push:**
```bash
git add .
git commit -m "feat: Nova funcionalidade"
git push origin main
```

2. **GitHub Actions inicia CI:**
   - Build do projeto
   - Executa testes
   - Scan de segurança

3. **Se CI passar, inicia Docker Build:**
   - Constrói imagem Docker
   - Faz push para registry
   - Scan de segurança da imagem

4. **Imagem disponível para deploy:**
```bash
docker pull ghcr.io/<seu-usuario>/ellomei:latest
```

---

## 🔐 **CONFIGURAR SECRETS**

Alguns workflows precisam de secrets configurados no GitHub.

### **Acessar Secrets:**

1. Vá para o repositório no GitHub
2. Clique em "Settings"
3. Clique em "Secrets and variables" → "Actions"
4. Clique em "New repository secret"

---

### **Secrets Necessários:**

#### **Para Docker Build (opcional):**

Se quiser usar Docker Hub em vez de GitHub Container Registry:

```
DOCKER_USERNAME=seu-usuario-dockerhub
DOCKER_PASSWORD=sua-senha-dockerhub
```

#### **Para Deploy Automático (opcional):**

```
SSH_PRIVATE_KEY=sua-chave-ssh-privada
SERVER_HOST=seu-servidor.com
SERVER_USER=usuario-ssh
```

#### **Para SonarQube (opcional):**

```
SONAR_TOKEN=seu-token-sonarqube
SONAR_HOST_URL=https://sonarcloud.io
```

---

## 🚀 **DEPLOY MANUAL**

### **PASSO 1: Pull da Imagem**

```bash
# Login no GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull da imagem
docker pull ghcr.io/<seu-usuario>/ellomei:latest
```

---

### **PASSO 2: Parar Aplicação Atual**

```bash
./docker-start.sh stop
```

---

### **PASSO 3: Atualizar docker-compose.yml**

Edite `docker-compose.yml`:

```yaml
services:
  app:
    # Comentar build local
    # build:
    #   context: .
    #   dockerfile: Dockerfile
    
    # Usar imagem do registry
    image: ghcr.io/<seu-usuario>/ellomei:latest
```

---

### **PASSO 4: Iniciar Nova Versão**

```bash
./docker-start.sh start
```

---

### **PASSO 5: Verificar Deploy**

```bash
# Verificar logs
docker compose logs app

# Verificar health
curl http://localhost:8080/actuator/health
```

---

## 🔧 **TROUBLESHOOTING**

### **CI falha no build**

**Erro:**
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**Solução:**
1. Verificar erros de compilação localmente:
```bash
mvn clean compile
```

2. Corrigir erros
3. Fazer commit e push novamente

---

### **CI falha nos testes**

**Erro:**
```
Tests run: 10, Failures: 2, Errors: 0, Skipped: 0
```

**Solução:**
1. Executar testes localmente:
```bash
mvn test
```

2. Corrigir testes que falharam
3. Fazer commit e push novamente

---

### **Docker build falha**

**Erro:**
```
ERROR: failed to solve: process "/bin/sh -c mvn clean package" did not complete successfully
```

**Solução:**
1. Testar build Docker localmente:
```bash
docker build -t ellomei-test .
```

2. Verificar logs de erro
3. Corrigir Dockerfile se necessário

---

### **Não consigo fazer pull da imagem**

**Erro:**
```
Error response from daemon: pull access denied
```

**Solução:**
1. Fazer login no registry:
```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```

2. Verificar se imagem existe:
   - Acessar: https://github.com/<seu-usuario>/ellomei/pkgs/container/ellomei

3. Verificar permissões do package

---

## 📚 **RECURSOS ÚTEIS**

### **Documentação:**
- GitHub Actions: https://docs.github.com/en/actions
- Docker Build: https://docs.docker.com/build/ci/github-actions/
- Trivy Security Scanner: https://github.com/aquasecurity/trivy

### **Exemplos:**
- GitHub Actions Marketplace: https://github.com/marketplace?type=actions
- Awesome Actions: https://github.com/sdras/awesome-actions

---

## ✅ **CHECKLIST**

### **Configuração Inicial:**
- [ ] Workflows criados (`.github/workflows/`)
- [ ] Repositório no GitHub
- [ ] Secrets configurados (se necessário)
- [ ] Primeiro push testado

### **Para Cada Release:**
- [ ] Testes passando localmente
- [ ] Commit com mensagem descritiva
- [ ] Push para branch correta
- [ ] CI passou com sucesso
- [ ] Imagem Docker gerada
- [ ] Deploy realizado
- [ ] Aplicação funcionando

---

## 🎉 **RESUMO**

**Ver status dos workflows:**
- Acessar: https://github.com/<seu-usuario>/ellomei/actions

**Executar workflow manualmente:**
1. Ir em "Actions"
2. Selecionar workflow
3. Clicar em "Run workflow"

**Fazer release:**
```bash
# Criar tag
git tag -a v1.0.0 -m "Release v1.0.0"

# Push da tag
git push origin v1.0.0

# GitHub Actions irá:
# 1. Executar CI
# 2. Build Docker image
# 3. Push com tag v1.0.0
```

---

**Dúvidas?** Consulte a documentação do GitHub Actions.

