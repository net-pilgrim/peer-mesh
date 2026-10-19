<script>
  import { Building, MapPin, ExternalLink, Plus } from '@lucide/svelte';
  import { fetchCompanies, followEntity } from './api.js';

  let { currentProfile, onTagClick } = $props();

  let companies = $state([]);
  let loading = $state(true);

  $effect(() => {
    loadCompanies();
  });

  async function loadCompanies() {
    loading = true;
    try {
      companies = await fetchCompanies();
    } catch (err) {
      console.error('Failed to load companies:', err);
    } finally {
      loading = false;
    }
  }

  async function handleFollow(companyId) {
    if (!currentProfile) return;
    try {
      await followEntity(currentProfile.id, companyId, 'COMPANY');
      alert('Following company! Their announcements will appear in your network feed.');
    } catch (err) {
      alert('Failed to follow: ' + err.message);
    }
  }
</script>

<div class="card" style="display: flex; flex-direction: column; gap: 20px;">
  <div>
    <h2 style="font-size: 20px; font-weight: 700;">Verified Organizations & Labs</h2>
    <p style="font-size: 13px; color: var(--text-muted);">
      Company hubs without corporate spam. Follow for engineering blogs, open-source releases, and milestones.
    </p>
  </div>

  {#if loading}
    <p style="color: var(--text-muted); font-size: 13px;">Loading organizations...</p>
  {:else}
    <div style="display: flex; flex-direction: column; gap: 16px;">
      {#each companies as comp}
        <div style="padding: 18px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 12px;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 10px;">
            <div style="display: flex; gap: 14px; align-items: center;">
              <img src={comp.logoUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'} alt={comp.name} class="avatar" style="width: 48px; height: 48px; border-radius: var(--radius-sm);" />
              <div>
                <h3 style="font-size: 16px; font-weight: 700;">{comp.name}</h3>
                <div style="font-size: 12px; color: var(--text-dim); display: flex; gap: 8px;">
                  <span>@{comp.handle}</span>
                  <span>•</span>
                  <span>{comp.industry}</span>
                  {#if comp.headquarters}
                    <span>•</span>
                    <span style="display: inline-flex; align-items: center; gap: 3px;">
                      <MapPin size={11} /> {comp.headquarters}
                    </span>
                  {/if}
                </div>
              </div>
            </div>

            <button
              class="btn-primary"
              style="padding: 6px 14px; font-size: 12px; display: inline-flex; align-items: center; gap: 4px;"
              onclick={() => handleFollow(comp.id)}
            >
              <Plus size={13} />
              <span>Follow</span>
            </button>
          </div>

          <p style="font-size: 14px; color: var(--text-main); line-height: 1.5;">
            {comp.descriptionMarkdown}
          </p>

          {#if comp.websiteUrl}
            <div style="font-size: 12px;">
              <a href={comp.websiteUrl} target="_blank" rel="noopener noreferrer" style="display: inline-flex; align-items: center; gap: 5px;">
                <ExternalLink size={12} />
                <span>{comp.websiteUrl}</span>
              </a>
            </div>
          {/if}
        </div>
      {/each}
    </div>
  {/if}
</div>
