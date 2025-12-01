# Caso de Uso Completo: E-commerce com IA

## Visão Geral

Este exemplo demonstra como usar o Spring AI Agent para criar um assistente de vendas inteligente que gerencia todo o processo de compra, desde a pesquisa de produtos até o pós-venda.

## Fluxo de Fases

```
PESQUISA_PRODUTO → CARRINHO → CHECKOUT → POS_VENDA
```

## Passo 1: Configurar as Fases (Catálogo)

Primeiro, crie as fases no catálogo:

```bash
# Fase 1: Pesquisa de Produtos
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "PESQUISA_PRODUTO",
    "description": "Pesquisa e recomendação de produtos baseado em preferências",
    "category": "ECOMMERCE",
    "orderIndex": 1
  }'

# Fase 2: Carrinho
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "CARRINHO",
    "description": "Gerenciamento de carrinho e cálculo de preços",
    "category": "ECOMMERCE",
    "orderIndex": 2
  }'

# Fase 3: Checkout
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "CHECKOUT",
    "description": "Processamento de pagamento e finalização",
    "category": "ECOMMERCE",
    "orderIndex": 3
  }'

# Fase 4: Pós-Venda
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "POS_VENDA",
    "description": "Confirmação, rastreamento e suporte pós-venda",
    "category": "ECOMMERCE",
    "orderIndex": 4
  }'
```

## Passo 2: Criar o Flow

```bash
curl -X POST http://localhost:8080/api/flows \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "name": "Compra - João Silva",
    "description": "Processo de compra de notebook para desenvolvimento",
    "initialPhase": "PESQUISA_PRODUTO"
  }'

# Resposta (guarde o flowId):
# {
#   "id": "550e8400-e29b-41d4-a716-446655440000",
#   "name": "Compra - João Silva",
#   ...
# }
```

## Passo 3: Configurar Fase de Pesquisa

```bash
export FLOW_ID="550e8400-e29b-41d4-a716-446655440000"

curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/PESQUISA_PRODUTO" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "cliente_nome": "João Silva",
      "categoria": "notebook",
      "budget": 5000,
      "uso": "desenvolvimento",
      "preferencias": ["ssd_512gb", "16gb_ram", "processador_i7"]
    },
    "toolPolicy": {
      "allow": ["http", "database"],
      "allowDomains": [
        "https://api.mercadolivre.com",
        "https://api.magazineluiza.com"
      ]
    },
    "ragFilter": "category == '\''electronics'\'' && subcategory == '\''laptop'\'' && in_stock == true",
    "systemPromptTemplate": "Você é um assistente de vendas especializado em {{variables.categoria}}.\n\nCliente: {{variables.cliente_nome}}\nOrçamento: R$ {{variables.budget}}\nUso pretendido: {{variables.uso}}\n\nRecomende produtos que atendam às preferências:\n{{#each variables.preferencias}}\n- {{this}}\n{{/each}}\n\nUse a base de conhecimento para encontrar os melhores produtos e compare preços."
  }'
```

## Passo 4: Executar Pesquisa de Produtos

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Preciso de um notebook para desenvolvimento de software. Quero algo com bom desempenho, SSD de pelo menos 512GB e 16GB de RAM. Meu orçamento é de até R$ 5000.\",
    \"mode\": \"AUTONOMOUS\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"

# Resposta esperada:
# {
#   "id": "...",
#   "status": "SUCCEEDED",
#   "result": "Encontrei 3 excelentes opções para você:\n\n1. **Dell Inspiron 15**\n   - Processador: Intel Core i7-12700H\n   - RAM: 16GB DDR4\n   - Armazenamento: 512GB SSD NVMe\n   - Preço: R$ 4.899,00\n   - Avaliação: 4.5/5 ⭐\n\n2. **Lenovo IdeaPad Gaming 3**\n   - Processador: AMD Ryzen 7 5800H\n   - RAM: 16GB DDR4\n   - Armazenamento: 512GB SSD\n   - GPU: GTX 1650\n   - Preço: R$ 4.599,00\n   - Avaliação: 4.7/5 ⭐\n\n3. **Acer Aspire 5**\n   - Processador: Intel Core i7-1165G7\n   - RAM: 16GB DDR4\n   - Armazenamento: 512GB SSD\n   - Preço: R$ 4.299,00\n   - Avaliação: 4.3/5 ⭐\n\nTodos estão disponíveis em estoque e atendem seus requisitos. Qual gostaria de adicionar ao carrinho?"
# }
```

## Passo 5: Avançar para Fase de Carrinho

```bash
curl -X POST "http://localhost:8080/api/flows/$FLOW_ID:advance" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "to": "CARRINHO",
    "guardVars": {
      "produto_selecionado": "Dell Inspiron 15",
      "produto_id": "DELL-INS15-I7-001",
      "preco": 4899.00
    }
  }'
```

## Passo 6: Configurar Fase de Carrinho

```bash
curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/CARRINHO" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "produtos": [
        {
          "id": "DELL-INS15-I7-001",
          "nome": "Dell Inspiron 15",
          "preco": 4899.00,
          "quantidade": 1
        }
      ],
      "cupom": "PRIMEIRACOMPRA",
      "cep_entrega": "01310-100"
    },
    "toolPolicy": {
      "allow": ["http", "database"]
    },
    "systemPromptTemplate": "Você está gerenciando o carrinho de compras.\n\nProdutos no carrinho:\n{{#each variables.produtos}}\n- {{this.nome}}: R$ {{this.preco}} ({{this.quantidade}}x)\n{{/each}}\n\nCupom: {{variables.cupom}}\nCEP: {{variables.cep_entrega}}\n\nCalcule:\n1. Subtotal\n2. Desconto do cupom\n3. Frete\n4. Total final"
  }'
```

## Passo 7: Calcular Totais no Carrinho

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Calcule o total do carrinho aplicando o cupom PRIMEIRACOMPRA (10% de desconto) e o frete para o CEP 01310-100.\",
    \"mode\": \"AUTONOMOUS\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"

# Resposta esperada:
# {
#   "result": "Resumo do pedido:\n\nSubtotal: R$ 4.899,00\nDesconto (PRIMEIRACOMPRA - 10%): -R$ 489,90\nFrete (PAC - 5-7 dias): R$ 25,00\n\nTotal: R$ 4.434,10"
# }
```

## Passo 8: Avançar para Checkout

```bash
curl -X POST "http://localhost:8080/api/flows/$FLOW_ID:advance" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "to": "CHECKOUT",
    "guardVars": {
      "total_aprovado": 4434.10,
      "forma_pagamento": "cartao_credito",
      "parcelas": 3
    }
  }'
```

## Passo 9: Configurar e Processar Checkout

```bash
curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/CHECKOUT" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "total": 4434.10,
      "forma_pagamento": "cartao_credito",
      "parcelas": 3,
      "cartao_final": "1234"
    },
    "toolPolicy": {
      "allow": ["http", "database", "email"],
      "allowDomains": [
        "https://api.pagamento.com"
      ]
    },
    "systemPromptTemplate": "Você está processando o checkout.\n\nTotal: R$ {{variables.total}}\nForma de pagamento: {{variables.forma_pagamento}}\nParcelas: {{variables.parcelas}}x\n\nProcesse o pagamento e gere o pedido."
  }'

curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Processe o pagamento de R$ 4.434,10 em 3x no cartão final 1234 e gere o número do pedido.\",
    \"mode\": \"SUPERVISED\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"
```

## Passo 10: Pós-Venda

```bash
curl -X POST "http://localhost:8080/api/flows/$FLOW_ID:advance" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "to": "POS_VENDA",
    "guardVars": {
      "pedido_id": "PED-2024-001234",
      "pagamento_aprovado": true
    }
  }'

curl -X PUT "http://localhost:8080/api/flows/$FLOW_ID/phases/POS_VENDA" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "variables": {
      "pedido_id": "PED-2024-001234",
      "email_cliente": "joao.silva@email.com"
    },
    "toolPolicy": {
      "allow": ["email", "http"]
    },
    "systemPromptTemplate": "Você está no pós-venda.\n\nPedido: {{variables.pedido_id}}\nCliente: {{variables.email_cliente}}\n\nEnvie email de confirmação com:\n1. Resumo do pedido\n2. Código de rastreamento (quando disponível)\n3. Prazo de entrega estimado"
  }'

curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d "{
    \"prompt\": \"Envie email de confirmação do pedido PED-2024-001234 para joao.silva@email.com com resumo da compra e informações de entrega.\",
    \"mode\": \"AUTONOMOUS\",
    \"flowId\": \"$FLOW_ID\",
    \"sync\": true
  }"
```

## Verificar Histórico do Flow

```bash
# Ver todas as tasks do flow
curl -X GET "http://localhost:8080/api/tasks?flowId=$FLOW_ID" \
  -H "X-API-Key: dev-token"

# Ver status atual do flow
curl -X GET "http://localhost:8080/api/flows/$FLOW_ID" \
  -H "X-API-Key: dev-token"

# Ver todos os contextos de fase
curl -X GET "http://localhost:8080/api/flows/$FLOW_ID/phases" \
  -H "X-API-Key: dev-token"
```

## Métricas e Insights

Ao final, você pode criar tasks de análise:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-token" \
  -d '{
    "prompt": "Analise o fluxo de compra completo e identifique pontos de melhoria na experiência do cliente.",
    "mode": "AUTONOMOUS",
    "sync": true
  }'
```

## Próximos Passos

1. **Integrar com sistemas reais:**
   - Conectar com catálogo de produtos
   - Integrar gateway de pagamento real
   - Conectar com sistema de estoque

2. **Adicionar mais fases:**
   - Negociação de preços
   - Upselling/Cross-selling
   - Programa de fidelidade

3. **Melhorar RAG:**
   - Indexar catálogo completo de produtos
   - Adicionar reviews de clientes
   - Incluir políticas de troca e devolução

4. **Automações:**
   - Webhook para atualização de status
   - Notificações automáticas
   - Alertas de estoque baixo
