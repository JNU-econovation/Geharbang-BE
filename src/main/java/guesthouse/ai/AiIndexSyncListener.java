package guesthouse.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiIndexSyncListener {

    private final RestClient.Builder restClientBuilder;

    @Value("${geharbang-ai.base-url:http://localhost:8001}")
    private String aiBaseUrl;

    @Value("${ai.data-key:}")
    private String aiDataKey;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void synchronize(AiIndexSyncEvent event) {
        if (aiDataKey.isBlank()) {
            log.warn("AI index sync skipped: AI_DATA_KEY is not configured");
            return;
        }

        String uri = "/internal/indexes/%s/%d".formatted(event.domain().path(), event.entityId());
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                send(event, uri);
                log.info("AI index synchronized: domain={}, id={}, action={}, attempt={}",
                        event.domain(), event.entityId(), event.action(), attempt);
                return;
            } catch (RestClientException error) {
                if (attempt == 3) {
                    log.error("AI index synchronization failed: domain={}, id={}, action={}",
                            event.domain(), event.entityId(), event.action(), error);
                    return;
                }
                try {
                    Thread.sleep(attempt * 500L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    log.warn("AI index synchronization interrupted: domain={}, id={}",
                            event.domain(), event.entityId());
                    return;
                }
            }
        }
    }

    private void send(AiIndexSyncEvent event, String uri) {
        RestClient client = restClientBuilder.baseUrl(aiBaseUrl).build();
        if (event.action() == AiIndexSyncAction.UPSERT) {
            client.post().uri(uri).header("X-AI-Data-Key", aiDataKey)
                    .retrieve().toBodilessEntity();
        } else {
            client.delete().uri(uri).header("X-AI-Data-Key", aiDataKey)
                    .retrieve().toBodilessEntity();
        }
    }
}
