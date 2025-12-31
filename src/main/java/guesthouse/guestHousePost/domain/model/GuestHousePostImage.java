package guesthouse.guestHousePost.domain.model;

import guesthouse.guestHousePost.domain.vo.ImageType;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GuestHousePostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long guestHousePostId;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    @Column(nullable = false)
    private String imageUrl;

    @Column(name = "image_index")
    private int index;
}