package guesthouse.guestHousePost.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.guestHousePost.domain.vo.*;
import guesthouse.guestHousePost.dto.GuestHousePostDetailsDTO;
import guesthouse.guestHousePost.dto.GuestHousePostsResponse;
import guesthouse.guestHousePost.dto.RandomGuestHousePostsResponse;
import guesthouse.guestHousePost.dto.request.ChangeStatusRequest;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import guesthouse.guestHousePost.dto.response.GuestHouseCreateResponse;
import guesthouse.guestHousePost.dto.response.GuestHouseMapPostDto;
import guesthouse.guestHousePost.dto.response.GuestHousePostDetailsResponse;
import guesthouse.guestHousePost.dto.response.OwnerGuestHousePostsResponse;
import guesthouse.guestHousePost.service.GuestHousePostService;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.SortType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/guest-houses")
@RestController
@RequiredArgsConstructor
@Tag(name = "Guest House", description = "게스트하우스 게시글 API")
public class GuestHousePostController {

    private final GuestHousePostService guestHousePostService;

    @GetMapping
    public ResponseEntity<GuestHousePostsResponse> getPosts(
            @RequestParam(defaultValue = "최신순") SortType sort,
            @RequestParam(required = false) List<Region> region,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer lowestRoomPrice,
            @RequestParam(required = false) Integer highestRoomPrice,
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

    @GetMapping("/map")
    @Operation(summary = "게스트하우스 지도 목록 조회", description = "지도 화면에 표시할 활성 게스트하우스 목록을 조회한다.")
    public ResponseEntity<List<GuestHouseMapPostDto>> getGuestHouseMapPosts() {
        List<GuestHouseMapPostDto> response = guestHousePostService.getGuestHouseMapPosts();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GuestHouseCreateResponse> createGuestHousePost(
            @RequestBody @Valid GuestHouseCreateRequest request,
            @UserId Long userId
    ) {
        Long guestHouseId = guestHousePostService.create(request, userId);
        return ResponseEntity.ok(new GuestHouseCreateResponse(guestHouseId));
    }

    @GetMapping("/{guestHousePostId}/details")
    public ResponseEntity<GuestHousePostDetailsResponse> getGuestHousePostDetails(
            @PathVariable Long guestHousePostId,
            @UserId(required = false) Long userId
    ) {
        GuestHousePostDetailsDTO details = guestHousePostService.getDetails(guestHousePostId, userId);
        return ResponseEntity.ok(GuestHousePostDetailsResponse.from(details));

    }

    @GetMapping("/owner")
    public ResponseEntity<OwnerGuestHousePostsResponse> getOwnGuestHousePosts(@UserId Long userId) {
        OwnerGuestHousePostsResponse response = guestHousePostService.getOwnGuestHousePosts(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "게스트하우스 게시글 수정", description = "운영자가 본인이 등록한 게스트하우스 게시글 내용을 수정한다.")
    public ResponseEntity<Void> updateGuestHousePost(
            @UserId Long userId,
            @PathVariable Long id,
            @RequestBody @Valid GuestHouseCreateRequest request
    ) {
        guestHousePostService.updateGuestHousePost(userId, id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "게스트하우스 게시글 상태 변경", description = "운영자가 본인이 등록한 게스트하우스 게시글 상태를 ACTIVE 또는 INACTIVE로 변경한다.")
    public ResponseEntity<Void> changeStatus(
            @RequestBody @Valid ChangeStatusRequest changeStatusRequest,
            @UserId Long userId,
            @PathVariable("id") Long guestHousePostId
    ) {
        guestHousePostService.changeStatus(changeStatusRequest.status(), userId, guestHousePostId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuestHousePost(
            @UserId Long userId,
            @PathVariable("id") Long guestHousePostId
    ) {
        guestHousePostService.deleteGuestHousePost(userId, guestHousePostId);
        return ResponseEntity.ok().build();
    }
}
