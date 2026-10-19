package com.network;

import com.network.model.*;
import com.network.repository.NetworkRepository;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.UUID;

@MicronautTest
public class NetworkApiTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    NetworkRepository repository;

    @Test
    void testSeedDataAndFeed() {
        List<Profile> profiles = repository.getProfiles();
        Assertions.assertTrue(profiles.size() >= 4, "Should have seeded profiles");

        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        List<Post> feed = repository.getFeed(elenaId, "all", null, null, null, null, 10, 0);
        Assertions.assertFalse(feed.isEmpty(), "Feed should contain posts");

        // Verify unified post with table
        boolean foundTablePost = feed.stream().anyMatch(Post::hasTables);
        Assertions.assertTrue(foundTablePost, "Should find post containing markdown table");

        // Verify unified post with link
        boolean foundLinkPost = feed.stream().anyMatch(Post::hasLinks);
        Assertions.assertTrue(foundLinkPost, "Should find post containing unified link metadata");
    }

    @Test
    void testGraphDegreesAndPath() {
        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        UUID marcusId = UUID.fromString("a0000000-0000-0000-0000-000000000002");
        UUID kaiId = UUID.fromString("a0000000-0000-0000-0000-000000000004");

        // Elena is directly connected to Marcus
        List<ConnectionNode> direct = repository.get1stDegreeConnections(elenaId);
        Assertions.assertTrue(direct.stream().anyMatch(n -> n.id().equals(marcusId)), "Marcus should be 1st degree to Elena");

        // Marcus is connected to Kai, so Kai is 2nd degree to Elena
        List<ConnectionNode> suggestions = repository.get2ndDegreeConnections(elenaId, 10);
        Assertions.assertTrue(suggestions.stream().anyMatch(n -> n.id().equals(kaiId)), "Kai should be 2nd degree suggestion to Elena");

        // Shortest path Elena -> Kai should be 2 hops
        PathResult path = repository.getShortestPath(elenaId, kaiId);
        Assertions.assertEquals(2, path.distance(), "Shortest path from Elena to Kai should be 2 hops");
        Assertions.assertTrue(path.pathHandles().contains("elena_rostova"));
        Assertions.assertTrue(path.pathHandles().contains("marcus_poly"));
        Assertions.assertTrue(path.pathHandles().contains("dev_kai"));
    }

    @Test
    void testDecentralizedContactAndGdpr() {
        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        Profile elena = repository.getProfileById(elenaId).orElseThrow();
        Assertions.assertEquals("@elena:matrix.org", elena.matrixHandle());
        Assertions.assertFalse(elena.simplexAddress().isBlank());
        Assertions.assertFalse(elena.nostrNpub().isBlank());

        GdprExport export = repository.exportGdpr(elenaId);
        Assertions.assertNotNull(export.profile());
        Assertions.assertFalse(export.posts().isEmpty());
    }

    @Test
    void testGitHubSelfHostedProvenance() {
        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        List<Post> feed = repository.getFeed(elenaId, "all", null, null, null, null, 10, 0);
        Post gitPost = feed.stream().filter(Post::selfHosted).findFirst().orElse(null);
        Assertions.assertNotNull(gitPost, "Should find at least one Git self-hosted post");
        Assertions.assertEquals("marcus-thorne/database-internals", gitPost.githubRepo());
        Assertions.assertEquals("benchmarks/postgres-recursive-graphs.md", gitPost.githubPath());
        Assertions.assertEquals("7a9e32f", gitPost.githubCommitSha());
    }

    @Test
    void testSearchDoctorsInTimisoara() {
        SearchResult res = repository.searchUnified("doctors in timisoara");
        Assertions.assertEquals("doctor", res.detectedRole());
        Assertions.assertEquals("timisoara", res.detectedLocation());
        Assertions.assertFalse(res.people().isEmpty(), "Should find doctors in Timișoara");
        boolean foundAndrei = res.people().stream().anyMatch(p -> p.handle().equals("dr_andrei_muresan"));
        boolean foundSorina = res.people().stream().anyMatch(p -> p.handle().equals("dr_sorina_popa"));
        Assertions.assertTrue(foundAndrei, "Should find Dr. Andrei Mureșan");
        Assertions.assertTrue(foundSorina, "Should find Dr. Sorina Popa");
        Assertions.assertFalse(res.companies().isEmpty(), "Should find Timișoara hospital");
        Assertions.assertTrue(res.companies().stream().anyMatch(c -> c.handle().equals("spitalul_judetean_timisoara")));
        Assertions.assertFalse(res.posts().isEmpty(), "Should find post about emergency cardiology in Timișoara");
    }

    @Test
    void testSearchDoctorsInTimisoaraHttpEndpoint() {
        io.micronaut.http.HttpRequest<?> request = io.micronaut.http.HttpRequest.GET("/api/search/unified?q=doctors%20in%20timisoara");
        SearchResult res = client.toBlocking().retrieve(request, SearchResult.class);
        Assertions.assertNotNull(res);
        Assertions.assertEquals("doctor", res.detectedRole());
        Assertions.assertEquals("timisoara", res.detectedLocation());
        Assertions.assertFalse(res.people().isEmpty());
        Assertions.assertFalse(res.companies().isEmpty());
        Assertions.assertFalse(res.posts().isEmpty());
    }

    @Test
    void testWorkExperienceEndpointsAndGdpr() {
        // 1. Fetch experiences for Dr. Andrei
        UUID drAndreiId = UUID.fromString("a0000000-0000-0000-0000-000000000005");
        List<WorkExperience> exps = repository.getWorkExperiences(drAndreiId);
        Assertions.assertFalse(exps.isEmpty(), "Should have seeded work experiences for Dr. Andrei");
        WorkExperience chief = exps.stream().filter(WorkExperience::isCurrent).findFirst().orElse(null);
        Assertions.assertNotNull(chief);
        Assertions.assertEquals("Chief of Cardiology & Emergency Doctor", chief.title());
        Assertions.assertEquals("Timișoara County Emergency Clinical Hospital", chief.companyName());

        // 2. Add new experience for Elena
        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        WorkExperience newExp = new WorkExperience(
            null,
            elenaId,
            "Research Fellow in Zero-Knowledge",
            "Max Planck Institute",
            null,
            "Berlin, Germany",
            "2016-01",
            "2018-01",
            false,
            "Researching non-interactive zero-knowledge arguments of knowledge (zk-SNARKs).",
            List.of("Cryptography", "Mathematics", "Zero-Knowledge")
        );
        WorkExperience saved = repository.saveWorkExperience(newExp);
        Assertions.assertNotNull(saved.id());

        // Verify retrieval via HTTP endpoint
        io.micronaut.core.type.Argument<List<WorkExperience>> listType = io.micronaut.core.type.Argument.listOf(WorkExperience.class);
        List<WorkExperience> elenaExps = client.toBlocking().retrieve(
            io.micronaut.http.HttpRequest.GET("/api/profiles/elena_rostova/experience"),
            listType
        );
        Assertions.assertTrue(elenaExps.stream().anyMatch(e -> e.title().equals("Research Fellow in Zero-Knowledge")));

        // 3. Verify GDPR export includes work experiences
        GdprExport export = repository.exportGdpr(elenaId);
        Assertions.assertNotNull(export.workExperiences());
        Assertions.assertFalse(export.workExperiences().isEmpty());

        // Clean up added experience
        repository.deleteWorkExperience(saved.id(), elenaId);
    }

    @Inject
    com.network.service.ObjectStorageService storageService;

    @Test
    void testEducationEndpointsAndGdpr() {
        UUID drAndreiId = UUID.fromString("a0000000-0000-0000-0000-000000000005");

        // 1. Verify seeded education for Dr. Andrei
        List<Education> edus = repository.getEducations(drAndreiId);
        Assertions.assertFalse(edus.isEmpty(), "Should have seeded educations for Dr. Andrei");
        Education md = edus.stream().filter(e -> e.degree().contains("Doctor of Medicine")).findFirst().orElse(null);
        Assertions.assertNotNull(md);
        Assertions.assertEquals("Victor Babeș University of Medicine and Pharmacy Timișoara", md.institutionName());

        // 2. Add education for Elena via repository & verify HTTP endpoint
        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        Education newEdu = new Education(
            null,
            elenaId,
            "Stanford University",
            "Postdoctoral Researcher",
            "Applied Cryptography & Zero-Knowledge",
            "2017",
            "2018",
            false,
            "Honors",
            "Focus on cryptographic lattices and polynomial commitments.",
            List.of("Zero-Knowledge", "Cryptography", "Research"),
            null
        );
        Education saved = repository.saveEducation(newEdu);
        Assertions.assertNotNull(saved.id());

        io.micronaut.core.type.Argument<List<Education>> listType = io.micronaut.core.type.Argument.listOf(Education.class);
        List<Education> elenaEdus = client.toBlocking().retrieve(
            io.micronaut.http.HttpRequest.GET("/api/profiles/elena_rostova/education"),
            listType
        );
        Assertions.assertTrue(elenaEdus.stream().anyMatch(e -> e.institutionName().equals("Stanford University")));

        // 3. Verify GDPR export includes education
        GdprExport export = repository.exportGdpr(elenaId);
        Assertions.assertNotNull(export.educations());
        Assertions.assertFalse(export.educations().isEmpty());

        // Clean up
        repository.deleteEducation(saved.id(), elenaId);
    }

    @Test
    void testObjectStorageOperations() {
        // 1. Verify direct storage upload and retrieval
        String testContent = "# Distributed Architecture\nBenchmarking local object storage provider.";
        String key = storageService.saveMedia("architecture.md", testContent.getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/markdown");
        Assertions.assertTrue(storageService.exists(key), "Object should exist in object storage");

        java.util.Optional<String> retrieved = storageService.retrieveString(key);
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(testContent, retrieved.get());

        // 2. Verify post content is stored in object storage
        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        Post post = repository.createPost(new Post(
            null,
            elenaId,
            "PERSON",
            "elena_rostova",
            "Elena Rostova",
            "",
            "Testing Object Storage Posts",
            "Post body markdown persisted directly into local object storage.",
            null, null, null, null, null,
            List.of("ObjectStorage", "Micronaut"),
            false, false, false,
            0, 0, false, "YOU",
            null
        ));
        Assertions.assertNotNull(post.id());

        java.util.Optional<String> postStoredContent = storageService.getPostContent(post.id());
        Assertions.assertTrue(postStoredContent.isPresent(), "Post markdown must be stored in object storage");
        Assertions.assertEquals("Post body markdown persisted directly into local object storage.", postStoredContent.get());

        // 3. Verify HTTP Storage API
        java.util.Map<String, Object> rawReq = java.util.Map.of(
            "filename", "note.txt",
            "content", "Stored via StorageController into local provider.",
            "contentType", "text/plain"
        );
        java.util.Map<?, ?> res = client.toBlocking().retrieve(
            io.micronaut.http.HttpRequest.POST("/api/storage/upload/raw", rawReq),
            java.util.Map.class
        );
        Assertions.assertNotNull(res.get("url"));
        String storedUrl = res.get("url").toString();

        String fetchedDoc = client.toBlocking().retrieve(
            io.micronaut.http.HttpRequest.GET(storedUrl),
            String.class
        );
        Assertions.assertEquals("Stored via StorageController into local provider.", fetchedDoc);
    }
}
