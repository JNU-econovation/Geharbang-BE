package guesthouse.map.controller;

import guesthouse.map.dto.NaverAddressResponse;
import guesthouse.map.dto.NaverReverseGeocodeResponse;
import guesthouse.map.service.NaverMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
@Tag(name = "Maps", description = "지도/주소 검색 API")
public class NaverMapController {

    private final NaverMapService naverMapService;

    @GetMapping("/local-search")
    @Operation(summary = "네이버 지역 검색", description = "키워드로 장소를 검색하고 좌표를 함께 반환한다.")
    public ResponseEntity<List<NaverAddressResponse>> localSearch(@RequestParam String query) {
        return ResponseEntity.ok(naverMapService.localSearch(query));
    }

    @GetMapping("/geocode")
    @Operation(summary = "주소 좌표 변환", description = "주소 또는 장소 키워드를 좌표로 변환한다.")
    public ResponseEntity<List<NaverAddressResponse>> geocode(@RequestParam String query) {
        return ResponseEntity.ok(naverMapService.geocode(query));
    }

    @GetMapping("/reverse-geocode")
    @Operation(summary = "좌표 주소 변환", description = "위도/경도를 주소로 변환한다.")
    public ResponseEntity<NaverReverseGeocodeResponse> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lng
    ) {
        return ResponseEntity.ok(naverMapService.reverseGeocode(lat, lng));
    }
}
