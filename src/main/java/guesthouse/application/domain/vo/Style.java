package guesthouse.application.domain.vo;

import guesthouse.application.exception.MbtiNotMatchedException;
import guesthouse.application.exception.StyleNotMatchedException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Style {
    FRIENDLY("친근한"),
    ACTIVE("활발한"),
    CALM("차분한"),
    DILIGENT("성실한"),
    HUMOROUS("유머"),
    RESPONSIBLE("책임감");

    private String value;

    public static Style fromValue(String value) {
        for (Style style : values()) {
            if (style.getValue().equals(value)) {
                return style;
            }
        }

        throw new StyleNotMatchedException();
    }
}
