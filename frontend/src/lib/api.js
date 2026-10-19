// PeerMesh Client-Side API & Offline Local Storage

const API_BASE = '/api';

// Local storage keys for offline caching
const CACHE_POSTS_KEY = 'peermesh_cached_posts';
const CACHE_PROFILES_KEY = 'peermesh_cached_profiles';
const DRAFTS_KEY = 'peermesh_offline_drafts';

export async function fetchFeed({ type = 'all', tag = '', hasTables = null, hasImages = null, hasLinks = null, viewerId = null, limit = 25, offset = 0 } = {}) {
  const params = new URLSearchParams();
  if (type) params.set('type', type);
  if (tag) params.set('tag', tag);
  if (hasTables !== null) params.set('hasTables', hasTables);
  if (hasImages !== null) params.set('hasImages', hasImages);
  if (hasLinks !== null) params.set('hasLinks', hasLinks);
  if (viewerId) params.set('viewerId', viewerId);
  params.set('limit', limit);
  params.set('offset', offset);

  try {
    const res = await fetch(`${API_BASE}/feed?${params.toString()}`);
    if (!res.ok) throw new Error(`Feed error: ${res.status}`);
    const data = await res.json();
    // Cache for offline viewing
    try {
      localStorage.setItem(CACHE_POSTS_KEY, JSON.stringify(data));
    } catch (_) {}
    return { data, fromCache: false };
  } catch (err) {
    console.warn('Network fetch failed, attempting cache recovery:', err);
    const cached = localStorage.getItem(CACHE_POSTS_KEY);
    if (cached) {
      return { data: JSON.parse(cached), fromCache: true };
    }
    throw err;
  }
}

export async function searchPosts({ q = '', hasTables = null, hasImages = null, hasLinks = null, limit = 25, offset = 0 } = {}) {
  const params = new URLSearchParams();
  if (q) params.set('q', q);
  if (hasTables !== null) params.set('hasTables', hasTables);
  if (hasImages !== null) params.set('hasImages', hasImages);
  if (hasLinks !== null) params.set('hasLinks', hasLinks);
  params.set('limit', limit);
  params.set('offset', offset);

  const res = await fetch(`${API_BASE}/search?${params.toString()}`);
  if (!res.ok) throw new Error(`Search error: ${res.status}`);
  return await res.json();
}

export async function searchUnified(q = '') {
  if (!q || !q.trim()) {
    return { query: '', detectedRole: null, detectedLocation: null, people: [], companies: [], posts: [], totalCount: 0 };
  }
  const params = new URLSearchParams({ q: q.trim() });
  const res = await fetch(`${API_BASE}/search/unified?${params.toString()}`);
  if (!res.ok) throw new Error(`Unified search error: ${res.status}`);
  return await res.json();
}

export async function createPost(post) {
  const res = await fetch(`${API_BASE}/posts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(post)
  });
  if (!res.ok) throw new Error(`Create post error: ${res.status}`);
  return await res.json();
}

export async function togglePostLike(postId, userId) {
  const res = await fetch(`${API_BASE}/posts/${postId}/reactions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId })
  });
  if (!res.ok) throw new Error(`Toggle like error: ${res.status}`);
  return await res.json();
}

export async function fetchComments(postId) {
  const res = await fetch(`${API_BASE}/posts/${postId}/comments`);
  if (!res.ok) throw new Error(`Comments error: ${res.status}`);
  return await res.json();
}

export async function addComment(postId, authorId, bodyMarkdown) {
  const res = await fetch(`${API_BASE}/posts/${postId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ authorId, bodyMarkdown })
  });
  if (!res.ok) throw new Error(`Add comment error: ${res.status}`);
  return await res.json();
}

export async function fetchProfiles() {
  try {
    const res = await fetch(`${API_BASE}/profiles`);
    if (!res.ok) throw new Error(`Profiles error: ${res.status}`);
    const data = await res.json();
    try {
      localStorage.setItem(CACHE_PROFILES_KEY, JSON.stringify(data));
    } catch (_) {}
    return data;
  } catch (err) {
    const cached = localStorage.getItem(CACHE_PROFILES_KEY);
    if (cached) return JSON.parse(cached);
    throw err;
  }
}

export async function fetchProfile(handleOrId) {
  const res = await fetch(`${API_BASE}/profiles/${encodeURIComponent(handleOrId)}`);
  if (!res.ok) throw new Error(`Profile not found`);
  return await res.json();
}

export async function updateProfile(handleOrId, profileData) {
  const res = await fetch(`${API_BASE}/profiles/${encodeURIComponent(handleOrId)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(profileData)
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Failed to update profile: ${res.status}`);
  }
  return await res.json();
}

export async function fetchDecentralizedContact(handleOrId) {
  const res = await fetch(`${API_BASE}/profiles/${encodeURIComponent(handleOrId)}/contact`);
  if (!res.ok) throw new Error(`Contact not found`);
  return await res.json();
}

export async function fetchWorkExperiences(handleOrId) {
  const res = await fetch(`${API_BASE}/profiles/${encodeURIComponent(handleOrId)}/experience`);
  if (!res.ok) throw new Error(`Failed to fetch work experience: ${res.status}`);
  return await res.json();
}

export async function createWorkExperience(profileId, experience) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/experience`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(experience)
  });
  if (!res.ok) throw new Error(`Failed to create work experience: ${res.status}`);
  return await res.json();
}

export async function updateWorkExperience(profileId, expId, experience) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/experience/${expId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(experience)
  });
  if (!res.ok) throw new Error(`Failed to update work experience: ${res.status}`);
  return await res.json();
}

export async function deleteWorkExperience(profileId, expId) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/experience/${expId}`, {
    method: 'DELETE'
  });
  if (!res.ok) throw new Error(`Failed to delete work experience: ${res.status}`);
  return true;
}

// ---------------------------------------------------------------
// Education API (Academic Degrees & University Credentials)
// ---------------------------------------------------------------

export async function fetchEducations(handleOrId) {
  const res = await fetch(`${API_BASE}/profiles/${encodeURIComponent(handleOrId)}/education`);
  if (!res.ok) throw new Error(`Failed to fetch education: ${res.status}`);
  return await res.json();
}

export async function createEducation(profileId, education) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/education`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(education)
  });
  if (!res.ok) throw new Error(`Failed to create education: ${res.status}`);
  return await res.json();
}

export async function updateEducation(profileId, eduId, education) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/education/${eduId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(education)
  });
  if (!res.ok) throw new Error(`Failed to update education: ${res.status}`);
  return await res.json();
}

export async function deleteEducation(profileId, eduId) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/education/${eduId}`, {
    method: 'DELETE'
  });
  if (!res.ok) throw new Error(`Failed to delete education: ${res.status}`);
  return true;
}

// ---------------------------------------------------------------
// Highlights API (Achievements, Patents, Prizes, Books, Conferences)
// ---------------------------------------------------------------

export async function fetchHighlights(handleOrId) {
  const res = await fetch(`${API_BASE}/profiles/${encodeURIComponent(handleOrId)}/highlights`);
  if (!res.ok) throw new Error(`Failed to fetch highlights: ${res.status}`);
  return await res.json();
}

export async function createHighlight(profileId, highlight) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/highlights`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(highlight)
  });
  if (!res.ok) throw new Error(`Failed to create highlight: ${res.status}`);
  return await res.json();
}

export async function updateHighlight(profileId, highlightId, highlight) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/highlights/${highlightId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(highlight)
  });
  if (!res.ok) throw new Error(`Failed to update highlight: ${res.status}`);
  return await res.json();
}

export async function deleteHighlight(profileId, highlightId) {
  const res = await fetch(`${API_BASE}/profiles/${profileId}/highlights/${highlightId}`, {
    method: 'DELETE'
  });
  if (!res.ok) throw new Error(`Failed to delete highlight: ${res.status}`);
  return true;
}

// ---------------------------------------------------------------
// Object Storage API (Local Provider in Micronaut)
// ---------------------------------------------------------------

export async function uploadStorageFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch(`${API_BASE}/storage/upload`, {
    method: 'POST',
    body: formData
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Upload error: ${res.status}`);
  }
  return await res.json();
}

export async function uploadRawStorage(filename, content, contentType = 'text/plain') {
  const res = await fetch(`${API_BASE}/storage/upload/raw`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ filename, content, contentType })
  });
  if (!res.ok) throw new Error(`Raw upload error: ${res.status}`);
  return await res.json();
}

export async function fetchCompanies() {
  const res = await fetch(`${API_BASE}/companies`);
  if (!res.ok) throw new Error(`Companies error: ${res.status}`);
  return await res.json();
}

export async function fetch1stDegreeConnections(userId) {
  const res = await fetch(`${API_BASE}/graph/connections/${userId}`);
  if (!res.ok) throw new Error(`Connections error: ${res.status}`);
  return await res.json();
}

export async function fetch2ndDegreeSuggestions(userId, limit = 20) {
  const res = await fetch(`${API_BASE}/graph/suggestions/${userId}?limit=${limit}`);
  if (!res.ok) throw new Error(`Suggestions error: ${res.status}`);
  return await res.json();
}

export async function fetchShortestPath(fromId, toId) {
  const res = await fetch(`${API_BASE}/graph/path?from=${fromId}&to=${toId}`);
  if (!res.ok) throw new Error(`Path error: ${res.status}`);
  return await res.json();
}

export async function connectUsers(sourceId, targetId) {
  const res = await fetch(`${API_BASE}/graph/connect`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sourceId, targetId })
  });
  if (!res.ok) throw new Error(`Connect error: ${res.status}`);
  return await res.json();
}

export async function followEntity(sourceId, targetId, targetType = 'PERSON') {
  const res = await fetch(`${API_BASE}/graph/follow`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sourceId, targetId, targetType })
  });
  if (!res.ok) throw new Error(`Follow error: ${res.status}`);
  return await res.json();
}

export async function disconnectUsers(sourceId, targetId) {
  const res = await fetch(`${API_BASE}/graph/disconnect`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sourceId, targetId })
  });
  if (!res.ok) throw new Error(`Disconnect error: ${res.status}`);
  return await res.json();
}

export async function exportGdpr(userId) {
  const res = await fetch(`${API_BASE}/gdpr/export/${userId}`);
  if (!res.ok) throw new Error(`GDPR export error: ${res.status}`);
  return await res.json();
}

export async function purgeGdpr(userId) {
  const res = await fetch(`${API_BASE}/gdpr/purge/${userId}`, { method: 'DELETE' });
  if (!res.ok) throw new Error(`GDPR purge error: ${res.status}`);
  return await res.json();
}

// ---------------------------------------------------------------
// GitHub Self-Hosting Syndication & Synchronization
// ---------------------------------------------------------------

export async function previewGitHubContent(url) {
  const res = await fetch(`${API_BASE}/github/preview?url=${encodeURIComponent(url)}`);
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `GitHub preview error: ${res.status}`);
  }
  return await res.json();
}

export async function importGitHubPost(url, authorId) {
  const res = await fetch(`${API_BASE}/github/import-url`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url, authorId })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `GitHub import error: ${res.status}`);
  }
  return await res.json();
}

export async function syncGitHubPost(postId) {
  const res = await fetch(`${API_BASE}/github/sync-post/${postId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `GitHub re-sync error: ${res.status}`);
  }
  return await res.json();
}

// ---------------------------------------------------------------
// Dex OIDC Authentication & Redis Session Management
// ---------------------------------------------------------------

export async function fetchAuthConfig() {
  const res = await fetch(`${API_BASE}/auth/config`);
  if (!res.ok) throw new Error(`Auth config error: ${res.status}`);
  return await res.json();
}

export async function dexCallback(payload) {
  const res = await fetch(`${API_BASE}/auth/dex/callback`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || `Dex callback error: ${res.status}`);
  }
  return await res.json();
}

export async function dexDirectLogin(email, password = 'password') {
  const res = await fetch(`${API_BASE}/auth/dex/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || `Login failed: ${res.status}`);
  }
  return await res.json();
}

export async function fetchAuthSession(sessionId) {
  if (!sessionId) return { active: false };
  const res = await fetch(`${API_BASE}/auth/session?sessionId=${encodeURIComponent(sessionId)}`, {
    headers: { 'Authorization': `Bearer ${sessionId}` }
  });
  if (!res.ok) return { active: false };
  return await res.json();
}

export async function dexLogout(sessionId) {
  if (!sessionId) return { success: true };
  const res = await fetch(`${API_BASE}/auth/logout`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${sessionId}`
    },
    body: JSON.stringify({ sessionId })
  });
  if (!res.ok) return { success: false };
  return await res.json();
}

// ---------------------------------------------------------------
// Job Announcements & Object Storage API
// ---------------------------------------------------------------

export async function fetchJobs({ department = 'ALL', type = 'ALL', search = '', tag = '', limit = 50, offset = 0 } = {}) {
  const params = new URLSearchParams();
  if (department && department !== 'ALL') params.set('department', department);
  if (type && type !== 'ALL') params.set('type', type);
  if (search) params.set('search', search);
  if (tag) params.set('tag', tag);
  if (limit) params.set('limit', limit);
  if (offset) params.set('offset', offset);

  const res = await fetch(`${API_BASE}/jobs?${params.toString()}`);
  if (!res.ok) throw new Error(`Failed to fetch jobs: ${res.status}`);
  return await res.json();
}

export async function fetchJob(id) {
  const res = await fetch(`${API_BASE}/jobs/${encodeURIComponent(id)}`);
  if (!res.ok) throw new Error(`Failed to fetch job details: ${res.status}`);
  return await res.json();
}

export async function createJob(job) {
  const res = await fetch(`${API_BASE}/jobs`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(job)
  });
  if (!res.ok) throw new Error(`Failed to create job announcement: ${res.status}`);
  return await res.json();
}

export async function fetchJobStorage(id) {
  const res = await fetch(`${API_BASE}/storage/jobs/${encodeURIComponent(id)}`);
  if (!res.ok) throw new Error(`Failed to fetch job object storage: ${res.status}`);
  return await res.json();
}

