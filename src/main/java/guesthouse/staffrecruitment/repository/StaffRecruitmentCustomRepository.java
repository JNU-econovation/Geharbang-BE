package guesthouse.staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentFilter;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffRecruitmentCustomRepository {
    List<StaffRecruitment> searchByFilter(Pageable pageable, StaffRecruitmentFilter staffRecruitmentFilter);

    List<StaffRecruitment> findRandom(int count, Region region);
}

