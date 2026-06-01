package guesthouse.chat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.chat.dto.response.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketSessionRegistry {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> activeRoomByUserId = new ConcurrentHashMap<>();

    public void add(Long userId, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUserId.remove(userId);
        }
    }

    public void addRoomPresence(Long userId, Long roomId) {
        activeRoomByUserId.put(userId, roomId);
    }

    public void removeRoomPresence(Long userId, Long roomId) {
        activeRoomByUserId.remove(userId, roomId);
    }

    public boolean isInRoom(Long userId, Long roomId) {
        return roomId.equals(activeRoomByUserId.get(userId));
    }

    public void broadcast(List<Long> receiverIds, ChatMessageDto message) {
        String payload = serialize(message);
        receiverIds.forEach(receiverId -> send(receiverId, payload));
    }

    private String serialize(ChatMessageDto message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize chat message", e);
        }
    }

    private void send(Long receiverId, String payload) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(receiverId);
        if (sessions == null) {
            return;
        }

        sessions.removeIf(session -> {
            if (!session.isOpen()) {
                return true;
            }
            try {
                session.sendMessage(new TextMessage(payload));
                return false;
            } catch (IOException e) {
                log.warn("Failed to send chat websocket message. receiverId={}, sessionId={}", receiverId, session.getId(), e);
                return true;
            }
        });
    }
}
