package guesthouse.staffrecruitment.dto.response;

import java.util.List;

public record StaffRecruitmentAiDataResponse(
        List<Item> staffRecruitments
) {
    public record Item(
            Long id,
            StaffRecruitmentDetailsResponse details
    ) { }
}
