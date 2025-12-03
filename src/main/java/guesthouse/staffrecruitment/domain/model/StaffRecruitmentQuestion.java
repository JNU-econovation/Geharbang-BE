package guesthouse.staffrecruitment.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class StaffRecruitmentQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long staffRecruitmentId;
    private String content;

    public StaffRecruitmentQuestion(Long id, Long staffRecruitmentId, String content) {
        this.id = id;
        this.staffRecruitmentId = staffRecruitmentId;
        this.content = content;
    }
}
