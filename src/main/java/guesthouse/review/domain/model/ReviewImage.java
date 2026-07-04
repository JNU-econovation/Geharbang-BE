package guesthouse.review.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reviewId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(name = "image_index")
    private int index;

    public ReviewImage(Long reviewId, String imageUrl, int index) {
        this.reviewId = reviewId;
        this.imageUrl = imageUrl;
        this.index = index;
    }
}
