package guesthouse.user.dto.response;

import guesthouse.user.dto.ProfileDTO;
import lombok.Builder;

@Builder
public record ProfileResponse(
        String name,
        String imageUrl,
        Boolean isOwner,
        Boolean inReview
) {
    public static ProfileResponse from(ProfileDTO profile) {
        return ProfileResponse.builder()
                .name(profile.name())
                .imageUrl(profile.imageUrl())
                .isOwner(profile.isOwner())
                .inReview(profile.isReview())
                .build();
    }
}
