package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;

@Serdeable
public record GdprExport(
    Instant exportedAt,
    String legalBasis,
    Profile profile,
    List<Post> posts,
    List<GraphEdge> connectionsAndFollows,
    List<WorkExperience> workExperiences,
    List<Education> educations,
    List<Highlight> highlights
) {
    public GdprExport(Instant exportedAt, String legalBasis, Profile profile, List<Post> posts, List<GraphEdge> connectionsAndFollows) {
        this(exportedAt, legalBasis, profile, posts, connectionsAndFollows, List.of(), List.of(), List.of());
    }

    public GdprExport(Instant exportedAt, String legalBasis, Profile profile, List<Post> posts, List<GraphEdge> connectionsAndFollows, List<WorkExperience> workExperiences, List<Education> educations) {
        this(exportedAt, legalBasis, profile, posts, connectionsAndFollows, workExperiences, educations, List.of());
    }
}
