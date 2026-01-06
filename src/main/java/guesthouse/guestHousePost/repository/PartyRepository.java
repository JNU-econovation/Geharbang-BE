package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyRepository extends JpaRepository<Party,Long> {
    List<Party> findByGuestHousePostId(Long guestHousePostId);
}
