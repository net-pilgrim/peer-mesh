<script>
  import { marked } from 'marked';
  import {
    Briefcase,
    Building,
    MapPin,
    DollarSign,
    CheckCircle2,
    HardDrive,
    ExternalLink,
    Mail,
    X,
    FileText,
    Code,
    Sparkles,
    Calendar
  } from '@lucide/svelte';
  import { fetchJobStorage } from './api.js';

  let { job, onClose } = $props();

  let viewRawStorage = $state(false);
  let rawStorageContent = $state('');
  let loadingRaw = $state(false);

  const renderedMarkdown = $derived(
    marked.parse(job.descriptionMarkdown || '', { gfm: true, breaks: true })
  );

  async function toggleRawStorage() {
    viewRawStorage = !viewRawStorage;
    if (viewRawStorage && !rawStorageContent) {
      loadingRaw = true;
      try {
        const res = await fetchJobStorage(job.id);
        rawStorageContent = res.bodyMarkdown || job.descriptionMarkdown || '';
      } catch (err) {
        rawStorageContent = job.descriptionMarkdown || 'Could not fetch raw storage: ' + err.message;
      } finally {
        loadingRaw = false;
      }
    }
  }

  function formatType(t) {
    if (t === 'FULL_TIME') return 'Full-time';
    if (t === 'PART_TIME') return 'Part-time';
    if (t === 'CONTRACT') return 'Contract';
    if (t === 'REMOTE') return 'Remote';
    return t || 'Full-time';
  }
</script>

<div class="modal-backdrop" onclick={onClose} onkeydown={(e) => e.key === 'Escape' && onClose()} role="presentation">
  <div class="modal-card job-detail-card" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()} role="dialog" aria-modal="true" tabindex="-1">

    <!-- Modal Header -->
    <div class="modal-header" style="border-bottom: 1px solid var(--border-subtle); padding: 18px 24px;">
      <div style="display: flex; gap: 14px; align-items: flex-start; flex: 1; min-width: 0;">
        <img
          src={job.companyLogoUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'}
          alt={job.companyName}
          class="avatar"
          style="width: 48px; height: 48px; border-radius: var(--radius-sm); flex-shrink: 0;"
        />
        <div style="min-width: 0; flex: 1;">
          <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
            <span style="font-size: 13px; font-weight: 600; color: var(--text-dim);">
              {job.companyName}
            </span>
            <span class="brand-badge" style="font-size: 10px;">
              {job.department}
            </span>
            {#if job.featured}
              <span class="brand-badge" style="background: rgba(16, 185, 129, 0.12); color: #059669; border-color: rgba(16, 185, 129, 0.3); font-size: 10px;">
                Verified Partner
              </span>
            {/if}
          </div>

          <h2 style="font-size: 18px; font-weight: 800; margin: 4px 0 6px; color: var(--text-main); line-height: 1.3;">
            {job.title}
          </h2>

          <div style="display: flex; gap: 10px; flex-wrap: wrap; font-size: 12px; color: var(--text-muted); align-items: center;">
            <span style="display: inline-flex; align-items: center; gap: 4px;">
              <MapPin size={13} color="var(--accent-primary)" />
              {job.location}
            </span>
            <span>•</span>
            <span style="display: inline-flex; align-items: center; gap: 4px;">
              <Briefcase size={13} />
              {formatType(job.employmentType)} ({job.experienceLevel || 'Senior'})
            </span>
            {#if job.salaryRange}
              <span>•</span>
              <span style="font-weight: 600; color: var(--accent-success); display: inline-flex; align-items: center; gap: 3px;">
                <DollarSign size={13} />
                {job.salaryRange}
              </span>
            {/if}
          </div>
        </div>
      </div>

      <button class="btn-icon" onclick={onClose} title="Close" aria-label="Close modal">
        <X size={18} />
      </button>
    </div>

    <!-- Object Storage Badge Bar -->
    <div class="job-storage-bar">
      <div style="display: flex; align-items: center; gap: 8px;">
        <div style="width: 22px; height: 22px; border-radius: 4px; background: rgba(99, 102, 241, 0.12); color: var(--accent-primary); display: flex; align-items: center; justify-content: center;">
          <HardDrive size={13} />
        </div>
        <div style="font-size: 11px;">
          <span style="color: var(--text-dim);">Object Storage Document:</span>
          <code style="background: var(--bg-input); padding: 1px 6px; border-radius: 3px; color: var(--text-main); font-family: monospace; font-size: 11px; margin-left: 4px;">
            {job.storageKey || `jobs/${job.id}.md`}
          </code>
        </div>
      </div>

      <button
        type="button"
        class="btn-secondary"
        style="font-size: 11px; padding: 3px 8px; display: inline-flex; align-items: center; gap: 4px;"
        onclick={toggleRawStorage}
      >
        {#if viewRawStorage}
          <FileText size={12} />
          <span>Rendered View</span>
        {:else}
          <Code size={12} />
          <span>Inspect Raw Markdown</span>
        {/if}
      </button>
    </div>

    <!-- Modal Body -->
    <div class="job-modal-content">
      {#if viewRawStorage}
        <div style="display: flex; flex-direction: column; gap: 8px;">
          <div style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: flex; align-items: center; justify-content: space-between;">
            <span>Raw Markdown from Object Storage:</span>
            <span style="font-family: monospace; font-size: 11px; color: var(--accent-primary);">text/markdown; charset=UTF-8</span>
          </div>
          {#if loadingRaw}
            <div style="padding: 20px; text-align: center; color: var(--text-dim);">Loading from storage...</div>
          {:else}
            <pre class="raw-markdown-pre"><code>{rawStorageContent}</code></pre>
          {/if}
        </div>
      {:else}
        <!-- Rendered Markdown Body -->
        <div class="markdown-body job-markdown-render">
          {@html renderedMarkdown}
        </div>
      {/if}

      <!-- Skills Requirements -->
      {#if job.skills && job.skills.length > 0}
        <div style="border-top: 1px solid var(--border-subtle); padding-top: 16px; margin-top: 16px;">
          <h4 style="font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-dim); margin: 0 0 8px;">
            Required Skills & Protocol Domains
          </h4>
          <div style="display: flex; gap: 6px; flex-wrap: wrap;">
            {#each job.skills as skill}
              <span class="filter-chip active" style="font-size: 11px; cursor: default;">
                #{skill}
              </span>
            {/each}
          </div>
        </div>
      {/if}
    </div>

    <!-- Modal Footer / Apply Bar -->
    <div class="modal-footer" style="padding: 14px 24px; border-top: 1px solid var(--border-subtle); display: flex; align-items: center; justify-content: space-between; gap: 12px; background: var(--bg-surface-elevated);">
      <div style="font-size: 12px; color: var(--text-dim);">
        {#if job.applyContact}
          <span>Apply via: <strong style="color: var(--text-main); font-family: monospace;">{job.applyContact}</strong></span>
        {:else}
          <span>Contact the organization directly</span>
        {/if}
      </div>

      <div style="display: flex; gap: 8px; align-items: center;">
        <button class="btn-secondary" onclick={onClose} style="font-size: 12px; padding: 7px 14px;">
          Close
        </button>

        {#if job.applyContact}
          {#if job.applyContact.startsWith('http')}
            <a
              href={job.applyContact}
              target="_blank"
              rel="noopener noreferrer"
              class="btn-primary"
              style="font-size: 12px; padding: 7px 16px; display: inline-flex; align-items: center; gap: 6px; text-decoration: none;"
            >
              <span>Apply Online</span>
              <ExternalLink size={13} />
            </a>
          {:else if job.applyContact.includes('@')}
            <a
              href="mailto:{job.applyContact}?subject=Application: {encodeURIComponent(job.title)}"
              class="btn-primary"
              style="font-size: 12px; padding: 7px 16px; display: inline-flex; align-items: center; gap: 6px; text-decoration: none;"
            >
              <Mail size={13} />
              <span>Apply via Email</span>
            </a>
          {:else}
            <button
              class="btn-primary"
              style="font-size: 12px; padding: 7px 16px; display: inline-flex; align-items: center; gap: 6px;"
              onclick={() => alert(`Connect directly via Matrix or sovereign contact: ${job.applyContact}`)}
            >
              <span>Connect & Apply</span>
            </button>
          {/if}
        {/if}
      </div>
    </div>

  </div>
</div>

<style>
  .job-detail-card {
    max-width: 720px;
    width: 95vw;
    max-height: 85vh;
    display: flex;
    flex-direction: column;
  }

  .job-storage-bar {
    background: var(--bg-surface-elevated);
    padding: 8px 24px;
    border-bottom: 1px solid var(--border-subtle);
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .job-modal-content {
    padding: 24px;
    overflow-y: auto;
    flex: 1;
  }

  .raw-markdown-pre {
    background: var(--bg-input);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-sm);
    padding: 16px;
    font-family: monospace;
    font-size: 12px;
    line-height: 1.5;
    color: var(--text-main);
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 400px;
    overflow-y: auto;
  }

  .job-markdown-render {
    line-height: 1.6;
    color: var(--text-main);
  }

  .job-markdown-render :global(h3) {
    font-size: 15px;
    font-weight: 700;
    margin: 16px 0 8px;
    color: var(--text-main);
  }

  .job-markdown-render :global(h3:first-child) {
    margin-top: 0;
  }

  .job-markdown-render :global(p) {
    margin-bottom: 12px;
    font-size: 13px;
  }

  .job-markdown-render :global(ul) {
    margin-bottom: 14px;
    padding-left: 20px;
  }

  .job-markdown-render :global(li) {
    margin-bottom: 6px;
    font-size: 13px;
  }

  .job-markdown-render :global(code) {
    background: var(--bg-input);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
  }
</style>
