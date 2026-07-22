package guesthouse.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.ai.domain.AiConversation;
import guesthouse.ai.domain.AiConversationMessage;
import guesthouse.ai.dto.AiChatGatewayRequest;
import guesthouse.ai.dto.AiChatGatewayResponse;
import guesthouse.ai.dto.AiChatRequest;
import guesthouse.ai.dto.AiChatResponse;
import guesthouse.ai.dto.AiConversationDetailResponse;
import guesthouse.ai.dto.AiConversationMessageResponse;
import guesthouse.ai.dto.AiConversationSummary;
import guesthouse.ai.repository.AiConversationMessageRepository;
import guesthouse.ai.repository.AiConversationRepository;
import guesthouse.application.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final int TITLE_MAX_LENGTH = 40;
    private static final int PREVIEW_MAX_LENGTH = 300;
    private static final TypeReference<Map<String, Object>> CONTEXT_TYPE = new TypeReference<>() {
    };

    private final AiChatClient aiChatClient;
    private final AiConversationRepository conversationRepository;
    private final AiConversationMessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final ImageService imageService;

    public AiChatResponse chat(Long userId, AiChatRequest request) {
        return executeChat(userId, request, null, aiChatClient::chat);
    }

    public AiChatResponse chatWithImage(
            Long userId,
            AiChatRequest request,
            MultipartFile image
    ) {
        if (userId == null) {
            return executeChat(
                    null,
                    request,
                    null,
                    gatewayRequest -> aiChatClient.chatWithImage(gatewayRequest, image)
            );
        }

        String imageUrl = imageService.saveImage(image, userId);
        return executeChat(
                userId,
                request,
                imageUrl,
                gatewayRequest -> aiChatClient.chatWithImage(gatewayRequest, image)
        );
    }

    private AiChatResponse executeChat(
            Long userId,
            AiChatRequest request,
            String imageUrl,
            GatewayCall gatewayCall
    ) {
        if (userId == null) {
            return gatewayCall.execute(new AiChatGatewayRequest(
                    request.message(),
                    request.sessionId(),
                    null
            )).toPublicResponse();
        }

        Optional<AiConversation> savedConversation = request.sessionId() == null
                ? Optional.empty()
                : conversationRepository.findBySessionIdAndUserId(request.sessionId(), userId);

        AiChatGatewayResponse aiResponse = gatewayCall.execute(new AiChatGatewayRequest(
                request.message(),
                savedConversation.map(AiConversation::getSessionId).orElse(null),
                savedConversation.map(this::readContext).orElse(null)
        ));

        AiConversation conversation = savedConversation.orElseGet(() -> new AiConversation(
                userId,
                aiResponse.sessionId(),
                truncate(normalizeTitle(request.message()), TITLE_MAX_LENGTH),
                truncate(aiResponse.answer(), PREVIEW_MAX_LENGTH),
                writeContext(aiResponse.context())
        ));

        conversation.updateAfterAnswer(
                truncate(aiResponse.answer(), PREVIEW_MAX_LENGTH),
                writeContext(aiResponse.context())
        );
        conversation = conversationRepository.save(conversation);

        messageRepository.saveAll(List.of(
                AiConversationMessage.user(
                        conversation.getId(),
                        request.message(),
                        imageUrl
                ),
                AiConversationMessage.assistant(
                        conversation.getId(),
                        aiResponse.answer(),
                        aiResponse.domain(),
                        aiResponse.confidence()
                )
        ));

        return aiResponse.toPublicResponse();
    }

    @Transactional(readOnly = true)
    public List<AiConversationSummary> getConversations(Long userId) {
        return conversationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(AiConversationSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AiConversationDetailResponse getConversation(Long userId, String sessionId) {
        AiConversation conversation = getOwnedConversation(userId, sessionId);
        List<AiConversationMessageResponse> messages = messageRepository
                .findAllByConversationIdOrderByIdAsc(conversation.getId())
                .stream()
                .map(AiConversationMessageResponse::from)
                .toList();
        return new AiConversationDetailResponse(
                conversation.getSessionId(),
                conversation.getTitle(),
                messages
        );
    }

    @Transactional
    public void deleteConversation(Long userId, String sessionId) {
        AiConversation conversation = getOwnedConversation(userId, sessionId);
        messageRepository.deleteAllByConversationId(conversation.getId());
        conversationRepository.delete(conversation);
    }

    private AiConversation getOwnedConversation(Long userId, String sessionId) {
        return conversationRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "AI 대화 기록을 찾을 수 없습니다."
                ));
    }

    private Map<String, Object> readContext(AiConversation conversation) {
        if (conversation.getContextJson() == null || conversation.getContextJson().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(conversation.getContextJson(), CONTEXT_TYPE);
        } catch (JsonProcessingException error) {
            return null;
        }
    }

    private String writeContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("AI 대화 문맥을 저장할 수 없습니다.", error);
        }
    }

    private String normalizeTitle(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String value, int maxCodePoints) {
        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount <= maxCodePoints) {
            return value;
        }
        int endIndex = value.offsetByCodePoints(0, maxCodePoints);
        return value.substring(0, endIndex);
    }

    @FunctionalInterface
    private interface GatewayCall {
        AiChatGatewayResponse execute(AiChatGatewayRequest request);
    }
}
