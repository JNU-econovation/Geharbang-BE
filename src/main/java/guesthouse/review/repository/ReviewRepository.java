package guesthouse.review.repository;

import guesthouse.review.domain.model.Review;
import guesthouse.review.domain.vo.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByGuestHousePostIdAndStatusOrderByIdDesc(Long guestHousePostId, ReviewStatus status, Pageable pageable);

    List<Review> findByGuestHousePostIdAndStatus(Long guestHousePostId, ReviewStatus status);

    List<Review> findByStaffRecruitmentIdAndStatusOrderByIdDesc(Long staffRecruitmentId, ReviewStatus status, Pageable pageable);

    List<Review> findByStaffRecruitmentIdAndStatus(Long staffRecruitmentId, ReviewStatus status);

    Optional<Review> findByIdAndStatus(Long id, ReviewStatus status);

    boolean existsByGuestHousePostIdAndUserIdAndStatus(Long guestHousePostId, Long userId, ReviewStatus status);

    boolean existsByStaffRecruitmentIdAndUserIdAndStatus(Long staffRecruitmentId, Long userId, ReviewStatus status);

    long countByGuestHousePostIdAndStatus(Long guestHousePostId, ReviewStatus status);

    long countByStaffRecruitmentIdAndStatus(Long staffRecruitmentId, ReviewStatus status);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.guestHousePostId = :guestHousePostId and r.status = :status")
    double averageRating(@Param("guestHousePostId") Long guestHousePostId, @Param("status") ReviewStatus status);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.staffRecruitmentId = :staffRecruitmentId and r.status = :status")
    double averageStaffRecruitmentRating(@Param("staffRecruitmentId") Long staffRecruitmentId, @Param("status") ReviewStatus status);
}
