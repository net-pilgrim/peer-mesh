package com.network.controller;

import com.network.model.Comment;
import com.network.model.Post;
import com.network.model.SearchResult;
import com.network.repository.NetworkRepository;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.*;

@Controller("/api")
@ExecuteOn(TaskExecutors.BLOCKING)
public class FeedController {

    private final NetworkRepository repository;

    public FeedController(NetworkRepository repository) {
        this.repository = repository;
    }

    @Get("/feed")
    public List<Post> getFeed(
            @QueryValue(defaultValue = "all") String type,
            @Nullable @QueryValue String tag,
            @Nullable @QueryValue Boolean hasTables,
            @Nullable @QueryValue Boolean hasImages,
            @Nullable @QueryValue Boolean hasLinks,
            @Nullable @QueryValue UUID viewerId,
            @QueryValue(defaultValue = "25") int limit,
            @QueryValue(defaultValue = "0") int offset) {
        return repository.getFeed(viewerId, type, tag, hasTables, hasImages, hasLinks, limit, offset);
    }

    @Get("/posts/{id}")
    public HttpResponse<Post> getPost(@PathVariable UUID id, @Nullable @QueryValue UUID viewerId) {
        return repository.getPost(id, viewerId)
                .map(HttpResponse::ok)
                .orElseGet(HttpResponse::notFound);
    }

    @io.micronaut.http.annotation.Post("/posts")
    public HttpResponse<Post> createPost(@Body Post request) {
        Post created = repository.createPost(request);
        return HttpResponse.created(created);
    }

    @io.micronaut.http.annotation.Post("/posts/{id}/reactions")
    public HttpResponse<Map<String, Object>> toggleReaction(@PathVariable UUID id, @Body Map<String, UUID> body) {
        UUID userId = body.get("userId");
        if (userId == null) {
            return HttpResponse.badRequest(Map.of("error", "userId is required"));
        }
        boolean liked = repository.toggleLike(id, userId);
        return HttpResponse.ok(Map.of("liked", liked));
    }

    @Get("/posts/{id}/comments")
    public List<Comment> getComments(@PathVariable UUID id) {
        return repository.getComments(id);
    }

    @io.micronaut.http.annotation.Post("/posts/{id}/comments")
    public HttpResponse<Comment> addComment(@PathVariable UUID id, @Body Map<String, String> body) {
        String authorIdStr = body.get("authorId");
        String markdown = body.get("bodyMarkdown");
        if (authorIdStr == null || markdown == null || markdown.isBlank()) {
            return HttpResponse.badRequest();
        }
        UUID authorId = UUID.fromString(authorIdStr);
        Comment comment = repository.addComment(id, authorId, markdown);
        return HttpResponse.created(comment);
    }

    @Get("/search")
    public List<Post> search(
            @Nullable @QueryValue String q,
            @Nullable @QueryValue Boolean hasTables,
            @Nullable @QueryValue Boolean hasImages,
            @Nullable @QueryValue Boolean hasLinks,
            @QueryValue(defaultValue = "25") int limit,
            @QueryValue(defaultValue = "0") int offset) {
        return repository.searchPosts(q, hasTables, hasImages, hasLinks, limit, offset);
    }

    @Get("/search/unified")
    public SearchResult searchUnified(@Nullable @QueryValue String q) {
        return repository.searchUnified(q);
    }
}
