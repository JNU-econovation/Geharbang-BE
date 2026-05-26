package guesthouse.staffrecruitment.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.staffrecruitment.domain.vo.Gender;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.WorkingPeriod;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaffRecruitmentDetailsResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void from_returnsCoordinatesAsLongitudeLatitude() throws Exception {
        StaffRecruitmentDetailsDTO details = StaffRecruitmentDetailsDTO.builder()
                .representativeImages(List.of())
                .title("스텝 모집")
                .guestHouseName("제주 게스트하우스")
                .region(Region.제주시)
                .address("제주시")
                .coordinates(createPoint(126.5312, 33.4996))
                .startDate(LocalDate.of(2026, 5, 26))
                .isStartDateNegotiable(false)
                .workingPeriod(WorkingPeriod.단기)
                .jobs(List.of())
                .content("소개")
                .contentImages(List.of())
                .gender(Gender.무관)
                .advantages("")
                .employeeBenefits("")
                .instagramId("")
                .phoneNumber("")
                .webSite("")
                .isWished(false)
                .ownerMessage("환영합니다")
                .build();

        StaffRecruitmentDetailsResponse response = StaffRecruitmentDetailsResponse.from(details);
        JsonNode coordinates = objectMapper.valueToTree(response).path("location").path("coordinates");

        assertThat(coordinates.get(0).asDouble()).isEqualTo(126.5312);
        assertThat(coordinates.get(1).asDouble()).isEqualTo(33.4996);
    }

    private static org.locationtech.jts.geom.Point createPoint(double longitude, double latitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
