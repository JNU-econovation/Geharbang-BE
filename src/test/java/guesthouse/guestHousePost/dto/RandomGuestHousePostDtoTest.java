package guesthouse.guestHousePost.dto;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.Mood;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RandomGuestHousePostDtoTest {

    @Test
    void of_usesRoadNameAddressFirst() {
        GuestHousePost post = GuestHousePost.builder()
                .id(1L)
                .guestHouseName("제주 게스트하우스")
                .roadNameAddress("제주특별자치도 제주시 해안로 1")
                .lotNumberAddress("제주특별자치도 제주시 애월읍 10")
                .moods(Set.of(Mood.바닷가))
                .build();

        RandomGuestHousePostDto response = RandomGuestHousePostDto.of(post, "image.jpg");

        assertThat(response.address()).isEqualTo("제주특별자치도 제주시 해안로 1");
    }

    @Test
    void of_fallsBackToLotNumberAddress() {
        GuestHousePost post = GuestHousePost.builder()
                .id(1L)
                .guestHouseName("제주 게스트하우스")
                .roadNameAddress(" ")
                .lotNumberAddress("제주특별자치도 제주시 애월읍 10")
                .moods(Set.of(Mood.조용한))
                .build();

        RandomGuestHousePostDto response = RandomGuestHousePostDto.of(post, "image.jpg");

        assertThat(response.address()).isEqualTo("제주특별자치도 제주시 애월읍 10");
    }
}
