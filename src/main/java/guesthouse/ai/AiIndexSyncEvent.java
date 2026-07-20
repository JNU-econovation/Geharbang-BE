package guesthouse.ai;

public record AiIndexSyncEvent(
        AiIndexDomain domain,
        Long entityId,
        AiIndexSyncAction action
) { }
