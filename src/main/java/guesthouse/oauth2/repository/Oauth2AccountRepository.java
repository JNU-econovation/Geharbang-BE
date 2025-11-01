package guesthouse.oauth2.repository;

import guesthouse.oauth2.domain.model.Oauth2Account;
import guesthouse.oauth2.domain.vo.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface Oauth2AccountRepository extends JpaRepository<Oauth2Account, Long> {

    @Query("select oa from Oauth2Account oa where oa.socialId = :socialId and oa.provider = :provider")
    Optional<Oauth2Account> findBySocialId(String socialId, Provider provider);

}
