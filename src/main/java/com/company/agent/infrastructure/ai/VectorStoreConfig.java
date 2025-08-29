package com.company.agent.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class VectorStoreConfig {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public VectorStore vectorStore() {
        return new PgVectorStore.Builder(jdbcTemplate, embeddingModel)
                .withSchemaName("public")
                .withVectorTableName("vector_store")
                .withSchemaValidation(true)
                .withInitializeSchema(true)
                .build();
    }
}
