package guesthouse.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class GoogleOAuth2Service {

    private static final String USER_LOGIN_URL = "https://accounts.google.com/o/oauth2/v2/auth";

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

}

