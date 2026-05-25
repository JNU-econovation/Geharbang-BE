package guesthouse.chat.websocket;

import guesthouse.oauth2.service.TokenProcessor;
import guesthouse.user.exception.UserErrorCode;
import guesthouse.user.exception.UserException;
import guesthouse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String TOKEN_QUERY_PARAM = "token";
    private static final String ROOM_ID_QUERY_PARAM = "roomId";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_ATTRIBUTE = "userId";

    private final TokenProcessor tokenProcessor;
    private final UserRepository userRepository;
    private final ChatWebSocketSessionRegistry sessionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            Long userId = resolveUserId(session.getUri());
            session.getAttributes().put(USER_ID_ATTRIBUTE, userId);
            sessionRegistry.add(userId, session);

            Long roomId = resolveRoomId(session.getUri());
            if (roomId != null) {
                sessionRegistry.addRoomPresence(userId, roomId);
            }
        } catch (RuntimeException e) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Unauthorized"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userId = session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId instanceof Long id) {
            sessionRegistry.remove(id, session);
            sessionRegistry.removeRoomPresence(id);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Message sending is handled by REST. WebSocket is currently server-push only.
    }

    private Long resolveUserId(URI uri) {
        String token = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst(TOKEN_QUERY_PARAM);
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
        if (token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length());
        }

        Long userId = tokenProcessor.parseAccessToken(token);
        userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
        return userId;
    }

    private Long resolveRoomId(URI uri) {
        String roomIdStr = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst(ROOM_ID_QUERY_PARAM);
        if (roomIdStr == null) {
            return null;
        }
        try {
            return Long.parseLong(roomIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
