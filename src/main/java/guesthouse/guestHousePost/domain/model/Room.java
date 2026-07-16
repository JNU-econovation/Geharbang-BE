package guesthouse.guestHousePost.domain.model;

import guesthouse.guestHousePost.domain.vo.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long guestHousePostId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false)
    private Integer headCount;

    @Column(nullable = false)
    private LocalTime checkInTime;

    @Column(nullable = false)
    private LocalTime checkOutTime;

    @Column(nullable = false)
    private Integer pricePerNight;
}
