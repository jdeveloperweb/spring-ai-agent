-- V1__create_initial_tables.sql
-- Criação das tabelas iniciais do sistema

-- Extensão para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela de Tasks
CREATE TABLE tasks (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       prompt TEXT NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                       mode VARCHAR(20) NOT NULL DEFAULT 'AUTONOMOUS',
                       tenant VARCHAR(100) NOT NULL,
                       flow_id UUID,
                       result TEXT,
                       error_message TEXT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       started_at TIMESTAMP,
                       completed_at TIMESTAMP,

                       CONSTRAINT tasks_status_check CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED')),
                       CONSTRAINT tasks_mode_check CHECK (mode IN ('AUTONOMOUS', 'SUPERVISED', 'PLANNING'))
);

-- Índices para Tasks
CREATE INDEX idx_tasks_tenant ON tasks(tenant);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_flow_id ON tasks(flow_id);
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);
CREATE INDEX idx_tasks_tenant_status ON tasks(tenant, status);

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $
BEGIN
NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$ LANGUAGE plpgsql;

CREATE TRIGGER update_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Comentários nas tabelas
COMMENT ON TABLE tasks IS 'Tabela de tarefas executadas pelo agente';
COMMENT ON COLUMN tasks.prompt IS 'Prompt/instrução da tarefa';
COMMENT ON COLUMN tasks.status IS 'Status atual da tarefa';
COMMENT ON COLUMN tasks.mode IS 'Modo de execução da tarefa';
COMMENT ON COLUMN tasks.tenant IS 'Tenant/organização proprietária da tarefa';
COMMENT ON COLUMN tasks.flow_id IS 'ID do fluxo/workflow ao qual a tarefa pertence';