package com.company.agent.api;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController implements HealthIndicator {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Health health = health();

        Map<String, Object> response = Map.of(
                "status", health.getStatus().getCode(),
                "timestamp", LocalDateTime.now(),
                "details", health.getDetails()
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public Health health() {
        try {
            // Verificar conexão com banco
            try (Connection conn = dataSource.getConnection()) {
                conn.createStatement().execute("SELECT 1");
            }

            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("timestamp", LocalDateTime.now())
                    .withDetail("service", "spring-ai-agent")
                    .withDetail("version", "1.0.0-SNAPSHOT")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
        }
    }
}