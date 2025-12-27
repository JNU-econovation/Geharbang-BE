package guesthouse.application.repository;

import guesthouse.application.domain.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Boolean existsByUserId(Long userId);
}
