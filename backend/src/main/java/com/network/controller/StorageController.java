package com.network.controller;

import com.network.service.ObjectStorageService;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.objectstorage.ObjectStorageEntry;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller("/api/storage")
@ExecuteOn(TaskExecutors.BLOCKING)
public class StorageController {

    private final ObjectStorageService storageService;

    public StorageController(ObjectStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Upload an image, document, or markdown file to local object storage.
     */
    @Post(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA)
    public HttpResponse<Map<String, Object>> uploadFile(CompletedFileUpload file) {
        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType().map(MediaType::getName).orElse("application/octet-stream");
            String key = storageService.saveMedia(file.getFilename(), bytes, contentType);
            return HttpResponse.ok(Map.of(
                "key", key,
                "url", "/api/storage/objects/" + key,
                "filename", file.getFilename(),
                "size", bytes.length,
                "contentType", contentType
            ));
        } catch (IOException e) {
            return HttpResponse.serverError(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @Serdeable
    public record RawUploadRequest(String filename, String content, String contentType) {}

    /**
     * Upload raw text or markdown content into object storage.
     */
    @Post("/upload/raw")
    public HttpResponse<Map<String, Object>> uploadRaw(@Body RawUploadRequest request) {
        String filename = request.filename() != null ? request.filename() : "document.txt";
        String contentType = request.contentType() != null ? request.contentType() : "text/plain";
        byte[] bytes = request.content() != null ? request.content().getBytes(StandardCharsets.UTF_8) : new byte[0];
        String key = storageService.saveMedia(filename, bytes, contentType);
        return HttpResponse.ok(Map.of(
            "key", key,
            "url", "/api/storage/objects/" + key,
            "filename", filename,
            "size", bytes.length,
            "contentType", contentType
        ));
    }

    /**
     * Stream an object from object storage.
     */
    @Get("/objects/{path:.*}")
    public HttpResponse<?> getObject(@PathVariable String path) {
        Optional<ObjectStorageEntry<?>> opt = storageService.getObject(path);
        if (opt.isEmpty()) {
            return HttpResponse.notFound();
        }
        ObjectStorageEntry<?> entry = opt.get();
        String contentType = entry.getContentType().orElse("application/octet-stream");
        InputStream is = entry.getInputStream();
        StreamedFile streamedFile = new StreamedFile(is, MediaType.of(contentType));
        return HttpResponse.ok(streamedFile)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400");
    }

    /**
     * Inspect post content stored in object storage.
     */
    @Get("/posts/{postId}")
    public HttpResponse<Map<String, String>> getPostStorage(@PathVariable UUID postId) {
        Optional<String> content = storageService.getPostContent(postId);
        return content.map(body -> HttpResponse.ok(Map.of(
            "postId", postId.toString(),
            "key", "posts/" + postId + ".md",
            "bodyMarkdown", body
        ))).orElseGet(HttpResponse::notFound);
    }

    /**
     * Inspect job announcement markdown stored in object storage.
     */
    @Get("/jobs/{jobId}")
    public HttpResponse<Map<String, String>> getJobStorage(@PathVariable UUID jobId) {
        Optional<String> content = storageService.getJobContent(jobId);
        return content.map(body -> HttpResponse.ok(Map.of(
            "jobId", jobId.toString(),
            "key", "jobs/" + jobId + ".md",
            "bodyMarkdown", body
        ))).orElseGet(HttpResponse::notFound);
    }
}

