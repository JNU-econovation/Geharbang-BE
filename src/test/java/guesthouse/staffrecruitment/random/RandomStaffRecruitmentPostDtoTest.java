package guesthouse.staffrecruitment.random;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.WorkingPeriod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomStaffRecruitmentPostDtoTest {

    @Test
    void of_usesRoadNameAddressFirst() {
        StaffRecruitment recruitment = StaffRecruitment.builder()
                .id(1L)
                .guestHouseName("제주 게스트하우스")
                .roadNameAddress("제주특별자치도 서귀포시 중문로 1")
                .lotNumberAddress("제주특별자치도 서귀포시 중문동 10")
                .workingPeriod(WorkingPeriod.단기)
                .build();

        RandomStaffRecruitmentPostDto response = RandomStaffRecruitmentPostDto.of(recruitment, "image.jpg");

        assertThat(response.address()).isEqualTo("제주특별자치도 서귀포시 중문로 1");
    }

    @Test
    void of_fallsBackToLotNumberAddress() {
        StaffRecruitment recruitment = StaffRecruitment.builder()
                .id(1L)
                .guestHouseName("제주 게스트하우스")
                .roadNameAddress(null)
                .lotNumberAddress("제주특별자치도 서귀포시 중문동 10")
                .workingPeriod(WorkingPeriod.장기)
                .build();

        RandomStaffRecruitmentPostDto response = RandomStaffRecruitmentPostDto.of(recruitment, "image.jpg");

        assertThat(response.address()).isEqualTo("제주특별자치도 서귀포시 중문동 10");
    }
}
