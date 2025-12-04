package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentImageType;
import guesthouse.staffrecruitment.dto.request.StaffRecruitmentCreateRequest;

import java.util.ArrayList;
import java.util.List;

public final class StaffRecruitmentImageMapper {

    private StaffRecruitmentImageMapper() {

    }

    public static List<StaffRecruitmentImage> from(StaffRecruitmentCreateRequest request, Long staffRecruitmentId) {
        List<StaffRecruitmentImage> images = new ArrayList<>();

        List<String> representativeImageUrls = request.representativeImageUrls();
        images.addAll(createFrom(staffRecruitmentId, representativeImageUrls, StaffRecruitmentImageType.대표이미지));

        List<String> contentImageUrls = request.introduction().imageUrls();
        images.addAll(createFrom(staffRecruitmentId, contentImageUrls, StaffRecruitmentImageType.내용이미지));

        return images;
    }

    private static List<StaffRecruitmentImage> createFrom(Long staffRecruitmentId, List<String> imageUrls, StaffRecruitmentImageType type) {
        List<StaffRecruitmentImage> images = new ArrayList<>();
        for (int index = 0; index < imageUrls.size(); index++) {
            String representativeImageUrl = imageUrls.get(index);
            StaffRecruitmentImage image = StaffRecruitmentImage.builder()
                    .staffRecruitmentId(staffRecruitmentId)
                    .imageUrl(representativeImageUrl)
                    .index(index)
                    .type(type)
                    .build();
            images.add(image);
        }
        return images;
    }


}
