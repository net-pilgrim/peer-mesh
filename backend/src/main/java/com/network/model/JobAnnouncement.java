package com.network.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Serdeable
public record JobAnnouncement(
    UUID id,
    UUID companyId,
    String companyName,
    String companyLogoUrl,
    String title,
    String location,
    String employmentType, // 'FULL_TIME', 'REMOTE', 'CONTRACT', 'PART_TIME'
    String department,     // 'Engineering', 'Medicine & Clinical', 'Infrastructure', 'Design & UX', 'Security'
    String experienceLevel,// 'Senior', 'Staff / Lead', 'Specialist / Attending', 'Fellow / Mid'
    String salaryRange,    // e.g. '€120,000 - €150,000'
    String descriptionMarkdown,
    List<String> skills,
    String applyContact,   // Email, Matrix handle, or URL
    String storageKey,     // "jobs/{id}.md"
    boolean featured,
    Instant createdAt
) {
    public JobAnnouncement withId(UUID newId) {
        String key = storageKey != null && !storageKey.isBlank() ? storageKey : "jobs/" + newId + ".md";
        return new JobAnnouncement(
            newId,
            companyId,
            companyName,
            companyLogoUrl,
            title,
            location,
            employmentType != null ? employmentType : "FULL_TIME",
            department != null ? department : "Engineering",
            experienceLevel != null ? experienceLevel : "Senior",
            salaryRange != null ? salaryRange : "",
            descriptionMarkdown != null ? descriptionMarkdown : "",
            skills != null ? skills : List.of(),
            applyContact != null ? applyContact : "",
            key,
            featured,
            createdAt != null ? createdAt : Instant.now()
        );
    }
}
