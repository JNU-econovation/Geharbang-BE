package guesthouse.wish.service;

import guesthouse.wish.domain.model.Wish;
import guesthouse.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishService {
    private final WishRepository wishRepository;

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

}
