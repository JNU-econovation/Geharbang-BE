package guesthouse.application.mapper;

import guesthouse.application.domain.model.Application;
import guesthouse.application.domain.vo.DayOfWeek;
import guesthouse.application.domain.vo.Mbti;
import guesthouse.application.domain.vo.Style;
import guesthouse.application.dto.requset.ApplicationSaveRequest;
import guesthouse.user.domain.model.User;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ApplicationMapper {
    public static Application toEntity(ApplicationSaveRequest request, User user) {
        return Application.builder()
                .user(user)
                .availableStartDate(request.availableStartDate())
                .availableDayOfWeek(toDayOfWeekSet(request.availableDayOfWeek()))
                .selfIntroduction(request.selfIntroduction())
                .mbti(Mbti.fromValue(request.mbti()))
                .style(toStyleSet(request.style()))
                .instagramId(request.instagramId())
                .imageUrl(request.imageUrl())
                .build();
    }

    public static Set<DayOfWeek> toDayOfWeekSet(List<String> values) {
        return values.stream()
                .map(DayOfWeek::fromValue)
                .collect(Collectors.toSet());
    }

    public static Set<Style> toStyleSet(List<String> values) {
        return values.stream()
                .map(Style::fromValue)
                .collect(Collectors.toSet());
    }
}
