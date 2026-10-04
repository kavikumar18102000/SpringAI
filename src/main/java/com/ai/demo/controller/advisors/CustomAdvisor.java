package com.ai.demo.controller.advisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;

public class CustomAdvisor implements BaseAdvisor {

    private final String role;
    private final int order;

    public CustomAdvisor(String role,  int order) {
        this.role = role;
        this.order = order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

        //added another layer of instruction, such that it appended not replaced to the main system prompt.
        Prompt augmentedSystemMessage = chatClientRequest.prompt()
                .augmentSystemMessage("You are " + role + ". Stay in character while answering. "
                        + "Answer about the topic in 2 lines max.");

        return chatClientRequest
                .mutate()
                .prompt(augmentedSystemMessage)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }


    @Override
    public int getOrder() {
        return this.order;
    }
}
