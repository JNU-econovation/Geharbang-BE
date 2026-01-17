package guesthouse.application_record.dto;

import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.user.domain.model.User;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ApplicationRecordDTO(
        Long id,
        String title,
        Region region,
        LocalDate appliedAt,
        Boolean isAccepted,
        String imageUrl
) {
    public static ApplicationRecordDTO from (ApplicationRecord record, StaffRecruitment staffRecruitment, String representativeImageUrl) {
        return ApplicationRecordDTO.builder()
                .id(record.getId())
                .title(staffRecruitment.getTitle())
                .region(staffRecruitment.getRegion())
                .appliedAt(record.getCreatedAt().toLocalDate())
                .isAccepted(record.getStatus().isAccepted())
                .imageUrl(representativeImageUrl)
                .build();
    }
}
