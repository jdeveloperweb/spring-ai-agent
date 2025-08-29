package com.company.agent.infrastructure.ai;

import com.company.agent.infrastructure.tools.DatabaseTool;
import com.company.agent.infrastructure.tools.EmailTool;
import com.company.agent.infrastructure.tools.FileTool;
import com.company.agent.infrastructure.tools.HttpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SpringAiConfig {

    private final OpenAiChatModel openAiChatModel;
    private final HttpTool httpTool;
    private final EmailTool emailTool;
    private final FileTool fileTool;
    private final DatabaseTool databaseTool;

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem("Você é um agente executor de tarefas inteligente. " +
                        "Use as ferramentas disponíveis para completar as tarefas solicitadas. " +
                        "Seja preciso, eficiente e mantenha um registro das ações executadas.")
                .defaultFunctions(
                        httpTool,
                        emailTool,
                        fileTool,
                        databaseTool
                )
                .build();
    }
}
