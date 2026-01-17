package guesthouse.staffrecruitment.dto.request;

import guesthouse.staffrecruitment.domain.vo.Status;

public record ChangeStatusRequest(
        Status status
) {
}
