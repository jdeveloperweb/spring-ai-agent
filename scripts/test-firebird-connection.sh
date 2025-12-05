#!/bin/bash

# Script para testar a conexão com o Firebird via túnel SSH

set -e

echo "==================================="
echo "Teste de Conexão - Firebird via SSH"
echo "==================================="
echo ""

# Carregar variáveis do .env
if [ ! -f .env ]; then
    echo "❌ Arquivo .env não encontrado!"
    echo "Execute primeiro: ./scripts/setup-firebird-ssh.sh"
    exit 1
fi

source .env

echo "📋 Configuração atual:"
echo "  SSH Host: $SSH_HOST:$SSH_PORT"
echo "  SSH User: $SSH_USER"
echo "  Firebird: $FIREBIRD_REMOTE_HOST:$FIREBIRD_REMOTE_PORT"
echo "  Database: $FIREBIRD_DATABASE_PATH"
echo ""

# Verificar se o container do túnel está rodando
echo "🔍 Verificando container ssh-tunnel..."
if ! docker-compose ps ssh-tunnel | grep -q "Up"; then
    echo "❌ Container ssh-tunnel não está rodando!"
    echo "Iniciando container..."
    docker-compose up -d ssh-tunnel
    echo "⏳ Aguardando 10 segundos para o túnel conectar..."
    sleep 10
fi

# Verificar logs do túnel
echo ""
echo "📝 Últimos logs do túnel SSH:"
echo "---"
docker-compose logs --tail=20 ssh-tunnel
echo "---"
echo ""

# Verificar se a porta está aberta
echo "🔍 Testando porta do túnel (3050)..."
if docker-compose exec ssh-tunnel nc -z localhost 3050 2>/dev/null; then
    echo "✅ Porta 3050 está aberta no túnel!"
else
    echo "❌ Porta 3050 não está acessível no túnel!"
    echo "Verifique os logs acima para mais detalhes."
    exit 1
fi

# Verificar conectividade SSH
echo ""
echo "🔍 Testando conectividade SSH..."
if docker-compose exec ssh-tunnel ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no $SSH_USER@$SSH_HOST "echo 'SSH OK'" 2>/dev/null | grep -q "SSH OK"; then
    echo "✅ Conexão SSH está funcionando!"
else
    echo "⚠️  Não foi possível verificar a conexão SSH diretamente"
    echo "Mas isso pode ser normal se o SSH já está em modo túnel."
fi

# Verificar se a aplicação está rodando
echo ""
echo "🔍 Verificando aplicação..."
if docker-compose ps app | grep -q "Up"; then
    echo "✅ Aplicação está rodando!"
    echo ""
    echo "📝 Logs da aplicação relacionados ao Firebird:"
    echo "---"
    docker-compose logs app | grep -i firebird | tail -20 || echo "Nenhum log do Firebird encontrado ainda."
    docker-compose logs app | grep -i "HikariPool.*firebird" | tail -10 || true
    echo "---"
else
    echo "⚠️  Aplicação não está rodando."
    echo "Para iniciar: docker-compose up -d app"
fi

echo ""
echo "==================================="
echo "Resumo do Teste"
echo "==================================="
echo ""

# Status final
tunnel_status="❓"
if docker-compose ps ssh-tunnel | grep -q "Up"; then
    tunnel_status="✅"
fi

app_status="❓"
if docker-compose ps app | grep -q "Up"; then
    app_status="✅"
fi

echo "Status dos Serviços:"
echo "  SSH Tunnel: $tunnel_status"
echo "  Aplicação:  $app_status"
echo ""

if [ "$tunnel_status" == "✅" ] && [ "$app_status" == "✅" ]; then
    echo "✅ Todos os serviços estão rodando!"
    echo ""
    echo "Para verificar se a conexão com o Firebird está funcionando:"
    echo "  docker-compose logs -f app | grep -i firebird"
else
    echo "⚠️  Alguns serviços não estão rodando corretamente."
    echo ""
    echo "Comandos úteis:"
    echo "  docker-compose up -d          # Iniciar todos os serviços"
    echo "  docker-compose logs -f        # Ver todos os logs"
    echo "  docker-compose restart        # Reiniciar serviços"
fi
