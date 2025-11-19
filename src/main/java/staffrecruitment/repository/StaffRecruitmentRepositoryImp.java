package staffrecruitment.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import staffrecruitment.domain.StaffRecruitment;
import staffrecruitment.vo.*;

import java.util.List;

import static staffrecruitment.domain.QStaffRecruitment.staffRecruitment;
import static staffrecruitment.domain.QStaffRecruitmentJob.staffRecruitmentJob;

@RequiredArgsConstructor
public class StaffRecruitmentRepositoryImp implements StaffRecruitmentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<StaffRecruitment> findByFilter(Pageable pageable, StaffRecruitmentFilter filter) {
        return jpaQueryFactory.select(staffRecruitment)
                .from(staffRecruitment)
                .join(staffRecruitmentJob)
                .on(staffRecruitmentJob.staffRecruitmentId.eq(staffRecruitment.id))
                .where(
                        regionIn(filter.getRegion()),
                        genderEq(filter.getGender()),
                        workingPeriodsIn(filter.getWorkingPeriods()),
                        workScheduleIn(filter.getWorkScheduleType()),
                        keywordContains(filter.getKeyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression regionIn(List<Region> region) {
        return region.isEmpty() ? null : staffRecruitment.region.in(region);
    }

    private BooleanExpression workingPeriodsIn(List<WorkingPeriod> workingPeriods) {
        return workingPeriods.isEmpty() ? null : staffRecruitment.workingPeriod.in(workingPeriods);
    }

    private BooleanExpression genderEq(Gender gender) {
        return gender == null ? null : staffRecruitment.gender.eq(gender);
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword == null ? null : staffRecruitment.guesthouseName.contains(keyword);
    }

    private BooleanExpression workScheduleIn(List<WorkScheduleType> workScheduleTypes) {
        List<Integer> workDayValues = workScheduleTypes.stream()
                .map(WorkScheduleType::getWorkDays)
                .toList();
        return staffRecruitmentJob.workDays.in(workDayValues);
    }


    private OrderSpecifier<?> orderBy(SortType sortType) {
        return switch (sortType) {
            //조인 필요
            case SortType.최신순 -> staffRecruitment.createdAt.desc();
            case 조회순 -> null;
            case 찜_많은순 -> null;
        };
    }

}
