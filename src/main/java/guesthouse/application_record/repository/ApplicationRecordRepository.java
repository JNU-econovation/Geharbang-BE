package guesthouse.application_record.repository;

import guesthouse.application_record.domain.model.ApplicationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord,Long> {
    Boolean existsByStaffRecruitmentIdAndUserId(Long recruitmentId, Long userId);

    List<ApplicationRecord> findByStaffRecruitmentId(Long recruitmentId);
}
