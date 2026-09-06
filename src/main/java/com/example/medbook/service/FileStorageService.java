package com.example.medbook.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx"
    );

    @Autowired
    private Cloudinary cloudinary;

    @Value("${app.upload.max-file-size-bytes:10485760}")
    private long maxFileSizeBytes;

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            byte[] content = validateFile(file);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(content, ObjectUtils.asMap("resource_type", "auto"));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải file lên Cloudinary: " + e.getMessage(), e);
        }
    }

    private byte[] validateFile(MultipartFile file) throws IOException {
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("Tệp tải lên không được vượt quá 10 MB.");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Định dạng tệp không được hỗ trợ.");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Phần mở rộng tệp không được hỗ trợ.");
        }

        byte[] content = file.getBytes();
        if (!hasExpectedSignature(contentType, content)) {
            throw new IllegalArgumentException("Nội dung tệp không khớp với định dạng đã khai báo.");
        }
        return content;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot < 0 ? "" : filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean hasExpectedSignature(String contentType, byte[] content) {
        return switch (contentType) {
            case "image/jpeg" -> startsWith(content, 0xFF, 0xD8, 0xFF);
            case "image/png" -> startsWith(content, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/gif" -> startsWith(content, "GIF87a") || startsWith(content, "GIF89a");
            case "image/webp" -> startsWith(content, "RIFF") && hasBytesAt(content, 8, "WEBP");
            case "application/pdf" -> startsWith(content, "%PDF-");
            case "application/msword" -> startsWith(content, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> startsWith(content, 0x50, 0x4B, 0x03, 0x04);
            default -> false;
        };
    }

    private boolean startsWith(byte[] content, int... signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if ((content[index] & 0xFF) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean startsWith(byte[] content, String signature) {
        return hasBytesAt(content, 0, signature);
    }

    private boolean hasBytesAt(byte[] content, int offset, String signature) {
        if (content.length < offset + signature.length()) {
            return false;
        }
        for (int index = 0; index < signature.length(); index++) {
            if (content[offset + index] != (byte) signature.charAt(index)) {
                return false;
            }
        }
        return true;
    }
}
