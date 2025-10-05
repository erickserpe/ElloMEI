#!/bin/bash

# Script para testar envio de email com CSRF token

echo "========================================="
echo "🧪 TESTE DE ENVIO DE EMAIL (COMPLETO)"
echo "========================================="
echo ""

# Email para teste
EMAIL="serpetabordaerickluan@gmail.com"

echo "1️⃣ Verificando se a aplicação está rodando..."
if curl -s http://localhost:8080/login > /dev/null 2>&1; then
    echo "✅ Aplicação está rodando"
else
    echo "❌ Aplicação não está rodando"
    echo "Execute: ./docker-start.sh restart"
    exit 1
fi

echo ""
echo "2️⃣ Obtendo CSRF token..."

# Obter página de recuperação de senha e extrair CSRF token
RESPONSE=$(curl -s -c cookies.txt http://localhost:8080/recuperar-senha)
CSRF_TOKEN=$(echo "$RESPONSE" | grep -oP 'name="_csrf" value="\K[^"]+')

if [ -z "$CSRF_TOKEN" ]; then
    echo "❌ Não foi possível obter CSRF token"
    exit 1
fi

echo "✅ CSRF token obtido: ${CSRF_TOKEN:0:20}..."

echo ""
echo "3️⃣ Enviando solicitação de recuperação de senha para: $EMAIL"

# Fazer requisição POST com CSRF token
HTTP_CODE=$(curl -s -b cookies.txt -c cookies.txt \
    -X POST "http://localhost:8080/recuperar-senha/solicitar" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "_csrf=$CSRF_TOKEN&email=$EMAIL" \
    -w "%{http_code}" \
    -o /dev/null \
    -L)

echo ""
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    echo "✅ Solicitação enviada com sucesso! (HTTP $HTTP_CODE)"
    echo ""
    echo "4️⃣ Aguardando processamento do email..."
    sleep 3
    
    echo ""
    echo "5️⃣ Verificando logs da aplicação..."
    echo ""
    
    # Verificar logs de email
    LOGS=$(docker compose logs app 2>&1 | grep -i "email.*$EMAIL\|recupera.*senha" | tail -10)
    
    if [ -n "$LOGS" ]; then
        echo "$LOGS"
    else
        echo "⚠️  Nenhum log de email encontrado"
    fi
    
    echo ""
    echo "========================================="
    echo "📧 RESULTADO DO TESTE"
    echo "========================================="
    echo ""
    
    # Verificar se há mensagem de sucesso nos logs
    if docker compose logs app 2>&1 | grep -q "Email.*enviado.*$EMAIL"; then
        echo "✅ EMAIL ENVIADO COM SUCESSO!"
        echo ""
        echo "Verifique a caixa de entrada de: $EMAIL"
        echo "Não esqueça de verificar a pasta de SPAM!"
    elif docker compose logs app 2>&1 | grep -q "Erro ao enviar email"; then
        echo "❌ ERRO AO ENVIAR EMAIL"
        echo ""
        echo "Logs de erro:"
        docker compose logs app 2>&1 | grep -A 5 "Erro ao enviar email" | tail -10
    else
        echo "⚠️  STATUS DESCONHECIDO"
        echo ""
        echo "Últimos logs da aplicação:"
        docker compose logs app 2>&1 | tail -20
    fi
    
else
    echo "❌ Erro ao enviar solicitação (HTTP $HTTP_CODE)"
    echo ""
    echo "Últimos logs da aplicação:"
    docker compose logs app 2>&1 | tail -20
fi

# Limpar cookies
rm -f cookies.txt

echo ""
echo "========================================="
echo "📊 CONFIGURAÇÃO DE EMAIL"
echo "========================================="
echo ""
echo "Configurações atuais (.env):"
grep "MAIL_" .env | grep -v "^#"
echo ""
echo "Para verificar se o email está configurado corretamente:"
echo "1. Verifique se MAIL_USERNAME está correto"
echo "2. Verifique se MAIL_PASSWORD é a senha de app do Google (16 caracteres)"
echo "3. Verifique se MAIL_FROM_NAME está configurado"
echo ""

