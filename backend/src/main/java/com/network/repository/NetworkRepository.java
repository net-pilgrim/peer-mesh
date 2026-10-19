package com.network.repository;

import com.network.model.*;
import io.micronaut.context.annotation.Context;
import io.micronaut.data.connection.ConnectionOperations;
import jakarta.annotation.PostConstruct;
import java.sql.*;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;

@Context
public class NetworkRepository {

    private final ConnectionOperations<Connection> connectionOps;
    private final com.network.service.ObjectStorageService objectStorageService;

    public NetworkRepository(ConnectionOperations<Connection> connectionOps, com.network.service.ObjectStorageService objectStorageService) {
        this.connectionOps = connectionOps;
        this.objectStorageService = objectStorageService;
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    private void runWrite(SqlConsumer consumer) {
        connectionOps.executeWrite(status -> {
            try {
                Connection conn = status.getConnection();
                consumer.accept(conn);
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("DB Write error: " + e.getMessage(), e);
            }
        });
    }

    private <T> T runRead(SqlFunction<T> function) {
        return connectionOps.executeRead(status -> {
            try {
                return function.apply(status.getConnection());
            } catch (SQLException e) {
                throw new RuntimeException("DB Read error: " + e.getMessage(), e);
            }
        });
    }

    @PostConstruct
    public void init() {
        // Flyway handles database schema and seed migrations on startup.
        // Synchronize all post markdown bodies into Object Storage.
        try {
            List<Post> posts = getFeed(null, "all", null, null, null, null, 1000, 0);
            for (Post p : posts) {
                if (p.bodyMarkdown() != null && !p.bodyMarkdown().isBlank()) {
                    objectStorageService.savePostContent(p.id(), p.bodyMarkdown());
                }
            }
        } catch (Exception ignored) {}

        // Synchronize all job announcement markdown bodies into Object Storage.
        try {
            List<JobAnnouncement> jobs = getJobAnnouncements(null, null, null, null, 1000, 0);
            for (JobAnnouncement j : jobs) {
                if (j.descriptionMarkdown() != null && !j.descriptionMarkdown().isBlank()) {
                    objectStorageService.saveJobContent(j.id(), j.descriptionMarkdown());
                }
            }
        } catch (Exception ignored) {}
    }

    private void seedData(Connection conn) throws SQLException {
        // Seed Elena
        UUID elenaId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        saveProfileDirect(conn, new Profile(
            elenaId,
            "elena_rostova",
            "Elena Rostova",
            "Staff Distributed Systems Architect | Ex-Kernel Dev",
            "Building peer-to-peer zero-knowledge communication and sovereign networks. Decentralized privacy advocate.",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80",
            List.of("Rust", "PostgreSQL", "Distributed Systems", "Cryptography", "Svelte"),
            "@elena:matrix.org",
            "npub1elena89xk23nd09f8asdnv0934jnkdf908234kjnds89",
            "simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Felena-relay.net",
            "05e89a421b5821c990bfa2384910248239081249",
            "elena@proton.me",
            "4A8F B832 9901 DC34 2200  E120 8943 F980 BB32 4410",
            "elenarostova",
            "elenarostova/zk-protocols",
            "Berlin, Germany",
            Instant.now()
        ));

        // Seed Marcus
        UUID marcusId = UUID.fromString("a0000000-0000-0000-0000-000000000002");
        saveProfileDirect(conn, new Profile(
            marcusId,
            "marcus_poly",
            "Marcus Thorne",
            "Principal Database Engineer | Graph & Relational Specialist",
            "Query planner and PostgreSQL internals specialist. Speaker at PGConf. Exploring SQL:2023 PGQ.",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
            "",
            List.of("PostgreSQL 17", "SQL:2023", "Query Optimization", "C", "Micronaut"),
            "@marcus:matrix.org",
            "npub1marcus789234jhskdf89234njkdf90234hjskdf902",
            "simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Fmarcus-chat.net",
            "",
            "marcus@pg-specialists.io",
            "",
            "marcus_poly",
            "marcus-thorne/database-internals",
            "London, UK",
            Instant.now()
        ));

        // Seed Nora
        UUID noraId = UUID.fromString("a0000000-0000-0000-0000-000000000003");
        saveProfileDirect(conn, new Profile(
            noraId,
            "nora_zen",
            "Nora Lindqvist",
            "Lead Frontend Engineer & Interaction Designer",
            "Advocate for lightweight, zero-framework vanilla CSS and reactive frontends with Svelte. Hater of bloated 3-column UI feeds.",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
            "",
            List.of("Svelte 5", "Vanilla CSS", "PWA", "Web Performance", "Accessibility"),
            "@nora:matrix.org",
            "",
            "simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Fnora-simplex.org",
            "05bb239012480923849012849012849012849012",
            "",
            "",
            "",
            "",
            "Stockholm, Sweden",
            Instant.now()
        ));

        // Seed Kai
        UUID kaiId = UUID.fromString("a0000000-0000-0000-0000-000000000004");
        saveProfileDirect(conn, new Profile(
            kaiId,
            "dev_kai",
            "Kai Zhang",
            "High-Performance Backend & Cloud Infrastructure Engineer",
            "Building microservices that sip RAM and handle millions of RPS. Micronaut, Rust, and Linux tuning.",
            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
            "",
            List.of("Micronaut", "Java 25", "GraalVM Native", "Docker", "Linux Kernel"),
            "@kai:matrix.org",
            "npub1kaizhang098234jksdf908234njksdf980234jksdf",
            "",
            "",
            "kai.infra@pm.me",
            "",
            "",
            "",
            "Singapore",
            Instant.now()
        ));

        // Seed Doctors in Timișoara
        UUID drAndreiId = UUID.fromString("a0000000-0000-0000-0000-000000000005");
        saveProfileDirect(conn, new Profile(
            drAndreiId,
            "dr_andrei_muresan",
            "Dr. Andrei Mureșan",
            "Chief of Cardiology & Emergency Doctor | Timișoara County Emergency Clinical Hospital",
            "Consultant Cardiologist and Emergency Medicine Specialist in Timișoara with 15+ years of clinical experience. Focused on acute coronary syndrome interventions and modernizing medical data networks.",
            "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=200&auto=format&fit=crop&q=80",
            "",
            List.of("Cardiology", "Emergency Medicine", "Echocardiography", "Internal Medicine", "Clinical Triage"),
            "@andrei.muresan:matrix.org",
            "",
            "simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Fdr-muresan.org",
            "05ee892348092384902384901284901284901284",
            "andrei.muresan@hosptim.ro",
            "",
            "",
            "",
            "Timișoara, Romania",
            Instant.now()
        ));

        UUID drSorinaId = UUID.fromString("a0000000-0000-0000-0000-000000000006");
        saveProfileDirect(conn, new Profile(
            drSorinaId,
            "dr_sorina_popa",
            "Dr. Sorina Popa",
            "Pediatric Surgeon & Medical Doctor | Timișoara Children's Hospital",
            "Pediatric surgeon specializing in minimally invasive and robotic laparoscopy. Passionate about evidence-based pediatric surgery and open clinical research across western Romania.",
            "https://images.unsplash.com/photo-1594824813583-16a7f34beee3?w=200&auto=format&fit=crop&q=80",
            "",
            List.of("Pediatric Surgery", "Robotic Laparoscopy", "Pediatrics", "Neonatal Surgery", "Clinical Research"),
            "@sorina.popa:matrix.org",
            "npub1sorinapopa98234jksdf908234njksdf980234",
            "",
            "",
            "sorina.popa@pediatrie-tm.ro",
            "",
            "",
            "",
            "Timișoara, Romania",
            Instant.now()
        ));

        UUID drRaduId = UUID.fromString("a0000000-0000-0000-0000-000000000007");
        saveProfileDirect(conn, new Profile(
            drRaduId,
            "dr_radu_ionescu",
            "Dr. Radu Ionescu",
            "Neurologist & Neuro-Critical Care Doctor | Victor Babeș University Timișoara",
            "Associate Professor of Neurology and attending neuro-intensivist in Timișoara. Specializing in acute ischemic stroke treatment and neuro-rehabilitation protocols.",
            "https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=200&auto=format&fit=crop&q=80",
            "",
            List.of("Neurology", "Neuroscience", "Critical Care", "Stroke Intervention", "EEG Diagnostics"),
            "@radu.ionescu:matrix.org",
            "",
            "",
            "05ab239840192384091283049182034918230948",
            "radu.ionescu@umft.ro",
            "",
            "",
            "",
            "Timișoara, Romania",
            Instant.now()
        ));

        // Seed Companies
        UUID sovereignMeshId = UUID.fromString("b0000000-0000-0000-0000-000000000001");
        saveCompanyDirect(conn, new Company(
            sovereignMeshId,
            "sovereign-mesh",
            "Sovereign Mesh Technologies",
            "Decentralized identity, zero-knowledge verification, and private networking protocols.",
            "We build open-source tools that empower individuals and organizations to collaborate without surveillance capitalism.",
            "https://sovereign-mesh.org",
            "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=200&auto=format&fit=crop&q=80",
            "Cybersecurity & Decentralized Protocols",
            "Stockholm, Sweden",
            Instant.now()
        ));

        UUID leanStackId = UUID.fromString("b0000000-0000-0000-0000-000000000002");
        saveCompanyDirect(conn, new Company(
            leanStackId,
            "lean-stack",
            "LeanStack Labs",
            "Performance engineering for ultra-cheap, ultra-efficient cloud systems.",
            "Helping tech teams ditch Kubernetes bloat in favor of single-box bare metal and Micronaut/Rust native stacks.",
            "https://leanstack.tech",
            "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200&auto=format&fit=crop&q=80",
            "Developer Tools & Cloud Infrastructure",
            "Zurich, Switzerland",
            Instant.now()
        ));

        UUID spitalulId = UUID.fromString("b0000000-0000-0000-0000-000000000003");
        saveCompanyDirect(conn, new Company(
            spitalulId,
            "spitalul_judetean_timisoara",
            "Timișoara County Emergency Clinical Hospital",
            "Major tertiary hospital, trauma center, cardiology, and university surgical clinics in western Romania.",
            "Serving over 450,000 regional consultations annually with 1,100 beds, accredited Level 1 trauma and acute cardiac care units.",
            "https://hosptim.ro",
            "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=200&auto=format&fit=crop&q=80",
            "Healthcare & Hospitals",
            "Timișoara, Romania",
            Instant.now()
        ));

        // Seed Graph Edges
        insertEdgeDirect(conn, elenaId, "PERSON", marcusId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, marcusId, "PERSON", kaiId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, elenaId, "PERSON", noraId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, elenaId, "PERSON", leanStackId, "COMPANY", "FOLLOWS", "ACCEPTED");
        insertEdgeDirect(conn, noraId, "PERSON", sovereignMeshId, "COMPANY", "FOLLOWS", "ACCEPTED");
        insertEdgeDirect(conn, drAndreiId, "PERSON", spitalulId, "COMPANY", "WORKS_AT", "ACCEPTED");
        insertEdgeDirect(conn, drAndreiId, "PERSON", drSorinaId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, drSorinaId, "PERSON", drAndreiId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, drAndreiId, "PERSON", drRaduId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, drRaduId, "PERSON", drAndreiId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, elenaId, "PERSON", drAndreiId, "PERSON", "CONNECTED_TO", "ACCEPTED");
        insertEdgeDirect(conn, drAndreiId, "PERSON", elenaId, "PERSON", "CONNECTED_TO", "ACCEPTED");

        // Seed Unified Posts
        savePostDirect(conn, new Post(
            UUID.fromString("c0000000-0000-0000-0000-000000000001"),
            elenaId,
            "PERSON",
            "elena_rostova",
            "Elena Rostova",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
            "Why We Replaced Corporate Chat with Decentralized Protocol Deep-Links",
            """
On traditional corporate platforms, direct messaging is a massive liability:
- Platforms inspect private communications for advertising or profiling.
- Centralized databases are prime targets for leaks and subpoenas.
- Massive infrastructure costs just to host chat servers and notification pipelines.

Here is our protocol comparison matrix:

| Feature | Centralized Silo | PeerMesh Decentralized Model |
| :--- | :--- | :--- |
| **Data Storage** | Centralized server plaintext/keys | 0 server storage (on-device only) |
| **GDPR Exposure** | High (must retain & process DM logs) | **Zero PII liability** |
| **Protocol** | Proprietary walled garden | Matrix, SimpleX, Nostr, Session |
| **Platform Cost** | $thousands/month for live chat | **$0.00** server messaging overhead |

When someone wants to message me, they click my Matrix address or SimpleX link and we communicate end-to-end encrypted directly.
            """,
            null, null, null, null, null,
            List.of("Privacy", "Decentralized", "GDPR", "Matrix", "Architecture"),
            true, false, false,
            14, 3, false, "YOU",
            Instant.now().minusSeconds(7200)
        ));

        savePostDirect(conn, new Post(
            UUID.fromString("c0000000-0000-0000-0000-000000000002"),
            marcusId,
            "PERSON",
            "marcus_poly",
            "Marcus Thorne",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
            "PostgreSQL Recursive Graph Traversals vs Neo4j in Production",
            """
Many teams immediately reach for dedicated graph databases like Neo4j as soon as they need 1st/2nd degree connection lookups or social network feeds.

With modern PostgreSQL and recursive CTEs or SQL:2023 `GRAPH_TABLE`, you can execute multi-hop graph queries directly on your relational tables with sub-millisecond execution.

```sql
WITH RECURSIVE network_path AS (
    SELECT source_id, target_id, 1 AS depth
    FROM graph_edges WHERE source_id = :start_user
    UNION
    SELECT e.source_id, e.target_id, np.depth + 1
    FROM graph_edges e JOIN network_path np ON e.source_id = np.target_id
    WHERE np.depth < 3
)
SELECT * FROM network_path;
```

Check out the benchmark whitepaper below:
            """,
            "https://postgresql.org/docs/current/queries-with.html",
            "PostgreSQL Documentation: Queries with WITH (Recursive CTEs)",
            "Detailed reference on writing recursive queries and graph traversal optimizations in PostgreSQL.",
            "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600&auto=format&fit=crop&q=80",
            "postgresql.org",
            List.of("PostgreSQL", "Databases", "Graph", "Performance", "SQL"),
            false, false, true,
            28, 7, false, "CONNECTION",
            "marcus-thorne/database-internals",
            "benchmarks/postgres-recursive-graphs.md",
            "7a9e32f",
            true,
            Instant.now().minusSeconds(18000)
        ));

        savePostDirect(conn, new Post(
            UUID.fromString("c0000000-0000-0000-0000-000000000003"),
            noraId,
            "PERSON",
            "nora_zen",
            "Nora Lindqvist",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
            "Death to the 3-Column LinkedIn Layout: Designing for Focus and Density",
            """
Why does the standard LinkedIn desktop view look like a billboard highway from 2008?

1. **Column 1**: Tiny profile card with vanity metrics nobody asked for.
2. **Column 2**: Squeezed central stream surrounded by distractions.
3. **Column 3**: Clickbait news, sponsored recommendation widgets, and empty margins.

In our Svelte implementation, we chose:
- **No CSS frameworks**: Handcrafted semantic CSS with CSS variables (`--bg`, `--accent`, `--border`).
- **Client-heavy state**: Fast local caching with zero server render penalties.
- **Fluid stream**: 720px focused reader that expands to split-view when inspecting deep discussions or graph connections.

Here is how clean reading can feel when marketing widgets are eliminated:

![Clean Interface Preview](https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800&auto=format&fit=crop&q=80)

Zero Tailwind. Zero bloat. Pure semantic web.
            """,
            "https://svelte.dev",
            "Svelte: Cybernetically enhanced web apps",
            "Write high-performance reactive applications with zero virtual-DOM overhead.",
            "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&auto=format&fit=crop&q=80",
            "svelte.dev",
            List.of("Svelte", "UI", "UX", "WebPerf", "CSS"),
            false, true, true,
            42, 11, false, "CONNECTION",
            Instant.now().minusSeconds(86400)
        ));

        savePostDirect(conn, new Post(
            UUID.fromString("c0000000-0000-0000-0000-000000000004"),
            leanStackId,
            "COMPANY",
            "lean-stack",
            "LeanStack Labs",
            "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200&auto=format&fit=crop&q=80",
            "LeanStack Announcement: Slashing JVM Footprint with Micronaut & GraalVM",
            """
We are thrilled to publish our latest benchmarks running Micronaut microservices on bare-metal:

| Runtime | RSS Memory | Cold Startup Time | Max RPS (1 vCPU) |
| :--- | :--- | :--- | :--- |
| Traditional Spring Boot | ~350 MB | 3.8s | 8,200 req/s |
| **Micronaut JIT (Java 25 Loom)** | **~58 MB** | **0.6s** | **24,500 req/s** |
| **Micronaut GraalVM Native** | **~24 MB** | **18 ms** | **29,000 req/s** |

You do NOT need costly multi-thousand dollar cloud bills to run performant professional networks. With the right stack, a single $5/month VPS can easily serve 50,000 active members.
            """,
            "https://micronaut.io",
            "Micronaut Framework - Modern, JVM-Based, Full-Stack Framework",
            "Build modular, easily testable microservice and serverless applications with minimal memory footprint.",
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            "micronaut.io",
            List.of("Micronaut", "Java25", "GraalVM", "CloudCost", "Performance"),
            true, false, true,
            65, 9, false, "FOLLOWING",
            Instant.now().minusSeconds(172800)
        ));

        savePostDirect(conn, new Post(
            UUID.fromString("c0000000-0000-0000-0000-000000000005"),
            drAndreiId,
            "PERSON",
            "dr_andrei_muresan",
            "Dr. Andrei Mureșan",
            "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=200&auto=format&fit=crop&q=80",
            "Emergency Cardiology Triage in Timișoara: Open Protocols & Response Times",
            """
In acute coronary syndrome (ACS), every minute saved preserves viable myocardial tissue. Over the past 12 months, our medical teams in Timișoara have overhauled pre-hospital triage protocols.

Here is our benchmark performance matrix at the Timișoara Emergency Hospital:

| Protocol Stage | Standard Regional Target | Timișoara Emergency Unit (2026) | Outcome Improvement |
| :--- | :--- | :--- | :--- |
| **Door-to-ECG** | < 10 minutes | **4.2 minutes** | -58% diagnostic delay |
| **Door-to-Balloon (PCI)** | < 90 minutes | **56 minutes** | +34% myocardial salvage |
| **Tele-ECG Triage** | In-transit review | **100% active units** | Pre-hospital activation |

Decentralized physician networks and open collaboration allow doctors in Timișoara to exchange critical clinical insights rapidly without corporate platform noise.
            """,
            "https://hosptim.ro",
            "Timișoara Emergency Clinical Hospital - Acute Cardiology Protocol",
            "Clinical triage metrics and emergency cardiology guidelines for the Timișoara metropolitan region.",
            "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=600&auto=format&fit=crop&q=80",
            "hosptim.ro",
            List.of("Medicine", "Cardiology", "Timișoara", "Timisoara", "Healthcare", "EmergencyCare"),
            true, false, true,
            38, 8, false, "NETWORK",
            null, null, null, false,
            Instant.now().minusSeconds(14400)
        ));

        // Seed Work Experiences
        saveWorkExperienceDirect(conn, new WorkExperience(
            UUID.fromString("e0000000-0000-0000-0000-000000000001"),
            drAndreiId,
            "Chief of Cardiology & Emergency Doctor",
            "Timișoara County Emergency Clinical Hospital",
            spitalulId,
            "Timișoara, Romania",
            "2018-03",
            "",
            true,
            """
Directing acute myocardial infarction triage, primary PCI cath-lab interventions, and clinical quality protocols across the emergency department.

- Reduced median door-to-balloon time to **56 minutes** (-38% vs national target).
- Supervised 1,400+ diagnostic and emergency coronary angiographies.
- Introduced pre-hospital 12-lead tele-ECG triage across county ambulance services.
            """,
            List.of("Cardiology", "Emergency Medicine", "Clinical Triage", "Interventional Cardiology", "Echocardiography"),
            Instant.now()
        ));

        saveWorkExperienceDirect(conn, new WorkExperience(
            UUID.fromString("e0000000-0000-0000-0000-000000000002"),
            drAndreiId,
            "Cardiology Fellow & Clinical Registrar",
            "Victor Babeș University Cardiology Clinic",
            null,
            "Timișoara, Romania",
            "2012-10",
            "2018-02",
            false,
            "Completed 5-year specialty residency training in clinical cardiology and acute cardiovascular intensive care.",
            List.of("Internal Medicine", "Cardiology", "Intensive Care"),
            Instant.now()
        ));

        saveWorkExperienceDirect(conn, new WorkExperience(
            UUID.fromString("e0000000-0000-0000-0000-000000000003"),
            drSorinaId,
            "Pediatric Surgeon & Medical Doctor",
            "Louis Țurcanu Children's Clinical Hospital",
            null,
            "Timișoara, Romania",
            "2019-06",
            "",
            true,
            """
Consultant pediatric surgeon specializing in robotic laparoscopy and neonatal surgical anomalies.

- Performed over 650 pediatric laparoscopic procedures.
- Co-investigator on multicenter randomized trial for neonatal intestinal atresia protocols.
            """,
            List.of("Pediatric Surgery", "Robotic Laparoscopy", "Neonatal Surgery", "Clinical Research"),
            Instant.now()
        ));

        saveWorkExperienceDirect(conn, new WorkExperience(
            UUID.fromString("e0000000-0000-0000-0000-000000000004"),
            drRaduId,
            "Neurologist & Neuro-Critical Care Doctor",
            "Victor Babeș University of Medicine and Pharmacy",
            null,
            "Timișoara, Romania",
            "2016-09",
            "",
            true,
            "Associate Professor and attending neuro-intensivist. Directing rapid stroke response and mechanical thrombectomy assessment protocols.",
            List.of("Neurology", "Neuroscience", "Critical Care", "Stroke Intervention"),
            Instant.now()
        ));

        saveWorkExperienceDirect(conn, new WorkExperience(
            UUID.fromString("e0000000-0000-0000-0000-000000000005"),
            elenaId,
            "Staff Distributed Systems Architect",
            "Sovereign Mesh",
            sovereignMeshId,
            "Berlin, Germany",
            "2022-04",
            "",
            true,
            "Designing zero-knowledge cryptographic authentication protocols and decentralized peer-to-peer gossip layers.",
            List.of("Rust", "Cryptography", "Distributed Systems", "Zero-Knowledge"),
            Instant.now()
        ));

        saveWorkExperienceDirect(conn, new WorkExperience(
            UUID.fromString("e0000000-0000-0000-0000-000000000006"),
            elenaId,
            "Senior Linux Kernel Engineer",
            "RedCore Systems",
            null,
            "Munich, Germany",
            "2018-02",
            "2022-03",
            false,
            "Developed and maintained in-kernel eBPF socket filters and high-performance packet routing drivers.",
            List.of("C", "Linux Kernel", "eBPF", "Networking"),
            Instant.now()
        ));

        saveWorkExperienceDirect(conn, new WorkExperience(
            UUID.fromString("e0000000-0000-0000-0000-000000000007"),
            marcusId,
            "Principal Database Engineer",
            "LeanStack Labs",
            leanStackId,
            "London, UK",
            "2021-08",
            "",
            true,
            "Leading PostgreSQL query optimization engine, SQL:2023 Property Graph implementations, and microsecond relational partitioning.",
            List.of("PostgreSQL", "SQL:2023", "Query Optimization", "C", "Micronaut"),
            Instant.now()
        ));
    }

    private void saveWorkExperienceDirect(Connection conn, WorkExperience exp) throws SQLException {
        String sql = """
            INSERT INTO work_experiences (id, profile_id, title, company_name, company_id, location, start_date, end_date, is_current, description_markdown, skills, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, exp.id());
            ps.setObject(2, exp.profileId());
            ps.setString(3, exp.title());
            ps.setString(4, exp.companyName());
            ps.setObject(5, exp.companyId());
            ps.setString(6, exp.location() != null ? exp.location() : "");
            ps.setString(7, exp.startDate() != null ? exp.startDate() : "");
            ps.setString(8, exp.endDate() != null ? exp.endDate() : "");
            ps.setBoolean(9, exp.isCurrent());
            ps.setString(10, exp.descriptionMarkdown() != null ? exp.descriptionMarkdown() : "");
            ps.setString(11, String.join(",", exp.skills() != null ? exp.skills() : List.of()));
            ps.setTimestamp(12, Timestamp.from(exp.createdAt() != null ? exp.createdAt() : Instant.now()));
            ps.executeUpdate();
        }
    }

    private void saveProfileDirect(Connection conn, Profile p) throws SQLException {
        String sql = """
            INSERT INTO profiles (id, handle, display_name, headline, bio_markdown, avatar_url, banner_url, skills, matrix_handle, nostr_npub, simplex_address, session_id, email_contact, pgp_fingerprint, github_username, github_content_repo, location, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, p.id());
            ps.setString(2, p.handle());
            ps.setString(3, p.displayName());
            ps.setString(4, p.headline());
            ps.setString(5, p.bioMarkdown());
            ps.setString(6, p.avatarUrl());
            ps.setString(7, p.bannerUrl());
            ps.setString(8, String.join(",", p.skills() != null ? p.skills() : List.of()));
            ps.setString(9, p.matrixHandle());
            ps.setString(10, p.nostrNpub());
            ps.setString(11, p.simplexAddress());
            ps.setString(12, p.sessionId());
            ps.setString(13, p.emailContact());
            ps.setString(14, p.pgpFingerprint());
            ps.setString(15, p.githubUsername() != null ? p.githubUsername() : "");
            ps.setString(16, p.githubContentRepo() != null ? p.githubContentRepo() : "");
            ps.setString(17, p.location() != null ? p.location() : "");
            ps.setTimestamp(18, Timestamp.from(p.createdAt() != null ? p.createdAt() : Instant.now()));
            ps.executeUpdate();
        }
    }

    private void saveCompanyDirect(Connection conn, Company c) throws SQLException {
        String sql = """
            INSERT INTO companies (id, handle, name, tagline, description_markdown, website_url, logo_url, industry, headquarters, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, c.id());
            ps.setString(2, c.handle());
            ps.setString(3, c.name());
            ps.setString(4, c.tagline());
            ps.setString(5, c.descriptionMarkdown());
            ps.setString(6, c.websiteUrl());
            ps.setString(7, c.logoUrl());
            ps.setString(8, c.industry());
            ps.setString(9, c.headquarters());
            ps.setTimestamp(10, Timestamp.from(c.createdAt() != null ? c.createdAt() : Instant.now()));
            ps.executeUpdate();
        }
    }

    private void insertEdgeDirect(Connection conn, UUID src, String srcType, UUID tgt, String tgtType, String edgeType, String status) throws SQLException {
        String sql = """
            INSERT INTO graph_edges (id, source_id, source_type, target_id, target_type, edge_type, status, properties, weight, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, '{}', 1.0, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, src);
            ps.setString(3, srcType);
            ps.setObject(4, tgt);
            ps.setString(5, tgtType);
            ps.setString(6, edgeType);
            ps.setString(7, status);
            ps.setTimestamp(8, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }

    private void savePostDirect(Connection conn, Post p) throws SQLException {
        String sql = """
            INSERT INTO posts (id, author_id, author_type, title, body_markdown, link_url, link_title, link_description, link_image_url, link_domain, tags, has_tables, has_images, has_links, like_count, comment_count, github_repo, github_path, github_commit_sha, is_self_hosted, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, p.id());
            ps.setObject(2, p.authorId());
            ps.setString(3, p.authorType());
            ps.setString(4, p.title());
            ps.setString(5, p.bodyMarkdown());
            ps.setString(6, p.linkUrl());
            ps.setString(7, p.linkTitle());
            ps.setString(8, p.linkDescription());
            ps.setString(9, p.linkImageUrl());
            ps.setString(10, p.linkDomain());
            ps.setString(11, String.join(",", p.tags() != null ? p.tags() : List.of()));
            ps.setBoolean(12, p.hasTables());
            ps.setBoolean(13, p.hasImages());
            ps.setBoolean(14, p.hasLinks());
            ps.setInt(15, p.likeCount());
            ps.setInt(16, p.commentCount());
            ps.setString(17, p.githubRepo());
            ps.setString(18, p.githubPath());
            ps.setString(19, p.githubCommitSha());
            ps.setBoolean(20, p.selfHosted());
            ps.setTimestamp(21, Timestamp.from(p.createdAt() != null ? p.createdAt() : Instant.now()));
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // Public API Methods
    // ----------------------------------------------------------------

    public List<Post> getFeed(UUID viewerId, String feedType, String tag, Boolean hasTables, Boolean hasImages, Boolean hasLinks, int limit, int offset) {
        return runRead(conn -> {
            StringBuilder sql = new StringBuilder("""
                SELECT p.*,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.handle ELSE pr.handle END AS a_handle,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.name ELSE pr.display_name END AS a_name,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.logo_url ELSE pr.avatar_url END AS a_avatar
                FROM posts p
                LEFT JOIN profiles pr ON pr.id = p.author_id AND p.author_type = 'PERSON'
                LEFT JOIN companies c ON c.id = p.author_id AND p.author_type = 'COMPANY'
                WHERE 1=1
            """);

            List<Object> params = new ArrayList<>();

            if ("companies".equalsIgnoreCase(feedType)) {
                sql.append(" AND p.author_type = 'COMPANY'");
            }

            if (tag != null && !tag.isBlank()) {
                sql.append(" AND p.tags LIKE ?");
                params.add("%" + tag.trim() + "%");
            }

            if (hasTables != null) {
                sql.append(" AND p.has_tables = ?");
                params.add(hasTables);
            }
            if (hasImages != null) {
                sql.append(" AND p.has_images = ?");
                params.add(hasImages);
            }
            if (hasLinks != null) {
                sql.append(" AND p.has_links = ?");
                params.add(hasLinks);
            }

            sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
            params.add(Math.min(limit > 0 ? limit : 25, 100));
            params.add(Math.max(offset, 0));

            List<Post> results = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapPost(rs, viewerId, conn));
                    }
                }
            }
            return results;
        });
    }

    private static final Set<String> STOP_WORDS = Set.of(
        "in", "at", "of", "from", "near", "the", "a", "an", "and", "or", "for", "with", "to", "by", "on"
    );

    public static String normalizeText(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    private static String singularize(String word) {
        if (word == null) return "";
        String w = word.toLowerCase();
        if (w.endsWith("ies") && w.length() > 4) {
            return w.substring(0, w.length() - 3) + "y";
        }
        if (w.endsWith("es") && w.length() > 4) {
            return w.substring(0, w.length() - 2);
        }
        if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 3) {
            return w.substring(0, w.length() - 1);
        }
        return w;
    }

    private static List<String> getVariations(String tok) {
        if (tok == null || tok.isBlank()) return List.of();
        Set<String> set = new LinkedHashSet<>();
        set.add(tok);
        set.add(singularize(tok));
        if (tok.contains("timisoara")) {
            set.add(tok.replace("timisoara", "timișoara"));
            set.add(tok.replace("timisoara", "timişoara"));
            set.add(singularize(tok).replace("timisoara", "timișoara"));
            set.add(singularize(tok).replace("timisoara", "timişoara"));
        } else if (tok.contains("timișoara") || tok.contains("timişoara")) {
            String ascii = tok.replace("timișoara", "timisoara").replace("timişoara", "timisoara");
            set.add(ascii);
            set.add(singularize(ascii));
        }
        return new ArrayList<>(set);
    }

    private static final Map<String, List<String>> ROLE_SYNONYMS = Map.of(
        "doctor", List.of("doctor", "physician", "medic", "surgeon", "cardiologist", "neurologist", "pediatrician", "specialist", "clinical", "hospital", "dr"),
        "physician", List.of("doctor", "physician", "medic", "specialist", "clinical"),
        "surgeon", List.of("surgeon", "surgery", "surgical", "doctor", "medic"),
        "cardiologist", List.of("cardiologist", "cardiology", "cardiac", "doctor"),
        "neurologist", List.of("neurologist", "neurology", "neuroscience", "doctor"),
        "pediatrician", List.of("pediatrician", "pediatrics", "pediatric", "doctor"),
        "developer", List.of("developer", "engineer", "architect", "programmer", "coder", "dev"),
        "engineer", List.of("engineer", "developer", "architect", "infrastructure", "dev")
    );

    public SearchResult searchUnified(String query) {
        if (query == null || query.isBlank()) {
            return new SearchResult("", null, null, List.of(), List.of(), List.of(), 0);
        }
        String raw = query.trim();
        String normQ = normalizeText(raw);

        String detectedRole = null;
        String detectedLocation = null;

        if (normQ.contains(" in ")) {
            int idx = normQ.indexOf(" in ");
            detectedRole = singularize(normQ.substring(0, idx).trim());
            detectedLocation = normQ.substring(idx + 4).trim();
        } else if (normQ.contains(" at ")) {
            int idx = normQ.indexOf(" at ");
            detectedRole = singularize(normQ.substring(0, idx).trim());
            detectedLocation = normQ.substring(idx + 4).trim();
        }

        List<Profile> people = searchPeople(normQ, detectedRole, detectedLocation);
        List<Company> companies = searchCompanies(normQ, detectedRole, detectedLocation);
        List<Post> posts = searchPosts(raw, null, null, null, 25, 0);

        return new SearchResult(
            raw,
            detectedRole,
            detectedLocation,
            people,
            companies,
            posts,
            people.size() + companies.size() + posts.size()
        );
    }

    public List<Profile> searchPeople(String normQ, String role, String location) {
        return runRead(conn -> {
            List<Profile> all = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM profiles")) {
                while (rs.next()) {
                    all.add(mapProfile(rs));
                }
            }

            Map<UUID, List<WorkExperience>> expByProfile = new HashMap<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM work_experiences")) {
                while (rs.next()) {
                    WorkExperience we = mapWorkExperience(rs);
                    expByProfile.computeIfAbsent(we.profileId(), k -> new ArrayList<>()).add(we);
                }
            } catch (Exception ignored) {}

            Map<UUID, List<Education>> eduByProfile = new HashMap<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM educations")) {
                while (rs.next()) {
                    Education ed = mapEducation(rs);
                    eduByProfile.computeIfAbsent(ed.profileId(), k -> new ArrayList<>()).add(ed);
                }
            } catch (Exception ignored) {}

            String roleSingular = role != null ? singularize(role) : null;
            List<String> roleTokens = new ArrayList<>();
            if (roleSingular != null) {
                roleTokens.add(roleSingular);
                if (ROLE_SYNONYMS.containsKey(roleSingular)) {
                    roleTokens.addAll(ROLE_SYNONYMS.get(roleSingular));
                }
            }

            List<String> queryTokens = Arrays.stream(normQ.split("\\s+"))
                    .filter(t -> !STOP_WORDS.contains(t) && !t.isBlank())
                    .toList();

            List<Profile> results = new ArrayList<>();
            for (Profile p : all) {
                String nameNorm = normalizeText(p.displayName());
                String handleNorm = normalizeText(p.handle());
                String headlineNorm = normalizeText(p.headline());
                String bioNorm = normalizeText(p.bioMarkdown());
                String locNorm = normalizeText(p.location());
                String skillsNorm = normalizeText(String.join(" ", p.skills() != null ? p.skills() : List.of()));
                
                List<WorkExperience> exps = expByProfile.getOrDefault(p.id(), List.of());
                StringBuilder expText = new StringBuilder();
                for (WorkExperience e : exps) {
                    expText.append(" ").append(normalizeText(e.title()))
                           .append(" ").append(normalizeText(e.companyName()))
                           .append(" ").append(normalizeText(e.location()))
                           .append(" ").append(normalizeText(e.descriptionMarkdown()))
                           .append(" ").append(normalizeText(String.join(" ", e.skills() != null ? e.skills() : List.of())));
                }

                List<Education> edus = eduByProfile.getOrDefault(p.id(), List.of());
                StringBuilder eduText = new StringBuilder();
                for (Education ed : edus) {
                    eduText.append(" ").append(normalizeText(ed.institutionName()))
                           .append(" ").append(normalizeText(ed.degree()))
                           .append(" ").append(normalizeText(ed.fieldOfStudy()))
                           .append(" ").append(normalizeText(ed.activitiesMarkdown()))
                           .append(" ").append(normalizeText(String.join(" ", ed.skills() != null ? ed.skills() : List.of())));
                }

                String full = String.join(" ", nameNorm, handleNorm, headlineNorm, bioNorm, locNorm, skillsNorm, expText.toString(), eduText.toString());

                boolean matched = false;

                // 1. Structured Match if role & location were detected (e.g. "doctors in timisoara")
                if (role != null && location != null && !role.isBlank() && !location.isBlank()) {
                    boolean locMatched = locNorm.contains(location) || headlineNorm.contains(location) || full.contains(location);
                    boolean roleMatched = false;
                    for (String rt : roleTokens) {
                        if (headlineNorm.contains(rt) || skillsNorm.contains(rt) || bioNorm.contains(rt) || nameNorm.contains(rt) || expText.indexOf(rt) >= 0 || eduText.indexOf(rt) >= 0) {
                            roleMatched = true;
                            break;
                        }
                    }
                    if (locMatched && roleMatched) {
                        matched = true;
                    }
                }

                // 2. Token Match: all non-stopword tokens must match across the profile
                if (!matched && !queryTokens.isEmpty()) {
                    boolean allTokens = true;
                    for (String tok : queryTokens) {
                        String tokSingular = singularize(tok);
                        List<String> syns = ROLE_SYNONYMS.getOrDefault(tokSingular, List.of(tokSingular));
                        boolean tokMatched = false;
                        for (String s : syns) {
                            if (full.contains(s) || full.contains(tok)) {
                                tokMatched = true;
                                break;
                            }
                        }
                        if (!tokMatched) {
                            allTokens = false;
                            break;
                        }
                    }
                    if (allTokens) {
                        matched = true;
                    }
                }

                if (matched) {
                    results.add(p);
                }
            }
            return results;
        });
    }

    public List<Company> searchCompanies(String normQ, String role, String location) {
        return runRead(conn -> {
            List<Company> all = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM companies")) {
                while (rs.next()) {
                    all.add(mapCompany(rs));
                }
            }

            List<String> queryTokens = Arrays.stream(normQ.split("\\s+"))
                    .filter(t -> !STOP_WORDS.contains(t) && !t.isBlank())
                    .toList();

            List<Company> results = new ArrayList<>();
            for (Company c : all) {
                String nameNorm = normalizeText(c.name());
                String handleNorm = normalizeText(c.handle());
                String taglineNorm = normalizeText(c.tagline());
                String descNorm = normalizeText(c.descriptionMarkdown());
                String indNorm = normalizeText(c.industry());
                String hqNorm = normalizeText(c.headquarters());
                String full = String.join(" ", nameNorm, handleNorm, taglineNorm, descNorm, indNorm, hqNorm);

                boolean matched = false;

                if (location != null && !location.isBlank() && hqNorm.contains(location)) {
                    if (role != null && !role.isBlank()) {
                        String rSing = singularize(role);
                        List<String> syns = ROLE_SYNONYMS.getOrDefault(rSing, List.of(rSing));
                        for (String s : syns) {
                            if (full.contains(s)) {
                                matched = true;
                                break;
                            }
                        }
                    } else {
                        matched = true;
                    }
                }

                if (!matched && !queryTokens.isEmpty()) {
                    boolean allTokens = true;
                    for (String tok : queryTokens) {
                        String tokSing = singularize(tok);
                        List<String> syns = ROLE_SYNONYMS.getOrDefault(tokSing, List.of(tokSing));
                        boolean tokMatched = false;
                        for (String s : syns) {
                            if (full.contains(s) || full.contains(tok)) {
                                tokMatched = true;
                                break;
                            }
                        }
                        if (!tokMatched) {
                            allTokens = false;
                            break;
                        }
                    }
                    if (allTokens) {
                        matched = true;
                    }
                }

                if (matched) {
                    results.add(c);
                }
            }
            return results;
        });
    }

    public List<Post> searchPosts(String query, Boolean hasTables, Boolean hasImages, Boolean hasLinks, int limit, int offset) {
        return runRead(conn -> {
            StringBuilder sql = new StringBuilder("""
                SELECT p.*,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.handle ELSE pr.handle END AS a_handle,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.name ELSE pr.display_name END AS a_name,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.logo_url ELSE pr.avatar_url END AS a_avatar
                FROM posts p
                LEFT JOIN profiles pr ON pr.id = p.author_id AND p.author_type = 'PERSON'
                LEFT JOIN companies c ON c.id = p.author_id AND p.author_type = 'COMPANY'
                WHERE 1=1
            """);

            List<Object> params = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String normQ = normalizeText(query);
                List<String> tokens = Arrays.stream(normQ.split("\\s+"))
                        .filter(t -> !STOP_WORDS.contains(t) && !t.isBlank())
                        .toList();
                if (tokens.isEmpty()) {
                    tokens = List.of(normQ);
                }

                for (String tok : tokens) {
                    List<String> vars = getVariations(tok);
                    List<String> orClauses = new ArrayList<>();
                    for (String v : vars) {
                        orClauses.add("LOWER(p.title) LIKE ?");
                        params.add("%" + v + "%");
                        orClauses.add("LOWER(p.body_markdown) LIKE ?");
                        params.add("%" + v + "%");
                        orClauses.add("LOWER(p.link_title) LIKE ?");
                        params.add("%" + v + "%");
                        orClauses.add("LOWER(p.link_url) LIKE ?");
                        params.add("%" + v + "%");
                        orClauses.add("LOWER(p.tags) LIKE ?");
                        params.add("%" + v + "%");
                    }
                    if (!orClauses.isEmpty()) {
                        sql.append(" AND (").append(String.join(" OR ", orClauses)).append(")");
                    }
                }
            }
            if (hasTables != null) {
                sql.append(" AND p.has_tables = ?");
                params.add(hasTables);
            }
            if (hasImages != null) {
                sql.append(" AND p.has_images = ?");
                params.add(hasImages);
            }
            if (hasLinks != null) {
                sql.append(" AND p.has_links = ?");
                params.add(hasLinks);
            }

            sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
            params.add(Math.min(limit > 0 ? limit : 25, 100));
            params.add(Math.max(offset, 0));

            List<Post> results = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapPost(rs, null, conn));
                    }
                }
            }
            return results;
        });
    }

    public Optional<Post> getPost(UUID id, UUID viewerId) {
        return runRead(conn -> {
            String sql = """
                SELECT p.*,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.handle ELSE pr.handle END AS a_handle,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.name ELSE pr.display_name END AS a_name,
                       CASE WHEN p.author_type = 'COMPANY' THEN c.logo_url ELSE pr.avatar_url END AS a_avatar
                FROM posts p
                LEFT JOIN profiles pr ON pr.id = p.author_id AND p.author_type = 'PERSON'
                LEFT JOIN companies c ON c.id = p.author_id AND p.author_type = 'COMPANY'
                WHERE p.id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapPost(rs, viewerId, conn));
                    }
                }
            }
            return Optional.empty();
        });
    }

    public Post createPost(Post req) {
        UUID id = req.id() != null ? req.id() : UUID.randomUUID();
        boolean hasTables = Post.checkHasTables(req.bodyMarkdown());
        boolean hasImages = Post.checkHasImages(req.bodyMarkdown());
        boolean hasLinks = Post.checkHasLinks(req.bodyMarkdown(), req.linkUrl());
        Instant now = req.createdAt() != null ? req.createdAt() : Instant.now();

        runWrite(conn -> {
            String sql = """
                INSERT INTO posts (id, author_id, author_type, title, body_markdown, link_url, link_title, link_description, link_image_url, link_domain, tags, has_tables, has_images, has_links, like_count, comment_count, github_repo, github_path, github_commit_sha, is_self_hosted, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                ps.setObject(2, req.authorId());
                ps.setString(3, req.authorType() != null ? req.authorType() : "PERSON");
                ps.setString(4, req.title());
                ps.setString(5, req.bodyMarkdown());
                ps.setString(6, req.linkUrl());
                ps.setString(7, req.linkTitle());
                ps.setString(8, req.linkDescription());
                ps.setString(9, req.linkImageUrl());
                ps.setString(10, req.linkDomain());
                ps.setString(11, String.join(",", req.tags() != null ? req.tags() : List.of()));
                ps.setBoolean(12, hasTables);
                ps.setBoolean(13, hasImages);
                ps.setBoolean(14, hasLinks);
                ps.setString(15, req.githubRepo());
                ps.setString(16, req.githubPath());
                ps.setString(17, req.githubCommitSha());
                ps.setBoolean(18, req.selfHosted());
                ps.setTimestamp(19, Timestamp.from(now));
                ps.executeUpdate();
            }
        });
        objectStorageService.savePostContent(id, req.bodyMarkdown());
        return getPost(id, req.authorId()).orElseThrow();
    }

    public Post updatePost(Post post) {
        boolean hasTables = Post.checkHasTables(post.bodyMarkdown());
        boolean hasImages = Post.checkHasImages(post.bodyMarkdown());
        boolean hasLinks = Post.checkHasLinks(post.bodyMarkdown(), post.linkUrl());

        runWrite(conn -> {
            String sql = """
                UPDATE posts SET
                    title = ?,
                    body_markdown = ?,
                    link_url = ?,
                    link_title = ?,
                    link_description = ?,
                    link_image_url = ?,
                    link_domain = ?,
                    tags = ?,
                    has_tables = ?,
                    has_images = ?,
                    has_links = ?,
                    github_repo = ?,
                    github_path = ?,
                    github_commit_sha = ?,
                    is_self_hosted = ?
                WHERE id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, post.title());
                ps.setString(2, post.bodyMarkdown());
                ps.setString(3, post.linkUrl());
                ps.setString(4, post.linkTitle());
                ps.setString(5, post.linkDescription());
                ps.setString(6, post.linkImageUrl());
                ps.setString(7, post.linkDomain());
                ps.setString(8, String.join(",", post.tags() != null ? post.tags() : List.of()));
                ps.setBoolean(9, hasTables);
                ps.setBoolean(10, hasImages);
                ps.setBoolean(11, hasLinks);
                ps.setString(12, post.githubRepo());
                ps.setString(13, post.githubPath());
                ps.setString(14, post.githubCommitSha());
                ps.setBoolean(15, post.selfHosted());
                ps.setObject(16, post.id());
                ps.executeUpdate();
            }
        });
        objectStorageService.savePostContent(post.id(), post.bodyMarkdown());
        return getPost(post.id(), post.authorId()).orElseThrow();
    }

    public boolean toggleLike(UUID postId, UUID userId) {
        return runWriteWithResult(conn -> {
            String checkSql = "SELECT id FROM post_reactions WHERE post_id = ? AND user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setObject(1, postId);
                ps.setObject(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Unlike
                        try (PreparedStatement del = conn.prepareStatement("DELETE FROM post_reactions WHERE post_id = ? AND user_id = ?")) {
                            del.setObject(1, postId);
                            del.setObject(2, userId);
                            del.executeUpdate();
                        }
                        try (PreparedStatement upd = conn.prepareStatement("UPDATE posts SET like_count = GREATEST(0, like_count - 1) WHERE id = ?")) {
                            upd.setObject(1, postId);
                            upd.executeUpdate();
                        }
                        return false;
                    } else {
                        // Like
                        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO post_reactions (id, post_id, user_id) VALUES (?, ?, ?)")) {
                            ins.setObject(1, UUID.randomUUID());
                            ins.setObject(2, postId);
                            ins.setObject(3, userId);
                            ins.executeUpdate();
                        }
                        try (PreparedStatement upd = conn.prepareStatement("UPDATE posts SET like_count = like_count + 1 WHERE id = ?")) {
                            upd.setObject(1, postId);
                            upd.executeUpdate();
                        }
                        return true;
                    }
                }
            }
        });
    }

    private <T> T runWriteWithResult(SqlFunction<T> function) {
        return connectionOps.executeWrite(status -> {
            try {
                Connection conn = status.getConnection();
                T res = function.apply(conn);
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                return res;
            } catch (SQLException e) {
                throw new RuntimeException("DB Write error: " + e.getMessage(), e);
            }
        });
    }

    public Comment addComment(UUID postId, UUID authorId, String bodyMarkdown) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        runWrite(conn -> {
            String ins = "INSERT INTO post_comments (id, post_id, author_id, body_markdown, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                ps.setObject(1, id);
                ps.setObject(2, postId);
                ps.setObject(3, authorId);
                ps.setString(4, bodyMarkdown);
                ps.setTimestamp(5, Timestamp.from(now));
                ps.executeUpdate();
            }
            try (PreparedStatement upd = conn.prepareStatement("UPDATE posts SET comment_count = comment_count + 1 WHERE id = ?")) {
                upd.setObject(1, postId);
                upd.executeUpdate();
            }
        });
        Profile author = getProfileById(authorId).orElse(null);
        return new Comment(
            id, postId, authorId,
            author != null ? author.handle() : "unknown",
            author != null ? author.displayName() : "Anonymous",
            author != null ? author.avatarUrl() : "",
            bodyMarkdown, now
        );
    }

    public List<Comment> getComments(UUID postId) {
        return runRead(conn -> {
            String sql = """
                SELECT c.*, p.handle, p.display_name, p.avatar_url
                FROM post_comments c
                JOIN profiles p ON p.id = c.author_id
                WHERE c.post_id = ?
                ORDER BY c.created_at ASC
            """;
            List<Comment> results = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, postId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new Comment(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("post_id"),
                            (UUID) rs.getObject("author_id"),
                            rs.getString("handle"),
                            rs.getString("display_name"),
                            rs.getString("avatar_url"),
                            rs.getString("body_markdown"),
                            rs.getTimestamp("created_at").toInstant()
                        ));
                    }
                }
            }
            return results;
        });
    }

    public List<Profile> getProfiles() {
        return runRead(conn -> {
            String sql = "SELECT * FROM profiles ORDER BY display_name ASC";
            List<Profile> list = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapProfile(rs));
                }
            }
            return list;
        });
    }

    public Optional<Profile> getProfile(String handleOrId) {
        try {
            UUID id = UUID.fromString(handleOrId);
            return getProfileById(id);
        } catch (IllegalArgumentException e) {
            return getProfileByHandle(handleOrId.replaceFirst("^@", ""));
        }
    }

    public Optional<Profile> getProfileById(UUID id) {
        return runRead(conn -> {
            String sql = "SELECT * FROM profiles WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapProfile(rs));
                }
            }
            return Optional.empty();
        });
    }

    public Optional<Profile> getProfileByHandle(String handle) {
        return runRead(conn -> {
            String sql = "SELECT * FROM profiles WHERE LOWER(handle) = LOWER(?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, handle);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapProfile(rs));
                }
            }
            return Optional.empty();
        });
    }

    public Optional<Profile> getProfileByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return runRead(conn -> {
            String sql = "SELECT * FROM profiles WHERE LOWER(email_contact) = LOWER(?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapProfile(rs));
                }
            }
            return Optional.empty();
        });
    }

    public Profile saveProfile(Profile p) {
        UUID id = p.id() != null ? p.id() : UUID.randomUUID();
        Profile toSave = p.withId(id);
        runWrite(conn -> saveProfileDirect(conn, toSave));
        return toSave;
    }

    public Optional<Profile> updateProfile(UUID id, Profile p) {
        return runWriteWithResult(conn -> {
            String sql = """
                UPDATE profiles
                SET handle = COALESCE(NULLIF(?, ''), handle),
                    display_name = ?,
                    headline = ?,
                    bio_markdown = ?,
                    avatar_url = ?,
                    banner_url = ?,
                    skills = ?,
                    matrix_handle = ?,
                    nostr_npub = ?,
                    simplex_address = ?,
                    session_id = ?,
                    email_contact = ?,
                    pgp_fingerprint = ?,
                    github_username = ?,
                    github_content_repo = ?,
                    location = ?
                WHERE id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.handle() != null ? p.handle().trim() : "");
                ps.setString(2, p.displayName() != null ? p.displayName().trim() : "");
                ps.setString(3, p.headline() != null ? p.headline().trim() : "");
                ps.setString(4, p.bioMarkdown() != null ? p.bioMarkdown().trim() : "");
                ps.setString(5, p.avatarUrl() != null ? p.avatarUrl().trim() : "");
                ps.setString(6, p.bannerUrl() != null ? p.bannerUrl().trim() : "");
                ps.setString(7, p.skills() != null ? String.join(",", p.skills()) : "");
                ps.setString(8, p.matrixHandle() != null ? p.matrixHandle().trim() : "");
                ps.setString(9, p.nostrNpub() != null ? p.nostrNpub().trim() : "");
                ps.setString(10, p.simplexAddress() != null ? p.simplexAddress().trim() : "");
                ps.setString(11, p.sessionId() != null ? p.sessionId().trim() : "");
                ps.setString(12, p.emailContact() != null ? p.emailContact().trim() : "");
                ps.setString(13, p.pgpFingerprint() != null ? p.pgpFingerprint().trim() : "");
                ps.setString(14, p.githubUsername() != null ? p.githubUsername().trim() : "");
                ps.setString(15, p.githubContentRepo() != null ? p.githubContentRepo().trim() : "");
                ps.setString(16, p.location() != null ? p.location().trim() : "");
                ps.setObject(17, id);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    return getProfileById(id);
                }
            }
            return Optional.empty();
        });
    }

    public List<WorkExperience> getWorkExperiences(UUID profileId) {
        return runRead(conn -> {
            List<WorkExperience> list = new ArrayList<>();
            String sql = "SELECT * FROM work_experiences WHERE profile_id = ? ORDER BY is_current DESC, start_date DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, profileId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapWorkExperience(rs));
                    }
                }
            }
            return list;
        });
    }

    public WorkExperience saveWorkExperience(WorkExperience exp) {
        UUID id = exp.id() != null ? exp.id() : UUID.randomUUID();
        WorkExperience toSave = exp.withId(id);
        runWrite(conn -> {
            saveWorkExperienceDirect(conn, toSave);
            if (toSave.isCurrent() && toSave.companyId() != null) {
                insertEdgeDirect(conn, toSave.profileId(), "PERSON", toSave.companyId(), "COMPANY", "WORKS_AT", "ACCEPTED");
            }
        });
        return toSave;
    }

    public Optional<WorkExperience> updateWorkExperience(UUID id, WorkExperience exp) {
        return runWriteWithResult(conn -> {
            String sql = """
                UPDATE work_experiences
                SET title = ?, company_name = ?, company_id = ?, location = ?, start_date = ?, end_date = ?, is_current = ?, description_markdown = ?, skills = ?
                WHERE id = ? AND profile_id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, exp.title());
                ps.setString(2, exp.companyName());
                ps.setObject(3, exp.companyId());
                ps.setString(4, exp.location() != null ? exp.location() : "");
                ps.setString(5, exp.startDate() != null ? exp.startDate() : "");
                ps.setString(6, exp.endDate() != null ? exp.endDate() : "");
                ps.setBoolean(7, exp.isCurrent());
                ps.setString(8, exp.descriptionMarkdown() != null ? exp.descriptionMarkdown() : "");
                ps.setString(9, String.join(",", exp.skills() != null ? exp.skills() : List.of()));
                ps.setObject(10, id);
                ps.setObject(11, exp.profileId());
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    if (exp.isCurrent() && exp.companyId() != null) {
                        insertEdgeDirect(conn, exp.profileId(), "PERSON", exp.companyId(), "COMPANY", "WORKS_AT", "ACCEPTED");
                    }
                    return Optional.of(exp.withId(id));
                }
            }
            return Optional.empty();
        });
    }

    public boolean deleteWorkExperience(UUID id, UUID profileId) {
        return runWriteWithResult(conn -> {
            String sql = "DELETE FROM work_experiences WHERE id = ? AND profile_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                ps.setObject(2, profileId);
                return ps.executeUpdate() > 0;
            }
        });
    }

    // ----------------------------------------------------------------
    // Education Methods (Academic Credentials & University Degrees)
    // ----------------------------------------------------------------

    public List<Education> getEducations(UUID profileId) {
        return runRead(conn -> {
            String sql = "SELECT * FROM educations WHERE profile_id = ? ORDER BY is_current DESC, start_date DESC";
            List<Education> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, profileId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapEducation(rs));
                    }
                }
            } catch (Exception ignored) {}
            return list;
        });
    }

    public Education saveEducation(Education edu) {
        UUID id = edu.id() != null ? edu.id() : UUID.randomUUID();
        Education toSave = new Education(
            id,
            edu.profileId(),
            edu.institutionName(),
            edu.degree(),
            edu.fieldOfStudy(),
            edu.startDate(),
            edu.endDate(),
            edu.isCurrent(),
            edu.grade(),
            edu.activitiesMarkdown(),
            edu.skills(),
            Instant.now()
        );
        runWrite(conn -> {
            saveEducationDirect(conn, toSave);
        });
        return toSave;
    }

    private void saveEducationDirect(Connection conn, Education edu) throws SQLException {
        String sql = """
            INSERT INTO educations (id, profile_id, institution_name, degree, field_of_study, start_date, end_date, is_current, grade, activities_markdown, skills, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, edu.id());
            ps.setObject(2, edu.profileId());
            ps.setString(3, edu.institutionName());
            ps.setString(4, edu.degree());
            ps.setString(5, edu.fieldOfStudy() != null ? edu.fieldOfStudy() : "");
            ps.setString(6, edu.startDate() != null ? edu.startDate() : "");
            ps.setString(7, edu.endDate() != null ? edu.endDate() : "");
            ps.setBoolean(8, edu.isCurrent());
            ps.setString(9, edu.grade() != null ? edu.grade() : "");
            ps.setString(10, edu.activitiesMarkdown() != null ? edu.activitiesMarkdown() : "");
            ps.setString(11, String.join(",", edu.skills() != null ? edu.skills() : List.of()));
            ps.setTimestamp(12, Timestamp.from(edu.createdAt() != null ? edu.createdAt() : Instant.now()));
            ps.executeUpdate();
        }
    }

    public Optional<Education> updateEducation(UUID id, Education edu) {
        return runWriteWithResult(conn -> {
            String sql = """
                UPDATE educations
                SET institution_name = ?, degree = ?, field_of_study = ?, start_date = ?, end_date = ?, is_current = ?, grade = ?, activities_markdown = ?, skills = ?
                WHERE id = ? AND profile_id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, edu.institutionName());
                ps.setString(2, edu.degree());
                ps.setString(3, edu.fieldOfStudy() != null ? edu.fieldOfStudy() : "");
                ps.setString(4, edu.startDate() != null ? edu.startDate() : "");
                ps.setString(5, edu.endDate() != null ? edu.endDate() : "");
                ps.setBoolean(6, edu.isCurrent());
                ps.setString(7, edu.grade() != null ? edu.grade() : "");
                ps.setString(8, edu.activitiesMarkdown() != null ? edu.activitiesMarkdown() : "");
                ps.setString(9, String.join(",", edu.skills() != null ? edu.skills() : List.of()));
                ps.setObject(10, id);
                ps.setObject(11, edu.profileId());
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    return Optional.of(new Education(
                        id,
                        edu.profileId(),
                        edu.institutionName(),
                        edu.degree(),
                        edu.fieldOfStudy(),
                        edu.startDate(),
                        edu.endDate(),
                        edu.isCurrent(),
                        edu.grade(),
                        edu.activitiesMarkdown(),
                        edu.skills(),
                        edu.createdAt()
                    ));
                }
            }
            return Optional.empty();
        });
    }

    public boolean deleteEducation(UUID id, UUID profileId) {
        return runWriteWithResult(conn -> {
            String sql = "DELETE FROM educations WHERE id = ? AND profile_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                ps.setObject(2, profileId);
                return ps.executeUpdate() > 0;
            }
        });
    }

    // ----------------------------------------------------------------
    // Profile Highlights (Achievements, Patents, Prizes, Books, Conferences)
    // ----------------------------------------------------------------

    public List<Highlight> getHighlights(UUID profileId) {
        return runRead(conn -> {
            String sql = "SELECT * FROM profile_highlights WHERE profile_id = ? ORDER BY date_or_year DESC, created_at DESC";
            List<Highlight> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, profileId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapHighlight(rs));
                    }
                }
            } catch (Exception ignored) {}
            return list;
        });
    }

    public Highlight saveHighlight(Highlight h) {
        UUID id = h.id() != null ? h.id() : UUID.randomUUID();
        Highlight toSave = new Highlight(
            id,
            h.profileId(),
            h.type(),
            h.title(),
            h.issuerOrVenue(),
            h.dateOrYear(),
            h.descriptionMarkdown(),
            h.externalUrl(),
            Instant.now()
        );
        runWrite(conn -> {
            saveHighlightDirect(conn, toSave);
        });
        return toSave;
    }

    private void saveHighlightDirect(Connection conn, Highlight h) throws SQLException {
        String sql = """
            INSERT INTO profile_highlights (id, profile_id, type, title, issuer_or_venue, date_or_year, description_markdown, external_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, h.id());
            ps.setObject(2, h.profileId());
            ps.setString(3, h.type() != null ? h.type() : "ACHIEVEMENT");
            ps.setString(4, h.title());
            ps.setString(5, h.issuerOrVenue() != null ? h.issuerOrVenue() : "");
            ps.setString(6, h.dateOrYear() != null ? h.dateOrYear() : "");
            ps.setString(7, h.descriptionMarkdown() != null ? h.descriptionMarkdown() : "");
            ps.setString(8, h.externalUrl() != null ? h.externalUrl() : "");
            ps.setTimestamp(9, Timestamp.from(h.createdAt() != null ? h.createdAt() : Instant.now()));
            ps.executeUpdate();
        }
    }

    public Optional<Highlight> updateHighlight(UUID id, Highlight h) {
        return runWriteWithResult(conn -> {
            String sql = """
                UPDATE profile_highlights
                SET type = ?, title = ?, issuer_or_venue = ?, date_or_year = ?, description_markdown = ?, external_url = ?
                WHERE id = ? AND profile_id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, h.type() != null ? h.type() : "ACHIEVEMENT");
                ps.setString(2, h.title());
                ps.setString(3, h.issuerOrVenue() != null ? h.issuerOrVenue() : "");
                ps.setString(4, h.dateOrYear() != null ? h.dateOrYear() : "");
                ps.setString(5, h.descriptionMarkdown() != null ? h.descriptionMarkdown() : "");
                ps.setString(6, h.externalUrl() != null ? h.externalUrl() : "");
                ps.setObject(7, id);
                ps.setObject(8, h.profileId());
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    return Optional.of(new Highlight(
                        id,
                        h.profileId(),
                        h.type(),
                        h.title(),
                        h.issuerOrVenue(),
                        h.dateOrYear(),
                        h.descriptionMarkdown(),
                        h.externalUrl(),
                        h.createdAt()
                    ));
                }
            }
            return Optional.empty();
        });
    }

    public boolean deleteHighlight(UUID id, UUID profileId) {
        return runWriteWithResult(conn -> {
            String sql = "DELETE FROM profile_highlights WHERE id = ? AND profile_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                ps.setObject(2, profileId);
                return ps.executeUpdate() > 0;
            }
        });
    }

    public List<Company> getCompanies() {
        return runRead(conn -> {
            String sql = "SELECT * FROM companies ORDER BY name ASC";
            List<Company> list = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapCompany(rs));
                }
            }
            return list;
        });
    }

    public Optional<Company> getCompany(String handleOrId) {
        return runRead(conn -> {
            String sql = "SELECT * FROM companies WHERE LOWER(handle) = LOWER(?) OR CAST(id AS VARCHAR) = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, handleOrId.replaceFirst("^@", ""));
                ps.setString(2, handleOrId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapCompany(rs));
                }
            }
            return Optional.empty();
        });
    }

    // ----------------------------------------------------------------
    // Graph Queries
    // ----------------------------------------------------------------

    public List<ConnectionNode> get1stDegreeConnections(UUID userId) {
        return runRead(conn -> {
            String sql = """
                SELECT p.*, e.created_at AS connected_since
                FROM graph_edges e
                JOIN profiles p ON p.id = (CASE WHEN e.source_id = ? THEN e.target_id ELSE e.source_id END)
                WHERE (e.source_id = ? OR e.target_id = ?)
                  AND e.edge_type = 'CONNECTED_TO'
                  AND e.status = 'ACCEPTED'
                ORDER BY p.display_name ASC
            """;
            List<ConnectionNode> nodes = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, userId);
                ps.setObject(2, userId);
                ps.setObject(3, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        nodes.add(new ConnectionNode(
                            (UUID) rs.getObject("id"),
                            rs.getString("handle"),
                            rs.getString("display_name"),
                            rs.getString("headline"),
                            rs.getString("avatar_url"),
                            1, 0,
                            rs.getString("matrix_handle"),
                            rs.getString("nostr_npub"),
                            rs.getString("simplex_address"),
                            rs.getString("session_id"),
                            rs.getString("email_contact"),
                            rs.getTimestamp("connected_since").toInstant()
                        ));
                    }
                }
            }
            return nodes;
        });
    }

    public List<ConnectionNode> get2ndDegreeConnections(UUID userId, int limit) {
        Set<UUID> directSet = new HashSet<>();
        directSet.add(userId);
        for (ConnectionNode n : get1stDegreeConnections(userId)) {
            directSet.add(n.id());
        }

        Map<UUID, Long> candidateMutualCounts = new HashMap<>();
        runRead(conn -> {
            for (UUID friendId : directSet) {
                if (friendId.equals(userId)) continue;
                String sql = """
                    SELECT (CASE WHEN source_id = ? THEN target_id ELSE source_id END) AS cand_id
                    FROM graph_edges
                    WHERE (source_id = ? OR target_id = ?)
                      AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
                """;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setObject(1, friendId);
                    ps.setObject(2, friendId);
                    ps.setObject(3, friendId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            UUID cand = (UUID) rs.getObject("cand_id");
                            if (!directSet.contains(cand)) {
                                candidateMutualCounts.put(cand, candidateMutualCounts.getOrDefault(cand, 0L) + 1);
                            }
                        }
                    }
                }
            }
            return null;
        });

        List<ConnectionNode> results = new ArrayList<>();
        candidateMutualCounts.entrySet().stream()
            .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
            .limit(limit > 0 ? limit : 20)
            .forEach(entry -> {
                getProfileById(entry.getKey()).ifPresent(p -> {
                    results.add(new ConnectionNode(
                        p.id(), p.handle(), p.displayName(), p.headline(), p.avatarUrl(),
                        2, entry.getValue(),
                        p.matrixHandle(), p.nostrNpub(), p.simplexAddress(), p.sessionId(), p.emailContact(),
                        null
                    ));
                });
            });

        return results;
    }

    public PathResult getShortestPath(UUID fromId, UUID toId) {
        if (fromId.equals(toId)) {
            Profile p = getProfileById(fromId).orElse(null);
            return new PathResult(0, List.of(fromId), List.of(p != null ? p.handle() : "unknown"));
        }

        Queue<List<UUID>> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();
        queue.add(List.of(fromId));
        visited.add(fromId);

        return runRead(conn -> {
            while (!queue.isEmpty()) {
                List<UUID> path = queue.poll();
                UUID current = path.get(path.size() - 1);

                if (current.equals(toId)) {
                    List<String> handles = path.stream()
                        .map(id -> getProfileById(id).map(Profile::handle).orElse("unknown"))
                        .toList();
                    return new PathResult(path.size() - 1, path, handles);
                }

                if (path.size() > 5) continue;

                String sql = """
                    SELECT (CASE WHEN source_id = ? THEN target_id ELSE source_id END) AS peer_id
                    FROM graph_edges
                    WHERE (source_id = ? OR target_id = ?)
                      AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
                """;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setObject(1, current);
                    ps.setObject(2, current);
                    ps.setObject(3, current);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            UUID peer = (UUID) rs.getObject("peer_id");
                            if (!visited.contains(peer)) {
                                visited.add(peer);
                                List<UUID> nextPath = new ArrayList<>(path);
                                nextPath.add(peer);
                                queue.add(nextPath);
                            }
                        }
                    }
                }
            }
            return new PathResult(-1, List.of(), List.of());
        });
    }

    public void connect(UUID src, UUID tgt) {
        runWrite(conn -> insertEdgeDirect(conn, src, "PERSON", tgt, "PERSON", "CONNECTED_TO", "ACCEPTED"));
    }

    public void follow(UUID src, UUID tgt, String tgtType) {
        runWrite(conn -> insertEdgeDirect(conn, src, "PERSON", tgt, tgtType != null ? tgtType : "PERSON", "FOLLOWS", "ACCEPTED"));
    }

    public void disconnect(UUID src, UUID tgt) {
        runWrite(conn -> {
            String sql = "DELETE FROM graph_edges WHERE (source_id = ? AND target_id = ?) OR (source_id = ? AND target_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, src);
                ps.setObject(2, tgt);
                ps.setObject(3, tgt);
                ps.setObject(4, src);
                ps.executeUpdate();
            }
        });
    }

    // ----------------------------------------------------------------
    // GDPR Functions
    // ----------------------------------------------------------------

    public GdprExport exportGdpr(UUID userId) {
        Profile p = getProfileById(userId).orElseThrow();
        List<Post> posts = getFeed(userId, "all", null, null, null, null, 1000, 0)
            .stream().filter(post -> userId.equals(post.authorId())).toList();

        List<GraphEdge> edges = runRead(conn -> {
            List<GraphEdge> list = new ArrayList<>();
            String sql = "SELECT * FROM graph_edges WHERE source_id = ? OR target_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, userId);
                ps.setObject(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new GraphEdge(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("source_id"),
                            rs.getString("source_type"),
                            (UUID) rs.getObject("target_id"),
                            rs.getString("target_type"),
                            rs.getString("edge_type"),
                            rs.getString("status"),
                            Map.of(),
                            rs.getDouble("weight"),
                            rs.getTimestamp("created_at").toInstant()
                        ));
                    }
                }
            }
            return list;
        });

        List<WorkExperience> experiences = getWorkExperiences(userId);
        List<Education> educations = getEducations(userId);
        List<Highlight> highlights = getHighlights(userId);

        GdprExport export = new GdprExport(
            Instant.now(),
            "GDPR Article 20 - Right to Data Portability",
            p, posts, edges, experiences, educations, highlights
        );

        // Store GDPR snapshot in Object Storage
        try {
            String snapshotJson = io.micronaut.serde.ObjectMapper.getDefault().writeValueAsString(export);
            objectStorageService.saveGdprSnapshot(userId, snapshotJson);
        } catch (Exception ignored) {}

        return export;
    }

    public void purgeUser(UUID userId) {
        runWrite(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM profile_highlights WHERE profile_id = ?")) {
                ps.setObject(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM educations WHERE profile_id = ?")) {
                ps.setObject(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM work_experiences WHERE profile_id = ?")) {
                ps.setObject(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM graph_edges WHERE source_id = ? OR target_id = ?")) {
                ps.setObject(1, userId);
                ps.setObject(2, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM post_reactions WHERE user_id = ?")) {
                ps.setObject(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM post_comments WHERE author_id = ?")) {
                ps.setObject(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM posts WHERE author_id = ?")) {
                ps.setObject(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM profiles WHERE id = ?")) {
                ps.setObject(1, userId);
                ps.executeUpdate();
            }
        });
        objectStorageService.purgeUserData(userId);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Post mapPost(ResultSet rs, UUID viewerId, Connection conn) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        boolean likedByMe = false;
        if (viewerId != null) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM post_reactions WHERE post_id = ? AND user_id = ?")) {
                ps.setObject(1, id);
                ps.setObject(2, viewerId);
                try (ResultSet rsl = ps.executeQuery()) {
                    likedByMe = rsl.next();
                }
            }
        }

        String tagsRaw = rs.getString("tags");
        List<String> tags = (tagsRaw != null && !tagsRaw.isBlank())
            ? List.of(tagsRaw.split(","))
            : List.of();

        return new Post(
            id,
            (UUID) rs.getObject("author_id"),
            rs.getString("author_type"),
            rs.getString("a_handle"),
            rs.getString("a_name"),
            rs.getString("a_avatar"),
            rs.getString("title"),
            rs.getString("body_markdown"),
            rs.getString("link_url"),
            rs.getString("link_title"),
            rs.getString("link_description"),
            rs.getString("link_image_url"),
            rs.getString("link_domain"),
            tags,
            rs.getBoolean("has_tables"),
            rs.getBoolean("has_images"),
            rs.getBoolean("has_links"),
            rs.getInt("like_count"),
            rs.getInt("comment_count"),
            likedByMe,
            "NETWORK",
            rs.getString("github_repo"),
            rs.getString("github_path"),
            rs.getString("github_commit_sha"),
            rs.getBoolean("is_self_hosted"),
            rs.getTimestamp("created_at").toInstant()
        );
    }

    private Profile mapProfile(ResultSet rs) throws SQLException {
        String skillsRaw = rs.getString("skills");
        List<String> skills = (skillsRaw != null && !skillsRaw.isBlank())
            ? List.of(skillsRaw.split(","))
            : List.of();

        return new Profile(
            (UUID) rs.getObject("id"),
            rs.getString("handle"),
            rs.getString("display_name"),
            rs.getString("headline"),
            rs.getString("bio_markdown"),
            rs.getString("avatar_url"),
            rs.getString("banner_url"),
            skills,
            rs.getString("matrix_handle"),
            rs.getString("nostr_npub"),
            rs.getString("simplex_address"),
            rs.getString("session_id"),
            rs.getString("email_contact"),
            rs.getString("pgp_fingerprint"),
            rs.getString("github_username") != null ? rs.getString("github_username") : "",
            rs.getString("github_content_repo") != null ? rs.getString("github_content_repo") : "",
            rs.getString("location") != null ? rs.getString("location") : "",
            rs.getTimestamp("created_at").toInstant()
        );
    }

    private Company mapCompany(ResultSet rs) throws SQLException {
        return new Company(
            (UUID) rs.getObject("id"),
            rs.getString("handle"),
            rs.getString("name"),
            rs.getString("tagline"),
            rs.getString("description_markdown"),
            rs.getString("website_url"),
            rs.getString("logo_url"),
            rs.getString("industry"),
            rs.getString("headquarters"),
            rs.getTimestamp("created_at").toInstant()
        );
    }

    private WorkExperience mapWorkExperience(ResultSet rs) throws SQLException {
        String skillsStr = rs.getString("skills");
        List<String> skills = (skillsStr != null && !skillsStr.isBlank())
                ? List.of(skillsStr.split(","))
                : List.of();

        return new WorkExperience(
            (UUID) rs.getObject("id"),
            (UUID) rs.getObject("profile_id"),
            rs.getString("title"),
            rs.getString("company_name"),
            (UUID) rs.getObject("company_id"),
            rs.getString("location") != null ? rs.getString("location") : "",
            rs.getString("start_date") != null ? rs.getString("start_date") : "",
            rs.getString("end_date") != null ? rs.getString("end_date") : "",
            rs.getBoolean("is_current"),
            rs.getString("description_markdown") != null ? rs.getString("description_markdown") : "",
            skills,
            rs.getTimestamp("created_at").toInstant()
        );
    }

    private Education mapEducation(ResultSet rs) throws SQLException {
        String skillsStr = rs.getString("skills");
        List<String> skills = (skillsStr != null && !skillsStr.isBlank())
                ? List.of(skillsStr.split(","))
                : List.of();

        return new Education(
            (UUID) rs.getObject("id"),
            (UUID) rs.getObject("profile_id"),
            rs.getString("institution_name"),
            rs.getString("degree"),
            rs.getString("field_of_study") != null ? rs.getString("field_of_study") : "",
            rs.getString("start_date") != null ? rs.getString("start_date") : "",
            rs.getString("end_date") != null ? rs.getString("end_date") : "",
            rs.getBoolean("is_current"),
            rs.getString("grade") != null ? rs.getString("grade") : "",
            rs.getString("activities_markdown") != null ? rs.getString("activities_markdown") : "",
            skills,
            rs.getTimestamp("created_at").toInstant()
        );
    }

    private Highlight mapHighlight(ResultSet rs) throws SQLException {
        return new Highlight(
            (UUID) rs.getObject("id"),
            (UUID) rs.getObject("profile_id"),
            rs.getString("type"),
            rs.getString("title"),
            rs.getString("issuer_or_venue") != null ? rs.getString("issuer_or_venue") : "",
            rs.getString("date_or_year") != null ? rs.getString("date_or_year") : "",
            rs.getString("description_markdown") != null ? rs.getString("description_markdown") : "",
            rs.getString("external_url") != null ? rs.getString("external_url") : "",
            rs.getTimestamp("created_at").toInstant()
        );
    }

    // ----------------------------------------------------------------
    // Job Announcements Methods (Sovereign Opportunities & Object Storage)
    // ----------------------------------------------------------------

    public List<JobAnnouncement> getJobAnnouncements(String department, String employmentType, String search, String tag, int limit, int offset) {
        return runRead(conn -> {
            StringBuilder sql = new StringBuilder("SELECT * FROM job_announcements WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (department != null && !department.isBlank() && !"ALL".equalsIgnoreCase(department)) {
                sql.append(" AND LOWER(department) = LOWER(?)");
                params.add(department.trim());
            }

            if (employmentType != null && !employmentType.isBlank() && !"ALL".equalsIgnoreCase(employmentType)) {
                sql.append(" AND LOWER(employment_type) = LOWER(?)");
                params.add(employmentType.trim());
            }

            if (tag != null && !tag.isBlank()) {
                sql.append(" AND LOWER(skills) LIKE LOWER(?)");
                params.add("%" + tag.trim() + "%");
            }

            if (search != null && !search.isBlank()) {
                sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(company_name) LIKE LOWER(?) OR LOWER(location) LIKE LOWER(?) OR LOWER(description_markdown) LIKE LOWER(?))");
                String q = "%" + search.trim() + "%";
                params.add(q);
                params.add(q);
                params.add(q);
                params.add(q);
            }

            sql.append(" ORDER BY featured DESC, created_at DESC LIMIT ? OFFSET ?");
            params.add(Math.min(limit > 0 ? limit : 50, 100));
            params.add(Math.max(offset, 0));

            List<JobAnnouncement> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapJobAnnouncement(rs));
                    }
                }
            }
            return list;
        });
    }

    public Optional<JobAnnouncement> getJobAnnouncementById(UUID id) {
        return runRead(conn -> {
            String sql = "SELECT * FROM job_announcements WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapJobAnnouncement(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }

    public JobAnnouncement saveJobAnnouncement(JobAnnouncement job) {
        UUID id = job.id() != null ? job.id() : UUID.randomUUID();
        JobAnnouncement toSave = job.withId(id);
        runWrite(conn -> {
            String sql = """
                INSERT INTO job_announcements (id, company_id, company_name, company_logo_url, title, location, employment_type, department, experience_level, salary_range, description_markdown, skills, apply_contact, storage_key, featured, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, toSave.id());
                ps.setObject(2, toSave.companyId());
                ps.setString(3, toSave.companyName());
                ps.setString(4, toSave.companyLogoUrl() != null ? toSave.companyLogoUrl() : "");
                ps.setString(5, toSave.title());
                ps.setString(6, toSave.location() != null ? toSave.location() : "");
                ps.setString(7, toSave.employmentType() != null ? toSave.employmentType() : "FULL_TIME");
                ps.setString(8, toSave.department() != null ? toSave.department() : "Engineering");
                ps.setString(9, toSave.experienceLevel() != null ? toSave.experienceLevel() : "Senior");
                ps.setString(10, toSave.salaryRange() != null ? toSave.salaryRange() : "");
                ps.setString(11, toSave.descriptionMarkdown() != null ? toSave.descriptionMarkdown() : "");
                ps.setString(12, String.join(",", toSave.skills() != null ? toSave.skills() : List.of()));
                ps.setString(13, toSave.applyContact() != null ? toSave.applyContact() : "");
                ps.setString(14, toSave.storageKey());
                ps.setBoolean(15, toSave.featured());
                ps.setTimestamp(16, Timestamp.from(toSave.createdAt()));
                ps.setTimestamp(17, Timestamp.from(Instant.now()));
                ps.executeUpdate();
            }
        });

        // Store markdown in object storage
        if (toSave.descriptionMarkdown() != null && !toSave.descriptionMarkdown().isBlank()) {
            objectStorageService.saveJobContent(toSave.id(), toSave.descriptionMarkdown());
        }

        return toSave;
    }

    public boolean deleteJobAnnouncement(UUID id) {
        boolean deleted = runWriteWithResult(conn -> {
            String sql = "DELETE FROM job_announcements WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                return ps.executeUpdate() > 0;
            }
        });
        if (deleted) {
            objectStorageService.deleteJobContent(id);
        }
        return deleted;
    }

    private JobAnnouncement mapJobAnnouncement(ResultSet rs) throws SQLException {
        String skillsStr = rs.getString("skills");
        List<String> skills = (skillsStr != null && !skillsStr.isBlank())
                ? List.of(skillsStr.split(","))
                : List.of();

        return new JobAnnouncement(
            (UUID) rs.getObject("id"),
            (UUID) rs.getObject("company_id"),
            rs.getString("company_name"),
            rs.getString("company_logo_url") != null ? rs.getString("company_logo_url") : "",
            rs.getString("title"),
            rs.getString("location") != null ? rs.getString("location") : "",
            rs.getString("employment_type") != null ? rs.getString("employment_type") : "FULL_TIME",
            rs.getString("department") != null ? rs.getString("department") : "Engineering",
            rs.getString("experience_level") != null ? rs.getString("experience_level") : "Senior",
            rs.getString("salary_range") != null ? rs.getString("salary_range") : "",
            rs.getString("description_markdown") != null ? rs.getString("description_markdown") : "",
            skills,
            rs.getString("apply_contact") != null ? rs.getString("apply_contact") : "",
            rs.getString("storage_key") != null ? rs.getString("storage_key") : "jobs/" + rs.getObject("id") + ".md",
            rs.getBoolean("featured"),
            rs.getTimestamp("created_at").toInstant()
        );
    }
}
