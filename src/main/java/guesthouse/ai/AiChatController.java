package guesthouse.ai;

import guesthouse.ai.dto.AiChatRequest;
import guesthouse.ai.dto.AiChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiChatController {

    private final AiChatClient aiChatClient;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(aiChatClient.health());
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiChatClient.chat(request));
    }

    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<Void> reset(@PathVariable String sessionId) {
        aiChatClient.reset(sessionId);
        return ResponseEntity.noContent().build();
    }
}
