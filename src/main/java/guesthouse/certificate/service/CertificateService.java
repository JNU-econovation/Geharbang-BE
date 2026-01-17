package guesthouse.certificate.service;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.Status;
import guesthouse.certificate.dto.CertificateDTO;
import guesthouse.certificate.dto.request.SubmitCertificateRequest;
import guesthouse.certificate.dto.response.CertificateDetailsDTO;
import guesthouse.certificate.exception.CertificateErrorCode;
import guesthouse.certificate.exception.CertificateException;
import guesthouse.certificate.repository.CertificateRepository;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    public static final String URL_PREFIX = "/files/certifications/";
    private static final Set<String> ALLOWED_FILE_TYPES =
            Set.of("jpeg", "jpg", "pdf", "png");

    private final CertificateRepository certificateRepository;
    private final UserService userService;

    @Value("${certificate.directory.path}")
    private String DIR_PATH;

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

    public String uploadFile(MultipartFile file, String fileType, String fileName, Long userId) {
        if (!ALLOWED_FILE_TYPES.contains(fileType)) {
            throw new CertificateException(CertificateErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }

        String prefix = UUID.randomUUID().toString().substring(0, 8);
        String filePath = DIR_PATH + prefix + fileName;
        String dbFilePath = URL_PREFIX + prefix + fileName;
        try {
            saveFile(file, filePath);
            log.info("파일 저장 완료. fileName : {}, userId: {}", fileName, userId);
        } catch (IOException e) {
            throw new CertificateException(CertificateErrorCode.FILE_UPLOAD_FAILED, e);
        }
        return dbFilePath;
    }

    private void saveFile(MultipartFile image, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());
    }

    @Transactional
    public void decide(Long userId, Long certificateId, Boolean isApproved) {
        userService.validateAdmin(userId);

        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(()-> new CertificateException(CertificateErrorCode.NOT_FOUND));

        certificate.decide(isApproved);
    }
  
    @Transactional(readOnly = true)
    public List<CertificateDTO> getSubmittedCertificates(Long userId) {
        userService.validateAdmin(userId);

        List<Certificate> certificates = certificateRepository.findAll();

        return certificates.stream()
                .map(CertificateDTO::from)
                .toList();
    }
      
    public CertificateDetailsDTO getDetails(Long userId, Long certificateId) {
        userService.validateAdmin(userId);

        return certificateRepository.findById(certificateId)
                .map(CertificateDetailsDTO::from)
                .orElseThrow(()-> new CertificateException(CertificateErrorCode.NOT_FOUND));
    }
}
