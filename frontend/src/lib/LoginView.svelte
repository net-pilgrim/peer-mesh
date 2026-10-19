<script>
  import { onMount } from 'svelte';
  import {
    ShieldCheck,
    KeyRound,
    User,
    Lock,
    ExternalLink,
    Database,
    Server,
    CheckCircle2,
    Sparkles,
    AlertCircle,
    ArrowRight,
    AtSign
  } from '@lucide/svelte';
  import { fetchAuthConfig, dexDirectLogin } from './api.js';

  let { onLoginSuccess, onContinueAsGuest } = $props();

  let authConfig = $state(null);
  let loading = $state(true);
  let loggingInEmail = $state(null);
  let errorMsg = $state('');

  // Credentials form
  let identifier = $state(''); // email or handle
  let password = $state('password');
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
      } else {
        errorMsg = res.message || 'Login failed';
      }
    } catch (err) {
      errorMsg = err.message || 'Authentication error';
    } finally {
      loggingInEmail = null;
    }
  }

  async function handleCredentialsLogin(e) {
    if (e) e.preventDefault();
    if (!identifier.trim()) {
      errorMsg = 'Please enter your email address or @handle';
      return;
    }
    isSubmitting = true;
    errorMsg = '';
    try {
      const res = await dexDirectLogin(identifier.trim(), password || 'password');
      if (res.success) {
        if (onLoginSuccess) onLoginSuccess(res);
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

<div class="login-page-container">
  <div class="login-card-wrapper">

    <!-- Brand Header -->
    <div class="login-brand-header">
      <div class="brand-logo" style="width: 44px; height: 44px; font-size: 22px; margin: 0 auto 12px;">P</div>
      <h1 style="margin: 0; font-size: 24px; font-weight: 800; color: var(--text-main); letter-spacing: -0.5px;">
        PeerMesh
      </h1>
      <p style="margin: 6px 0 0; font-size: 13px; color: var(--text-muted);">
        GDPR Sovereign & Decentralized Professional Network
      </p>
    </div>

    <!-- Dex & Redis Server Status Bar -->
    <div class="login-status-bar">
      <div style="display: flex; align-items: center; gap: 6px;">
        <Server size={13} color="var(--accent-primary)" />
        <span style="color: var(--text-dim);">Dex IdP:</span>
        <code style="background: var(--bg-input); padding: 1px 5px; border-radius: var(--radius-xs); font-size: 10px; color: var(--text-main);">
          :5556
        </code>
      </div>

      <div style="display: flex; align-items: center; gap: 6px;">
        <Database size={13} color={authConfig?.redisConnected ? 'var(--accent-success)' : 'var(--accent-warning)'} />
        <span style="color: var(--text-dim);">Session Store:</span>
        {#if authConfig?.redisConnected}
          <span style="color: var(--accent-success); font-weight: 600; display: inline-flex; align-items: center; gap: 3px; font-size: 11px;">
            <CheckCircle2 size={11} /> Redis Active (:6379)
          </span>
        {:else}
          <span style="color: var(--accent-warning); font-size: 11px;">
            Memory Fallback
          </span>
        {/if}
      </div>
    </div>

    <!-- Error Alert Banner -->
    {#if errorMsg}
      <div class="login-error-alert">
        <AlertCircle size={15} style="flex-shrink: 0;" />
        <span>{errorMsg}</span>
      </div>
    {/if}

    <!-- Main Authentication Form -->
    <div class="login-card-body">

      <!-- 1. Direct Credentials Form -->
      <form onsubmit={handleCredentialsLogin} style="display: flex; flex-direction: column; gap: 12px;">
        <div>
          <label for="login-id" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">
            Email Address or @Handle
          </label>
          <div style="position: relative; display: flex; align-items: center;">
            <span style="position: absolute; left: 10px; color: var(--text-dim);">
              <AtSign size={14} />
            </span>
            <input
              id="login-id"
              type="text"
              placeholder="e.g. andrei.muresan@hosptim.ro or @dr_andrei_muresan"
              bind:value={identifier}
              style="width: 100%; padding: 9px 12px 9px 32px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
            />
          </div>
        </div>

        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
            <label for="login-pwd" style="font-size: 12px; font-weight: 600; color: var(--text-dim);">
              Password
            </label>
            <span style="font-size: 11px; color: var(--text-dim);">
              Demo: <strong style="color: var(--accent-primary);">password</strong>
            </span>
          </div>
          <div style="position: relative; display: flex; align-items: center;">
            <span style="position: absolute; left: 10px; color: var(--text-dim);">
              <Lock size={14} />
            </span>
            <input
              id="login-pwd"
              type="password"
              placeholder="Enter password"
              bind:value={password}
              style="width: 100%; padding: 9px 12px 9px 32px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
            />
          </div>
        </div>

        <button
          type="submit"
          class="btn-primary"
          style="padding: 10px; width: 100%; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; justify-content: center; gap: 8px; margin-top: 4px; background: linear-gradient(135deg, var(--accent-primary), #6366f1);"
          disabled={isSubmitting}
        >
          <Lock size={14} />
          <span>{isSubmitting ? 'Authenticating with Dex...' : 'Sign In'}</span>
        </button>
      </form>

      <!-- Divider -->
      <div style="display: flex; align-items: center; gap: 10px; margin: 4px 0; color: var(--text-dim); font-size: 11px; text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px;">
        <div style="flex: 1; height: 1px; background: var(--border-subtle);"></div>
        <span>Or 1-Click Provisioned Personas</span>
        <div style="flex: 1; height: 1px; background: var(--border-subtle);"></div>
      </div>

      <!-- 2. One-Click Provisioned Dex Personas -->
      <div>
        {#if loading}
          <div style="text-align: center; color: var(--text-dim); padding: 16px; font-size: 12px;">
            Loading provisioned personas...
          </div>
        {:else if authConfig?.provisionedUsers}
          <div style="display: flex; flex-direction: column; gap: 7px; max-height: 240px; overflow-y: auto; padding-right: 2px;">
            {#each authConfig.provisionedUsers as u}
              <button
                type="button"
                class="login-persona-row"
                disabled={loggingInEmail === u.email}
                onclick={() => handleQuickLogin(u)}
              >
                <img
                  src={u.avatarUrl}
                  alt={u.name}
                  class="avatar"
                  style="width: 32px; height: 32px; flex-shrink: 0;"
                />
                <div style="text-align: left; overflow: hidden; flex: 1; min-width: 0;">
                  <div style="font-size: 12px; font-weight: 700; color: var(--text-main); text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">
                    {u.name}
                  </div>
                  <div style="font-size: 11px; color: var(--text-dim); text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">
                    <span>@{u.handle}</span>
                    <span style="margin: 0 3px;">•</span>
                    <span style="color: var(--accent-primary);">{u.role ? u.role.split('|')[0].trim() : u.email}</span>
                  </div>
                </div>

                <span class="btn-secondary" style="font-size: 11px; padding: 4px 10px; pointer-events: none; flex-shrink: 0;">
                  {loggingInEmail === u.email ? 'Logging In...' : 'Sign In →'}
                </span>
              </button>
            {/each}
          </div>
        {/if}
      </div>

      <!-- 3. Dex Browser SSO & Guest Options -->
      <div style="display: flex; flex-direction: column; gap: 8px; border-top: 1px solid var(--border-subtle); padding-top: 14px;">
        <button
          type="button"
          class="btn-secondary"
          style="width: 100%; padding: 8px; font-size: 12px; display: inline-flex; align-items: center; justify-content: center; gap: 6px;"
          onclick={launchDexBrowserFlow}
        >
          <ShieldCheck size={14} color="var(--accent-primary)" />
          <span>Sign In via Dex SSO Portal (:5556)</span>
          <ExternalLink size={12} />
        </button>

        {#if onContinueAsGuest}
          <button
            type="button"
            class="btn-subtle"
            style="width: 100%; padding: 6px; font-size: 12px; display: inline-flex; align-items: center; justify-content: center; gap: 6px; color: var(--text-dim);"
            onclick={onContinueAsGuest}
          >
            <span>Explore public network as Guest (Read-Only)</span>
            <ArrowRight size={13} />
          </button>
        {/if}
      </div>

    </div>
  </div>
</div>

<style>
  .login-page-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px 16px;
    background: radial-gradient(circle at 50% 0%, rgba(99, 102, 241, 0.08), transparent 70%), var(--bg-main);
  }

  .login-card-wrapper {
    width: 100%;
    max-width: 480px;
    background: var(--bg-surface);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-lg);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    overflow: hidden;
  }

  .login-brand-header {
    text-align: center;
    padding: 28px 24px 16px;
    background: var(--bg-surface-elevated);
    border-bottom: 1px solid var(--border-subtle);
  }

  .login-status-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 18px;
    background: var(--bg-surface);
    border-bottom: 1px solid var(--border-subtle);
    font-size: 11px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .login-error-alert {
    margin: 14px 18px 0;
    padding: 9px 12px;
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.25);
    border-radius: var(--radius-sm);
    color: #ef4444;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .login-card-body {
    padding: 20px 22px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .login-persona-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 7px 10px;
    background: var(--bg-surface-elevated);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-sm);
    gap: 10px;
    cursor: pointer;
    transition: all var(--transition-fast);
    width: 100%;
  }

  .login-persona-row:hover {
    border-color: var(--accent-primary);
    background: var(--accent-subtle);
  }
</style>
