package guesthouse.application_record.controller;

import guesthouse.application_record.dto.response.ApplicationRecordResponse;
import guesthouse.application_record.service.ApplicationRecordService;
import guesthouse.application_record.dto.response.SubmittedApplicationsResponse;
import guesthouse.common.annotation.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/application-records")
@RestController
@RequiredArgsConstructor
public class ApplicationRecordController {

    private final ApplicationRecordService applicationRecordService;

    @GetMapping("/{id}")
    public ResponseEntity<SubmittedApplicationsResponse> getApplicationRecords(
            @UserId Long userId,
            @PathVariable("id") Long staffRecruitmentId
    ) {
        SubmittedApplicationsResponse response = applicationRecordService.getApplicationRecords(userId, staffRecruitmentId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{recordId}")
    public ResponseEntity<ApplicationRecordResponse> getApplicationRecord(
            @UserId Long userId,
            @PathVariable("recordId") Long applicationRecordId
    ) {
        ApplicationRecordResponse response = applicationRecordService.getApplicationRecord(userId, applicationRecordId);
        return ResponseEntity.ok(response);
    }

}
