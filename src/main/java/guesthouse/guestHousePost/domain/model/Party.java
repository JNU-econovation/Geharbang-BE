package guesthouse.guestHousePost.domain.model;

import guesthouse.application.domain.vo.DayOfWeek;
import guesthouse.guestHousePost.domain.vo.PartyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long guestHousePostId;

    @Column(nullable = false)
    private PartyType partyType;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @ElementCollection
    @JoinTable(
            name = "weekly_day",
            joinColumns = @JoinColumn(name="party_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "value")
    private Set<DayOfWeek> weeklyDays = new HashSet<>();

    @Column(nullable = false)
    private String place;

    @Column(nullable = false)
    private String moods;

    @Column(nullable = false)
    private Boolean isExternalGuestAllowed;

    @Column(nullable = false)
    private Long guestFee;

    private Long externalGuestFee;

    @Column(columnDefinition = "TEXT")
    private String information;
}
