package guesthouse.staffrecruitment.random;

import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostDto;

import java.util.List;

public record RandomStaffRecruitmentPostsResponse(
        List<RandomStaffRecruitmentPostDto> staffRecruitmentPosts
) { }
