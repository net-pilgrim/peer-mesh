# PeerMesh (Clean, Privacy-First Professional Network)

> A modern, lightweight LinkedIn alternative built for speed, sovereign privacy, native graph relations, and distraction-free communication.

---

## 🎯 Architectural Philosophy & Core Decisions

| LinkedIn Weakness | PeerMesh Solution |
| :--- | :--- |
| **Cluttered 3-Column Layout** | **Focused Fluid Workspace:** Clean left dock (240px) + readable 740px content column + collapsible contextual inspector. Zero promotional sidebars or sponsored news widgets. |
| **Artificial Post vs Link Split** | **Unified Publication Model:** Every post is a unified document supporting CommonMark Markdown, interactive tables, images, and embedded rich link cards. All fully searchable via PostgreSQL full-text search. |
| **Invasive In-App Messaging Silo** | **Decentralized Messaging Outside the Platform:** Zero private message storage on servers. 1-click deep-links to **Matrix**, **SimpleX Chat**, **Nostr (NIP-04/17)**, **Session**, or **PGP/Email**. Zero surveillance, zero DM liability, 100% GDPR compliant. |
| **Massive PII Collection** | **GDPR by Design:** Pseudonymous handles, no mandatory phone numbers, no tracking cookies. Built-in 1-click **Article 20 Data Portability Export** (JSON) and **Article 17 Right-to-be-Forgotten Hard Purge**. |
| **Expensive Dedicated Graph DBs** | **PostgreSQL 18+ (18/19) Graph Engine:** Uses SQL:2023 `GRAPH_TABLE` Property Graph Queries & optimized recursive CTEs for 1st-degree neighbors, 2nd-degree mutual connections ("People you may know"), and shortest path network traversals. |
| **Bloated Frontend & Server Load** | **Svelte 5 + Pure Vanilla CSS:** Zero CSS frameworks (no Tailwind, no Bootstrap). 11.6 kB total CSS (2.85 kB gzipped). Heavy client-side processing (Canvas image resizing, local caching). |
| **Heavy Cloud Bills & Slow Startups** | **Micronaut (Java 25 Virtual Threads):** High-throughput, non-reflection AOT framework with ~50MB RAM footprint, 1.1s startup time, runnable on a $3-$5/mo VPS or as a GraalVM native binary. |
| **App Store Lock-In** | **Installable PWA:** Web App Manifest + Service Worker shell caching with offline capabilities. |

---

## 🐳 Running with Docker Compose (Recommended)

The entire platform (PostgreSQL 18+ database, Micronaut Java 25 backend, and Svelte 5 Nginx frontend) runs with a single command:

```bash
docker compose up --build
```

### Services Started:

| Service | Image / Build | Port | Description |
| :--- | :--- | :--- | :--- |
| **frontend** | Multi-stage Node 22 + Nginx Alpine | `http://localhost:3000` / `http://localhost:5173` | Serves Svelte 5 PWA with Gzip compression and reverse-proxies `/api` to backend |
| **backend** | Multi-stage Temurin 25 JDK + JRE Alpine | `http://localhost:8085` | Micronaut REST service with Loom virtual threads, Redis session store, and Dex OIDC integration |
| **dex** | `ghcr.io/dexidp/dex:v2.41.1` | `http://localhost:5556` | OpenID Connect Identity Provider with provisioned network personas & password DB |
| **redis** | `redis:7-alpine` | `6379` | High-performance session cache & auth token storage with automated fallback |
| **postgres** | `postgres:18-alpine` (or `postgres:19`) | `5432` | PostgreSQL 18+ with auto-initialized schema, SQL:2023 Property Graph, and seed data |

### 🔐 Dex OpenID Connect (OIDC) Authentication

PeerMesh uses **Dex** as its federated identity provider, exposed on port `5556`. User sessions are stored in **Redis** (port `6379`).

#### Provisioned Personas in Dex (Default password: `password`):

| Persona | Email | Handle | Role |
| :--- | :--- | :--- | :--- |
| **Elena Rostova** | `elena@proton.me` | `@elena_rostova` | Staff Distributed Systems Architect |
| **Dr. Andrei Mureșan** | `andrei.muresan@hosptim.ro` | `@dr_andrei_muresan` | Chief of Cardiology & Emergency Doctor (Timișoara) |
| **Dr. Radu Ionescu** | `radu.ionescu@umft.ro` | `@dr_radu_ionescu` | Neurologist & Stroke Lead (Timișoara) |
| **Dr. Sorina Popa** | `sorina.popa@pediatrie-tm.ro` | `@dr_sorina_popa` | Pediatric Surgeon (Timișoara) |
| **Marcus Thorne** | `marcus@pg-specialists.io` | `@marcus_poly` | Principal Database Engineer |
| **Nora Lindqvist** | `nora@zen-frontend.dev` | `@nora_zen` | Lead Frontend Engineer & Interaction Designer |
| **Kai Zhang** | `kai.infra@pm.me` | `@dev_kai` | High-Performance Cloud Infrastructure Engineer |

Users can authenticate via the standard Dex browser OIDC authorization code flow or 1-click quick login in the UI.

To stop the services:
```bash
docker compose down
```

To wipe the database and Redis volumes and re-seed from scratch:
```bash
docker compose down -v
docker compose up --build
```

---

## 💻 Local Development (Without Docker)

You can also run both services locally without Docker:

### 1. Backend (Micronaut + Java 25)
The backend includes a zero-dependency in-memory PostgreSQL-compatible mode with pre-seeded data:
```bash
cd backend
./gradlew run
```
*Listens on `http://localhost:8085` (starts in ~1.1s).*

To run backend tests:
```bash
cd backend
./gradlew test
```

### 2. Frontend (Svelte 5 + Vite)
```bash
cd frontend
npm install
npm run dev
```
*Open `http://localhost:5173` in your browser (proxies `/api` to port 8085).*

---

## 📂 Repository Structure

```
linkedin/
├── backend/                  # Micronaut (Java 25) Backend Service
│   ├── Dockerfile            # Multi-stage Dockerfile (Temurin 25 JDK -> JRE Noble)
│   ├── src/main/java/com/network/
│   │   ├── controller/       # FeedController, ProfileController, CompanyController, GraphController, GdprController
│   │   ├── model/            # Profile, Company, Post, Comment, GraphEdge, ConnectionNode, PathResult
│   │   └── repository/       # NetworkRepository (HikariCP, ConnectionOperations, SQL Graph Queries)
│   ├── src/main/resources/   # application.yml (H2 dev fallback), application-postgres.yml (Postgres 17+)
│   └── build.gradle          # Micronaut 5.x, Hikari, Serde Jackson, Postgres, GraalVM native
├── frontend/                 # Svelte 5 PWA
│   ├── Dockerfile            # Multi-stage Dockerfile (Node 22 -> Nginx Alpine)
│   ├── nginx.conf            # Nginx config with Gzip, PWA caching, and /api reverse proxy
│   ├── public/               # manifest.webmanifest, sw.js (Service Worker)
│   ├── src/
│   │   ├── lib/
│   │   │   ├── api.js                 # API client with localStorage/IndexedDB caching
│   │   │   ├── UnifiedComposer.svelte # Markdown composer + Table builder + Image/Link cards
│   │   │   ├── PostCard.svelte        # Post card with rendered tables, links & reactions
│   │   │   ├── GraphExplorer.svelte   # 1st/2nd degree mutuals & shortest path finder
│   │   │   ├── DecentralizedModal.svelte # Matrix/SimpleX/Nostr protocol launcher
│   │   │   ├── CompaniesView.svelte   # Organizations directory & follow actions
│   │   │   └── GdprModal.svelte       # 1-click JSON export & permanent account purge
│   │   ├── app.css           # Pure Vanilla CSS design system (Dark/Light mode)
│   │   ├── App.svelte        # Root reactive shell & sidebar dock
│   │   └── main.js
│   └── vite.config.js        # Vite + Svelte with /api proxy to backend
├── db/
│   ├── schema.sql            # PostgreSQL 18+ (18/19) schema with graph edges & tsvector search
│   ├── graph_queries.sql     # SQL:2023 Property Graph & Recursive CTE traversal functions
│   └── seed.sql              # Realistic seed data (Elena, Marcus, Nora, Kai, Sovereign Mesh, LeanStack)
├── docker-compose.yml        # Orchestrates PostgreSQL 18+, Backend, and Frontend
└── LICENSE                   # GNU Affero General Public License v3.0 (AGPL-3.0)
```

---

## 🔍 Key Feature Walkthrough

### 1. Unified Posts: Markdown, Tables & Links
- Compose markdown posts with headings, bullet lists, blockquotes, and code blocks.
- Click **📊 Table** in the composer to insert configurable markdown tables.
- Click **🔗 Link Card** to attach rich metadata (preview image, domain pill, title, description) unified with the text.
- Filter feed with instant chips: `[All]`, `[Organizations]`, `[📊 Has Tables]`, `[🔗 Has Links]`, `[🖼️ Has Images]`.

### 2. Decentralized Messaging Outside the Platform
- On any post or profile card, click **Direct Connect**.
- Generates 1-click launch URLs for:
  - **Matrix**: `https://matrix.to/#/@handle:matrix.org`
  - **SimpleX Chat**: One-click invite link (zero user identifier protocol)
  - **Nostr**: `nostr:npub1...` (compatible with Damus, Amethyst, Snort)
  - **Session**: Onion-routed Session ID
  - **Email / PGP**: Mailto link with public OpenPGP key fingerprint
- **Zero server overhead:** Our servers never see, transport, or store private conversations.

### 3. Native Graph Traversals
- Navigate to **Network Graph**:
  - **1st-Degree:** Direct connections with mutual connection timestamp.
  - **2nd-Degree Suggestions:** "People You May Know" ranked by count of mutual connections.
  - **Shortest Path Traversal:** Select any member to discover the shortest hop distance through the relational graph (e.g., `Elena -> Marcus -> Kai: 2 hops`).

### 4. GDPR Article 20 & Article 17 Sovereignty
- Navigate to **Privacy & GDPR**:
  - **1-Click Export:** Instantly downloads a full `.json` archive of your profile, posts, reactions, and network edges.
  - **1-Click Hard Purge:** Permanently cascades deletion of all your user records from the database.

### 5. 🐙 Developer Self-Hosting via GitHub Integration
Developers can host their own articles and technical notes directly inside public Git repositories or Gists, retaining full ownership of their content:
- **Zero Token Architecture**: No personal access tokens or credentials are stored on PeerMesh servers. Content is syndicated via standard raw HTTPS endpoints.
- **Frontmatter & Heading Parsing**: PeerMesh parses YAML frontmatter (`title`, `tags`, `link`) or extracts the top `# Heading` automatically.
- **Verifiable Git Provenance**: Sourced posts display a `[🐙 Git-Backed: repo/path @commit]` badge linking directly to the source repository on GitHub.
- **Two Syndication Modes**:
  1. **Load into Editor**: Imports remote markdown into the Unified Composer for review or editing before posting.
  2. **Direct Syndication**: Instantly publishes a Git-backed post tagged with repository metadata.
- **1-Click Re-Sync ("🔄 Pull Git Update")**: When an author pushes updates to their GitHub repository, clicking **Pull Git Update** pulls and updates the post in PeerMesh without losing likes, reactions, or comments.

### 6. 🔍 Smart Professional Network Discovery (e.g. "doctors in timisoara")
PeerMesh provides intelligent, privacy-preserving professional search without surveillance or tracking:
- **Natural Language Intent Detection**: Queries like `"doctors in timisoara"` or `"surgeons at timisoara"` automatically decompose into structured intent (Role: `"doctor"` + Location: `"timisoara"`).
- **Diacritics & Stemming Resilience**: Strips and normalizes accents (`Timișoara` vs `timisoara`) and performs singularization (`doctors` -> `doctor`, `surgeons` -> `surgeon`).
- **Domain Synonym Expansion**: Searching `"doctor"` automatically discovers cardiologists, neurologists, pediatric surgeons, physicians, and medical specialists.
- **Unified Tri-Modal Results**: Returns matching:
  1. **Specialists & Professionals**: Detailed profile cards with location badges, clinical skills, direct decentralized messaging deep-links, and connection actions.
  2. **Verified Organizations & Clinics**: Regional hospitals and healthcare hubs with follow actions and verified domains.
  3. **Unified Posts & Clinical Research**: Articles and publications discussing triage protocols, clinical outcomes, and open medical networks.
- **Privacy First (GDPR Compliant)**: Only city/country granularity is stored. Zero GPS coordinates, zero device geofencing, zero search history profiling.

---

## 📄 License

This project is open-source software licensed under the **GNU Affero General Public License v3.0** (GNU AGPLv3).

See the [LICENSE](file:///home/dsv/work/linkedin/LICENSE) file for the full license terms.


