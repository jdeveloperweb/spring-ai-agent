package com.company.agent.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final TenantContext tenantContext;
    private final ApiKeyConfig apiKeyConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Pular autenticação para endpoints de health
        String path = request.getRequestURI();
        if (path.startsWith("/actuator/") || path.equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = extractApiKey(request);

        if (apiKey == null) {
            unauthorized(response, "API Key é obrigatória");
            return;
        }

        Optional<ApiKeyInfo> keyInfoOpt = apiKeyConfig.findByKey(apiKey);

        if (keyInfoOpt.isEmpty()) {
            unauthorized(response, "API Key inválida");
            return;
        }

        ApiKeyInfo keyInfo = keyInfoOpt.get();
        tenantContext.setCurrentTenant(keyInfo.tenant());

        log.debug("Requisição autenticada para tenant: {} (key: {})",
                keyInfo.tenant(), keyInfo.name());

        try {
            filterChain.doFilter(request, response);
        } finally {
            tenantContext.clear();
        }
    }

    private String extractApiKey(HttpServletRequest request) {
        // Tentar header X-API-Key primeiro
        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null) {
            // Tentar Authorization Bearer
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                apiKey = auth.substring(7);
            }
        }

        return apiKey;
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    @ConfigurationProperties(prefix = "agent.security")
    @Component
    public static class ApiKeyConfig {

        private final List<ApiKeyInfo> apiKeys;

        @ConstructorBinding
        public ApiKeyConfig(List<ApiKeyInfo> apiKeys) {
            this.apiKeys = apiKeys != null ? apiKeys : List.of();
        }

        public Optional<ApiKeyInfo> findByKey(String key) {
            return apiKeys.stream()
                    .filter(info -> key.equals(info.key()))
                    .findFirst();
        }

        public List<ApiKeyInfo> getApiKeys() {
            return apiKeys;
        }
    }

    public record ApiKeyInfo(String key, String tenant, String name) {}
}
