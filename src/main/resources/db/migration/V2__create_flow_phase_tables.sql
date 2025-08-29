-- V2__create_flow_phase_tables.sql
-- Criação das tabelas para Flows e Phase Contexts

-- Tabela de Phases (catálogo de fases disponíveis)
CREATE TABLE phases (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        name VARCHAR(100) NOT NULL,
                        description TEXT,
                        tenant VARCHAR(100) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        order_index INTEGER,
                        category VARCHAR(50),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT phases_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED')),
                        CONSTRAINT phases_tenant_name_unique UNIQUE(tenant, name)
);

-- Índices para Phases
CREATE INDEX idx_phases_tenant ON phases(tenant);
CREATE INDEX idx_phases_status ON phases(status);
CREATE INDEX idx_phases_category ON phases(category);
CREATE INDEX idx_phases_order ON phases(order_index);

-- Trigger para atualizar updated_at
CREATE TRIGGER update_phases_updated_at
    BEFORE UPDATE ON phases
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Tabela de Flows
CREATE TABLE flows (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       tenant VARCHAR(100) NOT NULL,
                       current_phase VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT flows_tenant_name_unique UNIQUE(tenant, name)
);

-- Índices para Flows
CREATE INDEX idx_flows_tenant ON flows(tenant);
CREATE INDEX idx_flows_current_phase ON flows(current_phase);
CREATE INDEX idx_flows_created_at ON flows(created_at DESC);

-- Trigger para atualizar updated_at
CREATE TRIGGER update_flows_updated_at
    BEFORE UPDATE ON flows
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Tabela de Phase Contexts
CREATE TABLE phase_contexts (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                flow_id UUID NOT NULL,
                                phase_name VARCHAR(100) NOT NULL,
                                variables JSONB,
                                tool_policy JSONB,
                                rag_filter TEXT,
                                system_prompt_template TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT phase_contexts_flow_phase_unique UNIQUE(flow_id, phase_name),
                                CONSTRAINT fk_phase_contexts_flow FOREIGN KEY (flow_id) REFERENCES flows(id) ON DELETE CASCADE
);

-- Índices para Phase Contexts
CREATE INDEX idx_phase_contexts_flow_id ON phase_contexts(flow_id);
CREATE INDEX idx_phase_contexts_phase_name ON phase_contexts(phase_name);
CREATE INDEX idx_phase_contexts_flow_phase ON phase_contexts(flow_id, phase_name);

-- Índices GIN para JSONB
CREATE INDEX idx_phase_contexts_variables_gin ON phase_contexts USING GIN(variables);
CREATE INDEX idx_phase_contexts_tool_policy_gin ON phase_contexts USING GIN(tool_policy);

-- Trigger para atualizar updated_at
CREATE TRIGGER update_phase_contexts_updated_at
    BEFORE UPDATE ON phase_contexts
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Atualizar tabela tasks para adicionar FK com flows
ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_flow
        FOREIGN KEY (flow_id) REFERENCES flows(id) ON DELETE SET NULL;

-- Comentários
COMMENT ON TABLE phases IS 'Catálogo de fases disponíveis no sistema';
COMMENT ON COLUMN phases.name IS 'Nome único da fase por tenant';
COMMENT ON COLUMN phases.order_index IS 'Ordem da fase no fluxo padrão';
COMMENT ON COLUMN phases.category IS 'Categoria/tipo da fase';

COMMENT ON TABLE flows IS 'Tabela de fluxos/workflows do sistema';
COMMENT ON COLUMN flows.current_phase IS 'Fase atual do fluxo';
COMMENT ON COLUMN flows.name IS 'Nome único do fluxo por tenant';

COMMENT ON TABLE phase_contexts IS 'Contextos específicos para cada fase de um fluxo';
COMMENT ON COLUMN phase_contexts.variables IS 'Variáveis de contexto da fase em formato JSON';
COMMENT ON COLUMN phase_contexts.tool_policy IS 'Política de ferramentas permitidas/negadas em formato JSON';
COMMENT ON COLUMN phase_contexts.rag_filter IS 'Filtro para busca RAG na fase';
COMMENT ON COLUMN phase_contexts.system_prompt_template IS 'Template do prompt do sistema para a fase';