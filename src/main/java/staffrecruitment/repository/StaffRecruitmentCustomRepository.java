package staffrecruitment.repository;

import org.springframework.data.domain.Pageable;
import staffrecruitment.domain.StaffRecruitment;
import staffrecruitment.vo.StaffRecruitmentFilter;

import java.util.List;

public interface StaffRecruitmentCustomRepository {
    List<StaffRecruitment> findByFilter(Pageable pageable, StaffRecruitmentFilter staffRecruitmentFilter);

}

