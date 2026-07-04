package guesthouse.review.dto.request;

import java.util.List;

public record ReviewSaveRequest(
        double rating,
        String content,
        List<String> imageUrls
) {
}
