package com.ai.demo.controller.embedding;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/*
Cosine Similarity(how similar the things are) (0 & 1)

1. 1.0 -> same meaning
2. 0.5 -> loosely related
3. 0.0 -> completely unrelated
*/

record EmbedResponse(String text, int length, float[] vector) {}

record SimilarityResult(String text1, String text2, double similarity) {}

record DocumentResult(String id, String text, Double score){
    public static DocumentResult documentResult(Document document){
        return new DocumentResult(document.getId(), document.getText(), document.getScore());
    }
}

@RestController
@RequestMapping("/api/semantic")
class SemanticSearch {

    private final EmbeddingModel embeddingModel;
    private final VectorStore  vectorStore;

    SemanticSearch(OpenAiEmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
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

    /*@PostConstruct
    void seed(){
        vectorStore.add(List.of(
                new Document("Spring AI provides a unified Java API for interacting with AI language models"),
                new Document("ChatClient is the main interface for sending prompts and receiving responses from AI")
                *//*new Document("Advisors in Spring AI intercept and modify prompts and responses in a chain"),
                new Document("VectorStore stores document embeddings for semantic similarity search"),
                new Document("EmbeddingModel converts text into high-dimensional numerical vectors called embeddings"),
                new Document("RAG stands for Retrieval Augmented Generation: retrieve relevant docs, then generate an answer"),
                new Document("ChatMemory stores conversation history to enable multi-turn conversations"),
                new Document("PromptTemplate allows parameterized prompt construction with named variables"),
                new Document("Structured output maps AI responses directly into Java records using entity()"),
                new Document("Streaming responses use Flux to deliver tokens to the client as they are generated"),
                new Document("SimpleVectorStore is an in-memory vector store backed by a ConcurrentHashMap"),
                new Document("Cosine similarity measures the angle between two vectors: 1.0 means identical, 0.0 means unrelated")*//*
        ));
    }*/

    //adding metadata for more precise results
    @PostConstruct
    void seed() {
        vectorStore.add(List.of(
                /*new Document("Spring AI provides a unified Java API for AI models",
                        Map.of("category", "framework")),
                new Document("ChatClient is the main interface for sending prompts to AI",
                        Map.of("category", "framework")),
                new Document("VectorStore stores document embeddings for semantic search",
                        Map.of("category", "framework")),
                new Document("Your invoice is generated on the 1st of every month",
                        Map.of("category", "billing")),
                new Document("You can cancel your subscription from account settings",
                        Map.of("category", "billing")),
                new Document("Refunds are processed within 5-7 business days",
                        Map.of("category", "billing")),*/
                new Document("To reset your password click forgot password on login",
                        Map.of("category", "account")),
                new Document("Two factor authentication can be enabled in security settings",
                        Map.of("category", "account"))
        ));
    }

    @GetMapping("/semantic-search")
    List<DocumentResult> semanticSearch(@RequestParam String query, @RequestParam(defaultValue = "4") int topK){
        return vectorStore
//                .similaritySearch(query)
                .similaritySearch(
                        SearchRequest.builder().query(query).topK(topK).build()
                )
                .stream()
                .map(DocumentResult::documentResult)
                .toList();
    }

    @GetMapping("/semantic-search/metadata")
    List<DocumentResult> metadataSearch(@RequestParam String query, @RequestParam(defaultValue = "4") int topK
        ,@RequestParam String metaData){

        SearchRequest.Builder request = SearchRequest.builder()
                .query(query)
                .topK(topK);

        if (Objects.nonNull(metaData)){
                request.filterExpression("category == '" + metaData + "'");
        }

        return vectorStore
                .similaritySearch(request.build())
                .stream()
                .map(DocumentResult::documentResult)
                .toList();
    }


}