package guesthouse.certificate.domain.model;

import guesthouse.certificate.domain.vo.CertificateType;
import guesthouse.certificate.domain.vo.Status;
import guesthouse.common.domain.TimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static guesthouse.certificate.service.CertificateService.URL_PREFIX;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String guestHouseName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CertificateType certificateType;

    @Builder
    public Certificate(
            String ownerName, String guestHouseName, String phoneNumber,
            String fileUrl, CertificateType certificateType,
            Long userId
    ) {
        this.ownerName = ownerName;
        this.guestHouseName = guestHouseName;
        this.phoneNumber = phoneNumber;
        this.fileUrl = fileUrl;
        this.certificateType = certificateType;
        this.status = Status.검토_대기;
        this.userId = userId;
    }

    public String getFileName() {
        return fileUrl.substring(URL_PREFIX.length() + 8, fileUrl.length());
    }
}
