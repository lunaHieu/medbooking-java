package com.example.medbook.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageServiceTest {

    @Test
    void rejectsSvgUploads() {
        FileStorageService service = serviceWithTenMegabyteLimit();
        MockMultipartFile file = new MockMultipartFile(
                "file", "payload.svg", "image/svg+xml", "<svg></svg>".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.uploadFile(file));
    }

    @Test
    void rejectsFilesWhoseBytesDoNotMatchTheDeclaredType() {
        FileStorageService service = serviceWithTenMegabyteLimit();
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "not-a-png".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.uploadFile(file));
    }

    private FileStorageService serviceWithTenMegabyteLimit() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "maxFileSizeBytes", 10 * 1024 * 1024L);
        return service;
    }
}
