package guesthouse.wish.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.wish.dto.response.WishResponse;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/v1/wish")
public class WishController {

    private final WishService wishService;

    @PostMapping("/staff-recruitment/{id}")
    public ResponseEntity<WishResponse> addWish(@UserId Long userId, @PathVariable Long id) {
        Long wishId = wishService.addWish(userId, id);
        return ResponseEntity.ok(new WishResponse(wishId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWish(@UserId Long userId, @PathVariable Long id) {
        wishService.deleteWish(userId, id);
        return ResponseEntity.ok().build();
    }
}
