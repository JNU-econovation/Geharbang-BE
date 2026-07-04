package guesthouse.guestHousePost.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.guestHousePost.domain.vo.Contact;
import guesthouse.guestHousePost.dto.GuestHousePostDetailsDTO;
import guesthouse.review.dto.response.ReviewSummaryResponse;
import guesthouse.staffrecruitment.domain.vo.Region;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GuestHousePostDetailsResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void from_returnsCoordinatesAsLongitudeLatitude() throws Exception {
        GuestHousePostDetailsDTO details = GuestHousePostDetailsDTO.builder()
                .guestHouseName("제주 게스트하우스")
                .region(Region.제주시)
                .lotNumberAddress("제주시")
                .roadNameAddress("제주로")
                .coordinates(createPoint(126.5312, 33.4996))
                .introduction("소개")
                .moods(Set.of())
                .imageUrls(List.of())
                .contact(new Contact("", "", ""))
                .ownerMessage("환영합니다")
                .isWished(false)
                .amenities(List.of())
                .parties(List.of())
                .rooms(List.of())
                .reviewSummary(new ReviewSummaryResponse(0.0, 0, false))
                .build();

        GuestHousePostDetailsResponse response = GuestHousePostDetailsResponse.from(details);
        JsonNode coordinates = objectMapper.valueToTree(response).path("location").path("coordinates");

        assertThat(coordinates.get(0).asDouble()).isEqualTo(126.5312);
        assertThat(coordinates.get(1).asDouble()).isEqualTo(33.4996);
    }

    private static org.locationtech.jts.geom.Point createPoint(double longitude, double latitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
