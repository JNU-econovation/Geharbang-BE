package guesthouse.review.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ReviewAnalysisDtos {

    public record ReviewAiReviewRequest(
            Long id,
            String review
    ) {
    }

    public record ReviewAiCategorizeRequest(
            List<ReviewAiReviewRequest> reviews
    ) {
    }

    public record ReviewAiReportRequest(
            @JsonProperty("house_name")
            String houseName,
            List<String> reviews
    ) {
    }

    public record ReviewAiKeywordResponse(
            String keyword,
            double score,
            @JsonProperty("review_ids")
            List<Long> reviewIds
    ) {
    }

    public record ReviewAiReportResponse(
            @JsonProperty("house_name")
            String houseName,
            String report
    ) {
    }

    public record ReviewCategoryGroupResponse(
            String category,
            List<Long> reviewIds
    ) {
    }

    public record ReviewKeywordGroupResponse(
            String keyword,
            double score,
            List<Long> reviewIds
    ) {
    }

    public record ReviewInsightsResponse(
            List<ReviewCategoryGroupResponse> categories,
            List<ReviewKeywordGroupResponse> keywords
    ) {
        public static ReviewInsightsResponse empty() {
            return new ReviewInsightsResponse(List.of(), List.of());
        }
    }

    public record ReviewReportResponse(
            Long guestHousePostId,
            String guestHouseName,
            long reviewCount,
            String report
    ) {
    }

    public static List<ReviewCategoryGroupResponse> toCategoryGroups(Map<String, List<Long>> groups) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }

        return groups.entrySet().stream()
                .map(entry -> new ReviewCategoryGroupResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<ReviewKeywordGroupResponse> toKeywordGroups(List<ReviewAiKeywordResponse> groups) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }

        return groups.stream()
                .map(group -> new ReviewKeywordGroupResponse(
                        group.keyword(),
                        group.score(),
                        group.reviewIds()
                ))
                .toList();
    }
}
