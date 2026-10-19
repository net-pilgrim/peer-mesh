package com.network.controller;

import com.network.model.ConnectionNode;
import com.network.model.PathResult;
import com.network.repository.NetworkRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.*;

@Controller("/api/graph")
@ExecuteOn(TaskExecutors.BLOCKING)
public class GraphController {

    private final NetworkRepository repository;

    public GraphController(NetworkRepository repository) {
        this.repository = repository;
    }

    @Get("/connections/{userId}")
    public List<ConnectionNode> get1stDegree(@PathVariable UUID userId) {
        return repository.get1stDegreeConnections(userId);
    }

    @Get("/suggestions/{userId}")
    public List<ConnectionNode> get2ndDegree(
            @PathVariable UUID userId,
            @QueryValue(defaultValue = "20") int limit) {
        return repository.get2ndDegreeConnections(userId, limit);
    }

    @Get("/path")
    public PathResult getShortestPath(@QueryValue UUID from, @QueryValue UUID to) {
        return repository.getShortestPath(from, to);
    }

    @Post("/connect")
    public HttpResponse<Map<String, String>> connect(@Body Map<String, UUID> body) {
        UUID source = body.get("sourceId");
        UUID target = body.get("targetId");
        if (source == null || target == null) {
            return HttpResponse.badRequest(Map.of("error", "sourceId and targetId are required"));
        }
        repository.connect(source, target);
        return HttpResponse.ok(Map.of("status", "connected"));
    }

    @Post("/follow")
    public HttpResponse<Map<String, String>> follow(@Body Map<String, Object> body) {
        UUID source = UUID.fromString((String) body.get("sourceId"));
        UUID target = UUID.fromString((String) body.get("targetId"));
        String targetType = (String) body.getOrDefault("targetType", "PERSON");
        repository.follow(source, target, targetType);
        return HttpResponse.ok(Map.of("status", "followed"));
    }

    @Delete("/disconnect")
    public HttpResponse<Map<String, String>> disconnect(@Body Map<String, UUID> body) {
        UUID source = body.get("sourceId");
        UUID target = body.get("targetId");
        if (source != null && target != null) {
            repository.disconnect(source, target);
        }
        return HttpResponse.ok(Map.of("status", "disconnected"));
    }
}
