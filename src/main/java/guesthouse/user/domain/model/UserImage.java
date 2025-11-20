package guesthouse.user.domain.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class UserImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String imageUrl;

}
