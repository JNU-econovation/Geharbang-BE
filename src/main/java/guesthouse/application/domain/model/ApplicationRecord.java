package guesthouse.application.domain.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long staffRecruitmentId;

    @Column(nullable = false)
    private Long applicationId;

    public ApplicationRecord(Long recruitmentId, Long applicationId) {
        this.staffRecruitmentId = recruitmentId;
        this.applicationId = applicationId;
    }
}
