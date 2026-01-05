package guesthouse.guestHousePost.repository;

import com.querydsl.core.Fetchable;
import guesthouse.guestHousePost.domain.model.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    List<RoomImage> findByRoomId(Long roomId);
}
