package guesthouse.guestHousePost.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import guesthouse.guestHousePost.dto.response.GuestHouseCreateResponse;
import guesthouse.guestHousePost.service.GuestHousePostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/guest-houses")
@RestController
@RequiredArgsConstructor
public class GuestHousePostController {

    private final GuestHousePostService guestHousePostService;

    @PostMapping
    public ResponseEntity<GuestHouseCreateResponse> createGuestHousePost(
            @RequestBody @Valid GuestHouseCreateRequest request,
            @UserId Long userId
    ) {
        Long guestHouseId = guestHousePostService.create(request, userId);
        return ResponseEntity.ok(new GuestHouseCreateResponse(guestHouseId));
    }
}
