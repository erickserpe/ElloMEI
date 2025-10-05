#!/bin/bash

# Script de refatoração completa: SCF-MEI → ElloMEI
# Data: 05/10/2025

echo "=========================================="
echo "🔄 REFATORAÇÃO: SCF-MEI → ElloMEI"
echo "=========================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Contador de arquivos modificados
COUNT=0

echo "📝 Iniciando refatoração..."
echo ""

# ==========================================
# 1. RENOMEAR ESTRUTURA DE PACOTES JAVA
# ==========================================
echo "${YELLOW}1. Renomeando estrutura de pacotes Java...${NC}"

# Criar nova estrutura de diretórios
mkdir -p src/main/java/br/com/ellomei
mkdir -p src/test/java/br/com/ellomei

# Mover arquivos mantendo estrutura
if [ -d "src/main/java/br/com/scfmei" ]; then
    echo "  → Movendo src/main/java/br/com/scfmei → src/main/java/br/com/ellomei"
    cp -r src/main/java/br/com/scfmei/* src/main/java/br/com/ellomei/
    COUNT=$((COUNT + 1))
fi

if [ -d "src/test/java/br/com/scfmei" ]; then
    echo "  → Movendo src/test/java/br/com/scfmei → src/test/java/br/com/ellomei"
    cp -r src/test/java/br/com/scfmei/* src/test/java/br/com/ellomei/
    COUNT=$((COUNT + 1))
fi

# ==========================================
# 2. SUBSTITUIR PACOTES EM ARQUIVOS JAVA
# ==========================================
echo ""
echo "${YELLOW}2. Substituindo declarações de pacote em arquivos Java...${NC}"

find src/main/java/br/com/ellomei -type f -name "*.java" -exec sed -i 's/package br\.com\.scfmei/package br.com.ellomei/g' {} \;
find src/main/java/br/com/ellomei -type f -name "*.java" -exec sed -i 's/import br\.com\.scfmei/import br.com.ellomei/g' {} \;

find src/test/java/br/com/ellomei -type f -name "*.java" -exec sed -i 's/package br\.com\.scfmei/package br.com.ellomei/g' {} \; 2>/dev/null
find src/test/java/br/com/ellomei -type f -name "*.java" -exec sed -i 's/import br\.com\.scfmei/import br.com.ellomei/g' {} \; 2>/dev/null

echo "  ✅ Pacotes Java atualizados"

# ==========================================
# 3. RENOMEAR CLASSE PRINCIPAL
# ==========================================
echo ""
echo "${YELLOW}3. Renomeando classe principal...${NC}"

if [ -f "src/main/java/br/com/ellomei/ScfMeiApplication.java" ]; then
    mv src/main/java/br/com/ellomei/ScfMeiApplication.java src/main/java/br/com/ellomei/ElloMeiApplication.java
    sed -i 's/class ScfMeiApplication/class ElloMeiApplication/g' src/main/java/br/com/ellomei/ElloMeiApplication.java
    sed -i 's/ScfMeiApplication/ElloMeiApplication/g' src/main/java/br/com/ellomei/ElloMeiApplication.java
    echo "  ✅ ScfMeiApplication.java → ElloMeiApplication.java"
    COUNT=$((COUNT + 1))
fi

# ==========================================
# 4. ATUALIZAR POM.XML
# ==========================================
echo ""
echo "${YELLOW}4. Atualizando pom.xml...${NC}"

sed -i 's/<groupId>br\.com\.scfmei<\/groupId>/<groupId>br.com.ellomei<\/groupId>/g' pom.xml
sed -i 's/<artifactId>SCF-MEI<\/artifactId>/<artifactId>ElloMEI<\/artifactId>/g' pom.xml
sed -i 's/<name>SCF-MEI<\/name>/<name>ElloMEI<\/name>/g' pom.xml
sed -i 's/<description>SCF-MEI<\/description>/<description>ElloMEI - Sistema de Controle Financeiro para MEI<\/description>/g' pom.xml

echo "  ✅ pom.xml atualizado"
COUNT=$((COUNT + 1))

# ==========================================
# 5. ATUALIZAR DOCKER-COMPOSE.YML
# ==========================================
echo ""
echo "${YELLOW}5. Atualizando docker-compose.yml...${NC}"

sed -i 's/scf-mei-mysql/ellomei-mysql/g' docker-compose.yml
sed -i 's/scf-mei-app/ellomei-app/g' docker-compose.yml
sed -i 's/scf-mei-network/ellomei-network/g' docker-compose.yml
sed -i 's/scf-mei-monitor/ellomei-monitor/g' docker-compose.yml

echo "  ✅ docker-compose.yml atualizado"
COUNT=$((COUNT + 1))

# ==========================================
# 6. ATUALIZAR .ENV
# ==========================================
echo ""
echo "${YELLOW}6. Atualizando .env...${NC}"

sed -i 's/scf_mei/ellomei/g' .env
sed -i 's/SCF_MEI/ELLOMEI/g' .env
sed -i 's/LOGGING_LEVEL_BR_COM_SCFMEI/LOGGING_LEVEL_BR_COM_ELLOMEI/g' .env

echo "  ✅ .env atualizado"
COUNT=$((COUNT + 1))

# ==========================================
# 7. ATUALIZAR ARQUIVOS DE PROPERTIES
# ==========================================
echo ""
echo "${YELLOW}7. Atualizando arquivos de properties...${NC}"

find src/main/resources -name "*.properties" -exec sed -i 's/scfmei/ellomei/g' {} \;
find src/main/resources -name "*.properties" -exec sed -i 's/SCF-MEI/ElloMEI/g' {} \;
find src/main/resources -name "*.properties" -exec sed -i 's/SCF MEI/Ello MEI/g' {} \;

echo "  ✅ Properties atualizados"
COUNT=$((COUNT + 1))

# ==========================================
# 8. ATUALIZAR TEMPLATES HTML
# ==========================================
echo ""
echo "${YELLOW}8. Atualizando templates HTML...${NC}"

find src/main/resources/templates -name "*.html" -exec sed -i 's/SCF-MEI/ElloMEI/g' {} \;
find src/main/resources/templates -name "*.html" -exec sed -i 's/SCF MEI/Ello MEI/g' {} \;

echo "  ✅ Templates HTML atualizados"
COUNT=$((COUNT + 1))

# ==========================================
# 9. ATUALIZAR DOCUMENTAÇÃO
# ==========================================
echo ""
echo "${YELLOW}9. Atualizando documentação (.md)...${NC}"

find docs -name "*.md" -exec sed -i 's/SCF-MEI/ElloMEI/g' {} \;
find docs -name "*.md" -exec sed -i 's/SCF MEI/Ello MEI/g' {} \;
find docs -name "*.md" -exec sed -i 's/scf-mei/ellomei/g' {} \;
find docs -name "*.md" -exec sed -i 's/scf_mei/ellomei/g' {} \;
find docs -name "*.md" -exec sed -i 's/br\.com\.scfmei/br.com.ellomei/g' {} \;

sed -i 's/SCF-MEI/ElloMEI/g' README.md
sed -i 's/SCF MEI/Ello MEI/g' README.md
sed -i 's/scf-mei/ellomei/g' README.md
sed -i 's/scf_mei/ellomei/g' README.md

echo "  ✅ Documentação atualizada"
COUNT=$((COUNT + 1))

# ==========================================
# 10. ATUALIZAR SCRIPTS
# ==========================================
echo ""
echo "${YELLOW}10. Atualizando scripts...${NC}"

find scripts -name "*.sh" -exec sed -i 's/scf-mei/ellomei/g' {} \;
find scripts -name "*.sh" -exec sed -i 's/scf_mei/ellomei/g' {} \;
find scripts -name "*.sh" -exec sed -i 's/SCF-MEI/ElloMEI/g' {} \;
find scripts -name "*.sh" -exec sed -i 's/scfmei/ellomei/g' {} \;

echo "  ✅ Scripts atualizados"
COUNT=$((COUNT + 1))

# ==========================================
# 11. ATUALIZAR MONITORING
# ==========================================
echo ""
echo "${YELLOW}11. Atualizando configurações de monitoramento...${NC}"

find monitoring -type f -exec sed -i 's/scf-mei/ellomei/g' {} \;
find monitoring -type f -exec sed -i 's/SCF-MEI/ElloMEI/g' {} \;

echo "  ✅ Monitoramento atualizado"
COUNT=$((COUNT + 1))

# ==========================================
# 12. ATUALIZAR GITHUB WORKFLOWS
# ==========================================
echo ""
echo "${YELLOW}12. Atualizando GitHub workflows...${NC}"

find .github/workflows -name "*.yml" -exec sed -i 's/scf_mei/ellomei/g' {} \; 2>/dev/null
find .github/workflows -name "*.yml" -exec sed -i 's/SCF-MEI/ElloMEI/g' {} \; 2>/dev/null

echo "  ✅ GitHub workflows atualizados"
COUNT=$((COUNT + 1))

# ==========================================
# 13. RENOMEAR ARQUIVO .IML
# ==========================================
echo ""
echo "${YELLOW}13. Renomeando arquivo IntelliJ...${NC}"

if [ -f "SCF-MEI.iml" ]; then
    mv SCF-MEI.iml ElloMEI.iml
    sed -i 's/SCF-MEI/ElloMEI/g' ElloMEI.iml
    echo "  ✅ SCF-MEI.iml → ElloMEI.iml"
    COUNT=$((COUNT + 1))
fi

# ==========================================
# 14. LIMPAR ESTRUTURA ANTIGA
# ==========================================
echo ""
echo "${YELLOW}14. Removendo estrutura antiga...${NC}"

if [ -d "src/main/java/br/com/scfmei" ]; then
    rm -rf src/main/java/br/com/scfmei
    echo "  ✅ Removido src/main/java/br/com/scfmei"
fi

if [ -d "src/test/java/br/com/scfmei" ]; then
    rm -rf src/test/java/br/com/scfmei
    echo "  ✅ Removido src/test/java/br/com/scfmei"
fi

if [ -d "target" ]; then
    rm -rf target
    echo "  ✅ Removido target/ (será recriado no próximo build)"
fi

# ==========================================
# RESUMO
# ==========================================
echo ""
echo "=========================================="
echo "${GREEN}✅ REFATORAÇÃO CONCLUÍDA!${NC}"
echo "=========================================="
echo ""
echo "📊 Estatísticas:"
echo "  • Categorias atualizadas: $COUNT"
echo "  • Pacote Java: br.com.scfmei → br.com.ellomei"
echo "  • Classe principal: ScfMeiApplication → ElloMeiApplication"
echo "  • Nome do projeto: SCF-MEI → ElloMEI"
echo ""
echo "📝 Próximos passos:"
echo "  1. Revisar as mudanças: git status"
echo "  2. Testar a aplicação: ./scripts/docker-start.sh"
echo "  3. Fazer commit: git add -A && git commit -m 'refactor: Renomear projeto de SCF-MEI para ElloMEI'"
echo ""
echo "⚠️  IMPORTANTE:"
echo "  • Atualize o remote do Git se necessário"
echo "  • Reconfigure variáveis de ambiente de produção"
echo "  • Atualize documentação externa"
echo ""

