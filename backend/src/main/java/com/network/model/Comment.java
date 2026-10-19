package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.UUID;

@Serdeable
public record Comment(
    UUID id,
    UUID postId,
    UUID authorId,
    String authorHandle,
    String authorDisplayName,
    String authorAvatarUrl,
    String bodyMarkdown,
    Instant createdAt
) {}
