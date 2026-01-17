package guesthouse.staffrecruitment.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static guesthouse.staffrecruitment.domain.model.QStaffRecruitment.staffRecruitment;
import static guesthouse.staffrecruitment.domain.model.QStaffRecruitmentJob.staffRecruitmentJob;
import static guesthouse.staffrecruitment.domain.vo.SortType.*;


@RequiredArgsConstructor
public class StaffRecruitmentRepositoryImpl implements StaffRecruitmentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<StaffRecruitment> searchByFilter(Pageable pageable, StaffRecruitmentFilter filter) {
        return jpaQueryFactory.selectDistinct(staffRecruitment)
                .from(staffRecruitment)
                .leftJoin(staffRecruitmentJob)
                .on(staffRecruitmentJob.staffRecruitmentId.eq(staffRecruitment.id))
                .where(
                        regionIn(filter.getRegion()),
                        genderEq(filter.getGender()),
                        workTypeEq(filter.getWorkType()),
                        workDaysEq(filter.getWorkDays()),
                        restDaysEq(filter.getRestDays()),
                        workingPeriodsIn(filter.getWorkingPeriods()),
                        workScheduleIn(filter.getWorkScheduleType()),
                        keywordContains(filter.getKeyword()),
                        isActive()
                )
                .orderBy(orderBy(filter.getSortType()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression regionIn(List<Region> region) {
        if (region == null || region.isEmpty()) {
            return null;
        }
        return staffRecruitment.region.in(region);
    }

    private BooleanExpression workingPeriodsIn(List<WorkingPeriod> workingPeriods) {
        if (workingPeriods == null || workingPeriods.isEmpty()) {
            return null;
        }
        return staffRecruitment.workingPeriod.in(workingPeriods);
    }

    private BooleanExpression genderEq(Gender gender) {
        return gender == null ? null : staffRecruitment.feature.gender.eq(gender);
    }

    private Predicate workTypeEq(WorkType workType) {
        return workType == null ? null : staffRecruitmentJob.standard.eq(workType);
    }

    private Predicate workDaysEq(Integer workDays) {
        return workDays == null ? null : staffRecruitmentJob.workDays.eq(workDays);
    }

    private Predicate restDaysEq(Integer restDays) {
        return restDays == null ? null : staffRecruitmentJob.restDays.eq(restDays);
    }


    private BooleanExpression keywordContains(String keyword) {
        return keyword == null ? null : staffRecruitment.guestHouseName.contains(keyword);
    }

    private BooleanExpression workScheduleIn(List<WorkScheduleType> workScheduleTypes) {
        if (workScheduleTypes == null || workScheduleTypes.isEmpty()) {
            return null;
        }

        List<Integer> workDayValues = workScheduleTypes.stream()
                .map(WorkScheduleType::getWorkDays)
                .toList();
        return staffRecruitmentJob.workDays.in(workDayValues);
    }

    private BooleanExpression isActive() {
        return staffRecruitment.status.eq(Status.ACTIVE);
    }


    private OrderSpecifier<?> orderBy(SortType sortType) {
        return switch (sortType) {
            case 최신순 -> staffRecruitment.createdAt.desc();
            case 조회순 -> staffRecruitment.viewCount.desc();
            case 찜_많은순 -> null;
        };
    }


    @Override
    public List<StaffRecruitment> findRandom(int count, Region region) {
        return jpaQueryFactory
                .selectFrom(staffRecruitment)
                .where(staffRecruitment.region.eq(region),
                        isActive())
                .orderBy(Expressions.numberTemplate(Double.class, "RAND()").asc())
                .limit(count)
                .fetch();
    }
}
