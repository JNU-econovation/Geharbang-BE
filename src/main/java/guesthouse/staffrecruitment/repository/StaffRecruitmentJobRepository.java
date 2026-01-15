package guesthouse.staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRecruitmentJobRepository extends JpaRepository<StaffRecruitmentJob,Long> {
    List<StaffRecruitmentJob> findJobsByStaffRecruitmentId(Long id);

    void deleteByStaffRecruitmentId(Long staffRecruitmentId);
}
