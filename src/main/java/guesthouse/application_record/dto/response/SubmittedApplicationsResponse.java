package guesthouse.application_record.dto.response;

import java.util.List;

public record SubmittedApplicationsResponse(
        List<SubmittedApplicationDto> submittedApplications
) {
}
