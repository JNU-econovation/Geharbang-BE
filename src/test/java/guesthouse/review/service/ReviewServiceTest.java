package guesthouse.review.service;

import guesthouse.ai.AiIndexDomain;
import guesthouse.ai.AiIndexSyncAction;
import guesthouse.ai.AiIndexSyncEvent;
import guesthouse.application_record.domain.vo.Status;
import guesthouse.application_record.repository.ApplicationRecordRepository;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.review.domain.model.Review;
import guesthouse.review.domain.vo.ReviewStatus;
import guesthouse.review.domain.vo.ReviewTargetType;
import guesthouse.review.dto.request.ReviewSaveRequest;
import guesthouse.review.dto.response.ReviewSummaryResponse;
import guesthouse.review.exception.ReviewErrorCode;
import guesthouse.review.exception.ReviewException;
import guesthouse.review.analysis.event.GuestHouseReviewChangedEvent;
import guesthouse.review.repository.ReviewImageRepository;
import guesthouse.review.repository.ReviewRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private GuestHousePostRepository guestHousePostRepository;

    @Mock
    private StaffRecruitmentRepository staffRecruitmentRepository;

    @Mock
    private ApplicationRecordRepository applicationRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createGuestHouseReview_publishesAnalysisAndAiIndexRefreshEvents() {
        Long guestHousePostId = 1L;
        Long userId = 2L;
        ReviewSaveRequest request = new ReviewSaveRequest(5, "깨끗하고 친절했어요.", List.of());

        when(guestHousePostRepository.existsById(guestHousePostId)).thenReturn(true);
        when(reviewRepository.existsByGuestHousePostIdAndUserIdAndStatus(
                guestHousePostId,
                userId,
                ReviewStatus.ACTIVE
        )).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.create(guestHousePostId, userId, request);

        verify(eventPublisher).publishEvent(new GuestHouseReviewChangedEvent(guestHousePostId));
        verify(eventPublisher).publishEvent(new AiIndexSyncEvent(
                AiIndexDomain.GUESTHOUSE,
                guestHousePostId,
                AiIndexSyncAction.UPSERT
        ));
    }

    @Test
    void createStaffRecruitmentReview_savesReviewWhenApplicantIsAccepted() {
        Long staffRecruitmentId = 1L;
        Long userId = 2L;
        ReviewSaveRequest request = new ReviewSaveRequest(4.5, "숙소 제공이 좋았어요.", List.of());

        when(staffRecruitmentRepository.existsById(staffRecruitmentId)).thenReturn(true);
        when(applicationRecordRepository.existsByStaffRecruitmentIdAndUserIdAndStatus(
                staffRecruitmentId,
                userId,
                Status.합격
        )).thenReturn(true);
        when(reviewRepository.existsByStaffRecruitmentIdAndUserIdAndStatus(
                staffRecruitmentId,
                userId,
                ReviewStatus.ACTIVE
        )).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.createStaffRecruitmentReview(staffRecruitmentId, userId, request);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review savedReview = captor.getValue();
        assertThat(savedReview.getTargetType()).isEqualTo(ReviewTargetType.STAFF_RECRUITMENT);
        assertThat(savedReview.getGuestHousePostId()).isNull();
        assertThat(savedReview.getStaffRecruitmentId()).isEqualTo(staffRecruitmentId);
        assertThat(savedReview.getUserId()).isEqualTo(userId);
        assertThat(savedReview.getRating()).isEqualByComparingTo("4.5");
        verify(eventPublisher).publishEvent(new AiIndexSyncEvent(
                AiIndexDomain.STAFF_RECRUITMENT,
                staffRecruitmentId,
                AiIndexSyncAction.UPSERT
        ));
    }

    @Test
    void updateStaffRecruitmentReview_publishesAiIndexRefreshEvent() {
        Long reviewId = 10L;
        Long staffRecruitmentId = 20L;
        Long userId = 2L;
        Review review = Review.staffRecruitment(staffRecruitmentId, userId, 4.0, "기존 후기");
        when(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE))
                .thenReturn(java.util.Optional.of(review));

        reviewService.update(reviewId, userId, new ReviewSaveRequest(5.0, "수정한 후기", List.of()));

        verify(eventPublisher).publishEvent(new AiIndexSyncEvent(
                AiIndexDomain.STAFF_RECRUITMENT,
                staffRecruitmentId,
                AiIndexSyncAction.UPSERT
        ));
    }

    @Test
    void deleteStaffRecruitmentReview_publishesAiIndexRefreshEvent() {
        Long reviewId = 10L;
        Long staffRecruitmentId = 20L;
        Long userId = 2L;
        Review review = Review.staffRecruitment(staffRecruitmentId, userId, 4.0, "기존 후기");
        when(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE))
                .thenReturn(java.util.Optional.of(review));

        reviewService.delete(reviewId, userId);

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELETED);
        verify(eventPublisher).publishEvent(new AiIndexSyncEvent(
                AiIndexDomain.STAFF_RECRUITMENT,
                staffRecruitmentId,
                AiIndexSyncAction.UPSERT
        ));
    }

    @Test
    void createSummaries_usesSingleAggregateResultSet() {
        ReviewRepository.GuestHouseReviewAggregate aggregate = mock(
                ReviewRepository.GuestHouseReviewAggregate.class
        );
        when(aggregate.getGuestHousePostId()).thenReturn(10L);
        when(aggregate.getAverageRating()).thenReturn(4.666);
        when(aggregate.getReviewCount()).thenReturn(3L);
        when(reviewRepository.aggregateGuestHouseReviews(List.of(10L, 20L), ReviewStatus.ACTIVE))
                .thenReturn(List.of(aggregate));

        Map<Long, ReviewSummaryResponse> summaries = reviewService.createSummaries(List.of(10L, 20L));

        assertThat(summaries).containsOnlyKeys(10L);
        assertThat(summaries.get(10L).averageRating()).isEqualTo(4.7);
        assertThat(summaries.get(10L).reviewCount()).isEqualTo(3L);
        assertThat(reviewService.createSummaries(List.of()).isEmpty()).isTrue();
        verify(reviewRepository, times(1))
                .aggregateGuestHouseReviews(anyList(), eq(ReviewStatus.ACTIVE));
    }

    @Test
    void createStaffRecruitmentSummaries_usesSingleAggregateResultSet() {
        ReviewRepository.StaffRecruitmentReviewAggregate aggregate = mock(
                ReviewRepository.StaffRecruitmentReviewAggregate.class
        );
        when(aggregate.getStaffRecruitmentId()).thenReturn(30L);
        when(aggregate.getAverageRating()).thenReturn(4.45);
        when(aggregate.getReviewCount()).thenReturn(2L);
        when(reviewRepository.aggregateStaffRecruitmentReviews(List.of(30L, 40L), ReviewStatus.ACTIVE))
                .thenReturn(List.of(aggregate));

        Map<Long, ReviewSummaryResponse> summaries =
                reviewService.createStaffRecruitmentSummaries(List.of(30L, 40L));

        assertThat(summaries).containsOnlyKeys(30L);
        assertThat(summaries.get(30L).averageRating()).isEqualTo(4.5);
        assertThat(summaries.get(30L).reviewCount()).isEqualTo(2L);
        assertThat(reviewService.createStaffRecruitmentSummaries(List.of()).isEmpty()).isTrue();
        verify(reviewRepository).aggregateStaffRecruitmentReviews(anyList(), eq(ReviewStatus.ACTIVE));
    }

    @Test
    void createStaffRecruitmentReview_rejectsUserWithoutAcceptedApplicationRecord() {
        Long staffRecruitmentId = 1L;
        Long userId = 2L;
        ReviewSaveRequest request = new ReviewSaveRequest(5, "숙소 제공이 좋았어요.", List.of());

        when(staffRecruitmentRepository.existsById(staffRecruitmentId)).thenReturn(true);
        when(applicationRecordRepository.existsByStaffRecruitmentIdAndUserIdAndStatus(
                staffRecruitmentId,
                userId,
                Status.합격
        )).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createStaffRecruitmentReview(staffRecruitmentId, userId, request))
                .isInstanceOf(ReviewException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.STAFF_RECRUITMENT_REVIEW_FORBIDDEN);
        verify(reviewRepository, never()).save(any());
    }
}
