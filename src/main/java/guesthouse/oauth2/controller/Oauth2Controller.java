package guesthouse.oauth2.controller;

import guesthouse.oauth2.dto.response.KaKaoLoginUriResponse;
import guesthouse.oauth2.service.KaKaoOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final KaKaoOAuth2Service kaKaoOAuth2Service;

    @GetMapping("/api/v1/oauth/kakao/login")
    ResponseEntity<KaKaoLoginUriResponse> getKakaoLoginUri() {
        String loginUri = kaKaoOAuth2Service.getLoginUri();
        return ResponseEntity.ok(new KaKaoLoginUriResponse(loginUri));
    }
}
