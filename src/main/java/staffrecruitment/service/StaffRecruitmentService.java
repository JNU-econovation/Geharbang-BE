package staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import staffrecruitment.dto.response.StaffRecruitmentPostDto;
import staffrecruitment.dto.response.StaffRecruitmentPostsResponse;
import staffrecruitment.repository.StaffRecruitmentImageRepository;
import staffrecruitment.repository.StaffRecruitmentRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffRecruitmentService {

    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final StaffRecruitmentImageRepository staffRecruitmentImageRepository;

    public StaffRecruitmentPostsResponse getStaffRecruitments(int pageNumber, StaffRecruitmentFilter filter) {
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
                    false,
                    image.getImageUrl()
            );
            dtos.add(staffRecruitmentPostDto);
        }
        return new StaffRecruitmentPostsResponse(dtos);
    }

}
