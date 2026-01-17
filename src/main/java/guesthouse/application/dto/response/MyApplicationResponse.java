package guesthouse.application.dto.response;

import guesthouse.application.domain.vo.DayOfWeek;
import guesthouse.application.domain.vo.Mbti;
import guesthouse.application.domain.vo.Style;
import guesthouse.application.dto.MyApplicationDTO;
import guesthouse.user.domain.vo.Gender;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record MyApplicationResponse(
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
    public static MyApplicationResponse from(MyApplicationDTO dto) {
        return MyApplicationResponse.builder()
                .name(dto.name())
                .gender(dto.gender())
                .phoneNumber(dto.phoneNumber())
                .birthDate(dto.birthDate())
                .availableStartDate(dto.availableStartDate())
                .availableDayOfWeek(dto.availableDayOfWeek())
                .introduction(dto.introduction())
                .mbti(dto.mbti())
                .styles(dto.styles())
                .instagramId(dto.instagramId())
                .imageUrl(dto.imageUrl())
                .build();
    }
}
