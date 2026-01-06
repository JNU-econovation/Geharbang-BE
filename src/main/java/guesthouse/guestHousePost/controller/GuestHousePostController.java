package guesthouse.guestHousePost.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.guestHousePost.dto.GuestHousePostDetailsDTO;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import guesthouse.guestHousePost.dto.response.GuestHouseCreateResponse;
import guesthouse.guestHousePost.dto.response.GuestHousePostDetailsResponse;
import guesthouse.guestHousePost.service.GuestHousePostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{guestHousePostId}/details")
    public ResponseEntity<GuestHousePostDetailsResponse> getGuestHousePostDetails(
            @PathVariable Long guestHousePostId
    ) {
        GuestHousePostDetailsDTO details = guestHousePostService.getDetails(guestHousePostId);
        return ResponseEntity.ok(GuestHousePostDetailsResponse.from(details));
    }
}
