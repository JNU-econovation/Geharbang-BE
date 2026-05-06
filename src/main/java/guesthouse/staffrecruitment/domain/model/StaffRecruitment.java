package guesthouse.staffrecruitment.domain.model;

import guesthouse.common.domain.TimeEntity;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.Status;
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

    @Enumerated(EnumType.STRING)
    private Status status;

    public void plusViewCount() {
        this.viewCount += 1;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    public void update(String title,
                       String guestHouseName,
                       Region region,
                       String lotNumberAddress,
                       String roadNameAddress,
                       Point coordinates,
                       LocalDate startDate,
                       WorkingPeriod workingPeriod,
                       String content,
                       Feature feature,
                       Contact contact,
                       String ownerMessage) {
        this.title = title;
        this.guestHouseName = guestHouseName;
        this.region = region;
        this.lotNumberAddress = lotNumberAddress;
        this.roadNameAddress = roadNameAddress;
        this.coordinates = coordinates;
        this.startDate = startDate;
        this.workingPeriod = workingPeriod;
        this.content = content;
        this.feature = feature;
        this.contact = contact;
        this.ownerMessage = ownerMessage;
    }

}
