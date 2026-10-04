package com.ai.demo.controller.advisors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

public class StreamTimingAdvisor implements StreamAdvisor {

    private final static Logger log = LoggerFactory.getLogger(StreamTimingAdvisor.class);
    private final int order;

    public StreamTimingAdvisor(int order) {
        this.order = order;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        long start = System.currentTimeMillis();
        return streamAdvisorChain
                .nextStream(chatClientRequest)
                .doOnComplete(() -> log.info("[STREAM_TIMING_ADVISOR] took {}ms", (System.currentTimeMillis() - start)));
    }

    @Override
    public String getName() {
        return "STREAM_TIMING_ADVISOR";
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
