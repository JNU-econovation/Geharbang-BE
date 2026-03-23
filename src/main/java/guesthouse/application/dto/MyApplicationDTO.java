package guesthouse.application.dto;

import guesthouse.application.domain.model.Application;
import guesthouse.application.domain.vo.DayOfWeek;
import guesthouse.application.domain.vo.Mbti;
import guesthouse.application.domain.vo.Style;
import guesthouse.user.domain.model.User;
import guesthouse.user.domain.vo.Gender;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record MyApplicationDTO(
        String name,
        Gender gender,
        String phoneNumber,
        LocalDate birthDate,
        LocalDate availableStartDate,
        Set<DayOfWeek> availableDayOfWeek,
        String introduction,
        Mbti mbti,
        Set<Style> styles,
        String instagramId,
        String imageUrl
) {
    public static MyApplicationDTO from(Application application) {
        User user = application.getUser();
        String name = user.getPersonalInfo().getName();
        Gender gender = user.getPersonalInfo().getGender();
        String phoneNumber = user.getPersonalInfo().getPhoneNumber();
        LocalDate birthDate = user.getPersonalInfo().getBirthDate();

        return MyApplicationDTO.builder()
                .name(name)
                .gender(gender)
                .phoneNumber(phoneNumber)
                .birthDate(birthDate)
                .availableStartDate(application.getAvailableStartDate())
                .availableDayOfWeek(application.getAvailableDayOfWeek())
                .introduction(application.getSelfIntroduction())
                .mbti(application.getMbti())
                .styles(application.getStyle())
                .instagramId(application.getInstagramId())
                .imageUrl(application.getImageUrl())
                .build();
    }
}
