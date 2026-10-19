-- ====================================================================
-- PeerMesh: Flyway Migration V2 - Seed Data
-- Personas, Companies, Graph Connections, Posts, Work Experiences, and Educations
-- ====================================================================

-- 1. Profiles
INSERT INTO profiles (id, handle, display_name, headline, bio_markdown, avatar_url, banner_url, skills, matrix_handle, nostr_npub, simplex_address, session_id, email_contact, pgp_fingerprint, github_username, github_content_repo, location)
VALUES 
(
    'a0000000-0000-0000-0000-000000000001',
    'elena_rostova',
    'Elena Rostova',
    'Staff Distributed Systems Architect | Ex-Kernel Dev',
    'Passionate about peer-to-peer networks, zero-knowledge proofs, and sovereign decentralized communication. Building censorship-resistant infrastructure.',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80',
    'Rust,PostgreSQL,Distributed Systems,Cryptography,Svelte',
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
    'PostgreSQL 17,SQL:2023,Query Optimization,C,Micronaut',
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
    'Svelte 5,Vanilla CSS,PWA,Web Performance,Accessibility',
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
    'Micronaut,Java 25,GraalVM Native,Docker,Linux Kernel',
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
    'Cardiology,Emergency Medicine,Echocardiography,Internal Medicine,Clinical Triage',
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
    'Pediatric Surgery,Robotic Laparoscopy,Pediatrics,Neonatal Surgery,Clinical Research',
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
    'Neurology,Neuroscience,Critical Care,Stroke Intervention,EEG Diagnostics',
    '@radu.ionescu:matrix.org',
    '',
    '',
    '05ab239840192384091283049182034918230948',
    'radu.ionescu@umft.ro',
    '',
    '',
    '',
    'Timișoara, Romania'
);

-- 2. Companies / Organizations
INSERT INTO companies (id, handle, name, tagline, description_markdown, website_url, logo_url, industry, headquarters)
VALUES
(
    'b0000000-0000-0000-0000-000000000001',
    'sovereign_mesh',
    'Sovereign Mesh',
    'Decentralized identity and zero-knowledge infrastructure protocols.',
    'We build open protocols for sovereign identity, decentralized data storage, and verifiable computing.',
    'https://sovereignmesh.io',
    'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&auto=format&fit=crop&q=80',
    'Decentralized Infrastructure',
    'Berlin, Germany'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'leanstack_labs',
    'LeanStack Labs',
    'Radically minimal, high-efficiency developer tools and database systems.',
    'Rejecting bloatware. Crafting sub-millisecond database queries and featherweight frontends.',
    'https://leanstack.dev',
    'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=200&auto=format&fit=crop&q=80',
    'Developer Tools',
    'Stockholm, Sweden'
),
(
    'b0000000-0000-0000-0000-000000000003',
    'spitalul_judetean_timisoara',
    'Timișoara County Emergency Clinical Hospital',
    'Major tertiary hospital, trauma center, cardiology, and university surgical clinics in western Romania.',
    'Serving over 450,000 regional consultations annually with 1,100 beds, accredited Level 1 trauma and acute cardiac care units.',
    'https://hosptim.ro',
    'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=200&auto=format&fit=crop&q=80',
    'Healthcare & Hospitals',
    'Timișoara, Romania'
),
(
    'b0000000-0000-0000-0000-000000000004',
    'spitalul_copii_timisoara',
    'Louis Țurcanu Clinical Emergency Children''s Hospital',
    'Premier pediatric hospital and research facility in western Romania covering pediatric surgery and neonatal critical care.',
    'Equipped with advanced robotic surgery suites and pediatric emergency care units for western Romania.',
    'https://spitaluldecopiitm.ro',
    'https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=200&auto=format&fit=crop&q=80',
    'Pediatric Healthcare',
    'Timișoara, Romania'
),
(
    'b0000000-0000-0000-0000-000000000005',
    'umf_victor_babes_timisoara',
    'Victor Babeș University of Medicine and Pharmacy Timișoara',
    'Renowned academic medical university advancing biomedical research, physician training, and clinical innovation.',
    'Training over 7,000 future physicians, researchers, and specialists across 3 faculties with international clinical partnerships.',
    'https://umft.ro',
    'https://images.unsplash.com/photo-1562774053-701939374585?w=200&auto=format&fit=crop&q=80',
    'Higher Education & Medicine',
    'Timișoara, Romania'
);

-- 3. Graph Edges
INSERT INTO graph_edges (id, source_id, source_type, target_id, target_type, edge_type, status, weight)
VALUES
(
    'd0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001', 'PERSON',
    'a0000000-0000-0000-0000-000000000002', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000002', 'PERSON',
    'a0000000-0000-0000-0000-000000000001', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000002', 'PERSON',
    'a0000000-0000-0000-0000-000000000003', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000003', 'PERSON',
    'a0000000-0000-0000-0000-000000000002', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000005', 'PERSON',
    'a0000000-0000-0000-0000-000000000006', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000006',
    'a0000000-0000-0000-0000-000000000006', 'PERSON',
    'a0000000-0000-0000-0000-000000000005', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000007',
    'a0000000-0000-0000-0000-000000000005', 'PERSON',
    'a0000000-0000-0000-0000-000000000007', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000008',
    'a0000000-0000-0000-0000-000000000007', 'PERSON',
    'a0000000-0000-0000-0000-000000000005', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000012',
    'a0000000-0000-0000-0000-000000000002', 'PERSON',
    'a0000000-0000-0000-0000-000000000004', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000013',
    'a0000000-0000-0000-0000-000000000004', 'PERSON',
    'a0000000-0000-0000-0000-000000000002', 'PERSON',
    'CONNECTED_TO', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000009',
    'a0000000-0000-0000-0000-000000000005', 'PERSON',
    'b0000000-0000-0000-0000-000000000003', 'COMPANY',
    'WORKS_AT', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000010',
    'a0000000-0000-0000-0000-000000000006', 'PERSON',
    'b0000000-0000-0000-0000-000000000004', 'COMPANY',
    'WORKS_AT', 'ACCEPTED', 1.0
),
(
    'd0000000-0000-0000-0000-000000000011',
    'a0000000-0000-0000-0000-000000000007', 'PERSON',
    'b0000000-0000-0000-0000-000000000005', 'COMPANY',
    'WORKS_AT', 'ACCEPTED', 1.0
);

-- 4. Work Experiences
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
    true,
    'Directing acute myocardial infarction triage, primary PCI cath-lab interventions, and clinical quality protocols across the emergency department. Reduced median door-to-balloon time to 56 minutes.',
    'Cardiology,Emergency Medicine,Clinical Triage,Interventional Cardiology,Echocardiography'
),
(
    'e0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000005',
    'Cardiology Fellow & Clinical Registrar',
    'Victor Babeș University Cardiology Clinic',
    'b0000000-0000-0000-0000-000000000005',
    'Timișoara, Romania',
    '2012-10',
    '2018-02',
    false,
    'Completed 5-year specialty residency training in clinical cardiology and acute cardiovascular intensive care.',
    'Internal Medicine,Cardiology,Intensive Care'
),
(
    'e0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000006',
    'Attending Pediatric Surgeon & Laparoscopy Lead',
    'Louis Țurcanu Clinical Emergency Children''s Hospital',
    'b0000000-0000-0000-0000-000000000004',
    'Timișoara, Romania',
    '2019-01',
    '',
    true,
    'Performing neonatal reconstructions, pediatric trauma surgeries, and minimally invasive robotic laparoscopy. Spearheaded western Romania pediatric emergency clinical registry.',
    'Pediatric Surgery,Robotic Laparoscopy,Neonatal Surgery,Pediatrics'
),
(
    'e0000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000007',
    'Associate Professor of Neurology & Neuro-Intensivist',
    'Victor Babeș University of Medicine and Pharmacy',
    'b0000000-0000-0000-0000-000000000005',
    'Timișoara, Romania',
    '2017-09',
    '',
    true,
    'Heading clinical research on stroke thrombolysis protocols, endovascular thrombectomy candidacy, and neuro-ICU monitoring across Timișoara university hospitals.',
    'Neurology,Critical Care,Stroke Intervention,EEG Diagnostics'
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
    true,
    'Designing zero-knowledge cryptographic authentication protocols and decentralized peer-to-peer gossip layers.',
    'Rust,Cryptography,Distributed Systems,Zero-Knowledge'
),
(
    'e0000000-0000-0000-0000-000000000006',
    'a0000000-0000-0000-0000-000000000002',
    'Principal Database Engineer',
    'LeanStack Labs',
    'b0000000-0000-0000-0000-000000000002',
    'London, UK',
    '2021-06',
    '',
    true,
    'Architecting high-performance graph algorithms on top of relational database engines.',
    'PostgreSQL 17,SQL:2023,Query Optimization,C'
);

-- 5. Educations (Academic Degrees, Medical Residencies, Universities)
INSERT INTO educations (id, profile_id, institution_name, degree, field_of_study, start_date, end_date, is_current, grade, activities_markdown, skills)
VALUES
(
    'f0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000005',
    'Victor Babeș University of Medicine and Pharmacy Timișoara',
    'Doctor of Medicine (M.D.)',
    'General Medicine & Cardiovascular Pathophysiology',
    '2004',
    '2010',
    false,
    'Graduated Magna Cum Laude (9.85/10.0)',
    'President of the Timișoara Medical Students Association (SSMT). Published undergraduate research on acute electrocardiographic markers in pre-infarction angina.',
    'Cardiology,Clinical Medicine,Pathophysiology,Internal Medicine'
),
(
    'f0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000005',
    'Victor Babeș University Cardiology Residency Program',
    'Medical Specialist Diploma',
    'Clinical Cardiology & Critical Emergency Care',
    '2010',
    '2015',
    false,
    'Certified Specialist Board Exam Top 1%',
    'Clinical fellowship at the Institute of Cardiovascular Diseases Timișoara. Completed European Society of Cardiology (ESC) echocardiography certification.',
    'Echocardiography,Clinical Triage,Interventional Cardiology'
),
(
    'f0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000006',
    'Victor Babeș University of Medicine and Pharmacy Timișoara',
    'Doctor of Medicine (M.D.) & Pediatric Residency',
    'Pediatric Medicine & Minimally Invasive Surgery',
    '2008',
    '2014',
    false,
    'Summa Cum Laude (9.92/10.0)',
    'Awarded National Young Researcher Prize in Pediatric Surgery. Research on neonatal laparoscopic outcomes in congenital diaphragmatic hernia.',
    'Pediatrics,Pediatric Surgery,Neonatal Surgery'
),
(
    'f0000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000007',
    'Victor Babeș University of Medicine and Pharmacy Timișoara',
    'Ph.D. in Clinical Neurophysiology & M.D.',
    'Neurology, Stroke Therapeutics & Critical Care',
    '2005',
    '2015',
    false,
    'Doctorate with Highest Distinction',
    'Author of 24 peer-reviewed neurology papers on acute stroke care pathways and continuous electroencephalography monitoring in neuro-intensive care.',
    'Neurology,Neuroscience,Critical Care,EEG Diagnostics'
),
(
    'f0000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000001',
    'Technical University of Munich (TUM)',
    'Master of Science (M.Sc.)',
    'Distributed Computing & Applied Cryptography',
    '2014',
    '2016',
    false,
    'Grade: 1.1 (High Distinction)',
    'Master Thesis on Zero-Knowledge Succinct Non-Interactive Arguments of Knowledge (zk-SNARKs) applied to peer-to-peer gossip validation.',
    'Rust,Cryptography,Distributed Systems,Mathematics'
),
(
    'f0000000-0000-0000-0000-000000000006',
    'a0000000-0000-0000-0000-000000000002',
    'Imperial College London',
    'Bachelor of Engineering (B.Eng. Hons)',
    'Computing & Database Engine Architecture',
    '2010',
    '2013',
    false,
    'First Class Honours',
    'President of the Systems & Database Research Group. Research on query cost estimation models for graph-relational queries.',
    'PostgreSQL,Query Optimization,C,Data Structures'
);

-- 6. Posts
INSERT INTO posts (id, author_id, author_type, title, body_markdown, link_url, link_title, link_description, link_image_url, link_domain, tags, has_tables, has_images, has_links, like_count, comment_count, github_repo, github_path, github_commit_sha, is_self_hosted)
VALUES
(
    'c0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001',
    'PERSON',
    'Zero-Knowledge Proofs in Peer-to-Peer Communication',
    'Decentralized communication networks must eliminate centralized credential authorities. We benchmarked zk-STARK verification times across browser WebAssembly runtimes:

| Proof System | Prover Time (ms) | Verification Time (ms) | Proof Size (KB) |
| :--- | :--- | :--- | :--- |
| **Groth16 (SNARK)** | 1,420 | **2.8** | **0.8** |
| **Plonk** | 2,150 | 4.6 | 1.4 |
| **STARK (Halo2)** | **890** | 7.1 | 48.0 |

Here is the protocol topology diagram:
![Zero-Knowledge Network Topology](https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80)

Our git-backed specifications are available in the open repository below.',
    'https://github.com/elenarostova/zk-protocols',
    'zk-protocols: Zero-Knowledge Verification Engines for Sovereign Mesh',
    'High-performance Rust implementation of decentralized identity and zero-knowledge verification.',
    'https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=600&auto=format&fit=crop&q=80',
    'github.com',
    'Cryptography,Rust,Privacy,ZeroKnowledge,P2P',
    true, true, true,
    42, 3,
    '',
    '',
    '',
    false
),
(
    'c0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000002',
    'PERSON',
    'Native Graph Traversals in PostgreSQL 17/18: Eliminating Graph DB Sprawl',
    'You do not need a dedicated graph database for professional network recommendations. Recursive CTEs in PostgreSQL achieve sub-10ms performance on 100k+ edge graphs:

| Traversal Depth | Neo4j 5.x (ms) | PostgreSQL 17/18 (ms) | PostgreSQL Memory (MB) |
| :--- | :--- | :--- | :--- |
| **1-Hop (Direct)** | 1.2 | **0.6** | **8** |
| **2-Hop (Mutuals)** | 4.8 | **3.9** | **14** |
| **Shortest Path** | 6.5 | **5.1** | **19** |

Native database graph modeling keeps data consistency 100% relational while eliminating separate sync daemons.',
    'https://postgresql.org',
    'PostgreSQL: The World''s Most Advanced Open Source Relational Database',
    'Official specifications for SQL:2023 Property Graph Queries (PGQ) and relational graph optimization.',
    'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600&auto=format&fit=crop&q=80',
    'postgresql.org',
    'PostgreSQL,Database,SQL,Architecture,Graph',
    true, false, true,
    57, 5,
    'marcus-thorne/database-internals',
    'benchmarks/postgres-recursive-graphs.md',
    '7a9e32f',
    true
),
(
    'c0000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000005',
    'PERSON',
    'Emergency Cardiology Triage in Timișoara: Open Protocols & Response Times',
    'In acute coronary syndrome (ACS), every minute saved preserves viable myocardial tissue. Over the past 12 months, our medical teams in Timișoara have overhauled pre-hospital triage protocols.

Here is our benchmark performance matrix at the Timișoara Emergency Hospital:

| Protocol Stage | Standard Regional Target | Timișoara Emergency Unit (2026) | Outcome Improvement |
| :--- | :--- | :--- | :--- |
| **Door-to-ECG** | < 10 minutes | **4.2 minutes** | -58% diagnostic delay |
| **Door-to-Balloon (PCI)** | < 90 minutes | **56 minutes** | +34% myocardial salvage |
| **Tele-ECG Triage** | In-transit review | **100% active units** | Pre-hospital activation |

Decentralized physician networks and open collaboration allow doctors in Timișoara to exchange critical clinical insights rapidly without corporate platform noise.',
    'https://hosptim.ro',
    'Timișoara Emergency Clinical Hospital - Acute Cardiology Protocol',
    'Clinical triage metrics and emergency cardiology guidelines for the Timișoara metropolitan region.',
    'https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=600&auto=format&fit=crop&q=80',
    'hosptim.ro',
    'Medicine,Cardiology,Timișoara,Timisoara,Healthcare,EmergencyCare',
    true, false, true,
    38, 8,
    '', '', '', false
);

-- 7. Comments
INSERT INTO post_comments (id, post_id, author_id, body_markdown)
VALUES
(
    '10000000-0000-0000-0000-000000000001',
    'c0000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000007',
    'Outstanding clinical benchmark, Andrei. We are adopting a similar pre-hospital triage protocol for our acute stroke response units across Timișoara.'
),
(
    '10000000-0000-0000-0000-000000000002',
    'c0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000002',
    'The Groth16 verification latency at 2.8ms is stellar for client-side cryptographic proof checking on low-power devices.'
);
