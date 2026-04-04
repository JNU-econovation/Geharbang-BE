package guesthouse.user.dto;

public record ProfileDTO(
        String name,
        String imageUrl,
        Boolean isOwner,
        Boolean isReview,
        Boolean isAdmin,
        String certificateStatus
) {
}
