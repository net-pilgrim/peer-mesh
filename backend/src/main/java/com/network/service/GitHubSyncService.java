package com.network.service;

import io.micronaut.context.annotation.Prototype;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Prototype
public class GitHubSyncService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Pattern GITHUB_BLOB_PATTERN = Pattern.compile(
            "https?://github\\.com/([^/]+)/([^/]+)/blob/([^/]+)/(.+)"
    );

    private static final Pattern GITHUB_RAW_PATTERN = Pattern.compile(
            "https?://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/([^/]+)/(.+)"
    );

    public record ImportedContent(
            String title,
            String bodyMarkdown,
            String githubRepo,
            String githubPath,
            String githubCommitSha,
            String linkUrl,
            String linkTitle,
            List<String> tags
    ) {}

    /**
     * Resolves and fetches raw markdown from a GitHub or Gist URL.
     */
    public ImportedContent fetchFromUrl(String url) throws IOException, InterruptedException {
        String cleanUrl = url.trim();
        String rawUrl = cleanUrl;
        String repo = null;
        String path = null;
        String commitOrBranch = null;

        Matcher blobMatcher = GITHUB_BLOB_PATTERN.matcher(cleanUrl);
        if (blobMatcher.matches()) {
            String owner = blobMatcher.group(1);
            String repoName = blobMatcher.group(2);
            commitOrBranch = blobMatcher.group(3);
            path = blobMatcher.group(4);
            repo = owner + "/" + repoName;
            rawUrl = String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", owner, repoName, commitOrBranch, path);
        } else {
            Matcher rawMatcher = GITHUB_RAW_PATTERN.matcher(cleanUrl);
            if (rawMatcher.matches()) {
                String owner = rawMatcher.group(1);
                String repoName = rawMatcher.group(2);
                commitOrBranch = rawMatcher.group(3);
                path = rawMatcher.group(4);
                repo = owner + "/" + repoName;
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(rawUrl))
                .header("User-Agent", "PeerMesh-GitSync/1.0 (Privacy-First Network)")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("GitHub returned HTTP " + response.statusCode() + " when fetching: " + rawUrl);
        }

        String rawContent = response.body();
        return parseMarkdownContent(rawContent, repo, path, commitOrBranch, cleanUrl);
    }

    private ImportedContent parseMarkdownContent(String rawContent, String repo, String path, String commitOrBranch, String originalUrl) {
        String title = null;
        String body = rawContent;
        String linkUrl = originalUrl;
        String linkTitle = "GitHub Source Repository";
        List<String> tags = new ArrayList<>();
        tags.add("GitHub");

        // Parse Frontmatter if present: --- ... ---
        if (rawContent.startsWith("---")) {
            int secondDelim = rawContent.indexOf("---", 3);
            if (secondDelim != -1) {
                String frontmatter = rawContent.substring(3, secondDelim).trim();
                body = rawContent.substring(secondDelim + 3).trim();

                for (String line : frontmatter.split("\n")) {
                    int colon = line.indexOf(':');
                    if (colon != -1) {
                        String key = line.substring(0, colon).trim().toLowerCase();
                        String val = line.substring(colon + 1).trim().replaceAll("^[\"']|[\"']$", "");

                        switch (key) {
                            case "title" -> title = val;
                            case "link" -> linkUrl = val;
                            case "linktitle" -> linkTitle = val;
                            case "tags" -> {
                                for (String t : val.split(",")) {
                                    if (!t.isBlank()) tags.add(t.trim());
                                }
                            }
                        }
                    }
                }
            }
        }

        // If title still null, look for first "# Heading"
        if (title == null || title.isBlank()) {
            for (String line : body.split("\n")) {
                if (line.startsWith("# ")) {
                    title = line.substring(2).trim();
                    break;
                }
            }
        }

        if (title == null || title.isBlank()) {
            if (path != null) {
                title = path.substring(path.lastIndexOf('/') + 1).replace(".md", "").replace('-', ' ');
            } else {
                title = "Self-Hosted Git Publication";
            }
        }

        return new ImportedContent(
                title,
                body,
                repo,
                path,
                commitOrBranch,
                linkUrl,
                linkTitle,
                tags
        );
    }
}
