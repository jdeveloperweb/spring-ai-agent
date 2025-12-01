# Caso de Uso: Atendimento ao Cliente com IA

## Cenário

Sistema de atendimento ao cliente automatizado que classifica, analisa e resolve problemas técnicos, com escalada automática quando necessário.

## Fluxo de Fases

```
TRIAGEM → ANALISE → RESOLUCAO → FOLLOWUP
```

## Configuração Inicial

### 1. Criar Fases do Atendimento

```bash
# TRIAGEM
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "TRIAGEM",
    "description": "Classificação inicial e priorização do atendimento",
    "category": "CUSTOMER_SERVICE",
    "orderIndex": 1
  }'

# ANALISE
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "ANALISE",
    "description": "Análise técnica detalhada do problema",
    "category": "CUSTOMER_SERVICE",
    "orderIndex": 2
  }'

# RESOLUCAO
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "RESOLUCAO",
    "description": "Execução da solução do problema",
    "category": "CUSTOMER_SERVICE",
    "orderIndex": 3
  }'

# FOLLOWUP
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "FOLLOWUP",
    "description": "Acompanhamento e confirmação de satisfação",
    "category": "CUSTOMER_SERVICE",
    "orderIndex": 4
  }'
```

## Caso 1: Problema de Login

### Criar Flow de Atendimento

```bash
curl -X POST http://localhost:8080/api/flows \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "Suporte Técnico - Cliente #12345",
    "description": "Problema de login após atualização do sistema",
    "initialPhase": "TRIAGEM"
  }'

# Guardar o flowId retornado
export FLOW_ID="sua-uuid-aqui"
```

### Fase 1: TRIAGEM

**Configurar contexto:**

```bash
curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/TRIAGEM" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "cliente_id": "12345",
      "cliente_nome": "Maria Santos",
      "plano": "premium",
      "data_abertura": "2024-01-15T10:30:00"
    },
    "toolPolicy": {
      "allow": ["database", "http"],
      "deny": ["email"]
    },
    "ragFilter": "category == '\''kb_support'\'' && topic == '\''authentication'\''",
    "systemPromptTemplate": "🎯 TRIAGEM DE ATENDIMENTO\n\nCliente: {{variables.cliente_nome}} (ID: {{variables.cliente_id}})\nPlano: {{variables.plano}}\n\n**Sua missão:**\n1. Classificar o tipo de problema\n2. Determinar severidade (1-5)\n3. Verificar se é problema conhecido\n4. Definir prioridade de atendimento\n\n**Ferramentas disponíveis:**\n- Database: Histórico do cliente\n- HTTP: Status dos sistemas\n\n{{#eq variables.plano \"premium\"}}\n⭐ Cliente premium - prioridade alta\n{{/eq}}\n\n**Output esperado:**\nJSON com classificação, severidade, prioridade e recomendação de próxima fase."
  }'
```

**Executar triagem:**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Cliente reporta: 'Não consigo fazer login no sistema desde a atualização de ontem. Aparece mensagem de senha incorreta, mas tenho certeza que é a senha certa. Já tentei redefinir a senha 2 vezes mas o problema persiste.'\",
    \"mode\": \"AUTONOMOUS\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"

# Resposta esperada:
# {
#   "status": "SUCCEEDED",
#   "result": "{
#     \"classificacao\": \"autenticacao_bloqueio\",
#     \"tipo_problema\": \"login_falha_pos_atualizacao\",
#     \"severidade\": 4,
#     \"prioridade\": \"alta\",
#     \"problema_conhecido\": true,
#     \"afeta_multiplos_usuarios\": false,
#     \"recomendacao\": \"ANALISE\",
#     \"observacoes\": \"Cliente premium com problema recorrente. Histórico mostra 5 tentativas falhadas de login nas últimas 2 horas. Provável bloqueio automático de segurança.\"
#   }"
# }
```

### Fase 2: ANALISE

**Avançar para análise:**

```bash
curl -X POST "http://localhost:8080/api/flows/$FLOW_ID:advance" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "to": "ANALISE",
    "guardVars": {
      "classificacao": "autenticacao_bloqueio",
      "severidade": 4,
      "prioridade": "alta"
    }
  }'
```

**Configurar fase de análise:**

```bash
curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/ANALISE" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "cliente_id": "12345",
      "problema_tipo": "autenticacao_bloqueio",
      "logs_disponiveis": true,
      "ambiente": "producao"
    },
    "toolPolicy": {
      "allow": ["database", "file", "http"]
    },
    "ragFilter": "category == '\''troubleshooting'\'' && problem_type == '\''auth_lock'\''",
    "systemPromptTemplate": "🔍 ANÁLISE TÉCNICA\n\n**Caso:** Problema de autenticação - Cliente {{variables.cliente_id}}\n**Tipo:** {{variables.problema_tipo}}\n**Ambiente:** {{variables.ambiente}}\n\n**Objetivos da análise:**\n1. Verificar logs de autenticação\n2. Identificar causa raiz\n3. Verificar tentativas de login\n4. Checar status do serviço de autenticação\n5. Propor 2-3 soluções possíveis\n\n**Procedimento:**\n- Consulte histórico de login (database)\n- Verifique logs de erro (file)\n- Verifique status do Auth Service (http)\n\n⚠️ Ambiente de PRODUÇÃO - cuidado com comandos destrutivos"
  }'
```

**Executar análise:**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Analise o problema de autenticação. Verifique os logs e identifique a causa raiz. Proponha soluções.\",
    \"mode\": \"AUTONOMOUS\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"

# Resposta esperada:
# {
#   "result": "📊 ANÁLISE COMPLETA\n\n**Causa Raiz Identificada:**\nConta bloqueada automaticamente após 5 tentativas falhadas de login entre 08:30 e 10:15.\n\n**Detalhes técnicos:**\n- Última tentativa válida: 14/01/2024 18:45\n- Primeira falha pós-update: 15/01/2024 08:30\n- Bloqueio automático: 15/01/2024 10:15\n- Serviço de Auth: ✅ Operacional\n- Hash de senha: Incompatível com nova versão\n\n**Causa:**\nMigração de algoritmo de hash (MD5 → bcrypt) na atualização não migrou senhas antigas corretamente.\n\n**Soluções propostas:**\n\n1. **Desbloqueio + Reset de Senha (RECOMENDADA)**\n   - Tempo: 2 minutos\n   - Risco: Baixo\n   - Impacto: Resolve definitivamente\n\n2. **Migração manual do hash**\n   - Tempo: 5 minutos\n   - Risco: Médio\n   - Requer acesso ao DB\n\n3. **Rollback temporário**\n   - Tempo: 15 minutos\n   - Risco: Alto\n   - Afeta todos os usuários"
# }
```

### Fase 3: RESOLUCAO

**Avançar para resolução:**

```bash
curl -X POST "http://localhost:8080/api/flows/$FLOW_ID:advance" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "to": "RESOLUCAO",
    "guardVars": {
      "causa_identificada": "hash_migration_issue",
      "solucao_escolhida": "unlock_and_reset",
      "aprovado_por": "supervisor_tech"
    }
  }'
```

**Configurar resolução:**

```bash
curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/RESOLUCAO" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "cliente_id": "12345",
      "cliente_email": "maria.santos@email.com",
      "solucao": "unlock_and_reset",
      "ambiente": "producao"
    },
    "toolPolicy": {
      "allow": ["database", "email", "http"]
    },
    "systemPromptTemplate": "⚡ EXECUÇÃO DE SOLUÇÃO\n\n**Cliente:** {{variables.cliente_id}}\n**Solução:** {{variables.solucao}}\n**Email:** {{variables.cliente_email}}\n\n**Procedimento:**\n1. Desbloquear conta no sistema\n2. Gerar token de reset de senha\n3. Enviar email com instruções\n4. Validar desbloqueio\n5. Aguardar confirmação do cliente\n\n{{#eq variables.ambiente \"producao\"}}\n🔴 PRODUÇÃO - Documente todas as ações\n{{/eq}}\n\n**Rollback:**\nEm caso de erro, reverter desbloqueio imediatamente."
  }'
```

**Executar resolução:**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Execute o desbloqueio da conta e envie email de reset de senha para o cliente.\",
    \"mode\": \"SUPERVISED\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"

# Resposta esperada:
# {
#   "result": "✅ SOLUÇÃO EXECUTADA COM SUCESSO\n\n**Ações realizadas:**\n\n1. ✅ Conta desbloqueada (10:45:30)\n   - UserID: 12345\n   - Status: ACTIVE\n   - Login attempts: 0\n\n2. ✅ Token de reset gerado\n   - Token: abc123xyz789\n   - Validade: 1 hora\n   - Expira em: 11:45:30\n\n3. ✅ Email enviado\n   - Para: maria.santos@email.com\n   - Assunto: 'Redefinição de Senha - Conta Desbloqueada'\n   - Status: Entregue (10:45:35)\n\n4. ✅ Validação\n   - Sistema de auth: Respondendo\n   - Conta acessível: Sim\n   - Pendências: Reset senha\n\n**Próximos passos:**\n- Aguardar cliente redefinir senha\n- Confirmar primeiro login bem-sucedido\n- Marcar caso como resolvido"
# }
```

### Fase 4: FOLLOWUP

**Avançar para follow-up:**

```bash
curl -X POST "http://localhost:8080/api/flows/$FLOW_ID:advance" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "to": "FOLLOWUP",
    "guardVars": {
      "resolucao_executada": true,
      "timestamp_resolucao": "2024-01-15T10:45:30"
    }
  }'
```

**Configurar follow-up:**

```bash
curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/FOLLOWUP" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "cliente_id": "12345",
      "cliente_email": "maria.santos@email.com",
      "cliente_nome": "Maria Santos",
      "tempo_resolucao": "45min"
    },
    "toolPolicy": {
      "allow": ["email", "database"]
    },
    "systemPromptTemplate": "📞 FOLLOW-UP\n\n**Cliente:** {{variables.cliente_nome}}\n**Tempo de resolução:** {{variables.tempo_resolucao}}\n\n**Checklist de fechamento:**\n- [ ] Cliente confirmou resolução\n- [ ] Login funcionando normalmente\n- [ ] Sem efeitos colaterais\n- [ ] Pesquisa de satisfação enviada\n- [ ] Base de conhecimento atualizada\n- [ ] Caso documentado\n\n**Aguardar 2 horas após resolução antes de fechar definitivamente.**"
  }'
```

**Executar follow-up:**

```bash
# Após 2 horas...
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Verifique se o cliente fez login com sucesso e envie pesquisa de satisfação.\",
    \"mode\": \"AUTONOMOUS\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"

# Resposta:
# {
#   "result": "✅ CASO RESOLVIDO E FECHADO\n\n**Verificações:**\n✅ Cliente fez login às 11:30:00\n✅ 3 sessões ativas desde então\n✅ Nenhum erro reportado\n✅ Tempo total desde abertura: 1h 15min\n\n**Pesquisa enviada:**\nEmail com NPS enviado às 13:00:00\n\n**Documentação:**\n- KB Article #4521 atualizado\n- Issue #UPDATE-2024-001 documentado\n- Alerta criado para QA validar migração de hash\n\n**Métricas:**\n- Tempo de resolução: 45min ⚡\n- SLA: Atendido (< 2h)\n- Primeira vez resolvido: Sim\n- Escalações: 0"
# }
```

## Caso 2: Escalação para Humano

Para casos que requerem intervenção humana:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Cliente muito insatisfeito e exige falar com gerente. Problema financeiro de cobrança duplicada.\",
    \"mode\": \"PLANNING\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"

# No modo PLANNING, o agente apenas planeja as ações mas não executa
# Permite revisão humana antes da execução
```

## Métricas e Relatórios

```bash
# Listar todas as tasks do flow para análise
curl -X GET "http://localhost:8080/api/tasks?flowId=$FLOW_ID" \
  -H "X-API-Key: dev-token"

# Análise de desempenho
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "prompt": "Analise todas as tasks do flow '$FLOW_ID' e gere relatório de desempenho, tempo de resolução e pontos de melhoria.",
    "mode": "AUTONOMOUS",
    "sync": true
  }'
```

## Boas Práticas

1. **Sempre use modo SUPERVISED para ações críticas**
   - Modificações em produção
   - Ações financeiras
   - Comunicações oficiais

2. **Configure RAG filters específicos**
   - Use base de conhecimento para problemas comuns
   - Mantenha KB atualizado

3. **Documente todas as fases**
   - Use guardVars para transferir contexto
   - Mantenha histórico completo

4. **Implemente SLA tracking**
   - Monitor tempo em cada fase
   - Alerte sobre casos críticos

5. **Feedback loop**
   - Use followup para melhorar base de conhecimento
   - Identifique problemas recorrentes
