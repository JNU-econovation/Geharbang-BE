package guesthouse.application_record.repository;

import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.application_record.domain.vo.Status;
import guesthouse.application_record.exception.ApplicationRecordErrorCode;
import guesthouse.application_record.exception.ApplicationRecordException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {
    Boolean existsByStaffRecruitmentIdAndUserId(Long recruitmentId, Long userId);

    boolean existsByStaffRecruitmentIdAndUserIdAndStatus(Long recruitmentId, Long userId, Status status);

    List<ApplicationRecord> findAllByStaffRecruitmentId(Long recruitmentId);

    default ApplicationRecord findByIdOrThrow(Long applicationRecordId) {
        return findById(applicationRecordId)
                .orElseThrow(() -> new ApplicationRecordException(ApplicationRecordErrorCode.NOT_FOUND));
    }

    Page<ApplicationRecord> findAllByUserId(Pageable pageable, Long userId);

    Page<ApplicationRecord> findAllByUserIdAndStatus(Pageable pageable, Long userId, Status status);
}
