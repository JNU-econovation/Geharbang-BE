package guesthouse.wish.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.wish.dto.response.WishResponse;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WishController {

    private final WishService wishService;

    @PostMapping("/api/v1/wish/staff-recruitment/{id}")
    public ResponseEntity<WishResponse> addWish(@UserId Long userId, @PathVariable Long id) {
        Long wishId = wishService.addWish(userId, id);
        return ResponseEntity.ok(new WishResponse(wishId));
    }
}
