package guesthouse.wish.service;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.model.GuestHousePostImage;
import guesthouse.guestHousePost.dto.GuestHousePostDto;
import guesthouse.guestHousePost.dto.GuestHousePostsResponse;
import guesthouse.guestHousePost.repository.GuestHousePostImageRepository;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentImage;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostDto;
import guesthouse.staffrecruitment.dto.response.StaffRecruitmentPostsResponse;
import guesthouse.staffrecruitment.repository.StaffRecruitmentImageRepository;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.wish.domain.model.Wish;
import guesthouse.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishService {
    private final WishRepository wishRepository;
    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final StaffRecruitmentImageRepository staffRecruitmentImageRepository;
    private final GuestHousePostRepository guestHousePostRepository;
    private final GuestHousePostImageRepository guestHousePostImageRepository;

    @Transactional(readOnly = true)
    public Boolean isWished(Long userId, Long staffRecruitmentId) {
        return wishRepository.existsByUserIdAndStaffRecruitmentId(userId, staffRecruitmentId);
    }

    @Transactional
    public Long addWish(Long userId, Long staffRecruitmentId) {
        return wishRepository.findByUserIdAndStaffRecruitmentId(userId, staffRecruitmentId)
                .map(Wish::getId)
                .orElseGet(() -> wishRepository.save(new Wish(userId, staffRecruitmentId)).getId());
    }

    @Transactional
    public void deleteWishByStaffRecruitmentId(Long userId, Long staffRecruitmentId) {
        wishRepository.deleteByUserIdAndStaffRecruitmentId(userId, staffRecruitmentId);
    }

    @Transactional(readOnly = true)
    public Boolean isWishedGuestHousePost(Long userId, Long guestHousePostId) {
        return wishRepository.existsByUserIdAndGuestHousePostId(userId, guestHousePostId);
    }

    @Transactional
    public Long addWishGuestHousePost(Long userId, Long guestHousePostId) {
        return wishRepository.findByUserIdAndGuestHousePostId(userId, guestHousePostId)
                .map(Wish::getId)
                .orElseGet(() -> wishRepository.save(new Wish(userId, guestHousePostId, true)).getId());
    }

    @Transactional
    public void deleteWishByGuestHousePostId(Long userId, Long guestHousePostId) {
        wishRepository.deleteByUserIdAndGuestHousePostId(userId, guestHousePostId);
    }

    @Transactional
    public void deleteWish(Long userId, Long wishId) {
        wishRepository.findByIdAndUserId(wishId, userId)
                .ifPresent(wishRepository::delete);
    }

    @Transactional(readOnly = true)
    public StaffRecruitmentPostsResponse getMyWishedStaffRecruitments(Long userId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<Long> staffRecruitmentIds = wishRepository
                .findByUserIdAndStaffRecruitmentIdIsNotNullOrderByIdDesc(userId, pageable)
                .stream()
                .map(Wish::getStaffRecruitmentId)
                .toList();

        List<StaffRecruitmentPostDto> dtos = findStaffRecruitmentsByIds(staffRecruitmentIds)
                .stream()
                .map(this::createWishedStaffRecruitmentDto)
                .toList();

        return new StaffRecruitmentPostsResponse(dtos);
    }

    @Transactional(readOnly = true)
    public GuestHousePostsResponse getMyWishedGuestHousePosts(Long userId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<Long> guestHousePostIds = wishRepository
                .findByUserIdAndGuestHousePostIdIsNotNullOrderByIdDesc(userId, pageable)
                .stream()
                .map(Wish::getGuestHousePostId)
                .toList();

        List<GuestHousePostDto> dtos = findGuestHousePostsByIds(guestHousePostIds)
                .stream()
                .map(this::createWishedGuestHousePostDto)
                .toList();

        return new GuestHousePostsResponse(dtos);
    }

    private List<StaffRecruitment> findStaffRecruitmentsByIds(List<Long> ids) {
        Map<Long, StaffRecruitment> staffRecruitmentById = staffRecruitmentRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(StaffRecruitment::getId, Function.identity()));

        return ids.stream()
                .map(staffRecruitmentById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<GuestHousePost> findGuestHousePostsByIds(List<Long> ids) {
        Map<Long, GuestHousePost> guestHousePostById = guestHousePostRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(GuestHousePost::getId, Function.identity()));

        return ids.stream()
                .map(guestHousePostById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private StaffRecruitmentPostDto createWishedStaffRecruitmentDto(StaffRecruitment staffRecruitment) {
        StaffRecruitmentImage image = staffRecruitmentImageRepository
                .getRepresentativeImageByStaffRecruitmentId(staffRecruitment.getId());

        return StaffRecruitmentPostDto.of(
                staffRecruitment,
                true,
                image == null ? "" : image.getImageUrl()
        );
    }

    private GuestHousePostDto createWishedGuestHousePostDto(GuestHousePost guestHousePost) {
        GuestHousePostImage image = guestHousePostImageRepository
                .getFirstImageByGuestHousePostId(guestHousePost.getId());

        return GuestHousePostDto.of(
                guestHousePost,
                true,
                image == null ? "" : image.getImageUrl()
        );
    }
}
