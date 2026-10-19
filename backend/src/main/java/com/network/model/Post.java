package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Serdeable
public record Post(
    UUID id,
    UUID authorId,
    String authorType, // 'PERSON' or 'COMPANY'
    String authorHandle,
    String authorDisplayName,
    String authorAvatarUrl,
    String title,
    String bodyMarkdown,
    // Unified Link Card Metadata
    String linkUrl,
    String linkTitle,
    String linkDescription,
    String linkImageUrl,
    String linkDomain,
    List<String> tags,
    // Computed formatting flags
    boolean hasTables,
    boolean hasImages,
    boolean hasLinks,
    int likeCount,
    int commentCount,
    boolean likedByMe,
    String feedReason,
    // GitHub Self-Hosted Content & Provenance
    String githubRepo,       // e.g. "elena-rostova/protocols"
    String githubPath,       // e.g. "posts/zk-mesh-whitepaper.md"
    String githubCommitSha,  // e.g. "7a9f2bc..."
    boolean selfHosted,      // true if sourced from Git repository
    Instant createdAt
) {
    public Post(
        UUID id,
        UUID authorId,
        String authorType,
        String authorHandle,
        String authorDisplayName,
        String authorAvatarUrl,
        String title,
        String bodyMarkdown,
        String linkUrl,
        String linkTitle,
        String linkDescription,
        String linkImageUrl,
        String linkDomain,
        List<String> tags,
        boolean hasTables,
        boolean hasImages,
        boolean hasLinks,
        int likeCount,
        int commentCount,
        boolean likedByMe,
        String feedReason,
        Instant createdAt
    ) {
        this(id, authorId, authorType, authorHandle, authorDisplayName, authorAvatarUrl, title, bodyMarkdown, linkUrl, linkTitle, linkDescription, linkImageUrl, linkDomain, tags, hasTables, hasImages, hasLinks, likeCount, commentCount, likedByMe, feedReason, null, null, null, false, createdAt);
    }

    public static boolean checkHasTables(String text) {
        if (text == null) return false;
        return text.contains("|") && text.matches("(?s).*\\|[^\\n]+\\|\\s*\\n\\|[\\s:\\-|]+\\|.*");
    }

    public static boolean checkHasImages(String text) {
        if (text == null) return false;
        return text.matches("(?s).*\n?!\\[.*?\\]\\(.*?\\).*");
    }

    public static boolean checkHasLinks(String text, String linkUrl) {
        if (linkUrl != null && !linkUrl.isBlank()) return true;
        if (text == null) return false;
        return text.matches("(?s).*https?://.*");
    }
}
