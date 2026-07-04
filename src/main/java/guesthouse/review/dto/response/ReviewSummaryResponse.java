package guesthouse.review.dto.response;

public record ReviewSummaryResponse(
        double averageRating,
        long reviewCount,
        boolean hasMyReview,
        boolean canWriteReview
) {
}
