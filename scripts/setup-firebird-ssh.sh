#!/bin/bash

# Script de configuração do túnel SSH para Firebird
# Este script ajuda a configurar as variáveis de ambiente necessárias

set -e

echo "==================================="
echo "Setup do Túnel SSH para Firebird"
echo "==================================="
echo ""

# Verificar se .env existe
if [ -f .env ]; then
    echo "⚠️  Arquivo .env já existe!"
    read -p "Deseja sobrescrever? (s/N): " overwrite
    if [[ ! $overwrite =~ ^[Ss]$ ]]; then
        echo "Abortado."
        exit 0
    fi
fi

# Copiar .env.example
echo "📋 Criando arquivo .env..."
cp .env.example .env

echo ""
echo "📝 Por favor, forneça as informações do SSH:"
echo ""

# SSH Host
read -p "SSH Host (ex: servidor.com): " ssh_host
sed -i "s/SSH_HOST=.*/SSH_HOST=$ssh_host/" .env

# SSH Port
read -p "SSH Port [22]: " ssh_port
ssh_port=${ssh_port:-22}
sed -i "s/SSH_PORT=.*/SSH_PORT=$ssh_port/" .env

# SSH User
read -p "SSH User: " ssh_user
sed -i "s/SSH_USER=.*/SSH_USER=$ssh_user/" .env

# SSH Private Key
echo ""
echo "Para a chave SSH, você tem duas opções:"
echo "1. Usar chave existente"
echo "2. Gerar nova chave"
read -p "Escolha (1/2): " key_option

if [ "$key_option" == "2" ]; then
    echo "🔑 Gerando nova chave SSH..."
    ssh-keygen -t rsa -b 4096 -f ./ssh_firebird_key -N ""
    echo ""
    echo "✅ Chave gerada em: ./ssh_firebird_key"
    echo "📤 Copie a chave pública para o servidor:"
    echo ""
    cat ./ssh_firebird_key.pub
    echo ""
    read -p "Pressione ENTER após copiar a chave pública para o servidor..."

    # Converter chave para formato adequado
    private_key=$(cat ./ssh_firebird_key | sed ':a;N;$!ba;s/\n/\\n/g')
else
    read -p "Caminho da chave privada [~/.ssh/id_rsa]: " key_path
    key_path=${key_path:-~/.ssh/id_rsa}

    if [ ! -f "$key_path" ]; then
        echo "❌ Arquivo não encontrado: $key_path"
        exit 1
    fi

    # Converter chave para formato adequado
    private_key=$(cat $key_path | sed ':a;N;$!ba;s/\n/\\n/g')
fi

# Atualizar .env com a chave privada
echo "SSH_PRIVATE_KEY=$private_key" >> .env

echo ""
echo "📝 Configuração do Firebird:"
echo ""

# Firebird Remote Host
read -p "Firebird Remote Host [localhost]: " fb_remote_host
fb_remote_host=${fb_remote_host:-localhost}
sed -i "s/FIREBIRD_REMOTE_HOST=.*/FIREBIRD_REMOTE_HOST=$fb_remote_host/" .env

# Firebird Remote Port
read -p "Firebird Remote Port [3050]: " fb_remote_port
fb_remote_port=${fb_remote_port:-3050}
sed -i "s/FIREBIRD_REMOTE_PORT=.*/FIREBIRD_REMOTE_PORT=$fb_remote_port/" .env

# Firebird Database Path
read -p "Caminho do banco Firebird: " fb_db_path
sed -i "s|FIREBIRD_DATABASE_PATH=.*|FIREBIRD_DATABASE_PATH=$fb_db_path|" .env

# Firebird User
read -p "Usuário Firebird [sysdba]: " fb_user
fb_user=${fb_user:-sysdba}
sed -i "s/FIREBIRD_USER=.*/FIREBIRD_USER=$fb_user/" .env

# Firebird Password
read -sp "Senha Firebird: " fb_password
echo ""
sed -i "s/FIREBIRD_PASSWORD=.*/FIREBIRD_PASSWORD=$fb_password/" .env

echo ""
echo "✅ Configuração concluída!"
echo ""
echo "Próximos passos:"
echo "1. Revise o arquivo .env"
echo "2. Execute: docker-compose up -d ssh-tunnel"
echo "3. Verifique os logs: docker-compose logs -f ssh-tunnel"
echo "4. Execute: docker-compose up -d app"
echo ""
echo "Para testar a conexão, use: ./scripts/test-firebird-connection.sh"
