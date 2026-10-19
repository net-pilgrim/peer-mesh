package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Serdeable
public record WorkExperience(
    UUID id,
    UUID profileId,
    String title,
    String companyName,
    UUID companyId,
    String location,
    String startDate,
    String endDate,
    boolean isCurrent,
    String descriptionMarkdown,
    List<String> skills,
    Instant createdAt
) {
    public WorkExperience(UUID id, UUID profileId, String title, String companyName, UUID companyId, String location, String startDate, String endDate, boolean isCurrent, String descriptionMarkdown, List<String> skills) {
        this(id, profileId, title, companyName, companyId, location, startDate, endDate, isCurrent, descriptionMarkdown, skills, Instant.now());
    }

    public WorkExperience withId(UUID newId) {
        return new WorkExperience(
            newId,
            profileId,
            title,
            companyName,
            companyId,
            location,
            startDate,
            endDate,
            isCurrent,
            descriptionMarkdown,
            skills != null ? skills : List.of(),
            createdAt != null ? createdAt : Instant.now()
        );
    }
}
