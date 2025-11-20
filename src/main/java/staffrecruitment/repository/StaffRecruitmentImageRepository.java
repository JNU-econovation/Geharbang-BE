package staffrecruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import staffrecruitment.domain.StaffRecruitmentImage;

public interface StaffRecruitmentImageRepository extends JpaRepository<StaffRecruitmentImage, Long> {

    @Query("select i from StaffRecruitmentImage i where i.staffRecruitmentId = :staffRecruitmentId and i.index=0")
    StaffRecruitmentImage getRepresentativeImageByStaffRecruitmentId(Long staffRecruitmentId);

}

