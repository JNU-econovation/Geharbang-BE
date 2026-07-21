package guesthouse.review.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "review_analysis_cache")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewAnalysisCache {

    @Id
    @Column(name = "guest_house_post_id")
    private Long guestHousePostId;

    @Column(name = "source_fingerprint", nullable = false, length = 64)
    private String sourceFingerprint;

    @Lob
    @Column(name = "categories_json", nullable = false, columnDefinition = "LONGTEXT")
    private String categoriesJson;

    @Lob
    @Column(name = "keywords_json", nullable = false, columnDefinition = "LONGTEXT")
    private String keywordsJson;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String report;

    @Column(name = "review_count", nullable = false)
    private long reviewCount;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    public ReviewAnalysisCache(
            Long guestHousePostId,
            String sourceFingerprint,
            String categoriesJson,
            String keywordsJson,
            String report,
            long reviewCount
    ) {
        this.guestHousePostId = guestHousePostId;
        update(sourceFingerprint, categoriesJson, keywordsJson, report, reviewCount);
    }

    public void update(
            String sourceFingerprint,
            String categoriesJson,
            String keywordsJson,
            String report,
            long reviewCount
    ) {
        this.sourceFingerprint = sourceFingerprint;
        this.categoriesJson = categoriesJson;
        this.keywordsJson = keywordsJson;
        this.report = report;
        this.reviewCount = reviewCount;
        this.analyzedAt = LocalDateTime.now();
    }
}
