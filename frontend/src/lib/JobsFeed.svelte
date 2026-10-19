<script>
  import { onMount } from 'svelte';
  import {
    Briefcase,
    Building,
    MapPin,
    DollarSign,
    Search,
    X,
    Plus,
    Filter,
    CheckCircle2,
    HardDrive,
    Sparkles,
    ExternalLink,
    ChevronRight,
    Tag
  } from '@lucide/svelte';
  import { fetchJobs } from './api.js';

  let { currentProfile, onSelectJob, onOpenPostJob } = $props();

  let jobs = $state([]);
  let loading = $state(true);
  let searchQuery = $state('');
  let activeDepartment = $state('ALL');
  let activeType = $state('ALL');
  let activeTag = $state('');

  const departments = [
    { id: 'ALL', label: 'All Fields' },
    { id: 'Engineering', label: 'Engineering' },
    { id: 'Medicine & Clinical', label: 'Clinical & Medicine' },
    { id: 'Infrastructure', label: 'Infrastructure' },
    { id: 'Design & UX', label: 'Design' }
  ];

  const employmentTypes = [
    { id: 'ALL', label: 'All Types' },
    { id: 'REMOTE', label: 'Remote' },
    { id: 'FULL_TIME', label: 'Full-time' },
    { id: 'CONTRACT', label: 'Contract' }
  ];

  onMount(() => {
    loadJobs();
  });

  export async function loadJobs() {
    loading = true;
    try {
      jobs = await fetchJobs({
        department: activeDepartment,
        type: activeType,
        search: searchQuery,
        tag: activeTag
      });
    } catch (err) {
      console.error('Failed to load jobs:', err);
    } finally {
      loading = false;
    }
  }

  function handleSearchInput(e) {
    searchQuery = e.target.value;
    loadJobs();
  }

  function clearSearch() {
    searchQuery = '';
    loadJobs();
  }

  function setDepartment(dept) {
    activeDepartment = dept;
    loadJobs();
  }

  function setType(t) {
    activeType = t;
    loadJobs();
  }

  function filterByTag(tag) {
    activeTag = activeTag === tag ? '' : tag;
    loadJobs();
  }

  function formatType(t) {
    if (t === 'FULL_TIME') return 'Full-time';
    if (t === 'PART_TIME') return 'Part-time';
    if (t === 'CONTRACT') return 'Contract';
    if (t === 'REMOTE') return 'Remote';
    return t;
  }
</script>

<div class="jobs-feed-container">
  <!-- Jobs Header -->
  <div class="jobs-feed-header">
    <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
      <div style="display: flex; align-items: center; gap: 8px;">
        <div class="jobs-icon-wrap">
          <Briefcase size={16} />
        </div>
        <div>
          <h3 style="font-size: 14px; font-weight: 700; margin: 0; color: var(--text-main); display: flex; align-items: center; gap: 6px;">
            <span>Opportunities</span>
            {#if !loading}
              <span class="jobs-count-pill">{jobs.length}</span>
            {/if}
          </h3>
          <span style="font-size: 11px; color: var(--text-dim);">Object-storage verified positions</span>
        </div>
      </div>

      <button
        class="btn-primary"
        style="font-size: 11px; padding: 5px 9px; display: inline-flex; align-items: center; gap: 4px;"
        onclick={onOpenPostJob}
        title="Publish a new job announcement stored as markdown"
      >
        <Plus size={13} />
        <span>Post Job</span>
      </button>
    </div>

    <!-- Search Input -->
    <div class="jobs-search-box">
      <Search size={13} color="var(--text-dim)" />
      <input
        type="text"
        placeholder="Filter by role, skill, or clinic..."
        value={searchQuery}
        oninput={handleSearchInput}
      />
      {#if searchQuery}
        <button class="jobs-clear-btn" onclick={clearSearch} title="Clear search">
          <X size={12} />
        </button>
      {/if}
    </div>

    <!-- Department Filter Pills -->
    <div class="jobs-filter-scroll">
      {#each departments as d}
        <button
          class="jobs-filter-pill"
          class:active={activeDepartment === d.id}
          onclick={() => setDepartment(d.id)}
        >
          {d.label}
        </button>
      {/each}
    </div>

    <!-- Type Filter Pills -->
    <div style="display: flex; gap: 5px; flex-wrap: wrap;">
      {#each employmentTypes as t}
        <button
          class="jobs-type-pill"
          class:active={activeType === t.id}
          onclick={() => setType(t.id)}
        >
          {t.label}
        </button>
      {/each}
      {#if activeTag}
        <button
          class="jobs-type-pill active"
          onclick={() => filterByTag('')}
          title="Clear skill filter"
        >
          #{activeTag} ✕
        </button>
      {/if}
    </div>
  </div>

  <!-- Job Cards Stream -->
  <div class="jobs-stream">
    {#if loading}
      <div style="padding: 24px; text-align: center; color: var(--text-dim); font-size: 12px;">
        Loading announcements...
      </div>
    {:else if jobs.length === 0}
      <div class="jobs-empty-state">
        <Briefcase size={28} color="var(--text-dim)" style="opacity: 0.5; margin-bottom: 6px;" />
        <div style="font-weight: 600; font-size: 13px; color: var(--text-main);">No opportunities found</div>
        <div style="font-size: 11px; color: var(--text-dim); margin-top: 4px;">
          Try adjusting your department or employment filter.
        </div>
      </div>
    {:else}
      {#each jobs as job (job.id)}
        <div
          class="job-card"
          class:featured={job.featured}
          onclick={() => onSelectJob && onSelectJob(job)}
          onkeydown={(e) => (e.key === 'Enter' || e.key === ' ') && onSelectJob && onSelectJob(job)}
          role="button"
          tabindex="0"
        >
          <!-- Top Row: Company & Badges -->
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 8px;">
            <div style="display: flex; align-items: center; gap: 8px; min-width: 0; flex: 1;">
              <img
                src={job.companyLogoUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'}
                alt={job.companyName}
                class="avatar"
                style="width: 28px; height: 28px; border-radius: var(--radius-xs); flex-shrink: 0;"
              />
              <span style="font-size: 11px; font-weight: 600; color: var(--text-muted); text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">
                {job.companyName}
              </span>
            </div>

            <span class="jobs-dept-badge">
              {job.department}
            </span>
          </div>

          <!-- Title -->
          <div class="job-card-title">
            {job.title}
          </div>

          <!-- Metadata Badges -->
          <div style="display: flex; gap: 6px; flex-wrap: wrap; font-size: 11px; align-items: center;">
            <span style="display: inline-flex; align-items: center; gap: 3px; color: var(--text-dim);">
              <MapPin size={11} /> {job.location}
            </span>
            <span class="job-meta-chip">
              {formatType(job.employmentType)}
            </span>
            {#if job.salaryRange}
              <span class="job-salary-chip">
                {job.salaryRange}
              </span>
            {/if}
          </div>

          <!-- Skills tags -->
          {#if job.skills && job.skills.length > 0}
            <div style="display: flex; gap: 4px; flex-wrap: wrap;">
              {#each job.skills.slice(0, 3) as skill}
                <button
                  type="button"
                  class="job-skill-chip"
                  onclick={(e) => { e.stopPropagation(); filterByTag(skill); }}
                  title="Filter by {skill}"
                >
                  #{skill}
                </button>
              {/each}
              {#if job.skills.length > 3}
                <span style="font-size: 10px; color: var(--text-dim); align-self: center;">
                  +{job.skills.length - 3}
                </span>
              {/if}
            </div>
          {/if}

          <!-- Bottom bar: Object storage indicator & View arrow -->
          <div class="job-card-footer">
            <span class="job-storage-indicator" title="Markdown full spec stored in Object Storage: jobs/{job.id}.md">
              <HardDrive size={10} />
              <span>jobs/{job.id ? job.id.substring(0, 8) : 'raw'}.md</span>
            </span>

            <span class="job-view-cta">
              <span>View</span>
              <ChevronRight size={12} />
            </span>
          </div>
        </div>
      {/each}
    {/if}
  </div>
</div>

<style>
  .jobs-feed-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .jobs-feed-header {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-subtle);
  }

  .jobs-icon-wrap {
    width: 28px;
    height: 28px;
    border-radius: var(--radius-sm);
    background: rgba(99, 102, 241, 0.12);
    color: var(--accent-primary);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .jobs-count-pill {
    font-size: 10px;
    background: var(--bg-surface-elevated);
    border: 1px solid var(--border-subtle);
    padding: 1px 6px;
    border-radius: var(--radius-full);
    color: var(--accent-primary);
    font-weight: 600;
  }

  .jobs-search-box {
    position: relative;
    display: flex;
    align-items: center;
  }

  .jobs-search-box input {
    width: 100%;
    padding: 6px 26px 6px 26px;
    background: var(--bg-input);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-sm);
    font-size: 12px;
    color: var(--text-main);
  }

  .jobs-search-box :global(svg:first-child) {
    position: absolute;
    left: 8px;
    pointer-events: none;
  }

  .jobs-clear-btn {
    position: absolute;
    right: 6px;
    background: transparent;
    border: none;
    color: var(--text-dim);
    cursor: pointer;
    padding: 2px;
    display: flex;
    align-items: center;
  }

  .jobs-filter-scroll {
    display: flex;
    gap: 4px;
    overflow-x: auto;
    padding-bottom: 2px;
    scrollbar-width: none;
  }

  .jobs-filter-scroll::-webkit-scrollbar {
    display: none;
  }

  .jobs-filter-pill {
    padding: 3px 8px;
    font-size: 11px;
    white-space: nowrap;
    border-radius: var(--radius-full);
    background: var(--bg-surface-elevated);
    border: 1px solid var(--border-subtle);
    color: var(--text-muted);
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .jobs-filter-pill.active {
    background: var(--accent-primary);
    color: #fff;
    border-color: var(--accent-primary);
    font-weight: 600;
  }

  .jobs-type-pill {
    padding: 2px 7px;
    font-size: 10px;
    border-radius: var(--radius-xs);
    background: transparent;
    border: 1px solid var(--border-subtle);
    color: var(--text-dim);
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .jobs-type-pill.active {
    background: rgba(99, 102, 241, 0.12);
    color: var(--accent-primary);
    border-color: rgba(99, 102, 241, 0.3);
    font-weight: 600;
  }

  .jobs-stream {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .jobs-empty-state {
    padding: 28px 16px;
    text-align: center;
    background: var(--bg-surface-elevated);
    border: 1px dashed var(--border-subtle);
    border-radius: var(--radius-md);
  }

  .job-card {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 12px;
    background: var(--bg-surface);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-md);
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .job-card:hover {
    border-color: var(--accent-primary);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }

  .job-card.featured {
    border-left: 3px solid var(--accent-primary);
    background: linear-gradient(to right, rgba(99, 102, 241, 0.02), var(--bg-surface));
  }

  .jobs-dept-badge {
    font-size: 10px;
    padding: 1px 6px;
    background: var(--bg-surface-elevated);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-xs);
    color: var(--text-dim);
    font-weight: 500;
    white-space: nowrap;
  }

  .job-card-title {
    font-size: 13px;
    font-weight: 700;
    color: var(--text-main);
    line-height: 1.3;
    transition: color var(--transition-fast);
  }

  .job-card:hover .job-card-title {
    color: var(--accent-primary);
  }

  .job-meta-chip {
    padding: 1px 5px;
    background: var(--bg-surface-elevated);
    border-radius: 3px;
    color: var(--text-dim);
  }

  .job-salary-chip {
    padding: 1px 5px;
    background: rgba(16, 185, 129, 0.1);
    border: 1px solid rgba(16, 185, 129, 0.2);
    border-radius: 3px;
    color: #059669;
    font-weight: 600;
  }

  .job-skill-chip {
    font-size: 10px;
    padding: 1px 5px;
    background: var(--bg-surface-elevated);
    border: 1px solid var(--border-subtle);
    border-radius: 3px;
    color: var(--text-dim);
    cursor: pointer;
  }

  .job-skill-chip:hover {
    border-color: var(--accent-primary);
    color: var(--accent-primary);
  }

  .job-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid var(--border-subtle);
    padding-top: 7px;
    margin-top: 2px;
  }

  .job-storage-indicator {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 10px;
    color: var(--text-dim);
    font-family: monospace;
  }

  .job-view-cta {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: var(--accent-primary);
    font-weight: 600;
  }
</style>
