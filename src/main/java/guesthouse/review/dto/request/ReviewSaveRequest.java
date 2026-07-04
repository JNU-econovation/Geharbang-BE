package guesthouse.review.dto.request;

import java.util.List;

public record ReviewSaveRequest(
        int rating,
        String content,
        List<String> imageUrls
) {
}
