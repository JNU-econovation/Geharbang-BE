package guesthouse.application_record.domain.model;

import guesthouse.application_record.domain.vo.Status;
import guesthouse.common.domain.TimeEntity;
import guesthouse.staffrecruitment.domain.vo.Region;
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

    private String staffRecruitmentTitle;

    @Enumerated(EnumType.STRING)
    private Region staffRecruitmentRegion;

    private String staffRecruitmentImageUrl;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
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

    public ApplicationRecord(
            Long recruitmentId,
            Long userId,
            String applicationSnapShot,
            String staffRecruitmentTitle,
            Region staffRecruitmentRegion,
            String staffRecruitmentImageUrl
    ) {
        this(recruitmentId, userId, applicationSnapShot);
        this.staffRecruitmentTitle = staffRecruitmentTitle;
        this.staffRecruitmentRegion = staffRecruitmentRegion;
        this.staffRecruitmentImageUrl = staffRecruitmentImageUrl;
    }

    public boolean hasStaffRecruitmentSnapshot() {
        return staffRecruitmentTitle != null && staffRecruitmentRegion != null;
    }

    public void approve() {
        this.status = Status.합격;
    }
}
