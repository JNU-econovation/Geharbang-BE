package guesthouse.application.dto;

import guesthouse.application.domain.model.Application;
import guesthouse.application.domain.vo.DayOfWeek;
import guesthouse.application.domain.vo.Mbti;
import guesthouse.application.domain.vo.Style;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationSnapShotDTO {

    private LocalDate availableStartDate;
    private Set<DayOfWeek> availableDayOfWeek;
    private String selfIntroduction;
    private Mbti mbti;
    private Set<Style> style;
    private String instagramId;
    private String imageUrl;

    public static ApplicationSnapShotDTO from (Application application){
        return ApplicationSnapShotDTO.builder()
                .availableStartDate(application.getAvailableStartDate())
                .availableDayOfWeek(application.getAvailableDayOfWeek())
                .selfIntroduction(application.getSelfIntroduction())
                .mbti(application.getMbti())
                .style(application.getStyle())
                .instagramId(application.getInstagramId())
                .imageUrl(application.getImageUrl())
                .build();
    }
}
