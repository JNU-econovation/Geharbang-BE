package guesthouse.certificate.repository;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate,Long> {
    List<Certificate> findAllByStatus(Status status);
}
