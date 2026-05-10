package guesthouse.wish.repository;

import guesthouse.wish.domain.model.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {
    Boolean existsByUserIdAndStaffRecruitmentId(Long userId, Long staffRecruitmentId);
    Optional<Wish> findByUserIdAndStaffRecruitmentId(Long userId, Long staffRecruitmentId);
    void deleteByUserIdAndStaffRecruitmentId(Long userId, Long staffRecruitmentId);

    Boolean existsByUserIdAndGuestHousePostId(Long userId, Long guestHousePostId);
    Optional<Wish> findByUserIdAndGuestHousePostId(Long userId, Long guestHousePostId);
    void deleteByUserIdAndGuestHousePostId(Long userId, Long guestHousePostId);

    Optional<Wish> findByIdAndUserId(Long id, Long userId);
}
