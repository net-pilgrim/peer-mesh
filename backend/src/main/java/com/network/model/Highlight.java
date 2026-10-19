package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.UUID;

@Serdeable
public record Highlight(
    UUID id,
    UUID profileId,
    String type, // 'ACHIEVEMENT', 'PATENT', 'PRIZE', 'BOOK', 'CONFERENCE'
    String title,
    String issuerOrVenue,
    String dateOrYear,
    String descriptionMarkdown,
    String externalUrl,
    Instant createdAt
) {
    public Highlight {
        if (issuerOrVenue == null) issuerOrVenue = "";
        if (dateOrYear == null) dateOrYear = "";
        if (descriptionMarkdown == null) descriptionMarkdown = "";
        if (externalUrl == null) externalUrl = "";
        if (createdAt == null) createdAt = Instant.now();
    }
}
