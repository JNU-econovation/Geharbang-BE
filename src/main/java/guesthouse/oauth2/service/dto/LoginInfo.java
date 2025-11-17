package guesthouse.oauth2.service.dto;

public record LoginInfo(
        Long userId,
        String accessToken
) {

}
