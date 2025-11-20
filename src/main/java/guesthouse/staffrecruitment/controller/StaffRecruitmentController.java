package guesthouse.staffrecruitment.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentDetailsResponse;
import guesthouse.staffrecruitment.service.StaffRecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/staff-recruitment")
@RestController
@RequiredArgsConstructor
public class StaffRecruitmentController {

    private final StaffRecruitmentService staffRecruitmentService;

    @GetMapping("/{id}/details")
    public ResponseEntity<StaffRecruitmentDetailsResponse> getDetails (
            @PathVariable Long id,
            @UserId (required = false)  Long userId
    ) {
        StaffRecruitmentDetailsDTO details= staffRecruitmentService.getDetails(id, userId);
        return ResponseEntity.ok(StaffRecruitmentDetailsResponse.from(details));
    }
}
