package guesthouse.ai;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AiChatImageValidatorTest {

    @Test
    void acceptsSupportedImageSignaturesMatchingTheirContentTypes() {
        assertThat(AiChatImageValidator.matchesContentType(
                bytes(0xFF, 0xD8, 0xFF, 0xE0),
                "image/jpeg"
        )).isTrue();
        assertThat(AiChatImageValidator.matchesContentType(
                bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
                "image/png"
        )).isTrue();
        assertThat(AiChatImageValidator.matchesContentType(
                "RIFF\0\0\0\0WEBP".getBytes(StandardCharsets.US_ASCII),
                "image/webp"
        )).isTrue();
        assertThat(AiChatImageValidator.matchesContentType(
                bytes(0, 0, 0, 24, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c'),
                "image/heic"
        )).isTrue();
    }

    @Test
    void rejectsSpoofedOrMismatchedImageContentTypes() {
        assertThat(AiChatImageValidator.matchesContentType(
                "not-an-image".getBytes(StandardCharsets.US_ASCII),
                "image/jpeg"
        )).isFalse();
        assertThat(AiChatImageValidator.matchesContentType(
                bytes(0xFF, 0xD8, 0xFF, 0xE0),
                "image/png"
        )).isFalse();
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }
}
