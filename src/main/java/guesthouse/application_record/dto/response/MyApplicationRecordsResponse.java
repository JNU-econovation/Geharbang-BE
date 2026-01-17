package guesthouse.application_record.dto.response;

import guesthouse.application_record.dto.ApplicationRecordDTO;

import java.util.List;

public record MyApplicationRecordsResponse(
        List<ApplicationRecordDTO> applicationRecords
) {
}
