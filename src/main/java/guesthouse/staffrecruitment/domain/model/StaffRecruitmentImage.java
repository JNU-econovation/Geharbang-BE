package guesthouse.staffrecruitment.domain.model;

import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentImageType;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StaffRecruitmentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long staffRecruitmentId;

    @Enumerated(EnumType.STRING)
    private StaffRecruitmentImageType type;

    private String imageUrl;

    @Column(name = "image_index")
    private Long index;
}
