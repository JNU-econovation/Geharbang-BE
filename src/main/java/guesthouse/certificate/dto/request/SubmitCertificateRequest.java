package guesthouse.certificate.dto.request;

import guesthouse.certificate.domain.vo.CertificateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitCertificateRequest(
        @NotNull
        CertificateType certificateType,
        @NotBlank
        String guestHouseName,
        @NotBlank
        String ownerName,
        @NotBlank
        String phoneNumber,
        @NotBlank
        String fileUrl
) {
}
