package guesthouse.review.analysis.service;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.exception.GuestHousePostErrorCode;
import guesthouse.guestHousePost.exception.GuestHousePostException;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiCategorizeRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiKeywordResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReportRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewAiReviewRequest;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewInsightsResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewReportResponse;
import guesthouse.review.domain.model.Review;
import guesthouse.review.domain.vo.ReviewStatus;
import guesthouse.review.repository.ReviewRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.exception.UserErrorCode;
import guesthouse.user.exception.UserException;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private static final Duration REVIEW_AI_INSIGHTS_TIMEOUT = Duration.ofSeconds(6);
    private static final String REVIEW_REPORT_EMPTY_MESSAGE =
            "아직 분석할 리뷰가 없어요. 리뷰가 쌓이면 장점/단점 리포트를 확인할 수 있습니다.";
    private static final String REVIEW_AI_REPORT_FALLBACK =
            "리뷰 분석 서버와 연결할 수 없어 리포트를 생성하지 못했습니다. 잠시 후 다시 시도해주세요.";

    private final ReviewRepository reviewRepository;
    private final GuestHousePostRepository guestHousePostRepository;
    private final UserService userService;
    private final ReviewAnalysisClient reviewAnalysisClient;

    @Transactional(readOnly = true)
    public ReviewInsightsResponse getGuestHouseReviewInsights(Long guestHousePostId) {
        validateGuestHousePostExists(guestHousePostId);

        List<Review> reviews = getGuestHouseReviews(guestHousePostId);
        if (reviews.isEmpty()) {
            return ReviewInsightsResponse.empty();
        }

        ReviewAiCategorizeRequest request = new ReviewAiCategorizeRequest(toAiReviewRequests(reviews));
        var insights = Mono.zip(
                        reviewAnalysisClient.categorize(request),
                        reviewAnalysisClient.keywords(request)
                )
                .block(REVIEW_AI_INSIGHTS_TIMEOUT);

        Map<String, List<Long>> categories = insights == null ? Map.of() : insights.getT1();
        List<ReviewAiKeywordResponse> keywords = insights == null ? List.of() : insights.getT2();

        return new ReviewInsightsResponse(
                ReviewAnalysisDtos.toCategoryGroups(categories),
                ReviewAnalysisDtos.toKeywordGroups(keywords)
        );
    }

    @Transactional(readOnly = true)
    public ReviewReportResponse getGuestHouseReviewReport(Long guestHousePostId, Long userId) {
        GuestHousePost guestHousePost = getGuestHousePost(guestHousePostId);
        validateReportAccess(guestHousePost, userId);

        List<Review> reviews = getGuestHouseReviews(guestHousePostId);
        if (reviews.isEmpty()) {
            return new ReviewReportResponse(
                    guestHousePostId,
                    guestHousePost.getGuestHouseName(),
                    0,
                    REVIEW_REPORT_EMPTY_MESSAGE
            );
        }

        String report = "";
        report = reviewAnalysisClient.report(new ReviewAiReportRequest(
                guestHousePost.getGuestHouseName(),
                reviews.stream().map(Review::getContent).toList()
        ));

        return new ReviewReportResponse(
                guestHousePostId,
                guestHousePost.getGuestHouseName(),
                reviews.size(),
                report == null || report.isBlank() ? REVIEW_AI_REPORT_FALLBACK : report
        );
    }

    private List<Review> getGuestHouseReviews(Long guestHousePostId) {
        return reviewRepository.findByGuestHousePostIdAndStatus(guestHousePostId, ReviewStatus.ACTIVE);
    }

    private List<ReviewAiReviewRequest> toAiReviewRequests(List<Review> reviews) {
        return reviews.stream()
                .map(review -> new ReviewAiReviewRequest(review.getId(), review.getContent()))
                .toList();
    }

    private void validateGuestHousePostExists(Long guestHousePostId) {
        if (!guestHousePostRepository.existsById(guestHousePostId)) {
            throw new GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND);
        }
    }

    private GuestHousePost getGuestHousePost(Long guestHousePostId) {
        return guestHousePostRepository.findById(guestHousePostId)
                .orElseThrow(() -> new GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND));
    }

    private void validateReportAccess(GuestHousePost guestHousePost, Long userId) {
        User user = userService.findById(userId);
        if (user.isAdmin()) {
            return;
        }
        if (guestHousePost.getOwnerId().equals(userId)) {
            return;
        }

        throw new UserException(UserErrorCode.NOT_APPROVED_OWNER);
    }
}
