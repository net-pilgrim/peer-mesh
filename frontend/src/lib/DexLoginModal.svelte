<script>
  import { onMount } from 'svelte';
  import {
    ShieldCheck,
    KeyRound,
    User,
    Lock,
    ExternalLink,
    X,
    Database,
    Server,
    CheckCircle2,
    Sparkles,
    AlertCircle
  } from '@lucide/svelte';
  import { fetchAuthConfig, dexDirectLogin } from './api.js';

  let { onClose, onLoginSuccess } = $props();

  let authConfig = $state(null);
  let loading = $state(true);
  let loggingInEmail = $state(null);
  let errorMsg = $state('');

  // Custom credentials form
  let customEmail = $state('');
  let customPassword = $state('');
  let isSubmitting = $state(false);

  onMount(async () => {
    try {
      authConfig = await fetchAuthConfig();
    } catch (err) {
      console.error('Failed to load auth config:', err);
      errorMsg = 'Could not contact authentication service: ' + err.message;
    } finally {
      loading = false;
    }
  });

  async function handleQuickLogin(user) {
    loggingInEmail = user.email;
    errorMsg = '';
    try {
      const res = await dexDirectLogin(user.email, user.defaultPassword || 'password');
      if (res.success) {
        if (onLoginSuccess) onLoginSuccess(res);
        onClose();
      } else {
        errorMsg = res.message || 'Login failed';
      }
    } catch (err) {
      errorMsg = err.message || 'Authentication error';
    } finally {
      loggingInEmail = null;
    }
  }

  async function handleCustomLogin(e) {
    e.preventDefault();
    if (!customEmail.trim()) {
      errorMsg = 'Please enter an email address';
      return;
    }
    isSubmitting = true;
    errorMsg = '';
    try {
      const res = await dexDirectLogin(customEmail.trim(), customPassword || 'password');
      if (res.success) {
        if (onLoginSuccess) onLoginSuccess(res);
        onClose();
      } else {
        errorMsg = res.message || 'Login failed';
      }
    } catch (err) {
      errorMsg = err.message || 'Authentication error';
    } finally {
      isSubmitting = false;
    }
  }

  function launchDexBrowserFlow() {
    if (!authConfig) return;
    const state = 'state_' + Math.random().toString(36).substring(2, 12);
    const params = new URLSearchParams({
      client_id: authConfig.clientId,
      redirect_uri: authConfig.redirectUri,
      response_type: 'code',
      scope: 'openid profile email',
      state: state
    });

    const url = `${authConfig.authorizationEndpoint}?${params.toString()}`;
    window.location.href = url;
  }
</script>

<div class="modal-backdrop" onclick={onClose} onkeydown={(e) => e.key === 'Escape' && onClose()} role="presentation">
  <div class="modal-card" style="max-width: 680px; width: 95vw;" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()} role="dialog" aria-modal="true" tabindex="-1">
    
    <!-- Modal Header -->
    <div class="modal-header" style="border-bottom: 1px solid var(--border-subtle);">
      <div style="display: flex; gap: 12px; align-items: center;">
        <div style="width: 42px; height: 42px; border-radius: var(--radius-md); background: var(--accent-subtle); border: 1px solid var(--accent-border); display: flex; align-items: center; justify-content: center; color: var(--accent-primary);">
          <ShieldCheck size={24} />
        </div>
        <div>
          <h2 style="font-size: 17px; font-weight: 700; margin: 0; color: var(--text-main); display: flex; align-items: center; gap: 6px;">
            <span>Dex OIDC Authentication</span>
            <span class="brand-badge" style="font-size: 11px;">OpenID Connect</span>
          </h2>
          <div style="font-size: 12px; color: var(--text-dim); margin-top: 2px;">
            Federated Identity via Dex IdP (:5556) with Redis Session Persistence (:6379)
          </div>
        </div>
      </div>
      <button class="btn-icon" onclick={onClose} title="Close" aria-label="Close modal">
        <X size={18} />
      </button>
    </div>

    <!-- Dex & Redis Status Bar -->
    <div style="background: var(--bg-surface-elevated); padding: 10px 20px; border-bottom: 1px solid var(--border-subtle); display: flex; gap: 16px; align-items: center; justify-content: space-between; font-size: 12px; flex-wrap: wrap;">
      <div style="display: flex; align-items: center; gap: 6px; color: var(--text-muted);">
        <Server size={14} color="var(--accent-primary)" />
        <span>Dex IdP:</span>
        <code style="background: var(--bg-input); padding: 1px 6px; border-radius: var(--radius-xs); color: var(--text-main); font-size: 11px;">
          {authConfig?.dexIssuer || 'http://localhost:5556/dex'}
        </code>
      </div>

      <div style="display: flex; align-items: center; gap: 6px;">
        <Database size={14} color={authConfig?.redisConnected ? 'var(--accent-success)' : 'var(--accent-warning)'} />
        <span style="color: var(--text-muted);">Session Store:</span>
        {#if authConfig?.redisConnected}
          <span style="color: var(--accent-success); font-weight: 600; display: inline-flex; align-items: center; gap: 3px;">
            <CheckCircle2 size={12} /> Redis Active (port 6379)
          </span>
        {:else}
          <span style="color: var(--accent-warning); font-weight: 600;" title="Docker Redis container active when running docker-compose">
            Memory Fallback (Redis in Standby)
          </span>
        {/if}
      </div>
    </div>

    <!-- Error Alert if any -->
    {#if errorMsg}
      <div style="margin: 16px 20px 0; padding: 10px 14px; background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.25); border-radius: var(--radius-sm); font-size: 13px; color: #ef4444; display: flex; align-items: center; gap: 8px;">
        <AlertCircle size={16} />
        <span>{errorMsg}</span>
      </div>
    {/if}

    <div style="padding: 20px; display: flex; flex-direction: column; gap: 20px; max-height: 70vh; overflow-y: auto;">
      
      <!-- Primary: Provisioned Dex Personas -->
      <div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <h3 style="font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-muted); margin: 0; display: flex; align-items: center; gap: 6px;">
            <Sparkles size={14} color="var(--accent-primary)" />
            Provisioned Dex Personas (1-Click Login)
          </h3>
          <span style="font-size: 11px; color: var(--text-dim); background: var(--bg-surface-elevated); padding: 2px 8px; border-radius: var(--radius-full); border: 1px solid var(--border-subtle);">
            Password: <strong>password</strong>
          </span>
        </div>

        {#if loading}
          <div style="text-align: center; color: var(--text-dim); padding: 20px;">
            Loading provisioned Dex personas...
          </div>
        {:else if authConfig?.provisionedUsers}
          <div style="display: grid; grid-template-columns: 1fr; gap: 8px;">
            {#each authConfig.provisionedUsers as u}
              <div
                style="display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); gap: 12px; transition: border-color var(--transition-fast);"
              >
                <div style="display: flex; align-items: center; gap: 10px; min-width: 0;">
                  <img
                    src={u.avatarUrl}
                    alt={u.name}
                    class="avatar"
                    style="width: 38px; height: 38px; flex-shrink: 0;"
                  />
                  <div style="min-width: 0;">
                    <div style="font-size: 13px; font-weight: 700; color: var(--text-main); text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">
                      {u.name}
                    </div>
                    <div style="font-size: 11px; color: var(--text-dim); display: flex; align-items: center; gap: 6px;">
                      <span>@{u.handle}</span>
                      <span>•</span>
                      <span style="color: var(--accent-primary);">{u.email}</span>
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  class="btn-primary"
                  style="font-size: 12px; padding: 6px 14px; flex-shrink: 0; display: inline-flex; align-items: center; gap: 6px;"
                  disabled={loggingInEmail === u.email}
                  onclick={() => handleQuickLogin(u)}
                >
                  <KeyRound size={13} />
                  <span>{loggingInEmail === u.email ? 'Logging In...' : 'Sign In'}</span>
                </button>
              </div>
            {/each}
          </div>
        {/if}
      </div>

      <!-- Divider -->
      <div style="display: flex; align-items: center; gap: 10px; color: var(--text-dim); font-size: 12px;">
        <div style="flex: 1; height: 1px; background: var(--border-subtle);"></div>
        <span>OR</span>
        <div style="flex: 1; height: 1px; background: var(--border-subtle);"></div>
      </div>

      <!-- Dex Standard OIDC Redirect Flow -->
      <div style="padding: 14px; background: var(--bg-surface); border: 1px dashed var(--border-strong); border-radius: var(--radius-md); display: flex; align-items: center; justify-content: space-between; gap: 12px;">
        <div>
          <div style="font-size: 13px; font-weight: 700; color: var(--text-main); display: flex; align-items: center; gap: 6px;">
            <ShieldCheck size={16} color="var(--accent-primary)" />
            Dex Browser Redirect Flow
          </div>
          <div style="font-size: 12px; color: var(--text-dim); margin-top: 2px;">
            Standard OpenID Connect authorization code grant through Dex portal on port 5556
          </div>
        </div>

        <button
          type="button"
          class="btn-secondary"
          style="font-size: 12px; padding: 7px 14px; display: inline-flex; align-items: center; gap: 6px; white-space: nowrap;"
          onclick={launchDexBrowserFlow}
        >
          <span>Open Dex Portal</span>
          <ExternalLink size={13} />
        </button>
      </div>

      <!-- Custom Credentials Form -->
      <form onsubmit={handleCustomLogin} style="display: flex; flex-direction: column; gap: 10px; border-top: 1px solid var(--border-subtle); padding-top: 14px;">
        <span style="font-size: 12px; font-weight: 600; color: var(--text-dim);">Sign in with any provisioned email & password:</span>
        
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
          <div>
            <label for="dex-custom-email" style="font-size: 11px; color: var(--text-dim); display: block; margin-bottom: 3px;">Email or @Handle</label>
            <input
              id="dex-custom-email"
              type="text"
              placeholder="e.g. andrei.muresan@hosptim.ro or @dr_andrei_muresan"
              bind:value={customEmail}
              style="width: 100%; padding: 7px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            />
          </div>

          <div>
            <label for="dex-custom-pwd" style="font-size: 11px; color: var(--text-dim); display: block; margin-bottom: 3px;">Password</label>
            <input
              id="dex-custom-pwd"
              type="password"
              placeholder="password"
              bind:value={customPassword}
              style="width: 100%; padding: 7px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-main);"
            />
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 4px;">
          <button
            type="submit"
            class="btn-primary"
            style="font-size: 12px; padding: 7px 16px; display: inline-flex; align-items: center; gap: 6px;"
            disabled={isSubmitting}
          >
            <Lock size={13} />
            <span>{isSubmitting ? 'Verifying...' : 'Authenticate with Dex'}</span>
          </button>
        </div>
      </form>

    </div>
  </div>
</div>
