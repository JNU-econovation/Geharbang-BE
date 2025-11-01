package guesthouse.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.oauth2.service.dto.GoogleTokenResponse;
import guesthouse.oauth2.service.dto.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class GoogleOAuth2Service {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String USER_LOGIN_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_REQUEST_URL = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "openid profile email";

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.redirect-uri}")
    private String redirectUri;


    public String getLoginUri() {
        return UriComponentsBuilder
                .fromUriString(USER_LOGIN_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .build()
                .toUriString();
    }

    public GoogleTokenResponse getToken(String code) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("redirect_uri", redirectUri);
            formData.add("code", code);

            return WebClient.create(TOKEN_REQUEST_URL)
                    .post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public GoogleUserInfo getUserInfo(String accessToken) {
        Map<String, Object> payload = parseToken(accessToken);
        return new GoogleUserInfo((String) payload.get("sub"));
    }

    private Map<String, Object> parseToken(String accessToken) {
        String[] parts = accessToken.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

        Map<String, Object> payload;
        try {
            payload = OBJECT_MAPPER.readValue(payloadJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("구글 토큰 페이로드 파싱 에러", e);
        }
        return payload;
    }

}

