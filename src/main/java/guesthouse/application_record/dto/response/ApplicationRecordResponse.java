package guesthouse.application_record.dto.response;

import guesthouse.application.domain.vo.DayOfWeek;
import guesthouse.application.domain.vo.Mbti;
import guesthouse.application.domain.vo.Style;
import guesthouse.application.dto.ApplicationSnapShotDTO;
import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.user.domain.model.User;
import guesthouse.user.domain.vo.Gender;

import java.time.LocalDate;
import java.util.List;

public record ApplicationRecordResponse(
        String name,
        Gender gender,
        String phoneNumber,
        LocalDate birthDate,
        LocalDate availableStartDate,
        List<DayOfWeek> availableDayOfWeek,
        String introduction,
        Mbti mbti,
        String instagramId,
        List<Style> styles
) {

    public static ApplicationRecordResponse of(ApplicationSnapShotDTO applicationSnapShotDTO, User user) {
        return new ApplicationRecordResponse(
                user.getPersonalInfo().getName(),
                user.getPersonalInfo().getGender(),
                user.getPersonalInfo().getPhoneNumber(),
                user.getPersonalInfo().getBirthDate(),
                applicationSnapShotDTO.getAvailableStartDate(),
                applicationSnapShotDTO.getAvailableDayOfWeek().stream().sorted().toList(),
                applicationSnapShotDTO.getSelfIntroduction(),
                applicationSnapShotDTO.getMbti(),
                applicationSnapShotDTO.getInstagramId(),
                applicationSnapShotDTO.getStyle().stream().sorted().toList()
        );
    }
}
