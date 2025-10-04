# 📊 MONITORAMENTO - PROMETHEUS + GRAFANA

Este documento explica como configurar e usar o monitoramento do SCF-MEI com Prometheus e Grafana.

---

## 📋 **ÍNDICE**

1. [Visão Geral](#-visão-geral)
2. [Arquitetura](#-arquitetura)
3. [Instalação](#-instalação)
4. [Acessar Dashboards](#-acessar-dashboards)
5. [Métricas Disponíveis](#-métricas-disponíveis)
6. [Criar Alertas](#-criar-alertas)
7. [Troubleshooting](#-troubleshooting)

---

## 🤔 **VISÃO GERAL**

O SCF-MEI usa uma stack de monitoramento moderna:

- **Spring Boot Actuator**: Expõe métricas da aplicação
- **Micrometer**: Biblioteca de métricas (formato Prometheus)
- **Prometheus**: Coleta e armazena métricas
- **Grafana**: Visualização de métricas em dashboards

**Benefícios:**
- ✅ Monitoramento em tempo real
- ✅ Histórico de métricas (30 dias)
- ✅ Dashboards customizáveis
- ✅ Alertas configuráveis
- ✅ Análise de performance
- ✅ Detecção de problemas

---

## 🏗️ **ARQUITETURA**

```
┌─────────────────┐
│   SCF-MEI App   │
│  (Spring Boot)  │
│                 │
│  /actuator/     │
│  prometheus     │
└────────┬────────┘
         │ HTTP GET (15s)
         │ Métricas
         ▼
┌─────────────────┐
│   Prometheus    │
│  (Coletor)      │
│                 │
│  Port: 9090     │
└────────┬────────┘
         │ PromQL
         │ Queries
         ▼
┌─────────────────┐
│    Grafana      │
│ (Visualização)  │
│                 │
│  Port: 3000     │
└─────────────────┘
```

**Fluxo:**
1. Spring Boot expõe métricas em `/actuator/prometheus`
2. Prometheus coleta métricas a cada 15 segundos
3. Prometheus armazena métricas (retenção: 30 dias)
4. Grafana consulta Prometheus e exibe dashboards

---

## 🚀 **INSTALAÇÃO**

### **PASSO 1: Iniciar Stack de Monitoramento**

```bash
# Iniciar aplicação + monitoramento
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d

# Ou usar o script (se disponível)
./docker-start.sh start-monitoring
```

**Serviços iniciados:**
- `scf-mei-app` (porta 8080)
- `scf-mei-mysql` (porta 3306)
- `scf-mei-prometheus` (porta 9090)
- `scf-mei-grafana` (porta 3000)

---

### **PASSO 2: Verificar Status**

```bash
# Verificar containers
docker compose ps

# Verificar logs do Prometheus
docker compose logs prometheus

# Verificar logs do Grafana
docker compose logs grafana
```

**Saída esperada:**
```
NAME                  STATUS    PORTS
scf-mei-app           Up        0.0.0.0:8080->8080/tcp
scf-mei-mysql         Up        0.0.0.0:3306->3306/tcp
scf-mei-prometheus    Up        0.0.0.0:9090->9090/tcp
scf-mei-grafana       Up        0.0.0.0:3000->3000/tcp
```

---

## 🌐 **ACESSAR DASHBOARDS**

### **Prometheus**

**URL:** http://localhost:9090

**Funcionalidades:**
- Consultar métricas (PromQL)
- Ver targets (alvos de coleta)
- Ver alertas
- Ver configuração

**Exemplo de query:**
```promql
# Taxa de requisições HTTP
rate(http_server_requests_seconds_count[1m])

# Uso de memória JVM
jvm_memory_used_bytes{area="heap"}

# Conexões ativas do pool
hikaricp_connections_active
```

---

### **Grafana**

**URL:** http://localhost:3000

**Credenciais padrão:**
- **Usuário:** admin
- **Senha:** admin

**⚠️ IMPORTANTE:** Altere a senha no primeiro login!

**Dashboards disponíveis:**
1. **SCF-MEI Overview** - Visão geral da aplicação
   - Uptime
   - Taxa de requisições HTTP
   - Uso de memória JVM
   - Pool de conexões do banco

---

## 📈 **MÉTRICAS DISPONÍVEIS**

### **1. Métricas da JVM**

```promql
# Memória heap usada
jvm_memory_used_bytes{area="heap"}

# Memória heap máxima
jvm_memory_max_bytes{area="heap"}

# Threads ativas
jvm_threads_live_threads

# Garbage Collection
rate(jvm_gc_pause_seconds_count[1m])
```

---

### **2. Métricas HTTP**

```promql
# Taxa de requisições por segundo
rate(http_server_requests_seconds_count[1m])

# Latência média (p50, p95, p99)
histogram_quantile(0.95, http_server_requests_seconds_bucket)

# Requisições por endpoint
sum by (uri) (rate(http_server_requests_seconds_count[1m]))

# Taxa de erros (status 5xx)
rate(http_server_requests_seconds_count{status=~"5.."}[1m])
```

---

### **3. Métricas do Banco de Dados**

```promql
# Conexões ativas
hikaricp_connections_active

# Conexões ociosas
hikaricp_connections_idle

# Tempo de espera por conexão
hikaricp_connections_acquire_seconds

# Timeout de conexões
hikaricp_connections_timeout_total
```

---

### **4. Métricas do Sistema**

```promql
# CPU usage
process_cpu_usage

# Uptime
process_uptime_seconds

# Threads
jvm_threads_live_threads
```

---

## 🔔 **CRIAR ALERTAS**

### **Exemplo: Alerta de Memória Alta**

Crie o arquivo `monitoring/prometheus/rules/alerts.yml`:

```yaml
groups:
  - name: scf-mei-alerts
    interval: 30s
    rules:
      # Alerta: Memória heap > 90%
      - alert: HighMemoryUsage
        expr: |
          (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
        for: 5m
        labels:
          severity: warning
          service: scf-mei
        annotations:
          summary: "Uso de memória alto"
          description: "Memória heap está em {{ $value | humanizePercentage }}"
      
      # Alerta: Taxa de erros > 5%
      - alert: HighErrorRate
        expr: |
          rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
          service: scf-mei
        annotations:
          summary: "Taxa de erros alta"
          description: "{{ $value }} erros por segundo"
      
      # Alerta: Aplicação down
      - alert: ApplicationDown
        expr: up{job="scf-mei-app"} == 0
        for: 1m
        labels:
          severity: critical
          service: scf-mei
        annotations:
          summary: "Aplicação fora do ar"
          description: "SCF-MEI não está respondendo"
```

**Ativar alertas:**

Edite `monitoring/prometheus/prometheus.yml`:

```yaml
rule_files:
  - '/etc/prometheus/rules/*.yml'
```

Reinicie o Prometheus:

```bash
docker compose restart prometheus
```

---

## 🔧 **TROUBLESHOOTING**

### **Prometheus não está coletando métricas**

**Verificar:**

1. Endpoint de métricas está acessível:
```bash
curl http://localhost:8080/actuator/prometheus
```

2. Prometheus consegue acessar a aplicação:
```bash
docker compose logs prometheus | grep "scf-mei-app"
```

3. Verificar targets no Prometheus:
   - Acessar: http://localhost:9090/targets
   - Status deve ser "UP"

**Solução:**
- Verificar se aplicação está rodando
- Verificar rede Docker
- Verificar configuração do Prometheus

---

### **Grafana não mostra dados**

**Verificar:**

1. Datasource está configurado:
   - Acessar: http://localhost:3000/datasources
   - Prometheus deve estar listado

2. Testar conexão:
   - Clicar em "Test" no datasource
   - Deve retornar "Data source is working"

3. Verificar query no dashboard:
   - Editar painel
   - Verificar se query retorna dados

**Solução:**
- Recriar datasource
- Verificar URL do Prometheus
- Verificar se Prometheus tem dados

---

### **Métricas não aparecem**

**Causa:** Actuator não está expondo métricas.

**Solução:**

Verificar `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.enable.jvm=true
```

Reiniciar aplicação:

```bash
./docker-start.sh restart
```

---

## 📚 **RECURSOS ÚTEIS**

### **Documentação:**
- Prometheus: https://prometheus.io/docs/
- Grafana: https://grafana.com/docs/
- Micrometer: https://micrometer.io/docs/
- Spring Boot Actuator: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

### **Dashboards Prontos:**
- Grafana Dashboards: https://grafana.com/grafana/dashboards/
- Spring Boot Dashboard: https://grafana.com/grafana/dashboards/12900

### **PromQL:**
- Guia de PromQL: https://prometheus.io/docs/prometheus/latest/querying/basics/
- Exemplos: https://prometheus.io/docs/prometheus/latest/querying/examples/

---

## ✅ **CHECKLIST**

### **Desenvolvimento:**
- [ ] Prometheus rodando (http://localhost:9090)
- [ ] Grafana rodando (http://localhost:3000)
- [ ] Métricas sendo coletadas
- [ ] Dashboard "SCF-MEI Overview" funcionando
- [ ] Senha do Grafana alterada

### **Produção:**
- [ ] Prometheus com autenticação
- [ ] Grafana com autenticação forte
- [ ] HTTPS configurado
- [ ] Alertas configurados
- [ ] Notificações configuradas (email, Slack, etc.)
- [ ] Backup de dashboards
- [ ] Retenção de dados configurada

---

## 🎉 **RESUMO**

**Iniciar monitoramento:**
```bash
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
```

**Acessar:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

**Parar monitoramento:**
```bash
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml down
```

---

**Dúvidas?** Consulte a documentação oficial do Prometheus e Grafana.

