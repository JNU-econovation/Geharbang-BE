package guesthouse.application.domain.vo;

import guesthouse.application.exception.DayOfWeekNotMatchedException;
import guesthouse.application.exception.GenderNotMatchedException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Gender {
    MALE("남"),
    FEMALE("여");

    private String value;

    public static Gender fromValue(String value) {
        for (Gender gender : values()) {
            if (gender.getValue().equals(value)) {
                return gender;
            }
        }

        throw new GenderNotMatchedException();
    }
}
