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
    private Long userId;

    @Lob
    @Column(nullable = false)
    private String applicationSnapShot;

    public ApplicationRecord(Long recruitmentId, Long userId, String applicationSnapShot) {
        this.staffRecruitmentId = recruitmentId;
        this.userId = userId;
        this.applicationSnapShot = applicationSnapShot;
    }
}
