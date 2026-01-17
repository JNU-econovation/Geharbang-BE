package guesthouse.guestHousePost.dto.request;

import guesthouse.guestHousePost.domain.vo.Status;

public record ChangeStatusRequest(
        Status status
) {
}
