package guesthouse.review.domain.model;

import guesthouse.common.domain.TimeEntity;
import guesthouse.review.domain.vo.ReviewStatus;
import guesthouse.review.domain.vo.ReviewTargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_review_guest_house_status_user", columnList = "guestHousePostId,status,userId"),
        @Index(name = "idx_review_staff_recruitment_status_user", columnList = "staffRecruitmentId,status,userId")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReviewTargetType targetType;

    @Column(name = "guest_house_post_id")
    private Long guestHousePostId;

    @Column(name = "staff_recruitment_id")
    private Long staffRecruitmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    public Review(Long guestHousePostId, Long userId, int rating, String content) {
        this.targetType = ReviewTargetType.GUEST_HOUSE_POST;
        this.guestHousePostId = guestHousePostId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
        this.status = ReviewStatus.ACTIVE;
    }

    public static Review staffRecruitment(Long staffRecruitmentId, Long userId, int rating, String content) {
        Review review = new Review();
        review.targetType = ReviewTargetType.STAFF_RECRUITMENT;
        review.staffRecruitmentId = staffRecruitmentId;
        review.userId = userId;
        review.rating = rating;
        review.content = content;
        review.status = ReviewStatus.ACTIVE;
        return review;
    }

    public void update(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public void delete() {
        this.status = ReviewStatus.DELETED;
    }
}
