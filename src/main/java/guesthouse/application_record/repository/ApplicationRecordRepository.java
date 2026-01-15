package guesthouse.application_record.repository;

import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.application_record.exception.ApplicationRecordErrorCode;
import guesthouse.application_record.exception.ApplicationRecordException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {
    Boolean existsByStaffRecruitmentIdAndUserId(Long recruitmentId, Long userId);

    List<ApplicationRecord> findAllByStaffRecruitmentId(Long recruitmentId);

    default ApplicationRecord findByIdOrThrow(Long applicationRecordId) {
        return findById(applicationRecordId)
                .orElseThrow(() -> new ApplicationRecordException(ApplicationRecordErrorCode.NOT_FOUND));
    }
}
