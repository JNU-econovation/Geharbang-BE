package guesthouse.staffrecruitment.domain.model;

import guesthouse.common.domain.TimeEntity;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.WorkingPeriod;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StaffRecruitment extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;

    private String title;
    private String guestHouseName;

    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    private String lotNumberAddress;
    private String roadNameAddress;

    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point coordinates;

    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    private WorkingPeriod workingPeriod;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Embedded
    private Feature feature;

    @Embedded
    private Contact contact;

    private String ownerMessage;

    @Column(nullable = false)
    private int viewCount;

    public void plusViewCount() {
        this.viewCount += 1;
    }
}
