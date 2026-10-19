package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record SearchResult(
    String query,
    String detectedRole,
    String detectedLocation,
    List<Profile> people,
    List<Company> companies,
    List<Post> posts,
    int totalCount
) {}
