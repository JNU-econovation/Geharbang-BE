package guesthouse.application.repository;

import guesthouse.application.domain.model.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer,Long> {

    List<QuestionAnswer> findAllByApplicationRecordId(Long applicationRecordId);
}
