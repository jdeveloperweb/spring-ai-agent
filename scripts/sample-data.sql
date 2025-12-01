-- Script de dados de exemplo para Spring AI Agent
-- Execute este script após as migrations do Flyway para popular dados de teste

-- Limpar dados existentes (use com cuidado!)
-- DELETE FROM tasks WHERE tenant = 'sample';
-- DELETE FROM phase_contexts WHERE flow_id IN (SELECT id FROM flows WHERE tenant = 'sample');
-- DELETE FROM flows WHERE tenant = 'sample';
-- DELETE FROM phases WHERE tenant = 'sample';

-- ============================================================
-- FASES DE EXEMPLO
-- ============================================================

-- Fases para um fluxo de atendimento ao cliente
INSERT INTO phases (id, name, description, tenant, status, order_index, category, created_at, updated_at)
VALUES
    (uuid_generate_v4(), 'TRIAGEM', 'Fase inicial de triagem e classificação da solicitação', 'sample', 'ACTIVE', 1, 'CUSTOMER_SERVICE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ANALISE', 'Análise detalhada do problema ou solicitação', 'sample', 'ACTIVE', 2, 'CUSTOMER_SERVICE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'RESOLUCAO', 'Execução da solução ou resposta ao cliente', 'sample', 'ACTIVE', 3, 'CUSTOMER_SERVICE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'FOLLOWUP', 'Acompanhamento e confirmação de satisfação', 'sample', 'ACTIVE', 4, 'CUSTOMER_SERVICE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Fases para um fluxo de e-commerce
INSERT INTO phases (id, name, description, tenant, status, order_index, category, created_at, updated_at)
VALUES
    (uuid_generate_v4(), 'PESQUISA_PRODUTO', 'Pesquisa e recomendação de produtos', 'sample', 'ACTIVE', 1, 'ECOMMERCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'CARRINHO', 'Gerenciamento de carrinho e cálculo de preços', 'sample', 'ACTIVE', 2, 'ECOMMERCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'CHECKOUT', 'Processamento do pagamento e finalização', 'sample', 'ACTIVE', 3, 'ECOMMERCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'POS_VENDA', 'Confirmação, rastreamento e suporte pós-venda', 'sample', 'ACTIVE', 4, 'ECOMMERCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- FLUXOS DE EXEMPLO
-- ============================================================

-- Fluxo de atendimento ao cliente
INSERT INTO flows (id, name, description, tenant, current_phase, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Suporte Técnico - Cliente #12345', 'Atendimento de suporte técnico sobre problema de login', 'sample', 'TRIAGEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Fluxo de e-commerce
INSERT INTO flows (id, name, description, tenant, current_phase, created_at, updated_at)
VALUES
    ('22222222-2222-2222-2222-222222222222', 'Compra - Pedido #98765', 'Processo de compra de eletrônicos', 'sample', 'PESQUISA_PRODUTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- CONTEXTOS DE FASE (PHASE CONTEXTS)
-- ============================================================

-- Contextos para o fluxo de suporte técnico
INSERT INTO phase_contexts (id, flow_id, phase_name, variables, tool_policy, rag_filter, system_prompt_template, created_at, updated_at)
VALUES
    (
        uuid_generate_v4(),
        '11111111-1111-1111-1111-111111111111',
        'TRIAGEM',
        '{"cliente_id": "12345", "tipo_problema": "login", "prioridade": "alta"}'::jsonb,
        '{"allow": ["http", "database"], "deny": ["email"]}'::jsonb,
        'category == ''technical_support'' && priority == ''high''',
        'Você está na fase de TRIAGEM. Classifique o problema do cliente {{cliente_id}} e determine a prioridade. Use as ferramentas de consulta ao banco de dados para verificar histórico.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        uuid_generate_v4(),
        '11111111-1111-1111-1111-111111111111',
        'ANALISE',
        '{"cliente_id": "12345", "categoria_problema": "autenticacao", "logs_coletados": true}'::jsonb,
        '{"allow": ["http", "database", "file"], "deny": []}'::jsonb,
        'category == ''technical_support'' && phase == ''analysis''',
        'Você está na fase de ANÁLISE. Analise detalhadamente o problema de autenticação do cliente {{cliente_id}}. Use ferramentas de arquivo para verificar logs e documentação.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        uuid_generate_v4(),
        '11111111-1111-1111-1111-111111111111',
        'RESOLUCAO',
        '{"cliente_id": "12345", "solucao_proposta": "reset_senha", "aprovado": true}'::jsonb,
        '{"allow": ["http", "database", "email"], "deny": []}'::jsonb,
        'category == ''technical_support'' && phase == ''resolution''',
        'Você está na fase de RESOLUÇÃO. Execute a solução aprovada ({{solucao_proposta}}) para o cliente {{cliente_id}}. Use email para notificar o cliente sobre os próximos passos.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- Contextos para o fluxo de e-commerce
INSERT INTO phase_contexts (id, flow_id, phase_name, variables, tool_policy, rag_filter, system_prompt_template, created_at, updated_at)
VALUES
    (
        uuid_generate_v4(),
        '22222222-2222-2222-2222-222222222222',
        'PESQUISA_PRODUTO',
        '{"categoria": "eletronicos", "budget": 5000, "preferencias": ["notebook", "alta_performance"]}'::jsonb,
        '{"allow": ["http", "database"], "deny": ["email", "file"], "allowDomains": ["https://api.ecommerce.com", "https://api.reviews.com"]}'::jsonb,
        'category == ''product'' && subcategory == ''electronics''',
        'Você está na fase de PESQUISA DE PRODUTO. Ajude o cliente a encontrar {{preferencias}} dentro do orçamento de R$ {{budget}}. Use a base de conhecimento de produtos e reviews.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        uuid_generate_v4(),
        '22222222-2222-2222-2222-222222222222',
        'CARRINHO',
        '{"produtos_selecionados": ["PROD-123", "PROD-456"], "cupom_desconto": "PRIMEIRACOMPRA"}'::jsonb,
        '{"allow": ["http", "database"], "deny": ["email", "file"]}'::jsonb,
        'category == ''cart'' && status == ''active''',
        'Você está na fase de CARRINHO. Calcule o total com os produtos {{produtos_selecionados}} e aplique o cupom {{cupom_desconto}}. Verifique disponibilidade em estoque.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- ============================================================
-- TAREFAS DE EXEMPLO
-- ============================================================

-- Tarefas do fluxo de suporte
INSERT INTO tasks (id, prompt, status, mode, tenant, flow_id, result, created_at, updated_at)
VALUES
    (
        uuid_generate_v4(),
        'Cliente reporta erro ao fazer login: "Senha incorreta mesmo após reset". Verificar histórico de tentativas e logs recentes.',
        'SUCCEEDED',
        'AUTONOMOUS',
        'sample',
        '11111111-1111-1111-1111-111111111111',
        'Análise concluída: Cliente tem 5 tentativas falhadas nas últimas 2 horas. Conta foi temporariamente bloqueada por segurança. Recomendo reset de senha via email e desbloqueio manual da conta.',
        CURRENT_TIMESTAMP - INTERVAL '2 hours',
        CURRENT_TIMESTAMP - INTERVAL '1 hour'
    ),
    (
        uuid_generate_v4(),
        'Executar reset de senha para o cliente e enviar email com instruções.',
        'PENDING',
        'SUPERVISED',
        'sample',
        '11111111-1111-1111-1111-111111111111',
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- Tarefas do fluxo de e-commerce
INSERT INTO tasks (id, prompt, status, mode, tenant, flow_id, result, created_at, updated_at)
VALUES
    (
        uuid_generate_v4(),
        'Recomendar notebooks de alta performance até R$ 5000 para desenvolvimento de software.',
        'SUCCEEDED',
        'AUTONOMOUS',
        'sample',
        '22222222-2222-2222-2222-222222222222',
        'Recomendações encontradas: 1) Dell Inspiron 15 (R$ 4.899) - 16GB RAM, i7, SSD 512GB. 2) Lenovo IdeaPad Gaming (R$ 4.599) - 16GB RAM, Ryzen 7, RTX 3050. Ambos atendem requisitos e têm boas avaliações.',
        CURRENT_TIMESTAMP - INTERVAL '30 minutes',
        CURRENT_TIMESTAMP - INTERVAL '25 minutes'
    );

-- Tarefas independentes (sem flow)
INSERT INTO tasks (id, prompt, status, mode, tenant, flow_id, result, created_at, updated_at)
VALUES
    (
        uuid_generate_v4(),
        'Analisar sentimento dos últimos 100 reviews de produtos e gerar relatório.',
        'RUNNING',
        'PLANNING',
        'sample',
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '5 minutes',
        CURRENT_TIMESTAMP - INTERVAL '4 minutes'
    ),
    (
        uuid_generate_v4(),
        'Buscar preços de concorrentes para a categoria de eletrônicos.',
        'SUCCEEDED',
        'AUTONOMOUS',
        'sample',
        NULL,
        'Análise de preços concluída. Encontrados 15 concorrentes com preços 5-10% menores em notebooks da mesma categoria.',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '10 minutes'
    );

-- ============================================================
-- COMENTÁRIOS E DICAS
-- ============================================================

-- Para verificar os dados inseridos:
-- SELECT * FROM phases WHERE tenant = 'sample' ORDER BY category, order_index;
-- SELECT * FROM flows WHERE tenant = 'sample';
-- SELECT * FROM phase_contexts WHERE flow_id IN (SELECT id FROM flows WHERE tenant = 'sample');
-- SELECT * FROM tasks WHERE tenant = 'sample' ORDER BY created_at DESC;

-- Para usar esses dados de exemplo:
-- 1. Execute este script após rodar as migrations
-- 2. Use o tenant 'sample' nas requisições da API
-- 3. Use a API key configurada para o tenant 'sample' no application.yml
-- 4. Teste avançar o flow '11111111-1111-1111-1111-111111111111' entre as fases
