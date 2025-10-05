# 📚 Documentação do ElloMEI

Bem-vindo à documentação completa do **Sistema de Controle Financeiro para MEI**.

---

## 📋 Índice Geral

### 🚀 [Setup - Configuração Inicial](./setup/)
Guias para configurar e iniciar o projeto.

- **[Quick Start](./setup/quick-start.md)** - Início rápido em 3 passos
- **[Docker](./setup/docker.md)** - Configuração completa com Docker
- **[Docker Optimization](./setup/docker-optimization.md)** - Otimizações para produção
- **[Database](./setup/database.md)** - Guia do banco de dados
- **[Profiles](./setup/profiles.md)** - Perfis de desenvolvimento e produção
- **[Email Configuration](./setup/email-configuration.md)** - Configuração de email (Google Workspace)
- **[SSL/HTTPS](./setup/ssl-https.md)** - Configuração de certificados SSL
- **[Mercado Pago](./setup/mercadopago.md)** - Integração com Mercado Pago

---

### ⚙️ [Features - Funcionalidades](./features/)
Documentação das funcionalidades avançadas do sistema.

- **[Backup](./features/backup.md)** - Sistema de backup automático
- **[CI/CD](./features/cicd.md)** - Pipeline de integração e deploy contínuo
- **[Monitoring](./features/monitoring.md)** - Monitoramento com Prometheus e Grafana
- **[Rate Limiting](./features/rate-limiting.md)** - Proteção contra abuso
- **[Flyway Migrations](./features/flyway-migrations.md)** - Migrações de banco de dados
- **[Payment System](./features/payment-system.md)** - Sistema de pagamentos
- **[SaaS Implementation](./features/saas-implementation.md)** - Implementação SaaS multi-tenant
- **[Advanced Features](./features/advanced-features.md)** - Recursos avançados

---

### 📖 [Guides - Guias de Uso](./guides/)
Guias práticos para desenvolvedores e usuários.

- **[Testing Guide](./guides/testing.md)** - Guia completo de testes
- **[Validations](./guides/validations.md)** - Validações implementadas

---

### 🔧 [Development - Desenvolvimento](./development/)
Documentação para desenvolvedores.

- **[Features Summary](./development/features-summary.md)** - Resumo de funcionalidades
- **[Cleanup Report](./development/cleanup-report.md)** - Relatório de limpeza
- **[Tests Report](./development/tests-report.md)** - Relatório de testes

---

## 🎯 Início Rápido

### Para Desenvolvedores

1. **Clone o repositório**
   ```bash
   git clone <repository-url>
   cd ElloMEI
   ```

2. **Configure o ambiente**
   ```bash
   cp .env.example .env
   # Edite o .env com suas configurações
   ```

3. **Inicie com Docker**
   ```bash
   docker compose up -d
   ```

4. **Acesse a aplicação**
   - URL: http://localhost:8080
   - Documentação completa: [Quick Start](./setup/quick-start.md)

---

## 📊 Estrutura do Projeto

```
ElloMEI/
├── docs/                          # 📚 Documentação
│   ├── setup/                     # Configuração inicial
│   ├── features/                  # Funcionalidades
│   ├── guides/                    # Guias de uso
│   └── development/               # Desenvolvimento
├── src/
│   ├── main/
│   │   ├── java/                  # Código Java
│   │   └── resources/             # Resources (templates, configs)
│   └── test/                      # Testes
├── docker-compose.yml             # Docker Compose
├── Dockerfile                     # Dockerfile otimizado
├── pom.xml                        # Maven dependencies
└── README.md                      # README principal
```

---

## 🔑 Principais Funcionalidades

### 💰 Gestão Financeira
- ✅ Controle de receitas e despesas
- ✅ Categorização de lançamentos
- ✅ Contas bancárias múltiplas
- ✅ Relatórios financeiros
- ✅ Dashboard analítico

### 👥 Multi-Tenant (SaaS)
- ✅ Isolamento de dados por usuário
- ✅ Planos FREE e PRO
- ✅ Limites por plano
- ✅ Upgrade/downgrade de planos

### 💳 Pagamentos
- ✅ Integração com Mercado Pago
- ✅ Webhooks automáticos
- ✅ Histórico de pagamentos
- ✅ Retry automático de falhas

### 📧 Comunicação
- ✅ Email de boas-vindas
- ✅ Recuperação de senha
- ✅ Notificações de pagamento
- ✅ Templates HTML profissionais

### 🔒 Segurança
- ✅ Autenticação Spring Security
- ✅ Validação de senha forte
- ✅ Validação de email (DNS/MX)
- ✅ Rate limiting
- ✅ SSL/HTTPS

### 📊 Monitoramento
- ✅ Prometheus metrics
- ✅ Grafana dashboards
- ✅ Health checks
- ✅ Logs estruturados

### 🔄 DevOps
- ✅ Docker containerizado
- ✅ CI/CD com GitHub Actions
- ✅ Backup automático
- ✅ Migrações Flyway

---

## 🛠️ Tecnologias

### Backend
- **Java 17**
- **Spring Boot 3.5.5**
- **Spring Security**
- **Spring Data JPA**
- **Hibernate**

### Frontend
- **Thymeleaf**
- **Bootstrap 5**
- **Chart.js**
- **JavaScript**

### Banco de Dados
- **MySQL 8.0**
- **Flyway Migrations**

### Infraestrutura
- **Docker & Docker Compose**
- **Prometheus**
- **Grafana**

### Integrações
- **Mercado Pago API**
- **Google Workspace (Email)**

---

## 📖 Guias por Categoria

### 🎯 Quero começar a usar
→ [Quick Start](./setup/quick-start.md)

### 🐳 Quero configurar Docker
→ [Docker Setup](./setup/docker.md)

### 📧 Quero configurar email
→ [Email Configuration](./setup/email-configuration.md)

### 💳 Quero integrar pagamentos
→ [Mercado Pago Setup](./setup/mercadopago.md)

### 🔒 Quero configurar HTTPS
→ [SSL/HTTPS Setup](./setup/ssl-https.md)

### 📊 Quero monitorar a aplicação
→ [Monitoring Guide](./features/monitoring.md)

### 🧪 Quero testar a aplicação
→ [Testing Guide](./guides/testing.md)

### 🚀 Quero fazer deploy
→ [CI/CD Guide](./features/cicd.md)

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📞 Suporte

- **Documentação:** Você está aqui! 📚
- **Issues:** Use o GitHub Issues para reportar bugs
- **Email:** Configure seguindo [este guia](./setup/email-configuration.md)

---

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

## 🎉 Agradecimentos

Desenvolvido com ❤️ para Microempreendedores Individuais (MEI).

---

**Última atualização:** 05/10/2025  
**Versão da documentação:** 2.0

