package guesthouse.application.controller;

import guesthouse.application.dto.QuestionAnswerDTO;
import guesthouse.application.dto.requset.ApplicationApplyRequest;
import guesthouse.application.dto.requset.ApplicationSaveRequest;
import guesthouse.application.dto.response.ApplicationSaveResponse;
import guesthouse.application.service.ApplicationRecordService;
import guesthouse.application.service.ApplicationService;
import guesthouse.common.annotation.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/application")
@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationRecordService applicationRecordService;

    @PostMapping
    public ResponseEntity<ApplicationSaveResponse> saveApplication(
            @RequestBody @Valid ApplicationSaveRequest applicationSaveRequest,
            @UserId Long userId
    ) {
        Long applicationId = applicationService.save(applicationSaveRequest, userId);
        return ResponseEntity.ok(new ApplicationSaveResponse(applicationId));
    }

    @PostMapping("/staff-recruitment/{recruitmentId}")
    public ResponseEntity<Void> applyForStaffRecruitment(
            @RequestBody ApplicationApplyRequest applicationApplyRequest,
            @PathVariable Long recruitmentId,
            @UserId Long userId
    ) {
        List<QuestionAnswerDTO> answers = applicationApplyRequest.answers();
        applicationRecordService.apply(answers, recruitmentId, userId);
        return ResponseEntity.ok().build();
    }
}
