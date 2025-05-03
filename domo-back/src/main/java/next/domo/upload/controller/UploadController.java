package next.domo.upload.controller;

import lombok.RequiredArgsConstructor;
import next.domo.upload.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final S3Service s3Service;

    private static final List<String> allowedTypes = List.of("character", "item", "quest");

    @PostMapping("/{type}")
    public ResponseEntity<String> uploadFileByType(
            @PathVariable String type,
            @RequestPart MultipartFile file
    ) {
        if (!allowedTypes.contains(type)) {
            return ResponseEntity.badRequest().body("잘못된 업로드 타입입니다. (character, item, quest만 허용)");
        }

        String url = s3Service.uploadFile(file, type);
        return ResponseEntity.ok(url);
    }
}
