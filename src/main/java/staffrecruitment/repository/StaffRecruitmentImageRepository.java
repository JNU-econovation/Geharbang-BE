package staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface StaffRecruitmentImageRepository extends JpaRepository<StaffRecruitmentImage, Long> {

    @Query("select i from StaffRecruitmentImage i where i.staffRecruitmentId = :staffRecruitmentId and i.index=0")
    StaffRecruitmentImage getRepresentativeImageByStaffRecruitmentId(Long staffRecruitmentId);

}

