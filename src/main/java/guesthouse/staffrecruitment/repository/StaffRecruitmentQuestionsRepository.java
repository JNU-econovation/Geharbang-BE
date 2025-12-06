package guesthouse.staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface StaffRecruitmentQuestionsRepository extends JpaRepository<StaffRecruitmentQuestion, Long> {

    Optional<StaffRecruitmentQuestion> findByIdAndStaffRecruitmentId(Long staffRecruitmentQuestionId, Long staffRecruitmentId);

    Boolean existsByStaffRecruitmentId(Long recruitmentId);
  
    List<StaffRecruitmentQuestion> findAllByStaffRecruitmentId(Long id);

}
