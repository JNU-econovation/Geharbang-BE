package guesthouse.application.controller;

import guesthouse.application.dto.response.ApplicationImageSaveResponse;
import guesthouse.application.dto.response.ImagesSaveResponse;
import guesthouse.application.service.ImageService;
import guesthouse.common.annotation.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/api/v1/application/images")
    public ResponseEntity<ApplicationImageSaveResponse> saveApplicationImage(
            @RequestParam MultipartFile image,
            @UserId Long userId
    ) {
        String imageUrl = imageService.saveImage(image, userId);
        return ResponseEntity.ok(new ApplicationImageSaveResponse(imageUrl));
    }

    @PostMapping("/api/v1/images")
    public ResponseEntity<ImagesSaveResponse> saveApplicationImages(
            @RequestParam List<MultipartFile> images,
            @UserId Long userId
    ) {
        List<String> imageUrls = imageService.saveImages(images, userId);
        return ResponseEntity.ok(new ImagesSaveResponse(imageUrls));
    }
}
