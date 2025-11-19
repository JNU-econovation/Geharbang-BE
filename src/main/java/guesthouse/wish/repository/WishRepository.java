package guesthouse.wish.repository;

import guesthouse.wish.domain.model.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {
    Boolean existsByUserIdAndStaffRecruitmentId(Long userId, Long staffRecruitmentId);
}
