package com.company.agent.application;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TemplateEngine {

    private final Handlebars handlebars;
    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();

    public TemplateEngine() {
        this.handlebars = new Handlebars();

        // Registrar helpers customizados
        handlebars.registerHelper("upper", (context, options) ->
                context != null ? context.toString().toUpperCase() : "");

        handlebars.registerHelper("lower", (context, options) ->
                context != null ? context.toString().toLowerCase() : "");

        handlebars.registerHelper("defaultValue", (context, options) ->
                context != null ? context : options.param(0, ""));
    }

    public String render(String templateString, Map<String, Object> variables) {
        if (templateString == null || templateString.trim().isEmpty()) {
            return templateString;
        }

        try {
            // Usar cache para templates compilados
            Template template = templateCache.computeIfAbsent(templateString, this::compileTemplate);

            if (template == null) {
                log.warn("Falha ao compilar template, retornando string original");
                return templateString;
            }

            return template.apply(variables != null ? variables : Map.of());

        } catch (Exception e) {
            log.error("Erro ao renderizar template: {}", e.getMessage(), e);
            return templateString; // Fallback para template original
        }
    }

    private Template compileTemplate(String templateString) {
        try {
            return handlebars.compileInline(templateString);
        } catch (IOException e) {
            log.error("Erro ao compilar template: {}", e.getMessage());
            return null;
        }
    }

    // Método utilitário para aplicar variáveis simples (sem Handlebars)
    public static String applySimple(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }
}
