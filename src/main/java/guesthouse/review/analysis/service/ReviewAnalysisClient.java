package guesthouse.review.analysis.service;

import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiCategorizeRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiKeywordResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReportRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAnalysisClient {

    private static final Duration REVIEW_AI_TIMEOUT = Duration.ofSeconds(5);

    private final WebClient.Builder webClientBuilder;

    @Value("${review-ai.base-url:http://localhost:8000}")
    private String reviewAiBaseUrl;

    public Mono<Map<String, List<Long>>> categorize(ReviewAiCategorizeRequest request) {
        return categorizeStrict(request)
                .onErrorResume(this::handleCategorizeError)
                .defaultIfEmpty(Map.of());
    }

    public Mono<Map<String, List<Long>>> categorizeStrict(ReviewAiCategorizeRequest request) {
        return webClientBuilder.baseUrl(reviewAiBaseUrl)
                .build()
                .post()
                .uri("/categorize")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<Long>>>() {
                })
                .timeout(REVIEW_AI_TIMEOUT);
    }

    public Mono<List<ReviewAiKeywordResponse>> keywords(ReviewAiCategorizeRequest request) {
        return keywordsStrict(request)
                .onErrorResume(this::handleKeywordsError)
                .defaultIfEmpty(List.of());
    }

    public Mono<List<ReviewAiKeywordResponse>> keywordsStrict(ReviewAiCategorizeRequest request) {
        return webClientBuilder.baseUrl(reviewAiBaseUrl)
                .build()
                .post()
                .uri("/keywords")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReviewAiKeywordResponse>>() {
                })
                .timeout(REVIEW_AI_TIMEOUT);
    }

    public String report(ReviewAiReportRequest request) {
        try {
            ReviewAiReportResponse response = reportStrict(request).block(REVIEW_AI_TIMEOUT);

            return response == null ? "" : response.report();
        } catch (WebClientResponseException | WebClientRequestException | IllegalStateException e) {
            log.warn("Failed to build review report with review AI server", e);
            return "";
        }
    }

    public Mono<ReviewAiReportResponse> reportStrict(ReviewAiReportRequest request) {
        return webClientBuilder.baseUrl(reviewAiBaseUrl)
                .build()
                .post()
                .uri("/report")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ReviewAiReportResponse.class)
                .timeout(REVIEW_AI_TIMEOUT);
    }

    private Mono<Map<String, List<Long>>> handleCategorizeError(Throwable e) {
        log.warn("Failed to categorize reviews with review AI server", e);
        return Mono.just(Map.of());
    }

    private Mono<List<ReviewAiKeywordResponse>> handleKeywordsError(Throwable e) {
        log.warn("Failed to extract review keywords with review AI server", e);
        return Mono.just(List.of());
    }
}
