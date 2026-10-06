package com.ai.demo.controller.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

record EmbedResponse(String text, int length, float[] vector) {}

@RestController
@RequestMapping("/api/semantic")
class SemanticSearch {

    private final EmbeddingModel embeddingModel;

    SemanticSearch(OpenAiEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/embedding")
    EmbedResponse embed(@RequestParam String message){
        /*EmbeddingResponse embeddingResponse = embeddingModel
                .embedForResponse(List.of(message));*/
        float[] embed = embeddingModel
                .embed(message);
        return new EmbedResponse(message, embed.length, embed);
    }
}