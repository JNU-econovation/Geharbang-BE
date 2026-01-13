package guesthouse.staffrecruitment.dto.response;

import java.util.List;

public record OwnerStaffRecruitmentPostsResponse(
        List<OwnerStaffRecruitmentPostDto> staffRecruitmentPosts
) {
}
