package guesthouse.application.service;

import guesthouse.application.exception.ImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private static final String IMAGE_URL_PREFIX = "/images/application/";
    private static final String UNDER_BAR = "_";

    @Value("${image.directory.path}")
    private String IMAGE_DIR_PATH;

    public String saveImage(MultipartFile image, Long userId) {
        String fileName = makeImageName(image, userId);
        String filePath = IMAGE_DIR_PATH + fileName;
        String dbFilePath = IMAGE_URL_PREFIX + fileName;

        try {
            saveFile(image, filePath);
            log.info("파일 저장 완료. fileName : {}", fileName);
        } catch (IOException e) {
            throw new ImageException();
        }

        return dbFilePath;
    }

    private String makeImageName(MultipartFile image, Long userId) {
        return System.currentTimeMillis() + UNDER_BAR + userId + UNDER_BAR + image.getOriginalFilename();
    }


    private void saveFile(MultipartFile image, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());
    }

    public List<String> saveImages(List<MultipartFile> images, Long userId) {
        return images.stream()
                .map(image -> saveImage(image, userId))
                .toList();
    }

}
