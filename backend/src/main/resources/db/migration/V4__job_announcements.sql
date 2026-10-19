-- ====================================================================
-- PeerMesh: Flyway Migration V4 - Job Announcements with Object Storage
-- Supports both PostgreSQL 18+ and H2 in PostgreSQL Mode
-- ====================================================================

CREATE TABLE IF NOT EXISTS job_announcements (
    id UUID PRIMARY KEY,
    company_id UUID,
    company_name VARCHAR(128) NOT NULL,
    company_logo_url TEXT DEFAULT '',
    title VARCHAR(160) NOT NULL,
    location VARCHAR(128) DEFAULT '',
    employment_type VARCHAR(32) NOT NULL DEFAULT 'FULL_TIME',
    department VARCHAR(64) NOT NULL DEFAULT 'Engineering',
    experience_level VARCHAR(64) DEFAULT 'Senior',
    salary_range VARCHAR(64) DEFAULT '',
    description_markdown TEXT NOT NULL,
    skills VARCHAR(512) DEFAULT '',
    apply_contact VARCHAR(256) DEFAULT '',
    storage_key VARCHAR(128) DEFAULT '',
    featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_jobs_dept ON job_announcements(department);
CREATE INDEX IF NOT EXISTS idx_jobs_emp_type ON job_announcements(employment_type);
CREATE INDEX IF NOT EXISTS idx_jobs_comp ON job_announcements(company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_created ON job_announcements(created_at DESC);

-- Seed Initial Job Announcements

-- 1. Sovereign Mesh Technologies - Senior Rust Protocol Engineer
INSERT INTO job_announcements (
    id, company_id, company_name, company_logo_url, title, location, employment_type, department, experience_level, salary_range, description_markdown, skills, apply_contact, storage_key, featured
) VALUES (
    'd0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000001',
    'Sovereign Mesh Technologies',
    'https://images.unsplash.com/photo-1563986768609-322da13575f3?w=200&auto=format&fit=crop&q=80',
    'Senior Rust Core Protocol Engineer',
    'Remote (Berlin / Stockholm)',
    'FULL_TIME',
    'Engineering',
    'Staff / Lead',
    '€120,000 - €150,000 + Grant',
    '### About Sovereign Mesh Technologies
We build censorship-resistant, decentralized communication engines and zero-knowledge verification frameworks that operate without centralized telecom or identity authorities.

### The Role
As a **Senior Rust Protocol Engineer**, you will lead the core development of our sovereign gossip routing layer and zk-STARK verification engine. You will optimize low-latency network primitives for WebAssembly and native Linux bare-metal runtimes.

### Key Responsibilities
- Architect high-throughput p2p gossip protocol primitives using `libp2p` and custom QUIC transports.
- Optimize STARK prover & verifier performance in Rust for cross-platform WASM and mobile environments.
- Benchmark memory safety, lock-free ring buffers, and cache locality on Linux multi-core servers.
- Author formal cryptographic protocol specifications and review smart contract verifiers.

### Requirements & Tech Stack
- 5+ years of production experience in **Rust** and systems programming.
- Deep familiarity with asynchronous networking (`tokio`, `mio`) and P2P protocols.
- Basic understanding of zero-knowledge proofs (zk-SNARKs/STARKs) or willingness to learn.
- Strong advocate for sovereign, private user infrastructure.

### Compensation & Benefits
- Base salary: €120,000 – €150,000 + Sovereign Token/Equity grant
- 100% remote within European timezones (UTC-1 to UTC+3)
- €4,000 hardware & home-office stipend
- Annual team hacking retreats in Stockholm and the Swiss Alps',
    'Rust,Distributed Systems,libp2p,ZeroKnowledge,Cryptography',
    'jobs@sovereign-mesh.org',
    'jobs/d0000000-0000-0000-0000-000000000001.md',
    TRUE
);

-- 2. Timișoara County Emergency Clinical Hospital - Lead Interventional Cardiologist
INSERT INTO job_announcements (
    id, company_id, company_name, company_logo_url, title, location, employment_type, department, experience_level, salary_range, description_markdown, skills, apply_contact, storage_key, featured
) VALUES (
    'd0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000003',
    'Timișoara County Emergency Clinical Hospital',
    'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=200&auto=format&fit=crop&q=80',
    'Lead Interventional Cardiologist (Cath-Lab & Acute Triage)',
    'Timișoara, Romania',
    'FULL_TIME',
    'Medicine & Clinical',
    'Specialist / Attending',
    '€65,000 - €90,000 (Clinical Scale + On-Call)',
    '### About Spitalul Clinic Județean de Urgență Timișoara
The Timișoara County Emergency Clinical Hospital (Spitalul Județean Pius Brînzeu) is Western Romania''s largest tertiary medical center with 1,100 beds, Level 1 regional trauma accreditation, and university cardiology clinics affiliated with UMF Victor Babeș.

### Clinical Focus & Responsibilities
- Lead primary percutaneous coronary interventions (PCI) during regional 24/7 STEMI emergency on-call shifts.
- Perform structural cardiac interventions, intravascular ultrasound (IVUS), and hemodynamic triage.
- Mentor cardiology residents and medical doctoral students in invasive cardiovascular research.
- Participate in European Society of Cardiology (ESC) multicenter randomized clinical trials.

### Candidate Profile
- Board-certified Specialist or Senior Medical Doctor (Medic Primar) in Cardiology.
- Documented competency in invasive catheterization and emergency coronary revascularization.
- Clean medical licensing eligible for Romanian College of Physicians (CMR).

### Department Infrastructure
- 2 state-of-the-art biplane angiographic cath-labs with integrated hemodynamic telemetry.
- Direct collaboration with Dr. Andrei Mureșan and the cardiac intensive care unit (CICU).',
    'Cardiology,Cath-Lab,Interventional Cardiology,Emergency Medicine,PCI',
    'andrei.muresan@hosptim.ro',
    'jobs/d0000000-0000-0000-0000-000000000002.md',
    TRUE
);

-- 3. LeanStack Labs - Principal Database Systems Architect
INSERT INTO job_announcements (
    id, company_id, company_name, company_logo_url, title, location, employment_type, department, experience_level, salary_range, description_markdown, skills, apply_contact, storage_key, featured
) VALUES (
    'd0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000002',
    'LeanStack Labs',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200&auto=format&fit=crop&q=80',
    'Principal PostgreSQL Database Systems Architect',
    'Zurich, Switzerland / Remote EU',
    'FULL_TIME',
    'Infrastructure',
    'Staff / Lead',
    'CHF 160,000 - 195,000',
    '### Mission at LeanStack Labs
We reject cloud bloat and $50k/month managed Kubernetes invoices. We architect sovereign, ultra-efficient bare-metal databases that outperform cloud clusters at 1/10th the cost.

### Responsibilities
- Architect native PostgreSQL graph traversal extensions and recursive CTE optimizations.
- Engineer lock-free transaction caching and async read-replica synchronization using Redis and Lettuce.
- Tune Linux kernel parameters (`io_uring`, NVMe queue depths, page dirty limits) for sustained 100k+ writes/sec.
- Author technical case studies on replacing cloud lock-in with self-hosted PostgreSQL 18 instances.

### Experience
- 8+ years deep technical experience in PostgreSQL internals and query planning.
- Mastery of C, Rust, or modern Java/Micronaut for high-performance data systems.
- Passion for software minimalism, bare-metal simplicity, and cost efficiency.',
    'PostgreSQL,C,Performance Tuning,Linux Kernel,Distributed SQL',
    'marcus@pg-specialists.io',
    'jobs/d0000000-0000-0000-0000-000000000003.md',
    FALSE
);

-- 4. Sovereign Mesh Technologies - Lead Svelte Interaction Designer
INSERT INTO job_announcements (
    id, company_id, company_name, company_logo_url, title, location, employment_type, department, experience_level, salary_range, description_markdown, skills, apply_contact, storage_key, featured
) VALUES (
    'd0000000-0000-0000-0000-000000000004',
    'b0000000-0000-0000-0000-000000000001',
    'Sovereign Mesh Technologies',
    'https://images.unsplash.com/photo-1563986768609-322da13575f3?w=200&auto=format&fit=crop&q=80',
    'Senior Svelte 5 & Interaction Designer',
    'Remote (Global EU/US)',
    'CONTRACT',
    'Design & UX',
    'Senior',
    '€85 - €120 / hour',
    '### The Opportunity
Craft the user interface of the next-generation decentralized professional web. Zero advertising trackers, zero 3-column feed clutter, zero framework bloat.

### What You Will Build
- Build high-density timeline visualization components in **Svelte 5** using fine-grained reactivity (`$state`, `$derived`).
- Design keyboard-driven interfaces with sub-16ms frame render times.
- Implement responsive dark/light themes and offline PWA synchronization.
- Integrate WebAssembly cryptographic key generators seamlessly into user workflows.',
    'Svelte 5,TypeScript,Vanilla CSS,PWA,Interaction Design',
    'nora@zen-frontend.dev',
    'jobs/d0000000-0000-0000-0000-000000000004.md',
    FALSE
);

-- 5. Timișoara County Emergency Clinical Hospital - Pediatric Minimally Invasive Surgery Fellow
INSERT INTO job_announcements (
    id, company_id, company_name, company_logo_url, title, location, employment_type, department, experience_level, salary_range, description_markdown, skills, apply_contact, storage_key, featured
) VALUES (
    'd0000000-0000-0000-0000-000000000005',
    'b0000000-0000-0000-0000-000000000003',
    'Timișoara County Emergency Clinical Hospital',
    'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=200&auto=format&fit=crop&q=80',
    'Pediatric Laparoscopic & Neonatal Surgical Fellow',
    'Timișoara, Romania',
    'FULL_TIME',
    'Medicine & Clinical',
    'Fellow / Mid',
    '€45,000 - €60,000',
    '### 2-Year Advanced Surgical Fellowship
Join the Pediatric Surgery Department at the Timișoara Children''s Hospital and County Emergency Hospital under the clinical direction of Dr. Sorina Popa.

### Clinical Scope
- Over 800 pediatric laparoscopic, thoracoscopic, and reconstructive procedures performed annually.
- Comprehensive exposure to congenital neonatal surgical emergencies (CDH, esophageal atresia, Hirschsprung).
- Participation in academic clinical trials and international EUPSA collaborative registry research.',
    'Pediatric Surgery,Laparoscopy,Neonatal Surgery,Clinical Research',
    'sorina.popa@pediatrie-tm.ro',
    'jobs/d0000000-0000-0000-0000-000000000005.md',
    FALSE
);

-- 6. LeanStack Labs - High-Performance Cache & Redis Specialist
INSERT INTO job_announcements (
    id, company_id, company_name, company_logo_url, title, location, employment_type, department, experience_level, salary_range, description_markdown, skills, apply_contact, storage_key, featured
) VALUES (
    'd0000000-0000-0000-0000-000000000006',
    'b0000000-0000-0000-0000-000000000002',
    'LeanStack Labs',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200&auto=format&fit=crop&q=80',
    'High-Performance Cache & Redis Systems Engineer',
    'Remote (EU Timezones)',
    'REMOTE',
    'Infrastructure',
    'Senior',
    '€100,000 - €130,000',
    '### The Role
Design resilient, high-speed session storage and cache layers supporting millions of concurrent peer connections with sub-millisecond p99 latencies.

### Key Deliverables
- Deploy and benchmark Redis Cluster with Lettuce async drivers within a Micronaut runtime.
- Implement distributed rate-limiting and cryptographic session eviction.
- Build automatic failover mechanisms with zero data loss.',
    'Redis,Lettuce,Micronaut,Java 21,Distributed Systems',
    'kai.infra@pm.me',
    'jobs/d0000000-0000-0000-0000-000000000006.md',
    FALSE
);
