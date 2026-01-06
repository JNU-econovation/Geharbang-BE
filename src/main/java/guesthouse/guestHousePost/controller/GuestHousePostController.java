package guesthouse.guestHousePost.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.guestHousePost.dto.GuestHousePostsResponse;
import guesthouse.guestHousePost.dto.RandomGuestHousePostsResponse;
import guesthouse.guestHousePost.domain.vo.*;
import guesthouse.guestHousePost.service.GuestHousePostService;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/guest-houses")
@RestController
@RequiredArgsConstructor
public class GuestHousePostController {

    private final GuestHousePostService guestHousePostService;

    @GetMapping
    public ResponseEntity<GuestHousePostsResponse> getPosts(
            @RequestParam(defaultValue = "최신순") SortType sort,
            @RequestParam(required = false) List<Region> region,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) int lowestRoomPrice,
            @RequestParam(required = false) int highestRoomPrice,
            @RequestParam(required = false) List<PartyType> partyType,
            @RequestParam(required = false) List<RoomType> roomType,
            @RequestParam(required = false) List<RoomHeadCount> headCountType,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(required = false) List<Mood> moods,
            @RequestParam int pageNumber,
            @UserId(required = false) Long userId
    ) {

        GuestHouseFilter guestHouseFilter = new GuestHouseFilter(
                keyword, sort, lowestRoomPrice,
                highestRoomPrice, region, roomType,
                headCountType, moods, partyType, amenities
        );

        GuestHousePostsResponse response = guestHousePostService.getGuestHousePosts(userId, pageNumber, guestHouseFilter);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendation")
    public ResponseEntity<RandomGuestHousePostsResponse> random(@RequestParam Region region) {
        RandomGuestHousePostsResponse response = guestHousePostService.getRandomGuestHousePosts(region);
        return ResponseEntity.ok(response);
    }
}
