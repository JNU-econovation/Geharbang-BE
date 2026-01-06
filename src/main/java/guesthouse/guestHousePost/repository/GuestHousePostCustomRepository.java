package guesthouse.guestHousePost.repository;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.GuestHouseFilter;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GuestHousePostCustomRepository {

    List<GuestHousePost> searchByFilter(Pageable pageable, GuestHouseFilter filter);

}

