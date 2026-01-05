package guesthouse.guestHousePost.domain.repository;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestHousePostRepository extends JpaRepository<GuestHousePost, Long>, GuestHousePostCustomRepository {

}
