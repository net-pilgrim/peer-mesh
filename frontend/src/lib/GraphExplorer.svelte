<script>
  import {
    Network,
    Compass,
    UserPlus,
    UserMinus,
    MessageSquare,
    Briefcase,
    GraduationCap,
    Share2,
    Users,
    Layers
  } from '@lucide/svelte';
  import {
    fetch1stDegreeConnections,
    fetch2ndDegreeSuggestions,
    fetchShortestPath,
    connectUsers,
    disconnectUsers,
    fetchProfiles
  } from './api.js';

  let { currentProfile, onOpenContact, onOpenExperience } = $props();

  let firstDegree = $state([]);
  let suggestions = $state([]);
  let allProfiles = $state([]);
  let selectedTargetId = $state('');
  let pathResult = $state(null);
  let loadingPath = $state(false);
  let loadingGraph = $state(true);

  $effect(() => {
    if (currentProfile?.id) {
      loadGraph();
    }
  });

  async function loadGraph() {
    loadingGraph = true;
    try {
      const [f1, s2, all] = await Promise.all([
        fetch1stDegreeConnections(currentProfile.id),
        fetch2ndDegreeSuggestions(currentProfile.id, 10),
        fetchProfiles()
      ]);
      firstDegree = f1;
      suggestions = s2;
      allProfiles = all.filter(p => p.id !== currentProfile.id);
      if (allProfiles.length > 0 && !selectedTargetId) {
        selectedTargetId = allProfiles[0].id;
      }
    } catch (err) {
      console.error('Failed to load graph data:', err);
    } finally {
      loadingGraph = false;
    }
  }

  async function handleConnect(targetId) {
    try {
      await connectUsers(currentProfile.id, targetId);
      await loadGraph();
    } catch (err) {
      alert('Failed to connect: ' + err.message);
    }
  }

  async function handleDisconnect(targetId) {
    if (!confirm('Remove this connection?')) return;
    try {
      await disconnectUsers(currentProfile.id, targetId);
      await loadGraph();
    } catch (err) {
      alert('Failed to disconnect: ' + err.message);
    }
  }

  async function calculateShortestPath() {
    if (!selectedTargetId) return;
    loadingPath = true;
    try {
      pathResult = await fetchShortestPath(currentProfile.id, selectedTargetId);
    } catch (err) {
      alert('Path lookup failed: ' + err.message);
    } finally {
      loadingPath = false;
    }
  }
</script>

<div class="card graph-explorer" style="display: flex; flex-direction: column; gap: 24px;">
  <div>
    <div style="display: flex; align-items: center; justify-content: space-between;">
      <div>
        <h2 style="font-size: 20px; font-weight: 700;">Network Graph Explorer</h2>
        <p style="font-size: 13px; color: var(--text-muted);">
          Database-native graph traversals: 1st-degree neighbors, 2nd-degree mutuals & shortest paths.
        </p>
      </div>
      <span class="brand-badge" style="background: rgba(16, 185, 129, 0.1); color: var(--accent-success); border: 1px solid rgba(16, 185, 129, 0.3);">
        PostgreSQL Graph Engine Active
      </span>
    </div>
  </div>

  <!-- Interactive Shortest Path Finder -->
  <div style="padding: 16px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md);">
    <h3 style="font-size: 15px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; gap: 6px;">
      <Compass size={16} />
      <span>Network Distance & Path Traversal (BFS / Recursive CTE)</span>
    </h3>
    <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
      <span style="font-size: 13px; color: var(--text-muted);">Path from @{currentProfile?.handle} to:</span>
      <select
        bind:value={selectedTargetId}
        style="padding: 6px 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main); font-size: 13px;"
      >
        {#each allProfiles as p}
          <option value={p.id}>@{p.handle} ({p.displayName})</option>
        {/each}
      </select>
      <button class="btn-primary" onclick={calculateShortestPath} disabled={loadingPath} style="padding: 6px 14px; font-size: 13px; display: inline-flex; align-items: center; gap: 4px;">
        <Compass size={13} />
        <span>{loadingPath ? 'Calculating...' : 'Find Path'}</span>
      </button>
    </div>

    {#if pathResult}
      <div style="margin-top: 14px; padding: 12px; background: var(--bg-input); border-radius: var(--radius-sm); font-size: 14px;">
        {#if pathResult.distance === -1}
          <span style="color: var(--text-dim);">No path found within 4 degrees.</span>
        {:else if pathResult.distance === 0}
          <span style="color: var(--accent-success);">This is you! (0 degrees)</span>
        {:else}
          <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
            <strong>Network Distance: {pathResult.distance} hop{pathResult.distance > 1 ? 's' : ''}</strong>
            <span style="color: var(--text-dim);">•</span>
            <span>Path:</span>
            {#each pathResult.pathHandles as handle, i}
              <span class="brand-badge" style="background: var(--bg-surface-hover); color: var(--text-main);">
                @{handle}
              </span>
              {#if i < pathResult.pathHandles.length - 1}
                <span style="color: var(--accent-primary);">→</span>
              {/if}
            {/each}
          </div>
        {/if}
      </div>
    {/if}
  </div>

  <!-- 1st-Degree Connections -->
  <div>
    <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
      <Users size={16} />
      <span>Direct 1st-Degree Connections</span>
      <span class="brand-badge">{firstDegree.length}</span>
    </h3>

    {#if loadingGraph}
      <p style="color: var(--text-muted); font-size: 13px;">Loading graph edges...</p>
    {:else if firstDegree.length === 0}
      <p style="color: var(--text-dim); font-size: 13px;">No direct connections yet. Connect with members below!</p>
    {:else}
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px;">
        {#each firstDegree as node}
          <div style="padding: 14px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 10px;">
            <div style="display: flex; gap: 10px; align-items: center;">
              <img src={node.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'} alt={node.displayName} class="avatar" style="width: 38px; height: 38px;" />
              <div>
                <div style="font-weight: 600; font-size: 14px;">{node.displayName}</div>
                <div style="font-size: 12px; color: var(--text-dim);">@{node.handle}</div>
              </div>
            </div>

            <p style="font-size: 12px; color: var(--text-muted); line-height: 1.4;">
              {node.headline || 'Member'}
            </p>

            <div style="display: flex; gap: 6px; align-items: center; justify-content: space-between; margin-top: auto; flex-wrap: wrap;">
              <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                <button
                  class="contact-badge"
                  onclick={() => onOpenExperience && onOpenExperience(node, 'all')}
                  title="View complete timeline, career highlights, and education"
                >
                  <Layers size={12} color="var(--accent-primary)" />
                  <span>Timeline</span>
                </button>
                <button
                  class="contact-badge"
                  onclick={() => onOpenContact && onOpenContact(node.handle)}
                >
                  <MessageSquare size={12} />
                  <span>Message</span>
                </button>
              </div>
              <button
                class="btn-icon"
                style="color: var(--accent-danger); font-size: 11px; padding: 4px 6px; display: inline-flex; align-items: center; gap: 4px;"
                onclick={() => handleDisconnect(node.id)}
                title="Remove Connection"
              >
                <UserMinus size={12} />
                <span>Disconnect</span>
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>

  <!-- 2nd-Degree Suggestions -->
  <div>
    <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
      <Network size={16} />
      <span>2nd-Degree Mutual Connections (People You May Know)</span>
      <span class="brand-badge">{suggestions.length}</span>
    </h3>

    {#if suggestions.length === 0}
      <p style="color: var(--text-dim); font-size: 13px;">No 2nd-degree suggestions at the moment.</p>
    {:else}
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px;">
        {#each suggestions as sug}
          <div style="padding: 14px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 10px;">
            <div style="display: flex; gap: 10px; align-items: center;">
              <img src={sug.avatarUrl || 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200'} alt={sug.displayName} class="avatar" style="width: 38px; height: 38px;" />
              <div>
                <div style="font-weight: 600; font-size: 14px;">{sug.displayName}</div>
                <div style="font-size: 12px; color: var(--text-dim);">@{sug.handle}</div>
              </div>
            </div>

            <div style="font-size: 11px; color: var(--accent-primary); font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
              <Share2 size={12} />
              <span>{sug.mutualCount} mutual connection{sug.mutualCount > 1 ? 's' : ''} in network</span>
            </div>

            <p style="font-size: 12px; color: var(--text-muted); line-height: 1.4;">
              {sug.headline || 'Engineer'}
            </p>

            <div style="display: flex; gap: 6px; margin-top: auto; flex-wrap: wrap;">
              <button
                class="btn-secondary"
                style="flex: 1; justify-content: center; font-size: 11px; padding: 6px 6px; display: inline-flex; align-items: center; gap: 4px;"
                onclick={() => onOpenExperience && onOpenExperience(sug, 'all')}
                title="View complete career timeline, highlights, and education"
              >
                <Layers size={12} color="var(--accent-primary)" />
                <span>Timeline</span>
              </button>
              <button
                class="btn-primary"
                style="flex: 1; justify-content: center; font-size: 11px; padding: 6px 4px; display: inline-flex; align-items: center; gap: 4px;"
                onclick={() => handleConnect(sug.id)}
              >
                <UserPlus size={12} />
                <span>Connect</span>
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>
