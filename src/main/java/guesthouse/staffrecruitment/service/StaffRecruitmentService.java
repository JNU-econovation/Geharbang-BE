package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentFilter;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentImageType;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.response.QuestionResponse;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostDto;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostsResponse;
import guesthouse.staffrecruitment.random.RandomStaffRecruitmentPostDto;
import guesthouse.staffrecruitment.random.RandomStaffRecruitmentPostsResponse;
import guesthouse.staffrecruitment.repository.StaffRecruitmentImageRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentJobRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentQuestionsRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.service.UserService;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
@Service
@RequiredArgsConstructor
public class StaffRecruitmentService {

    private final WishService wishService;
    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final StaffRecruitmentJobRepository staffRecruitmentJobRepository;
    private final StaffRecruitmentImageRepository staffRecruitmentImageRepository;
    private final StaffRecruitmentQuestionsRepository staffRecruitmentQuestionsRepository;
    private final UserService userService;


    @Transactional(readOnly = true)
    public StaffRecruitmentDetailsDTO getDetails(Long id, Long userId) {
        StaffRecruitment recruitment = getStaffRecruitmentById(id);
        List<StaffRecruitmentJob> jobs = getJobsByRecruitmentId(id);
        List<String> representativeImages = getRepresentativeImageUrls(id);
        List<String> contentImages = getContentImageUrls(id);
        Boolean isWished = isWished(id, userId);

        return StaffRecruitmentDetailsDTO.from(recruitment, jobs, representativeImages, contentImages, isWished);
    }

    private Boolean isWished(Long staffRecruitmentId, Long userId) {
        if (isGuest(userId))
            return false;

        return wishService.isWished(userId, staffRecruitmentId);
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
    public QuestionResponse getQuestions(Long userId, Long recruitmentId) {
        StaffRecruitment staffRecruitment = getStaffRecruitmentById(recruitmentId);
        List<StaffRecruitmentQuestion> questions = staffRecruitmentQuestionsRepository.findAllByStaffRecruitmentId(staffRecruitment.getId());
        StaffRecruitmentImage image = staffRecruitmentImageRepository.getRepresentativeImageByStaffRecruitmentId(staffRecruitment.getId());
        User user = userService.findById(userId);
        return QuestionResponse.of(
                staffRecruitment,
                image.getImageUrl(),
                user.getPersonalInfo().getName(),
                user.getProfileImageUrl(),
                questions
        );
    }


    @Transactional(readOnly = true)
    public StaffRecruitmentPostsResponse getStaffRecruitments(Long userId, int pageNumber, StaffRecruitmentFilter filter) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<StaffRecruitment> staffRecruitments = staffRecruitmentRepository.searchByFilter(pageable, filter);

        List<StaffRecruitmentPostDto> DTOs = staffRecruitments.stream()
                .map(staffRecruitment -> createDTO(staffRecruitment, userId))
                .toList();
        return new StaffRecruitmentPostsResponse(DTOs);

    }

    private StaffRecruitmentPostDto createDTO(StaffRecruitment staffRecruitment, Long userId) {
        StaffRecruitmentImage image = staffRecruitmentImageRepository.getRepresentativeImageByStaffRecruitmentId(staffRecruitment.getId());
        return StaffRecruitmentPostDto.of(
                staffRecruitment,
                isWished(staffRecruitment.getId(), userId),
                image.getImageUrl()
        );
    }

    public StaffRecruitmentQuestion getStaffRecruitmentQuestion(Long staffRecruitmentQuestionId, Long staffRecruitmentId) {
        return staffRecruitmentQuestionsRepository.findByIdAndStaffRecruitmentId(staffRecruitmentQuestionId, staffRecruitmentId)
                .orElseThrow(() -> new IllegalArgumentException("질문이 존재하지 않습니다"));
    }

    public Boolean hasQuestion(Long recruitmentId) {
        return staffRecruitmentQuestionsRepository.existsByStaffRecruitmentId(recruitmentId);
    }

    public RandomStaffRecruitmentPostsResponse getRandomStaffRecruitments(Region region) {
        List<StaffRecruitment> staffRecruitments = staffRecruitmentRepository.findRandom(10, region);
        List<RandomStaffRecruitmentPostDto> DTOs = staffRecruitments.stream()
                .map(this::createRandomDTO)
                .toList();
        return new RandomStaffRecruitmentPostsResponse(DTOs);
    }

    private RandomStaffRecruitmentPostDto createRandomDTO(StaffRecruitment staffRecruitment) {
        StaffRecruitmentImage image = staffRecruitmentImageRepository.getRepresentativeImageByStaffRecruitmentId(staffRecruitment.getId());
        return RandomStaffRecruitmentPostDto.of(staffRecruitment, image.getImageUrl());
    }
}
