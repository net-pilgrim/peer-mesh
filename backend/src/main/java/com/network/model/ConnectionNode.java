package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.UUID;

@Serdeable
public record ConnectionNode(
    UUID id,
    String handle,
    String displayName,
    String headline,
    String avatarUrl,
    int degree, // 1 for direct, 2 for mutual
    long mutualCount,
    // Decentralized contact endpoints
    String matrixHandle,
    String nostrNpub,
    String simplexAddress,
    String sessionId,
    String emailContact,
    Instant connectedSince
) {}
