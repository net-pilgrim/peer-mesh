package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Serdeable
public record AuthSession(
    String sessionId,
    UUID profileId,
    String handle,
    String email,
    String displayName,
    String idToken,
    String accessToken,
    String authProvider,
    Instant createdAt,
    Instant expiresAt
) implements Serializable {}
