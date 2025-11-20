package staffrecruitment.dto.response;

import java.util.List;

public record StaffRecruitmentPostsResponse(
        List<StaffRecruitmentPostDto> staffRecruitmentPosts
) { }
