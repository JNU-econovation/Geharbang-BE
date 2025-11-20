package staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRecruitmentRepository extends JpaRepository<StaffRecruitment, Long>, StaffRecruitmentCustomRepository {

}

