package guesthouse.wish.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.guestHousePost.dto.GuestHousePostsResponse;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostsResponse;
import guesthouse.wish.dto.response.WishResponse;
import guesthouse.wish.service.WishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Wish", description = "찜 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wish")
public class WishController {

    private final WishService wishService;

    @Operation(summary = "내가 찜한 스태프 모집글 목록 조회", description = "로그인한 사용자가 찜한 스태프 모집글 목록을 최신 찜 순으로 조회합니다.")
    @GetMapping("/staff-recruitment/my")
    public ResponseEntity<StaffRecruitmentPostsResponse> getMyWishedStaffRecruitments(
            @UserId Long userId,
            @Parameter(description = "페이지 번호. 0부터 시작", example = "0") @RequestParam(defaultValue = "0") int pageNumber
    ) {
        StaffRecruitmentPostsResponse response = wishService.getMyWishedStaffRecruitments(userId, pageNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스태프 모집글 찜 추가", description = "로그인한 사용자가 스태프 모집글을 찜합니다. 이미 찜한 경우 기존 찜 ID를 반환합니다.")
    @PostMapping("/staff-recruitment/{id}")
    public ResponseEntity<WishResponse> addStaffRecruitmentWish(
            @UserId Long userId,
            @Parameter(description = "스태프 모집글 ID", example = "1") @PathVariable Long id
    ) {
        Long wishId = wishService.addWish(userId, id);
        return ResponseEntity.ok(new WishResponse(wishId));
    }

    @Operation(summary = "스태프 모집글 찜 삭제", description = "로그인한 사용자의 스태프 모집글 찜을 삭제합니다.")
    @DeleteMapping("/staff-recruitment/{id}")
    public ResponseEntity<Void> deleteStaffRecruitmentWish(
            @UserId Long userId,
            @Parameter(description = "스태프 모집글 ID", example = "1") @PathVariable Long id
    ) {
        wishService.deleteWishByStaffRecruitmentId(userId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내가 찜한 게스트하우스 게시글 목록 조회", description = "로그인한 사용자가 찜한 게스트하우스 게시글 목록을 최신 찜 순으로 조회합니다.")
    @GetMapping("/guest-houses/my")
    public ResponseEntity<GuestHousePostsResponse> getMyWishedGuestHousePosts(
            @UserId Long userId,
            @Parameter(description = "페이지 번호. 0부터 시작", example = "0") @RequestParam(defaultValue = "0") int pageNumber
    ) {
        GuestHousePostsResponse response = wishService.getMyWishedGuestHousePosts(userId, pageNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게스트하우스 게시글 찜 추가", description = "로그인한 사용자가 게스트하우스 게시글을 찜합니다. 이미 찜한 경우 기존 찜 ID를 반환합니다.")
    @PostMapping("/guest-houses/{id}")
    public ResponseEntity<WishResponse> addGuestHouseWish(
            @UserId Long userId,
            @Parameter(description = "게스트하우스 게시글 ID", example = "1") @PathVariable Long id
    ) {
        Long wishId = wishService.addWishGuestHousePost(userId, id);
        return ResponseEntity.ok(new WishResponse(wishId));
    }

    @Operation(summary = "게스트하우스 게시글 찜 삭제", description = "로그인한 사용자의 게스트하우스 게시글 찜을 삭제합니다.")
    @DeleteMapping("/guest-houses/{id}")
    public ResponseEntity<Void> deleteGuestHouseWish(
            @UserId Long userId,
            @Parameter(description = "게스트하우스 게시글 ID", example = "1") @PathVariable Long id
    ) {
        wishService.deleteWishByGuestHousePostId(userId, id);
        return ResponseEntity.ok().build();
    }
}
