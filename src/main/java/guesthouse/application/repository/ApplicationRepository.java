package guesthouse.application.repository;

import guesthouse.application.domain.model.Application;
import guesthouse.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Boolean existsByUserId(Long userId);

    Optional<Application> findByUser(User user);
}
