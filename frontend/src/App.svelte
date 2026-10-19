<script>
  import { onMount } from 'svelte';
  import {
    Rss,
    Network,
    Building,
    ShieldCheck,
    Briefcase,
    GraduationCap,
    Sun,
    Moon,
    Search,
    X,
    Table,
    Image,
    Link2,
    FileText,
    MessageSquare,
    UserPlus,
    ExternalLink,
    MapPin,
    Stethoscope,
    Sparkles,
    Layers,
    KeyRound,
    LogOut,
    CheckCircle2,
    UserCheck,
    Pencil
  } from '@lucide/svelte';
  import {
    fetchFeed,
    fetchProfiles,
    searchUnified,
    connectUsers,
    followEntity,
    dexCallback,
    fetchAuthSession,
    dexLogout
  } from './lib/api.js';
  import UnifiedComposer from './lib/UnifiedComposer.svelte';
  import PostCard from './lib/PostCard.svelte';
  import GraphExplorer from './lib/GraphExplorer.svelte';
  import CompaniesView from './lib/CompaniesView.svelte';
  import DecentralizedModal from './lib/DecentralizedModal.svelte';
  import GdprModal from './lib/GdprModal.svelte';
  import WorkExperienceModal from './lib/WorkExperienceModal.svelte';
  import DexLoginModal from './lib/DexLoginModal.svelte';
  import LoginView from './lib/LoginView.svelte';
  import JobsFeed from './lib/JobsFeed.svelte';
  import JobDetailModal from './lib/JobDetailModal.svelte';
  import PostJobModal from './lib/PostJobModal.svelte';

  // Navigation State
  let activeTab = $state('feed'); // 'feed' | 'graph' | 'companies'
  let currentTheme = $state(typeof localStorage !== 'undefined' && localStorage.getItem('peermesh-theme') ? localStorage.getItem('peermesh-theme') : 'light');
  let isOnline = $state(navigator.onLine);

  // Authentication & Session State
  let authChecking = $state(true);
  let authSession = $state(null);
  let isGuest = $state(false);
  let showDexLoginModal = $state(false);

  // Identity Profiles State
  let profiles = $state([]);
  const currentProfile = $derived(authSession?.profile || null);
  const isLoggedIn = $derived(Boolean(authSession && currentProfile));

  // Feed State
  let posts = $state([]);
  let loadingFeed = $state(true);
  let feedType = $state('all'); // 'all' | 'companies'
  let activeTag = $state('');
  let filterTables = $state(null);
  let filterImages = $state(null);
  let filterLinks = $state(null);

  // Unified Search State
  let searchQuery = $state('');
  let searchResult = $state(null);
  let searchCategory = $state('all'); // 'all' | 'people' | 'companies' | 'posts'
  let isSearching = $derived(!!searchQuery.trim());

  // Modals & Panels
  let contactTargetHandle = $state(null);
  let showGdprModal = $state(false);
  let workExpTargetProfile = $state(null);
  let workExpInitialTab = $state('experience');
  let selectedJob = $state(null);
  let showPostJobModal = $state(false);
  let jobsFeedRef = $state(null);

  onMount(async () => {
    document.documentElement.setAttribute('data-theme', currentTheme);

    // Listen to network status
    window.addEventListener('online', () => isOnline = true);
    window.addEventListener('offline', () => isOnline = false);

    // Load initial profiles
    try {
      profiles = await fetchProfiles();
    } catch (err) {
      console.error('Failed to load profiles:', err);
    }

    // Handle Dex OIDC callback code if redirected back from Dex portal
    const urlParams = new URLSearchParams(window.location.search);
    const authCode = urlParams.get('code');
    if (authCode) {
      try {
        const res = await dexCallback({
          code: authCode,
          redirectUri: window.location.origin + window.location.pathname
        });
        if (res.success && res.sessionId) {
          localStorage.setItem('peermesh-dex-session', res.sessionId);
          authSession = res;
          window.history.replaceState({}, document.title, window.location.pathname);
        }
      } catch (err) {
        console.error('Dex callback authentication failed:', err);
      }
    } else {
      // Check for existing Dex session in localStorage
      const savedSessionId = localStorage.getItem('peermesh-dex-session');
      if (savedSessionId) {
        try {
          const sessionRes = await fetchAuthSession(savedSessionId);
          if (sessionRes.active && sessionRes.profile) {
            authSession = sessionRes;
          } else {
            localStorage.removeItem('peermesh-dex-session');
          }
        } catch (err) {
          console.warn('Session verification failed:', err);
          localStorage.removeItem('peermesh-dex-session');
        }
      }
    }

    authChecking = false;
    loadPosts();
  });

  function handleLoginSuccess(loginRes) {
    authSession = loginRes;
    if (loginRes.sessionId) {
      localStorage.setItem('peermesh-dex-session', loginRes.sessionId);
    }
    isGuest = false;
    showDexLoginModal = false;
    loadPosts();
  }

  async function handleLogout() {
    if (authSession?.sessionId) {
      await dexLogout(authSession.sessionId).catch(() => {});
    }
    localStorage.removeItem('peermesh-dex-session');
    authSession = null;
    isGuest = false;
    loadPosts();
  }

  async function loadPosts() {
    loadingFeed = true;
    try {
      if (searchQuery.trim()) {
        searchResult = await searchUnified(searchQuery.trim());
        posts = searchResult.posts || [];
      } else {
        searchResult = null;
        const res = await fetchFeed({
          type: feedType,
          tag: activeTag,
          hasTables: filterTables,
          hasImages: filterImages,
          hasLinks: filterLinks,
          viewerId: currentProfile?.id
        });
        posts = res.data;
      }
    } catch (err) {
      console.error('Failed to load posts/search:', err);
    } finally {
      loadingFeed = false;
    }
  }

  function handlePostCreated(newPost) {
    posts = [newPost, ...posts];
  }

  function toggleTheme() {
    currentTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', currentTheme);
    try {
      localStorage.setItem('peermesh-theme', currentTheme);
    } catch (_) {}
  }

  function handleSearchInput(e) {
    searchQuery = e.target.value;
    loadPosts();
  }

  function setSearchQuery(val) {
    searchQuery = val;
    activeTab = 'feed';
    searchCategory = 'all';
    loadPosts();
  }

  function clearSearch() {
    searchQuery = '';
    searchResult = null;
    searchCategory = 'all';
    loadPosts();
  }

  async function handleConnect(targetId) {
    if (!currentProfile) return;
    try {
      await connectUsers(currentProfile.id, targetId);
      alert('Connection established on decentralized network graph!');
    } catch (err) {
      alert('Failed to connect: ' + err.message);
    }
  }

  async function handleFollow(companyId) {
    if (!currentProfile) return;
    try {
      await followEntity(currentProfile.id, companyId, 'COMPANY');
      alert('Following organization! Their updates will appear in your feed.');
    } catch (err) {
      alert('Failed to follow: ' + err.message);
    }
  }

  function setTagFilter(tag) {
    activeTag = activeTag === tag ? '' : tag;
    loadPosts();
  }


  function openExperienceByHandle(handle, initialTab = 'all') {
    workExpInitialTab = initialTab;
    const found = profiles.find(p => p.handle === handle);
    if (found) {
      workExpTargetProfile = found;
    } else {
      workExpTargetProfile = { handle, displayName: handle };
    }
  }

  function handleProfileUpdated(updated) {
    profiles = profiles.map(p => p.id === updated.id ? updated : p);
    loadPosts();
  }
</script>

{#if authChecking}
  <div style="min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; background: var(--bg-main); color: var(--text-dim);">
    <div style="width: 36px; height: 36px; border: 3px solid var(--border-subtle); border-top-color: var(--accent-primary); border-radius: 50%; animation: spin 0.8s linear infinite;"></div>
    <span style="font-size: 13px; font-weight: 500;">Verifying sovereign session...</span>
  </div>
{:else if !isLoggedIn && !isGuest}
  <LoginView
    onLoginSuccess={handleLoginSuccess}
    onContinueAsGuest={() => { isGuest = true; loadPosts(); }}
  />
{:else}
<div class="app-layout">
  <!-- Left Side Dock / Navigation -->
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-logo">P</div>
      <div class="brand-text">
        <h1>PeerMesh</h1>
        <span class="brand-badge">GDPR Sovereign</span>
      </div>
    </div>

    <ul class="nav-menu">
      <li class="nav-item" class:active={activeTab === 'feed'}>
        <button onclick={() => activeTab = 'feed'}>
          <Rss size={18} />
          <span>Unified Feed</span>
        </button>
      </li>

      <li class="nav-item" class:active={activeTab === 'graph'}>
        <button onclick={() => activeTab = 'graph'}>
          <Network size={18} />
          <span>Network Graph</span>
        </button>
      </li>

      <li class="nav-item" class:active={activeTab === 'companies'}>
        <button onclick={() => activeTab = 'companies'}>
          <Building size={18} />
          <span>Organizations</span>
        </button>
      </li>

      <li class="nav-item">
        <button onclick={() => showGdprModal = true}>
          <ShieldCheck size={18} />
          <span>Privacy & GDPR</span>
        </button>
      </li>
    </ul>

    <!-- Active Profile & Switcher -->
    <div style="margin-top: auto; padding-top: 16px; border-top: 1px solid var(--border-subtle); display: flex; flex-direction: column; gap: 10px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 11px; text-transform: uppercase; color: var(--text-dim); font-weight: 700; letter-spacing: 0.5px;">
          Active Persona
        </span>
        {#if isLoggedIn}
          <span class="brand-badge" style="background: rgba(16, 185, 129, 0.12); color: #059669; border-color: rgba(16, 185, 129, 0.3); font-size: 10px; display: inline-flex; align-items: center; gap: 3px;" title="Authenticated via Dex OIDC IDP">
            <CheckCircle2 size={10} /> Dex Verified
          </span>
        {/if}
      </div>

      {#if isLoggedIn}
        <div style="display: flex; gap: 8px; align-items: center; justify-content: space-between; padding: 7px 9px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md);">
          <div style="display: flex; gap: 9px; align-items: center; min-width: 0; flex: 1;">
            <img
              src={currentProfile.avatarUrl}
              alt={currentProfile.displayName}
              class="avatar"
              style="width: 36px; height: 36px; flex-shrink: 0;"
            />
            <div style="overflow: hidden; flex: 1; min-width: 0;">
              <div style="font-weight: 600; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; overflow: hidden;" title={currentProfile.displayName}>{currentProfile.displayName}</div>
              {#if currentProfile.headline}
                <div style="font-size: 11px; color: var(--accent-primary); font-weight: 500; text-overflow: ellipsis; white-space: nowrap; overflow: hidden;" title={currentProfile.headline}>
                  {currentProfile.headline}
                </div>
              {/if}
              <div style="font-size: 10px; color: var(--text-dim); text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">
                @{currentProfile.handle}{#if authSession?.email} • {authSession.email}{/if}
              </div>
            </div>
          </div>

          <!-- Sleek Icon Actions -->
          <div style="display: flex; align-items: center; gap: 4px; flex-shrink: 0;">
            <button
              class="sleek-action-btn edit-btn"
              onclick={() => { workExpInitialTab = 'personal'; workExpTargetProfile = currentProfile; }}
              title="Edit Profile — Update your sovereign identity, career timeline, education, and highlights"
              aria-label="Edit Profile"
            >
              <Pencil size={15} />
            </button>

            <button
              class="sleek-action-btn logout-btn"
              onclick={handleLogout}
              title="Log Out — End your sovereign session and clear credentials from Redis"
              aria-label="Log Out"
            >
              <LogOut size={15} />
            </button>
          </div>
        </div>
      {:else}
        <div style="background: var(--bg-surface-elevated); border: 1px dashed var(--border-subtle); border-radius: var(--radius-sm); padding: 12px 10px; text-align: center; display: flex; flex-direction: column; gap: 8px;">
          <div style="font-weight: 700; font-size: 12px; color: var(--text-main);">Guest Mode</div>
          <div style="font-size: 11px; color: var(--text-dim); line-height: 1.4;">
            Viewing sovereign network in read-only mode.
          </div>
          <button
            class="btn-primary"
            style="font-size: 12px; padding: 6px 12px; width: 100%; display: inline-flex; align-items: center; justify-content: center; gap: 6px;"
            onclick={() => { isGuest = false; }}
          >
            <KeyRound size={13} />
            <span>Sign In</span>
          </button>
        </div>
      {/if}

      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px;">
        <button class="btn-icon" onclick={toggleTheme} title="Toggle Theme" style="font-size: 12px; padding: 4px 8px; display: inline-flex; align-items: center; gap: 5px;">
          {#if currentTheme === 'light'}
            <Moon size={14} /> <span>Dark</span>
          {:else}
            <Sun size={14} /> <span>Light</span>
          {/if}
        </button>

        <span style="font-size: 11px; color: {isOnline ? 'var(--accent-success)' : 'var(--accent-warning)'}; display: flex; align-items: center; gap: 4px;">
          <span style="width: 6px; height: 6px; border-radius: 50%; background: {isOnline ? 'var(--accent-success)' : 'var(--accent-warning)'};"></span>
          {isOnline ? 'Online PWA' : 'Offline Mode'}
        </span>
      </div>
    </div>
  </aside>

  <!-- Center Main Content Stream -->
  <main class="main-workspace">
    <div class="content-column">
      <!-- Search & Filters Bar -->
      <div class="feed-header">
        <!-- Dex OIDC & Redis Session Bar -->
        <div style="background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: 8px 14px; display: flex; align-items: center; justify-content: space-between; gap: 12px; font-size: 12px; flex-wrap: wrap;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <div style="width: 24px; height: 24px; border-radius: 50%; background: {isLoggedIn ? 'rgba(16, 185, 129, 0.12)' : 'var(--accent-subtle)'}; display: flex; align-items: center; justify-content: center; color: {isLoggedIn ? 'var(--accent-success)' : 'var(--accent-primary)'};">
              {#if isLoggedIn}
                <CheckCircle2 size={14} />
              {:else}
                <KeyRound size={14} />
              {/if}
            </div>
            <div>
              {#if isLoggedIn}
                <span style="font-weight: 600; color: var(--text-main);">
                  Dex Sovereign Session:
                </span>
                <span style="color: var(--accent-primary); font-weight: 600;">{currentProfile.displayName}</span>
                {#if currentProfile.headline}
                  <span style="color: var(--text-muted); font-size: 11px; margin-left: 4px;">— {currentProfile.headline}</span>
                {:else}
                  <span style="color: var(--text-dim); font-size: 11px;">(@{currentProfile.handle} • {authSession?.email})</span>
                {/if}
              {:else}
                <span style="font-weight: 600; color: var(--text-main);">
                  Guest Mode (Read-Only)
                </span>
                <span style="color: var(--text-muted); font-size: 11px; margin-left: 4px;">
                  — Sign in with Dex (:5556) to publish updates and connect
                </span>
              {/if}
            </div>
          </div>

          <div style="display: flex; align-items: center; gap: 6px;">
            {#if isLoggedIn}
              <button
                class="sleek-action-btn edit-btn"
                onclick={() => { workExpInitialTab = 'personal'; workExpTargetProfile = currentProfile; }}
                title="Edit Profile — Update your sovereign identity, career timeline, education, and highlights"
                aria-label="Edit Profile"
              >
                <Pencil size={14} />
              </button>

              <button
                class="sleek-action-btn logout-btn"
                onclick={handleLogout}
                title="Log Out — End your sovereign session and clear credentials from Redis"
                aria-label="Log Out"
              >
                <LogOut size={14} />
              </button>
            {:else}
              <button
                class="btn-primary"
                style="font-size: 11px; padding: 4px 12px; display: inline-flex; align-items: center; gap: 5px; background: linear-gradient(135deg, var(--accent-primary), #6366f1);"
                onclick={() => { isGuest = false; }}
              >
                <KeyRound size={12} />
                <span>Sign in with Dex</span>
              </button>
            {/if}
          </div>
        </div>

        <div class="search-box">
          <Search size={16} />
          <input
            type="text"
            placeholder="Search 'doctors in timisoara', specialists, hospitals, or posts..."
            value={searchQuery}
            oninput={handleSearchInput}
          />
          {#if searchQuery}
            <button class="search-clear-btn" onclick={clearSearch} title="Clear search">
              <X size={14} />
            </button>
          {/if}
        </div>

        <div class="search-suggestions">
          <span>Try:</span>
          <button class="search-suggestion-chip" onclick={() => setSearchQuery('doctors in timisoara')}>
            doctors in timisoara
          </button>
          <button class="search-suggestion-chip" onclick={() => setSearchQuery('pediatric surgeon')}>
            pediatric surgeon
          </button>
          <button class="search-suggestion-chip" onclick={() => setSearchQuery('spitalul judetean')}>
            spitalul județean
          </button>
          <button class="search-suggestion-chip" onclick={() => setSearchQuery('emergency cardiology')}>
            cardiology
          </button>
          <button class="search-suggestion-chip" onclick={() => setSearchQuery('berlin')}>
            berlin
          </button>
        </div>

        {#if !isSearching}
          <div class="filter-bar">
            <button
              class="filter-chip"
              class:active={feedType === 'all' && !filterTables && !filterImages && !filterLinks && !activeTag}
              onclick={() => { feedType = 'all'; filterTables = null; filterImages = null; filterLinks = null; activeTag = ''; loadPosts(); }}
            >
              All Stream
            </button>
            <button
              class="filter-chip"
              class:active={feedType === 'companies'}
              onclick={() => { feedType = feedType === 'companies' ? 'all' : 'companies'; loadPosts(); }}
            >
              <Building size={13} />
              <span>Organizations</span>
            </button>
            <button
              class="filter-chip"
              class:active={filterTables === true}
              onclick={() => { filterTables = filterTables === true ? null : true; loadPosts(); }}
            >
              <Table size={13} />
              <span>Has Tables</span>
            </button>
            <button
              class="filter-chip"
              class:active={filterLinks === true}
              onclick={() => { filterLinks = filterLinks === true ? null : true; loadPosts(); }}
            >
              <Link2 size={13} />
              <span>Has Links</span>
            </button>
            <button
              class="filter-chip"
              class:active={filterImages === true}
              onclick={() => { filterImages = filterImages === true ? null : true; loadPosts(); }}
            >
              <Image size={13} />
              <span>Has Images</span>
            </button>
            {#if activeTag}
              <button class="filter-chip active" onclick={() => { activeTag = ''; loadPosts(); }}>
                Tag: #{activeTag} ✕
              </button>
            {/if}
          </div>
        {:else}
          <!-- Search Intent & Sub-categories Bar -->
          {#if searchResult && (searchResult.detectedRole || searchResult.detectedLocation)}
            <div class="search-intent-banner">
              <div class="search-intent-badge">
                <MapPin size={13} />
                <span>
                  Structured Discovery:
                  {#if searchResult.detectedRole}
                    Role: <strong style="color: var(--accent-primary);">{searchResult.detectedRole}</strong>
                  {/if}
                  {#if searchResult.detectedRole && searchResult.detectedLocation}
                    &nbsp;in&nbsp;
                  {/if}
                  {#if searchResult.detectedLocation}
                    Location: <strong style="color: var(--accent-primary);">{searchResult.detectedLocation}</strong>
                  {/if}
                </span>
              </div>
              <span style="font-size: 12px; color: var(--text-dim);">
                {searchResult.totalCount} result{searchResult.totalCount === 1 ? '' : 's'} found
              </span>
            </div>
          {/if}

          <div class="filter-bar">
            <button
              class="filter-chip"
              class:active={searchCategory === 'all'}
              onclick={() => searchCategory = 'all'}
            >
              All ({searchResult?.totalCount || 0})
            </button>
            <button
              class="filter-chip"
              class:active={searchCategory === 'people'}
              onclick={() => searchCategory = 'people'}
            >
              <Stethoscope size={13} />
              <span>Specialists & People ({searchResult?.people?.length || 0})</span>
            </button>
            <button
              class="filter-chip"
              class:active={searchCategory === 'companies'}
              onclick={() => searchCategory = 'companies'}
            >
              <Building size={13} />
              <span>Organizations ({searchResult?.companies?.length || 0})</span>
            </button>
            <button
              class="filter-chip"
              class:active={searchCategory === 'posts'}
              onclick={() => searchCategory = 'posts'}
            >
              <FileText size={13} />
              <span>Publications & Posts ({searchResult?.posts?.length || 0})</span>
            </button>
          </div>
        {/if}
      </div>

      <!-- Views -->
      {#if isSearching}
        {#if loadingFeed}
          <div style="padding: 40px; text-align: center; color: var(--text-muted);">
            Searching network for "{searchQuery}"...
          </div>
        {:else if !searchResult || searchResult.totalCount === 0}
          <div class="card" style="padding: 40px; text-align: center; color: var(--text-muted);">
            <p style="font-size: 16px; margin-bottom: 8px;">No results found for "{searchQuery}".</p>
            <p style="font-size: 13px; color: var(--text-dim); margin-bottom: 16px;">
              Try searching with broader terms like "doctors", "timisoara", "cardiology", or "surgeons".
            </p>
            <button class="btn-secondary" onclick={clearSearch}>
              Clear Search
            </button>
          </div>
        {:else}
          <!-- 1. People / Specialists -->
          {#if (searchCategory === 'all' || searchCategory === 'people') && searchResult.people && searchResult.people.length > 0}
            <div class="search-results-section">
              <div class="search-results-title">
                <Stethoscope size={16} />
                <span>Specialists & Professionals ({searchResult.people.length})</span>
              </div>
              <div style="display: flex; flex-direction: column; gap: 12px;">
                {#each searchResult.people as person}
                  <div class="search-person-card">
                    <div class="search-person-header">
                      <div class="search-person-info">
                        <img
                          src={person.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
                          alt={person.displayName}
                          class="avatar"
                          style="width: 48px; height: 48px;"
                        />
                        <div>
                          <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                            <strong style="font-size: 15px;">{person.displayName}</strong>
                            <span style="font-size: 12px; color: var(--text-dim);">@{person.handle}</span>
                            {#if person.location}
                              <span class="search-location-pill">
                                <MapPin size={11} />
                                <span>{person.location}</span>
                              </span>
                            {/if}
                          </div>
                          <div style="font-size: 13px; color: var(--text-muted); margin-top: 2px;">
                            {person.headline}
                          </div>
                        </div>
                      </div>
                      <div style="display: flex; gap: 6px; flex-shrink: 0; flex-wrap: wrap;">
                        <button
                          class="btn-secondary"
                          style="font-size: 12px; padding: 6px 10px; display: inline-flex; align-items: center; gap: 5px;"
                          onclick={() => { workExpInitialTab = 'all'; workExpTargetProfile = person; }}
                          title="View verified career timeline, highlights, and education"
                        >
                          <Layers size={13} color="var(--accent-primary)" />
                          <span>Timeline</span>
                        </button>
                        <button
                          class="btn-secondary"
                          style="font-size: 12px; padding: 6px 10px; display: inline-flex; align-items: center; gap: 5px;"
                          onclick={() => contactTargetHandle = person.handle}
                          title="Direct Decentralized Message"
                        >
                          <MessageSquare size={13} />
                          <span>Direct Message</span>
                        </button>
                        <button
                          class="btn-primary"
                          style="font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 5px;"
                          onclick={() => handleConnect(person.id)}
                          title="Connect on graph"
                        >
                          <UserPlus size={13} />
                          <span>Connect</span>
                        </button>
                      </div>
                    </div>

                    {#if person.bioMarkdown}
                      <p style="font-size: 13px; color: var(--text-main); margin: 0; line-height: 1.4;">
                        {person.bioMarkdown}
                      </p>
                    {/if}

                    {#if person.skills && person.skills.length > 0}
                      <div class="search-skills-wrap">
                        {#each person.skills as skill}
                          <span class="search-skill-pill">#{skill}</span>
                        {/each}
                      </div>
                    {/if}
                  </div>
                {/each}
              </div>
            </div>
          {/if}

          <!-- 2. Organizations / Hospitals -->
          {#if (searchCategory === 'all' || searchCategory === 'companies') && searchResult.companies && searchResult.companies.length > 0}
            <div class="search-results-section">
              <div class="search-results-title">
                <Building size={16} />
                <span>Organizations & Clinics ({searchResult.companies.length})</span>
              </div>
              <div style="display: flex; flex-direction: column; gap: 12px;">
                {#each searchResult.companies as comp}
                  <div class="search-person-card">
                    <div class="search-person-header">
                      <div class="search-person-info">
                        <img
                          src={comp.logoUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=200'}
                          alt={comp.name}
                          class="avatar"
                          style="width: 48px; height: 48px; border-radius: var(--radius-sm);"
                        />
                        <div>
                          <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                            <strong style="font-size: 15px;">{comp.name}</strong>
                            <span style="font-size: 12px; color: var(--text-dim);">@{comp.handle}</span>
                            <span style="font-size: 12px; color: var(--text-muted);">• {comp.industry}</span>
                            {#if comp.headquarters}
                              <span class="search-location-pill">
                                <MapPin size={11} />
                                <span>{comp.headquarters}</span>
                              </span>
                            {/if}
                          </div>
                          {#if comp.tagline}
                            <div style="font-size: 13px; color: var(--text-muted); margin-top: 2px;">
                              {comp.tagline}
                            </div>
                          {/if}
                        </div>
                      </div>
                      <div style="display: flex; gap: 6px; flex-shrink: 0;">
                        {#if comp.websiteUrl}
                          <a
                            href={comp.websiteUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            class="btn-secondary"
                            style="font-size: 12px; padding: 6px 10px; text-decoration: none; display: inline-flex; align-items: center; gap: 5px;"
                          >
                            <ExternalLink size={13} />
                            <span>Website</span>
                          </a>
                        {/if}
                        <button
                          class="btn-primary"
                          style="font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 5px;"
                          onclick={() => handleFollow(comp.id)}
                        >
                          <Building size={13} />
                          <span>Follow</span>
                        </button>
                      </div>
                    </div>

                    {#if comp.descriptionMarkdown}
                      <p style="font-size: 13px; color: var(--text-main); margin: 0; line-height: 1.4;">
                        {comp.descriptionMarkdown}
                      </p>
                    {/if}
                  </div>
                {/each}
              </div>
            </div>
          {/if}

          <!-- 3. Posts & Publications -->
          {#if (searchCategory === 'all' || searchCategory === 'posts') && searchResult.posts && searchResult.posts.length > 0}
            <div class="search-results-section">
              <div class="search-results-title">
                <span>📄</span>
                <span>Unified Publications & Posts ({searchResult.posts.length})</span>
              </div>
              <div style="display: flex; flex-direction: column; gap: 16px;">
                {#each searchResult.posts as post (post.id)}
                  <PostCard
                    {post}
                    {currentProfile}
                    onOpenContact={(handle) => contactTargetHandle = handle}
                    onTagClick={(tag) => setTagFilter(tag)}
                    onOpenExperience={(handle) => openExperienceByHandle(handle)}
                  />
                {/each}
              </div>
            </div>
          {/if}
        {/if}

      {:else}
        {#if activeTab === 'feed'}
          {#if isLoggedIn}
            <!-- Unified Composer: Markdown + Table Tool + Image Tool + Link Card -->
            <UnifiedComposer
              {currentProfile}
              onPostCreated={handlePostCreated}
            />
          {:else}
            <div class="card" style="padding: 16px 20px; display: flex; align-items: center; justify-content: space-between; gap: 16px; background: var(--bg-surface-elevated); margin-bottom: 16px;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <div style="width: 36px; height: 36px; border-radius: 50%; background: var(--accent-subtle); display: flex; align-items: center; justify-content: center; color: var(--accent-primary); flex-shrink: 0;">
                  <KeyRound size={18} />
                </div>
                <div>
                  <div style="font-weight: 600; font-size: 13px;">Sign in to publish updates or articles</div>
                  <div style="font-size: 12px; color: var(--text-dim);">Publish markdown thoughts, data tables, and link cards under your sovereign identity.</div>
                </div>
              </div>
              <button
                class="btn-primary"
                style="font-size: 12px; padding: 6px 14px; white-space: nowrap; flex-shrink: 0;"
                onclick={() => { isGuest = false; }}
              >
                Sign In
              </button>
            </div>
          {/if}

          <!-- Posts List -->
          {#if loadingFeed}
            <div style="padding: 40px; text-align: center; color: var(--text-muted);">
              Fetching unified feed...
            </div>
          {:else if posts.length === 0}
            <div class="card" style="padding: 40px; text-align: center; color: var(--text-muted);">
              <p style="font-size: 16px; margin-bottom: 8px;">No posts match your filters.</p>
              <p style="font-size: 13px; color: var(--text-dim);">Publish a markdown thought, table, or link card above!</p>
            </div>
          {:else}
            {#each posts as post (post.id)}
              <PostCard
                {post}
                {currentProfile}
                onOpenContact={(handle) => contactTargetHandle = handle}
                onTagClick={(tag) => setTagFilter(tag)}
                onOpenExperience={(handle, tab) => openExperienceByHandle(handle, tab)}
              />
            {/each}
          {/if}

        {:else if activeTab === 'graph'}
          <GraphExplorer
            {currentProfile}
            onOpenContact={(handle) => contactTargetHandle = handle}
            onOpenExperience={(node, tab = 'experience') => {
              workExpInitialTab = tab;
              workExpTargetProfile = node;
            }}
          />

        {:else if activeTab === 'companies'}
          <CompaniesView
            {currentProfile}
            onTagClick={(tag) => setTagFilter(tag)}
          />
        {/if}
      {/if}
    </div>
  </main>

  <!-- Right Context Drawer: Job Announcements Feed & Object Storage Posts -->
  <aside class="context-drawer">
    <JobsFeed
      bind:this={jobsFeedRef}
      {currentProfile}
      onSelectJob={(job) => selectedJob = job}
      onOpenPostJob={() => {
        if (!isLoggedIn) {
          alert('Please sign in with Dex to publish a job announcement.');
          return;
        }
        showPostJobModal = true;
      }}
    />

    <div style="border-top: 1px solid var(--border-subtle); padding-top: 14px; margin-top: auto;">
      <div style="font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-dim); margin-bottom: 8px;">
        Trending Protocol Tags
      </div>
      <div style="display: flex; gap: 5px; flex-wrap: wrap;">
        {#each ['Privacy', 'PostgreSQL', 'Decentralized', 'Svelte', 'Micronaut', 'Matrix', 'Architecture'] as tag}
          <button
            class="filter-chip"
            style="font-size: 11px; padding: 2px 7px;"
            onclick={() => { activeTab = 'feed'; setTagFilter(tag); }}
          >
            #{tag}
          </button>
        {/each}
      </div>
    </div>
  </aside>
</div>
{/if}

<!-- Modals -->
{#if contactTargetHandle}
  <DecentralizedModal
    targetHandle={contactTargetHandle}
    onClose={() => contactTargetHandle = null}
  />
{/if}

{#if showGdprModal}
  <GdprModal
    {currentProfile}
    onClose={() => showGdprModal = false}
    onUserPurged={async () => {
      await handleLogout();
      profiles = await fetchProfiles();
      loadPosts();
    }}
  />
{/if}

{#if workExpTargetProfile}
  <WorkExperienceModal
    profile={workExpTargetProfile}
    {currentProfile}
    initialTab={workExpInitialTab}
    onClose={() => workExpTargetProfile = null}
    onExperiencesUpdated={() => loadPosts()}
    onProfileUpdated={handleProfileUpdated}
  />
{/if}

{#if showDexLoginModal}
  <DexLoginModal
    onClose={() => showDexLoginModal = false}
    onLoginSuccess={handleLoginSuccess}
  />
{/if}

{#if selectedJob}
  <JobDetailModal
    job={selectedJob}
    onClose={() => selectedJob = null}
  />
{/if}

{#if showPostJobModal}
  <PostJobModal
    {currentProfile}
    onClose={() => showPostJobModal = false}
    onJobCreated={(newJob) => {
      selectedJob = newJob;
      if (jobsFeedRef?.loadJobs) jobsFeedRef.loadJobs();
    }}
  />
{/if}
