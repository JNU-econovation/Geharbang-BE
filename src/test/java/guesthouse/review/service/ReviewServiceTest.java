package guesthouse.review.service;

import guesthouse.application_record.domain.vo.Status;
import guesthouse.application_record.repository.ApplicationRecordRepository;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.review.domain.model.Review;
import guesthouse.review.domain.vo.ReviewStatus;
import guesthouse.review.domain.vo.ReviewTargetType;
import guesthouse.review.dto.request.ReviewSaveRequest;
import guesthouse.review.exception.ReviewErrorCode;
import guesthouse.review.exception.ReviewException;
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

import java.util.List;

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

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createStaffRecruitmentReview_savesReviewWhenApplicantIsAccepted() {
        Long staffRecruitmentId = 1L;
        Long userId = 2L;
        ReviewSaveRequest request = new ReviewSaveRequest(5, "숙소 제공이 좋았어요.", List.of());

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
        assertThat(savedReview.getRating()).isEqualTo(5);
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
