# Spring AI Agent

Sistema de agente executor de tarefas com Spring AI, RAG (Retrieval Augmented Generation) e gerenciamento de contexto por fases.

## 📋 Visão Geral

O Spring AI Agent é uma plataforma robusta para executar tarefas automatizadas usando Inteligência Artificial. O sistema oferece:

- **Execução de Tarefas com IA**: Execute tarefas complexas usando modelos de linguagem (GPT-4, GPT-3.5, etc.)
- **RAG (Retrieval Augmented Generation)**: Busca em base de conhecimento vetorial para respostas contextualizadas
- **Fluxos (Flows)**: Organize tarefas em fluxos multi-fase com contexto persistente
- **Gerenciamento de Fases**: Configure prompts, ferramentas e filtros específicos para cada fase do fluxo
- **Multi-Tenancy**: Isolamento completo de dados por tenant/organização
- **Ferramentas (Tools)**: HTTP, Email, File, Database - extensíveis
- **API RESTful**: Interface completa para integração

## 🚀 Início Rápido

### Pré-requisitos

- Java 21+
- Maven 3.8+
- Docker e Docker Compose
- Chave de API da OpenAI

### Configuração

1. **Clone o repositório**
```bash
git clone <repository-url>
cd spring-ai-agent
```

2. **Configure as variáveis de ambiente**
```bash
cp .env.example .env
# Edite o arquivo .env e adicione sua OPENAI_API_KEY
```

3. **Inicie os serviços com Docker Compose**
```bash
docker-compose up -d
```

A aplicação estará disponível em `http://localhost:8080`

### Alternativa: Executar localmente sem Docker

1. **Inicie o PostgreSQL com PGVector**
```bash
docker-compose up -d postgres
```

2. **Configure o banco de dados**
```bash
# Crie o banco e execute as migrations
./mvnw flyway:migrate
```

3. **Execute a aplicação**
```bash
./mvnw spring-boot:run
```

## 📚 Arquitetura

O projeto segue os princípios de Clean Architecture e DDD:

```
src/main/java/com/company/agent/
├── domain/              # Entidades e regras de negócio
│   ├── Task.java
│   ├── Flow.java
│   ├── Phase.java
│   └── PhaseContext.java
├── application/         # Casos de uso e orquestração
│   ├── AgentOrchestrator.java
│   ├── TaskService.java
│   ├── FlowService.java
│   └── PhaseManager.java
├── infrastructure/      # Adaptadores e implementações
│   ├── persistence/     # JPA e repositórios
│   ├── security/        # Autenticação e multi-tenancy
│   ├── ai/              # Spring AI e RAG
│   └── tools/           # Ferramentas do agente
└── api/                 # Controllers REST
    ├── TaskController.java
    ├── FlowController.java
    └── PhaseController.java
```

### Conceitos Principais

#### 1. **Task (Tarefa)**
Uma unidade de trabalho que o agente deve executar. Pode ser:
- **AUTONOMOUS**: Execução totalmente autônoma
- **SUPERVISED**: Requer aprovação humana
- **PLANNING**: Apenas planejamento, sem execução

#### 2. **Flow (Fluxo)**
Agrupa tarefas relacionadas e gerencia transições entre fases. Exemplo: processo de atendimento ao cliente com fases de triagem → análise → resolução → follow-up.

#### 3. **Phase (Fase)**
Define uma etapa em um workflow. Cada fase pode ter configurações específicas de ferramentas, prompts e filtros RAG.

#### 4. **PhaseContext (Contexto de Fase)**
Configuração específica de uma fase em um flow, incluindo:
- Variáveis de contexto (JSON)
- Política de ferramentas (quais tools estão disponíveis)
- Filtro RAG (query filter para busca vetorial)
- Template de prompt do sistema

## 🔧 Uso da API

### Autenticação

Todas as requisições requerem um header de autenticação:

```http
X-API-Key: dev-token
```

### Exemplos de Uso

#### Criar e Executar uma Tarefa Simples

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "prompt": "Analise o sentimento dos últimos 10 reviews do produto XYZ",
    "mode": "AUTONOMOUS",
    "sync": true
  }'
```

#### Criar um Fluxo

```bash
curl -X POST http://localhost:8080/api/flows \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "Atendimento Cliente #12345",
    "description": "Processo de suporte técnico",
    "initialPhase": "TRIAGEM"
  }'
```

#### Configurar Contexto de uma Fase

```bash
curl -X PUT http://localhost:8080/api/flows/{flowId}/phases/ANALISE \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "cliente_id": "12345",
      "prioridade": "alta"
    },
    "toolPolicy": {
      "allow": ["http", "database"],
      "deny": ["email"]
    },
    "ragFilter": "category == '\''support'\'' && priority == '\''high'\''",
    "systemPromptTemplate": "Você está analisando o caso do cliente {{cliente_id}} com prioridade {{prioridade}}."
  }'
```

#### Executar Tarefa em um Fluxo

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "prompt": "Verificar logs de erro do cliente e identificar causa raiz",
    "mode": "AUTONOMOUS",
    "flowId": "{flowId}",
    "sync": true
  }'
```

#### Avançar Fluxo para Próxima Fase

```bash
curl -X POST http://localhost:8080/api/flows/{flowId}:advance \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "to": "RESOLUCAO",
    "guardVars": {
      "analise_completa": true,
      "solucao_identificada": true
    }
  }'
```

### Endpoints Disponíveis

#### Tasks
- `POST /api/tasks` - Criar e executar tarefa
- `GET /api/tasks/{id}` - Buscar tarefa por ID
- `GET /api/tasks` - Listar tarefas (filtros: status, flowId)
- `POST /api/tasks/{id}/execute` - Executar tarefa pendente
- `POST /api/tasks/{id}/cancel` - Cancelar tarefa

#### Flows
- `POST /api/flows` - Criar flow
- `GET /api/flows/{id}` - Buscar flow por ID
- `GET /api/flows` - Listar flows
- `POST /api/flows/{id}:advance` - Avançar para próxima fase
- `PUT /api/flows/{id}/phases/{phase}` - Configurar contexto de fase
- `GET /api/flows/{id}/phases/{phase}` - Buscar contexto de fase
- `GET /api/flows/{id}/phases` - Listar todos os contextos do flow

#### Phases
- `POST /api/phases` - Criar fase (catálogo)
- `GET /api/phases/{id}` - Buscar fase por ID
- `GET /api/phases/name/{name}` - Buscar fase por nome
- `GET /api/phases` - Listar fases (filtros: status, category)
- `PUT /api/phases/{id}` - Atualizar fase
- `POST /api/phases/{id}/activate` - Ativar fase
- `POST /api/phases/{id}/deactivate` - Desativar fase
- `DELETE /api/phases/{id}` - Deletar fase

#### Health
- `GET /actuator/health` - Status da aplicação
- `GET /actuator/info` - Informações da aplicação
- `GET /actuator/metrics` - Métricas

## 🛠️ Ferramentas (Tools)

O agente possui ferramentas que podem ser usadas durante a execução:

### HttpTool
Faz requisições HTTP para APIs externas. Suporta domínios restritos por segurança.

```json
{
  "toolPolicy": {
    "allow": ["http"],
    "allowDomains": [
      "https://api.example.com",
      "https://jsonplaceholder.typicode.com"
    ]
  }
}
```

### EmailTool
Envia emails para notificações e comunicações.

### FileTool
Lê e escreve arquivos locais (com restrições de segurança).

### DatabaseTool
Executa queries no banco de dados (somente leitura por padrão).

## 🔐 Segurança e Multi-Tenancy

### API Keys

Configure as API keys no `application.yml`:

```yaml
agent:
  security:
    api-keys:
      - key: "sua-chave-segura"
        tenant: "empresa1"
        name: "Chave Principal Empresa 1"
      - key: "outra-chave-segura"
        tenant: "empresa2"
        name: "Chave Principal Empresa 2"
```

### Isolamento de Dados

- Cada tenant tem seus dados completamente isolados
- Filtros automáticos em queries por tenant
- RAG scope por tenant para evitar vazamento de dados

## 📊 Base de Conhecimento (RAG)

### Adicionar Documentos

O sistema usa PGVector para armazenar embeddings de documentos:

```java
// Exemplo de código para adicionar documentos
List<Document> documents = List.of(
    new Document("Conteúdo do documento 1",
        Map.of("tenant", "default", "category", "support"))
);

vectorStore.add(documents);
```

### Filtros RAG

Configure filtros específicos por fase:

```json
{
  "ragFilter": "tenant == 'default' && category == 'support' && priority == 'high'"
}
```

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
./mvnw test

# Testes específicos
./mvnw test -Dtest=TaskControllerTest
./mvnw test -Dtest=FlowServiceTest
```

### Dados de Exemplo

Para popular o banco com dados de teste:

```bash
# Após iniciar o banco e executar as migrations
psql -h localhost -U agent_user -d agent_db -f scripts/sample-data.sql
```

## 📦 Deploy

### Docker

Build da imagem:

```bash
docker build -t spring-ai-agent:latest .
```

### Produção

1. Configure o profile `prod`:
```bash
export SPRING_PROFILES_ACTIVE=prod
export OPENAI_API_KEY=sua-chave-producao
export DATABASE_URL=jdbc:postgresql://prod-host:5432/agent_db
```

2. Execute:
```bash
java -jar target/spring-ai-agent-1.0.0-SNAPSHOT.jar
```

### Kubernetes

Exemplo de deployment (criar arquivo `k8s-deployment.yaml`):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-ai-agent
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-ai-agent
  template:
    metadata:
      labels:
        app: spring-ai-agent
    spec:
      containers:
      - name: app
        image: spring-ai-agent:latest
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: ai-secrets
              key: openai-key
```

## 🔧 Configuração Avançada

### Templates de Prompt

Use templates Handlebars para prompts dinâmicos:

```handlebars
Você está na fase {{phaseName}} do fluxo {{flowId}}.
Cliente: {{variables.cliente_id}}
Prioridade: {{variables.prioridade}}

Instruções específicas da fase:
{{#if (eq phaseName "TRIAGEM")}}
  - Classifique o problema
  - Determine prioridade
{{/if}}
{{#if (eq phaseName "ANALISE")}}
  - Analise logs
  - Identifique causa raiz
{{/if}}
```

### Política de Ferramentas

Controle fino sobre quais ferramentas estão disponíveis:

```json
{
  "toolPolicy": {
    "allow": ["http", "database"],
    "deny": ["email", "file"],
    "allowDomains": [
      "https://api.trusted.com"
    ]
  }
}
```

## 📖 Documentação Adicional

- [Guia de Arquitetura](docs/architecture-overview.md)
- [Documentação da API](docs/api-documentation.md)
- [Guia de Deploy](docs/deployment-guide.md)

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## 🙋 Suporte

Para questões e suporte:
- Abra uma issue no GitHub
- Consulte a documentação em `/docs`
- Email: support@example.com

## 🗺️ Roadmap

- [ ] Suporte para mais modelos de IA (Anthropic Claude, Llama, etc.)
- [ ] Interface Web para gerenciamento
- [ ] Webhooks para notificações
- [ ] Métricas e observabilidade avançada
- [ ] Cache distribuído com Redis
- [ ] Fila de tarefas assíncrona com RabbitMQ/Kafka
- [ ] Suporte para execução de código (sandboxed)
- [ ] Integração com mais ferramentas (Slack, Teams, etc.)

---

**Desenvolvido com ❤️ usando Spring Boot e Spring AI**
