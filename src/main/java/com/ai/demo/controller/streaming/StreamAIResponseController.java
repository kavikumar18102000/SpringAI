package com.ai.demo.controller.streaming;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream")
class StreamAIResponseController {

    private final ChatClient chatClient;

    StreamAIResponseController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    //return each response as generated, without waiting for complete response like streaming every bit as generated.
    @GetMapping(value = "/sse-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> aiResponseStream(@RequestParam String topic){
        String prompt = """
                write 5 fun facts about %s, one per line.
                """.formatted(topic);

        return chatClient
                .prompt()
                .user(prompt)
                .stream()
                .content();

    }
}