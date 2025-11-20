package staffrecruitment.repository;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.StaffRecruitmentFilter;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffRecruitmentCustomRepository {
    List<StaffRecruitment> findByFilter(Pageable pageable, StaffRecruitmentFilter staffRecruitmentFilter);

}

