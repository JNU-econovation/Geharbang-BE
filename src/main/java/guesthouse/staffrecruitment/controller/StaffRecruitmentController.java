package guesthouse.staffrecruitment.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.staffrecruitment.domain.vo.*;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.request.ChangeStatusRequest;
import guesthouse.staffrecruitment.dto.request.StaffRecruitmentCreateRequest;
import guesthouse.staffrecruitment.dto.response.*;
import guesthouse.staffrecruitment.random.RandomStaffRecruitmentPostsResponse;
import guesthouse.staffrecruitment.service.StaffRecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-recruitment")
@RequiredArgsConstructor
@Tag(name = "Staff Recruitment", description = "스텝 구인 공고 API")
public class StaffRecruitmentController {

    private final StaffRecruitmentService staffRecruitmentService;

    @GetMapping("/{id}/details")
    public ResponseEntity<StaffRecruitmentDetailsResponse> getDetails(
            @PathVariable Long id,
            @UserId(required = false) Long userId
    ) {
        StaffRecruitmentDetailsDTO details = staffRecruitmentService.getDetails(id, userId);
        return ResponseEntity.ok(StaffRecruitmentDetailsResponse.from(details));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<QuestionResponse> getQuestions(
            @UserId Long userId,
            @PathVariable Long id
    ) {
        QuestionResponse response = staffRecruitmentService.getQuestions(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<StaffRecruitmentPostsResponse> getRecruitments(
            @RequestParam(defaultValue = "최신순") SortType sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Region> region,
            @RequestParam(required = false) WorkType workType,
            @RequestParam(required = false) Integer workDays,
            @RequestParam(required = false) Integer restDays,
            @RequestParam(required = false) List<WorkingPeriod> period,
            @RequestParam(required = false) List<WorkScheduleType> workScheduleType,
            @RequestParam(required = false) Gender gender,
            @RequestParam int pageNumber,
            @UserId(required = false) Long userId
    ) {
        StaffRecruitmentFilter filter = new StaffRecruitmentFilter(
                keyword, sort, region, workType, workDays, restDays, gender, period, workScheduleType
        );
        StaffRecruitmentPostsResponse response = staffRecruitmentService.getStaffRecruitments(userId, pageNumber, filter);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendation")
    public ResponseEntity<RandomStaffRecruitmentPostsResponse> random(@RequestParam Region region) {
        RandomStaffRecruitmentPostsResponse response = staffRecruitmentService.getRandomStaffRecruitments(region);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/map")
    @Operation(summary = "스텝 공고 지도 목록 조회", description = "지도 화면에 표시할 활성 스텝 구인 공고 목록을 조회한다.")
    public ResponseEntity<List<StaffRecruitmentMapPostDto>> getStaffRecruitmentMapPosts() {
        List<StaffRecruitmentMapPostDto> response = staffRecruitmentService.getStaffRecruitmentMapPosts();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<StaffRecruitmentIdResponse> createStaffRecruitment(
            @UserId Long userId,
            @RequestBody @Valid StaffRecruitmentCreateRequest request
    ) {
        Long staffRecruitmentId = staffRecruitmentService.createStaffRecruitment(userId, request);
        return ResponseEntity.ok(new StaffRecruitmentIdResponse(staffRecruitmentId));
    }

    @GetMapping("/owner")
    public ResponseEntity<OwnerStaffRecruitmentPostsResponse> getOwnStaffRecruitmentPosts(@UserId Long userId) {
        OwnerStaffRecruitmentPostsResponse response = staffRecruitmentService.getOwnStaffRecruitmentPosts(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "스텝 구인 공고 수정", description = "운영자가 본인이 등록한 스텝 구인 공고 내용을 수정한다.")
    public ResponseEntity<Void> updateStaffRecruitment(
            @UserId Long userId,
            @PathVariable Long id,
            @RequestBody @Valid StaffRecruitmentCreateRequest request
    ) {
        staffRecruitmentService.updateStaffRecruitment(userId, id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "스텝 구인 공고 상태 변경", description = "운영자가 본인이 등록한 스텝 구인 공고 상태를 ACTIVE 또는 INACTIVE로 변경한다.")
    public ResponseEntity<Void> changeStatus(
            @RequestBody @Valid ChangeStatusRequest changeStatusRequest,
            @UserId Long userId,
            @PathVariable("id") Long staffRecruitmentId
    ) {
        staffRecruitmentService.changeStatus(changeStatusRequest.status(), userId, staffRecruitmentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaffRecruitment(
            @UserId Long userId,
            @PathVariable("id") Long staffRecruitmentId
    ) {
        staffRecruitmentService.deleteStaffRecruitment(userId, staffRecruitmentId);
        return ResponseEntity.ok().build();
    }
}
