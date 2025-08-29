package com.company.agent.infrastructure.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@Component
@Slf4j
public class FileTool implements Function<FileTool.Request, FileTool.Response> {

    private static final String ALLOWED_BASE_PATH = "/tmp/agent-files/";

    @Override
    public Response apply(Request request) {
        log.info("Executando operação de arquivo: {} em {}", request.operation(), request.path());

        try {
            Path filePath = validateAndResolvePath(request.path());

            return switch (request.operation().toUpperCase()) {
                case "READ" -> readFile(filePath);
                case "WRITE" -> writeFile(filePath, request.content());
                case "DELETE" -> deleteFile(filePath);
                case "LIST" -> listDirectory(filePath);
                case "EXISTS" -> checkExists(filePath);
                default -> new Response(false, "Operação não suportada: " + request.operation(), null);
            };

        } catch (Exception e) {
            log.error("Erro na operação de arquivo: {}", e.getMessage(), e);
            return new Response(false, "Erro: " + e.getMessage(), null);
        }
    }

    private Path validateAndResolvePath(String path) {
        Path resolved = Paths.get(ALLOWED_BASE_PATH).resolve(path).normalize();

        if (!resolved.startsWith(ALLOWED_BASE_PATH)) {
            throw new SecurityException("Caminho não permitido: " + path);
        }

        return resolved;
    }

    private Response readFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new Response(false, "Arquivo não encontrado: " + path, null);
        }

        if (Files.isDirectory(path)) {
            return new Response(false, "Caminho é um diretório, não um arquivo", null);
        }

        String content = Files.readString(path);
        return new Response(true, "Arquivo lido com sucesso", content);
    }

    private Response writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content != null ? content : "");
        return new Response(true, "Arquivo escrito com sucesso", null);
    }

    private Response deleteFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new Response(false, "Arquivo não encontrado: " + path, null);
        }

        Files.delete(path);
        return new Response(true, "Arquivo deletado com sucesso", null);
    }

    private Response listDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new Response(false, "Diretório não encontrado: " + path, null);
        }

        if (!Files.isDirectory(path)) {
            return new Response(false, "Caminho não é um diretório", null);
        }

        StringBuilder listing = new StringBuilder();
        Files.list(path).forEach(p -> {
            String type = Files.isDirectory(p) ? "[DIR]" : "[FILE]";
            listing.append(type).append(" ").append(p.getFileName()).append("\n");
        });

        return new Response(true, "Listagem do diretório", listing.toString());
    }

    private Response checkExists(Path path) {
        boolean exists = Files.exists(path);
        String type = Files.isDirectory(path) ? "directory" : "file";
        return new Response(true, "Existe: " + exists + " (tipo: " + type + ")", String.valueOf(exists));
    }

    public record Request(
            String operation, // READ, WRITE, DELETE, LIST, EXISTS
            String path,
            String content
    ) {}

    public record Response(
            boolean success,
            String message,
            String content
    ) {}
}
