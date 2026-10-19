package com.network.service;

import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.ObjectStorageEntry;
import io.micronaut.objectstorage.request.UploadRequest;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service managing unstructured and document data (post markdown, media, GDPR archives)
 * using Micronaut Object Storage with local filesystem provider.
 */
@Singleton
public class ObjectStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectStorageService.class);

    private final ObjectStorageOperations<?, ?, ?> objectStorage;

    public ObjectStorageService(ObjectStorageOperations<?, ?, ?> objectStorage) {
        this.objectStorage = objectStorage;
    }

    /**
     * Store post markdown content directly in object storage under posts/{postId}.md.
     */
    public String savePostContent(UUID postId, String markdownBody) {
        String key = "posts/" + postId + ".md";
        byte[] bytes = markdownBody != null ? markdownBody.getBytes(StandardCharsets.UTF_8) : new byte[0];
        UploadRequest req = UploadRequest.fromBytes(bytes, key, "text/markdown; charset=UTF-8");
        objectStorage.upload(req);
        LOG.info("Persisted post content to object storage: key={}", key);
        return key;
    }

    /**
     * Retrieve post markdown body from object storage.
     */
    public Optional<String> getPostContent(UUID postId) {
        String key = "posts/" + postId + ".md";
        return retrieveString(key);
    }

    /**
     * Delete post markdown from object storage.
     */
    public void deletePostContent(UUID postId) {
        String key = "posts/" + postId + ".md";
        try {
            objectStorage.delete(key);
            LOG.info("Deleted post content from object storage: key={}", key);
        } catch (Exception e) {
            LOG.warn("Failed to delete post content: key={}, err={}", key, e.getMessage());
        }
    }

    /**
     * Store job announcement markdown content directly in object storage under jobs/{jobId}.md.
     */
    public String saveJobContent(UUID jobId, String markdownBody) {
        String key = "jobs/" + jobId + ".md";
        byte[] bytes = markdownBody != null ? markdownBody.getBytes(StandardCharsets.UTF_8) : new byte[0];
        UploadRequest req = UploadRequest.fromBytes(bytes, key, "text/markdown; charset=UTF-8");
        objectStorage.upload(req);
        LOG.info("Persisted job announcement markdown to object storage: key={}", key);
        return key;
    }

    /**
     * Retrieve job announcement markdown body from object storage.
     */
    public Optional<String> getJobContent(UUID jobId) {
        String key = "jobs/" + jobId + ".md";
        return retrieveString(key);
    }

    /**
     * Delete job announcement markdown from object storage.
     */
    public void deleteJobContent(UUID jobId) {
        String key = "jobs/" + jobId + ".md";
        try {
            objectStorage.delete(key);
            LOG.info("Deleted job announcement content from object storage: key={}", key);
        } catch (Exception e) {
            LOG.warn("Failed to delete job content: key={}, err={}", key, e.getMessage());
        }
    }

    /**
     * Store uploaded media or attachments in object storage.
     */
    public String saveMedia(String originalFilename, byte[] data, String contentType) {
        String safeName = (originalFilename != null && !originalFilename.isBlank())
                ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file.bin";
        String key = "media/" + UUID.randomUUID() + "_" + safeName;
        String mime = (contentType != null && !contentType.isBlank()) ? contentType : "application/octet-stream";
        UploadRequest req = UploadRequest.fromBytes(data, key, mime);
        objectStorage.upload(req);
        LOG.info("Stored media in object storage: key={}, size={} bytes", key, data.length);
        return key;
    }

    /**
     * Retrieve generic object storage entry.
     */
    public Optional<ObjectStorageEntry<?>> getObject(String key) {
        try {
            return objectStorage.retrieve(key);
        } catch (Exception e) {
            LOG.warn("Error retrieving object key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Read string content of an object.
     */
    public Optional<String> retrieveString(String key) {
        return getObject(key).flatMap(entry -> {
            try (InputStream is = entry.getInputStream()) {
                return Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                LOG.error("Failed reading object storage entry: key={}", key, e);
                return Optional.empty();
            }
        });
    }

    /**
     * Store GDPR Article 20 export archive in object storage.
     */
    public String saveGdprSnapshot(UUID userId, String jsonContent) {
        String key = "gdpr/" + userId + "-export.json";
        byte[] bytes = jsonContent.getBytes(StandardCharsets.UTF_8);
        UploadRequest req = UploadRequest.fromBytes(bytes, key, "application/json; charset=UTF-8");
        objectStorage.upload(req);
        LOG.info("Saved GDPR snapshot in object storage: key={}", key);
        return key;
    }

    /**
     * Erase user data from object storage during GDPR purge.
     */
    public void purgeUserData(UUID userId) {
        String gdprKey = "gdpr/" + userId + "-export.json";
        try {
            objectStorage.delete(gdprKey);
        } catch (Exception ignored) {}
    }

    public boolean exists(String key) {
        return objectStorage.exists(key);
    }

    public Set<String> listObjects() {
        return objectStorage.listObjects();
    }
}
