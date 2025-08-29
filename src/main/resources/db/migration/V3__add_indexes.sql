-- V3__add_indexes.sql
-- Índices adicionais para otimização de performance

-- Índices compostos para queries comuns
CREATE INDEX idx_tasks_tenant_created_desc ON tasks(tenant, created_at DESC);
CREATE INDEX idx_tasks_tenant_status_created ON tasks(tenant, status, created_at DESC);
CREATE INDEX idx_tasks_flow_status ON tasks(flow_id, status) WHERE flow_id IS NOT NULL;

-- Índices para busca por período
CREATE INDEX idx_tasks_completed_at ON tasks(completed_at) WHERE completed_at IS NOT NULL;
CREATE INDEX idx_tasks_started_at ON tasks(started_at) WHERE started_at IS NOT NULL;

-- Índices para flows
CREATE INDEX idx_flows_tenant_current_phase ON flows(tenant, current_phase);
CREATE INDEX idx_flows_name_pattern ON flows(name varchar_pattern_ops);

-- Índices para phase_contexts com condições específicas
CREATE INDEX idx_phase_contexts_rag_filter ON phase_contexts(rag_filter) WHERE rag_filter IS NOT NULL;
CREATE INDEX idx_phase_contexts_system_prompt ON phase_contexts(system_prompt_template) WHERE system_prompt_template IS NOT NULL;

-- Extensão para busca full-text (opcional)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Índices GIN para busca textual
CREATE INDEX idx_tasks_prompt_gin ON tasks USING GIN(prompt gin_trgm_ops);
CREATE INDEX idx_flows_name_gin ON flows USING GIN(name gin_trgm_ops);
CREATE INDEX idx_flows_description_gin ON flows USING GIN(description gin_trgm_ops) WHERE description IS NOT NULL;

-- Estatísticas para otimização do planejador
ANALYZE tasks;
ANALYZE flows;
ANALYZE phase_contexts;
