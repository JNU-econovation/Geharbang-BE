package guesthouse.guestHousePost.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.*;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static guesthouse.guestHousePost.domain.model.QAmenity.amenity;
import static guesthouse.guestHousePost.domain.model.QGuestHousePost.guestHousePost;
import static guesthouse.guestHousePost.domain.model.QParty.party;
import static guesthouse.guestHousePost.domain.model.QRoom.room;
import static guesthouse.wish.domain.model.QWish.wish;

@RequiredArgsConstructor
public class GuestHousePostRepositoryImpl implements GuestHousePostCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GuestHousePost> searchByFilter(Pageable pageable, GuestHouseFilter filter) {
        var query = jpaQueryFactory.selectDistinct(guestHousePost)
                .from(guestHousePost)
                .leftJoin(room)
                .on(guestHousePost.id.eq(room.guestHousePostId))
                .leftJoin(party)
                .on(guestHousePost.id.eq(party.guestHousePostId))
                .leftJoin(amenity)
                .on(guestHousePost.id.eq(amenity.guestHousePostId))
                .where(
                        regionIn(filter.getRegions()),
                        keywordContains(filter.getKeyword()),
                        priceBetween(filter.getLowestRoomPrice(), filter.getHighestRoomPrice()),
                        roomTypeIn(filter.getRoomTypes()),
                        headCountTypeIn(filter.getHeadCountTypes()),
                        partyTypeIn(filter.getPartyTypes()),
                        moodAllMatch(filter.getMoods()),
                        amenityIn(filter.getAmenities()),
                        isActive()
                )
                .groupBy(guestHousePost.id)
                .having(amenityCountEq(filter.getAmenities()));

        OrderSpecifier<?> order = orderBy(filter.getSortType());
        if (order != null) {
            query = query.orderBy(order);
        }

        return query
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
        return Expressions.anyOf(headCountTypes.stream()
                .map(this::headCountTypeEq)
                .toArray(BooleanExpression[]::new));
    }

    private BooleanExpression headCountTypeEq(RoomHeadCount headCountType) {
        return switch (headCountType) {
            case _1인실 -> room.headCount.eq(1);
            case _2인실 -> room.headCount.eq(2);
            case _3인이상 -> room.headCount.goe(3);
        };
    }

    private BooleanExpression priceBetween(Integer lowestRoomPrice, Integer highestRoomPrice) {
        if (lowestRoomPrice == null || highestRoomPrice == null) {
            return null;
        }
        return room.pricePerNight.between(lowestRoomPrice, highestRoomPrice);
    }

    private BooleanExpression moodAllMatch(List<Mood> moods) {
        if (moods == null || moods.isEmpty()) {
            return null;
        }

        return Expressions.allOf(moods.stream()
                .map(m -> guestHousePost.moods.contains(m))
                .toArray(BooleanExpression[]::new));
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

    private BooleanExpression isActive() {
        return guestHousePost.status.eq(Status.ACTIVE);
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
            case 최신순, 조회순 -> null;
            case 찜_많은순 -> new OrderSpecifier<>(
                    Order.DESC,
                    JPAExpressions.select(wish.count())
                            .from(wish)
                            .where(wish.guestHousePostId.eq(guestHousePost.id))
            );
        };
    }

    @Override
    public List<GuestHousePost> findRandom(int count, Region region) {
        return jpaQueryFactory
                .selectFrom(guestHousePost)
                .where(guestHousePost.region.eq(region),
                        isActive())
                .orderBy(Expressions.numberTemplate(Double.class, "RAND()").asc())
                .limit(count)
                .fetch();
    }

}
