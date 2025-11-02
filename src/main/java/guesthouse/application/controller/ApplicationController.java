package guesthouse.application.controller;

import guesthouse.application.dto.requset.ApplicationSaveRequest;
import guesthouse.application.dto.response.ApplicationSaveResponse;
import guesthouse.application.service.ApplicationService;
import guesthouse.common.annotation.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/application")
@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationSaveResponse> saveApplication(
            @RequestBody @Valid ApplicationSaveRequest applicationSaveRequest,
            @UserId Long userId
    ) {
        Long applicationId = applicationService.save(applicationSaveRequest, userId);
        return ResponseEntity.ok(new ApplicationSaveResponse(applicationId));
    }
}
