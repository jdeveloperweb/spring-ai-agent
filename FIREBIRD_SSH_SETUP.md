# Configuração do Túnel SSH para Firebird

Este documento descreve como configurar a conexão com o banco de dados Firebird via túnel SSH.

## Problema Resolvido

O erro "Your user name and password are not defined" ocorria porque o túnel SSH não estava configurado corretamente, impedindo a aplicação de se conectar ao banco Firebird remoto.

## Arquitetura da Solução

```
[Aplicação] ---> [Túnel SSH] ---> [Servidor Remoto] ---> [Firebird]
  Docker           Docker           SSH Host            Database
```

## Configuração

### 1. Criar arquivo .env

Copie o arquivo `.env.example` para `.env` e configure as variáveis:

```bash
cp .env.example .env
```

### 2. Configurar credenciais SSH

Edite o arquivo `.env` e configure:

```env
# SSH Tunnel Configuration
SSH_HOST=seu-servidor-ssh.com
SSH_USER=seu-usuario-ssh
SSH_PRIVATE_KEY=-----BEGIN OPENSSH PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
sua chave privada aqui (sem quebras de linha, use \n)
...
-----END OPENSSH PRIVATE KEY-----

# Firebird Configuration
FIREBIRD_REMOTE_HOST=localhost
FIREBIRD_REMOTE_PORT=3050
FIREBIRD_LOCAL_PORT=3050
FIREBIRD_DATABASE_PATH=u9/c6bank/suporte/scat108030/scci.gdb
FIREBIRD_USER=sysdba
FIREBIRD_PASSWORD=sua-senha-firebird
```

### 3. Preparar a chave SSH

#### Opção A: Usar chave SSH existente

```bash
# Converter a chave privada para formato adequado (uma linha com \n)
cat ~/.ssh/id_rsa | sed ':a;N;$!ba;s/\n/\\n/g'
```

Copie a saída e cole no `.env` na variável `SSH_PRIVATE_KEY`.

#### Opção B: Gerar nova chave SSH

```bash
# Gerar nova chave
ssh-keygen -t rsa -b 4096 -f ~/.ssh/firebird_key -N ""

# Copiar chave pública para o servidor
ssh-copy-id -i ~/.ssh/firebird_key.pub usuario@servidor-ssh.com

# Converter para formato adequado
cat ~/.ssh/firebird_key | sed ':a;N;$!ba;s/\n/\\n/g'
```

### 4. Iniciar os serviços

```bash
# Construir e iniciar os containers
docker-compose up -d

# Verificar se o túnel SSH está funcionando
docker-compose logs ssh-tunnel

# Verificar se a aplicação conectou
docker-compose logs app
```

## Troubleshooting

### Erro: "Connection refused"

**Causa**: O túnel SSH não está funcionando.

**Solução**:
1. Verifique se as credenciais SSH estão corretas
2. Verifique os logs: `docker-compose logs ssh-tunnel`
3. Teste a conexão SSH manualmente:
   ```bash
   ssh usuario@servidor-ssh.com
   ```

### Erro: "Your user name and password are not defined"

**Causa**: As credenciais do Firebird estão incorretas ou o túnel não está encaminhando corretamente.

**Solução**:
1. Verifique `FIREBIRD_USER` e `FIREBIRD_PASSWORD` no `.env`
2. Confirme que o banco de dados existe no caminho especificado
3. Verifique se o usuário tem permissões no banco

### Erro: "Database unavailable"

**Causa**: O caminho do banco de dados está incorreto.

**Solução**:
1. Verifique `FIREBIRD_DATABASE_PATH` no `.env`
2. Confirme o caminho correto com o administrador do banco

## Testando a Conexão

### Teste Manual do Túnel SSH

```bash
# Dentro do container ssh-tunnel
docker-compose exec ssh-tunnel sh

# Testar conexão SSH
ssh -o StrictHostKeyChecking=no usuario@servidor-ssh.com "echo 'Conexão OK'"
```

### Teste de Conexão ao Firebird

```bash
# Logs da aplicação
docker-compose logs -f app | grep -i firebird

# Verificar pool de conexões
docker-compose logs app | grep -i "HikariPool"
```

## Configurações Avançadas

### Alterar porta local do túnel

Se a porta 3050 já estiver em uso, altere no `.env`:

```env
FIREBIRD_LOCAL_PORT=3051
```

E atualize a URL no docker-compose.yml:

```yaml
SPRING_DATASOURCE_FIREBIRD_URL: jdbc:firebirdsql://ssh-tunnel:3051/...
```

### Usar host diferente no servidor remoto

Se o Firebird não estiver no mesmo servidor SSH:

```env
FIREBIRD_REMOTE_HOST=ip-do-servidor-firebird
FIREBIRD_REMOTE_PORT=3050
```

## Arquitetura dos Componentes

### 1. ssh-tunnel (Container)
- Cria túnel SSH para o servidor remoto
- Expõe porta local 3050 mapeada para porta remota do Firebird
- Usa imagem Alpine com SSH client

### 2. app (Container)
- Conecta ao ssh-tunnel na porta 3050
- Usa HikariCP para pool de conexões
- Configuração automática via Spring Boot

### 3. FirebirdDataSourceConfig
- Bean Spring para configurar DataSource do Firebird
- Pool de conexões otimizado
- Ativado apenas quando `spring.datasource.firebird.url` está definida

## Segurança

⚠️ **IMPORTANTE**:
- Nunca commite o arquivo `.env` no Git
- Use variáveis de ambiente no CI/CD
- Rotacione as credenciais regularmente
- Use chaves SSH com senha quando possível

## Suporte

Para problemas ou dúvidas:
1. Verifique os logs: `docker-compose logs`
2. Teste a conexão SSH manualmente
3. Confirme as credenciais com o administrador do banco
