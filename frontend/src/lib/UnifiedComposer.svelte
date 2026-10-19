<script>
  import { marked } from 'marked';
  import {
    GitBranch,
    Table,
    CloudUpload,
    Link2,
    Eye,
    Pencil,
    Send,
    X,
    Upload
  } from '@lucide/svelte';
  import { createPost, previewGitHubContent, importGitHubPost, uploadStorageFile } from './api.js';

  let { currentProfile, onPostCreated } = $props();

  let bodyMarkdown = $state('');
  let title = $state('');
  let linkUrl = $state('');
  let linkTitle = $state('');
  let linkDescription = $state('');
  let linkImageUrl = $state('');
  let showLinkFields = $state(false);
  let showTableHelper = $state(false);
  let showImageHelper = $state(false);
  let showGitHubImport = $state(false);
  let isPreview = $state(false);
  let isSubmitting = $state(false);
  let tagsInput = $state('General');
  let isUploadingFile = $state(false);
  let uploadError = $state(null);

  // GitHub self-hosting syndication
  let githubUrlInput = $state('');
  let isFetchingGitHub = $state(false);
  let gitPreview = $state(null);
  let gitError = $state(null);
  let isSyndicating = $state(false);

  async function handleFetchGitHubPreview() {
    if (!githubUrlInput.trim()) return;
    isFetchingGitHub = true;
    gitError = null;
    gitPreview = null;
    try {
      const res = await previewGitHubContent(githubUrlInput.trim());
      gitPreview = res;
    } catch (err) {
      gitError = err.message;
    } finally {
      isFetchingGitHub = false;
    }
  }

  function handleLoadIntoEditor() {
    if (!gitPreview) return;
    title = gitPreview.title || '';
    bodyMarkdown = gitPreview.bodyMarkdown || '';
    if (gitPreview.tags && gitPreview.tags.length > 0) {
      tagsInput = gitPreview.tags.join(', ');
    }
    if (gitPreview.linkUrl) {
      linkUrl = gitPreview.linkUrl;
      linkTitle = gitPreview.title;
      showLinkFields = true;
    }
    showGitHubImport = false;
  }

  async function handleDirectSyndicate() {
    if (!githubUrlInput.trim() || !currentProfile) return;
    isSyndicating = true;
    gitError = null;
    try {
      const created = await importGitHubPost(githubUrlInput.trim(), currentProfile.id);
      githubUrlInput = '';
      gitPreview = null;
      showGitHubImport = false;
      if (onPostCreated) onPostCreated(created);
    } catch (err) {
      gitError = err.message;
    } finally {
      isSyndicating = false;
    }
  }

  // Table builder parameters
  let tableRows = $state(3);
  let tableCols = $state(3);

  // Image parameters
  let imageUrlInput = $state('');
  let imageAltInput = $state('');

  function insertTable() {
    let md = '\n\n|';
    for (let c = 1; c <= tableCols; c++) md += ` Header ${c} |`;
    md += '\n|';
    for (let c = 1; c <= tableCols; c++) md += ' :--- |';
    md += '\n';
    for (let r = 1; r <= tableRows; r++) {
      md += '|';
      for (let c = 1; c <= tableCols; c++) md += ` Cell ${r},${c} |`;
      md += '\n';
    }
    md += '\n';
    bodyMarkdown += md;
    showTableHelper = false;
  }

  function insertImage() {
    if (imageUrlInput.trim()) {
      bodyMarkdown += `\n\n![${imageAltInput.trim() || 'Image'}](${imageUrlInput.trim()})\n\n`;
      imageUrlInput = '';
      imageAltInput = '';
      showImageHelper = false;
    }
  }

  async function handleFileSelect(e) {
    const file = e.target.files?.[0];
    if (!file) return;

    isUploadingFile = true;
    uploadError = null;

    try {
      if (file.type.startsWith('image/')) {
        // Resize client-side in Canvas to keep storage bandwidth minimal, then upload to Object Storage
        const reader = new FileReader();
        reader.onload = (event) => {
          const img = new Image();
          img.onload = () => {
            const canvas = document.createElement('canvas');
            const MAX_WIDTH = 1200;
            let width = img.width;
            let height = img.height;
            if (width > MAX_WIDTH) {
              height = Math.round(height * (MAX_WIDTH / width));
              width = MAX_WIDTH;
            }
            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, width, height);

            canvas.toBlob(async (blob) => {
              try {
                if (!blob) throw new Error('Failed to compress image in canvas');
                const cleanName = file.name.replace(/\.[^/.]+$/, "") + ".jpg";
                const uploadFileObj = new File([blob], cleanName, { type: 'image/jpeg' });
                const res = await uploadStorageFile(uploadFileObj);
                bodyMarkdown += `\n\n![${file.name}](${res.url})\n\n`;
                showImageHelper = false;
              } catch (err) {
                uploadError = 'Storage upload failed: ' + err.message;
              } finally {
                isUploadingFile = false;
              }
            }, 'image/jpeg', 0.85);
          };
          img.src = event.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        // Direct upload to object storage for documents / markdown files
        const res = await uploadStorageFile(file);
        bodyMarkdown += `\n\n[📎 ${file.name}](${res.url})\n\n`;
        showImageHelper = false;
        isUploadingFile = false;
      }
    } catch (err) {
      uploadError = 'Storage upload failed: ' + err.message;
      isUploadingFile = false;
    }
  }

  async function handleSubmit() {
    if (!bodyMarkdown.trim() || !currentProfile) return;
    isSubmitting = true;

    try {
      let domain = '';
      if (linkUrl.trim()) {
        try {
          domain = new URL(linkUrl).hostname.replace('www.', '');
        } catch (_) {}
      }

      const tags = tagsInput.split(',').map(t => t.trim()).filter(Boolean);

      const newPost = await createPost({
        authorId: currentProfile.id,
        authorType: 'PERSON',
        title: title.trim() || null,
        bodyMarkdown: bodyMarkdown.trim(),
        linkUrl: linkUrl.trim() || null,
        linkTitle: linkTitle.trim() || null,
        linkDescription: linkDescription.trim() || null,
        linkImageUrl: linkImageUrl.trim() || null,
        linkDomain: domain || null,
        tags
      });

      // Reset
      bodyMarkdown = '';
      title = '';
      linkUrl = '';
      linkTitle = '';
      linkDescription = '';
      linkImageUrl = '';
      showLinkFields = false;
      isPreview = false;

      if (onPostCreated) onPostCreated(newPost);
    } catch (err) {
      alert('Failed to publish post: ' + err.message);
    } finally {
      isSubmitting = false;
    }
  }
</script>

<div class="card composer-box">
  <div class="composer-header">
    <img
      src={currentProfile?.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
      alt={currentProfile?.displayName}
      class="avatar"
    />
    <div>
      <div style="font-weight: 600;">{currentProfile?.displayName || 'Anonymous'}</div>
      <div style="font-size: 12px; color: var(--text-dim);">Publish unified thought, link, or technical note</div>
    </div>
  </div>

  <input
    type="text"
    placeholder="Optional Title or Headline..."
    bind:value={title}
    style="width: 100%; padding: 8px 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); margin-bottom: 8px; color: var(--text-main);"
  />

  {#if isPreview}
    <div class="markdown-body" style="padding: 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); min-height: 120px;">
      {@html marked.parse(bodyMarkdown || '*Nothing to preview yet*')}
    </div>
  {:else}
    <textarea
      class="composer-textarea"
      placeholder="Write in Markdown... (tables, code, links, images supported without restriction)"
      bind:value={bodyMarkdown}
    ></textarea>
  {/if}

  <!-- Unified Link Preview Builder -->
  {#if showLinkFields}
    <div style="margin-top: 10px; padding: 12px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 8px;">
      <div style="font-size: 13px; font-weight: 600; color: var(--accent-primary);">Attached Unified Link (Rich Card)</div>
      <input
        type="url"
        placeholder="https://example.com/article"
        bind:value={linkUrl}
        style="padding: 6px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main); font-size: 13px;"
      />
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
        <input
          type="text"
          placeholder="Card Title"
          bind:value={linkTitle}
          style="padding: 6px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main); font-size: 13px;"
        />
        <input
          type="text"
          placeholder="Preview Image URL"
          bind:value={linkImageUrl}
          style="padding: 6px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main); font-size: 13px;"
        />
      </div>
      <input
        type="text"
        placeholder="Card Description summary..."
        bind:value={linkDescription}
        style="padding: 6px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main); font-size: 13px;"
      />
    </div>
  {/if}

  <!-- Interactive Table Helper -->
  {#if showTableHelper}
    <div style="margin-top: 10px; padding: 12px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; align-items: center; gap: 12px; font-size: 13px;">
      <span>Columns:</span>
      <input type="number" min="1" max="8" bind:value={tableCols} style="width: 50px; padding: 4px; background: var(--bg-input); border: 1px solid var(--border-subtle); color: var(--text-main);" />
      <span>Rows:</span>
      <input type="number" min="1" max="15" bind:value={tableRows} style="width: 50px; padding: 4px; background: var(--bg-input); border: 1px solid var(--border-subtle); color: var(--text-main);" />
      <button class="btn-primary" onclick={insertTable} style="padding: 4px 12px; font-size: 12px;">Insert Table</button>
      <button class="btn-icon" onclick={() => showTableHelper = false} style="padding: 4px 8px;">Cancel</button>
    </div>
  {/if}

  <!-- Interactive Image / Object Storage Helper -->
  {#if showImageHelper}
    <div style="margin-top: 10px; padding: 12px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 8px; font-size: 13px;">
      <div style="display: flex; gap: 8px;">
        <input type="text" placeholder="Image URL (https://...)" bind:value={imageUrlInput} style="flex: 1; padding: 6px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); color: var(--text-main);" />
        <input type="text" placeholder="Caption/Alt" bind:value={imageAltInput} style="width: 120px; padding: 6px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); color: var(--text-main);" />
        <button class="btn-primary" onclick={insertImage} style="padding: 6px 12px;">Add URL</button>
      </div>
      <div style="font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
        <span>☁️ Upload to Local Object Storage (Images, Documents, Markdown):</span>
        <input type="file" accept="image/*,.pdf,.doc,.docx,.txt,.md" onchange={handleFileSelect} disabled={isUploadingFile} style="font-size: 12px;" />
        {#if isUploadingFile}
          <span style="color: var(--accent-primary); font-weight: 500;">Uploading to local object storage...</span>
        {/if}
      </div>
      {#if uploadError}
        <div style="font-size: 12px; color: var(--accent-danger);">{uploadError}</div>
      {/if}
    </div>
  {/if}

  <!-- Interactive GitHub Self-Hosted Syndication Panel -->
  {#if showGitHubImport}
    <div class="github-import-panel">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 600; color: #38bdf8; font-size: 13px; display: flex; align-items: center; gap: 6px;">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
          </svg>
          Syndicate from GitHub / Gist (Self-Hosted Markdown)
        </span>
        <button class="btn-icon" onclick={() => showGitHubImport = false} style="font-size: 11px; padding: 2px 6px;">✕</button>
      </div>

      <div style="display: flex; gap: 8px;">
        <input
          type="url"
          placeholder="e.g. https://github.com/owner/repo/blob/main/post.md"
          bind:value={githubUrlInput}
          onkeydown={(e) => e.key === 'Enter' && handleFetchGitHubPreview()}
          style="flex: 1; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main); font-size: 13px;"
        />
        <button
          class="btn-primary"
          disabled={!githubUrlInput.trim() || isFetchingGitHub}
          onclick={handleFetchGitHubPreview}
          style="padding: 6px 14px; font-size: 13px;"
        >
          {isFetchingGitHub ? 'Fetching...' : 'Fetch & Preview'}
        </button>
      </div>

      {#if gitError}
        <div style="font-size: 12px; color: var(--accent-danger); background: rgba(239, 68, 68, 0.1); padding: 8px; border-radius: var(--radius-sm);">
          {gitError}
        </div>
      {/if}

      {#if gitPreview}
        <div class="github-preview-box">
          <div style="font-weight: 600; margin-bottom: 4px; color: var(--text-main);">{gitPreview.title}</div>
          <div style="font-size: 11px; color: var(--text-dim); margin-bottom: 8px;">
            Repo: <code>{gitPreview.githubRepo || 'Git'}</code> &bull; Path: <code>{gitPreview.githubPath || 'Markdown'}</code>
          </div>
          <div class="markdown-body" style="font-size: 12px; max-height: 120px; overflow-y: auto;">
            {@html marked.parse(gitPreview.bodyMarkdown || '')}
          </div>
        </div>

        <div style="display: flex; gap: 8px; justify-content: flex-end;">
          <button
            class="btn-icon"
            onclick={handleLoadIntoEditor}
            style="padding: 6px 12px; font-size: 12px; border: 1px solid var(--border-strong);"
          >
            ✏️ Load into Editor
          </button>
          <button
            class="btn-primary"
            disabled={isSyndicating}
            onclick={handleDirectSyndicate}
            style="padding: 6px 14px; font-size: 12px; background: #0284c7;"
          >
            {isSyndicating ? 'Syndicating...' : '🚀 Publish Git-Backed Post'}
          </button>
        </div>
      {/if}
    </div>
  {/if}

  <div class="composer-toolbar">
    <div class="toolbar-actions">
      <button class="btn-icon" onclick={() => showGitHubImport = !showGitHubImport} title="Syndicate from GitHub / Gist" style="display: inline-flex; align-items: center; gap: 5px;">
        <GitBranch size={14} />
        <span>Git Sync</span>
      </button>
      <button class="btn-icon" onclick={() => showTableHelper = !showTableHelper} title="Insert Markdown Table" style="display: inline-flex; align-items: center; gap: 5px;">
        <Table size={14} />
        <span>Table</span>
      </button>
      <button class="btn-icon" onclick={() => showImageHelper = !showImageHelper} title="Insert Image & Upload to Local Object Storage" style="display: inline-flex; align-items: center; gap: 5px;">
        <CloudUpload size={14} />
        <span>Media & Storage</span>
      </button>
      <button class="btn-icon" onclick={() => showLinkFields = !showLinkFields} title="Attach Unified Link Card" style="display: inline-flex; align-items: center; gap: 5px;">
        <Link2 size={14} />
        <span>Link Card</span>
      </button>
      <button class="btn-icon" onclick={() => isPreview = !isPreview} title="Toggle Preview" style="display: inline-flex; align-items: center; gap: 5px;">
        {#if isPreview}
          <Pencil size={14} />
          <span>Edit</span>
        {:else}
          <Eye size={14} />
          <span>Preview</span>
        {/if}
      </button>
    </div>

    <div style="display: flex; align-items: center; gap: 10px;">
      <input
        type="text"
        placeholder="Tags (comma separated)..."
        bind:value={tagsInput}
        style="width: 140px; font-size: 12px; padding: 4px 8px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); color: var(--text-main);"
      />
      <button
        class="btn-primary"
        disabled={!bodyMarkdown.trim() || isSubmitting}
        onclick={handleSubmit}
        style="display: inline-flex; align-items: center; gap: 5px;"
      >
        <Send size={13} />
        <span>{isSubmitting ? 'Posting...' : 'Publish'}</span>
      </button>
    </div>
  </div>
</div>
