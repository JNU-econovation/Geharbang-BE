package guesthouse.certificate.dto.response;


import guesthouse.certificate.dto.CertificateDTO;

import java.util.List;

public record SubmittedCertificatesResponse(
        List<CertificateDTO> certificates
) {
}
