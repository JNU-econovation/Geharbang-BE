package guesthouse.oauth2.controller;

import guesthouse.oauth2.domain.vo.Provider;
import guesthouse.oauth2.dto.response.KaKaoLoginUriResponse;
import guesthouse.oauth2.service.AuthService;
import guesthouse.oauth2.service.KaKaoOAuth2Service;
import guesthouse.oauth2.service.KaKaoUserInfoResponse;
import guesthouse.oauth2.service.KakaoTokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final AuthService authService;
    private final KaKaoOAuth2Service kaKaoOAuth2Service;

    @Value("${oauth2.base-redirect-uri}")
    private String redirectBaseUri;

    @GetMapping("/api/v1/oauth/kakao/login")
    public ResponseEntity<KaKaoLoginUriResponse> getKakaoLoginUri() {
        String loginUri = kaKaoOAuth2Service.getLoginUri();
        return ResponseEntity.ok(new KaKaoLoginUriResponse(loginUri));
    }

    @GetMapping("/api/v1/oauth/kakao/callback")
    public ResponseEntity<Void> login(@RequestParam("code") String code){
        KakaoTokenResponse kakaoResponse = kaKaoOAuth2Service.getToken(code);
        KaKaoUserInfoResponse userInfo = kaKaoOAuth2Service.getUserInfo(kakaoResponse.accessToken());
        String accessToken = authService.loginWithProvider(userInfo.id(), Provider.KAKAO);
        String redirectUri = redirectBaseUri + accessToken;
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUri)
                .build();
    }

}
