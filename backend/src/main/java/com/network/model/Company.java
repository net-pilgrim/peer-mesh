package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.UUID;

@Serdeable
public record Company(
    UUID id,
    String handle,
    String name,
    String tagline,
    String descriptionMarkdown,
    String websiteUrl,
    String logoUrl,
    String industry,
    String headquarters,
    Instant createdAt
) {
    public Company withId(UUID newId) {
        return new Company(newId, handle, name, tagline, descriptionMarkdown, websiteUrl, logoUrl, industry, headquarters, createdAt != null ? createdAt : Instant.now());
    }
}
