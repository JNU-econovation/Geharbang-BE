package guesthouse.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.ai.domain.AiConversation;
import guesthouse.ai.domain.AiConversationMessage;
import guesthouse.ai.dto.AiChatGatewayRequest;
import guesthouse.ai.dto.AiChatGatewayResponse;
import guesthouse.ai.dto.AiChatRequest;
import guesthouse.ai.dto.AiChatResponse;
import guesthouse.ai.repository.AiConversationMessageRepository;
import guesthouse.ai.repository.AiConversationRepository;
import guesthouse.application.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private AiConversationRepository conversationRepository;

    @Mock
    private AiConversationMessageRepository messageRepository;

    @Mock
    private ImageService imageService;

    private AiChatService aiChatService;

    @BeforeEach
    void setUp() {
        aiChatService = new AiChatService(
                aiChatClient,
                conversationRepository,
                messageRepository,
                new ObjectMapper(),
                imageService
        );
    }

    @Test
    void firstAuthenticatedQuestionCreatesConversationAndTwoMessages() {
        Map<String, Object> context = Map.of("activeDomain", "guesthouse");
        when(aiChatClient.chat(any())).thenReturn(new AiChatGatewayResponse(
                "new-session",
                "추천 답변",
                "guesthouse",
                0.98,
                context
        ));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiChatResponse response = aiChatService.chat(
                7L,
                new AiChatRequest("애월 게스트하우스 추천해줘", null)
        );

        assertThat(response.sessionId()).isEqualTo("new-session");

        ArgumentCaptor<AiConversation> conversationCaptor = ArgumentCaptor.forClass(AiConversation.class);
        verify(conversationRepository).save(conversationCaptor.capture());
        assertThat(conversationCaptor.getValue().getUserId()).isEqualTo(7L);
        assertThat(conversationCaptor.getValue().getContextJson()).contains("guesthouse");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AiConversationMessage>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageRepository).saveAll(messagesCaptor.capture());
        assertThat(messagesCaptor.getValue()).extracting(AiConversationMessage::getRole)
                .containsExactly("user", "assistant");
    }

    @Test
    void savedConversationRestoresDurableContext() {
        AiConversation conversation = new AiConversation(
                7L,
                "saved-session",
                "이전 질문",
                "이전 답변",
                "{\"activeDomain\":\"staff_step\"}"
        );
        when(conversationRepository.findBySessionIdAndUserId("saved-session", 7L))
                .thenReturn(Optional.of(conversation));
        when(aiChatClient.chat(any())).thenReturn(new AiChatGatewayResponse(
                "saved-session",
                "후속 답변",
                "staff_step",
                0.9,
                Map.of("activeDomain", "staff_step")
        ));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        aiChatService.chat(7L, new AiChatRequest("근무 기간은?", "saved-session"));

        ArgumentCaptor<AiChatGatewayRequest> requestCaptor = ArgumentCaptor.forClass(AiChatGatewayRequest.class);
        verify(aiChatClient).chat(requestCaptor.capture());
        assertThat(requestCaptor.getValue().sessionId()).isEqualTo("saved-session");
        assertThat(requestCaptor.getValue().context()).containsEntry("activeDomain", "staff_step");
    }

    @Test
    void unknownSessionIsNotForwardedForAuthenticatedUser() {
        when(conversationRepository.findBySessionIdAndUserId("someone-elses-session", 7L))
                .thenReturn(Optional.empty());
        when(aiChatClient.chat(any())).thenReturn(new AiChatGatewayResponse(
                "safe-new-session",
                "새 답변",
                "guesthouse",
                0.8,
                Map.of()
        ));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        aiChatService.chat(7L, new AiChatRequest("질문", "someone-elses-session"));

        ArgumentCaptor<AiChatGatewayRequest> requestCaptor = ArgumentCaptor.forClass(AiChatGatewayRequest.class);
        verify(aiChatClient).chat(requestCaptor.capture());
        assertThat(requestCaptor.getValue().sessionId()).isNull();
        assertThat(requestCaptor.getValue().context()).isNull();
    }

    @Test
    void imageQuestionStoresImageUrlWithUserMessage() {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "jeju.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );
        when(imageService.saveImage(image, 7L)).thenReturn("/images/application/jeju.jpg");
        when(aiChatClient.chatWithImage(any(), any())).thenReturn(new AiChatGatewayResponse(
                "image-session",
                "이미지 답변",
                "guesthouse",
                0.9,
                Map.of("activeDomain", "guesthouse")
        ));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        aiChatService.chatWithImage(
                7L,
                new AiChatRequest("이런 분위기 게하 추천해줘", null),
                image
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AiConversationMessage>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageRepository).saveAll(messagesCaptor.capture());
        assertThat(messagesCaptor.getValue().getFirst().getImageUrl())
                .isEqualTo("/images/application/jeju.jpg");
    }
}
