<script>
  import { marked } from 'marked';
  import {
    Briefcase,
    Building,
    MapPin,
    DollarSign,
    X,
    Save,
    HardDrive,
    Sparkles,
    Eye,
    Edit3
  } from '@lucide/svelte';
  import { createJob } from './api.js';

  let { currentProfile, onClose, onJobCreated } = $props();

  let title = $state('');
  let companyName = $state('Sovereign Lab');
  let companyLogoUrl = $state('https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200');
  let department = $state('Engineering');
  let employmentType = $state('FULL_TIME');
  let experienceLevel = $state('Senior');
  let location = $state('Remote (EU)');
  let salaryRange = $state('€90,000 - €120,000');
  let skillsInput = $state('Rust, Distributed Systems, PostgreSQL');
  let applyContact = $state('jobs@mesh.network');

  $effect(() => {
    if (currentProfile) {
      if (currentProfile.displayName) companyName = currentProfile.displayName + ' Lab';
      if (currentProfile.avatarUrl) companyLogoUrl = currentProfile.avatarUrl;
      if (currentProfile.location) location = currentProfile.location;
      if (currentProfile.emailContact) applyContact = currentProfile.emailContact;
    }
  });
  let descriptionMarkdown = $state(`### About the Role
We are seeking an experienced specialist to join our distributed team.

### Responsibilities
- Architect and maintain low-latency sovereign network services.
- Collaborate with open-source contributors and peer nodes.
- Ensure cryptographic integrity and strict privacy standards.

### Requirements
- Strong problem-solving mindset and technical expertise.
- Dedication to user sovereignty, privacy, and decentralized protocols.`);

  let showPreview = $state(false);
  let isSaving = $state(false);
  let errorMsg = $state('');

  const renderedPreview = $derived(
    marked.parse(descriptionMarkdown || '', { gfm: true, breaks: true })
  );

  async function handleSubmit(e) {
    e.preventDefault();
    if (!title.trim()) {
      errorMsg = 'Job title is required';
      return;
    }
    if (!companyName.trim()) {
      errorMsg = 'Company/Organization name is required';
      return;
    }
    if (!descriptionMarkdown.trim()) {
      errorMsg = 'Job description markdown is required';
      return;
    }

    isSaving = true;
    errorMsg = '';

    const skills = skillsInput
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);

    const payload = {
      title: title.trim(),
      companyName: companyName.trim(),
      companyLogoUrl: companyLogoUrl.trim(),
      department,
      employmentType,
      experienceLevel,
      location: location.trim(),
      salaryRange: salaryRange.trim(),
      skills,
      applyContact: applyContact.trim(),
      descriptionMarkdown: descriptionMarkdown.trim(),
      featured: false
    };

    try {
      const created = await createJob(payload);
      if (onJobCreated) onJobCreated(created);
      onClose();
    } catch (err) {
      errorMsg = 'Failed to publish job announcement: ' + err.message;
    } finally {
      isSaving = false;
    }
  }
</script>

<div class="modal-backdrop" onclick={onClose} onkeydown={(e) => e.key === 'Escape' && onClose()} role="presentation">
  <div class="modal-card post-job-card" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()} role="dialog" aria-modal="true" tabindex="-1">

    <!-- Modal Header -->
    <div class="modal-header" style="border-bottom: 1px solid var(--border-subtle); padding: 18px 24px;">
      <div style="display: flex; gap: 12px; align-items: center;">
        <div style="width: 38px; height: 38px; border-radius: var(--radius-sm); background: rgba(99, 102, 241, 0.12); color: var(--accent-primary); display: flex; align-items: center; justify-content: center;">
          <Briefcase size={20} />
        </div>
        <div>
          <h2 style="font-size: 16px; font-weight: 700; margin: 0; color: var(--text-main);">
            Post Job Announcement
          </h2>
          <div style="font-size: 12px; color: var(--text-dim); margin-top: 2px;">
            Markdown specification stored in Object Storage with network indexing
          </div>
        </div>
      </div>

      <button class="btn-icon" onclick={onClose} title="Close" aria-label="Close modal">
        <X size={18} />
      </button>
    </div>

    <!-- Storage Info Callout -->
    <div style="background: var(--bg-surface-elevated); padding: 8px 24px; border-bottom: 1px solid var(--border-subtle); font-size: 11px; display: flex; align-items: center; gap: 6px; color: var(--text-dim);">
      <HardDrive size={13} color="var(--accent-primary)" />
      <span>The markdown text will be written directly into <code style="font-family: monospace; font-size: 11px; background: var(--bg-input); padding: 1px 5px; border-radius: 3px;">build/object-storage/jobs/&#123;id&#125;.md</code>.</span>
    </div>

    {#if errorMsg}
      <div style="margin: 14px 24px 0; padding: 10px 14px; background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.25); border-radius: var(--radius-sm); color: #ef4444; font-size: 12px;">
        {errorMsg}
      </div>
    {/if}

    <!-- Form Content -->
    <form onsubmit={handleSubmit} style="display: flex; flex-direction: column; flex: 1; overflow-y: auto;">
      <div style="padding: 20px 24px; display: flex; flex-direction: column; gap: 14px;">

        <!-- Row 1: Title & Company -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
          <div>
            <label for="job-title" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Role / Title *
            </label>
            <input
              id="job-title"
              type="text"
              placeholder="e.g. Senior Rust Protocol Engineer"
              bind:value={title}
              required
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
            />
          </div>

          <div>
            <label for="job-company" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Organization / Company *
            </label>
            <input
              id="job-company"
              type="text"
              placeholder="e.g. Sovereign Mesh Technologies"
              bind:value={companyName}
              required
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
            />
          </div>
        </div>

        <!-- Row 2: Department & Employment Type -->
        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px;">
          <div>
            <label for="job-dept" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Department
            </label>
            <select
              id="job-dept"
              bind:value={department}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            >
              <option value="Engineering">Engineering</option>
              <option value="Medicine & Clinical">Medicine & Clinical</option>
              <option value="Infrastructure">Infrastructure</option>
              <option value="Design & UX">Design & UX</option>
              <option value="Security & Cryptography">Security & Cryptography</option>
            </select>
          </div>

          <div>
            <label for="job-type" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Employment Type
            </label>
            <select
              id="job-type"
              bind:value={employmentType}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            >
              <option value="FULL_TIME">Full-time</option>
              <option value="REMOTE">Remote</option>
              <option value="CONTRACT">Contract</option>
              <option value="PART_TIME">Part-time</option>
            </select>
          </div>

          <div>
            <label for="job-exp" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Experience Level
            </label>
            <select
              id="job-exp"
              bind:value={experienceLevel}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            >
              <option value="Senior">Senior</option>
              <option value="Staff / Lead">Staff / Lead</option>
              <option value="Specialist / Attending">Specialist / Attending</option>
              <option value="Fellow / Mid">Fellow / Mid</option>
            </select>
          </div>
        </div>

        <!-- Row 3: Location, Salary, Apply Contact -->
        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px;">
          <div>
            <label for="job-loc" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Location
            </label>
            <input
              id="job-loc"
              type="text"
              placeholder="e.g. Remote (EU) or Timișoara"
              bind:value={location}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            />
          </div>

          <div>
            <label for="job-sal" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Salary / Compensation
            </label>
            <input
              id="job-sal"
              type="text"
              placeholder="e.g. €110,000 - €140,000"
              bind:value={salaryRange}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            />
          </div>

          <div>
            <label for="job-contact" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
              Apply Email / Link
            </label>
            <input
              id="job-contact"
              type="text"
              placeholder="e.g. jobs@mesh.org or URL"
              bind:value={applyContact}
              style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            />
          </div>
        </div>

        <!-- Skills Input -->
        <div>
          <label for="job-skills" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
            Required Skills (comma separated)
          </label>
          <input
            id="job-skills"
            type="text"
            placeholder="e.g. Rust, Distributed Systems, libp2p, Cryptography"
            bind:value={skillsInput}
            style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
          />
        </div>

        <!-- Markdown Description Editor & Live Preview -->
        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
            <label for="job-md" style="font-size: 12px; font-weight: 600; color: var(--text-dim);">
              Job Description (Full Markdown) *
            </label>
            <button
              type="button"
              class="btn-secondary"
              style="font-size: 11px; padding: 3px 8px; display: inline-flex; align-items: center; gap: 4px;"
              onclick={() => showPreview = !showPreview}
            >
              {#if showPreview}
                <Edit3 size={12} />
                <span>Edit Source</span>
              {:else}
                <Eye size={12} />
                <span>Live Preview</span>
              {/if}
            </button>
          </div>

          {#if showPreview}
            <div class="markdown-body job-preview-box">
              {@html renderedPreview}
            </div>
          {:else}
            <textarea
              id="job-md"
              bind:value={descriptionMarkdown}
              rows="10"
              style="width: 100%; padding: 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; font-family: monospace; color: var(--text-main); resize: vertical; line-height: 1.5;"
              placeholder="Write full markdown description, requirements, benefits, and tech stack..."
            ></textarea>
          {/if}
        </div>

      </div>

      <!-- Footer Buttons -->
      <div style="padding: 14px 24px; border-top: 1px solid var(--border-subtle); display: flex; justify-content: flex-end; gap: 10px; background: var(--bg-surface-elevated);">
        <button type="button" class="btn-secondary" onclick={onClose} style="font-size: 12px; padding: 7px 16px;">
          Cancel
        </button>
        <button
          type="submit"
          class="btn-primary"
          style="font-size: 12px; padding: 7px 20px; display: inline-flex; align-items: center; gap: 6px; background: linear-gradient(135deg, var(--accent-primary), #6366f1);"
          disabled={isSaving}
        >
          <Save size={13} />
          <span>{isSaving ? 'Saving to Object Storage...' : 'Publish Announcement'}</span>
        </button>
      </div>
    </form>

  </div>
</div>

<style>
  .post-job-card {
    max-width: 740px;
    width: 95vw;
    max-height: 88vh;
    display: flex;
    flex-direction: column;
  }

  .job-preview-box {
    background: var(--bg-surface-elevated);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-sm);
    padding: 16px;
    min-height: 200px;
    max-height: 350px;
    overflow-y: auto;
    font-size: 13px;
    line-height: 1.6;
  }

  .job-preview-box :global(h3) {
    font-size: 14px;
    font-weight: 700;
    margin: 12px 0 6px;
    color: var(--text-main);
  }

  .job-preview-box :global(h3:first-child) {
    margin-top: 0;
  }

  .job-preview-box :global(ul) {
    padding-left: 18px;
    margin-bottom: 10px;
  }
</style>
