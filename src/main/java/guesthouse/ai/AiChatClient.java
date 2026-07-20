package guesthouse.ai;

import guesthouse.ai.dto.AiChatRequest;
import guesthouse.ai.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatClient {

    private static final Duration AI_TIMEOUT = Duration.ofSeconds(90);

    private final WebClient.Builder webClientBuilder;

    @Value("${geharbang-ai.base-url:http://localhost:8001}")
    private String aiBaseUrl;

    public AiChatResponse chat(AiChatRequest request) {
        return call(() -> webClientBuilder.baseUrl(aiBaseUrl)
                .build()
                .post()
                .uri("/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiChatResponse.class)
                .block(AI_TIMEOUT));
    }

    public Map<String, Object> health() {
        return call(() -> webClientBuilder.baseUrl(aiBaseUrl)
                .build()
                .get()
                .uri("/health")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block(Duration.ofSeconds(5)));
    }

    public void reset(String sessionId) {
        call(() -> {
            webClientBuilder.baseUrl(aiBaseUrl)
                    .build()
                    .delete()
                    .uri("/chat/{sessionId}", sessionId)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(5));
            return Boolean.TRUE;
        });
    }

    private <T> T call(AiCall<T> operation) {
        try {
            T response = operation.execute();
            if (response == null) {
                throw new IllegalStateException("AI server returned an empty response");
            }
            return response;
        } catch (RuntimeException error) {
            log.warn("Failed to communicate with Geharbang AI server", error);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI 챗봇 서버에 일시적으로 연결할 수 없습니다."
            );
        }
    }

    @FunctionalInterface
    private interface AiCall<T> {
        T execute();
    }
}
