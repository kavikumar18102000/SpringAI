package com.ai.demo.controller.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/*
Cosine Similarity(how similar the things are) (0 & 1)

1. 1.0 -> same meaning
2. 0.5 -> loosely related
3. 0.0 -> completely unrelated
*/

record EmbedResponse(String text, int length, float[] vector) {}

record SimilarityResult(String text1, String text2, double similarity) {}

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

    @GetMapping("/similarity")
    SimilarityResult similarity(@RequestParam String text1, @RequestParam String text2){
        float[] embed = embeddingModel.embed(text1);
        float[] embed1 = embeddingModel.embed(text2);
        double cosineSimilarity = SimpleVectorStore.EmbeddingMath.cosineSimilarity(embed, embed1);
        return new SimilarityResult(text1, text2, cosineSimilarity);
    }
}