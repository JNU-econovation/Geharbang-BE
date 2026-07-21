package guesthouse.review.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.review.analysis.domain.ReviewAnalysisCache;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiKeywordResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReportResponse;
import guesthouse.review.analysis.repository.ReviewAnalysisCacheRepository;
import guesthouse.review.domain.model.Review;
import guesthouse.review.domain.vo.ReviewStatus;
import guesthouse.review.repository.ReviewRepository;
import guesthouse.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewAnalysisServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GuestHousePostRepository guestHousePostRepository;

    @Mock
    private UserService userService;

    @Mock
    private ReviewAnalysisClient reviewAnalysisClient;

    @Mock
    private ReviewAnalysisCacheRepository cacheRepository;

    private ReviewAnalysisService reviewAnalysisService;

    @BeforeEach
    void setUp() {
        reviewAnalysisService = new ReviewAnalysisService(
                reviewRepository,
                guestHousePostRepository,
                userService,
                reviewAnalysisClient,
                cacheRepository,
                new ObjectMapper()
        );
    }

    @Test
    void insights_areAnalyzedOnceAndThenReadFromPersistentCache() {
        Long guestHousePostId = 1L;
        GuestHousePost post = GuestHousePost.builder()
                .id(guestHousePostId)
                .ownerId(10L)
                .guestHouseName("바다 게스트하우스")
                .build();
        Review review = new Review(guestHousePostId, 20L, 5, "바다가 보이고 깨끗해요.");
        ReflectionTestUtils.setField(review, "id", 100L);
        AtomicReference<ReviewAnalysisCache> storedCache = new AtomicReference<>();

        when(guestHousePostRepository.findById(guestHousePostId)).thenReturn(Optional.of(post));
        when(reviewRepository.findByGuestHousePostIdAndStatus(guestHousePostId, ReviewStatus.ACTIVE))
                .thenReturn(List.of(review));
        when(cacheRepository.findById(guestHousePostId))
                .thenAnswer(ignored -> Optional.ofNullable(storedCache.get()));
        when(cacheRepository.save(any(ReviewAnalysisCache.class))).thenAnswer(invocation -> {
            ReviewAnalysisCache cache = invocation.getArgument(0);
            storedCache.set(cache);
            return cache;
        });
        when(reviewAnalysisClient.categorizeStrict(any()))
                .thenReturn(Mono.just(Map.of("청결", List.of(100L))));
        when(reviewAnalysisClient.keywordsStrict(any()))
                .thenReturn(Mono.just(List.of(new ReviewAiKeywordResponse("바다", 0.9, List.of(100L)))));
        when(reviewAnalysisClient.reportStrict(any()))
                .thenReturn(Mono.just(new ReviewAiReportResponse("바다 게스트하우스", "바다 전망과 청결이 장점입니다.")));

        var first = reviewAnalysisService.getGuestHouseReviewInsights(guestHousePostId);
        var second = reviewAnalysisService.getGuestHouseReviewInsights(guestHousePostId);

        assertThat(first.categories()).hasSize(1);
        assertThat(first.keywords()).hasSize(1);
        assertThat(second).isEqualTo(first);
        verify(reviewAnalysisClient).categorizeStrict(any());
        verify(reviewAnalysisClient).keywordsStrict(any());
        verify(reviewAnalysisClient).reportStrict(any());
    }

    @Test
    void emptyReviews_areCachedWithoutCallingReviewAi() {
        Long guestHousePostId = 1L;
        GuestHousePost post = GuestHousePost.builder()
                .id(guestHousePostId)
                .ownerId(10L)
                .guestHouseName("조용한 게스트하우스")
                .build();

        when(guestHousePostRepository.findById(guestHousePostId)).thenReturn(Optional.of(post));
        when(reviewRepository.findByGuestHousePostIdAndStatus(guestHousePostId, ReviewStatus.ACTIVE))
                .thenReturn(List.of());
        when(cacheRepository.findById(guestHousePostId)).thenReturn(Optional.empty());
        when(cacheRepository.save(any(ReviewAnalysisCache.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = reviewAnalysisService.getGuestHouseReviewInsights(guestHousePostId);

        assertThat(result.categories()).isEmpty();
        assertThat(result.keywords()).isEmpty();
        verify(reviewAnalysisClient, never()).categorizeStrict(any());
        verify(reviewAnalysisClient, never()).keywordsStrict(any());
        verify(reviewAnalysisClient, never()).reportStrict(any());
        verify(cacheRepository).save(any(ReviewAnalysisCache.class));
    }
}
