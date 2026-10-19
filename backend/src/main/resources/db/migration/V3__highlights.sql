-- V3__highlights.sql: Profile Highlights (Achievements, Patents, Prizes, Books, Conferences)

CREATE TABLE IF NOT EXISTS profile_highlights (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL, -- 'ACHIEVEMENT', 'PATENT', 'PRIZE', 'BOOK', 'CONFERENCE'
    title VARCHAR(256) NOT NULL,
    issuer_or_venue VARCHAR(256) DEFAULT '',
    date_or_year VARCHAR(32) NOT NULL,
    description_markdown TEXT DEFAULT '',
    external_url VARCHAR(512) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_highlights_profile ON profile_highlights(profile_id, date_or_year DESC);
CREATE INDEX IF NOT EXISTS idx_highlights_type ON profile_highlights(type);

-- Seed Highlights for Dr. Andrei Mureșan (Chief of Cardiology, Timișoara)
INSERT INTO profile_highlights (id, profile_id, type, title, issuer_or_venue, date_or_year, description_markdown, external_url)
VALUES
(
    '80000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000005',
    'CONFERENCE',
    'Keynote Speaker: Door-to-Balloon Emergency Triage at Scale',
    'European Society of Cardiology (ESC) Congress 2024 - London',
    '2024',
    'Presented multi-center clinical trial findings on reducing door-to-balloon reperfusion latency below 55 minutes in regional emergency cardiology hubs.',
    'https://www.escardio.org/Congresses-Events/ESC-Congress'
),
(
    '80000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000005',
    'PATENT',
    'Patent EP3928192: Rapid-Deployment Transfemoral Catheter Diagnostic Sensor',
    'European Patent Office (EPO)',
    '2023',
    'Invented a real-time micro-piezoelectric intravascular hemodynamic sensor designed for ultra-rapid cath-lab navigation during acute myocardial infarction triage.',
    'https://worldwide.espacenet.com/patent/search/family/079234812/publication/EP3928192A1'
),
(
    '80000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000005',
    'BOOK',
    'Emergency Invasive Cardiology & Cath-Lab Clinical Protocols',
    'Medical University Press Timișoara (ISBN 978-606-8214-92-1)',
    '2022',
    'Lead author of a 420-page comprehensive clinical handbook adopted as standardized reference curriculum across 12 Romanian university cardiology clinics.',
    'https://www.umft.ro/editura-medicala'
),
(
    '80000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000005',
    'PRIZE',
    'National Emergency Medicine & Cardiology Excellence Prize',
    'Romanian Ministry of Health & Medical Association',
    '2021',
    'Awarded for pioneering the Western Romania STEMI Fast-Track Network, lowering cardiac emergency mortality by 23% across Timiș County.',
    'https://ms.ro'
),
(
    '80000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000005',
    'ACHIEVEMENT',
    '500+ Consecutive Successful Primary Percutaneous Coronary Interventions',
    'Timișoara County Emergency Clinical Hospital',
    '2019',
    'Milestone career threshold achieved with zero procedural mortality in primary cath-lab acute myocardial infarction revascularizations.',
    ''
);

-- Seed Highlights for Dr. Radu Ionescu (Neurologist, Timișoara)
INSERT INTO profile_highlights (id, profile_id, type, title, issuer_or_venue, date_or_year, description_markdown, external_url)
VALUES
(
    '80000000-0000-0000-0000-000000000006',
    'a0000000-0000-0000-0000-000000000007',
    'CONFERENCE',
    'Speaker: Endovascular Thrombectomy Time Windows in Acute Ischemic Stroke',
    'World Stroke Congress (WSC) 2024 - Abu Dhabi',
    '2024',
    'Delivered lecture on extended window perfusion imaging biomarkers for wake-up stroke patient eligibility in emergency neuro-intensive care.',
    'https://worldstrokecongress.org'
),
(
    '80000000-0000-0000-0000-000000000007',
    'a0000000-0000-0000-0000-000000000007',
    'BOOK',
    'Clinical Neuro-Critical Care & Comprehensive Stroke Management',
    'Springer European Medical Editions',
    '2023',
    'Co-authored reference manual on multi-modal neuromonitoring, neuro-resuscitation protocols, and intensive stroke care pathways.',
    'https://springer.com'
),
(
    '80000000-0000-0000-0000-000000000008',
    'a0000000-0000-0000-0000-000000000007',
    'ACHIEVEMENT',
    'Established Western Romania Regional Acute Stroke Fast-Track Protocol',
    'Victor Babeș University Hospital Timișoara',
    '2022',
    'Created integrated regional dispatch network connecting ambulance tele-neurology consultations directly with emergency thrombectomy teams.',
    ''
);

-- Seed Highlights for Dr. Sorina Popa (Pediatric Surgeon, Timișoara)
INSERT INTO profile_highlights (id, profile_id, type, title, issuer_or_venue, date_or_year, description_markdown, external_url)
VALUES
(
    '80000000-0000-0000-0000-000000000009',
    'a0000000-0000-0000-0000-000000000006',
    'PATENT',
    'Patent RO134892: Pediatric Laparoscopic Micro-Trocar with Dynamic Pressure Feedback',
    'State Office for Inventions and Trademarks (OSIM)',
    '2024',
    'Patented minimally invasive 2mm pediatric trocar system preventing insufflation pressure surges during neonatal laparoscopic procedures.',
    'https://osim.ro'
),
(
    '80000000-0000-0000-0000-000000000010',
    'a0000000-0000-0000-0000-000000000006',
    'PRIZE',
    'European Paediatric Surgeons Association (EUPSA) Innovation Award',
    'EUPSA Annual Congress - Bologna',
    '2023',
    'Honored for development of low-impact robotic pediatric abdominal reconstruction techniques with 40% reduced recovery duration.',
    'https://eupsa.info'
);

-- Seed Highlights for Elena Rostova (Staff Distributed Systems Architect)
INSERT INTO profile_highlights (id, profile_id, type, title, issuer_or_venue, date_or_year, description_markdown, external_url)
VALUES
(
    '80000000-0000-0000-0000-000000000011',
    'a0000000-0000-0000-0000-000000000001',
    'PATENT',
    'Patent US11894921: Zero-Knowledge Decentralized Identity Proof Sharding',
    'United States Patent and Trademark Office (USPTO)',
    '2024',
    'Novel recursive zk-SNARK cryptographic sharding algorithm enabling cross-peer identity verification without revealing public graph topology.',
    'https://patents.google.com/patent/US11894921'
),
(
    '80000000-0000-0000-0000-000000000012',
    'a0000000-0000-0000-0000-000000000001',
    'BOOK',
    'Architecting Sovereign Peer-to-Peer Networks: From Theory to Rust',
    'Open Systems Press (ISBN 978-013-4859-10-8)',
    '2023',
    'Comprehensive 560-page technical guide exploring decentralized gossip routing, encrypted overlays, and Byzantine fault-tolerant state machines.',
    'https://github.com/elenarostova/zk-protocols'
),
(
    '80000000-0000-0000-0000-000000000013',
    'a0000000-0000-0000-0000-000000000001',
    'CONFERENCE',
    'Keynote: High-Performance Async Gossip Sub-Protocols at Scale',
    'RustConf 2023 - Portland',
    '2023',
    'Keynote address detailing memory-efficient tokio-based message propagation across 100k+ concurrent adversarial p2p nodes.',
    'https://rustconf.com'
),
(
    '80000000-0000-0000-0000-000000000014',
    'a0000000-0000-0000-0000-000000000001',
    'PRIZE',
    'EFF Sovereign Technology Award for Decentralized Cryptographic Systems',
    'Electronic Frontier Foundation (EFF)',
    '2022',
    'Recognized for exceptional contributions to open-source censorship-resistant communication protocols.',
    'https://eff.org'
),
(
    '80000000-0000-0000-0000-000000000015',
    'a0000000-0000-0000-0000-000000000001',
    'ACHIEVEMENT',
    'Authored Core Zero-Knowledge PeerMesh Protocol Specification',
    'PeerMesh Open Consortium',
    '2021',
    'Designed the open verifiable cryptographic exchange wire format powering sovereign profile discovery without central directories.',
    ''
);
