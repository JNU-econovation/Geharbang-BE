package guesthouse.staffrecruitment.domain.model;

import guesthouse.common.domain.TimeEntity;
import guesthouse.staffrecruitment.domain.vo.Gender;
import guesthouse.staffrecruitment.domain.vo.Region;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import guesthouse.staffrecruitment.domain.vo.WorkingPeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String guesthouseName;

    @Enumerated(EnumType.STRING)
    private Region region;

    private String address;

    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point coordinates;

    private LocalDate startDate;
    private Boolean isStartDateNegotiable;

    @Enumerated(EnumType.STRING)
    private WorkingPeriod workingPeriod;


    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String advantages;
    private String employeeBenefits;
    private String instagramId;
    private String phoneNumber;
    private String email;
    private String webSite;
    private String ownerMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
