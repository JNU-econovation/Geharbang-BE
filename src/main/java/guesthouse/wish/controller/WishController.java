package guesthouse.wish.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.wish.dto.response.WishResponse;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wish")
public class WishController {

    private final WishService wishService;

    @PostMapping("/staff-recruitment/{id}")
    public ResponseEntity<WishResponse> addStaffRecruitmentWish(@UserId Long userId, @PathVariable Long id) {
        Long wishId = wishService.addWish(userId, id);
        return ResponseEntity.ok(new WishResponse(wishId));
    }

    @DeleteMapping("/staff-recruitment/{id}")
    public ResponseEntity<Void> deleteStaffRecruitmentWish(@UserId Long userId, @PathVariable Long id) {
        wishService.deleteWishByStaffRecruitmentId(userId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/guest-houses/{id}")
    public ResponseEntity<WishResponse> addGuestHouseWish(@UserId Long userId, @PathVariable Long id) {
        Long wishId = wishService.addWishGuestHousePost(userId, id);
        return ResponseEntity.ok(new WishResponse(wishId));
    }

    @DeleteMapping("/guest-houses/{id}")
    public ResponseEntity<Void> deleteGuestHouseWish(@UserId Long userId, @PathVariable Long id) {
        wishService.deleteWishByGuestHousePostId(userId, id);
        return ResponseEntity.ok().build();
    }
}
