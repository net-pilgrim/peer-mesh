package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.UUID;

@Serdeable
public record PathResult(
    int distance,
    List<UUID> pathNodeIds,
    List<String> pathHandles
) {}
