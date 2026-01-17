package guesthouse.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import guesthouse.application.exception.MbtiNotMatchedException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Mbti {
    ISTJ("ISTJ"),
    ISFJ("ISFJ"),
    INFJ("INFJ"),
    INTJ("INTJ"),
    ISTP("ISTP"),
    ISFP("ISFP"),
    INFP("INFP"),
    INTP("INTP"),
    ESTP("ESTP"),
    ESFP("ESFP"),
    ENFP("ENFP"),
    ENTP("ENTP"),
    ESTJ("ESTJ"),
    ESFJ("ESFJ"),
    ENFJ("ENFJ"),
    ENTJ("ENTJ");

    @JsonValue
    private String value;

    public static Mbti fromValue(String value) {
        for (Mbti mbti : Mbti.values()) {
            if(mbti.getValue().equalsIgnoreCase(value))
                return mbti;
        }

        throw new MbtiNotMatchedException();
    }
}
