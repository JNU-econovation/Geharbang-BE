package guesthouse.guestHousePost.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PartyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partyId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(name = "image_index")
    private int index;
}
