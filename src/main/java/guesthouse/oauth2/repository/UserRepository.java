package guesthouse.oauth2.repository;

import guesthouse.oauth2.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
