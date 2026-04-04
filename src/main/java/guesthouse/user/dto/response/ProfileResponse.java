package guesthouse.user.dto.response;

import guesthouse.user.dto.ProfileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ProfileResponse(
        String name,
        String imageUrl,
        @Schema(description = "승인_완료 상태의 인증서가 있는 경우 true (게스트하우스 사장님)", example = "false")
        Boolean isOwner,
        @Schema(description = "검토_대기 상태의 인증서가 있는 경우 true (심사 진행 중)", example = "false")
        Boolean inReview,
        @Schema(description = "User.role == 운영자인 경우 true (시스템 관리자)", example = "false")
        Boolean isAdmin,
        @Schema(description = "가장 최근 인증서 상태. 인증서 없으면 null", example = "거부됨",
                allowableValues = {"검토_대기", "승인_완료", "거부됨"})
        String certificateStatus
) {
    public static ProfileResponse from(ProfileDTO profile) {
        return ProfileResponse.builder()
                .name(profile.name())
                .imageUrl(profile.imageUrl())
                .isOwner(profile.isOwner())
                .inReview(profile.isReview())
                .isAdmin(profile.isAdmin())
                .certificateStatus(profile.certificateStatus())
                .build();
    }
}
