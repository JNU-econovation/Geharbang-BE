package guesthouse.staffrecruitment.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record StaffRecruitmentPostDto(
        Long id,
        String name,
        List<String> tags,
        String region,
        boolean isWished,
        String imageUrl
) {



}
