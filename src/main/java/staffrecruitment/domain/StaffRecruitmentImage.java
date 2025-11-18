package staffrecruitment.domain;

import jakarta.persistence.*;
import staffrecruitment.vo.StaffRecruitmentImageType;

@Entity
public class StaffRecruitmentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long staffRecruitmentId;

    @Enumerated(EnumType.STRING)
    private StaffRecruitmentImageType type;

    private String imageUrl;
    private String index;
}
