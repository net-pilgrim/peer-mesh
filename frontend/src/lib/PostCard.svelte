<script>
  import { marked } from 'marked';
  import {
    Briefcase,
    GraduationCap,
    Shield,
    GitBranch,
    RefreshCw,
    Heart,
    MessageSquare,
    Share2,
    Table,
    Image,
    Send,
    Sparkles,
    Layers
  } from '@lucide/svelte';
  import { togglePostLike, fetchComments, addComment, syncGitHubPost } from './api.js';

  let { post, currentProfile, onOpenContact, onTagClick, onOpenExperience } = $props();

  let isLiked = $state(false);
  let likeCount = $state(0);
  let showComments = $state(false);
  let comments = $state([]);
  let newCommentText = $state('');
  let isSubmittingComment = $state(false);
  let isSyncing = $state(false);

  $effect(() => {
    isLiked = post.likedByMe;
    likeCount = post.likeCount;
  });

  const renderedContent = $derived(marked.parse(post.bodyMarkdown || '', { gfm: true, breaks: true }));

  async function handleLike() {
    if (!currentProfile) return;
    // Optimistic UI update
    isLiked = !isLiked;
    likeCount += isLiked ? 1 : -1;
    try {
      await togglePostLike(post.id, currentProfile.id);
    } catch (err) {
      // Revert on error
      isLiked = !isLiked;
      likeCount += isLiked ? 1 : -1;
    }
  }

  async function handleReSync() {
    if (isSyncing || !post.selfHosted) return;
    isSyncing = true;
    try {
      const updated = await syncGitHubPost(post.id);
      post.bodyMarkdown = updated.bodyMarkdown;
      post.title = updated.title;
      post.githubCommitSha = updated.githubCommitSha;
      post.tags = updated.tags;
      alert('Content successfully re-synchronized with GitHub repository!');
    } catch (err) {
      alert('Failed to re-sync with GitHub: ' + err.message);
    } finally {
      isSyncing = false;
    }
  }

  async function toggleComments() {
    showComments = !showComments;
    if (showComments && comments.length === 0) {
      try {
        comments = await fetchComments(post.id);
      } catch (err) {
        console.error('Failed to load comments:', err);
      }
    }
  }

  async function handleAddComment() {
    if (!newCommentText.trim() || !currentProfile) return;
    isSubmittingComment = true;
    try {
      const added = await addComment(post.id, currentProfile.id, newCommentText.trim());
      comments = [...comments, added];
      newCommentText = '';
    } catch (err) {
      alert('Failed to add comment: ' + err.message);
    } finally {
      isSubmittingComment = false;
    }
  }

  function formatTime(isoStr) {
    if (!isoStr) return '';
    const date = new Date(isoStr);
    const diff = Math.floor((new Date() - date) / 1000);
    if (diff < 60) return `${diff}s ago`;
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return date.toLocaleDateString();
  }
</script>

<article class="card post-card">
  <header class="post-header">
    <div class="author-info">
      <img
        src={post.authorAvatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
        alt={post.authorDisplayName}
        class="avatar"
      />
      <div class="author-names">
        <div class="author-display-name">{post.authorDisplayName}</div>
        <div class="author-meta">
          <span>@{post.authorHandle}</span>
          <span>•</span>
          <span>{formatTime(post.createdAt)}</span>
          {#if post.authorType === 'COMPANY'}
            <span class="brand-badge">Org</span>
          {/if}
        </div>
      </div>
    </div>

    <!-- Profile Actions -->
    <div style="display: flex; gap: 6px; align-items: center;">
      {#if post.authorType === 'PERSON'}
        <button
          class="contact-badge"
          onclick={() => onOpenExperience && onOpenExperience(post.authorHandle, 'all')}
          title="View complete career timeline, highlights, and education"
        >
          <Layers size={12} color="var(--accent-primary)" />
          <span>Timeline</span>
        </button>
        <button
          class="contact-badge"
          onclick={() => onOpenContact && onOpenContact(post.authorHandle)}
          title="Direct Encrypted Contact via Matrix / SimpleX / Nostr / Session"
        >
          <Shield size={12} />
          <span>Direct Connect</span>
        </button>
      {/if}
    </div>
  </header>

  <!-- GitHub Self-Hosted Provenance Badge -->
  {#if post.selfHosted}
    <div class="git-badge-bar">
      <a
        href={post.linkUrl || (post.githubRepo ? `https://github.com/${post.githubRepo}/blob/main/${post.githubPath || ''}` : '#')}
        target="_blank"
        rel="noopener noreferrer"
        class="git-provenance-pill"
        title="View self-hosted source file on GitHub"
      >
        <GitBranch size={13} />
        <span>Git-Backed: <strong>{post.githubRepo || 'Self-Hosted'}</strong>{post.githubPath ? `/${post.githubPath}` : ''}</span>
        {#if post.githubCommitSha}
          <span class="commit-pill">@{post.githubCommitSha.substring(0, 7)}</span>
        {/if}
      </a>

      {#if currentProfile && currentProfile.id === post.authorId}
        <button
          class="resync-btn"
          disabled={isSyncing}
          onclick={handleReSync}
          title="Pull latest markdown changes from Git repository"
        >
          <RefreshCw size={12} />
          <span>{isSyncing ? 'Syncing...' : 'Pull Git Update'}</span>
        </button>
      {/if}
    </div>
  {/if}

  {#if post.title}
    <h3 class="post-title">{post.title}</h3>
  {/if}

  <div class="markdown-body">
    {@html renderedContent}
  </div>

  <!-- Unified Link Card (Preview embed unified with the post) -->
  {#if post.linkUrl}
    <a href={post.linkUrl} target="_blank" rel="noopener noreferrer" class="unified-link-card">
      {#if post.linkImageUrl}
        <img src={post.linkImageUrl} alt={post.linkTitle || 'Link Preview'} class="link-card-image" />
      {/if}
      <div class="link-card-body">
        <div class="link-card-domain">
          {post.linkDomain || new URL(post.linkUrl).hostname}
        </div>
        <div class="link-card-title">{post.linkTitle || post.linkUrl}</div>
        {#if post.linkDescription}
          <div class="link-card-desc">{post.linkDescription}</div>
        {/if}
      </div>
    </a>
  {/if}

  <!-- Tags -->
  {#if post.tags && post.tags.length > 0}
    <div style="display: flex; gap: 6px; flex-wrap: wrap; margin-top: 12px;">
      {#each post.tags as tag}
        <button
          class="filter-chip"
          style="font-size: 11px; padding: 2px 8px;"
          onclick={() => onTagClick && onTagClick(tag)}
        >
          #{tag}
        </button>
      {/each}
    </div>
  {/if}

  <footer class="post-footer">
    <div class="post-actions">
      <button class="action-btn" class:liked={isLiked} onclick={handleLike}>
        <Heart size={16} fill={isLiked ? "currentColor" : "none"} />
        <span>{likeCount}</span>
      </button>

      <button class="action-btn" onclick={toggleComments}>
        <MessageSquare size={16} />
        <span>{post.commentCount || comments.length}</span>
      </button>

      {#if post.hasTables}
        <span style="font-size: 12px; color: var(--text-dim); display: inline-flex; align-items: center; gap: 4px;">
          <Table size={12} /> Tables
        </span>
      {/if}
      {#if post.hasImages}
        <span style="font-size: 12px; color: var(--text-dim); display: inline-flex; align-items: center; gap: 4px;">
          <Image size={12} /> Media
        </span>
      {/if}
    </div>

    <button
      class="action-btn"
      onclick={() => {
        navigator.clipboard.writeText(window.location.origin + '#post-' + post.id);
        alert('Post link copied to clipboard!');
      }}
      title="Copy Link"
    >
      <Share2 size={15} />
    </button>
  </footer>

  <!-- Expandable Comment Section -->
  {#if showComments}
    <div style="margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border-subtle); display: flex; flex-direction: column; gap: 12px;">
      <div style="font-size: 13px; font-weight: 600; color: var(--text-muted);">Discussion ({comments.length})</div>

      {#each comments as comment}
        <div style="padding: 10px 14px; background: var(--bg-surface-elevated); border-radius: var(--radius-md); font-size: 14px;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
            <span style="font-weight: 600; font-size: 13px;">{comment.authorDisplayName} (@{comment.authorHandle})</span>
            <span style="font-size: 11px; color: var(--text-dim);">{formatTime(comment.createdAt)}</span>
          </div>
          <div class="markdown-body" style="font-size: 13px;">
            {@html marked.parse(comment.bodyMarkdown || '')}
          </div>
        </div>
      {/each}

      <div style="display: flex; gap: 8px; margin-top: 6px;">
        <input
          type="text"
          placeholder="Write a response in markdown..."
          bind:value={newCommentText}
          onkeydown={(e) => e.key === 'Enter' && handleAddComment()}
          style="flex: 1; padding: 8px 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main); font-size: 13px;"
        />
        <button class="btn-primary" disabled={!newCommentText.trim() || isSubmittingComment} onclick={handleAddComment} style="padding: 6px 14px; font-size: 13px; display: inline-flex; align-items: center; gap: 4px;">
          <Send size={13} />
          <span>Reply</span>
        </button>
      </div>
    </div>
  {/if}
</article>
