package com.ai.demo.controller.chatmemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.content.Content;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//store chat memory in DATABASE


record ChatMessageView(String role, String content){
    public static ChatMessageView from(Message message){
        return new ChatMessageView(message.getMessageType().getValue(), message.getText());
    }
}

@RestController
@RequestMapping("/api/persistent")
public class PersistentChatMemoryController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatMemoryRepository chatMemoryRepository;

    public PersistentChatMemoryController(ChatClient.Builder chatClientBuilder,
                                          ChatMemoryRepository chatMemoryRepository,
                                          ChatMemoryRepository chatMemoryRepository1) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        this.chatMemoryRepository = chatMemoryRepository1;
    }

    @GetMapping("/persistent-chat")
    String persistentChat(@RequestParam String conversationId, @RequestParam String message) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(a->a.advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @GetMapping("/conversations")
    List<String> getConversations(){
        return chatMemoryRepository
                .findConversationIds();
    }

    @GetMapping("/getConversation")
    List<ChatMessageView> getConversation(@RequestParam String conversationId){
        return chatMemoryRepository
                .findByConversationId(conversationId)
                .stream()
                .map(ChatMessageView::from)
                .toList();
    }

    @DeleteMapping("/conversation")
    ResponseEntity<String> deleteConversation(@RequestParam String conversationId){
        chatMemoryRepository.deleteByConversationId(conversationId);
        return ResponseEntity.ok("Deleted conversation's successfully....");
    }
}
