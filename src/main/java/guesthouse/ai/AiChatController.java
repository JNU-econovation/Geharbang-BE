package guesthouse.ai;

import guesthouse.ai.dto.AiChatRequest;
import guesthouse.ai.dto.AiChatResponse;
import guesthouse.ai.dto.AiConversationDetailResponse;
import guesthouse.ai.dto.AiConversationSummary;
import guesthouse.common.annotation.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiChatController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/pjpeg",
            "image/png",
            "image/webp",
            "image/heic",
            "image/heif"
    );

    private final AiChatClient aiChatClient;
    private final AiChatService aiChatService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(aiChatClient.health());
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @UserId(required = false) Long userId,
            @Valid @RequestBody AiChatRequest request
    ) {
        return ResponseEntity.ok(aiChatService.chat(userId, request));
    }

    @PostMapping(value = "/chat/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AiChatResponse> chatWithImage(
            @UserId(required = false) Long userId,
            @RequestPart("message") String message,
            @RequestPart(value = "sessionId", required = false) String sessionId,
            @RequestPart("image") MultipartFile image
    ) {
        validateImageChatRequest(message, image);
        String normalizedSessionId = sessionId == null || sessionId.isBlank()
                ? null
                : sessionId;
        return ResponseEntity.ok(aiChatService.chatWithImage(
                userId,
                new AiChatRequest(message, normalizedSessionId),
                image
        ));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<AiConversationSummary>> getConversations(@UserId Long userId) {
        return ResponseEntity.ok(aiChatService.getConversations(userId));
    }

    @GetMapping("/conversations/{sessionId}")
    public ResponseEntity<AiConversationDetailResponse> getConversation(
            @UserId Long userId,
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(aiChatService.getConversation(userId, sessionId));
    }

    @DeleteMapping("/conversations/{sessionId}")
    public ResponseEntity<Void> deleteConversation(
            @UserId Long userId,
            @PathVariable String sessionId
    ) {
        aiChatService.deleteConversation(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<Void> reset(@PathVariable String sessionId) {
        aiChatClient.reset(sessionId);
        return ResponseEntity.noContent().build();
    }

    private void validateImageChatRequest(String message, MultipartFile image) {
        if (message == null || message.isBlank() || message.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "질문을 입력해 주세요.");
        }
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일이 필요합니다.");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지는 5MB 이하만 가능합니다.");
        }
        String contentType = image.getContentType() == null
                ? ""
                : image.getContentType().split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_IMAGE_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 이미지 형식입니다.");
        }
        try {
            if (!AiChatImageValidator.matchesContentType(image.getBytes(), contentType)) {
                throw new ResponseStatusException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "파일 내용과 이미지 형식이 일치하지 않습니다."
                );
            }
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 읽을 수 없습니다.", error);
        }
    }
}
