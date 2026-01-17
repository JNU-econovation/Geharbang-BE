package guesthouse.certificate.dto.response;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.CertificateType;
import lombok.Builder;

@Builder
public record CertificateDetailsDTO(
        String guestHouseName,
        String ownerName,
        String phoneNumber,
        CertificateType certificateType,
        String fileUrl,
        String fileName
) {
    public static CertificateDetailsDTO from(Certificate certificate) {
        return CertificateDetailsDTO.builder()
                .guestHouseName(certificate.getGuestHouseName())
                .ownerName(certificate.getOwnerName())
                .phoneNumber(certificate.getPhoneNumber())
                .certificateType(certificate.getCertificateType())
                .fileUrl(certificate.getFileUrl())
                .fileName(certificate.getFileName())
                .build();
    }
}
