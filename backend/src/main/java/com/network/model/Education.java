package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Serdeable
public record Education(
    UUID id,
    UUID profileId,
    String institutionName,
    String degree,
    String fieldOfStudy,
    String startDate,
    String endDate,
    boolean isCurrent,
    String grade,
    String activitiesMarkdown,
    List<String> skills,
    Instant createdAt
) {
    public Education {
        if (fieldOfStudy == null) fieldOfStudy = "";
        if (endDate == null) endDate = "";
        if (grade == null) grade = "";
        if (activitiesMarkdown == null) activitiesMarkdown = "";
        if (skills == null) skills = List.of();
        if (createdAt == null) createdAt = Instant.now();
    }
}
