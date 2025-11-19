package staffrecruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import staffrecruitment.domain.StaffRecruitment;

public interface StaffRecruitmentRepository extends JpaRepository<StaffRecruitment, Long>, StaffRecruitmentCustomRepository {

}

