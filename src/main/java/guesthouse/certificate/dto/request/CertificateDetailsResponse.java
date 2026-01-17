package guesthouse.certificate.dto.request;

import guesthouse.certificate.domain.vo.CertificateType;
import guesthouse.certificate.dto.response.CertificateDetailsDTO;
import lombok.Builder;

@Builder
public record CertificateDetailsResponse(
        String guestHouseName,
        String ownerName,
        String phoneNumber,
        CertificateType certificateType,
        String fileUrl,
        String fileName
) {
    public static CertificateDetailsResponse from(CertificateDetailsDTO details) {
        return CertificateDetailsResponse.builder()
                .guestHouseName(details.guestHouseName())
                .ownerName(details.ownerName())
                .phoneNumber(details.phoneNumber())
                .certificateType(details.certificateType())
                .fileUrl(details.fileUrl())
                .fileName(details.fileName())
                .build();
    }
}
