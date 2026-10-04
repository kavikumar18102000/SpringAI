package com.ai.demo.controller.responseMapping;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/responseMap")
public class LLMResponseMappingController {

    private final ChatClient chatClient;


    public LLMResponseMappingController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/course-summary")
    CourseDetails courseSummary(@RequestParam String course){
        String prompt = """
                Create a course summary for a beginner-friendly course on "%s".
                Include a title, the difficulty level, a list of 4-6 learning objectives,
                and the estimated number of hours to complete the course.
                """.formatted(course);

        return chatClient.prompt(prompt)
                .call()
                .entity(CourseDetails.class);
    }
}

record CourseDetails(
        String title,
        String level,
        List<String> learningObjectives,
        int estimatedHours
){}
