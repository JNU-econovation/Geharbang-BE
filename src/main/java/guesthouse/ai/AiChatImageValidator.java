package guesthouse.ai;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

final class AiChatImageValidator {

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final Set<String> HEIF_BRANDS = Set.of(
            "heic", "heix", "hevc", "hevx",
            "heim", "heis", "hevm", "hevs",
            "mif1", "msf1"
    );

    private AiChatImageValidator() {
    }

    static boolean matchesContentType(byte[] data, String contentType) {
        ImageType declaredType = declaredType(contentType);
        return declaredType != null && declaredType == detectType(data);
    }

    private static ImageType declaredType(String contentType) {
        if (contentType == null) return null;

        String normalized = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "image/jpeg", "image/jpg", "image/pjpeg" -> ImageType.JPEG;
            case "image/png" -> ImageType.PNG;
            case "image/webp" -> ImageType.WEBP;
            case "image/heic", "image/heif" -> ImageType.HEIF;
            default -> null;
        };
    }

    private static ImageType detectType(byte[] data) {
        if (data == null) return null;
        if (data.length >= 3
                && unsigned(data[0]) == 0xFF
                && unsigned(data[1]) == 0xD8
                && unsigned(data[2]) == 0xFF) {
            return ImageType.JPEG;
        }
        if (startsWith(data, PNG_SIGNATURE)) return ImageType.PNG;
        if (data.length >= 12
                && ascii(data, 0, 4).equals("RIFF")
                && ascii(data, 8, 4).equals("WEBP")) {
            return ImageType.WEBP;
        }
        if (data.length >= 12 && ascii(data, 4, 4).equals("ftyp")) {
            int inspectionLimit = Math.min(data.length, 64);
            if (HEIF_BRANDS.contains(ascii(data, 8, 4))) return ImageType.HEIF;
            for (int offset = 16; offset + 4 <= inspectionLimit; offset += 4) {
                if (HEIF_BRANDS.contains(ascii(data, offset, 4))) {
                    return ImageType.HEIF;
                }
            }
        }
        return null;
    }

    private static boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) return false;
        for (int index = 0; index < signature.length; index++) {
            if (data[index] != signature[index]) return false;
        }
        return true;
    }

    private static int unsigned(byte value) {
        return value & 0xFF;
    }

    private static String ascii(byte[] data, int offset, int length) {
        return new String(data, offset, length, StandardCharsets.US_ASCII);
    }

    private enum ImageType {
        JPEG, PNG, WEBP, HEIF
    }
}
