package guesthouse.review.analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.exception.GuestHousePostErrorCode;
import guesthouse.guestHousePost.exception.GuestHousePostException;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.review.analysis.domain.ReviewAnalysisCache;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiCategorizeRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiKeywordResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReportRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReportResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReviewRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewCategoryGroupResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewInsightsResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewKeywordGroupResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewReportResponse;
import guesthouse.review.analysis.repository.ReviewAnalysisCacheRepository;
import guesthouse.review.domain.model.Review;
import guesthouse.review.domain.vo.ReviewStatus;
import guesthouse.review.repository.ReviewRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.exception.UserErrorCode;
import guesthouse.user.exception.UserException;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private static final Duration REVIEW_AI_TIMEOUT = Duration.ofSeconds(6);
    private static final String REVIEW_REPORT_EMPTY_MESSAGE =
            "아직 분석할 리뷰가 없어요. 리뷰가 쌓이면 장점/단점 리포트를 확인할 수 있습니다.";
    private static final String REVIEW_AI_REPORT_FALLBACK =
            "리뷰 분석 서버와 연결할 수 없어 리포트를 생성하지 못했습니다. 잠시 후 다시 시도해주세요.";

    private final ReviewRepository reviewRepository;
    private final GuestHousePostRepository guestHousePostRepository;
    private final UserService userService;
    private final ReviewAnalysisClient reviewAnalysisClient;
    private final ReviewAnalysisCacheRepository cacheRepository;
    private final ObjectMapper objectMapper;
    private final Map<Long, Object> refreshLocks = new ConcurrentHashMap<>();

    public ReviewInsightsResponse getGuestHouseReviewInsights(Long guestHousePostId) {
        AnalysisSource source = loadSource(guestHousePostId);
        ReviewAnalysisCache cache = getFreshCache(source);
        if (cache == null) {
            cache = refreshOrKeepPrevious(source);
        }
        return cache == null ? ReviewInsightsResponse.empty() : toInsightsResponse(cache);
    }

    public ReviewReportResponse getGuestHouseReviewReport(Long guestHousePostId, Long userId) {
        GuestHousePost guestHousePost = getGuestHousePost(guestHousePostId);
        validateReportAccess(guestHousePost, userId);

        AnalysisSource source = loadSource(guestHousePost);
        ReviewAnalysisCache cache = getFreshCache(source);
        if (cache == null) {
            cache = refreshOrKeepPrevious(source);
        }

        if (cache == null) {
            return new ReviewReportResponse(
                    guestHousePostId,
                    guestHousePost.getGuestHouseName(),
                    source.reviews().size(),
                    source.reviews().isEmpty() ? REVIEW_REPORT_EMPTY_MESSAGE : REVIEW_AI_REPORT_FALLBACK
            );
        }

        return new ReviewReportResponse(
                guestHousePostId,
                guestHousePost.getGuestHouseName(),
                cache.getReviewCount(),
                cache.getReport()
        );
    }

    public void refreshGuestHouseReviewAnalysis(Long guestHousePostId) {
        synchronized (lockFor(guestHousePostId)) {
            AnalysisSource source = loadSource(guestHousePostId);
            if (getFreshCache(source) == null) {
                refresh(source, 0);
            }
        }
    }

    private ReviewAnalysisCache refreshOrKeepPrevious(AnalysisSource source) {
        synchronized (lockFor(source.guestHousePost().getId())) {
            AnalysisSource latestSource = loadSource(source.guestHousePost().getId());
            ReviewAnalysisCache freshCache = getFreshCache(latestSource);
            if (freshCache != null) {
                return freshCache;
            }

            ReviewAnalysisCache previous = cacheRepository.findById(source.guestHousePost().getId()).orElse(null);
            try {
                return refresh(latestSource, 0);
            } catch (RuntimeException exception) {
                log.warn("Review analysis refresh failed; keeping the previous result: guestHousePostId={}",
                        source.guestHousePost().getId(), exception);
                return previous;
            }
        }
    }

    private ReviewAnalysisCache refresh(AnalysisSource source, int retryCount) {
        ReviewInsightsResponse insights;
        String report;

        if (source.reviews().isEmpty()) {
            insights = ReviewInsightsResponse.empty();
            report = REVIEW_REPORT_EMPTY_MESSAGE;
        } else {
            ReviewAiCategorizeRequest request = new ReviewAiCategorizeRequest(toAiReviewRequests(source.reviews()));
            ReviewAiReportRequest reportRequest = new ReviewAiReportRequest(
                    source.guestHousePost().getGuestHouseName(),
                    source.reviews().stream().map(Review::getContent).toList()
            );

            var result = Mono.zip(
                            reviewAnalysisClient.categorizeStrict(request),
                            reviewAnalysisClient.keywordsStrict(request),
                            reviewAnalysisClient.reportStrict(reportRequest)
                    )
                    .block(REVIEW_AI_TIMEOUT);

            if (result == null) {
                throw new IllegalStateException("Review AI returned an empty response");
            }

            Map<String, List<Long>> categories = result.getT1();
            List<ReviewAiKeywordResponse> keywords = result.getT2();
            ReviewAiReportResponse reportResponse = result.getT3();
            insights = new ReviewInsightsResponse(
                    ReviewAnalysisDtos.toCategoryGroups(categories),
                    ReviewAnalysisDtos.toKeywordGroups(keywords)
            );
            report = reportResponse.report();
            if (report == null || report.isBlank()) {
                throw new IllegalStateException("Review AI returned an empty report");
            }
        }

        AnalysisSource latestSource = loadSource(source.guestHousePost().getId());
        if (!latestSource.fingerprint().equals(source.fingerprint())) {
            if (retryCount >= 2) {
                throw new IllegalStateException("Reviews kept changing while analysis was running");
            }
            log.info("Review changed while analysis was running; retrying with latest reviews: guestHousePostId={}",
                    source.guestHousePost().getId());
            return refresh(latestSource, retryCount + 1);
        }

        return saveCache(source, insights, report);
    }

    private ReviewAnalysisCache saveCache(
            AnalysisSource source,
            ReviewInsightsResponse insights,
            String report
    ) {
        try {
            String categoriesJson = objectMapper.writeValueAsString(insights.categories());
            String keywordsJson = objectMapper.writeValueAsString(insights.keywords());
            ReviewAnalysisCache cache = cacheRepository.findById(source.guestHousePost().getId())
                    .orElseGet(() -> new ReviewAnalysisCache(
                            source.guestHousePost().getId(),
                            source.fingerprint(),
                            categoriesJson,
                            keywordsJson,
                            report,
                            source.reviews().size()
                    ));
            cache.update(
                    source.fingerprint(),
                    categoriesJson,
                    keywordsJson,
                    report,
                    source.reviews().size()
            );
            return cacheRepository.save(cache);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize review analysis", exception);
        }
    }

    private ReviewInsightsResponse toInsightsResponse(ReviewAnalysisCache cache) {
        try {
            List<ReviewCategoryGroupResponse> categories = objectMapper.readValue(
                    cache.getCategoriesJson(),
                    new TypeReference<>() {
                    }
            );
            List<ReviewKeywordGroupResponse> keywords = objectMapper.readValue(
                    cache.getKeywordsJson(),
                    new TypeReference<>() {
                    }
            );
            return new ReviewInsightsResponse(categories, keywords);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to deserialize cached review insights: guestHousePostId={}",
                    cache.getGuestHousePostId(), exception);
            return ReviewInsightsResponse.empty();
        }
    }

    private ReviewAnalysisCache getFreshCache(AnalysisSource source) {
        return cacheRepository.findById(source.guestHousePost().getId())
                .filter(cache -> cache.getSourceFingerprint().equals(source.fingerprint()))
                .orElse(null);
    }

    private AnalysisSource loadSource(Long guestHousePostId) {
        return loadSource(getGuestHousePost(guestHousePostId));
    }

    private AnalysisSource loadSource(GuestHousePost guestHousePost) {
        List<Review> reviews = reviewRepository
                .findByGuestHousePostIdAndStatus(guestHousePost.getId(), ReviewStatus.ACTIVE)
                .stream()
                .sorted(Comparator.comparing(Review::getId))
                .toList();
        return new AnalysisSource(guestHousePost, reviews, fingerprint(guestHousePost, reviews));
    }

    private String fingerprint(GuestHousePost guestHousePost, List<Review> reviews) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateDigest(digest, guestHousePost.getGuestHouseName());
            for (Review review : reviews) {
                updateDigest(digest, String.valueOf(review.getId()));
                updateDigest(digest, review.getContent());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not supported", exception);
        }
    }

    private void updateDigest(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update((byte) (bytes.length >>> 24));
        digest.update((byte) (bytes.length >>> 16));
        digest.update((byte) (bytes.length >>> 8));
        digest.update((byte) bytes.length);
        digest.update(bytes);
    }

    private List<ReviewAiReviewRequest> toAiReviewRequests(List<Review> reviews) {
        return reviews.stream()
                .map(review -> new ReviewAiReviewRequest(review.getId(), review.getContent()))
                .toList();
    }

    private Object lockFor(Long guestHousePostId) {
        return refreshLocks.computeIfAbsent(guestHousePostId, ignored -> new Object());
    }

    private GuestHousePost getGuestHousePost(Long guestHousePostId) {
        return guestHousePostRepository.findById(guestHousePostId)
                .orElseThrow(() -> new GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND));
    }

    private void validateReportAccess(GuestHousePost guestHousePost, Long userId) {
        User user = userService.findById(userId);
        if (user.isAdmin() || guestHousePost.getOwnerId().equals(userId)) {
            return;
        }
        throw new UserException(UserErrorCode.NOT_APPROVED_OWNER);
    }

    private record AnalysisSource(
            GuestHousePost guestHousePost,
            List<Review> reviews,
            String fingerprint
    ) {
    }
}
