package guesthouse.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import guesthouse.map.dto.NaverAddressResponse;
import guesthouse.map.dto.NaverReverseGeocodeResponse;
import guesthouse.map.exception.NaverMapException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static guesthouse.map.exception.NaverMapErrorCode.NAVER_LOCAL_API_FAILED;
import static guesthouse.map.exception.NaverMapErrorCode.NAVER_LOCAL_CONFIG_MISSING;
import static guesthouse.map.exception.NaverMapErrorCode.NAVER_MAP_API_FAILED;
import static guesthouse.map.exception.NaverMapErrorCode.NAVER_MAP_CONFIG_MISSING;
import static guesthouse.map.exception.NaverMapErrorCode.NAVER_MAP_RESPONSE_INVALID;

@Service
@RequiredArgsConstructor
public class NaverMapService {

    private static final String NAVER_LOCAL_SEARCH_URL = "https://openapi.naver.com/v1/search/local.json";
    private static final String NAVER_GEOCODE_URL = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode";
    private static final String NAVER_REVERSE_GEOCODE_URL = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc";

    private final WebClient.Builder webClientBuilder;

    @Value("${naver.map.client-id:}")
    private String mapClientId;

    @Value("${naver.map.client-secret:}")
    private String mapClientSecret;

    @Value("${naver.local.client-id:}")
    private String localClientId;

    @Value("${naver.local.client-secret:}")
    private String localClientSecret;

    public List<NaverAddressResponse> localSearch(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        validateLocalCredentials();

        JsonNode body = webClientBuilder.build()
                .get()
                .uri(buildLocalSearchUri(query))
                .header("X-Naver-Client-Id", localClientId)
                .header("X-Naver-Client-Secret", localClientSecret)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorMap(WebClientResponseException.class,
                        e -> new NaverMapException(NAVER_LOCAL_API_FAILED, e))
                .onErrorMap(WebClientRequestException.class,
                        e -> new NaverMapException(NAVER_LOCAL_API_FAILED, e))
                .block();

        JsonNode items = body == null ? null : body.path("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            return List.of();
        }

        List<NaverAddressResponse> results = Flux.fromIterable(items)
                .flatMapSequential(this::toLocalSearchResult, 5)
                .collectList()
                .block();

        return deduplicateByCoordinates(results == null ? List.of() : results);
    }

    public List<NaverAddressResponse> geocode(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        validateMapCredentials();

        List<NaverAddressResponse> results = geocodeMono(query).block();
        return results == null ? List.of() : results;
    }

    private Mono<List<NaverAddressResponse>> geocodeMono(String query) {
        if (query == null || query.isBlank()) {
            return Mono.just(List.of());
        }

        return webClientBuilder.build()
                .get()
                .uri(buildGeocodeUri(query))
                .headers(this::setMapHeaders)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseGeocodeResponse)
                .onErrorMap(WebClientResponseException.class,
                        e -> new NaverMapException(NAVER_MAP_API_FAILED, e))
                .onErrorMap(WebClientRequestException.class,
                        e -> new NaverMapException(NAVER_MAP_API_FAILED, e))
                .defaultIfEmpty(List.of());
    }

    public NaverReverseGeocodeResponse reverseGeocode(Double lat, Double lng) {
        validateMapCredentials();

        JsonNode body = webClientBuilder.build()
                .get()
                .uri(buildReverseGeocodeUri(lat, lng))
                .headers(this::setMapHeaders)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorMap(WebClientResponseException.class,
                        e -> new NaverMapException(NAVER_MAP_API_FAILED, e))
                .onErrorMap(WebClientRequestException.class,
                        e -> new NaverMapException(NAVER_MAP_API_FAILED, e))
                .block();

        JsonNode results = body == null ? null : body.path("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            return new NaverReverseGeocodeResponse("", "");
        }

        String roadAddress = "";
        String jibunAddress = "";
        for (JsonNode result : results) {
            String name = text(result, "name");
            if ("roadaddr".equals(name)) {
                roadAddress = buildAddress(result, true);
            }
            if ("addr".equals(name)) {
                jibunAddress = buildAddress(result, false);
            }
        }

        return new NaverReverseGeocodeResponse(roadAddress, jibunAddress);
    }

    private void setMapHeaders(HttpHeaders headers) {
        headers.set("X-NCP-APIGW-API-KEY-ID", mapClientId);
        headers.set("X-NCP-APIGW-API-KEY", mapClientSecret);
    }

    static URI buildLocalSearchUri(String query) {
        return UriComponentsBuilder.fromUriString(NAVER_LOCAL_SEARCH_URL)
                .queryParam("query", query)
                .queryParam("display", 5)
                .encode()
                .build()
                .toUri();
    }

    static URI buildGeocodeUri(String query) {
        return UriComponentsBuilder.fromUriString(NAVER_GEOCODE_URL)
                .queryParam("query", query)
                .queryParam("count", 5)
                .encode()
                .build()
                .toUri();
    }

    static URI buildReverseGeocodeUri(Double lat, Double lng) {
        return UriComponentsBuilder.fromUriString(NAVER_REVERSE_GEOCODE_URL)
                .queryParam("coords", lng + "," + lat)
                .queryParam("output", "json")
                .queryParam("orders", "roadaddr,addr")
                .encode()
                .build()
                .toUri();
    }

    private Mono<NaverAddressResponse> toLocalSearchResult(JsonNode item) {
        String roadAddress = text(item, "roadAddress");
        String jibunAddress = text(item, "address");
        String address = !roadAddress.isBlank() ? roadAddress : jibunAddress;
        if (address.isBlank()) {
            return Mono.empty();
        }

        return geocodeMono(address)
                .flatMap(geocoded -> {
                    if (geocoded.isEmpty()) {
                        return Mono.empty();
                    }

                    NaverAddressResponse first = geocoded.getFirst();
                    return Mono.just(new NaverAddressResponse(
                            roadAddress,
                            jibunAddress,
                            first.latitude(),
                            first.longitude()
                    ));
                });
    }

    private List<NaverAddressResponse> parseGeocodeResponse(JsonNode body) {
        JsonNode addresses = body == null ? null : body.path("addresses");
        if (addresses == null || !addresses.isArray() || addresses.isEmpty()) {
            return List.of();
        }

        List<NaverAddressResponse> results = new ArrayList<>();
        for (JsonNode address : addresses) {
            results.add(new NaverAddressResponse(
                    text(address, "roadAddress"),
                    text(address, "jibunAddress"),
                    parseDouble(text(address, "y")),
                    parseDouble(text(address, "x"))
            ));
        }
        return results;
    }

    private List<NaverAddressResponse> deduplicateByCoordinates(List<NaverAddressResponse> results) {
        Map<String, NaverAddressResponse> dedup = new LinkedHashMap<>();
        for (NaverAddressResponse result : results) {
            String key = result.latitude() + "," + result.longitude();
            dedup.putIfAbsent(key, result);
        }
        return new ArrayList<>(dedup.values());
    }

    private void validateMapCredentials() {
        if (isBlank(mapClientId) || isBlank(mapClientSecret)) {
            throw new NaverMapException(NAVER_MAP_CONFIG_MISSING);
        }
    }

    private void validateLocalCredentials() {
        if (isBlank(localClientId) || isBlank(localClientSecret)) {
            throw new NaverMapException(NAVER_LOCAL_CONFIG_MISSING);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String buildAddress(JsonNode result, boolean includeRoadName) {
        JsonNode region = result.path("region");
        JsonNode land = result.path("land");

        List<String> parts = new ArrayList<>();
        addIfNotBlank(parts, region.path("area1").path("name").asText(""));
        addIfNotBlank(parts, region.path("area2").path("name").asText(""));
        addIfNotBlank(parts, region.path("area3").path("name").asText(""));
        if (includeRoadName) {
            addIfNotBlank(parts, land.path("name").asText(""));
        }

        String number1 = land.path("number1").asText("");
        String number2 = land.path("number2").asText("");
        String landNumber = number1 + (number2.isBlank() ? "" : "-" + number2);
        addIfNotBlank(parts, landNumber);

        return String.join(" ", parts);
    }

    private void addIfNotBlank(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value);
        }
    }

    private String text(JsonNode node, String fieldName) {
        return node.path(fieldName).asText("");
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new NaverMapException(NAVER_MAP_RESPONSE_INVALID, e);
        }
    }
}
