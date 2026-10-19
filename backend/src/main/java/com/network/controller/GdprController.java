package com.network.controller;

import com.network.model.GdprExport;
import com.network.repository.NetworkRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.Map;
import java.util.UUID;

@Controller("/api/gdpr")
@ExecuteOn(TaskExecutors.BLOCKING)
public class GdprController {

    private final NetworkRepository repository;

    public GdprController(NetworkRepository repository) {
        this.repository = repository;
    }

    @Get("/export/{userId}")
    public HttpResponse<GdprExport> exportData(@PathVariable UUID userId) {
        try {
            GdprExport export = repository.exportGdpr(userId);
            return HttpResponse.ok(export);
        } catch (Exception e) {
            return HttpResponse.notFound();
        }
    }

    @Delete("/purge/{userId}")
    public HttpResponse<Map<String, String>> purgeData(@PathVariable UUID userId) {
        repository.purgeUser(userId);
        return HttpResponse.ok(Map.of(
            "status", "purged",
            "message", "All user records, graph edges, posts, and comments permanently erased from database."
        ));
    }
}
