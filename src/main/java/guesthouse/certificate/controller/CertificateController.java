package guesthouse.certificate.controller;

import guesthouse.certificate.dto.CertificateDTO;
import guesthouse.certificate.dto.request.SubmitCertificateRequest;
import guesthouse.certificate.dto.response.SubmittedCertificatesResponse;
import guesthouse.certificate.service.CertificateService;
import guesthouse.common.annotation.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping("/api/v1/certificate/owner")
    public ResponseEntity<Void> submitCertificate(@Valid @RequestBody SubmitCertificateRequest request, @UserId Long userId) {
        certificateService.submitCertificate(request, userId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/api/v1/certificate/file-upload")
    public ResponseEntity<FileUploadedResponse> uploadFile(
            @RequestParam MultipartFile file,
            @RequestPart("fileType") String fileType,
            @RequestPart("fileName") String fileName,
            @UserId Long userId
    ) {
        String fileUrl = certificateService.uploadFile(file, fileType, fileName, userId);
        return ResponseEntity.ok().body(new FileUploadedResponse(fileUrl));
    }

    @GetMapping("/api/v1/certificate")
    public ResponseEntity<SubmittedCertificatesResponse> getCertificateList(
            @UserId Long userId
    ) {
        List<CertificateDTO> certificates= certificateService.getSubmittedCertificates(userId);
        return ResponseEntity.ok(new SubmittedCertificatesResponse(certificates));
    }
}
