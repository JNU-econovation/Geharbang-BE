package guesthouse.map.dto;

public record NaverAddressResponse(
        String roadAddress,
        String jibunAddress,
        Double latitude,
        Double longitude
) {
}
