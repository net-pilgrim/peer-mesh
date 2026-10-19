package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Serdeable
public record Profile(
    UUID id,
    String handle,
    String displayName,
    String headline,
    String bioMarkdown,
    String avatarUrl,
    String bannerUrl,
    List<String> skills,
    // Decentralized Messaging Identifiers (zero platform chat storage)
    String matrixHandle,
    String nostrNpub,
    String simplexAddress,
    String sessionId,
    String emailContact,
    String pgpFingerprint,
    // GitHub Integration for Self-Hosted Content & Provenance
    String githubUsername,
    String githubContentRepo,
    // Location (City/Country only, privacy preserving, no GPS tracking)
    String location,
    Instant createdAt
) {
    public Profile(UUID id, String handle, String displayName, String headline, String bioMarkdown, String avatarUrl, String bannerUrl, List<String> skills, String matrixHandle, String nostrNpub, String simplexAddress, String sessionId, String emailContact, String pgpFingerprint, Instant createdAt) {
        this(id, handle, displayName, headline, bioMarkdown, avatarUrl, bannerUrl, skills, matrixHandle, nostrNpub, simplexAddress, sessionId, emailContact, pgpFingerprint, "", "", "", createdAt);
    }

    public Profile(UUID id, String handle, String displayName, String headline, String bioMarkdown, String avatarUrl, String bannerUrl, List<String> skills, String matrixHandle, String nostrNpub, String simplexAddress, String sessionId, String emailContact, String pgpFingerprint, String githubUsername, String githubContentRepo, Instant createdAt) {
        this(id, handle, displayName, headline, bioMarkdown, avatarUrl, bannerUrl, skills, matrixHandle, nostrNpub, simplexAddress, sessionId, emailContact, pgpFingerprint, githubUsername, githubContentRepo, "", createdAt);
    }

    public Profile withId(UUID newId) {
        return new Profile(newId, handle, displayName, headline, bioMarkdown, avatarUrl, bannerUrl, skills, matrixHandle, nostrNpub, simplexAddress, sessionId, emailContact, pgpFingerprint, githubUsername, githubContentRepo, location, createdAt != null ? createdAt : Instant.now());
    }
}
