package guesthouse.staffrecruitment.random;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import lombok.Builder;

import java.util.List;

@Builder
public record RandomStaffRecruitmentPostDto(
        Long id,
        String name,
        List<String> tags,
        String imageUrl
) {

    public static RandomStaffRecruitmentPostDto of(StaffRecruitment staffRecruitment, String imageUrl) {
        return new RandomStaffRecruitmentPostDto(
                staffRecruitment.getId(),
                staffRecruitment.getGuesthouseName(),
                List.of(staffRecruitment.getWorkingPeriod().name()),
                imageUrl
        );
    }

}
