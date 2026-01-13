package guesthouse.staffrecruitment.repository;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRecruitmentRepository extends JpaRepository<StaffRecruitment, Long>, StaffRecruitmentCustomRepository {

    List<StaffRecruitment> findByOwnerId(Long userId);
}
