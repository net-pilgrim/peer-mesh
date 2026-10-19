<script>
  import { marked } from 'marked';
  import {
    User,
    Mail,
    MapPin,
    Sparkles,
    Shield,
    Globe,
    GitBranch,
    FolderGit,
    Key,
    FileText,
    Check,
    X,
    Image,
    Tag,
    Camera,
    AtSign,
    Save,
    Eye,
    Edit3
  } from '@lucide/svelte';
  import { updateProfile } from './api.js';

  let { profile, onClose, onProfileUpdated } = $props();

  // Active section tab: 'basic' | 'media' | 'skills' | 'contact' | 'github'
  let activeSection = $state('basic');

  // Form states initialized empty and synced via $effect
  let handle = $state('');
  let displayName = $state('');
  let headline = $state('');
  let bioMarkdown = $state('');
  let avatarUrl = $state('');
  let bannerUrl = $state('');
  let location = $state('');
  let skillsInput = $state('');

  // Decentralized Identifiers
  let emailContact = $state('');
  let matrixHandle = $state('');
  let nostrNpub = $state('');
  let simplexAddress = $state('');
  let sessionId = $state('');
  let pgpFingerprint = $state('');

  // GitHub Provenance
  let githubUsername = $state('');
  let githubContentRepo = $state('');

  $effect(() => {
    if (profile) {
      handle = profile.handle || '';
      displayName = profile.displayName || '';
      headline = profile.headline || '';
      bioMarkdown = profile.bioMarkdown || '';
      avatarUrl = profile.avatarUrl || '';
      bannerUrl = profile.bannerUrl || '';
      location = profile.location || '';
      skillsInput = profile.skills ? profile.skills.join(', ') : '';
      emailContact = profile.emailContact || '';
      matrixHandle = profile.matrixHandle || '';
      nostrNpub = profile.nostrNpub || '';
      simplexAddress = profile.simplexAddress || '';
      sessionId = profile.sessionId || '';
      pgpFingerprint = profile.pgpFingerprint || '';
      githubUsername = profile.githubUsername || '';
      githubContentRepo = profile.githubContentRepo || '';
    }
  });

  let isSaving = $state(false);
  let errorMessage = $state('');
  let successMessage = $state('');

  // Bio preview toggle
  let showBioPreview = $state(false);

  // Derived parsed skills list for live tags
  const parsedSkills = $derived.by(() => {
    return skillsInput
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);
  });

  async function handleSave() {
    if (!displayName.trim()) {
      errorMessage = 'Display name cannot be empty.';
      return;
    }

    isSaving = true;
    errorMessage = '';
    successMessage = '';

    const payload = {
      handle: handle.trim().replace(/^@/, ''),
      displayName: displayName.trim(),
      headline: headline.trim(),
      bioMarkdown: bioMarkdown.trim(),
      avatarUrl: avatarUrl.trim(),
      bannerUrl: bannerUrl.trim(),
      location: location.trim(),
      skills: parsedSkills,
      emailContact: emailContact.trim(),
      matrixHandle: matrixHandle.trim(),
      nostrNpub: nostrNpub.trim(),
      simplexAddress: simplexAddress.trim(),
      sessionId: sessionId.trim(),
      pgpFingerprint: pgpFingerprint.trim(),
      githubUsername: githubUsername.trim(),
      githubContentRepo: githubContentRepo.trim()
    };

    try {
      const updated = await updateProfile(profile.id || profile.handle, payload);
      successMessage = 'Personal data updated successfully!';
      if (onProfileUpdated) {
        onProfileUpdated(updated);
      }
      setTimeout(() => {
        onClose();
      }, 700);
    } catch (err) {
      errorMessage = err.message || 'Failed to update personal data';
    } finally {
      isSaving = false;
    }
  }
</script>

<div class="modal-backdrop" onclick={onClose} onkeydown={(e) => e.key === 'Escape' && onClose()} role="presentation">
  <div class="modal-card" style="max-width: 840px; width: 95vw;" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()} role="dialog" aria-modal="true" tabindex="-1">
    
    <!-- Modal Header -->
    <div class="modal-header">
      <div style="display: flex; gap: 12px; align-items: center;">
        <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--accent-subtle); display: flex; align-items: center; justify-content: center; color: var(--accent-primary);">
          <User size={20} />
        </div>
        <div>
          <h2 style="font-size: 17px; font-weight: 700; margin: 0; color: var(--text-main);">
            Personal Data Editor
          </h2>
          <div style="font-size: 12px; color: var(--text-dim); margin-top: 2px;">
            Manage identity, avatar, headline, decentralized contacts, and skills for @{profile?.handle || 'user'}
          </div>
        </div>
      </div>
      <button class="btn-icon" onclick={onClose} title="Close" aria-label="Close modal">
        <X size={18} />
      </button>
    </div>

    <!-- Section Navigation Tabs -->
    <div style="display: flex; gap: 6px; padding: 8px 20px; background: var(--bg-surface-elevated); border-bottom: 1px solid var(--border-subtle); overflow-x: auto;">
      <button
        class="highlight-filter-chip"
        class:active={activeSection === 'basic'}
        onclick={() => activeSection = 'basic'}
      >
        <User size={13} />
        <span>Identity & Bio</span>
      </button>

      <button
        class="highlight-filter-chip"
        class:active={activeSection === 'media'}
        onclick={() => activeSection = 'media'}
      >
        <Camera size={13} />
        <span>Avatar & Visuals</span>
      </button>

      <button
        class="highlight-filter-chip"
        class:active={activeSection === 'skills'}
        onclick={() => activeSection = 'skills'}
      >
        <Tag size={13} />
        <span>Skills ({parsedSkills.length})</span>
      </button>

      <button
        class="highlight-filter-chip"
        class:active={activeSection === 'contact'}
        onclick={() => activeSection = 'contact'}
      >
        <Shield size={13} />
        <span>Decentralized Channels</span>
      </button>

      <button
        class="highlight-filter-chip"
        class:active={activeSection === 'github'}
        onclick={() => activeSection = 'github'}
      >
        <GitBranch size={13} />
        <span>Code Provenance</span>
      </button>
    </div>

    <!-- Modal Body Content -->
    <div style="padding: 22px; max-height: 68vh; overflow-y: auto; display: flex; flex-direction: column; gap: 18px;">
      
      {#if errorMessage}
        <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.3); color: var(--accent-danger); padding: 10px 14px; border-radius: var(--radius-sm); font-size: 13px;">
          {errorMessage}
        </div>
      {/if}

      {#if successMessage}
        <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); color: var(--accent-success); padding: 10px 14px; border-radius: var(--radius-sm); font-size: 13px; display: flex; align-items: center; gap: 6px;">
          <Check size={16} />
          <span>{successMessage}</span>
        </div>
      {/if}

      <!-- TAB 1: BASIC IDENTITY & BIO -->
      {#if activeSection === 'basic'}
        <div style="display: flex; flex-direction: column; gap: 14px;">
          <div class="work-form-grid">
            <div>
              <label for="p-display-name" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Display Name *</label>
              <input
                id="p-display-name"
                type="text"
                placeholder="e.g. Dr. Andrei Mureșan"
                bind:value={displayName}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>

            <div>
              <label for="p-handle" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Unique Handle (@username)</label>
              <div style="position: relative; display: flex; align-items: center;">
                <span style="position: absolute; left: 10px; color: var(--text-dim); font-size: 13px; font-weight: 600;">@</span>
                <input
                  id="p-handle"
                  type="text"
                  placeholder="e.g. dr_andrei_muresan"
                  bind:value={handle}
                  style="width: 100%; padding: 8px 10px 8px 26px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>
          </div>

          <div>
            <label for="p-headline" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Professional Headline / Title</label>
            <input
              id="p-headline"
              type="text"
              placeholder="e.g. Chief of Cardiology & Emergency Doctor | Timișoara County Emergency Clinical Hospital"
              bind:value={headline}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
            />
          </div>

          <div>
            <label for="p-location" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Location (City, Country - Privacy Preserving, No GPS)</label>
            <div style="position: relative; display: flex; align-items: center;">
              <span style="position: absolute; left: 10px; color: var(--text-dim);">
                <MapPin size={14} />
              </span>
              <input
                id="p-location"
                type="text"
                placeholder="e.g. Timișoara, Romania"
                bind:value={location}
                style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>
          </div>

          <div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
              <label for="p-bio" style="font-size: 12px; font-weight: 600; color: var(--text-dim);">Professional Summary / Bio (Markdown Enabled)</label>
              <button
                type="button"
                class="btn-icon"
                style="font-size: 11px; padding: 2px 6px; display: inline-flex; align-items: center; gap: 4px;"
                onclick={() => showBioPreview = !showBioPreview}
              >
                {#if showBioPreview}
                  <Edit3 size={12} /> <span>Switch to Editor</span>
                {:else}
                  <Eye size={12} /> <span>Preview Markdown</span>
                {/if}
              </button>
            </div>

            {#if showBioPreview}
              <div class="work-desc-markdown" style="min-height: 120px; padding: 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm);">
                {#if bioMarkdown}
                  {@html marked.parse(bioMarkdown, { gfm: true, breaks: true })}
                {:else}
                  <em style="color: var(--text-dim);">No bio text entered yet.</em>
                {/if}
              </div>
            {:else}
              <textarea
                id="p-bio"
                rows="5"
                placeholder="Write your professional summary, clinical focus, research interests, or background..."
                bind:value={bioMarkdown}
                style="width: 100%; padding: 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main); resize: vertical; line-height: 1.5;"
              ></textarea>
            {/if}
          </div>
        </div>
      {/if}

      <!-- TAB 2: AVATAR & VISUALS -->
      {#if activeSection === 'media'}
        <div style="display: flex; flex-direction: column; gap: 16px;">
          <!-- Avatar Preview Card -->
          <div style="display: flex; gap: 16px; align-items: center; padding: 16px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md);">
            <img
              src={avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
              alt="Avatar Preview"
              style="width: 72px; height: 72px; border-radius: 50%; object-fit: cover; border: 2px solid var(--accent-primary);"
              onerror={(e) => e.target.src = 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
            />
            <div style="flex: 1;">
              <label for="p-avatar-url" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Profile Avatar URL</label>
              <input
                id="p-avatar-url"
                type="text"
                placeholder="https://... or unsplash image URL"
                bind:value={avatarUrl}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
              <span style="font-size: 11px; color: var(--text-dim); margin-top: 4px; display: block;">
                Recommended: Square image URL (JPEG/PNG/WebP), hosted via object storage or public CDN.
              </span>
            </div>
          </div>

          <!-- Banner Preview Card -->
          <div style="padding: 16px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 10px;">
            <label for="p-banner-url" style="font-size: 12px; font-weight: 600; color: var(--text-dim);">Profile Header Banner URL (Optional)</label>
            <input
              id="p-banner-url"
              type="text"
              placeholder="https://images.unsplash.com/... header banner image"
              bind:value={bannerUrl}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
            />

            {#if bannerUrl}
              <div style="width: 100%; height: 90px; border-radius: var(--radius-sm); overflow: hidden; border: 1px solid var(--border-subtle);">
                <img
                  src={bannerUrl}
                  alt="Banner preview"
                  style="width: 100%; height: 100%; object-fit: cover;"
                  onerror={(e) => e.target.style.display = 'none'}
                />
              </div>
            {/if}
          </div>
        </div>
      {/if}

      <!-- TAB 3: SKILLS -->
      {#if activeSection === 'skills'}
        <div style="display: flex; flex-direction: column; gap: 14px;">
          <div>
            <label for="p-skills" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Skills & Competencies (Comma-separated)</label>
            <input
              id="p-skills"
              type="text"
              placeholder="e.g. Cardiology, Emergency Medicine, Echocardiography, Clinical Triage, Interventional"
              bind:value={skillsInput}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
            />
            <span style="font-size: 11px; color: var(--text-dim); margin-top: 4px; display: block;">
              Separate tags with commas. These empower the Unified Professional Discovery search engine.
            </span>
          </div>

          <!-- Live Rendered Skill Pills -->
          <div>
            <span style="font-size: 11px; font-weight: 600; text-transform: uppercase; color: var(--text-dim); display: block; margin-bottom: 8px;">
              Live Preview Tags ({parsedSkills.length}):
            </span>
            <div style="display: flex; gap: 6px; flex-wrap: wrap;">
              {#if parsedSkills.length === 0}
                <span style="font-size: 12px; color: var(--text-dim); font-style: italic;">No skills specified yet.</span>
              {:else}
                {#each parsedSkills as skill}
                  <span class="search-skill-pill" style="font-size: 12px; padding: 3px 10px; background: var(--accent-subtle); color: var(--accent-primary); border-color: var(--accent-border);">
                    #{skill}
                  </span>
                {/each}
              {/if}
            </div>
          </div>
        </div>
      {/if}

      <!-- TAB 4: DECENTRALIZED E2EE CHANNELS -->
      {#if activeSection === 'contact'}
        <div style="display: flex; flex-direction: column; gap: 14px;">
          <div style="padding: 10px 14px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-muted); line-height: 1.5;">
            <strong>Zero-Storage Communication:</strong> PeerMesh stores no chat logs or private messages. Connect directly with peers through sovereign, decentralized E2EE protocols.
          </div>

          <div class="work-form-grid">
            <div>
              <label for="p-email" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Contact Email</label>
              <div style="position: relative; display: flex; align-items: center;">
                <span style="position: absolute; left: 10px; color: var(--text-dim);"><Mail size={14} /></span>
                <input
                  id="p-email"
                  type="email"
                  placeholder="e.g. andrei.muresan@hosptim.ro"
                  bind:value={emailContact}
                  style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div>
              <label for="p-matrix" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Matrix Handle (E2EE Federated)</label>
              <div style="position: relative; display: flex; align-items: center;">
                <span style="position: absolute; left: 10px; color: var(--text-dim);"><AtSign size={14} /></span>
                <input
                  id="p-matrix"
                  type="text"
                  placeholder="e.g. @andrei.muresan:matrix.org"
                  bind:value={matrixHandle}
                  style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>
          </div>

          <div class="work-form-grid">
            <div>
              <label for="p-simplex" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">SimpleX Chat Address (Zero User ID)</label>
              <input
                id="p-simplex"
                type="text"
                placeholder="simplex:https://simplex.chat/contact#/?v=2-4..."
                bind:value={simplexAddress}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>

            <div>
              <label for="p-nostr" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Nostr Public Key (npub / NIP-04)</label>
              <input
                id="p-nostr"
                type="text"
                placeholder="npub1..."
                bind:value={nostrNpub}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>
          </div>

          <div class="work-form-grid">
            <div>
              <label for="p-session" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Session Onion ID</label>
              <input
                id="p-session"
                type="text"
                placeholder="05ee892348092384902384901284901284901284"
                bind:value={sessionId}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>

            <div>
              <label for="p-pgp" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">PGP / GPG Fingerprint</label>
              <input
                id="p-pgp"
                type="text"
                placeholder="40-hex PGP key fingerprint"
                bind:value={pgpFingerprint}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>
          </div>
        </div>
      {/if}

      <!-- TAB 5: GITHUB INTEGRATION -->
      {#if activeSection === 'github'}
        <div style="display: flex; flex-direction: column; gap: 14px;">
          <div style="padding: 10px 14px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-muted); line-height: 1.5;">
            <strong>Content Provenance & Git Syndication:</strong> Link your GitHub account and repository to verify publication commits, author identity, and decentralized content mirrors.
          </div>

          <div class="work-form-grid">
            <div>
              <label for="p-gh-user" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">GitHub Username</label>
              <div style="position: relative; display: flex; align-items: center;">
                <span style="position: absolute; left: 10px; color: var(--text-dim);"><GitBranch size={14} /></span>
                <input
                  id="p-gh-user"
                  type="text"
                  placeholder="e.g. dr-andrei-muresan"
                  bind:value={githubUsername}
                  style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div>
              <label for="p-gh-repo" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Content Repository</label>
              <div style="position: relative; display: flex; align-items: center;">
                <span style="position: absolute; left: 10px; color: var(--text-dim);"><FolderGit size={14} /></span>
                <input
                  id="p-gh-repo"
                  type="text"
                  placeholder="e.g. clinical-protocols-markdown"
                  bind:value={githubContentRepo}
                  style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>
          </div>
        </div>
      {/if}

    </div>

    <!-- Modal Footer Actions -->
    <div class="modal-footer" style="padding: 14px 22px; border-top: 1px solid var(--border-subtle); display: flex; justify-content: space-between; align-items: center; background: var(--bg-surface);">
      <button class="btn-secondary" onclick={onClose} disabled={isSaving}>
        Cancel
      </button>

      <button
        class="btn-primary"
        onclick={handleSave}
        disabled={isSaving}
        style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 16px;"
      >
        <Save size={14} />
        <span>{isSaving ? 'Saving Changes...' : 'Save Personal Data'}</span>
      </button>
    </div>

  </div>
</div>
