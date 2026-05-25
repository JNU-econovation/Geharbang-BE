package guesthouse.chat.repository;

import guesthouse.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByApplicationRecordId(Long applicationRecordId);

    Optional<ChatRoom> findByStaffRecruitmentIdAndApplicantId(Long staffRecruitmentId, Long applicantId);

    Optional<ChatRoom> findByGuestHousePostIdAndApplicantId(Long guestHousePostId, Long applicantId);

    List<ChatRoom> findByOwnerIdOrApplicantIdOrderByUpdatedAtDesc(Long ownerId, Long applicantId);
}
