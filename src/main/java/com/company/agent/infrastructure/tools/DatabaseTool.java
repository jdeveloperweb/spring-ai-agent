package com.company.agent.infrastructure.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseTool implements Function<DatabaseTool.Request, DatabaseTool.Response> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Response apply(Request request) {
        log.info("Executando query: {}", request.query().substring(0, Math.min(100, request.query().length())));

        try {
            // Validar query para evitar operações perigosas
            validateQuery(request.query());

            if (isSelectQuery(request.query())) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(request.query());

                return new Response(true,
                        "Query executada com sucesso. Retornadas " + results.size() + " linhas",
                        results);
            } else {
                int rowsAffected = jdbcTemplate.update(request.query());

                return new Response(true,
                        "Query executada com sucesso. " + rowsAffected + " linhas afetadas",
                        List.of(Map.of("rowsAffected", rowsAffected)));
            }

        } catch (Exception e) {
            log.error("Erro na execução da query: {}", e.getMessage(), e);
            return new Response(false, "Erro na execução: " + e.getMessage(), null);
        }
    }

    private void validateQuery(String query) {
        String upperQuery = query.trim().toUpperCase();

        // Lista de comandos perigosos não permitidos
        List<String> forbidden = List.of(
                "DROP", "TRUNCATE", "ALTER", "CREATE",
                "GRANT", "REVOKE", "EXEC", "EXECUTE"
        );

        for (String cmd : forbidden) {
            if (upperQuery.startsWith(cmd + " ")) {
                throw new SecurityException("Comando não permitido: " + cmd);
            }
        }

        // Apenas permitir SELECT, INSERT, UPDATE, DELETE em tabelas específicas
        if (!upperQuery.matches("^(SELECT|INSERT|UPDATE|DELETE)\\s+.*")) {
            throw new SecurityException("Apenas SELECT, INSERT, UPDATE e DELETE são permitidos");
        }
    }

    private boolean isSelectQuery(String query) {
        return query.trim().toUpperCase().startsWith("SELECT");
    }

    public record Request(
            String query
    ) {}

    public record Response(
            boolean success,
            String message,
            List<Map<String, Object>> data
    ) {}
}
