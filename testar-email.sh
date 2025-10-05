#!/bin/bash

# Script para testar envio de email

echo "========================================="
echo "🧪 TESTE DE ENVIO DE EMAIL"
echo "========================================="
echo ""

# Verificar se a aplicação está rodando
echo "1️⃣ Verificando se a aplicação está rodando..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Aplicação está rodando"
else
    echo "❌ Aplicação não está rodando"
    echo "Execute: ./docker-start.sh restart"
    exit 1
fi

echo ""
echo "2️⃣ Digite o email para teste (será enviado um email de recuperação de senha):"
read -p "Email: " EMAIL

if [ -z "$EMAIL" ]; then
    echo "❌ Email não pode ser vazio"
    exit 1
fi

echo ""
echo "3️⃣ Enviando solicitação de recuperação de senha..."

# Fazer requisição POST para recuperar senha
RESPONSE=$(curl -s -X POST "http://localhost:8080/recuperar-senha/solicitar" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "email=$EMAIL" \
    -w "\n%{http_code}" \
    -L)

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

echo ""
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    echo "✅ Solicitação enviada com sucesso!"
    echo ""
    echo "4️⃣ Verificando logs da aplicação..."
    echo ""
    
    # Aguardar 2 segundos para o email ser processado
    sleep 2
    
    # Verificar logs
    docker compose logs app 2>&1 | grep -i "email.*$EMAIL" | tail -5
    
    echo ""
    echo "========================================="
    echo "📧 VERIFIQUE SUA CAIXA DE ENTRADA"
    echo "========================================="
    echo ""
    echo "Se o email foi enviado com sucesso, você verá:"
    echo "✅ Email de recuperação de senha enviado para: $EMAIL"
    echo ""
    echo "Verifique:"
    echo "1. Caixa de entrada do email: $EMAIL"
    echo "2. Pasta de spam/lixo eletrônico"
    echo "3. Logs acima para confirmar envio"
    echo ""
else
    echo "❌ Erro ao enviar solicitação (HTTP $HTTP_CODE)"
    echo ""
    echo "Possíveis causas:"
    echo "1. Email não cadastrado no sistema"
    echo "2. Aplicação não está rodando"
    echo "3. Erro de configuração"
    echo ""
    echo "Logs da aplicação:"
    docker compose logs app 2>&1 | tail -20
fi

echo ""
echo "========================================="
echo "📊 LOGS COMPLETOS DE EMAIL"
echo "========================================="
echo ""
docker compose logs app 2>&1 | grep -i "email" | tail -10

