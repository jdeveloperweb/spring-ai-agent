package com.company.agent.infrastructure.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpTool implements Function<HttpTool.Request, HttpTool.Response> {

    private final WebClient webClient;
    private Set<String> allowedDomains = Set.of(
            "https://httpbin.org",
            "https://jsonplaceholder.typicode.com",
            "https://api.github.com"
    );

    @Override
    public Response apply(Request request) {
        log.info("Executando HTTP {} para {}", request.method(), request.url());

        try {
            // Validar domínio permitido
            URI uri = URI.create(request.url());
            String baseUrl = uri.getScheme() + "://" + uri.getHost();

            if (!allowedDomains.contains(baseUrl)) {
                return new Response(false, "Domínio não permitido: " + baseUrl, null, 403);
            }

            // Executar requisição
            WebClient.ResponseSpec responseSpec = switch (request.method().toUpperCase()) {
                case "GET" -> webClient.get()
                        .uri(request.url())
                        .headers(h -> request.headers().forEach(h::add))
                        .retrieve();

                case "POST" -> webClient.post()
                        .uri(request.url())
                        .headers(h -> request.headers().forEach(h::add))
                        .bodyValue(request.body() != null ? request.body() : "")
                        .retrieve();

                case "PUT" -> webClient.put()
                        .uri(request.url())
                        .headers(h -> request.headers().forEach(h::add))
                        .bodyValue(request.body() != null ? request.body() : "")
                        .retrieve();

                case "DELETE" -> webClient.delete()
                        .uri(request.url())
                        .headers(h -> request.headers().forEach(h::add))
                        .retrieve();

                default -> throw new IllegalArgumentException("Método HTTP não suportado: " + request.method());
            };

            String responseBody = responseSpec.bodyToMono(String.class).block();

            return new Response(true, "Requisição executada com sucesso", responseBody, 200);

        } catch (Exception e) {
            log.error("Erro na requisição HTTP: {}", e.getMessage(), e);
            return new Response(false, "Erro: " + e.getMessage(), null, 500);
        }
    }

    public HttpTool withAllowedDomains(Set<String> domains) {
        HttpTool newTool = new HttpTool(webClient);
        newTool.allowedDomains = domains;
        return newTool;
    }

    public record Request(
            String url,
            String method,
            Map<String, String> headers,
            String body
    ) {}

    public record Response(
            boolean success,
            String message,
            String body,
            int statusCode
    ) {}
}
