package guesthouse.staffrecruitment.domain.model;

import guesthouse.staffrecruitment.domain.vo.Gender;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Feature {

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String advantages;
    private String employeeBenefits;

    public Feature(Gender gender, String advantages, String employeeBenefits) {
        this.gender = gender;
        this.advantages = advantages;
        this.employeeBenefits = employeeBenefits;
    }
}
