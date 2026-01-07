package guesthouse.user.domain.vo;

import guesthouse.user.exception.UserErrorCode;
import guesthouse.user.exception.UserException;
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

        throw new UserException(UserErrorCode.INVALID_GENDER);
    }
}
