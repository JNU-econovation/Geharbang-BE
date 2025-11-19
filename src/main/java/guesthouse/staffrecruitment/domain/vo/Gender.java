package guesthouse.staffrecruitment.domain.vo;

import guesthouse.application.exception.GenderNotMatchedException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Gender {
    남,
    여,
    무관

}
