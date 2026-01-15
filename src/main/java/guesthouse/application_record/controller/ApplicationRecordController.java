package guesthouse.application_record.controller;

import guesthouse.application_record.dto.response.ApplicationRecordQuestionsResponse;
import guesthouse.application_record.dto.response.ApplicationRecordResponse;
import guesthouse.application_record.dto.response.SubmittedApplicationsResponse;
import guesthouse.application_record.service.ApplicationRecordService;
import guesthouse.common.annotation.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/application-records")
@RestController
@RequiredArgsConstructor
public class ApplicationRecordController {

    private final ApplicationRecordService applicationRecordService;

    @GetMapping("/{id}/all")
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

    @GetMapping("/questions/{recordId}")
    public ResponseEntity<ApplicationRecordQuestionsResponse> getApplicationRecordQuestions(
            @UserId Long userId,
            @PathVariable("recordId") Long applicationRecordId
    ) {
        ApplicationRecordQuestionsResponse response = applicationRecordService.getApplicationRecordQuestions(userId, applicationRecordId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{recordId}")
    public ResponseEntity<Void> approveApplicationRecord(
            @UserId Long userId,
            @PathVariable("recordId") Long applicationRecordId
    ) {
        applicationRecordService.approveApplicationRecord(userId, applicationRecordId);
        return ResponseEntity.ok().build();
    }

}
