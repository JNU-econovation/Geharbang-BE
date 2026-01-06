package guesthouse.guestHousePost.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.Mood;
import guesthouse.guestHousePost.domain.vo.PartyType;
import guesthouse.guestHousePost.domain.vo.RoomHeadCount;
import guesthouse.guestHousePost.domain.vo.RoomType;
import guesthouse.guestHousePost.domain.vo.GuestHouseFilter;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static guesthouse.guestHousePost.domain.model.QAmenity.amenity;
import static guesthouse.guestHousePost.domain.model.QGuestHousePost.guestHousePost;
import static guesthouse.guestHousePost.domain.model.QParty.party;
import static guesthouse.guestHousePost.domain.model.QRoom.room;

@RequiredArgsConstructor
public class GuestHousePostRepositoryImpl implements GuestHousePostCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GuestHousePost> searchByFilter(Pageable pageable, GuestHouseFilter filter) {
        return jpaQueryFactory.selectDistinct(guestHousePost)
                .from(guestHousePost)
                .join(room)
                .on(guestHousePost.id.eq(room.guestHousePostId))
                .join(party)
                .on(guestHousePost.id.eq(party.GuestHousePostId))
                .join(amenity)
                .on(guestHousePost.id.eq(amenity.guestHousePostId))
                .where(
                        regionIn(filter.getRegions()),
                        keywordContains(filter.getKeyword()),
                        priceBetween(filter.getLowestRoomPrice(), filter.getHighestRoomPrice()),
                        roomTypeIn(filter.getRoomTypes()),
                        headCountTypeIn(filter.getHeadCountTypes()),
                        partyTypeIn(filter.getPartyTypes()),
                        moodAllMatch(filter.getMoods()),
                        amenityIn(filter.getAmenities())
                        )
                .groupBy(guestHousePost.id)
                .having(
                        amenityCountEq(filter.getAmenities())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression regionIn(List<Region> region) {
        if (region == null || region.isEmpty()) {
            return null;
        }
        return guestHousePost.region.in(region);
    }

    private BooleanExpression headCountTypeIn(List<RoomHeadCount> headCountTypes) {
        if (headCountTypes == null || headCountTypes.isEmpty()) {
            return null;
        }
        return room.headCount.in(headCountTypes);
    }

    private BooleanExpression priceBetween(Integer lowestRoomPrice, Integer highestRoomPrice) {
        if (lowestRoomPrice == null || highestRoomPrice == null) {
            return null;
        }
        return room.pricePerNight.between(lowestRoomPrice, highestRoomPrice);
    }

    private BooleanExpression moodAllMatch(List<Mood> moods) {
        if (moods == null || moods.isEmpty()) return null;

        return guestHousePost.moods.size().eq(moods.size())
                .and(guestHousePost.moods.any().in(moods));
    }

    private BooleanExpression roomTypeIn(List<RoomType> roomTypes) {
        if (roomTypes == null || roomTypes.isEmpty()) {
            return null;
        }
        return room.type.in(roomTypes);
    }

    private BooleanExpression partyTypeIn(List<PartyType> partyTypes) {
        if (partyTypes == null || partyTypes.isEmpty()) {
            return null;
        }
        return party.partyType.in(partyTypes);
    }

    private BooleanExpression amenityIn(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return null;
        }
        return amenity.value.in(amenities);
    }


    private BooleanExpression keywordContains(String keyword) {
        return keyword == null ? null : guestHousePost.guestHouseName.contains(keyword);
    }

    private BooleanExpression amenityCountEq(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return null;
        }
        return amenity.value.countDistinct().eq((long) amenities.size());
    }

    private OrderSpecifier<?> orderBy(SortType sortType) {
        return switch (sortType) {
            //조인 필요
            case SortType.최신순 -> null;
            case 조회순 -> null;
            case 찜_많은순 -> null;
        };
    }

}

