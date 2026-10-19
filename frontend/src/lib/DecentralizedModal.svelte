<script>
  import { fetchDecentralizedContact } from './api.js';
  import { ShieldCheck, Copy, Check, ExternalLink, Lock, X } from '@lucide/svelte';

  let { targetHandle, onClose } = $props();

  let contactData = $state(null);
  let loading = $state(true);
  let error = $state(null);
  let copiedProtocol = $state(null);

  $effect(() => {
    if (targetHandle) {
      loadContact();
    }
  });

  async function loadContact() {
    loading = true;
    error = null;
    try {
      contactData = await fetchDecentralizedContact(targetHandle);
    } catch (err) {
      error = err.message;
    } finally {
      loading = false;
    }
  }

  async function copyIdentifier(protocol, text) {
    try {
      await navigator.clipboard.writeText(text);
      copiedProtocol = protocol;
      setTimeout(() => {
        if (copiedProtocol === protocol) copiedProtocol = null;
      }, 2000);
    } catch (err) {
      console.error('Failed to copy', err);
    }
  }

  function handleKeydown(e) {
    if (e.key === 'Escape') onClose();
  }
</script>

<svelte:window onkeydown={handleKeydown} />

<div class="modal-backdrop" onclick={onClose} role="presentation">
  <div class="modal-card" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()} role="dialog" aria-modal="true" tabindex="-1">
    <div class="modal-header">
      <div style="display: flex; align-items: center; gap: 8px;">
        <ShieldCheck size={20} color="var(--primary)" />
        <div>
          <h3 style="font-size: 18px; font-weight: 700; margin: 0;">Direct Decentralized Connect</h3>
          <p style="font-size: 13px; color: var(--text-muted); margin: 0;">
            Contacting @{targetHandle} outside PeerMesh
          </p>
        </div>
      </div>
      <button class="modal-close" onclick={onClose} aria-label="Close modal">
        <X size={18} />
      </button>
    </div>

    {#if loading}
      <div style="padding: 30px; text-align: center; color: var(--text-muted);">
        Resolving decentralized protocol endpoints...
      </div>
    {:else if error}
      <div style="padding: 20px; color: var(--accent-danger); text-align: center;">
        Could not resolve contact endpoints: {error}
      </div>
    {:else if contactData}
      <p style="font-size: 14px; color: var(--text-muted); margin-bottom: 14px;">
        To protect your privacy and comply with GDPR data minimization, PeerMesh does not store direct messages. Choose a decentralized protocol below to start an end-to-end encrypted conversation directly:
      </p>

      <div class="channel-list">
        {#each contactData.channels as channel}
          <div class="channel-card">
            <div class="channel-info">
              <div style="display: flex; align-items: center; gap: 8px;">
                <h4>{channel.protocol}</h4>
                <span class="brand-badge" style="font-size: 10px;">{channel.badge}</span>
              </div>
              <p style="margin-top: 4px; word-break: break-all;">{channel.identifier}</p>
            </div>

            <div style="display: flex; gap: 6px;">
              <button
                class="btn-icon"
                onclick={() => copyIdentifier(channel.protocol, channel.identifier)}
                title="Copy address"
                style="display: inline-flex; align-items: center; gap: 6px;"
              >
                {#if copiedProtocol === channel.protocol}
                  <Check size={14} color="var(--accent-success)" /> Copied
                {:else}
                  <Copy size={14} /> Copy
                {/if}
              </button>
              <a
                href={channel.actionUrl}
                target="_blank"
                rel="noopener noreferrer"
                class="btn-primary"
                style="padding: 6px 12px; font-size: 12px; display: inline-flex; align-items: center; gap: 6px;"
              >
                <span>Open Client</span>
                <ExternalLink size={13} />
              </a>
            </div>
          </div>
        {/each}
      </div>

      <div class="privacy-notice" style="display: flex; align-items: flex-start; gap: 8px;">
        <Lock size={16} color="var(--primary)" style="flex-shrink: 0; margin-top: 2px;" />
        <div>
          <strong>Sovereign & Private:</strong> {contactData.privacyNotice}
        </div>
      </div>
    {/if}

    <div style="margin-top: 20px; text-align: right;">
      <button class="btn-icon" onclick={onClose}>Close</button>
    </div>
  </div>
</div>
