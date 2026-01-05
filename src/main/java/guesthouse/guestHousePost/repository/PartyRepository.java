package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party,Long> {
}
