package com.network.controller;

import com.network.model.JobAnnouncement;
import com.network.repository.NetworkRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller("/api/jobs")
@ExecuteOn(TaskExecutors.BLOCKING)
public class JobController {

    private final NetworkRepository repository;

    public JobController(NetworkRepository repository) {
        this.repository = repository;
    }

    /**
     * List job announcements with filters.
     */
    @Get
    public HttpResponse<List<JobAnnouncement>> listJobs(
        @QueryValue(value = "department", defaultValue = "ALL") String department,
        @QueryValue(value = "type", defaultValue = "ALL") String type,
        @QueryValue(value = "search", defaultValue = "") String search,
        @QueryValue(value = "tag", defaultValue = "") String tag,
        @QueryValue(value = "limit", defaultValue = "50") int limit,
        @QueryValue(value = "offset", defaultValue = "0") int offset
    ) {
        List<JobAnnouncement> jobs = repository.getJobAnnouncements(department, type, search, tag, limit, offset);
        return HttpResponse.ok(jobs);
    }

    /**
     * Get specific job announcement details.
     */
    @Get("/{id}")
    public HttpResponse<JobAnnouncement> getJob(@PathVariable UUID id) {
        Optional<JobAnnouncement> job = repository.getJobAnnouncementById(id);
        return job.map(HttpResponse::ok).orElseGet(HttpResponse::notFound);
    }

    /**
     * Post a new job announcement (persisted in DB and Object Storage).
     */
    @Post
    public HttpResponse<JobAnnouncement> createJob(@Body JobAnnouncement job) {
        JobAnnouncement saved = repository.saveJobAnnouncement(job);
        return HttpResponse.created(saved);
    }

    /**
     * Delete a job announcement.
     */
    @Delete("/{id}")
    public HttpResponse<?> deleteJob(@PathVariable UUID id) {
        boolean deleted = repository.deleteJobAnnouncement(id);
        if (deleted) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
