package com.company.agent.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.firebird.url")
public class FirebirdDataSourceConfig {

    @Value("${spring.datasource.firebird.url}")
    private String firebirdUrl;

    @Value("${spring.datasource.firebird.username}")
    private String firebirdUsername;

    @Value("${spring.datasource.firebird.password}")
    private String firebirdPassword;

    @Bean(name = "firebirdDataSource")
    public DataSource firebirdDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(firebirdUrl);
        config.setUsername(firebirdUsername);
        config.setPassword(firebirdPassword);
        config.setDriverClassName("org.firebirdsql.jdbc.FBDriver");

        // Configurações de pool otimizadas para Firebird
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // Configurações específicas do Firebird
        config.addDataSourceProperty("encoding", "UTF8");
        config.addDataSourceProperty("charSet", "UTF-8");

        // Teste de conexão
        config.setConnectionTestQuery("SELECT 1 FROM RDB$DATABASE");

        return new HikariDataSource(config);
    }
}
