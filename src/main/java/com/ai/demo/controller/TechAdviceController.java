package com.ai.demo.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
class TechAdviceController {

    private final ChatClient chatClient;

    private final ChatClient chatClientWithDefaultSystem;

    private final ChatModel chatModel;

    @Value("classpath:/prompts/recipe-prompt.st")
    private Resource promptResource;

    public TechAdviceController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();

        //useful for production grade app for centralized AI behavior for the model
        //we same builder for two variables, so the default system will set for both the instance
        // and response might be different from expected, so clone the builder
        this.chatClientWithDefaultSystem = chatClientBuilder.clone()
                .defaultSystem("You are a strict technical interviewer conducting a Java interview. " +
                        "Ask probing follow-up questions in your response.")
                .build();

        this.chatModel = OpenAiChatModel.builder().build();
    }

    @GetMapping("/advise")
    String advise(){
        return chatClient
                .prompt()
                .user("Explain what is Spring AI in simple terms?")
                .call()
                .content();
    }

    @GetMapping("/default-system-prompt")
    String systemPrompt(@RequestParam String userQuery){
        return chatClientWithDefaultSystem
                .prompt()
                .user(userQuery)
                .call()
                .content();
    }

    @GetMapping("/prompt-variables")
    String promptTemplate(@RequestParam String language, String task){

        PromptTemplate promptTemplate = new PromptTemplate("""
                Write a short {language} code snippet that demonstrates how to {task}.
                Include comments explaining each step.
                """);

        Prompt prompt = promptTemplate.create(
                Map.of("language", language,
                        "task", task)
        );

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }

    @GetMapping("/inline-prompt-variables")
    String inlinePromptTemplate(@RequestParam String language, String task){
        return "<prev>" + chatClient
                .prompt()
                .user(u->u.text("""
                        Write a short {language} code snippet that demonstrates how to {task}.
                        Include comments explaining each step.
                        """)
                        .param("language", language)
                        .param("task", task))
                .call()
                .content() + "<prev>";
    }

    @GetMapping("/resource-prompt")
    String resourcePrompt(@RequestParam String ingredient){

        Prompt prompt = new PromptTemplate(promptResource)
                .create(Map.of("ingredient", ingredient));

        return chatClient
                .prompt(prompt)
                .call().content();
    }

    //Building conversation
    @GetMapping("/conversation-history")
    String conversation(@RequestParam String followUpQuestion){
        Message systemMessage = new SystemMessage("You are a experienced Java tutor.");
        Message userMessage = new UserMessage("Explain what is a Java record in simple terms?");

        Message priorMessage = new AssistantMessage("A record is a compact way to declare an immutable class."
        +"e.g. `record Point(int x, int y){}`");

        Message newUserMessage = new UserMessage(followUpQuestion);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, priorMessage, newUserMessage));

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }

    @GetMapping("/chat-model")
    String useChatModel(){
        return chatModel
                .call("What is composition, explain in simple terms in JAVA..");

    }
}