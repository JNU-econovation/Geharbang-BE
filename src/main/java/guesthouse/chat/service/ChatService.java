package guesthouse.chat.service;

import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.application_record.repository.ApplicationRecordRepository;
import guesthouse.chat.domain.model.ChatMessage;
import guesthouse.chat.domain.model.ChatRoom;
import guesthouse.chat.dto.request.ChatMessageSendRequest;
import guesthouse.chat.dto.request.ChatRoomCreateRequest;
import guesthouse.chat.dto.response.ChatMessageDto;
import guesthouse.chat.dto.response.ChatRoomDto;
import guesthouse.chat.exception.ChatErrorCode;
import guesthouse.chat.exception.ChatException;
import guesthouse.chat.repository.ChatMessageRepository;
import guesthouse.chat.repository.ChatRoomRepository;
import guesthouse.chat.websocket.ChatWebSocketSessionRegistry;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.exception.StaffRecruitmentErrorCode;
import guesthouse.staffrecruitment.exception.StaffRecruitmentException;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MESSAGE_PAGE_SIZE = 30;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final UserService userService;
    private final ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;

    @Transactional
    public Long createRoom(Long userId, ChatRoomCreateRequest request) {
        return chatRoomRepository.findByApplicationRecordId(request.applicationRecordId())
                .map(room -> {
                    validateParticipant(room, userId);
                    return room.getId();
                })
                .orElseGet(() -> createNewRoom(userId, request.applicationRecordId()).getId());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDto> getRooms(Long userId) {
        return chatRoomRepository.findByOwnerIdOrApplicantIdOrderByUpdatedAtDesc(userId, userId).stream()
                .map(room -> toRoomDto(room, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(Long userId, Long roomId, int pageNumber) {
        ChatRoom room = getRoomAndValidateParticipant(roomId, userId);
        Pageable pageable = PageRequest.of(pageNumber, MESSAGE_PAGE_SIZE);

        return chatMessageRepository.findByChatRoomIdOrderByIdDesc(room.getId(), pageable).stream()
                .map(ChatMessageDto::from)
                .toList();
    }

    @Transactional
    public ChatMessageDto sendMessage(Long userId, Long roomId, ChatMessageSendRequest request) {
        ChatRoom room = getRoomAndValidateParticipant(roomId, userId);
        validateContent(request.content());

        ChatMessage message = chatMessageRepository.save(new ChatMessage(room.getId(), userId, request.content()));
        room.updateLastMessage(request.content(), LocalDateTime.now());

        ChatMessageDto messageDto = ChatMessageDto.from(message);
        chatWebSocketSessionRegistry.broadcast(room.getParticipantIds(), messageDto);

        return messageDto;
    }

    @Transactional
    public void markAsRead(Long userId, Long roomId) {
        ChatRoom room = getRoomAndValidateParticipant(roomId, userId);
        chatMessageRepository.markOpponentMessagesAsRead(room.getId(), userId);
    }

    private ChatRoom createNewRoom(Long userId, Long applicationRecordId) {
        ApplicationRecord applicationRecord = applicationRecordRepository.findByIdOrThrow(applicationRecordId);
        StaffRecruitment staffRecruitment = staffRecruitmentRepository.findById(applicationRecord.getStaffRecruitmentId())
                .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND));

        ChatRoom room = new ChatRoom(
                applicationRecord.getId(),
                applicationRecord.getStaffRecruitmentId(),
                getStaffRecruitmentTitle(applicationRecord, staffRecruitment),
                staffRecruitment.getOwnerId(),
                applicationRecord.getUserId()
        );
        validateParticipant(room, userId);

        return chatRoomRepository.save(room);
    }

    private String getStaffRecruitmentTitle(ApplicationRecord applicationRecord, StaffRecruitment staffRecruitment) {
        if (applicationRecord.getStaffRecruitmentTitle() != null && !applicationRecord.getStaffRecruitmentTitle().isBlank()) {
            return applicationRecord.getStaffRecruitmentTitle();
        }
        return staffRecruitment.getTitle();
    }

    private ChatRoomDto toRoomDto(ChatRoom room, Long userId) {
        Long opponentId = room.getOpponentId(userId);
        User opponent = userService.findById(opponentId);
        Long unreadCount = chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), userId);

        return ChatRoomDto.of(
                room,
                userId,
                getUserName(opponent),
                opponent.getProfileImageUrl(),
                unreadCount
        );
    }

    private String getUserName(User user) {
        if (user.getPersonalInfo() == null) {
            return null;
        }
        return user.getPersonalInfo().getName();
    }

    private ChatRoom getRoomAndValidateParticipant(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));
        validateParticipant(room, userId);
        return room;
    }

    private void validateParticipant(ChatRoom room, Long userId) {
        if (!room.isParticipant(userId)) {
            throw new ChatException(ChatErrorCode.NOT_PARTICIPANT);
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ChatException(ChatErrorCode.MESSAGE_REQUIRED);
        }
    }
}
