package guesthouse.staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRecruitmentQuestionsRepository extends JpaRepository<StaffRecruitmentQuestion, Long> {

    List<StaffRecruitmentQuestion> findAllByStaffRecruitmentId(Long id);

}
