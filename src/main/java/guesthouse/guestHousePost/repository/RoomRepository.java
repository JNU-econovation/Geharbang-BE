package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findByGuestHousePostId(Long guestHousePostId);
}
