package next.domo.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LlamaService implements AIService{

    private final WebClient webClient;

    public LlamaService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String ask(String userPrompt) {
        String requestBody = """
                {
                  "model": "llama3:latest",
                  "prompt": "%s",
                  "stream": false
                }
                """.formatted(userPrompt);

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 블로킹 방식
    }
}