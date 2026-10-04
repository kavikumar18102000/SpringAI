package com.ai.demo.controller.advisors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

public class TimingAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(TimingAdvisor.class);

    private final int order;

    public TimingAdvisor(int order) {
        this.order = order;
    }


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        long start = System.currentTimeMillis();

        //by using call advisor instead of base advisor, we have control what to happen next in the stack.
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        log.info("[TIMING ADVISOR] call took {}ms.",(System.currentTimeMillis()-start));
        return chatClientResponse;
    }

    @Override
    public String getName() {
        return "TIMING ADVISOR";
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
