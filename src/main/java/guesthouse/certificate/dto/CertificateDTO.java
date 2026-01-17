package guesthouse.certificate.dto;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.CertificateType;
import guesthouse.certificate.domain.vo.Status;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CertificateDTO(
        Long id,
        String guestHouseName,
        String ownerName,
        CertificateType certificateType,
        Status status,
        LocalDate createdAt
) {
    public static CertificateDTO from(Certificate certificate) {
        return CertificateDTO.builder()
                .id(certificate.getId())
                .guestHouseName(certificate.getGuestHouseName())
                .ownerName(certificate.getOwnerName())
                .certificateType(certificate.getCertificateType())
                .status(certificate.getStatus())
                .createdAt(certificate.getCreatedAt().toLocalDate())
                .build();
    }
}
