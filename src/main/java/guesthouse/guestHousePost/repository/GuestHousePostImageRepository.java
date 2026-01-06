package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.GuestHousePostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestHousePostImageRepository extends JpaRepository<GuestHousePostImage, Long> {

    default GuestHousePostImage getFirstImageByGuestHousePostId(Long guestHousePostId) {
        return findByGuestHousePostIdAndIndex(guestHousePostId, 0);
    }

    GuestHousePostImage findByGuestHousePostIdAndIndex(Long staffRecruitmentId, int index);

    List<GuestHousePostImage> findByGuestHousePostId(Long guestHousePostId);

}
