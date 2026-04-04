package guesthouse.certificate.repository;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate,Long> {
    Boolean existsByUserIdAndStatus(Long userId, Status status);

    List<Certificate> findByUserId(Long userId);
}
