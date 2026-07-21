package guesthouse.review.analysis.repository;

import guesthouse.review.analysis.domain.ReviewAnalysisCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAnalysisCacheRepository extends JpaRepository<ReviewAnalysisCache, Long> {
}
