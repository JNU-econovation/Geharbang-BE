package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentFilter;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentImageType;
import guesthouse.staffrecruitment.domain.vo.Status;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.request.StaffRecruitmentCreateRequest;
import guesthouse.staffrecruitment.dto.response.*;
import guesthouse.staffrecruitment.exception.StaffRecruitmentErrorCode;
import guesthouse.staffrecruitment.exception.StaffRecruitmentException;
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


    @Transactional
    public StaffRecruitmentDetailsDTO getDetails(Long id, Long userId) {
        StaffRecruitment recruitment = getStaffRecruitmentById(id);
        List<StaffRecruitmentJob> jobs = getJobsByRecruitmentId(id);
        List<String> representativeImages = getRepresentativeImageUrls(id);
        List<String> contentImages = getContentImageUrls(id);
        Boolean isWished = isWished(id, userId);

        recruitment.plusViewCount();
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
                .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND));
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
    public String getFirstRepresentativeImageUrls(Long recruitmentId) {
        return staffRecruitmentImageRepository.getRepresentativeImageByStaffRecruitmentId(recruitmentId).getImageUrl();
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
                .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND_QUESTION));
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

    @Transactional
    public Long createStaffRecruitment(Long userId, StaffRecruitmentCreateRequest request) {
        StaffRecruitment staffRecruitment = StaffRecruitmentMapper.from(request, userId);
        staffRecruitmentRepository.save(staffRecruitment);

        List<StaffRecruitmentJob> jobs = StaffRecruitmentJobMapper.from(request, staffRecruitment.getId());
        staffRecruitmentJobRepository.saveAll(jobs);

        List<StaffRecruitmentQuestion> questions = StaffRecruitmentQuestionMapper.from(request, staffRecruitment.getId());
        staffRecruitmentQuestionsRepository.saveAll(questions);

        List<StaffRecruitmentImage> images = StaffRecruitmentImageMapper.from(request, staffRecruitment.getId());
        staffRecruitmentImageRepository.saveAll(images);

        return staffRecruitment.getId();
    }

    public OwnerStaffRecruitmentPostsResponse getOwnStaffRecruitmentPosts(Long userId) {
        List<StaffRecruitment> staffRecruitments = staffRecruitmentRepository.findByOwnerId(userId);
        List<String> imageUrls = staffRecruitments.stream()
                .map(s -> staffRecruitmentImageRepository.getRepresentativeImageByStaffRecruitmentId(s.getId()))
                .map(StaffRecruitmentImage::getImageUrl)
                .toList();
        List<OwnerStaffRecruitmentPostDto> dtos = OwnerStaffRecruitmentPostDto.of(staffRecruitments, imageUrls);
        return new OwnerStaffRecruitmentPostsResponse(dtos);
    }


    @Transactional
    public void changeStatus(Status status, Long userId, Long staffRecruitmentId) {
        StaffRecruitment staffRecruitment = getStaffRecruitmentById(staffRecruitmentId);

        if (!existsByOwnerIdAndId(userId, staffRecruitmentId))
            throw new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND);

        staffRecruitment.changeStatus(status);
    }

    public boolean existsByOwnerIdAndId(Long userId, Long staffRecruitmentId) {
        return staffRecruitmentRepository.existsByOwnerIdAndId(userId, staffRecruitmentId);
    }
}
