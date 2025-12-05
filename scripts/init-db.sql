-- ============================================================================
-- Script de Inicialização do Banco de Dados - Spring AI Agent
-- ============================================================================
-- Este script é executado automaticamente pelo Docker quando o container
-- PostgreSQL é criado pela primeira vez.
--
-- Responsabilidades:
-- 1. Habilitar extensões necessárias (pgvector, uuid-ossp, pg_trgm)
-- 2. Configurar parâmetros de performance
-- 3. Validar instalação das extensões
--
-- Nota: As tabelas e índices são criados pelas migrations Flyway
-- ============================================================================

-- ============================================================================
-- EXTENSÕES DO POSTGRESQL
-- ============================================================================

-- PGVector: Armazenamento e busca de embeddings para RAG (Retrieval Augmented Generation)
-- Usado pelo Spring AI para armazenar vetores de documentos e realizar buscas semânticas
CREATE EXTENSION IF NOT EXISTS vector;

-- UUID-OSSP: Geração automática de UUIDs para primary keys
-- Usado em todas as tabelas do sistema (tasks, flows, phases, phase_contexts)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- PG_TRGM: Busca textual com trigrams para melhor performance em LIKE/ILIKE
-- Usado para buscas full-text em prompts, descrições e nomes de flows
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================================
-- CONFIGURAÇÕES DE PERFORMANCE
-- ============================================================================

-- Otimização para operações com JSONB (usado em phase_contexts)
ALTER DATABASE agent_db SET default_statistics_target = 100;

-- Otimização para queries com ORDER BY e índices
ALTER DATABASE agent_db SET random_page_cost = 1.1;

-- Otimização para trabalho com índices GIN (usado em JSONB e full-text search)
ALTER DATABASE agent_db SET gin_pending_list_limit = 4096;

-- ============================================================================
-- VALIDAÇÃO DAS EXTENSÕES
-- ============================================================================

-- Verificar e exibir versões das extensões instaladas
DO $$
DECLARE
    ext_vector TEXT;
    ext_uuid TEXT;
    ext_trgm TEXT;
BEGIN
    -- Buscar versões instaladas
    SELECT installed_version INTO ext_vector
    FROM pg_extension WHERE extname = 'vector';

    SELECT installed_version INTO ext_uuid
    FROM pg_extension WHERE extname = 'uuid-ossp';

    SELECT installed_version INTO ext_trgm
    FROM pg_extension WHERE extname = 'pg_trgm';

    -- Exibir informações nos logs
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Spring AI Agent - Database Initialized';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Extensions installed:';
    RAISE NOTICE '  - pgvector: %', COALESCE(ext_vector, 'NOT INSTALLED');
    RAISE NOTICE '  - uuid-ossp: %', COALESCE(ext_uuid, 'NOT INSTALLED');
    RAISE NOTICE '  - pg_trgm: %', COALESCE(ext_trgm, 'NOT INSTALLED');
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Database ready for Flyway migrations';
    RAISE NOTICE '============================================';

    -- Validar que todas as extensões foram instaladas
    IF ext_vector IS NULL OR ext_uuid IS NULL OR ext_trgm IS NULL THEN
        RAISE EXCEPTION 'One or more required extensions failed to install';
    END IF;
END $$;