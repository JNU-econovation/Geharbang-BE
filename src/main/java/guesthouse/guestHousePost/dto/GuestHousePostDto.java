package guesthouse.guestHousePost.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GuestHousePostDto {

    private Long id;
    private String guestHouseName;
    private List<String> tags;
    private String region;
    @JsonProperty("isWished")
    private boolean isWished;
    private String imageUrl;

    public static GuestHousePostDto of(GuestHousePost guestHousePost, boolean isWished, String imageUrl) {
        return new GuestHousePostDto(
                guestHousePost.getId(),
                guestHousePost.getGuestHouseName(),
                guestHousePost.getMoods().stream().map(Enum::name).toList(),
                guestHousePost.getRegion().name(),
                isWished,
                imageUrl
                );

    }
}
