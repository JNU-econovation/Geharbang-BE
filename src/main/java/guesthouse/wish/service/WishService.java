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
        Optional<Wish> optionalWish = wishRepository.findByUserIdAndStaffRecruitmentId(userId, staffRecruitmentId);

        if (optionalWish.isPresent()) {
            return optionalWish.get().getId();
        }

        Wish wish = new Wish(userId, staffRecruitmentId);
        wishRepository.save(wish);
        return wish.getId();
    }

    @Transactional
    public void deleteWish(Long userId, Long wishId) {
        Optional<Wish> optionalWish = wishRepository.findByUserIdAndWishId(userId, wishId);

        if (optionalWish.isEmpty()) {
            return;
        }

        wishRepository.deleteById(wishId);
    }

}
