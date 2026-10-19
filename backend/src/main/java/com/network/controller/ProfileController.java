package com.network.controller;

import com.network.model.Profile;
import com.network.model.WorkExperience;
import com.network.model.Education;
import com.network.model.Highlight;
import com.network.repository.NetworkRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.*;

@Controller("/api/profiles")
@ExecuteOn(TaskExecutors.BLOCKING)
public class ProfileController {

    private final NetworkRepository repository;

    public ProfileController(NetworkRepository repository) {
        this.repository = repository;
    }

    @Get
    public List<Profile> listProfiles() {
        return repository.getProfiles();
    }

    @Get("/{handleOrId}")
    public HttpResponse<Profile> getProfile(@PathVariable String handleOrId) {
        return repository.getProfile(handleOrId)
                .map(HttpResponse::ok)
                .orElseGet(HttpResponse::notFound);
    }

    @Post
    public HttpResponse<Profile> createProfile(@Body Profile request) {
        Profile saved = repository.saveProfile(request);
        return HttpResponse.created(saved);
    }

    @Put("/{handleOrId}")
    public HttpResponse<?> updateProfile(@PathVariable String handleOrId, @Body Profile request) {
        Optional<Profile> existing = repository.getProfile(handleOrId);
        if (existing.isEmpty()) return HttpResponse.notFound();
        Profile current = existing.get();
        Profile toUpdate = new Profile(
            current.id(),
            request.handle() != null && !request.handle().isBlank() ? request.handle().replaceFirst("^@", "").trim() : current.handle(),
            request.displayName() != null ? request.displayName().trim() : current.displayName(),
            request.headline() != null ? request.headline().trim() : current.headline(),
            request.bioMarkdown() != null ? request.bioMarkdown().trim() : current.bioMarkdown(),
            request.avatarUrl() != null ? request.avatarUrl().trim() : current.avatarUrl(),
            request.bannerUrl() != null ? request.bannerUrl().trim() : current.bannerUrl(),
            request.skills() != null ? request.skills() : current.skills(),
            request.matrixHandle() != null ? request.matrixHandle().trim() : current.matrixHandle(),
            request.nostrNpub() != null ? request.nostrNpub().trim() : current.nostrNpub(),
            request.simplexAddress() != null ? request.simplexAddress().trim() : current.simplexAddress(),
            request.sessionId() != null ? request.sessionId().trim() : current.sessionId(),
            request.emailContact() != null ? request.emailContact().trim() : current.emailContact(),
            request.pgpFingerprint() != null ? request.pgpFingerprint().trim() : current.pgpFingerprint(),
            request.githubUsername() != null ? request.githubUsername().trim() : current.githubUsername(),
            request.githubContentRepo() != null ? request.githubContentRepo().trim() : current.githubContentRepo(),
            request.location() != null ? request.location().trim() : current.location(),
            current.createdAt()
        );
        try {
            Optional<Profile> result = repository.updateProfile(current.id(), toUpdate);
            return result.map(HttpResponse::ok).orElseGet(HttpResponse::notFound);
        } catch (Exception e) {
            return HttpResponse.badRequest(Map.of("error", e.getMessage()));
        }
    }

    @Get("/{handleOrId}/experience")
    public HttpResponse<List<WorkExperience>> getWorkExperiences(@PathVariable String handleOrId) {
        return repository.getProfile(handleOrId)
                .map(p -> HttpResponse.ok(repository.getWorkExperiences(p.id())))
                .orElseGet(HttpResponse::notFound);
    }

    @Post("/{profileId}/experience")
    public HttpResponse<WorkExperience> addWorkExperience(@PathVariable UUID profileId, @Body WorkExperience request) {
        Optional<Profile> p = repository.getProfileById(profileId);
        if (p.isEmpty()) return HttpResponse.notFound();
        WorkExperience toSave = new WorkExperience(
            UUID.randomUUID(),
            profileId,
            request.title(),
            request.companyName(),
            request.companyId(),
            request.location(),
            request.startDate(),
            request.endDate(),
            request.isCurrent(),
            request.descriptionMarkdown(),
            request.skills(),
            java.time.Instant.now()
        );
        WorkExperience saved = repository.saveWorkExperience(toSave);
        return HttpResponse.created(saved);
    }

    @Put("/{profileId}/experience/{id}")
    public HttpResponse<WorkExperience> updateWorkExperience(
            @PathVariable UUID profileId,
            @PathVariable UUID id,
            @Body WorkExperience request) {
        WorkExperience expToUpdate = new WorkExperience(
            id,
            profileId,
            request.title(),
            request.companyName(),
            request.companyId(),
            request.location(),
            request.startDate(),
            request.endDate(),
            request.isCurrent(),
            request.descriptionMarkdown(),
            request.skills(),
            request.createdAt()
        );
        Optional<WorkExperience> updated = repository.updateWorkExperience(id, expToUpdate);
        return updated.map(HttpResponse::ok).orElseGet(HttpResponse::notFound);
    }

    @Delete("/{profileId}/experience/{id}")
    public HttpResponse<Void> deleteWorkExperience(
            @PathVariable UUID profileId,
            @PathVariable UUID id) {
        boolean deleted = repository.deleteWorkExperience(id, profileId);
        return deleted ? HttpResponse.noContent() : HttpResponse.notFound();
    }

    @Get("/{handleOrId}/education")
    public HttpResponse<List<Education>> getEducations(@PathVariable String handleOrId) {
        return repository.getProfile(handleOrId)
                .map(p -> HttpResponse.ok(repository.getEducations(p.id())))
                .orElseGet(HttpResponse::notFound);
    }

    @Post("/{profileId}/education")
    public HttpResponse<Education> addEducation(@PathVariable UUID profileId, @Body Education request) {
        Optional<Profile> p = repository.getProfileById(profileId);
        if (p.isEmpty()) return HttpResponse.notFound();
        Education toSave = new Education(
            UUID.randomUUID(),
            profileId,
            request.institutionName(),
            request.degree(),
            request.fieldOfStudy(),
            request.startDate(),
            request.endDate(),
            request.isCurrent(),
            request.grade(),
            request.activitiesMarkdown(),
            request.skills(),
            java.time.Instant.now()
        );
        Education saved = repository.saveEducation(toSave);
        return HttpResponse.created(saved);
    }

    @Put("/{profileId}/education/{id}")
    public HttpResponse<Education> updateEducation(
            @PathVariable UUID profileId,
            @PathVariable UUID id,
            @Body Education request) {
        Education eduToUpdate = new Education(
            id,
            profileId,
            request.institutionName(),
            request.degree(),
            request.fieldOfStudy(),
            request.startDate(),
            request.endDate(),
            request.isCurrent(),
            request.grade(),
            request.activitiesMarkdown(),
            request.skills(),
            request.createdAt()
        );
        Optional<Education> updated = repository.updateEducation(id, eduToUpdate);
        return updated.map(HttpResponse::ok).orElseGet(HttpResponse::notFound);
    }

    @Delete("/{profileId}/education/{id}")
    public HttpResponse<Void> deleteEducation(
            @PathVariable UUID profileId,
            @PathVariable UUID id) {
        boolean deleted = repository.deleteEducation(id, profileId);
        return deleted ? HttpResponse.noContent() : HttpResponse.notFound();
    }

    // ----------------------------------------------------------------
    // Highlights Endpoints (Achievements, Patents, Prizes, Books, Conferences)
    // ----------------------------------------------------------------

    @Get("/{handleOrId}/highlights")
    public HttpResponse<List<Highlight>> getHighlights(@PathVariable String handleOrId) {
        return repository.getProfile(handleOrId)
                .map(p -> HttpResponse.ok(repository.getHighlights(p.id())))
                .orElseGet(HttpResponse::notFound);
    }

    @Post("/{profileId}/highlights")
    public HttpResponse<Highlight> addHighlight(@PathVariable UUID profileId, @Body Highlight request) {
        Optional<Profile> p = repository.getProfileById(profileId);
        if (p.isEmpty()) return HttpResponse.notFound();
        Highlight toSave = new Highlight(
            UUID.randomUUID(),
            profileId,
            request.type() != null ? request.type() : "ACHIEVEMENT",
            request.title(),
            request.issuerOrVenue(),
            request.dateOrYear(),
            request.descriptionMarkdown(),
            request.externalUrl(),
            java.time.Instant.now()
        );
        Highlight saved = repository.saveHighlight(toSave);
        return HttpResponse.created(saved);
    }

    @Put("/{profileId}/highlights/{id}")
    public HttpResponse<Highlight> updateHighlight(
            @PathVariable UUID profileId,
            @PathVariable UUID id,
            @Body Highlight request) {
        Highlight toUpdate = new Highlight(
            id,
            profileId,
            request.type() != null ? request.type() : "ACHIEVEMENT",
            request.title(),
            request.issuerOrVenue(),
            request.dateOrYear(),
            request.descriptionMarkdown(),
            request.externalUrl(),
            request.createdAt()
        );
        Optional<Highlight> updated = repository.updateHighlight(id, toUpdate);
        return updated.map(HttpResponse::ok).orElseGet(HttpResponse::notFound);
    }

    @Delete("/{profileId}/highlights/{id}")
    public HttpResponse<Void> deleteHighlight(
            @PathVariable UUID profileId,
            @PathVariable UUID id) {
        boolean deleted = repository.deleteHighlight(id, profileId);
        return deleted ? HttpResponse.noContent() : HttpResponse.notFound();
    }

    /**
     * Decentralized contact discovery endpoint.
     * Generates native protocol URIs (Matrix, Nostr, SimpleX, Session, PGP)
     * so that private messaging happens 100% outside the platform.
     */
    @Get("/{handleOrId}/contact")
    public HttpResponse<Map<String, Object>> getDecentralizedContact(@PathVariable String handleOrId) {
        Optional<Profile> opt = repository.getProfile(handleOrId);
        if (opt.isEmpty()) return HttpResponse.notFound();

        Profile p = opt.get();
        Map<String, Object> contactInfo = new LinkedHashMap<>();
        contactInfo.put("handle", p.handle());
        contactInfo.put("displayName", p.displayName());

        List<Map<String, String>> channels = new ArrayList<>();

        if (p.matrixHandle() != null && !p.matrixHandle().isBlank()) {
            channels.add(Map.of(
                "protocol", "Matrix",
                "identifier", p.matrixHandle(),
                "actionUrl", "https://matrix.to/#/" + p.matrixHandle(),
                "icon", "matrix",
                "badge", "Decentralized E2EE"
            ));
        }

        if (p.simplexAddress() != null && !p.simplexAddress().isBlank()) {
            channels.add(Map.of(
                "protocol", "SimpleX Chat",
                "identifier", p.simplexAddress(),
                "actionUrl", p.simplexAddress(),
                "icon", "simplex",
                "badge", "Zero User ID / Surveillance-Free"
            ));
        }

        if (p.nostrNpub() != null && !p.nostrNpub().isBlank()) {
            channels.add(Map.of(
                "protocol", "Nostr (NIP-04/17)",
                "identifier", p.nostrNpub(),
                "actionUrl", "nostr:" + p.nostrNpub(),
                "icon", "nostr",
                "badge", "Decentralized Relays"
            ));
        }

        if (p.sessionId() != null && !p.sessionId().isBlank()) {
            channels.add(Map.of(
                "protocol", "Session",
                "identifier", p.sessionId(),
                "actionUrl", "session://" + p.sessionId(),
                "icon", "session",
                "badge", "Onion Routed"
            ));
        }

        if (p.emailContact() != null && !p.emailContact().isBlank()) {
            channels.add(Map.of(
                "protocol", "Email / PGP",
                "identifier", p.emailContact(),
                "actionUrl", "mailto:" + p.emailContact(),
                "icon", "mail",
                "badge", p.pgpFingerprint() != null && !p.pgpFingerprint().isBlank() ? "PGP Verified" : "Direct"
            ));
        }

        contactInfo.put("channels", channels);
        contactInfo.put("privacyNotice", "Conversations happen end-to-end encrypted directly between parties. PeerMesh stores zero private messages, logs, or chat metadata.");

        return HttpResponse.ok(contactInfo);
    }
}
