package guesthouse.review.service;

import guesthouse.ai.AiIndexDomain;
import guesthouse.ai.AiIndexSyncAction;
import guesthouse.ai.AiIndexSyncEvent;
import guesthouse.application_record.domain.vo.Status;
import guesthouse.application_record.repository.ApplicationRecordRepository;
import guesthouse.guestHousePost.exception.GuestHousePostErrorCode;
import guesthouse.guestHousePost.exception.GuestHousePostException;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.review.domain.model.Review;
import guesthouse.review.domain.model.ReviewImage;
import guesthouse.review.domain.vo.ReviewStatus;
import guesthouse.review.domain.vo.ReviewTargetType;
import guesthouse.review.analysis.event.GuestHouseReviewChangedEvent;
import guesthouse.review.dto.request.ReviewSaveRequest;
import guesthouse.review.dto.response.ReviewResponse;
import guesthouse.review.dto.response.ReviewSummaryResponse;
import guesthouse.review.dto.response.ReviewsResponse;
import guesthouse.review.exception.ReviewErrorCode;
import guesthouse.review.exception.ReviewException;
import guesthouse.review.repository.ReviewImageRepository;
import guesthouse.review.repository.ReviewRepository;
import guesthouse.staffrecruitment.exception.StaffRecruitmentErrorCode;
import guesthouse.staffrecruitment.exception.StaffRecruitmentException;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final GuestHousePostRepository guestHousePostRepository;
    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(Long guestHousePostId, Long userId, ReviewSaveRequest request) {
        validateGuestHousePostExists(guestHousePostId);
        validateRequest(request);

        if (reviewRepository.existsByGuestHousePostIdAndUserIdAndStatus(guestHousePostId, userId, ReviewStatus.ACTIVE)) {
            throw new ReviewException(ReviewErrorCode.DUPLICATED);
        }

        Review review = reviewRepository.save(new Review(guestHousePostId, userId, request.rating(), request.content()));
        saveImages(review.getId(), request.imageUrls());
        publishGuestHouseReviewChanged(guestHousePostId);
        return review.getId();
    }

    @Transactional
    public Long createStaffRecruitmentReview(Long staffRecruitmentId, Long userId, ReviewSaveRequest request) {
        validateStaffRecruitmentExists(staffRecruitmentId);
        validateAcceptedApplicant(staffRecruitmentId, userId);
        validateRequest(request);

        if (reviewRepository.existsByStaffRecruitmentIdAndUserIdAndStatus(staffRecruitmentId, userId, ReviewStatus.ACTIVE)) {
            throw new ReviewException(ReviewErrorCode.DUPLICATED);
        }

        Review review = reviewRepository.save(Review.staffRecruitment(
                staffRecruitmentId,
                userId,
                request.rating(),
                request.content()
        ));
        saveImages(review.getId(), request.imageUrls());
        publishStaffRecruitmentReviewChanged(staffRecruitmentId);
        return review.getId();
    }

    @Transactional(readOnly = true)
    public ReviewsResponse getReviews(Long guestHousePostId, Long userId, int pageNumber) {
        validateGuestHousePostExists(guestHousePostId);

        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<Review> reviews = reviewRepository.findByGuestHousePostIdAndStatusOrderByIdDesc(
                guestHousePostId,
                ReviewStatus.ACTIVE,
                pageable
        );
        Map<Long, User> authorsById = getAuthorsById(reviews);

        List<ReviewResponse> responses = reviews.stream()
                .map(review -> ReviewResponse.from(
                        review,
                        authorsById.get(review.getUserId()),
                        getImageUrls(review.getId()),
                        userId
                ))
                .toList();

        return new ReviewsResponse(responses);
    }

    @Transactional(readOnly = true)
    public ReviewsResponse getStaffRecruitmentReviews(Long staffRecruitmentId, Long userId, int pageNumber) {
        validateStaffRecruitmentExists(staffRecruitmentId);

        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<Review> reviews = reviewRepository.findByStaffRecruitmentIdAndStatusOrderByIdDesc(
                staffRecruitmentId,
                ReviewStatus.ACTIVE,
                pageable
        );
        Map<Long, User> authorsById = getAuthorsById(reviews);

        List<ReviewResponse> responses = reviews.stream()
                .map(review -> ReviewResponse.from(
                        review,
                        authorsById.get(review.getUserId()),
                        getImageUrls(review.getId()),
                        userId
                ))
                .toList();

        return new ReviewsResponse(responses);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getSummary(Long guestHousePostId, Long userId) {
        validateGuestHousePostExists(guestHousePostId);
        return createSummary(guestHousePostId, userId);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getStaffRecruitmentSummary(Long staffRecruitmentId, Long userId) {
        validateStaffRecruitmentExists(staffRecruitmentId);
        return createStaffRecruitmentSummary(staffRecruitmentId, userId);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse createSummary(Long guestHousePostId, Long userId) {
        long reviewCount = reviewRepository.countByGuestHousePostIdAndStatus(guestHousePostId, ReviewStatus.ACTIVE);
        double averageRating = round(reviewRepository.averageRating(guestHousePostId, ReviewStatus.ACTIVE));
        boolean hasMyReview = userId != null && reviewRepository.existsByGuestHousePostIdAndUserIdAndStatus(
                guestHousePostId,
                userId,
                ReviewStatus.ACTIVE
        );

        boolean canWriteReview = userId != null && !hasMyReview;

        return new ReviewSummaryResponse(averageRating, reviewCount, hasMyReview, canWriteReview);
    }

    @Transactional(readOnly = true)
    public Map<Long, ReviewSummaryResponse> createSummaries(List<Long> guestHousePostIds) {
        if (guestHousePostIds == null || guestHousePostIds.isEmpty()) {
            return Map.of();
        }
        return reviewRepository.aggregateGuestHouseReviews(guestHousePostIds, ReviewStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        ReviewRepository.GuestHouseReviewAggregate::getGuestHousePostId,
                        aggregate -> new ReviewSummaryResponse(
                                round(aggregate.getAverageRating()),
                                aggregate.getReviewCount(),
                                false,
                                false
                        )
                ));
    }

    @Transactional(readOnly = true)
    public Map<Long, ReviewSummaryResponse> createStaffRecruitmentSummaries(List<Long> staffRecruitmentIds) {
        if (staffRecruitmentIds == null || staffRecruitmentIds.isEmpty()) {
            return Map.of();
        }
        return reviewRepository.aggregateStaffRecruitmentReviews(staffRecruitmentIds, ReviewStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        ReviewRepository.StaffRecruitmentReviewAggregate::getStaffRecruitmentId,
                        aggregate -> new ReviewSummaryResponse(
                                round(aggregate.getAverageRating()),
                                aggregate.getReviewCount(),
                                false,
                                false
                        )
                ));
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse createStaffRecruitmentSummary(Long staffRecruitmentId, Long userId) {
        long reviewCount = reviewRepository.countByStaffRecruitmentIdAndStatus(staffRecruitmentId, ReviewStatus.ACTIVE);
        double averageRating = round(reviewRepository.averageStaffRecruitmentRating(staffRecruitmentId, ReviewStatus.ACTIVE));
        boolean hasMyReview = userId != null && reviewRepository.existsByStaffRecruitmentIdAndUserIdAndStatus(
                staffRecruitmentId,
                userId,
                ReviewStatus.ACTIVE
        );
        boolean canWriteReview =
                userId != null &&
                !hasMyReview &&
                isAcceptedApplicant(staffRecruitmentId, userId);

        return new ReviewSummaryResponse(averageRating, reviewCount, hasMyReview, canWriteReview);
    }

    @Transactional
    public void update(Long reviewId, Long userId, ReviewSaveRequest request) {
        validateRequest(request);

        Review review = getActiveReview(reviewId);
        validateOwner(review, userId);
        review.update(request.rating(), request.content());

        reviewImageRepository.deleteByReviewId(reviewId);
        saveImages(reviewId, request.imageUrls());
        publishReviewChanged(review);
    }

    @Transactional
    public void delete(Long reviewId, Long userId) {
        Review review = getActiveReview(reviewId);
        validateOwner(review, userId);
        review.delete();
        publishReviewChanged(review);
    }

    @Transactional
    public void deleteAllByGuestHousePostId(Long guestHousePostId) {
        List<Review> reviews = reviewRepository.findByGuestHousePostIdAndStatus(guestHousePostId, ReviewStatus.ACTIVE);
        reviews.forEach(Review::delete);
        if (!reviews.isEmpty()) {
            // The guesthouse deletion flow publishes a DELETE index event itself.
            // Only the review-analysis cache needs invalidation here; emitting an
            // UPSERT as well would race with the deletion event after commit.
            eventPublisher.publishEvent(new GuestHouseReviewChangedEvent(guestHousePostId));
        }
    }

    @Transactional
    public void deleteAllByStaffRecruitmentId(Long staffRecruitmentId) {
        List<Review> reviews = reviewRepository.findByStaffRecruitmentIdAndStatus(staffRecruitmentId, ReviewStatus.ACTIVE);
        reviews.forEach(Review::delete);
    }

    private void saveImages(Long reviewId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<ReviewImage> images = IntStream.range(0, imageUrls.size())
                .filter(index -> imageUrls.get(index) != null && !imageUrls.get(index).isBlank())
                .mapToObj(index -> new ReviewImage(reviewId, imageUrls.get(index), index))
                .toList();

        reviewImageRepository.saveAll(images);
    }

    private void publishReviewChanged(Review review) {
        if (review.getTargetType() == ReviewTargetType.GUEST_HOUSE_POST) {
            publishGuestHouseReviewChanged(review.getGuestHousePostId());
        } else if (review.getTargetType() == ReviewTargetType.STAFF_RECRUITMENT) {
            publishStaffRecruitmentReviewChanged(review.getStaffRecruitmentId());
        }
    }

    private void publishGuestHouseReviewChanged(Long guestHousePostId) {
        eventPublisher.publishEvent(new GuestHouseReviewChangedEvent(guestHousePostId));
        eventPublisher.publishEvent(new AiIndexSyncEvent(
                AiIndexDomain.GUESTHOUSE,
                guestHousePostId,
                AiIndexSyncAction.UPSERT
        ));
    }

    private void publishStaffRecruitmentReviewChanged(Long staffRecruitmentId) {
        eventPublisher.publishEvent(new AiIndexSyncEvent(
                AiIndexDomain.STAFF_RECRUITMENT,
                staffRecruitmentId,
                AiIndexSyncAction.UPSERT
        ));
    }

    private List<String> getImageUrls(Long reviewId) {
        return reviewImageRepository.findByReviewId(reviewId)
                .stream()
                .sorted(Comparator.comparing(ReviewImage::getIndex))
                .map(ReviewImage::getImageUrl)
                .toList();
    }

    private Map<Long, User> getAuthorsById(List<Review> reviews) {
        List<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .distinct()
                .toList();

        return userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private Review getActiveReview(Long reviewId) {
        return reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.NOT_FOUND));
    }

    private void validateGuestHousePostExists(Long guestHousePostId) {
        if (!guestHousePostRepository.existsById(guestHousePostId)) {
            throw new GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND);
        }
    }

    private void validateStaffRecruitmentExists(Long staffRecruitmentId) {
        if (!staffRecruitmentRepository.existsById(staffRecruitmentId)) {
            throw new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND);
        }
    }

    private void validateAcceptedApplicant(Long staffRecruitmentId, Long userId) {
        if (!isAcceptedApplicant(staffRecruitmentId, userId)) {
            throw new ReviewException(ReviewErrorCode.STAFF_RECRUITMENT_REVIEW_FORBIDDEN);
        }
    }

    private boolean isAcceptedApplicant(Long staffRecruitmentId, Long userId) {
        return applicationRecordRepository.existsByStaffRecruitmentIdAndUserIdAndStatus(
                staffRecruitmentId,
                userId,
                Status.합격
        );
    }

    private void validateOwner(Review review, Long userId) {
        if (!review.getUserId().equals(userId)) {
            throw new ReviewException(ReviewErrorCode.FORBIDDEN);
        }
    }

    private void validateRequest(ReviewSaveRequest request) {
        if (request == null) {
            throw new ReviewException(ReviewErrorCode.CONTENT_REQUIRED);
        }
        if (request.rating() < 0.5 || request.rating() > 5 || !isHalfPointRating(request.rating())) {
            throw new ReviewException(ReviewErrorCode.INVALID_RATING);
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new ReviewException(ReviewErrorCode.CONTENT_REQUIRED);
        }
    }

    private boolean isHalfPointRating(double rating) {
        return Math.abs(rating * 2 - Math.rint(rating * 2)) < 0.000001;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
