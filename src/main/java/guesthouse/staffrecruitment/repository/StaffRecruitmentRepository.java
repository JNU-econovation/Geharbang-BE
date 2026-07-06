package guesthouse.staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRecruitmentRepository extends JpaRepository<StaffRecruitment, Long>, StaffRecruitmentCustomRepository {

    List<StaffRecruitment> findByOwnerId(Long userId);

    List<StaffRecruitment> findByStatusOrderByIdDesc(Status status);

    boolean existsByOwnerIdAndId(Long userId, Long staffRecruitmentId);
}
