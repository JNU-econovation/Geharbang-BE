package guesthouse.application.dto.response;

import java.util.List;

public record ImagesSaveResponse(
        List<String> imageUrl
) {
}
