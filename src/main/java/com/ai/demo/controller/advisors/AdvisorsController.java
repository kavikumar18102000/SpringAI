package com.ai.demo.controller.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/*An advisor is a code, that can be called/executed before the request to AI or after the response from the AI.*/


@RestController
@RequestMapping("/api/advisors")
class AdvisorsController {

    private final ChatClient chatClient;

    AdvisorsController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/simple-log-advisor")
    String simpleLoggingAdvisor(@RequestParam String topic){
        SimpleLoggerAdvisor customLoggingAdvisor = SimpleLoggerAdvisor.builder()
                .requestToString(request -> "SENDING PROMPT: " + request.prompt().getContents())
                .responseToString(response -> "GOT BACK: " + response.getResult().getOutput().getText())
                .build();

        return chatClient.prompt()
                .user("Tell me a fun fact about this in 2 lines: "+topic)
//                .advisors(new SimpleLoggerAdvisor())
                .advisors(customLoggingAdvisor)
                .call().content();
    }

    //advisor blocks the call to the model provider if the user input or prompt contains any of the sensitive words.
    @GetMapping("/safe-guard-advisor")
    String safeGuardAdvisor(@RequestParam String topic){
        SafeGuardAdvisor safeGuardAdvisor = SafeGuardAdvisor.builder()
                .sensitiveWords(List.of("GUN", "BULLET", "DRUGS"))
                .failureResponse("Invalid topic, please provide a valid one...")
                .build();

        return chatClient.prompt()
                .user("Tell me a fun fact about this in 2 lines: "+topic)
//                .advisors(new SafeGuardAdvisor(List.of("GUN", "BULLET", "DRUGS")))
                .advisors(safeGuardAdvisor)
                .call().content();
    }

    //custom advisor --> mutate(add additional instructions based on role) the request
    @GetMapping("/custom-advisor")
    String customAdvisor(@RequestParam String role, @RequestParam String question){
        return chatClient.prompt()
                .user(question)
                .advisors(new CustomAdvisor(role, Ordered.HIGHEST_PRECEDENCE))
                .call()
                .content();
    }

    //disclaimer-advisor -> mutate the ai response
    @GetMapping("/disclaimer-advisor")
    String disClaimerAdvisor(@RequestParam String question){
        return chatClient.prompt()
                .user(question)
                .advisors(new DisclaimerAdvisor(Ordered.HIGHEST_PRECEDENCE))
                .call().content();
    }

    @GetMapping("/timing-advisor")
    String timingAdvisor(@RequestParam String question){
        return chatClient.prompt()
                .user(question)
                .advisors(new TimingAdvisor(Ordered.HIGHEST_PRECEDENCE))
                .call().content();
    }

    @GetMapping("/stream-timing-advisor")
    Flux<String> streamTimingAdvisor(@RequestParam String question){
        return chatClient.prompt()
                .user(question)
                .advisors(new StreamTimingAdvisor(Ordered.HIGHEST_PRECEDENCE))
                .stream()
                .content();
    }
}