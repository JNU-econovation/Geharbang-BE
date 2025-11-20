package staffrecruitment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import staffrecruitment.dto.response.StaffRecruitmentPostsResponse;
import staffrecruitment.service.StaffRecruitmentService;
import staffrecruitment.vo.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class RecruitmentController {

    private final StaffRecruitmentService staffRecruitmentService;

    @GetMapping("/api/v1/staff/recruitment")
    public ResponseEntity<StaffRecruitmentPostsResponse> getRecruitments(
            @RequestParam(defaultValue = "최신순") SortType sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Region> region,
            @RequestParam(required = false) List<WorkingPeriod> period,
            @RequestParam(required = false) List<WorkScheduleType> workScheduleType,
            @RequestParam(required = false) Gender gender,
            @RequestParam int pageNumber
    ) {

        StaffRecruitmentFilter filter = new StaffRecruitmentFilter(
                keyword, sort, region, gender, period, workScheduleType
        );
        StaffRecruitmentPostsResponse response = staffRecruitmentService.getStaffRecruitments(pageNumber, filter);

        return ResponseEntity.ok(response);
    }

}
