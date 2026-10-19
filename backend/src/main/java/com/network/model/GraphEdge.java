package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Serdeable
public record GraphEdge(
    UUID id,
    UUID sourceId,
    String sourceType, // 'PERSON' or 'COMPANY'
    UUID targetId,
    String targetType, // 'PERSON' or 'COMPANY'
    String edgeType,   // 'CONNECTED_TO', 'FOLLOWS', 'WORKS_AT'
    String status,     // 'PENDING', 'ACCEPTED', 'REJECTED'
    Map<String, Object> properties,
    double weight,
    Instant createdAt
) {}
