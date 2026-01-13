package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestHousePostRepository extends JpaRepository<GuestHousePost, Long>, GuestHousePostCustomRepository {

    List<GuestHousePost> findByOwnerId(Long ownerId);

}
