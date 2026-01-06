package guesthouse.guestHousePost.domain.vo;

import guesthouse.staffrecruitment.domain.vo.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Builder
@Getter
@RequiredArgsConstructor
public class GuestHouseFilter {

    private final String keyword;
    private final SortType sortType;
    private final Integer lowestRoomPrice;
    private final Integer highestRoomPrice;
    private final List<Region> regions;
    private final List<RoomType> roomTypes;
    private final List<RoomHeadCount> headCountTypes;
    private final List<Mood> moods;
    private final List<PartyType> partyTypes;
    private final List<String> amenities;

}
