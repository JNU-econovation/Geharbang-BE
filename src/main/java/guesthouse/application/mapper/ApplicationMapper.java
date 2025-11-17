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
        Mbti mbti = Mbti.fromValue(request.mbti());
        List<DayOfWeek> listOfAvailableDayOfWeek = request.availableDayOfWeek()
                .stream()
                .map(DayOfWeek::fromValue)
                .toList();
        Set<Style> listOfStyle = request.style()
                .stream()
                .map(Style::fromValue)
                .collect(Collectors.toSet());;

        return Application.builder()
                .user(user)
                .availableStartDate(request.availableStartDate())
                .availableDayOfWeek(new HashSet<>(listOfAvailableDayOfWeek))
                .selfIntroduction(request.selfIntroduction())
                .mbti(mbti)
                .style(listOfStyle)
                .instagramId(request.instagramId())
                .imageUrl(request.imageUrl())
                .build();
    }
}
