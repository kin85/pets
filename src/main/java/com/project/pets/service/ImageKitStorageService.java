package com.project.pets.service;

import io.imagekit.client.ImageKitClient;
import io.imagekit.client.okhttp.ImageKitOkHttpClient;
import io.imagekit.models.files.FileDeleteParams;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ImageKitStorageService {

    private static final String DOGS_FOLDER = "/dogs";

    private final String publicKey;
    private final String urlEndpoint;
    private final ImageKitClient client;

    public ImageKitStorageService(
            @Value("${IMAGEKIT_PUBLIC_KEY:}") String publicKey,
            @Value("${IMAGEKIT_PRIVATE_KEY:}") String privateKey,
            @Value("${IMAGEKIT_URL_ENDPOINT:}") String urlEndpoint
    ) {
        this.publicKey = publicKey == null ? "" : publicKey.trim();
        this.urlEndpoint = normalizeUrlEndpoint(urlEndpoint);

        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) {
            this.client = null;
            return;
        }

        this.client = ImageKitOkHttpClient.builder()
                .privateKey(privateKey.trim())
                .build();
    }

    public StoredImage uploadDogPhoto(MultipartFile photo, String fileName) {
        ensureConfigured();

        try (InputStream inputStream = photo.getInputStream()) {
            FileUploadParams params = FileUploadParams.builder()
                    .file(inputStream)
                    .fileName(fileName)
                    .publicKey(publicKey)
                    .folder(DOGS_FOLDER)
                    .build();

            FileUploadResponse response = client.files().upload(params);
            String fileId = response.fileId().orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ImageKit upload missing fileId"));
            String filePath = response.filePath().orElse("");
            String url = response.url()
                    .filter(value -> !value.isBlank())
                    .orElseGet(() -> buildUrlFromEndpoint(filePath))
                    .trim();
            if (url.isBlank()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ImageKit upload missing url");
            }
            return new StoredImage(fileId, url);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read photo", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not upload photo", e);
        }
    }

    public void deleteByFileId(String fileId) {
        if (client == null || fileId == null || fileId.isBlank()) {
            return;
        }

        try {
            client.files().delete(FileDeleteParams.builder().fileId(fileId).build());
        } catch (Exception ignored) {
            // No bloqueamos la operacion principal si falla la limpieza remota.
        }
    }

    private void ensureConfigured() {
        if (client == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ImageKit is not configured"
            );
        }
    }

    private String normalizeUrlEndpoint(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "";
        }

        String trimmed = rawValue.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private String buildUrlFromEndpoint(String filePath) {
        if (urlEndpoint.isBlank() || filePath == null || filePath.isBlank()) {
            return "";
        }

        String normalizedPath = filePath.startsWith("/") ? filePath : "/" + filePath;
        return urlEndpoint + normalizedPath;
    }

    public record StoredImage(String fileId, String url) {
    }
}
