package guesthouse.staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRecruitmentImageRepository extends JpaRepository<StaffRecruitmentImage,Long>, StaffRecruitmentCustomRepository{
    List<StaffRecruitmentImage> findByStaffRecruitmentIdAndType(Long recruitmentId, StaffRecruitmentImageType staffRecruitmentImageType);

    @Query("select i from StaffRecruitmentImage i where i.staffRecruitmentId = :staffRecruitmentId and i.index=0")
    StaffRecruitmentImage getRepresentativeImageByStaffRecruitmentId(Long staffRecruitmentId);
}
