package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentFilter;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentImageType;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostDto;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostsResponse;
import guesthouse.staffrecruitment.repository.StaffRecruitmentImageRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentJobRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffRecruitmentService {

    private final WishService wishService;
    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final StaffRecruitmentJobRepository staffRecruitmentJobRepository;
    private final StaffRecruitmentImageRepository staffRecruitmentImageRepository;

    @Transactional(readOnly = true)
    public StaffRecruitmentDetailsDTO getDetails(Long id, Long userId) {
        StaffRecruitment recruitment = getStaffRecruitmentById(id);
        List<StaffRecruitmentJob> jobs = getJobsByRecruitmentId(id);
        List<String> representativeImages = getRepresentativeImageUrls(id);
        List<String> contentImages = getContentImageUrls(id);
        Boolean isWished = isWished(id, userId);

        return StaffRecruitmentDetailsDTO.from(recruitment, jobs, representativeImages, contentImages, isWished);
    }

    private Boolean isWished(Long id, Long userId) {
        if (isGuest(userId))
            return false;

        return wishService.isWished(id, userId);
    }

    private boolean isGuest(Long userId) {
        return userId == null;
    }

    @Transactional(readOnly = true)
    public StaffRecruitment getStaffRecruitmentById(Long id) {
        return staffRecruitmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스태프 모집글이 존재하지 않습니다"));
    }

    @Transactional(readOnly = true)
    public List<StaffRecruitmentJob> getJobsByRecruitmentId(Long id) {
        return staffRecruitmentJobRepository.findJobsByStaffRecruitmentId(id);
    }

    @Transactional(readOnly = true)
    public List<String> getRepresentativeImageUrls(Long recruitmentId) {
        return staffRecruitmentImageRepository.findByStaffRecruitmentIdAndType(recruitmentId, StaffRecruitmentImageType.대표이미지)
                .stream()
                .sorted(Comparator.comparing(StaffRecruitmentImage::getIndex))
                .map(StaffRecruitmentImage::getImageUrl)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getContentImageUrls(Long recruitmentId) {
        return staffRecruitmentImageRepository.findByStaffRecruitmentIdAndType(recruitmentId, StaffRecruitmentImageType.내용이미지)
                .stream()
                .sorted(Comparator.comparing(StaffRecruitmentImage::getIndex))
                .map(StaffRecruitmentImage::getImageUrl)
                .toList();
    }

    @Transactional(readOnly = true)
    public StaffRecruitmentPostsResponse getStaffRecruitments(Long userId, int pageNumber, StaffRecruitmentFilter filter) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<StaffRecruitment> staffRecruitments = staffRecruitmentRepository.findByFilter(pageable, filter);

        List<StaffRecruitmentPostDto> dtos = new ArrayList<>();
        for (StaffRecruitment staffRecruitment : staffRecruitments) {
            StaffRecruitmentImage image = staffRecruitmentImageRepository.getRepresentativeImageByStaffRecruitmentId(staffRecruitment.getId());
            StaffRecruitmentPostDto staffRecruitmentPostDto = new StaffRecruitmentPostDto(
                    staffRecruitment.getId(),
                    staffRecruitment.getGuesthouseName(),
                    List.of(staffRecruitment.getWorkingPeriod().name()),
                    staffRecruitment.getRegion().name(),
                    isWished(staffRecruitment.getId(), userId),
                    image.getImageUrl()
            );
            dtos.add(staffRecruitmentPostDto);
        }
        return new StaffRecruitmentPostsResponse(dtos);
    }
}
