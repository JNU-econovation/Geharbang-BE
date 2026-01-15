package guesthouse.user.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.user.dto.ProfileDTO;
import guesthouse.user.dto.response.ProfileResponse;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> hasApplication(
            @UserId Long userId
    ) {
        ProfileDTO profile = userService.getProfile(userId);
        return ResponseEntity.ok(ProfileResponse.from(profile));
    }
}
