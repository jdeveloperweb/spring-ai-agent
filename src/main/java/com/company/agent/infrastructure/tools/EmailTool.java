package com.company.agent.infrastructure.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTool implements Function<EmailTool.Request, EmailTool.Response> {

    @Override
    public Response apply(Request request) {
        log.info("Simulando envio de email para: {}", request.to());

        try {
            // Em um ambiente real, integraria com um provedor de email como SendGrid, SES, etc.
            // Por enquanto, apenas simula o envio

            validateEmail(request.to());

            log.info("Email enviado com sucesso:");
            log.info("  Para: {}", request.to());
            log.info("  Assunto: {}", request.subject());
            log.info("  Corpo: {}", request.body().substring(0, Math.min(100, request.body().length())));

            return new Response(true, "Email enviado com sucesso para " + request.to());

        } catch (Exception e) {
            log.error("Erro ao enviar email: {}", e.getMessage(), e);
            return new Response(false, "Erro ao enviar email: " + e.getMessage());
        }
    }

    private void validateEmail(List<String> emails) {
        for (String email : emails) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("Email inválido: " + email);
            }
        }
    }

    public record Request(
            List<String> to,
            String subject,
            String body,
            List<String> cc,
            List<String> bcc
    ) {}

    public record Response(
            boolean success,
            String message
    ) {}
}
