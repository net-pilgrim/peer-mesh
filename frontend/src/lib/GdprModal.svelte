<script>
  import { exportGdpr, purgeGdpr } from './api.js';
  import { ShieldCheck, Download, Trash, X } from '@lucide/svelte';

  let { currentProfile, onClose, onUserPurged } = $props();

  let isExporting = $state(false);
  let isPurging = $state(false);

  async function handleExport() {
    if (!currentProfile) return;
    isExporting = true;
    try {
      const data = await exportGdpr(currentProfile.id);
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `peermesh-gdpr-export-${currentProfile.handle}-${new Date().toISOString().slice(0, 10)}.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      alert('Export failed: ' + err.message);
    } finally {
      isExporting = false;
    }
  }

  async function handlePurge() {
    if (!currentProfile) return;
    const confirmed = prompt(
      `CAUTION: This will permanently delete @${currentProfile.handle}, all your posts, comments, reactions, and graph connections with ZERO backup.\n\nType your handle "${currentProfile.handle}" to confirm:`
    );
    if (confirmed !== currentProfile.handle) {
      alert('Confirmation did not match. Deletion aborted.');
      return;
    }

    isPurging = true;
    try {
      await purgeGdpr(currentProfile.id);
      alert('Your data has been permanently erased in accordance with GDPR Article 17.');
      if (onUserPurged) onUserPurged();
      onClose();
    } catch (err) {
      alert('Purge failed: ' + err.message);
    } finally {
      isPurging = false;
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
          <h3 style="font-size: 18px; font-weight: 700; margin: 0;">GDPR & Privacy Sovereignty Center</h3>
          <p style="font-size: 13px; color: var(--text-muted); margin: 0;">
            Data Minimization, Portability & Right to be Forgotten
          </p>
        </div>
      </div>
      <button class="modal-close" onclick={onClose} aria-label="Close modal">
        <X size={18} />
      </button>
    </div>

    <div style="font-size: 14px; color: var(--text-muted); line-height: 1.6; display: flex; flex-direction: column; gap: 14px;">
      <div style="padding: 12px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md);">
        <strong style="color: var(--text-main); display: inline-flex; align-items: center; gap: 6px;">
          <ShieldCheck size={16} color="var(--accent-success)" />
          Zero Surveillance Guarantee:
        </strong>
        <ul style="margin-top: 6px; padding-left: 18px; font-size: 13px;">
          <li>Zero tracking cookies or third-party behavioral pixels.</li>
          <li>Zero phone numbers or government identity demands.</li>
          <li>Zero server-side private message storage (messaging is off-platform via Matrix/SimpleX/Nostr).</li>
        </ul>
      </div>

      <div style="border-top: 1px solid var(--border-subtle); padding-top: 14px;">
        <h4 style="color: var(--text-main); font-size: 15px; margin-bottom: 4px;">Article 20: Right to Data Portability</h4>
        <p style="font-size: 13px; margin-bottom: 10px;">
          Download a complete, machine-readable JSON archive of your profile, posts, and graph edges.
        </p>
        <button class="btn-primary" onclick={handleExport} disabled={isExporting} style="display: inline-flex; align-items: center; gap: 6px;">
          <Download size={15} />
          <span>{isExporting ? 'Generating JSON Export...' : 'Export All My Data (JSON)'}</span>
        </button>
      </div>

      <div style="border-top: 1px solid var(--border-subtle); padding-top: 14px;">
        <h4 style="color: var(--accent-danger); font-size: 15px; margin-bottom: 4px;">Article 17: Right to be Forgotten (Hard Purge)</h4>
        <p style="font-size: 13px; margin-bottom: 10px;">
          Permanently delete all your personal data, posts, comments, likes, and network graph edges.
        </p>
        <button
          class="btn-icon"
          style="border-color: var(--accent-danger); color: var(--accent-danger); font-weight: 600; display: inline-flex; align-items: center; gap: 6px;"
          onclick={handlePurge}
          disabled={isPurging}
        >
          <Trash size={15} />
          <span>{isPurging ? 'Erasing...' : 'Permanently Delete My Account & All Data'}</span>
        </button>
      </div>
    </div>

    <div style="margin-top: 20px; text-align: right;">
      <button class="btn-icon" onclick={onClose}>Close</button>
    </div>
  </div>
</div>
