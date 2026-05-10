package guesthouse.wish.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long staffRecruitmentId;

    private Long guestHousePostId;

    private Long userId;

    public Wish(Long userId, Long staffRecruitmentId) {
        this.userId = userId;
        this.staffRecruitmentId = staffRecruitmentId;
    }

    public Wish(Long userId, Long guestHousePostId, boolean isGuestHousePost) {
        this.userId = userId;
        this.guestHousePostId = guestHousePostId;
    }
}
