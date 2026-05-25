package guesthouse.chat.repository;

import guesthouse.chat.domain.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByIdDesc(Long chatRoomId, Pageable pageable);

    Long countByChatRoomIdAndSenderIdNotAndIsReadFalse(Long chatRoomId, Long senderId);

    @Modifying(clearAutomatically = true)
    @Query("update ChatMessage m set m.isRead = true where m.chatRoomId = :chatRoomId and m.senderId <> :userId and m.isRead = false")
    void markOpponentMessagesAsRead(Long chatRoomId, Long userId);
}
