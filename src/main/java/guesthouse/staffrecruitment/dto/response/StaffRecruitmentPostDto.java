package guesthouse.staffrecruitment.dto.response;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import lombok.Builder;

import java.util.List;

@Builder
public record StaffRecruitmentPostDto(
        Long id,
        String title,
        List<String> tags,
        String region,
        boolean isWished,
        String imageUrl
) {

    public static StaffRecruitmentPostDto of(StaffRecruitment staffRecruitment, boolean isWished, String imageUrl) {
        return new StaffRecruitmentPostDto(
                staffRecruitment.getId(),
                staffRecruitment.getTitle(),
                List.of(staffRecruitment.getWorkingPeriod().name()),
                staffRecruitment.getRegion().name(),
                isWished,
                imageUrl
        );
    }

}
