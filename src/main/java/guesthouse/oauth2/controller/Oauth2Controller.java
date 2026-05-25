package guesthouse.oauth2.controller;

import guesthouse.oauth2.domain.vo.Provider;
import guesthouse.oauth2.dto.response.GoogleLoginUriResponse;
import guesthouse.oauth2.dto.response.KaKaoLoginUriResponse;
import guesthouse.oauth2.service.AuthService;
import guesthouse.oauth2.service.GoogleOAuth2Service;
import guesthouse.oauth2.service.KaKaoOAuth2Service;
import guesthouse.oauth2.service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final AuthService authService;
    private final KaKaoOAuth2Service kaKaoOAuth2Service;
    private final GoogleOAuth2Service googleOAuth2Service;

    @Value("${oauth2.base-redirect-uri}")
    private String redirectBaseUri;

    @GetMapping("/api/v1/oauth/kakao/login")
    public ResponseEntity<KaKaoLoginUriResponse> getKakaoLoginUri() {
        String loginUri = kaKaoOAuth2Service.getLoginUri();
        return ResponseEntity.ok(new KaKaoLoginUriResponse(loginUri));
    }

    @GetMapping("/api/v1/oauth/kakao/callback")
    public ResponseEntity<Void> login(@RequestParam("code") String code) {
        KakaoTokenResponse kakaoResponse = kaKaoOAuth2Service.getToken(code);
        KaKaoUserInfoResponse userInfo = kaKaoOAuth2Service.getUserInfo(kakaoResponse.accessToken());
        LoginInfo loginInfo = authService.loginWithProvider(String.valueOf(userInfo.id()), Provider.KAKAO);
        String redirectUri = redirectBaseUri + loginInfo.accessToken() + "&userId=" + loginInfo.userId();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUri)
                .build();
    }

    @GetMapping("/api/v1/oauth/google/login")
    public ResponseEntity<GoogleLoginUriResponse> getGoogleLoginUri() {
        String loginUri = googleOAuth2Service.getLoginUri();
        return ResponseEntity.ok(new GoogleLoginUriResponse(loginUri));
    }

    @GetMapping("/api/v1/oauth/google/callback")
    public ResponseEntity<Void> loginWithGoogle(@RequestParam("code") String code) {
        GoogleTokenResponse response = googleOAuth2Service.getToken(code);
        GoogleUserInfo userInfo = googleOAuth2Service.getUserInfo(response.idToken());
        LoginInfo loginInfo = authService.loginWithProvider(userInfo.id(), Provider.GOOGLE);
        String redirectUri = redirectBaseUri + loginInfo.accessToken() + "&userId=" + loginInfo.userId();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUri)
                .build();
    }

}
