package guesthouse.wish.controller;

import guesthouse.common.annotation.UserId;
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
