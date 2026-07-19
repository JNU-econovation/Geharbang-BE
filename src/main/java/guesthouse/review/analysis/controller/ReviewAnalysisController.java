package guesthouse.review.analysis.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewInsightsResponse;
import guesthouse.review.analysis.dto.ReviewAnalysisDtos.ReviewReportResponse;
import guesthouse.review.analysis.service.ReviewAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Review Analysis", description = "리뷰 AI 분석 API")
public class ReviewAnalysisController {

    private final ReviewAnalysisService reviewAnalysisService;

    @Operation(summary = "게스트하우스 리뷰 인사이트 조회", description = "카테고리별 리뷰 id와 자주 언급된 키워드를 조회한다.")
    @GetMapping("/api/v1/guest-houses/{guestHousePostId}/review-insights")
    public ResponseEntity<ReviewInsightsResponse> getGuestHouseReviewInsights(
            @PathVariable Long guestHousePostId
    ) {
        return ResponseEntity.ok(reviewAnalysisService.getGuestHouseReviewInsights(guestHousePostId));
    }

    @Operation(summary = "게스트하우스 운영자용 리뷰 리포트 조회", description = "게스트하우스 소유자 또는 운영자가 AI 리뷰 리포트를 조회한다.")
    @GetMapping("/api/v1/operator/guest-houses/{guestHousePostId}/review-report")
    public ResponseEntity<ReviewReportResponse> getGuestHouseReviewReport(
            @PathVariable Long guestHousePostId,
            @UserId Long userId
    ) {
        return ResponseEntity.ok(reviewAnalysisService.getGuestHouseReviewReport(guestHousePostId, userId));
    }
}
