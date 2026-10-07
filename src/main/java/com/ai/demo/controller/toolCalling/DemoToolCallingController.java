package com.ai.demo.controller.toolCalling;

import com.ai.demo.controller.tools.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tool-calling")
public class DemoToolCallingController {

    private final ChatClient chatClient;

    public DemoToolCallingController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/dateTime")
    String callDateTimeTool(@RequestParam String query){
        return chatClient.prompt()
                .user(query)
                .tools(new DateTimeTools())
                .call()
                .content();
    }
}
