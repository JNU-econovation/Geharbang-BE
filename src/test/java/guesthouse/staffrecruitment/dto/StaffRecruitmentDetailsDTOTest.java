package guesthouse.staffrecruitment.dto;

import guesthouse.staffrecruitment.domain.model.Contact;
import guesthouse.staffrecruitment.domain.model.Feature;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.Gender;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaffRecruitmentDetailsDTOTest {

    @Test
    void from_usesRoadNameAddressFirst() {
        StaffRecruitment recruitment = recruitment("제주특별자치도 제주시 해안로 1", "제주시 애월읍 10");

        StaffRecruitmentDetailsDTO result = StaffRecruitmentDetailsDTO.from(
                recruitment, List.of(), List.of(), List.of(), false
        );

        assertThat(result.address()).isEqualTo("제주특별자치도 제주시 해안로 1");
    }

    @Test
    void from_fallsBackToLotNumberAddress() {
        StaffRecruitment recruitment = recruitment(" ", "제주시 애월읍 10");

        StaffRecruitmentDetailsDTO result = StaffRecruitmentDetailsDTO.from(
                recruitment, List.of(), List.of(), List.of(), false
        );

        assertThat(result.address()).isEqualTo("제주시 애월읍 10");
    }

    private StaffRecruitment recruitment(String roadNameAddress, String lotNumberAddress) {
        return StaffRecruitment.builder()
                .roadNameAddress(roadNameAddress)
                .lotNumberAddress(lotNumberAddress)
                .feature(new Feature(Gender.무관, "", ""))
                .contact(new Contact("", "", ""))
                .build();
    }
}
