package guesthouse.guestHousePost.domain.model;

import guesthouse.guestHousePost.domain.vo.Contact;
import guesthouse.guestHousePost.domain.vo.Mood;
import guesthouse.guestHousePost.domain.vo.Status;
import guesthouse.staffrecruitment.domain.vo.Region;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuestHousePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String guestHouseName;

    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    private String lotNumberAddress;

    @Column(nullable = false)
    private String roadNameAddress;

    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point coordinates;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @ElementCollection
    @JoinTable(
            name = "mood",
            joinColumns = @JoinColumn(name="guest_house_post_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "value")
    private Set<Mood> moods = new HashSet<>();

    @Embedded
    private Contact contact;

    private String ownerMessage;

    @Enumerated(EnumType.STRING)
    private Status status;


    public void changeStatus(Status status) {
        this.status = status;
    }
}
