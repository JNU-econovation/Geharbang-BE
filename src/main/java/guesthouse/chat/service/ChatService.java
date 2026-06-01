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
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.exception.GuestHousePostErrorCode;
import guesthouse.guestHousePost.exception.GuestHousePostException;
import guesthouse.guestHousePost.repository.GuestHousePostRepository;
import guesthouse.notification.service.NotificationService;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final GuestHousePostRepository guestHousePostRepository;
    private final UserService userService;
    private final ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;
    private final NotificationService notificationService;

    @Transactional
    public Long createRoom(Long userId, ChatRoomCreateRequest request) {
        validateRoomTarget(request);
        if (request.applicationRecordId() != null) {
            return getOrCreateApplicationRecordRoom(userId, request.applicationRecordId());
        }
        if (request.staffRecruitmentId() != null) {
            return getOrCreateStaffRecruitmentRoom(userId, request.staffRecruitmentId());
        }
        return getOrCreateGuestHousePostRoom(userId, request.guestHousePostId());
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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                chatWebSocketSessionRegistry.broadcast(room.getParticipantIds(), messageDto);
            }
        });

        Long opponentId = room.getOpponentId(userId);
        boolean opponentInRoom = chatWebSocketSessionRegistry.isInRoom(opponentId, room.getId());
        if (!opponentInRoom) {
            notificationService.createChatMessageNotification(
                    opponentId,
                    room.getId(),
                    getUserName(userService.findById(userId)),
                    request.content()
            );
        }

        return messageDto;
    }

    @Transactional
    public void markAsRead(Long userId, Long roomId) {
        ChatRoom room = getRoomAndValidateParticipant(roomId, userId);
        chatMessageRepository.markOpponentMessagesAsRead(room.getId(), userId);
        notificationService.markChatRoomNotificationsAsRead(userId, room.getId());
    }

    private Long getOrCreateApplicationRecordRoom(Long userId, Long applicationRecordId) {
        return chatRoomRepository.findByApplicationRecordId(applicationRecordId)
                .map(room -> {
                    validateParticipant(room, userId);
                    return room.getId();
                })
                .orElseGet(() -> createNewApplicationRecordRoom(userId, applicationRecordId).getId());
    }

    private Long getOrCreateStaffRecruitmentRoom(Long userId, Long staffRecruitmentId) {
        return chatRoomRepository.findByStaffRecruitmentIdAndApplicantId(staffRecruitmentId, userId)
                .map(ChatRoom::getId)
                .orElseGet(() -> createNewStaffRecruitmentRoom(userId, staffRecruitmentId).getId());
    }

    private Long getOrCreateGuestHousePostRoom(Long userId, Long guestHousePostId) {
        return chatRoomRepository.findByGuestHousePostIdAndApplicantId(guestHousePostId, userId)
                .map(ChatRoom::getId)
                .orElseGet(() -> createNewGuestHousePostRoom(userId, guestHousePostId).getId());
    }

    private ChatRoom createNewApplicationRecordRoom(Long userId, Long applicationRecordId) {
        ApplicationRecord applicationRecord = applicationRecordRepository.findByIdOrThrow(applicationRecordId);
        StaffRecruitment staffRecruitment = staffRecruitmentRepository.findById(applicationRecord.getStaffRecruitmentId())
                .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND));
        validateApplicationRecordParticipant(staffRecruitment.getOwnerId(), applicationRecord.getUserId(), userId);

        ChatRoom room = new ChatRoom(
                applicationRecord.getId(),
                applicationRecord.getStaffRecruitmentId(),
                null,
                getStaffRecruitmentTitle(applicationRecord, staffRecruitment),
                staffRecruitment.getOwnerId(),
                applicationRecord.getUserId()
        );
        validateParticipant(room, userId);

        return chatRoomRepository.save(room);
    }

    private ChatRoom createNewStaffRecruitmentRoom(Long userId, Long staffRecruitmentId) {
        StaffRecruitment staffRecruitment = staffRecruitmentRepository.findById(staffRecruitmentId)
                .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND));
        validateUserExists(staffRecruitment.getOwnerId());
        validateNotSelfChat(staffRecruitment.getOwnerId(), userId);

        return chatRoomRepository.save(new ChatRoom(
                null,
                staffRecruitment.getId(),
                null,
                staffRecruitment.getTitle(),
                staffRecruitment.getOwnerId(),
                userId
        ));
    }

    private ChatRoom createNewGuestHousePostRoom(Long userId, Long guestHousePostId) {
        GuestHousePost guestHousePost = guestHousePostRepository.findById(guestHousePostId)
                .orElseThrow(() -> new GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND));
        validateUserExists(guestHousePost.getOwnerId());
        validateNotSelfChat(guestHousePost.getOwnerId(), userId);

        return chatRoomRepository.save(new ChatRoom(
                null,
                null,
                guestHousePost.getId(),
                guestHousePost.getGuestHouseName(),
                guestHousePost.getOwnerId(),
                userId
        ));
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

    private void validateRoomTarget(ChatRoomCreateRequest request) {
        int targetCount = 0;
        if (request.applicationRecordId() != null) targetCount++;
        if (request.staffRecruitmentId() != null) targetCount++;
        if (request.guestHousePostId() != null) targetCount++;
        if (targetCount != 1) {
            throw new ChatException(ChatErrorCode.INVALID_ROOM_TARGET);
        }
    }

    private void validateNotSelfChat(Long ownerId, Long userId) {
        if (ownerId.equals(userId)) {
            throw new ChatException(ChatErrorCode.SELF_CHAT_NOT_ALLOWED);
        }
    }

    private void validateApplicationRecordParticipant(Long ownerId, Long applicantId, Long userId) {
        if (!ownerId.equals(userId) && !applicantId.equals(userId)) {
            throw new ChatException(ChatErrorCode.NOT_PARTICIPANT);
        }
    }

    private void validateUserExists(Long userId) {
        userService.findById(userId);
    }
}
