package com.network.controller;

import com.network.model.Post;
import com.network.model.Profile;
import com.network.repository.NetworkRepository;
import com.network.service.GitHubSyncService;
import com.network.service.GitHubSyncService.ImportedContent;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;

@Controller("/api/github")
@ExecuteOn(TaskExecutors.BLOCKING)
public class GitHubController {

    private final NetworkRepository repository;
    private final GitHubSyncService gitHubSyncService;

    public GitHubController(NetworkRepository repository, GitHubSyncService gitHubSyncService) {
        this.repository = repository;
        this.gitHubSyncService = gitHubSyncService;
    }

    @Serdeable
    public record ImportGitHubRequest(String url, UUID authorId) {}

    @Serdeable
    public record PreviewResponse(
            String title,
            String bodyMarkdown,
            String githubRepo,
            String githubPath,
            String githubCommitSha,
            String linkUrl,
            List<String> tags
    ) {}

    /**
     * Preview raw markdown content from a GitHub / Gist URL before publishing.
     */
    @Get("/preview")
    public HttpResponse<?> preview(@QueryValue String url) {
        if (url == null || url.isBlank()) {
            return HttpResponse.badRequest(Map.of("error", "URL parameter is required"));
        }
        try {
            ImportedContent imported = gitHubSyncService.fetchFromUrl(url);
            return HttpResponse.ok(new PreviewResponse(
                    imported.title(),
                    imported.bodyMarkdown(),
                    imported.githubRepo(),
                    imported.githubPath(),
                    imported.githubCommitSha(),
                    imported.linkUrl(),
                    imported.tags()
            ));
        } catch (Exception e) {
            return HttpResponse.badRequest(Map.of("error", "Failed to fetch from GitHub: " + e.getMessage()));
        }
    }

    /**
     * Import and publish self-hosted content from GitHub into PeerMesh.
     */
    @io.micronaut.http.annotation.Post("/import-url")
    public HttpResponse<?> importFromUrl(@Body ImportGitHubRequest req) {
        if (req.url() == null || req.url().isBlank()) {
            return HttpResponse.badRequest(Map.of("error", "URL is required"));
        }
        if (req.authorId() == null) {
            return HttpResponse.badRequest(Map.of("error", "authorId is required"));
        }

        Profile author = repository.getProfileById(req.authorId()).orElse(null);
        if (author == null) {
            return HttpResponse.badRequest(Map.of("error", "Author profile not found"));
        }

        try {
            ImportedContent imported = gitHubSyncService.fetchFromUrl(req.url());

            Post post = new Post(
                    UUID.randomUUID(),
                    author.id(),
                    "PERSON",
                    author.handle(),
                    author.displayName(),
                    author.avatarUrl(),
                    imported.title(),
                    imported.bodyMarkdown(),
                    imported.linkUrl(),
                    imported.linkTitle(),
                    "Self-hosted content syndicated from Git repository: " + (imported.githubRepo() != null ? imported.githubRepo() : "GitHub"),
                    null,
                    "github.com",
                    imported.tags(),
                    Post.checkHasTables(imported.bodyMarkdown()),
                    Post.checkHasImages(imported.bodyMarkdown()),
                    Post.checkHasLinks(imported.bodyMarkdown(), imported.linkUrl()),
                    0,
                    0,
                    false,
                    "SELF_HOSTED",
                    imported.githubRepo(),
                    imported.githubPath(),
                    imported.githubCommitSha(),
                    true,
                    Instant.now()
            );

            Post saved = repository.createPost(post);
            return HttpResponse.created(saved);
        } catch (Exception e) {
            return HttpResponse.badRequest(Map.of("error", "Failed to syndicate GitHub content: " + e.getMessage()));
        }
    }

    /**
     * Re-synchronize an existing Git-backed post with the latest repository version.
     */
    @io.micronaut.http.annotation.Post("/sync-post/{id}")
    public HttpResponse<?> syncPost(@PathVariable UUID id) {
        Optional<Post> opt = repository.getPost(id, null);
        if (opt.isEmpty()) {
            return HttpResponse.notFound();
        }

        Post existing = opt.get();
        if (!existing.selfHosted()) {
            return HttpResponse.badRequest(Map.of("error", "Post is not flagged as self-hosted"));
        }

        String fetchUrl;
        if (existing.linkUrl() != null && (existing.linkUrl().contains("github.com") || existing.linkUrl().contains("raw.githubusercontent.com"))) {
            fetchUrl = existing.linkUrl();
        } else if (existing.githubRepo() != null && existing.githubPath() != null) {
            fetchUrl = String.format("https://github.com/%s/blob/main/%s", existing.githubRepo(), existing.githubPath());
        } else {
            return HttpResponse.badRequest(Map.of("error", "Cannot determine GitHub URL for post synchronization"));
        }

        try {
            ImportedContent imported = gitHubSyncService.fetchFromUrl(fetchUrl);

            Post updated = new Post(
                    existing.id(),
                    existing.authorId(),
                    existing.authorType(),
                    existing.authorHandle(),
                    existing.authorDisplayName(),
                    existing.authorAvatarUrl(),
                    imported.title(),
                    imported.bodyMarkdown(),
                    imported.linkUrl(),
                    imported.linkTitle(),
                    existing.linkDescription(),
                    existing.linkImageUrl(),
                    existing.linkDomain(),
                    imported.tags(),
                    Post.checkHasTables(imported.bodyMarkdown()),
                    Post.checkHasImages(imported.bodyMarkdown()),
                    Post.checkHasLinks(imported.bodyMarkdown(), imported.linkUrl()),
                    existing.likeCount(),
                    existing.commentCount(),
                    existing.likedByMe(),
                    existing.feedReason(),
                    imported.githubRepo() != null ? imported.githubRepo() : existing.githubRepo(),
                    imported.githubPath() != null ? imported.githubPath() : existing.githubPath(),
                    imported.githubCommitSha() != null ? imported.githubCommitSha() : existing.githubCommitSha(),
                    true,
                    existing.createdAt()
            );

            Post saved = repository.updatePost(updated);
            return HttpResponse.ok(saved);
        } catch (Exception e) {
            return HttpResponse.badRequest(Map.of("error", "Re-sync failed: " + e.getMessage()));
        }
    }
}
