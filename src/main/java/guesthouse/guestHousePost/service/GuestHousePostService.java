package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.model.GuestHousePostImage;
import guesthouse.guestHousePost.repository.GuestHousePostImageRepository;
import guesthouse.guestHousePost.domain.vo.GuestHouseFilter;
import guesthouse.guestHousePost.dto.GuestHousePostDto;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.guestHousePost.dto.GuestHousePostsResponse;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestHousePostService {

    private final GuestHousePostRepository guestHousePostRepository;
    private final GuestHousePostImageRepository guestHousePostImageRepository;
    private final WishService wishService;

    @Transactional(readOnly = true)
    public GuestHousePostsResponse getGuestHousePosts(Long userId, int pageNumber, GuestHouseFilter filter) {
        Pageable pageable = PageRequest.of(pageNumber, 10);

        List<GuestHousePost> guestHousePosts = guestHousePostRepository.searchByFilter(pageable, filter);

        List<GuestHousePostDto> dtos = guestHousePosts.stream()
                .map(guestHousePost -> createDto(guestHousePost, userId))
                .toList();

        return new GuestHousePostsResponse(dtos);
    }

    private GuestHousePostDto createDto(GuestHousePost guestHousePost, Long userId) {
        GuestHousePostImage image = guestHousePostImageRepository.getFirstImageByGuestHousePostId(guestHousePost.getId());
        return GuestHousePostDto.of(
                guestHousePost,
                isWished(guestHousePost.getId(), userId),
                image.getImageUrl()
        );
    }

    private Boolean isWished(Long staffRecruitmentId, Long userId) {
        if (isGuest(userId))
            return false;

        return wishService.isWished(userId, staffRecruitmentId);
    }

    private boolean isGuest(Long userId) {
        return userId == null;
    }

}
