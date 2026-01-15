package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findByGuestHousePostId(Long guestHousePostId);

    void deleteByGuestHousePostId(Long guestHousePostId);
}
