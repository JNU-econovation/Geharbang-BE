package guesthouse.certificate.repository;

import guesthouse.certificate.domain.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate,Long> {
    Boolean existsByUserId(Long userId);

    Optional<Certificate> findByUserId(Long userId);
}
