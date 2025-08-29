package com.company.agent.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RagAdvisorConfig {

    private final VectorStore vectorStore;

    @Value("${agent.rag.similarity-threshold:0.75}")
    private double defaultSimilarityThreshold;

    @Value("${agent.rag.top-k:6}")
    private int defaultTopK;

    @Bean
    public RetrievalAugmentationAdvisor defaultRagAdvisor() {
        // Criar um DocumentRetriever simples usando o VectorStore
        DocumentRetriever retriever = query -> {
            SearchRequest searchRequest = SearchRequest.query(String.valueOf(query))
                    .withTopK(defaultTopK)
                    .withSimilarityThreshold(defaultSimilarityThreshold);
            return vectorStore.similaritySearch(searchRequest);
        };

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .userTextAdvise("""
                        Use the following context information to help answer the user's question.
                        If the context doesn't contain relevant information, please indicate that clearly.
                        
                        Context: {context}
                        
                        Question: {input}
                        """)
                .build();
    }

    // Factory method para criar RAG Advisor com filtros específicos
    public RetrievalAugmentationAdvisor createRagAdvisorWithFilter(String filterExpression) {
        DocumentRetriever retriever = query -> {
            SearchRequest.Builder searchBuilder = SearchRequest.query(query)
                    .withTopK(defaultTopK)
                    .withSimilarityThreshold(defaultSimilarityThreshold);

            if (filterExpression != null && !filterExpression.trim().isEmpty()) {
                try {
                    searchBuilder.withFilterExpression(filterExpression);
                } catch (Exception e) {
                    System.err.println("Erro ao aplicar filtro RAG: " + e.getMessage());
                }
            }

            return vectorStore.similaritySearch(searchBuilder.build());
        };

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .userTextAdvise("""
                        Use the following context information to help answer the user's question.
                        Context is filtered based on current phase and tenant.
                        
                        Context: {context}
                        
                        Question: {input}
                        """)
                .build();
    }

    // Factory method para criar RAG Advisor para uma fase específica
    public RetrievalAugmentationAdvisor createRagAdvisorForPhase(String tenant, String phaseName) {
        DocumentRetriever retriever = query -> {
            String filterExpression = String.format("tenant == '%s' && phase == '%s'", tenant, phaseName);

            SearchRequest searchRequest = SearchRequest.query(query)
                    .withTopK(defaultTopK)
                    .withSimilarityThreshold(defaultSimilarityThreshold)
                    .withFilterExpression(filterExpression)
                    .build();

            return vectorStore.similaritySearch(searchRequest);
        };

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .userTextAdvise(String.format("""
                        Use the following context information specific to phase '%s' to help answer the user's question.
                        This context is filtered for the current phase and should be most relevant.
                        
                        Context: {context}
                        
                        Question: {input}
                        """, phaseName))
                .build();
    }

    // Factory method para criar RAG Advisor com configurações customizadas
    public RetrievalAugmentationAdvisor createCustomRagAdvisor(
            double similarityThreshold,
            int topK,
            String filterExpression,
            String customPromptTemplate) {

        DocumentRetriever retriever = query -> {
            SearchRequest.Builder searchBuilder = SearchRequest.query(query)
                    .withTopK(topK)
                    .withSimilarityThreshold(similarityThreshold);

            if (filterExpression != null && !filterExpression.trim().isEmpty()) {
                try {
                    searchBuilder.withFilterExpression(filterExpression);
                } catch (Exception e) {
                    System.err.println("Erro ao aplicar filtro RAG customizado: " + e.getMessage());
                }
            }

            return vectorStore.similaritySearch(searchBuilder.build());
        };

        String promptTemplate = customPromptTemplate != null ? customPromptTemplate : """
                Use the following context information to help answer the user's question.
                
                Context: {context}
                
                Question: {input}
                """;

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .userTextAdvise(promptTemplate)
                .build();
    }
}