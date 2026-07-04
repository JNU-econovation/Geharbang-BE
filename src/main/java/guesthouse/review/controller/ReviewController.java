package guesthouse.review.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.review.dto.request.ReviewSaveRequest;
import guesthouse.review.dto.response.ReviewSaveResponse;
import guesthouse.review.dto.response.ReviewSummaryResponse;
import guesthouse.review.dto.response.ReviewsResponse;
import guesthouse.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Review", description = "게스트하우스/스텝 공고 리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/v1/guest-houses/{guestHousePostId}/reviews")
    public ResponseEntity<ReviewSaveResponse> create(
            @PathVariable Long guestHousePostId,
            @UserId Long userId,
            @RequestBody ReviewSaveRequest request
    ) {
        Long reviewId = reviewService.create(guestHousePostId, userId, request);
        return ResponseEntity.ok(new ReviewSaveResponse(reviewId));
    }

    @PostMapping("/api/v1/staff-recruitment/{staffRecruitmentId}/reviews")
    public ResponseEntity<ReviewSaveResponse> createStaffRecruitmentReview(
            @PathVariable Long staffRecruitmentId,
            @UserId Long userId,
            @RequestBody ReviewSaveRequest request
    ) {
        Long reviewId = reviewService.createStaffRecruitmentReview(staffRecruitmentId, userId, request);
        return ResponseEntity.ok(new ReviewSaveResponse(reviewId));
    }

    @GetMapping("/api/v1/guest-houses/{guestHousePostId}/reviews")
    public ResponseEntity<ReviewsResponse> getReviews(
            @PathVariable Long guestHousePostId,
            @RequestParam int pageNumber,
            @UserId(required = false) Long userId
    ) {
        ReviewsResponse response = reviewService.getReviews(guestHousePostId, userId, pageNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/staff-recruitment/{staffRecruitmentId}/reviews")
    public ResponseEntity<ReviewsResponse> getStaffRecruitmentReviews(
            @PathVariable Long staffRecruitmentId,
            @RequestParam int pageNumber,
            @UserId(required = false) Long userId
    ) {
        ReviewsResponse response = reviewService.getStaffRecruitmentReviews(staffRecruitmentId, userId, pageNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/guest-houses/{guestHousePostId}/review-summary")
    public ResponseEntity<ReviewSummaryResponse> getSummary(
            @PathVariable Long guestHousePostId,
            @UserId(required = false) Long userId
    ) {
        ReviewSummaryResponse response = reviewService.getSummary(guestHousePostId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/staff-recruitment/{staffRecruitmentId}/review-summary")
    public ResponseEntity<ReviewSummaryResponse> getStaffRecruitmentSummary(
            @PathVariable Long staffRecruitmentId,
            @UserId(required = false) Long userId
    ) {
        ReviewSummaryResponse response = reviewService.getStaffRecruitmentSummary(staffRecruitmentId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Void> update(
            @PathVariable Long reviewId,
            @UserId Long userId,
            @RequestBody ReviewSaveRequest request
    ) {
        reviewService.update(reviewId, userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long reviewId,
            @UserId Long userId
    ) {
        reviewService.delete(reviewId, userId);
        return ResponseEntity.ok().build();
    }
}
