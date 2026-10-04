package com.ai.demo.controller.chatmemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//By default, AI models are stateless(ai doesn't remember previous conversation)
//ChatMemory is used to solve that issue in JAVA.
//We pass the previous chat along with the prompt to LLM.
//NOTE: LLM doesn't have memory by default(they are stateless).

@RestController
@RequestMapping("/api/memory")
public class ChatMemoryController {

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;

    public ChatMemoryController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
    }


    @GetMapping("/first-chat")
    String firstChatMemory(@RequestParam String conversationId, @RequestParam String message){
        return chatClient.prompt()
                .user(message)
                //every chat with LLM will have a conversation ID, for future use
                .advisors(a->a.advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
