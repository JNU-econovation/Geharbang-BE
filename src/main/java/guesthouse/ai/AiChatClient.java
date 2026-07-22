package guesthouse.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.ai.dto.AiChatGatewayRequest;
import guesthouse.ai.dto.AiChatGatewayResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
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
    private final ObjectMapper objectMapper;

    @Value("${geharbang-ai.base-url:http://localhost:8001}")
    private String aiBaseUrl;

    public AiChatGatewayResponse chat(AiChatGatewayRequest request) {
        return call(() -> webClientBuilder.baseUrl(aiBaseUrl)
                .build()
                .post()
                .uri("/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiChatGatewayResponse.class)
                .block(AI_TIMEOUT));
    }

    public AiChatGatewayResponse chatWithImage(
            AiChatGatewayRequest request,
            MultipartFile image
    ) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("message", request.message());
        if (request.sessionId() != null) {
            body.part("sessionId", request.sessionId());
        }
        if (request.context() != null) {
            body.part("context", writeContext(request.context()));
        }
        body.part("image", image.getResource())
                .filename(image.getOriginalFilename() == null ? "image" : image.getOriginalFilename())
                .contentType(resolveMediaType(image.getContentType()));

        return call(() -> webClientBuilder.baseUrl(aiBaseUrl)
                .build()
                .post()
                .uri("/chat/image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body.build()))
                .retrieve()
                .bodyToMono(AiChatGatewayResponse.class)
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

    private String writeContext(Map<String, Object> context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("AI 대화 문맥을 전송할 수 없습니다.", error);
        }
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return MediaType.parseMediaType(contentType);
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
