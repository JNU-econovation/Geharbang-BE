package guesthouse.guestHousePost.dto.response;

import java.util.List;

public record GuestHouseAiDataResponse(
        List<Item> guestHouses
) {
    public record Item(
            Long id,
            GuestHousePostDetailsResponse details
    ) { }
}
