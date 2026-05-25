package guesthouse.chat.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatPushThrottler {

    private static final Duration THROTTLE_DURATION = Duration.ofSeconds(30);

    private final ConcurrentHashMap<String, Instant> lastSentByRoomAndReceiver = new ConcurrentHashMap<>();

    public boolean shouldSend(Long chatRoomId, Long receiverId) {
        String key = chatRoomId + ":" + receiverId;
        Instant now = Instant.now();
        Instant last = lastSentByRoomAndReceiver.get(key);
        if (last != null && now.isBefore(last.plus(THROTTLE_DURATION))) {
            return false;
        }
        lastSentByRoomAndReceiver.put(key, now);
        return true;
    }
}
