package guesthouse.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import guesthouse.application.exception.DayOfWeekNotMatchedException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DayOfWeek {
    MONDAY("월"),
    TUESDAY("화"),
    WEDNESDAY("수"),
    THURSDAY("목"),
    FRIDAY("금"),
    SATURDAY("토"),
    SUNDAY("일");

    @JsonValue
    private String value;

    public static DayOfWeek fromValue(String value) {
        for (DayOfWeek dayOfWeek : values()) {
            if (dayOfWeek.getValue().equals(value)) {
                return dayOfWeek;
            }
        }

        throw new DayOfWeekNotMatchedException();
    }
}
