package guesthouse.application_record.dto.response;

import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.application_record.domain.vo.Status;
import guesthouse.user.domain.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record SubmittedApplicationDto(
        Long applicationRecordId,
        String name,
        String imageUrl,
        Status applicationStatus,
        LocalDate appliedAt
) {

    public static SubmittedApplicationDto of(ApplicationRecord applicationRecord, User user) {
        return new SubmittedApplicationDto(
                applicationRecord.getId(),
                user.getPersonalInfo().getName(),
                user.getProfileImageUrl(),
                applicationRecord.getStatus(),
                applicationRecord.getCreatedAt().toLocalDate()
        );
    }

    public static List<SubmittedApplicationDto> of(List<ApplicationRecord> applicationRecords, List<User> user) {
        List<SubmittedApplicationDto> dtos = new ArrayList<>();
        for (int i = 0; i < applicationRecords.size(); i++) {
            dtos.add(of(applicationRecords.get(i), user.get(i)));
        }
        return dtos;
    }

}
