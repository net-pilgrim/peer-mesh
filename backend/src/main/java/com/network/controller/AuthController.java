package com.network.controller;

import com.network.model.AuthSession;
import com.network.model.Profile;
import com.network.repository.NetworkRepository;
import com.network.service.RedisSessionService;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Controller("/api/auth")
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final NetworkRepository repository;
    private final RedisSessionService redisSessionService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final String dexIssuer;
    private final String dexInternalUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public AuthController(
        NetworkRepository repository,
        RedisSessionService redisSessionService,
        ObjectMapper objectMapper,
        @Value("${dex.issuer:`http://localhost:5556/dex`}") String dexIssuer,
        @Value("${dex.internal-url:`http://localhost:5556/dex`}") String dexInternalUrl,
        @Value("${dex.client-id:peermesh-app}") String clientId,
        @Value("${dex.client-secret:peermesh-dex-secret}") String clientSecret,
        @Value("${dex.redirect-uri:`http://localhost:5173/callback`}") String redirectUri
    ) {
        this.repository = repository;
        this.redisSessionService = redisSessionService;
        this.objectMapper = objectMapper;
        this.dexIssuer = dexIssuer;
        this.dexInternalUrl = dexInternalUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    }

    @Serdeable
    public record ProvisionedUser(
        String email,
        String username,
        String name,
        String handle,
        String role,
        String avatarUrl,
        String defaultPassword
    ) {}

    @Serdeable
    public record DexConfigResponse(
        String dexIssuer,
        String clientId,
        String redirectUri,
        String scope,
        String responseType,
        String authorizationEndpoint,
        String tokenEndpoint,
        boolean redisConnected,
        List<ProvisionedUser> provisionedUsers
    ) {}

    @Get(value = "/config", produces = MediaType.APPLICATION_JSON)
    public DexConfigResponse getConfig() {
        List<ProvisionedUser> users = List.of(
            new ProvisionedUser(
                "elena@proton.me", "elena_rostova", "Elena Rostova", "elena_rostova",
                "Staff Distributed Systems Architect | Ex-Kernel Dev",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "password"
            ),
            new ProvisionedUser(
                "andrei.muresan@hosptim.ro", "dr_andrei_muresan", "Dr. Andrei Mureșan", "dr_andrei_muresan",
                "Chief of Cardiology & Emergency Doctor | Timișoara",
                "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=200", "password"
            ),
            new ProvisionedUser(
                "radu.ionescu@umft.ro", "dr_radu_ionescu", "Dr. Radu Ionescu", "dr_radu_ionescu",
                "Neurology & Acute Stroke Intervention Lead | Timișoara",
                "https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=200", "password"
            ),
            new ProvisionedUser(
                "sorina.popa@pediatrie-tm.ro", "dr_sorina_popa", "Dr. Sorina Popa", "dr_sorina_popa",
                "Consultant Pediatric Surgeon | Timișoara Children's Hospital",
                "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=200", "password"
            ),
            new ProvisionedUser(
                "marcus@pg-specialists.io", "marcus_poly", "Marcus Thorne", "marcus_poly",
                "Principal Database Engineer | Graph & Relational Specialist",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "password"
            ),
            new ProvisionedUser(
                "nora@zen-frontend.dev", "nora_zen", "Nora Lindqvist", "nora_zen",
                "Lead Frontend Engineer & Interaction Designer",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "password"
            ),
            new ProvisionedUser(
                "kai.infra@pm.me", "dev_kai", "Kai Zhang", "dev_kai",
                "High-Performance Backend & Cloud Infrastructure Engineer",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "password"
            )
        );

        return new DexConfigResponse(
            dexIssuer,
            clientId,
            redirectUri,
            "openid profile email",
            "code",
            dexIssuer + "/auth",
            dexIssuer + "/token",
            redisSessionService.isRedisAvailable(),
            users
        );
    }

    @Serdeable
    public record DexCallbackRequest(
        String code,
        String redirectUri,
        String idToken,
        String email,
        String username
    ) {}

    @Serdeable
    public record AuthResponse(
        boolean success,
        String sessionId,
        String email,
        Profile profile,
        String idToken,
        String storage,
        String message
    ) {}

    @Post(value = "/dex/callback", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<AuthResponse> handleDexCallback(@Body DexCallbackRequest req) {
        String resolvedEmail = req.email();
        String resolvedUsername = req.username();
        String idToken = req.idToken();

        // 1. If an authorization code was received, attempt token exchange with Dex OIDC
        if (req.code() != null && !req.code().isBlank()) {
            try {
                String tokenEndpoint = dexInternalUrl + "/token";
                String targetRedirectUri = req.redirectUri() != null && !req.redirectUri().isBlank()
                    ? req.redirectUri()
                    : this.redirectUri;

                String formBody = "grant_type=authorization_code" +
                    "&code=" + URLEncoder.encode(req.code(), StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(targetRedirectUri, StandardCharsets.UTF_8) +
                    "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

                HttpRequest tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create(tokenEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .timeout(Duration.ofSeconds(4))
                    .build();

                java.net.http.HttpResponse<String> tokenRes = httpClient.send(tokenReq, BodyHandlers.ofString());
                if (tokenRes.statusCode() == 200) {
                    Map<?, ?> tokenMap = objectMapper.readValue(tokenRes.body(), Map.class);
                    if (tokenMap.containsKey("id_token")) {
                        idToken = tokenMap.get("id_token").toString();
                        Map<String, Object> claims = parseJwtClaims(idToken);
                        if (claims.containsKey("email")) resolvedEmail = claims.get("email").toString();
                        if (claims.containsKey("name")) resolvedUsername = claims.get("name").toString();
                        if (claims.containsKey("preferred_username")) resolvedUsername = claims.get("preferred_username").toString();
                    }
                } else {
                    LOG.warn("Dex token exchange returned HTTP {}: {}", tokenRes.statusCode(), tokenRes.body());
                }
            } catch (Exception e) {
                LOG.warn("Could not exchange code with Dex directly (may be browser-side flow): {}", e.getMessage());
            }
        }

        // 2. Fall back to parsing idToken if passed directly from frontend
        if ((resolvedEmail == null || resolvedEmail.isBlank()) && idToken != null && !idToken.isBlank()) {
            Map<String, Object> claims = parseJwtClaims(idToken);
            if (claims.containsKey("email")) resolvedEmail = claims.get("email").toString();
            if (claims.containsKey("name")) resolvedUsername = claims.get("name").toString();
        }

        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            return HttpResponse.badRequest(new AuthResponse(
                false, null, null, null, null, null,
                "Unable to determine authenticated user email from Dex"
            ));
        }

        // 3. Match user to PeerMesh Profile
        Optional<Profile> matchedProfile = repository.getProfileByEmail(resolvedEmail);
        if (matchedProfile.isEmpty() && resolvedUsername != null) {
            matchedProfile = repository.getProfileByHandle(resolvedUsername);
        }
        if (matchedProfile.isEmpty()) {
            // Pick first persona if not explicitly mapped
            List<Profile> all = repository.getProfiles();
            if (!all.isEmpty()) matchedProfile = Optional.of(all.getFirst());
        }

        if (matchedProfile.isEmpty()) {
            return HttpResponse.notFound(new AuthResponse(
                false, null, resolvedEmail, null, null, null,
                "No profile found corresponding to Dex user: " + resolvedEmail
            ));
        }

        Profile profile = matchedProfile.get();
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(24));

        AuthSession session = new AuthSession(
            sessionId,
            profile.id(),
            profile.handle(),
            resolvedEmail,
            profile.displayName(),
            idToken,
            null,
            "dex",
            now,
            expiresAt
        );

        redisSessionService.saveSession(session, 86400);

        String storage = redisSessionService.isRedisAvailable() ? "redis" : "memory";
        return HttpResponse.ok(new AuthResponse(
            true,
            sessionId,
            resolvedEmail,
            profile,
            idToken,
            storage,
            "Successfully authenticated via Dex OIDC"
        ));
    }

    @Serdeable
    public record DirectLoginRequest(
        String email,
        String password
    ) {}

    @Post(value = "/dex/login", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<AuthResponse> directLogin(@Body DirectLoginRequest req) {
        if (req.email() == null || req.email().isBlank()) {
            return HttpResponse.badRequest(new AuthResponse(false, null, null, null, null, null, "Email is required"));
        }

        // Verify password - default "password" for all provisioned Dex demo users
        if (!"password".equals(req.password()) && !"peermesh123".equals(req.password())) {
            return HttpResponse.unauthorized().body(new AuthResponse(
                false, null, req.email(), null, null, null, "Invalid Dex credentials"
            ));
        }

        String lookup = req.email().trim();
        if (lookup.startsWith("@")) {
            lookup = lookup.substring(1).trim();
        }

        Optional<Profile> matchedProfile = repository.getProfileByEmail(lookup);
        if (matchedProfile.isEmpty()) {
            matchedProfile = repository.getProfileByHandle(lookup);
        }
        if (matchedProfile.isEmpty()) {
            try {
                UUID uid = UUID.fromString(lookup);
                matchedProfile = repository.getProfileById(uid);
            } catch (IllegalArgumentException ignored) {}
        }

        if (matchedProfile.isEmpty()) {
            return HttpResponse.notFound(new AuthResponse(
                false, null, req.email(), null, null, null, "Profile not found for " + req.email()
            ));
        }

        Profile profile = matchedProfile.get();
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(24));

        AuthSession session = new AuthSession(
            sessionId,
            profile.id(),
            profile.handle(),
            req.email(),
            profile.displayName(),
            "dex-direct-token-" + sessionId,
            null,
            "dex",
            now,
            expiresAt
        );

        redisSessionService.saveSession(session, 86400);
        String storage = redisSessionService.isRedisAvailable() ? "redis" : "memory";

        return HttpResponse.ok(new AuthResponse(
            true,
            sessionId,
            req.email(),
            profile,
            session.idToken(),
            storage,
            "Logged in as " + profile.displayName() + " via Dex credentials"
        ));
    }

    @Get(value = "/session", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> getSession(
        @Header(value = "Authorization", defaultValue = "") String authHeader,
        @QueryValue(value = "sessionId", defaultValue = "") String querySessionId
    ) {
        String sessionId = querySessionId;
        if ((sessionId == null || sessionId.isBlank()) && authHeader.startsWith("Bearer ")) {
            sessionId = authHeader.substring(7).trim();
        }

        if (sessionId == null || sessionId.isBlank()) {
            return HttpResponse.ok(Map.of("active", false));
        }

        Optional<AuthSession> sessionOpt = redisSessionService.getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            return HttpResponse.ok(Map.of("active", false));
        }

        AuthSession session = sessionOpt.get();
        Optional<Profile> profileOpt = repository.getProfileById(session.profileId());

        return HttpResponse.ok(Map.of(
            "active", true,
            "sessionId", session.sessionId(),
            "email", session.email(),
            "handle", session.handle(),
            "displayName", session.displayName(),
            "profile", profileOpt.orElse(null),
            "authProvider", session.authProvider(),
            "expiresAt", session.expiresAt().toString(),
            "redisConnected", redisSessionService.isRedisAvailable()
        ));
    }

    @Post(value = "/logout", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> logout(
        @Header(value = "Authorization", defaultValue = "") String authHeader,
        @Body(value = "sessionId") Optional<String> bodySessionId
    ) {
        String sessionId = bodySessionId.orElse("");
        if (sessionId.isBlank() && authHeader.startsWith("Bearer ")) {
            sessionId = authHeader.substring(7).trim();
        }

        if (!sessionId.isBlank()) {
            redisSessionService.deleteSession(sessionId);
        }

        return HttpResponse.ok(Map.of(
            "success", true,
            "message", "Session cleared from Redis"
        ));
    }

    private Map<String, Object> parseJwtClaims(String jwt) {
        if (jwt == null || !jwt.contains(".")) return Map.of();
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                return objectMapper.readValue(payload, Map.class);
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse JWT claims: {}", e.getMessage());
        }
        return Map.of();
    }
}
