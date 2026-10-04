package com.ai.demo.controller.prompting;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
* - Role prompting
* - Output Constraints
* - Few-shot prompting
* - Structured Prompting
* - Prompt Security Awareness (never take prompt in the request param,if taken validate it before passing to ai,
*   as it may be malicious prompt which might hack your system)
*/

@RestController
@RequestMapping("/api/prompting")
class PromptingTechniquesController {

    private final ChatClient chatClient;

    @Value("classpath:/prompts/few-shot-prompt.st")
    private Resource fewShotPrompt;

    @Value("classpath:/prompts/structured-output-prompt.st")
    private Resource structuredPrompt;

    PromptingTechniquesController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/required-output")
    String requiredOutputPrompt(@RequestParam String dish){

        return chatClient
                .prompt("Your are nutritionist, calculate the " +dish+ " calories, protein_grams, fat_grams."+
                        "The output should be of JSON like "+
                        "{dish: string, calories: numeric, protein_grams: numeric, fat_grams: numeric")
                .call()
                .content();
    }

    @GetMapping("/few-shot-prompt")
    String fewShotPrompt(@RequestParam String review){
        return chatClient
                .prompt(new PromptTemplate(fewShotPrompt)
                        .create(Map.of("review", review)))
                .call()
                .content();
    }

    @GetMapping("/structured-prompt")
    String structuredPrompt(){
        return chatClient
                .prompt(new PromptTemplate(structuredPrompt).create())
                .call()
                .content();
    }

    @GetMapping("/chained-prompt")
    String chainedBlogPost(@RequestParam String topic){
        String outline = chatClient.prompt()
                .user("Create a short 3-point outline for a blog post about " + topic + ". "
                        + "Respond with just 3 points, one per line, no extra commentary.")
                .call()
                .content();

        String blogPost = chatClient.prompt()
                .user(u->u.text(
                        """
                        Expand the following outline into a short blog post, with roughly one
                        paragraph per point.
                        
                        Outline:
                        {outline}
                        """
                ).param("outline", outline))
                .call()
                .content();

        return """
                --- Step 1 output: outline ---
                %s
                
                --- Step 2 output: expanded blog post (built from step 1's output)
                %s
                """.formatted(outline, blogPost);
    }

}