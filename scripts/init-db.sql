-- Script de inicialização do banco de dados
-- Este script é executado automaticamente pelo Docker quando o container é criado

-- Habilitar extensão PGVector para armazenamento de embeddings
CREATE EXTENSION IF NOT EXISTS vector;

-- Habilitar extensão UUID para geração de IDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Verificar versões das extensões instaladas
SELECT * FROM pg_available_extensions WHERE name IN ('vector', 'uuid-ossp');

-- Mensagem de confirmação (será exibida nos logs)
DO $$
BEGIN
    RAISE NOTICE 'Database initialized successfully with extensions: vector, uuid-ossp';
END $$;