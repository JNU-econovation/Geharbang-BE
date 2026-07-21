package guesthouse.map.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaverMapServiceTest {

    @Test
    void localSearchUri_encodesKoreanQuery() {
        String uri = NaverMapService.buildLocalSearchUri("제주 게스트하우스").toString();

        assertThat(uri)
                .doesNotContain("제주")
                .contains("query=%EC%A0%9C%EC%A3%BC%20%EA%B2%8C%EC%8A%A4%ED%8A%B8%ED%95%98%EC%9A%B0%EC%8A%A4")
                .contains("display=5");
    }

    @Test
    void geocodeUri_encodesKoreanAddress() {
        String uri = NaverMapService.buildGeocodeUri("제주특별자치도 제주시").toString();

        assertThat(uri)
                .doesNotContain("제주")
                .contains("query=%EC%A0%9C%EC%A3%BC%ED%8A%B9%EB%B3%84%EC%9E%90%EC%B9%98%EB%8F%84%20%EC%A0%9C%EC%A3%BC%EC%8B%9C")
                .contains("count=5");
    }
}
