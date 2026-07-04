package guesthouse.review.dto.response;

import java.util.List;

public record ReviewsResponse(
        List<ReviewResponse> reviews
) {
}
