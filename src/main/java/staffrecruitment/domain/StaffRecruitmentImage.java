package staffrecruitment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import staffrecruitment.vo.StaffRecruitmentImageType;

@Entity
@Getter
public class StaffRecruitmentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long staffRecruitmentId;

    @Enumerated(EnumType.STRING)
    private StaffRecruitmentImageType type;

    private String imageUrl;
    private int index;
}
