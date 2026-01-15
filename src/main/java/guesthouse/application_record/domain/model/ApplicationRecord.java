package guesthouse.application_record.domain.model;

import guesthouse.application_record.domain.vo.Status;
import guesthouse.common.domain.TimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationRecord extends TimeEntity {

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public ApplicationRecord(Long recruitmentId, Long userId, String applicationSnapShot) {
        this.staffRecruitmentId = recruitmentId;
        this.userId = userId;
        this.applicationSnapShot = applicationSnapShot;
        this.status = Status.대기중;
    }

    public void approve() {
        this.status = Status.합격;
    }
}
