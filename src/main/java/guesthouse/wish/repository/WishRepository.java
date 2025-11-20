package guesthouse.wish.repository;

import guesthouse.wish.domain.model.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {
    Boolean existsByUserIdAndStaffRecruitmentId(Long userId, Long staffRecruitmentId);

    Optional<Wish> findByUserIdAndStaffRecruitmentId(Long userId, Long staffRecruitmentId);
}
