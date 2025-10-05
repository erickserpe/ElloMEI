#!/bin/bash

# ===================================
# Script para gerar certificado SSL auto-assinado
# ===================================
# Uso:
#   ./generate-ssl-cert.sh
#
# Gera:
#   - keystore.p12 (certificado SSL para Spring Boot)
#   - Configurações para HTTPS
# ===================================

set -e

echo "🔐 Gerando Certificado SSL Auto-Assinado para ElloMEI"
echo "======================================================"
echo ""

# Configurações
KEYSTORE_FILE="keystore.p12"
KEYSTORE_PASSWORD="ellomei2025"
ALIAS="ellomei"
VALIDITY_DAYS=365
DOMAIN="localhost"

# Verificar se keytool está disponível
if ! command -v keytool &> /dev/null; then
    echo "❌ ERRO: keytool não encontrado!"
    echo "   keytool faz parte do JDK. Instale o JDK 17 ou superior."
    exit 1
fi

# Verificar se já existe keystore
if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  Keystore já existe: $KEYSTORE_FILE"
    read -p "   Deseja sobrescrever? (s/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "❌ Operação cancelada."
        exit 0
    fi
    rm -f "$KEYSTORE_FILE"
fi

echo "📝 Configurações:"
echo "   - Arquivo: $KEYSTORE_FILE"
echo "   - Senha: $KEYSTORE_PASSWORD"
echo "   - Alias: $ALIAS"
echo "   - Validade: $VALIDITY_DAYS dias"
echo "   - Domínio: $DOMAIN"
echo ""

echo "🔨 Gerando certificado..."
keytool -genkeypair \
    -alias "$ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore "$KEYSTORE_FILE" \
    -validity "$VALIDITY_DAYS" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEYSTORE_PASSWORD" \
    -dname "CN=$DOMAIN, OU=ElloMEI, O=ElloMEI, L=Sao Paulo, ST=SP, C=BR" \
    -ext "SAN=dns:localhost,dns:ellomei-app,ip:127.0.0.1"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Certificado gerado com sucesso!"
    echo ""
    echo "📋 Informações do certificado:"
    keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$KEYSTORE_PASSWORD" | grep -A 5 "Alias name"
    echo ""
    echo "📁 Arquivo gerado: $KEYSTORE_FILE"
    echo ""
    echo "🔧 Próximos passos:"
    echo ""
    echo "1. Adicione ao .env:"
    echo "   SSL_ENABLED=true"
    echo "   SSL_KEYSTORE_PATH=keystore.p12"
    echo "   SSL_KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD"
    echo "   SSL_KEY_ALIAS=$ALIAS"
    echo ""
    echo "2. Reinicie a aplicação:"
    echo "   ./docker-start.sh restart"
    echo ""
    echo "3. Acesse via HTTPS:"
    echo "   https://localhost:8443"
    echo ""
    echo "⚠️  IMPORTANTE:"
    echo "   - Este é um certificado AUTO-ASSINADO para DESENVOLVIMENTO"
    echo "   - Navegadores mostrarão aviso de segurança (normal)"
    echo "   - Para PRODUÇÃO, use certificado de uma CA confiável (Let's Encrypt)"
    echo ""
else
    echo ""
    echo "❌ Erro ao gerar certificado!"
    exit 1
fi

