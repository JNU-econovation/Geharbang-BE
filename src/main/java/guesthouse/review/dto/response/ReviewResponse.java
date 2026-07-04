package guesthouse.review.dto.response;

import guesthouse.review.domain.model.Review;
import guesthouse.user.domain.model.User;
import guesthouse.user.domain.vo.PersonalInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReviewResponse(
        Long id,
        Long guestHousePostId,
        Long staffRecruitmentId,
        Long userId,
        String authorName,
        String authorProfileImageUrl,
        double rating,
        String content,
        List<String> imageUrls,
        boolean isMine,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ReviewResponse from(Review review, User author, List<String> imageUrls, Long requestUserId) {
        PersonalInfo personalInfo = author == null ? null : author.getPersonalInfo();
        String authorName = personalInfo == null ? "" : personalInfo.getName();
        String authorProfileImageUrl = author == null ? "" : author.getProfileImageUrl();

        return ReviewResponse.builder()
                .id(review.getId())
                .guestHousePostId(review.getGuestHousePostId())
                .staffRecruitmentId(review.getStaffRecruitmentId())
                .userId(review.getUserId())
                .authorName(authorName)
                .authorProfileImageUrl(authorProfileImageUrl)
                .rating(review.getRating().doubleValue())
                .content(review.getContent())
                .imageUrls(imageUrls)
                .isMine(review.getUserId().equals(requestUserId))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
