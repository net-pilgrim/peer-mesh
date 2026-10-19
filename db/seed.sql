-- ====================================================================
-- PeerMesh: Realistic Seed Data for Development & Testing
-- ====================================================================

-- Fixed UUIDs for predictable references in development
INSERT INTO profiles (id, handle, display_name, headline, bio_markdown, avatar_url, banner_url, skills, matrix_handle, nostr_npub, simplex_address, session_id, email_contact, pgp_fingerprint, github_username, github_content_repo, location)
VALUES 
(
    'a0000000-0000-0000-0000-000000000001',
    'satoshi_dev',
    'Elena Rostova',
    'Staff Distributed Systems Architect | Ex-Kernel Dev',
    'Passionate about peer-to-peer networks, zero-knowledge proofs, and sovereign decentralized communication. Building censorship-resistant infrastructure.',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80',
    ARRAY['Rust', 'PostgreSQL', 'Distributed Systems', 'Cryptography', 'Svelte'],
    '@elena:matrix.org',
    'npub1elena89xk23nd09f8asdnv0934jnkdf908234kjnds89',
    'simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Felena-relay.net',
    '05e89a421b5821c990bfa2384910248239081249',
    'elena@proton.me',
    '4A8F B832 9901 DC34 2200  E120 8943 F980 BB32 4410',
    'elenarostova',
    'elenarostova/zk-protocols',
    'Berlin, Germany'
),
(
    'a0000000-0000-0000-0000-000000000002',
    'marcus_poly',
    'Marcus Thorne',
    'Principal Database Engineer | Graph & Relational Specialist',
    'Obsessed with query planners, index strategies, and pushing PostgreSQL beyond typical relational limits. Speaker at PGConf.',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80',
    '',
    ARRAY['PostgreSQL 17', 'SQL:2023', 'Query Optimization', 'C', 'Micronaut'],
    '@marcus:matrix.org',
    'npub1marcus789234jhskdf89234njkdf90234hjskdf902',
    'simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Fmarcus-chat.net',
    '',
    'marcus@pg-specialists.io',
    '',
    'marcus_poly',
    'marcus-thorne/database-internals',
    'London, UK'
),
(
    'a0000000-0000-0000-0000-000000000003',
    'nora_zen',
    'Nora Lindqvist',
    'Lead Frontend Engineer & Interaction Designer',
    'Advocate for lightweight, zero-framework vanilla CSS and reactive frontends with Svelte. Hater of bloated 3-column UI feeds.',
    'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80',
    '',
    ARRAY['Svelte 5', 'Vanilla CSS', 'PWA', 'Web Performance', 'Accessibility'],
    '@nora:matrix.org',
    '',
    'simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Fnora-simplex.org',
    '05bb239012480923849012849012849012849012',
    'nora@zen-frontend.dev',
    '',
    '',
    '',
    'Stockholm, Sweden'
),
(
    'a0000000-0000-0000-0000-000000000004',
    'dev_kai',
    'Kai Zhang',
    'High-Performance Backend & Cloud Infrastructure Engineer',
    'Building microservices that sip RAM and handle millions of RPS. Micronaut, Rust, and Linux tuning.',
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80',
    '',
    ARRAY['Micronaut', 'Java 25', 'GraalVM Native', 'Docker', 'Linux Kernel'],
    '@kai:matrix.org',
    'npub1kaizhang098234jksdf908234njksdf980234jksdf',
    '',
    '',
    'kai.infra@pm.me',
    '',
    '',
    '',
    'Singapore'
),
(
    'a0000000-0000-0000-0000-000000000005',
    'dr_andrei_muresan',
    'Dr. Andrei Mureșan',
    'Chief of Cardiology & Emergency Doctor | Timișoara County Emergency Clinical Hospital',
    'Consultant Cardiologist and Emergency Medicine Specialist in Timișoara with 15+ years of clinical experience. Focused on acute coronary syndrome interventions and modernizing medical data networks.',
    'https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=200&auto=format&fit=crop&q=80',
    '',
    ARRAY['Cardiology', 'Emergency Medicine', 'Echocardiography', 'Internal Medicine', 'Clinical Triage'],
    '@andrei.muresan:matrix.org',
    '',
    'simplex:https://simplex.chat/contact#/?v=2-4&smp=smp%3A%2F%2Fdr-muresan.org',
    '05ee892348092384902384901284901284901284',
    'andrei.muresan@hosptim.ro',
    '',
    '',
    '',
    'Timișoara, Romania'
),
(
    'a0000000-0000-0000-0000-000000000006',
    'dr_sorina_popa',
    'Dr. Sorina Popa',
    'Pediatric Surgeon & Medical Doctor | Timișoara Children''s Hospital',
    'Pediatric surgeon specializing in minimally invasive and robotic laparoscopy. Passionate about evidence-based pediatric surgery and open clinical research across western Romania.',
    'https://images.unsplash.com/photo-1594824813583-16a7f34beee3?w=200&auto=format&fit=crop&q=80',
    '',
    ARRAY['Pediatric Surgery', 'Robotic Laparoscopy', 'Pediatrics', 'Neonatal Surgery', 'Clinical Research'],
    '@sorina.popa:matrix.org',
    'npub1sorinapopa98234jksdf908234njksdf980234',
    '',
    '',
    'sorina.popa@pediatrie-tm.ro',
    '',
    '',
    '',
    'Timișoara, Romania'
),
(
    'a0000000-0000-0000-0000-000000000007',
    'dr_radu_ionescu',
    'Dr. Radu Ionescu',
    'Neurologist & Neuro-Critical Care Doctor | Victor Babeș University Timișoara',
    'Associate Professor of Neurology and attending neuro-intensivist in Timișoara. Specializing in acute ischemic stroke treatment and neuro-rehabilitation protocols.',
    'https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=200&auto=format&fit=crop&q=80',
    '',
    ARRAY['Neurology', 'Neuroscience', 'Critical Care', 'Stroke Intervention', 'EEG Diagnostics'],
    '@radu.ionescu:matrix.org',
    '',
    '',
    '05ab239840192384091283049182034918230948',
    'radu.ionescu@umft.ro',
    '',
    '',
    '',
    'Timișoara, Romania'
)
ON CONFLICT (id) DO NOTHING;

-- Companies
INSERT INTO companies (id, handle, name, tagline, description_markdown, website_url, logo_url, industry, headquarters)
VALUES
(
    'b0000000-0000-0000-0000-000000000001',
    'sovereign-mesh',
    'Sovereign Mesh Technologies',
    'Decentralized identity, zero-knowledge verification, and private networking protocols.',
    'We build open-source tools that empower individuals and organizations to collaborate without surveillance capitalism or vendor lock-in.',
    'https://sovereign-mesh.org',
    'https://images.unsplash.com/photo-1563986768609-322da13575f3?w=200&auto=format&fit=crop&q=80',
    'Cybersecurity & Decentralized Protocols',
    'Stockholm, Sweden'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'lean-stack',
    'LeanStack Labs',
    'Performance engineering for ultra-cheap, ultra-efficient cloud systems.',
    'Helping tech teams ditch Kubernetes bloat and $20,000/mo cloud bills in favor of single-box bare metal and Micronaut/Rust native stacks.',
    'https://leanstack.tech',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200&auto=format&fit=crop&q=80',
    'Developer Tools & Cloud Infrastructure',
    'Zurich, Switzerland'
),
(
    'b0000000-0000-0000-0000-000000000003',
    'spitalul_judetean_timisoara',
    'Timișoara County Emergency Clinical Hospital',
    'Major tertiary university hospital, trauma center, cardiology, and university surgical clinics in western Romania.',
    'Serving over 450,000 regional consultations annually with 1,100 beds, accredited Level 1 trauma and acute cardiac care units.',
    'https://hosptim.ro',
    'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=200&auto=format&fit=crop&q=80',
    'Healthcare & Hospitals',
    'Timișoara, Romania'
)
ON CONFLICT (id) DO NOTHING;

-- Graph Edges: Connections, Follows, Affiliations
INSERT INTO graph_edges (source_id, source_type, target_id, target_type, edge_type, status, properties)
VALUES
-- Elena is connected to Marcus (1st degree)
('a0000000-0000-0000-0000-000000000001', 'PERSON', 'a0000000-0000-0000-0000-000000000002', 'PERSON', 'CONNECTED_TO', 'ACCEPTED', '{"notes": "Collaborated on PG distributed storage"}'::jsonb),
-- Marcus is connected to Kai (1st degree to Marcus, making Kai a 2nd degree connection to Elena!)
('a0000000-0000-0000-0000-000000000002', 'PERSON', 'a0000000-0000-0000-0000-000000000004', 'PERSON', 'CONNECTED_TO', 'ACCEPTED', '{"notes": "Database driver optimization"}'::jsonb),
-- Elena is connected to Nora (1st degree)
('a0000000-0000-0000-0000-000000000001', 'PERSON', 'a0000000-0000-0000-0000-000000000003', 'PERSON', 'CONNECTED_TO', 'ACCEPTED', '{"notes": "Frontend & protocol design"}'::jsonb),
-- Elena works at Sovereign Mesh
('a0000000-0000-0000-0000-000000000001', 'PERSON', 'b0000000-0000-0000-0000-000000000001', 'COMPANY', 'WORKS_AT', 'ACCEPTED', '{"role": "Staff Architect", "started": "2024-01-01"}'::jsonb),
-- Kai works at LeanStack Labs
('a0000000-0000-0000-0000-000000000004', 'PERSON', 'b0000000-0000-0000-0000-000000000002', 'COMPANY', 'WORKS_AT', 'ACCEPTED', '{"role": "Lead Systems Engineer", "started": "2023-06-15"}'::jsonb),
-- Elena follows LeanStack Labs
('a0000000-0000-0000-0000-000000000001', 'PERSON', 'b0000000-0000-0000-0000-000000000002', 'COMPANY', 'FOLLOWS', 'ACCEPTED', '{}'::jsonb),
-- Nora follows Sovereign Mesh
('a0000000-0000-0000-0000-000000000003', 'PERSON', 'b0000000-0000-0000-0000-000000000001', 'COMPANY', 'FOLLOWS', 'ACCEPTED', '{}'::jsonb),
-- Dr. Andrei works at Spitalul Judetean Timisoara
('a0000000-0000-0000-0000-000000000005', 'PERSON', 'b0000000-0000-0000-0000-000000000003', 'COMPANY', 'WORKS_AT', 'ACCEPTED', '{"role": "Chief of Cardiology"}'::jsonb),
-- Dr. Andrei is connected to Dr. Sorina
('a0000000-0000-0000-0000-000000000005', 'PERSON', 'a0000000-0000-0000-0000-000000000006', 'PERSON', 'CONNECTED_TO', 'ACCEPTED', '{"notes": "Hospital surgical board"}'::jsonb),
-- Dr. Andrei is connected to Dr. Radu
('a0000000-0000-0000-0000-000000000005', 'PERSON', 'a0000000-0000-0000-0000-000000000007', 'PERSON', 'CONNECTED_TO', 'ACCEPTED', '{"notes": "Acute emergency neuro-cardiac triage"}'::jsonb),
-- Elena is connected to Dr. Andrei (medical tech protocol collaboration)
('a0000000-0000-0000-0000-000000000001', 'PERSON', 'a0000000-0000-0000-0000-000000000005', 'PERSON', 'CONNECTED_TO', 'ACCEPTED', '{"notes": "Decentralized patient records research"}'::jsonb)
ON CONFLICT (source_id, target_id, edge_type) DO NOTHING;

-- Unified Posts (Demonstrating Markdown, Tables, Images, and Unified Links)
INSERT INTO posts (id, author_id, author_type, title, body_markdown, link_url, link_title, link_description, link_image_url, link_domain, tags, like_count, comment_count, created_at)
VALUES
(
    'c0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001',
    'PERSON',
    'Why We Replaced Corporate Chat with Decentralized Protocol Deep-Links',
    E'On traditional corporate platforms, direct messaging is a massive liability:\n- Platforms read your private communications for ad-targeting.\n- Centralized databases are prime targets for leaks and regulatory overreach.\n- Massive infrastructure costs just to host chat servers and notification pipelines.\n\nHere is our comparison matrix:\n\n| Feature | Traditional Corporate Silo | PeerMesh Decentralized Model |\n| :--- | :--- | :--- |\n| **Data Storage** | Centralized server plaintext/keys | 0 server storage (on-device only) |\n| **GDPR Exposure** | High (must process all DM logs) | Absolute Zero PII liability |\n| **Protocol** | Proprietary walled garden | Matrix, SimpleX, Nostr, Session |\n| **Platform Cost** | $thousands/month for live chat | **$0.00** server messaging overhead |\n\nWhen someone wants to message me, they click my Matrix address or SimpleX link and we talk end-to-end encrypted directly.\n\nWhat are your thoughts on decoupling social identity from messaging?',
    NULL, NULL, NULL, NULL, NULL,
    ARRAY['Privacy', 'Decentralized', 'GDPR', 'Matrix', 'Architecture'],
    14, 3,
    NOW() - INTERVAL '2 hours'
),
(
    'c0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000002',
    'PERSON',
    'PostgreSQL Recursive Graph Traversals vs Neo4j in Production',
    E'Many teams immediately reach for dedicated graph databases like Neo4j as soon as they need 1st/2nd degree connection lookups or social network feeds.\n\nWith modern PostgreSQL and recursive CTEs or SQL:2023 `GRAPH_TABLE`, you can execute multi-hop graph queries directly on your relational tables with sub-millisecond execution.\n\n```sql\nWITH RECURSIVE network_path AS (\n    SELECT source_id, target_id, 1 AS depth\n    FROM graph_edges WHERE source_id = :start_user\n    UNION\n    SELECT e.source_id, e.target_id, np.depth + 1\n    FROM graph_edges e JOIN network_path np ON e.source_id = np.target_id\n    WHERE np.depth < 3\n)\nSELECT * FROM network_path;\n```\n\nCheck out the benchmark whitepaper below:',
    'https://postgresql.org/docs/current/queries-with.html',
    'PostgreSQL Documentation: Queries with WITH (Recursive CTEs)',
    'Detailed reference on writing recursive queries and graph traversal optimizations in PostgreSQL.',
    'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600&auto=format&fit=crop&q=80',
    'postgresql.org',
    ARRAY['PostgreSQL', 'Databases', 'Graph', 'Performance', 'SQL'],
    28, 7,
    NOW() - INTERVAL '5 hours'
),
(
    'c0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000003',
    'PERSON',
    'Death to the 3-Column LinkedIn Layout: Designing for Focus and Density',
    E'Why does the standard LinkedIn desktop view look like a billboard highway from 2008?\n\n1. **Column 1**: Tiny profile card with vanity metrics nobody asked for.\n2. **Column 2**: Squeezed central stream surrounded by distractions.\n3. **Column 3**: Clickbait news, sponsored recommendation widgets, and empty margins.\n\nIn our Svelte implementation, we chose:\n- **No CSS frameworks**: Handcrafted semantic CSS with CSS variables (`--bg`, `--accent`, `--border`).\n- **Client-heavy state**: Fast local caching with zero server render penalties.\n- **Fluid stream**: 720px focused reader that expands to split-view when inspecting deep discussions or graph connections.\n\nHere is how clean reading can feel when marketing widgets are eliminated:\n\n![Clean Interface Preview](https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800&auto=format&fit=crop&q=80)\n\nZero Tailwind. Zero bloat. Pure semantic web.',
    'https://svelte.dev',
    'Svelte: Cybernetically enhanced web apps',
    'Write high-performance reactive applications with zero virtual-DOM overhead.',
    'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&auto=format&fit=crop&q=80',
    'svelte.dev',
    ARRAY['Svelte', 'UI', 'UX', 'WebPerf', 'CSS'],
    42, 11,
    NOW() - INTERVAL '1 day'
),
(
    'c0000000-0000-0000-0000-000000000004',
    'b0000000-0000-0000-0000-000000000002',
    'COMPANY',
    'LeanStack Announcement: Slashing JVM Footprint with Micronaut & GraalVM',
    E'We are thrilled to publish our latest benchmarks running Micronaut microservices on bare-metal:\n\n| Runtime | RSS Memory | Cold Startup Time | Max RPS (1 vCPU) |\n| :--- | :--- | :--- | :--- |\n| Traditional Spring Boot | ~350 MB | 3.8s | 8,200 req/s |\n| **Micronaut JIT (Java 25 Loom)** | **~58 MB** | **0.6s** | **24,500 req/s** |\n| **Micronaut GraalVM Native** | **~24 MB** | **18 ms** | **29,000 req/s** |\n\nYou do NOT need costly multi-thousand dollar cloud bills to run performant professional networks. With the right stack, a single $5/month VPS can easily serve 50,000 active members.\n\nRead our technical deep-dive on modern compiled runtimes.',
    'https://micronaut.io',
    'Micronaut Framework - Modern, JVM-Based, Full-Stack Framework',
    'Build modular, easily testable microservice and serverless applications with minimal memory footprint.',
    'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80',
    'micronaut.io',
    ARRAY['Micronaut', 'Java25', 'GraalVM', 'CloudCost', 'Performance'],
    65, 9,
    NOW() - INTERVAL '2 days'
),
(
    'c0000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000005',
    'PERSON',
    'Emergency Cardiology Triage in Timișoara: Open Protocols & Response Times',
    E'In acute coronary syndrome (ACS), every minute saved preserves viable myocardial tissue. Over the past 12 months, our medical teams in Timișoara have overhauled pre-hospital triage protocols.\n\nHere is our benchmark performance matrix at the Timișoara Emergency Hospital:\n\n| Protocol Stage | Standard Regional Target | Timișoara Emergency Unit (2026) | Outcome Improvement |\n| :--- | :--- | :--- | :--- |\n| **Door-to-ECG** | < 10 minutes | **4.2 minutes** | -58% diagnostic delay |\n| **Door-to-Balloon (PCI)** | < 90 minutes | **56 minutes** | +34% myocardial salvage |\n| **Tele-ECG Triage** | In-transit review | **100% active units** | Pre-hospital activation |\n\nDecentralized physician networks and open collaboration allow doctors in Timișoara to exchange critical clinical insights rapidly without corporate platform noise.',
    'https://hosptim.ro',
    'Timișoara Emergency Clinical Hospital - Acute Cardiology Protocol',
    'Clinical triage metrics and emergency cardiology guidelines for the Timișoara metropolitan region.',
    'https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=600&auto=format&fit=crop&q=80',
    'hosptim.ro',
    ARRAY['Medicine', 'Cardiology', 'Timișoara', 'Healthcare', 'EmergencyCare'],
    38, 8,
    NOW() - INTERVAL '4 hours'
)
ON CONFLICT (id) DO NOTHING;

-- Work Experiences (Demonstrating Career History & Clinical Affiliations)
INSERT INTO work_experiences (id, profile_id, title, company_name, company_id, location, start_date, end_date, is_current, description_markdown, skills)
VALUES
(
    'e0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000005',
    'Chief of Cardiology & Emergency Doctor',
    'Timișoara County Emergency Clinical Hospital',
    'b0000000-0000-0000-0000-000000000003',
    'Timișoara, Romania',
    '2018-03',
    '',
    TRUE,
    E'Directing acute myocardial infarction triage, primary PCI cath-lab interventions, and clinical quality protocols across the emergency department.\n\n- Reduced median door-to-balloon time to **56 minutes** (-38% vs national target).\n- Supervised 1,400+ diagnostic and emergency coronary angiographies.\n- Introduced pre-hospital 12-lead tele-ECG triage across county ambulance services.',
    ARRAY['Cardiology', 'Emergency Medicine', 'Clinical Triage', 'Interventional Cardiology', 'Echocardiography']
),
(
    'e0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000005',
    'Cardiology Fellow & Clinical Registrar',
    'Victor Babeș University Cardiology Clinic',
    NULL,
    'Timișoara, Romania',
    '2012-10',
    '2018-02',
    FALSE,
    E'Completed 5-year specialty residency training in clinical cardiology and acute cardiovascular intensive care.',
    ARRAY['Internal Medicine', 'Cardiology', 'Intensive Care']
),
(
    'e0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000006',
    'Pediatric Surgeon & Medical Doctor',
    'Louis Țurcanu Children''s Clinical Hospital',
    NULL,
    'Timișoara, Romania',
    '2019-06',
    '',
    TRUE,
    E'Consultant pediatric surgeon specializing in robotic laparoscopy and neonatal surgical anomalies.\n\n- Performed over 650 pediatric laparoscopic procedures.\n- Co-investigator on multicenter randomized trial for neonatal intestinal atresia protocols.',
    ARRAY['Pediatric Surgery', 'Robotic Laparoscopy', 'Neonatal Surgery', 'Clinical Research']
),
(
    'e0000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000007',
    'Neurologist & Neuro-Critical Care Doctor',
    'Victor Babeș University of Medicine and Pharmacy',
    NULL,
    'Timișoara, Romania',
    '2016-09',
    '',
    TRUE,
    E'Associate Professor and attending neuro-intensivist. Directing rapid stroke response and mechanical thrombectomy assessment protocols.',
    ARRAY['Neurology', 'Neuroscience', 'Critical Care', 'Stroke Intervention']
),
(
    'e0000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000001',
    'Staff Distributed Systems Architect',
    'Sovereign Mesh',
    'b0000000-0000-0000-0000-000000000001',
    'Berlin, Germany',
    '2022-04',
    '',
    TRUE,
    E'Designing zero-knowledge cryptographic authentication protocols and decentralized peer-to-peer gossip layers.',
    ARRAY['Rust', 'Cryptography', 'Distributed Systems', 'Zero-Knowledge']
),
(
    'e0000000-0000-0000-0000-000000000006',
    'a0000000-0000-0000-0000-000000000001',
    'Senior Linux Kernel Engineer',
    'RedCore Systems',
    NULL,
    'Munich, Germany',
    '2018-02',
    '2022-03',
    FALSE,
    E'Developed and maintained in-kernel eBPF socket filters and high-performance packet routing drivers.',
    ARRAY['C', 'Linux Kernel', 'eBPF', 'Networking']
),
(
    'e0000000-0000-0000-0000-000000000007',
    'a0000000-0000-0000-0000-000000000002',
    'Principal Database Engineer',
    'LeanStack Labs',
    'b0000000-0000-0000-0000-000000000002',
    'London, UK',
    '2021-08',
    '',
    TRUE,
    E'Leading PostgreSQL query optimization engine, SQL:2023 Property Graph implementations, and microsecond relational partitioning.',
    ARRAY['PostgreSQL', 'SQL:2023', 'Query Optimization', 'C', 'Micronaut']
)
ON CONFLICT (id) DO NOTHING;

