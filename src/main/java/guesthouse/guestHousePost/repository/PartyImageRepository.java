package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.PartyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyImageRepository extends JpaRepository<PartyImage,Long> {
}
