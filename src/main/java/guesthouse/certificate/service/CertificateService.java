package guesthouse.certificate.service;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.dto.request.SubmitCertificateRequest;
import guesthouse.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;

    @Transactional
    public void submitCertificate(SubmitCertificateRequest request, Long userId) {
        Certificate certificate = Certificate.builder()
                .certificateType(request.certificateType())
                .guestHouseName(request.guestHouseName())
                .phoneNumber(request.phoneNumber())
                .ownerName(request.ownerName())
                .fileUrl(request.fileUrl())
                .userId(userId)
                .build();

        certificateRepository.save(certificate);
    }

}
