package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentImageType;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.response.QuestionResponse;
import guesthouse.staffrecruitment.repository.StaffRecruitmentImageRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentJobRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentQuestionsRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.domain.model.UserImage;
import guesthouse.user.service.UserService;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
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
    public QuestionResponse getQuestions(Long userId, Long recruitmentId) {
        StaffRecruitment staffRecruitment = getStaffRecruitmentById(recruitmentId);
        List<StaffRecruitmentQuestion> questions = staffRecruitmentQuestionsRepository.findAllByStaffRecruitmentId(staffRecruitment.getId());
        User user = userService.findById(userId);
        UserImage userImage = userService.findImageByUserId(user.getId())
                .orElseGet(null);
        return QuestionResponse.of(
                staffRecruitment,
                user.getPersonalInfo().getName(), getUserImageUrl(userImage),
                questions
        );
    }

    private String getUserImageUrl(UserImage userImage) {
        if (userImage != null) {
            return userImage.getImageUrl();
        }
        return "";
    }


}
