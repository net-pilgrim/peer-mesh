<script>
  import { onMount } from 'svelte';
  import { marked } from 'marked';
  import {
    Briefcase,
    GraduationCap,
    Building,
    Calendar,
    MapPin,
    Award,
    BookOpen,
    Plus,
    Trash,
    Pencil,
    X,
    Layers,
    Sparkles,
    Trophy,
    Medal,
    ScrollText,
    Mic,
    ExternalLink,
    UserCheck,
    User,
    Mail,
    Camera,
    Save,
    AtSign,
    GitBranch,
    FolderGit,
    Eye,
    Edit3,
    Check,
    Shield,
    Tag,
    Globe,
    Key
  } from '@lucide/svelte';
  import {
    fetchWorkExperiences,
    createWorkExperience,
    updateWorkExperience,
    deleteWorkExperience,
    fetchEducations,
    createEducation,
    updateEducation,
    deleteEducation,
    fetchHighlights,
    createHighlight,
    updateHighlight,
    deleteHighlight,
    fetchCompanies,
    updateProfile
  } from './api.js';

  let { profile, currentProfile, initialTab = 'personal', onClose, onExperiencesUpdated, onProfileUpdated } = $props();

  // Top filter pills: 'all' | 'highlights' | 'experience' | 'education'
  let activeFilter = $state('all');
  let experiences = $state([]);
  let educations = $state([]);
  let highlights = $state([]);
  let companies = $state([]);
  let loading = $state(true);

  // Sub-filter for highlights
  let highlightTypeFilter = $state('ALL'); // 'ALL' | 'ACHIEVEMENT' | 'PATENT' | 'PRIZE' | 'BOOK' | 'CONFERENCE'

  // Single unified Add/Edit Form
  let showForm = $state(false);
  let formCategory = $state('HIGHLIGHT'); // 'HIGHLIGHT' | 'EXPERIENCE' | 'EDUCATION'
  let editingId = $state(null);

  // Highlight form fields
  let hlType = $state('ACHIEVEMENT');
  let hlTitle = $state('');
  let hlIssuer = $state('');
  let hlDate = $state('');
  let hlUrl = $state('');
  let hlDesc = $state('');

  // Experience form fields
  let expTitle = $state('');
  let expCompany = $state('');
  let expCompanyId = $state('');
  let expLocation = $state('');
  let expStartDate = $state('');
  let expEndDate = $state('');
  let expIsCurrent = $state(false);
  let expDesc = $state('');
  let expSkills = $state('');

  // Education form fields
  let eduInst = $state('');
  let eduDegree = $state('');
  let eduField = $state('');
  let eduStartDate = $state('');
  let eduEndDate = $state('');
  let eduIsCurrent = $state(false);
  let eduGrade = $state('');
  let eduActivities = $state('');
  let eduSkills = $state('');

  let isSaving = $state(false);

  // 2 Top-Level Tabs: 'personal' | 'timeline'
  let mainTab = $state('personal');
  // Sub-section within Personal Data: 'basic' | 'media' | 'skills' | 'contact' | 'github'
  let activePersonalSection = $state('basic');

  $effect(() => {
    if (initialTab === 'personal') {
      mainTab = 'personal';
    } else {
      mainTab = 'timeline';
      if (initialTab && initialTab !== 'timeline') {
        activeFilter = initialTab;
      }
    }
  });

  // Personal Data Form State
  let pHandle = $state('');
  let pDisplayName = $state('');
  let pHeadline = $state('');
  let pBioMarkdown = $state('');
  let pAvatarUrl = $state('');
  let pBannerUrl = $state('');
  let pLocation = $state('');
  let pSkillsInput = $state('');
  let pEmailContact = $state('');
  let pMatrixHandle = $state('');
  let pNostrNpub = $state('');
  let pSimplexAddress = $state('');
  let pSessionId = $state('');
  let pPgpFingerprint = $state('');
  let pGithubUsername = $state('');
  let pGithubContentRepo = $state('');

  let isSavingPersonal = $state(false);
  let personalError = $state('');
  let personalSuccess = $state('');
  let showBioPreview = $state(false);

  $effect(() => {
    if (profile) {
      pHandle = profile.handle || '';
      pDisplayName = profile.displayName || '';
      pHeadline = profile.headline || '';
      pBioMarkdown = profile.bioMarkdown || '';
      pAvatarUrl = profile.avatarUrl || '';
      pBannerUrl = profile.bannerUrl || '';
      pLocation = profile.location || '';
      pSkillsInput = profile.skills ? profile.skills.join(', ') : '';
      pEmailContact = profile.emailContact || '';
      pMatrixHandle = profile.matrixHandle || '';
      pNostrNpub = profile.nostrNpub || '';
      pSimplexAddress = profile.simplexAddress || '';
      pSessionId = profile.sessionId || '';
      pPgpFingerprint = profile.pgpFingerprint || '';
      pGithubUsername = profile.githubUsername || '';
      pGithubContentRepo = profile.githubContentRepo || '';
    }
  });

  const parsedPersonalSkills = $derived.by(() => {
    return pSkillsInput
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);
  });

  async function handleSavePersonalData() {
    if (!pDisplayName.trim()) {
      personalError = 'Display name cannot be empty.';
      return;
    }

    isSavingPersonal = true;
    personalError = '';
    personalSuccess = '';

    const payload = {
      handle: pHandle.trim().replace(/^@/, ''),
      displayName: pDisplayName.trim(),
      headline: pHeadline.trim(),
      bioMarkdown: pBioMarkdown.trim(),
      avatarUrl: pAvatarUrl.trim(),
      bannerUrl: pBannerUrl.trim(),
      location: pLocation.trim(),
      skills: parsedPersonalSkills,
      emailContact: pEmailContact.trim(),
      matrixHandle: pMatrixHandle.trim(),
      nostrNpub: pNostrNpub.trim(),
      simplexAddress: pSimplexAddress.trim(),
      sessionId: pSessionId.trim(),
      pgpFingerprint: pPgpFingerprint.trim(),
      githubUsername: pGithubUsername.trim(),
      githubContentRepo: pGithubContentRepo.trim()
    };

    try {
      const updated = await updateProfile(profile.id || profile.handle, payload);
      personalSuccess = 'Personal data saved successfully!';
      Object.assign(profile, updated);
      if (onProfileUpdated) {
        onProfileUpdated(updated);
      }
      setTimeout(() => {
        personalSuccess = '';
      }, 3000);
    } catch (err) {
      personalError = err.message || 'Failed to save personal data';
    } finally {
      isSavingPersonal = false;
    }
  }

  const isOwnProfile = $derived(Boolean(currentProfile && profile && (currentProfile.id === profile.id || currentProfile.handle === profile.handle)));

  // Combined sorted Career & Education list
  const combinedCareerEdu = $derived.by(() => {
    const exps = experiences.map(e => ({ ...e, itemType: 'experience' }));
    const edus = educations.map(ed => ({ ...ed, itemType: 'education' }));
    return [...exps, ...edus].sort((a, b) => (b.startDate || '').localeCompare(a.startDate || ''));
  });

  // Filtered highlights list
  const filteredHighlights = $derived.by(() => {
    if (highlightTypeFilter === 'ALL') return highlights;
    return highlights.filter(h => h.type === highlightTypeFilter);
  });

  function extractYear(val) {
    if (!val) return 'Other';
    const match = String(val).match(/\b(19\d\d|20\d\d)\b/);
    return match ? match[1] : 'Other';
  }

  function formatDuration(startDate, endDate, isCurrent) {
    if (!startDate) return '';
    const startY = parseInt(extractYear(startDate), 10);
    if (isNaN(startY)) return '';

    let endY;
    if (isCurrent) {
      endY = new Date().getFullYear();
    } else if (endDate) {
      endY = parseInt(extractYear(endDate), 10);
    } else {
      endY = startY;
    }
    if (isNaN(endY)) endY = startY;

    // Check if months are provided
    const startParts = String(startDate).split('-');
    const endParts = endDate ? String(endDate).split('-') : [];
    if (startParts.length >= 2 && (endParts.length >= 2 || isCurrent)) {
      const sm = parseInt(startParts[1], 10) || 1;
      const em = isCurrent ? (new Date().getMonth() + 1) : (parseInt(endParts[1], 10) || 12);
      const totalMonths = Math.max(1, (endY - startY) * 12 + (em - sm));
      const yrs = Math.floor(totalMonths / 12);
      const mos = totalMonths % 12;
      if (yrs > 0 && mos > 0) return `${yrs} yr${yrs > 1 ? 's' : ''} ${mos} mo${mos > 1 ? 's' : ''}`;
      if (yrs > 0) return `${yrs} yr${yrs > 1 ? 's' : ''}`;
      return `${mos} mo${mos > 1 ? 's' : ''}`;
    }

    const diff = Math.max(1, endY - startY + 1);
    return `${diff} ${diff === 1 ? 'yr' : 'yrs'}`;
  }

  // Collect all distinct milestone years across highlights, career, and education
  const allTimelineYears = $derived.by(() => {
    const yearSet = new Set();

    for (const hl of filteredHighlights) {
      const yr = extractYear(hl.dateOrYear);
      if (yr !== 'Other') yearSet.add(yr);
    }

    for (const item of combinedCareerEdu) {
      const startY = extractYear(item.startDate);
      if (startY !== 'Other') yearSet.add(startY);

      if (item.isCurrent) {
        const curY = String(new Date().getFullYear());
        yearSet.add(curY);
      } else if (item.endDate) {
        const endY = extractYear(item.endDate);
        if (endY !== 'Other') yearSet.add(endY);
      }
    }

    // Sort descending numerically
    const sorted = Array.from(yearSet).sort((a, b) => parseInt(b, 10) - parseInt(a, 10));

    // If there are items with 'Other' (unparseable year), append 'Other' at the end
    const hasOther = filteredHighlights.some(h => extractYear(h.dateOrYear) === 'Other') ||
      combinedCareerEdu.some(c => extractYear(c.startDate) === 'Other');
    if (hasOther) {
      sorted.push('Other');
    }

    return sorted;
  });

  // Map each year to 1-based CSS Grid row index
  const yearRowMap = $derived.by(() => {
    const map = new Map();
    allTimelineYears.forEach((yr, idx) => {
      map.set(yr, idx + 1);
    });
    return map;
  });

  // Group highlights by year for Left Column
  const highlightsByYear = $derived.by(() => {
    const map = new Map();
    for (const hl of filteredHighlights) {
      const yr = extractYear(hl.dateOrYear);
      if (!map.has(yr)) map.set(yr, []);
      map.get(yr).push(hl);
    }
    for (const list of map.values()) {
      list.sort((a, b) => (b.dateOrYear || '').localeCompare(a.dateOrYear || ''));
    }
    return map;
  });

  // Compute spanning Right Column items (Career & Education) with lane assignments
  const spanningRightItems = $derived.by(() => {
    const totalRows = allTimelineYears.length;
    const curYearStr = String(new Date().getFullYear());

    const items = combinedCareerEdu.map(item => {
      const startY = extractYear(item.startDate);
      let endY = startY;

      if (item.isCurrent) {
        endY = curYearStr;
      } else if (item.endDate) {
        endY = extractYear(item.endDate);
      }

      let topY = endY;
      let bottomY = startY;

      // Ensure topYear >= bottomYear
      if (topY !== 'Other' && bottomY !== 'Other' && parseInt(topY, 10) < parseInt(bottomY, 10)) {
        const tmp = topY;
        topY = bottomY;
        bottomY = tmp;
      }

      const topRow = yearRowMap.get(topY) || (topY === 'Other' ? totalRows : 1);
      const bottomRow = yearRowMap.get(bottomY) || (bottomY === 'Other' ? totalRows : totalRows);

      const rStart = Math.min(topRow, bottomRow);
      const rEnd = Math.max(topRow, bottomRow);

      return {
        ...item,
        topYear: topY,
        bottomYear: bottomY,
        topRow: rStart,
        bottomRow: rEnd,
        isSpanning: rEnd > rStart,
        durationText: formatDuration(item.startDate, item.endDate, item.isCurrent)
      };
    });

    // Sort primarily by topRow ascending, then by bottomRow descending (longer items first)
    items.sort((a, b) => {
      if (a.topRow !== b.topRow) return a.topRow - b.topRow;
      return b.bottomRow - a.bottomRow;
    });

    // Greedy interval packing to assign lanes
    const laneEndRows = [];
    const withLanes = items.map(item => {
      let assignedLane = -1;
      for (let l = 0; l < laneEndRows.length; l++) {
        if (laneEndRows[l] < item.topRow) {
          assignedLane = l;
          laneEndRows[l] = item.bottomRow;
          break;
        }
      }
      if (assignedLane === -1) {
        assignedLane = laneEndRows.length;
        laneEndRows.push(item.bottomRow);
      }
      return { ...item, lane: assignedLane };
    });

    // Detect overlap and determine totalLanes needed for each item
    return withLanes.map(item => {
      const overlappingItems = withLanes.filter(other => 
        other.id !== item.id &&
        Math.max(item.topRow, other.topRow) <= Math.min(item.bottomRow, other.bottomRow)
      );

      const hasOverlap = overlappingItems.length > 0;
      let totalLanes = 1;
      if (hasOverlap) {
        const maxL = Math.max(item.lane, ...overlappingItems.map(o => o.lane));
        totalLanes = maxL + 1;
      }

      return {
        ...item,
        hasOverlap,
        totalLanes
      };
    });
  });

  function getLaneStyle(item) {
    if (item.totalLanes <= 1) {
      return 'width: 100%; margin-left: 0;';
    }
    const pct = 100 / item.totalLanes;
    const gap = 6;
    const width = `calc(${pct}% - ${gap}px)`;
    const left = `calc(${item.lane * pct}% + ${item.lane > 0 ? gap : 0}px)`;
    return `width: ${width}; margin-left: ${left};`;
  }

  onMount(async () => {
    await loadData();
  });

  async function loadData() {
    loading = true;
    try {
      const handleOrId = profile.handle || profile.id;
      const [exps, edus, comps, hls] = await Promise.all([
        fetchWorkExperiences(handleOrId).catch(() => []),
        fetchEducations(handleOrId).catch(() => []),
        fetchCompanies().catch(() => []),
        fetchHighlights(handleOrId).catch(() => [])
      ]);
      experiences = exps;
      educations = edus;
      companies = comps;
      highlights = hls;
    } catch (err) {
      console.error('Failed to load timeline data:', err);
    } finally {
      loading = false;
    }
  }

  // --- Single Add Entry Initiation ---
  function startAddEntry(presetCategory = null) {
    editingId = null;
    formCategory = presetCategory || (activeFilter === 'experience' ? 'EXPERIENCE' : activeFilter === 'education' ? 'EDUCATION' : 'HIGHLIGHT');
    
    // Reset Highlight
    hlType = 'ACHIEVEMENT';
    hlTitle = '';
    hlIssuer = '';
    hlDate = '';
    hlUrl = '';
    hlDesc = '';

    // Reset Experience
    expTitle = '';
    expCompany = '';
    expCompanyId = '';
    expLocation = '';
    expStartDate = '';
    expEndDate = '';
    expIsCurrent = false;
    expDesc = '';
    expSkills = '';

    // Reset Education
    eduInst = '';
    eduDegree = '';
    eduField = '';
    eduStartDate = '';
    eduEndDate = '';
    eduIsCurrent = false;
    eduGrade = '';
    eduActivities = '';
    eduSkills = '';

    showForm = true;
  }

  // --- Edit Handlers ---
  function editHighlight(h) {
    editingId = h.id;
    formCategory = 'HIGHLIGHT';
    hlType = h.type || 'ACHIEVEMENT';
    hlTitle = h.title || '';
    hlIssuer = h.issuerOrVenue || '';
    hlDate = h.dateOrYear || '';
    hlUrl = h.externalUrl || '';
    hlDesc = h.descriptionMarkdown || '';
    showForm = true;
  }

  function editExperience(exp) {
    editingId = exp.id;
    formCategory = 'EXPERIENCE';
    expTitle = exp.title || '';
    expCompany = exp.companyName || '';
    expCompanyId = exp.companyId || '';
    expLocation = exp.location || '';
    expStartDate = exp.startDate || '';
    expEndDate = exp.endDate || '';
    expIsCurrent = !!exp.isCurrent;
    expDesc = exp.descriptionMarkdown || '';
    expSkills = exp.skills ? exp.skills.join(', ') : '';
    showForm = true;
  }

  function editEducation(edu) {
    editingId = edu.id;
    formCategory = 'EDUCATION';
    eduInst = edu.institutionName || '';
    eduDegree = edu.degree || '';
    eduField = edu.fieldOfStudy || '';
    eduStartDate = edu.startDate || '';
    eduEndDate = edu.endDate || '';
    eduIsCurrent = !!edu.isCurrent;
    eduGrade = edu.grade || '';
    eduActivities = edu.activitiesMarkdown || '';
    eduSkills = edu.skills ? edu.skills.join(', ') : '';
    showForm = true;
  }

  // --- Unified Save Handler ---
  async function handleSave() {
    isSaving = true;
    try {
      if (formCategory === 'HIGHLIGHT') {
        if (!hlTitle.trim() || !hlDate.trim()) {
          alert('Please enter highlight title and date/year');
          return;
        }
        const payload = {
          type: hlType,
          title: hlTitle.trim(),
          issuerOrVenue: hlIssuer.trim(),
          dateOrYear: hlDate.trim(),
          externalUrl: hlUrl.trim(),
          descriptionMarkdown: hlDesc.trim()
        };
        if (editingId) {
          await updateHighlight(currentProfile.id, editingId, payload);
        } else {
          await createHighlight(currentProfile.id, payload);
        }
      } else if (formCategory === 'EXPERIENCE') {
        if (!expTitle.trim() || !expCompany.trim() || !expStartDate.trim()) {
          alert('Please enter role title, company name, and start date');
          return;
        }
        const skillsArray = expSkills.split(',').map(s => s.trim()).filter(s => s.length > 0);
        const payload = {
          title: expTitle.trim(),
          companyName: expCompany.trim(),
          companyId: expCompanyId ? expCompanyId : null,
          location: expLocation.trim(),
          startDate: expStartDate.trim(),
          endDate: expIsCurrent ? '' : expEndDate.trim(),
          isCurrent: expIsCurrent,
          descriptionMarkdown: expDesc.trim(),
          skills: skillsArray
        };
        if (editingId) {
          await updateWorkExperience(currentProfile.id, editingId, payload);
        } else {
          await createWorkExperience(currentProfile.id, payload);
        }
        if (onExperiencesUpdated) onExperiencesUpdated();
      } else if (formCategory === 'EDUCATION') {
        if (!eduInst.trim() || !eduDegree.trim() || !eduStartDate.trim()) {
          alert('Please enter institution name, degree, and start date');
          return;
        }
        const skillsArray = eduSkills.split(',').map(s => s.trim()).filter(s => s.length > 0);
        const payload = {
          institutionName: eduInst.trim(),
          degree: eduDegree.trim(),
          fieldOfStudy: eduField.trim(),
          startDate: eduStartDate.trim(),
          endDate: eduIsCurrent ? '' : eduEndDate.trim(),
          isCurrent: eduIsCurrent,
          grade: eduGrade.trim(),
          activitiesMarkdown: eduActivities.trim(),
          skills: skillsArray
        };
        if (editingId) {
          await updateEducation(currentProfile.id, editingId, payload);
        } else {
          await createEducation(currentProfile.id, payload);
        }
        if (onExperiencesUpdated) onExperiencesUpdated();
      }

      showForm = false;
      await loadData();
    } catch (err) {
      alert('Failed to save timeline entry: ' + err.message);
    } finally {
      isSaving = false;
    }
  }

  // --- Deletion Handlers ---
  async function handleDelete(category, id) {
    if (!confirm(`Are you sure you want to delete this ${category.toLowerCase()} record?`)) return;
    try {
      if (category === 'HIGHLIGHT') {
        await deleteHighlight(currentProfile.id, id);
      } else if (category === 'EXPERIENCE') {
        await deleteWorkExperience(currentProfile.id, id);
        if (onExperiencesUpdated) onExperiencesUpdated();
      } else if (category === 'EDUCATION') {
        await deleteEducation(currentProfile.id, id);
        if (onExperiencesUpdated) onExperiencesUpdated();
      }
      await loadData();
    } catch (err) {
      alert('Delete failed: ' + err.message);
    }
  }
</script>

<div class="modal-backdrop" onclick={onClose} onkeydown={(e) => e.key === 'Escape' && onClose()} role="presentation">
  <div class="modal-card" style="max-width: 1080px; width: 95vw;" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()} role="dialog" aria-modal="true" tabindex="-1">
    
    <!-- Modal Header -->
    <div class="modal-header">
      <div style="display: flex; gap: 12px; align-items: center;">
        <img
          src={profile.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
          alt={profile.displayName}
          class="avatar"
          style="width: 44px; height: 44px;"
        />
        <div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <h2 style="font-size: 17px; font-weight: 700; margin: 0; color: var(--text-main);">
              {profile.displayName || profile.handle}
            </h2>
          </div>
          {#if profile.headline}
            <div style="font-size: 12px; font-weight: 500; color: var(--accent-primary); margin-top: 2px;">
              {profile.headline}
            </div>
          {/if}
          <div style="font-size: 12px; color: var(--text-dim); display: flex; align-items: center; gap: 8px; margin-top: 2px;">
            <span>@{profile.handle}</span>
            {#if profile.location}
              <span>•</span>
              <span style="display: inline-flex; align-items: center; gap: 3px;">
                <MapPin size={12} /> {profile.location}
              </span>
            {/if}
          </div>
        </div>
      </div>
      <button class="btn-icon" onclick={onClose} title="Close" aria-label="Close modal">
        <X size={18} />
      </button>
    </div>

    <!-- 2 Top-Level Tabs: Personal Data & Timeline -->
    <div class="profile-modal-tabs-bar">
      <button
        type="button"
        class="profile-modal-tab"
        class:active={mainTab === 'personal'}
        onclick={() => mainTab = 'personal'}
      >
        <User size={15} />
        <span>Personal Data</span>
      </button>

      <button
        type="button"
        class="profile-modal-tab"
        class:active={mainTab === 'timeline'}
        onclick={() => mainTab = 'timeline'}
      >
        <Layers size={15} />
        <span>Timeline ({highlights.length + experiences.length + educations.length})</span>
      </button>
    </div>

    <!-- TAB 1: PERSONAL DATA -->
    {#if mainTab === 'personal'}
      {#if isOwnProfile}
        <!-- Personal Data Sub-Section Navigation & Top Save Button -->
        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding: 8px 20px; background: var(--bg-surface-elevated); flex-wrap: wrap; gap: 10px;">
          <div style="display: flex; gap: 6px; overflow-x: auto; padding: 2px 0;">
            <button
              type="button"
              class="highlight-filter-chip"
              class:active={activePersonalSection === 'basic'}
              onclick={() => activePersonalSection = 'basic'}
            >
              <User size={13} />
              <span>Identity & Bio</span>
            </button>

            <button
              type="button"
              class="highlight-filter-chip"
              class:active={activePersonalSection === 'media'}
              onclick={() => activePersonalSection = 'media'}
            >
              <Camera size={13} />
              <span>Avatar & Banner</span>
            </button>

            <button
              type="button"
              class="highlight-filter-chip"
              class:active={activePersonalSection === 'skills'}
              onclick={() => activePersonalSection = 'skills'}
            >
              <Tag size={13} />
              <span>Skills ({parsedPersonalSkills.length})</span>
            </button>

            <button
              type="button"
              class="highlight-filter-chip"
              class:active={activePersonalSection === 'contact'}
              onclick={() => activePersonalSection = 'contact'}
            >
              <Shield size={13} />
              <span>Decentralized Channels</span>
            </button>

            <button
              type="button"
              class="highlight-filter-chip"
              class:active={activePersonalSection === 'github'}
              onclick={() => activePersonalSection = 'github'}
            >
              <GitBranch size={13} />
              <span>Code Provenance</span>
            </button>
          </div>

          <button
            type="button"
            class="btn-primary"
            style="font-size: 12px; padding: 6px 16px; display: inline-flex; align-items: center; gap: 6px;"
            onclick={handleSavePersonalData}
            disabled={isSavingPersonal}
          >
            <Save size={14} />
            <span>{isSavingPersonal ? 'Saving...' : 'Save Personal Data'}</span>
          </button>
        </div>

        <!-- Personal Data Form Body -->
        <div style="padding: 22px; max-height: calc(85vh - 140px); overflow-y: auto; display: flex; flex-direction: column; gap: 18px;">
          {#if personalError}
            <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.3); color: var(--accent-danger); padding: 10px 14px; border-radius: var(--radius-sm); font-size: 13px;">
              {personalError}
            </div>
          {/if}

          {#if personalSuccess}
            <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); color: var(--accent-success); padding: 10px 14px; border-radius: var(--radius-sm); font-size: 13px; display: flex; align-items: center; gap: 6px;">
              <Check size={16} />
              <span>{personalSuccess}</span>
            </div>
          {/if}

          <!-- SUB-TAB 1: IDENTITY & BIO -->
          {#if activePersonalSection === 'basic'}
            <div style="display: flex; flex-direction: column; gap: 14px;">
              <div class="work-form-grid">
                <div>
                  <label for="p-display-name" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Display Name *</label>
                  <input
                    id="p-display-name"
                    type="text"
                    placeholder="e.g. Dr. Andrei Mureșan"
                    bind:value={pDisplayName}
                    style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                  />
                </div>

                <div>
                  <label for="p-handle" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Unique Handle (@username)</label>
                  <div style="position: relative; display: flex; align-items: center;">
                    <span style="position: absolute; left: 10px; color: var(--text-dim); font-size: 13px; font-weight: 600;">@</span>
                    <input
                      id="p-handle"
                      type="text"
                      placeholder="e.g. dr_andrei_muresan"
                      bind:value={pHandle}
                      style="width: 100%; padding: 8px 10px 8px 26px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label for="p-headline" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Professional Headline / Title</label>
                <input
                  id="p-headline"
                  type="text"
                  placeholder="e.g. Chief of Cardiology & Emergency Doctor | Timișoara County Emergency Clinical Hospital"
                  bind:value={pHeadline}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>

              <div>
                <label for="p-location" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Location (City, Country - Privacy Preserving, No GPS)</label>
                <div style="position: relative; display: flex; align-items: center;">
                  <span style="position: absolute; left: 10px; color: var(--text-dim);">
                    <MapPin size={14} />
                  </span>
                  <input
                    id="p-location"
                    type="text"
                    placeholder="e.g. Timișoara, Romania"
                    bind:value={pLocation}
                    style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                  />
                </div>
              </div>

              <div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                  <label for="p-bio" style="font-size: 12px; font-weight: 600; color: var(--text-dim);">Professional Summary / Bio (Markdown Enabled)</label>
                  <button
                    type="button"
                    class="btn-icon"
                    style="font-size: 11px; padding: 2px 6px; display: inline-flex; align-items: center; gap: 4px;"
                    onclick={() => showBioPreview = !showBioPreview}
                  >
                    {#if showBioPreview}
                      <Edit3 size={12} /> <span>Switch to Editor</span>
                    {:else}
                      <Eye size={12} /> <span>Preview Markdown</span>
                    {/if}
                  </button>
                </div>

                {#if showBioPreview}
                  <div class="work-desc-markdown" style="min-height: 120px; padding: 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm);">
                    {#if pBioMarkdown}
                      {@html marked.parse(pBioMarkdown, { gfm: true, breaks: true })}
                    {:else}
                      <em style="color: var(--text-dim);">No bio text entered yet.</em>
                    {/if}
                  </div>
                {:else}
                  <textarea
                    id="p-bio"
                    rows="5"
                    placeholder="Write your professional summary, clinical focus, research interests, or background..."
                    bind:value={pBioMarkdown}
                    style="width: 100%; padding: 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main); resize: vertical; line-height: 1.5;"
                  ></textarea>
                {/if}
              </div>
            </div>
          {/if}

          <!-- SUB-TAB 2: AVATAR & BANNER -->
          {#if activePersonalSection === 'media'}
            <div style="display: flex; flex-direction: column; gap: 16px;">
              <div style="display: flex; gap: 16px; align-items: center; padding: 16px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md);">
                <img
                  src={pAvatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
                  alt="Avatar Preview"
                  style="width: 72px; height: 72px; border-radius: 50%; object-fit: cover; border: 2px solid var(--accent-primary);"
                  onerror={(e) => e.target.src = 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
                />
                <div style="flex: 1;">
                  <label for="p-avatar-url" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Profile Avatar URL</label>
                  <input
                    id="p-avatar-url"
                    type="text"
                    placeholder="https://... image URL (object storage, unsplash, etc.)"
                    bind:value={pAvatarUrl}
                    style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                  />
                  <span style="font-size: 11px; color: var(--text-dim); margin-top: 4px; display: block;">
                    Recommended: Square image URL (JPEG/PNG/WebP), hosted via object storage or public CDN.
                  </span>
                </div>
              </div>

              <div style="padding: 16px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 10px;">
                <label for="p-banner-url" style="font-size: 12px; font-weight: 600; color: var(--text-dim);">Profile Header Banner URL (Optional)</label>
                <input
                  id="p-banner-url"
                  type="text"
                  placeholder="https://images.unsplash.com/... header banner image"
                  bind:value={pBannerUrl}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />

                {#if pBannerUrl}
                  <div style="width: 100%; height: 90px; border-radius: var(--radius-sm); overflow: hidden; border: 1px solid var(--border-subtle);">
                    <img
                      src={pBannerUrl}
                      alt="Banner preview"
                      style="width: 100%; height: 100%; object-fit: cover;"
                      onerror={(e) => e.target.style.display = 'none'}
                    />
                  </div>
                {/if}
              </div>
            </div>
          {/if}

          <!-- SUB-TAB 3: SKILLS -->
          {#if activePersonalSection === 'skills'}
            <div style="display: flex; flex-direction: column; gap: 14px;">
              <div>
                <label for="p-skills" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Skills & Competencies (Comma-separated)</label>
                <input
                  id="p-skills"
                  type="text"
                  placeholder="e.g. Cardiology, Emergency Medicine, Echocardiography, Clinical Triage, Interventional"
                  bind:value={pSkillsInput}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
                <span style="font-size: 11px; color: var(--text-dim); margin-top: 4px; display: block;">
                  Separate tags with commas. These empower the Unified Professional Discovery search engine.
                </span>
              </div>

              <div>
                <span style="font-size: 11px; font-weight: 600; text-transform: uppercase; color: var(--text-dim); display: block; margin-bottom: 8px;">
                  Live Preview Tags ({parsedPersonalSkills.length}):
                </span>
                <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                  {#if parsedPersonalSkills.length === 0}
                    <span style="font-size: 12px; color: var(--text-dim); font-style: italic;">No skills specified yet.</span>
                  {:else}
                    {#each parsedPersonalSkills as skill}
                      <span class="search-skill-pill" style="font-size: 12px; padding: 3px 10px; background: var(--accent-subtle); color: var(--accent-primary); border-color: var(--accent-border);">
                        #{skill}
                      </span>
                    {/each}
                  {/if}
                </div>
              </div>
            </div>
          {/if}

          <!-- SUB-TAB 4: DECENTRALIZED CHANNELS -->
          {#if activePersonalSection === 'contact'}
            <div style="display: flex; flex-direction: column; gap: 14px;">
              <div style="padding: 10px 14px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-muted); line-height: 1.5;">
                <strong>Zero-Storage Communication:</strong> PeerMesh stores no chat logs or private messages. Connect directly with peers through sovereign, decentralized E2EE protocols.
              </div>

              <div class="work-form-grid">
                <div>
                  <label for="p-email" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Contact Email</label>
                  <div style="position: relative; display: flex; align-items: center;">
                    <span style="position: absolute; left: 10px; color: var(--text-dim);"><Mail size={14} /></span>
                    <input
                      id="p-email"
                      type="email"
                      placeholder="e.g. andrei.muresan@hosptim.ro"
                      bind:value={pEmailContact}
                      style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                    />
                  </div>
                </div>

                <div>
                  <label for="p-matrix" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Matrix Handle (E2EE Federated)</label>
                  <div style="position: relative; display: flex; align-items: center;">
                    <span style="position: absolute; left: 10px; color: var(--text-dim);"><AtSign size={14} /></span>
                    <input
                      id="p-matrix"
                      type="text"
                      placeholder="e.g. @andrei.muresan:matrix.org"
                      bind:value={pMatrixHandle}
                      style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                    />
                  </div>
                </div>
              </div>

              <div class="work-form-grid">
                <div>
                  <label for="p-simplex" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">SimpleX Chat Address (Zero User ID)</label>
                  <input
                    id="p-simplex"
                    type="text"
                    placeholder="simplex:https://simplex.chat/contact#/?v=2-4..."
                    bind:value={pSimplexAddress}
                    style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                  />
                </div>

                <div>
                  <label for="p-nostr" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Nostr Public Key (npub / NIP-04)</label>
                  <input
                    id="p-nostr"
                    type="text"
                    placeholder="npub1..."
                    bind:value={pNostrNpub}
                    style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                  />
                </div>
              </div>

              <div class="work-form-grid">
                <div>
                  <label for="p-session" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Session Onion ID</label>
                  <input
                    id="p-session"
                    type="text"
                    placeholder="05ee892348092384902384901284901284901284"
                    bind:value={pSessionId}
                    style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                  />
                </div>

                <div>
                  <label for="p-pgp" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">PGP / GPG Fingerprint</label>
                  <input
                    id="p-pgp"
                    type="text"
                    placeholder="40-hex PGP key fingerprint"
                    bind:value={pPgpFingerprint}
                    style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                  />
                </div>
              </div>
            </div>
          {/if}

          <!-- SUB-TAB 5: CODE PROVENANCE -->
          {#if activePersonalSection === 'github'}
            <div style="display: flex; flex-direction: column; gap: 14px;">
              <div style="padding: 10px 14px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 12px; color: var(--text-muted); line-height: 1.5;">
                <strong>Content Provenance & Git Syndication:</strong> Link your GitHub account and repository to verify publication commits, author identity, and decentralized content mirrors.
              </div>

              <div class="work-form-grid">
                <div>
                  <label for="p-gh-user" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">GitHub Username</label>
                  <div style="position: relative; display: flex; align-items: center;">
                    <span style="position: absolute; left: 10px; color: var(--text-dim);"><GitBranch size={14} /></span>
                    <input
                      id="p-gh-user"
                      type="text"
                      placeholder="e.g. dr-andrei-muresan"
                      bind:value={pGithubUsername}
                      style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                    />
                  </div>
                </div>

                <div>
                  <label for="p-gh-repo" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Content Repository</label>
                  <div style="position: relative; display: flex; align-items: center;">
                    <span style="position: absolute; left: 10px; color: var(--text-dim);"><FolderGit size={14} /></span>
                    <input
                      id="p-gh-repo"
                      type="text"
                      placeholder="e.g. peermesh-clinical-feed"
                      bind:value={pGithubContentRepo}
                      style="width: 100%; padding: 8px 10px 8px 30px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                    />
                  </div>
                </div>
              </div>
            </div>
          {/if}

          <!-- Bottom Save Button Bar -->
          <div style="display: flex; justify-content: flex-end; padding-top: 10px; border-top: 1px solid var(--border-subtle);">
            <button
              type="button"
              class="btn-primary"
              style="padding: 8px 20px; font-size: 13px; display: inline-flex; align-items: center; gap: 8px;"
              onclick={handleSavePersonalData}
              disabled={isSavingPersonal}
            >
              <Save size={15} />
              <span>{isSavingPersonal ? 'Saving Changes...' : 'Save Personal Data'}</span>
            </button>
          </div>
        </div>

      <!-- READ-ONLY VIEW (IF NOT OWN PROFILE) -->
      {:else}
        <div style="padding: 24px; max-height: calc(85vh - 140px); overflow-y: auto; display: flex; flex-direction: column; gap: 20px;">
          {#if profile.bannerUrl}
            <div style="width: 100%; height: 120px; border-radius: var(--radius-md); overflow: hidden; border: 1px solid var(--border-subtle);">
              <img src={profile.bannerUrl} alt="Banner" style="width: 100%; height: 100%; object-fit: cover;" />
            </div>
          {/if}

          <div style="display: flex; gap: 16px; align-items: center;">
            <img
              src={profile.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'}
              alt={profile.displayName}
              style="width: 72px; height: 72px; border-radius: 50%; object-fit: cover; border: 2px solid var(--accent-primary);"
            />
            <div>
              <h3 style="margin: 0; font-size: 18px; font-weight: 700; color: var(--text-main);">{profile.displayName || profile.handle}</h3>
              <div style="font-size: 13px; color: var(--text-dim); margin-top: 2px;">@{profile.handle}</div>
              {#if profile.headline}
                <div style="font-size: 13px; color: var(--text-muted); margin-top: 4px; font-weight: 500;">{profile.headline}</div>
              {/if}
              {#if profile.location}
                <div style="font-size: 12px; color: var(--text-dim); margin-top: 4px; display: flex; align-items: center; gap: 4px;">
                  <MapPin size={12} /> <span>{profile.location}</span>
                </div>
              {/if}
            </div>
          </div>

          {#if profile.bioMarkdown}
            <div style="padding: 16px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md);">
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; color: var(--text-dim); margin-bottom: 8px;">About</div>
              <div class="work-desc-markdown">
                {@html marked.parse(profile.bioMarkdown, { gfm: true, breaks: true })}
              </div>
            </div>
          {/if}

          {#if profile.skills && profile.skills.length > 0}
            <div>
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; color: var(--text-dim); margin-bottom: 8px;">Skills & Competencies</div>
              <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                {#each profile.skills as s}
                  <span class="search-skill-pill" style="font-size: 12px; padding: 3px 10px;">#{s}</span>
                {/each}
              </div>
            </div>
          {/if}

          <!-- Decentralized Contact Channels -->
          <div style="display: flex; gap: 10px; flex-wrap: wrap; padding-top: 10px; border-top: 1px solid var(--border-subtle);">
            {#if profile.emailContact}
              <a href="mailto:{profile.emailContact}" class="btn-subtle" style="font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 6px; text-decoration: none; color: inherit;">
                <Mail size={13} /> <span>{profile.emailContact}</span>
              </a>
            {/if}
            {#if profile.matrixHandle}
              <span class="brand-badge" style="font-size: 12px; padding: 6px 10px; display: inline-flex; align-items: center; gap: 5px;">
                <AtSign size={13} /> <span>Matrix: {profile.matrixHandle}</span>
              </span>
            {/if}
            {#if profile.nostrNpub}
              <span class="brand-badge" style="font-size: 12px; padding: 6px 10px; display: inline-flex; align-items: center; gap: 5px;">
                <Key size={13} /> <span>Nostr: {profile.nostrNpub.substring(0, 16)}...</span>
              </span>
            {/if}
            {#if profile.githubUsername}
              <a href="https://github.com/{profile.githubUsername}" target="_blank" rel="noopener noreferrer" class="btn-subtle" style="font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 6px; text-decoration: none; color: inherit;">
                <GitBranch size={13} /> <span>GitHub: {profile.githubUsername}</span>
              </a>
            {/if}
          </div>

          <div style="display: flex; justify-content: flex-end; margin-top: 10px;">
            <button class="btn-primary" onclick={() => mainTab = 'timeline'}>
              <Layers size={14} />
              <span>View Timeline</span>
            </button>
          </div>
        </div>
      {/if}

    <!-- TAB 2: TIMELINE -->
    {:else if mainTab === 'timeline'}
    <!-- Filter Pills on Top (Default: All) -->
    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding: 4px 20px; background: var(--bg-surface-elevated); flex-wrap: wrap; gap: 10px;">
      <div style="display: flex; gap: 6px; overflow-x: auto; padding: 6px 0;">
        <button
          class="highlight-filter-chip"
          class:active={activeFilter === 'all'}
          onclick={() => activeFilter = 'all'}
        >
          <Layers size={13} />
          <span>All Timeline</span>
          <span class="brand-badge" style="font-size: 10px; margin-left: 2px;">
            {highlights.length + experiences.length + educations.length}
          </span>
        </button>

        <button
          class="highlight-filter-chip"
          class:active={activeFilter === 'highlights'}
          onclick={() => activeFilter = 'highlights'}
        >
          <Sparkles size={13} />
          <span>Highlights</span>
          <span class="brand-badge" style="font-size: 10px; margin-left: 2px;">{highlights.length}</span>
        </button>

        <button
          class="highlight-filter-chip"
          class:active={activeFilter === 'experience'}
          onclick={() => activeFilter = 'experience'}
        >
          <Briefcase size={13} />
          <span>Career</span>
          <span class="brand-badge" style="font-size: 10px; margin-left: 2px;">{experiences.length}</span>
        </button>

        <button
          class="highlight-filter-chip"
          class:active={activeFilter === 'education'}
          onclick={() => activeFilter = 'education'}
        >
          <GraduationCap size={13} />
          <span>Education</span>
          <span class="brand-badge" style="font-size: 10px; margin-left: 2px;">{educations.length}</span>
        </button>
      </div>

      <!-- Single Action Button to Add / Edit Timeline Entries -->
      {#if isOwnProfile && !showForm}
        <button
          class="btn-primary"
          style="font-size: 12px; padding: 6px 14px; display: inline-flex; align-items: center; gap: 6px;"
          onclick={() => startAddEntry()}
        >
          <Plus size={14} />
          <span>Add to Timeline</span>
        </button>
      {/if}
    </div>

    <!-- Highlights Category Filter (Visible when viewing All or Highlights) -->
    {#if activeFilter === 'all' || activeFilter === 'highlights'}
      <div style="display: flex; align-items: center; gap: 6px; padding: 6px 20px; background: var(--bg-surface); border-bottom: 1px solid var(--border-subtle); overflow-x: auto;">
        <span style="font-size: 11px; font-weight: 600; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.5px;">Highlights Filter:</span>
        <button
          class="highlight-filter-chip"
          style="font-size: 10px; padding: 2px 8px;"
          class:active={highlightTypeFilter === 'ALL'}
          onclick={() => highlightTypeFilter = 'ALL'}
        >
          All Highlights ({highlights.length})
        </button>
        <button
          class="highlight-filter-chip"
          style="font-size: 10px; padding: 2px 8px;"
          class:active={highlightTypeFilter === 'ACHIEVEMENT'}
          onclick={() => highlightTypeFilter = 'ACHIEVEMENT'}
        >
          <Trophy size={11} /> Achievements
        </button>
        <button
          class="highlight-filter-chip"
          style="font-size: 10px; padding: 2px 8px;"
          class:active={highlightTypeFilter === 'PATENT'}
          onclick={() => highlightTypeFilter = 'PATENT'}
        >
          <ScrollText size={11} /> Patents
        </button>
        <button
          class="highlight-filter-chip"
          style="font-size: 10px; padding: 2px 8px;"
          class:active={highlightTypeFilter === 'PRIZE'}
          onclick={() => highlightTypeFilter = 'PRIZE'}
        >
          <Medal size={11} /> Prizes
        </button>
        <button
          class="highlight-filter-chip"
          style="font-size: 10px; padding: 2px 8px;"
          class:active={highlightTypeFilter === 'BOOK'}
          onclick={() => highlightTypeFilter = 'BOOK'}
        >
          <BookOpen size={11} /> Books
        </button>
        <button
          class="highlight-filter-chip"
          style="font-size: 10px; padding: 2px 8px;"
          class:active={highlightTypeFilter === 'CONFERENCE'}
          onclick={() => highlightTypeFilter = 'CONFERENCE'}
        >
          <Mic size={11} /> Conferences
        </button>
      </div>
    {/if}

    <div style="padding: 20px; display: flex; flex-direction: column; gap: 18px; max-height: 72vh; overflow-y: auto;">

      <!-- Unified Add / Edit Form -->
      {#if showForm}
        <div style="padding: 18px; background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: 14px;">
          
          <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding-bottom: 10px;">
            <strong style="font-size: 14px; display: flex; align-items: center; gap: 6px; color: var(--text-main);">
              <Pencil size={15} color="var(--accent-primary)" />
              {editingId ? 'Edit Timeline Entry' : 'Add New Entry to Timeline'}
            </strong>
            <button class="btn-icon" onclick={() => showForm = false} style="font-size: 12px; display: flex; align-items: center; gap: 4px;">
              <X size={14} /> Cancel
            </button>
          </div>

          <!-- Entry Type Switcher -->
          {#if !editingId}
            <div>
              <span style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 6px;">Select Timeline Category:</span>
              <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                <button
                  type="button"
                  class="btn-secondary"
                  style="font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 6px; {formCategory === 'HIGHLIGHT' ? 'border-color: var(--accent-primary); background: var(--accent-subtle); color: var(--accent-primary); font-weight: 700;' : ''}"
                  onclick={() => formCategory = 'HIGHLIGHT'}
                >
                  <Sparkles size={14} />
                  <span>Highlight (Patent, Prize, Book, Conference, Achievement)</span>
                </button>
                <button
                  type="button"
                  class="btn-secondary"
                  style="font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 6px; {formCategory === 'EXPERIENCE' ? 'border-color: var(--accent-primary); background: var(--accent-subtle); color: var(--accent-primary); font-weight: 700;' : ''}"
                  onclick={() => formCategory = 'EXPERIENCE'}
                >
                  <Briefcase size={14} />
                  <span>Career / Work Experience</span>
                </button>
                <button
                  type="button"
                  class="btn-secondary"
                  style="font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 6px; {formCategory === 'EDUCATION' ? 'border-color: var(--accent-primary); background: var(--accent-subtle); color: var(--accent-primary); font-weight: 700;' : ''}"
                  onclick={() => formCategory = 'EDUCATION'}
                >
                  <GraduationCap size={14} />
                  <span>Academic Education & Degrees</span>
                </button>
              </div>
            </div>
          {/if}

          <!-- Form Fields for Highlight -->
          {#if formCategory === 'HIGHLIGHT'}
            <div class="work-form-grid">
              <div>
                <label for="hl-form-type" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Highlight Type *</label>
                <select
                  id="hl-form-type"
                  bind:value={hlType}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                >
                  <option value="ACHIEVEMENT">🏆 Achievement / Landmark Milestone</option>
                  <option value="PATENT">📜 Patent (Registered or Pending)</option>
                  <option value="PRIZE">🥇 Prize / Distinction / Medal</option>
                  <option value="BOOK">📚 Book / Author / Publication</option>
                  <option value="CONFERENCE">🎤 Conference / Keynote / Speaker</option>
                </select>
              </div>

              <div>
                <label for="hl-form-date" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Year or Date (e.g. 2024, 2023-11) *</label>
                <input
                  id="hl-form-date"
                  type="text"
                  placeholder="e.g. 2024"
                  bind:value={hlDate}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div class="work-form-grid">
              <div>
                <label for="hl-form-title" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Title of Highlight *</label>
                <input
                  id="hl-form-title"
                  type="text"
                  placeholder="e.g. Patent EP3928192, ESC Congress Keynote, Author of..."
                  bind:value={hlTitle}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>

              <div>
                <label for="hl-form-issuer" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Issuer / Venue / Publisher</label>
                <input
                  id="hl-form-issuer"
                  type="text"
                  placeholder="e.g. European Patent Office, Springer, World Stroke Congress"
                  bind:value={hlIssuer}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div>
              <label for="hl-form-url" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">External Link (URL to Patent, Book, DOI, Certificate)</label>
              <input
                id="hl-form-url"
                type="url"
                placeholder="https://..."
                bind:value={hlUrl}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>

            <div>
              <label for="hl-form-desc" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Description & Details (Markdown Enabled)</label>
              <textarea
                id="hl-form-desc"
                placeholder="Describe key findings, patent claim, abstract, or citation..."
                bind:value={hlDesc}
                rows="3"
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main); resize: vertical;"
              ></textarea>
            </div>

          <!-- Form Fields for Experience -->
          {:else if formCategory === 'EXPERIENCE'}
            <div class="work-form-grid">
              <div>
                <label for="exp-form-title" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Role / Title *</label>
                <input
                  id="exp-form-title"
                  type="text"
                  placeholder="e.g. Chief of Cardiology, Senior Engineer"
                  bind:value={expTitle}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>

              <div>
                <label for="exp-form-company" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Company or Hospital Name *</label>
                <input
                  id="exp-form-company"
                  type="text"
                  placeholder="e.g. Timișoara County Emergency Clinical Hospital"
                  bind:value={expCompany}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div class="work-form-grid">
              <div>
                <label for="exp-form-org" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Linked Organization on Network (Optional)</label>
                <select
                  id="exp-form-org"
                  bind:value={expCompanyId}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                >
                  <option value="">-- None / Independent --</option>
                  {#each companies as comp}
                    <option value={comp.id}>🏢 {comp.name} (@{comp.handle})</option>
                  {/each}
                </select>
              </div>

              <div>
                <label for="exp-form-loc" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Location (City, Country)</label>
                <input
                  id="exp-form-loc"
                  type="text"
                  placeholder="e.g. Timișoara, Romania"
                  bind:value={expLocation}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div class="work-form-grid">
              <div>
                <label for="exp-form-start" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Start Date (YYYY-MM or YYYY) *</label>
                <input
                  id="exp-form-start"
                  type="text"
                  placeholder="e.g. 2018 or 2018-03"
                  bind:value={expStartDate}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>

              <div>
                <label for="exp-form-end" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">End Date (YYYY-MM or YYYY)</label>
                <input
                  id="exp-form-end"
                  type="text"
                  placeholder="e.g. 2022 or leave blank if current"
                  disabled={expIsCurrent}
                  bind:value={expEndDate}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main); opacity: {expIsCurrent ? '0.5' : '1'};"
                />
              </div>
            </div>

            <div style="display: flex; align-items: center; gap: 8px;">
              <input type="checkbox" id="expIsCurrent" bind:checked={expIsCurrent} />
              <label for="expIsCurrent" style="font-size: 13px; color: var(--text-main); cursor: pointer;">
                I currently hold this role
              </label>
            </div>

            <div>
              <label for="exp-form-desc" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Role Responsibilities & Milestones (Markdown Enabled)</label>
              <textarea
                id="exp-form-desc"
                placeholder="Describe procedures, architecture, leadership responsibilities..."
                bind:value={expDesc}
                rows="3"
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main); resize: vertical;"
              ></textarea>
            </div>

            <div>
              <label for="exp-form-skills" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Skills (comma-separated)</label>
              <input
                id="exp-form-skills"
                type="text"
                placeholder="e.g. Cardiology, Emergency Triage, Catheterization"
                bind:value={expSkills}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>

          <!-- Form Fields for Education -->
          {:else if formCategory === 'EDUCATION'}
            <div class="work-form-grid">
              <div>
                <label for="edu-form-inst" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Institution / University *</label>
                <input
                  id="edu-form-inst"
                  type="text"
                  placeholder="e.g. Victor Babeș University of Medicine and Pharmacy"
                  bind:value={eduInst}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>

              <div>
                <label for="edu-form-deg" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Degree *</label>
                <input
                  id="edu-form-deg"
                  type="text"
                  placeholder="e.g. Doctor of Medicine (M.D.), Specialist Diploma"
                  bind:value={eduDegree}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div class="work-form-grid">
              <div>
                <label for="edu-form-field" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Field of Study / Specialization</label>
                <input
                  id="edu-form-field"
                  type="text"
                  placeholder="e.g. Clinical Cardiology & Critical Emergency Care"
                  bind:value={eduField}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>

              <div>
                <label for="edu-form-grade" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Grade / Honors (Optional)</label>
                <input
                  id="edu-form-grade"
                  type="text"
                  placeholder="e.g. Magna Cum Laude, Board Top 1%"
                  bind:value={eduGrade}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>
            </div>

            <div class="work-form-grid">
              <div>
                <label for="edu-form-start" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Start Year *</label>
                <input
                  id="edu-form-start"
                  type="text"
                  placeholder="e.g. 2004"
                  bind:value={eduStartDate}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
                />
              </div>

              <div>
                <label for="edu-form-end" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">End / Graduation Year</label>
                <input
                  id="edu-form-end"
                  type="text"
                  placeholder="e.g. 2010 or ongoing"
                  disabled={eduIsCurrent}
                  bind:value={eduEndDate}
                  style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main); opacity: {eduIsCurrent ? '0.5' : '1'};"
                />
              </div>
            </div>

            <div style="display: flex; align-items: center; gap: 8px;">
              <input type="checkbox" id="eduIsCurrent" bind:checked={eduIsCurrent} />
              <label for="eduIsCurrent" style="font-size: 13px; color: var(--text-main); cursor: pointer;">
                Currently studying / ongoing fellowship
              </label>
            </div>

            <div>
              <label for="edu-form-act" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Research, Clinical Rotations & Activities (Markdown Enabled)</label>
              <textarea
                id="edu-form-act"
                placeholder="Describe thesis research, clinical rotations, student societies..."
                bind:value={eduActivities}
                rows="3"
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main); resize: vertical;"
              ></textarea>
            </div>

            <div>
              <label for="edu-form-skills" style="font-size: 12px; font-weight: 600; color: var(--text-dim); display: block; margin-bottom: 4px;">Competencies / Skills (comma-separated)</label>
              <input
                id="edu-form-skills"
                type="text"
                placeholder="e.g. Cardiology, Internal Medicine, Clinical Triage"
                bind:value={eduSkills}
                style="width: 100%; padding: 8px 10px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 13px; color: var(--text-main);"
              />
            </div>
          {/if}

          <!-- Form Buttons -->
          <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 6px;">
            <button class="btn-secondary" onclick={() => showForm = false} disabled={isSaving}>
              Cancel
            </button>
            <button class="btn-primary" onclick={handleSave} disabled={isSaving}>
              {isSaving ? 'Saving...' : (editingId ? 'Update Entry' : 'Save to Timeline')}
            </button>
          </div>
        </div>
      {/if}

      <!-- Main Timeline View -->
      {#if loading}
        <div style="text-align: center; color: var(--text-muted); padding: 40px;">
          Loading career timeline, verified highlights, and qualifications...
        </div>
      {:else if activeFilter === 'all'}
        <!-- Temporally Synchronized Symmetrical Timeline (Highlights on Left, Year Badge on Center Spine, Career & Education on Right) -->
        <div class="temporal-timeline-wrapper">
          
          <!-- Column Headers -->
          <div style="display: grid; grid-template-columns: 1fr 64px 1fr; column-gap: 20px; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid var(--border-subtle); align-items: center;">
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span class="dual-timeline-lane-title">
                <Sparkles size={14} color="var(--accent-primary)" />
                Highlights ({filteredHighlights.length})
              </span>
              <span style="font-size: 11px; color: var(--text-dim);">Patents, Prizes, Books, Talks</span>
            </div>

            <div style="text-align: center; font-size: 10px; font-weight: 700; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.5px;">
              Year
            </div>

            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span class="dual-timeline-lane-title">
                <Briefcase size={14} color="var(--accent-primary)" />
                Career & Education ({combinedCareerEdu.length})
              </span>
              <span style="font-size: 11px; color: var(--text-dim);">Roles & Qualifications</span>
            </div>
          </div>

          {#if allTimelineYears.length === 0}
            <div style="padding: 32px; text-align: center; color: var(--text-dim); font-size: 13px; background: var(--bg-surface-elevated); border-radius: var(--radius-md);">
              No timeline items recorded yet.
            </div>
          {:else}
            <div class="temporal-timeline-grid">
              <!-- Left Highlights and Center Spine Year Badges for each year -->
              {#each allTimelineYears as year, rowIndex (year)}
                <!-- LEFT SLOT: HIGHLIGHTS FOR THIS YEAR -->
                <div class="temporal-left-slot" style="grid-column: 1; grid-row: {rowIndex + 1};">
                  {#if highlightsByYear.has(year) && highlightsByYear.get(year).length > 0}
                    {#each highlightsByYear.get(year) as hl (hl.id)}
                      <div class="timeline-card-left">
                        <!-- Central Spine Dot -->
                        <div class="spine-dot">
                          {#if hl.type === 'PATENT'}
                            <ScrollText size={10} />
                          {:else if hl.type === 'PRIZE'}
                            <Medal size={10} />
                          {:else if hl.type === 'BOOK'}
                            <BookOpen size={10} />
                          {:else if hl.type === 'CONFERENCE'}
                            <Mic size={10} />
                          {:else}
                            <Trophy size={10} />
                          {/if}
                        </div>

                        <!-- Card Header -->
                        <div class="work-role-header">
                          <div style="flex: 1; min-width: 180px;">
                            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                              <span class="work-role-title">
                                {hl.title}
                              </span>

                              {#if hl.type === 'PATENT'}
                                <span class="highlight-type-badge patent"><ScrollText size={11} /> Patent</span>
                              {:else if hl.type === 'PRIZE'}
                                <span class="highlight-type-badge prize"><Medal size={11} /> Prize</span>
                              {:else if hl.type === 'BOOK'}
                                <span class="highlight-type-badge book"><BookOpen size={11} /> Book</span>
                              {:else if hl.type === 'CONFERENCE'}
                                <span class="highlight-type-badge conference"><Mic size={11} /> Conference</span>
                              {:else}
                                <span class="highlight-type-badge achievement"><Trophy size={11} /> Achievement</span>
                              {/if}
                            </div>

                            {#if hl.issuerOrVenue}
                              <div class="timeline-meta-line">
                                <span class="work-company-name">
                                  <Award size={13} />
                                  <span>{hl.issuerOrVenue}</span>
                                </span>
                              </div>
                            {/if}
                          </div>

                          <!-- Date & Actions -->
                          <div style="display: flex; align-items: center; gap: 6px; flex-shrink: 0;">
                            <span class="work-date-badge">
                              <Calendar size={12} />
                              <span>{hl.dateOrYear}</span>
                            </span>

                            {#if isOwnProfile}
                              <button class="btn-icon" style="padding: 4px;" onclick={() => editHighlight(hl)} title="Edit highlight">
                                <Pencil size={13} />
                              </button>
                              <button class="btn-icon" style="padding: 4px; color: var(--accent-danger);" onclick={() => handleDelete('HIGHLIGHT', hl.id)} title="Delete highlight">
                                <Trash size={13} />
                              </button>
                            {/if}
                          </div>
                        </div>

                        <!-- Markdown Description -->
                        {#if hl.descriptionMarkdown}
                          <div class="work-desc-markdown">
                            {@html marked.parse(hl.descriptionMarkdown, { gfm: true, breaks: true })}
                          </div>
                        {/if}

                        <!-- Reference Link if present -->
                        {#if hl.externalUrl}
                          <div style="margin-top: 4px;">
                            <a
                              href={hl.externalUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              style="font-size: 11px; color: var(--accent-primary); display: inline-flex; align-items: center; gap: 4px; text-decoration: none;"
                            >
                              <span>View Publication / Patent</span>
                              <ExternalLink size={11} />
                            </a>
                          </div>
                        {/if}
                      </div>
                    {/each}
                  {:else}
                    <div style="min-height: 20px;"></div>
                  {/if}
                </div>

                <!-- CENTER SPINE COLUMN: YEAR BADGE -->
                <div class="temporal-spine-col" style="grid-column: 2; grid-row: {rowIndex + 1};">
                  <div class="temporal-year-badge">
                    {year}
                  </div>
                </div>
              {/each}

              <!-- RIGHT SLOT: SPANNING CAREER & EDUCATION CARDS -->
              {#each spanningRightItems as item (item.id)}
                <div
                  class="timeline-card-right timeline-span-card"
                  class:education={item.itemType === 'education'}
                  class:current={item.isCurrent}
                  class:spanning={item.isSpanning}
                  style="grid-column: 3; grid-row: {item.topRow} / {item.bottomRow + 1}; {getLaneStyle(item)}"
                >
                  <!-- Central Spine Dot at Top of Box -->
                  <div class="spine-dot" class:current={item.isCurrent}>
                    {#if item.itemType === 'experience'}
                      <Briefcase size={10} />
                    {:else}
                      <GraduationCap size={10} />
                    {/if}
                  </div>

                  <!-- TOP ANCHOR: Status / Era End -->
                  <div class="span-card-anchor-top">
                    <span class="span-anchor-badge" class:current={item.isCurrent}>
                      {#if item.isCurrent}
                        <span class="pulsing-beacon"></span>
                        <span><strong>Present</strong> • Ongoing</span>
                      {:else}
                        <span style="width: 6px; height: 6px; border-radius: 50%; background: var(--text-dim);"></span>
                        <span><strong>{item.topYear}</strong> • Concluded</span>
                      {/if}
                    </span>

                    {#if item.durationText}
                      <span class="span-duration-pill">
                        {item.durationText}
                      </span>
                    {/if}
                  </div>

                  <!-- CARD MAIN CONTENT BODY -->
                  <div style="flex: 1; display: flex; flex-direction: column; gap: 6px;">
                    <div class="work-role-header">
                      <div style="flex: 1; min-width: 140px;">
                        <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                          <span class="work-role-title">
                            {item.itemType === 'experience' ? item.title : item.degree}
                          </span>

                          <span class="timeline-type-pill" class:edu={item.itemType === 'education'}>
                            {#if item.itemType === 'experience'}
                              <Briefcase size={11} /> Role
                            {:else}
                              <GraduationCap size={11} /> Degree
                            {/if}
                          </span>

                          {#if item.itemType === 'education' && item.grade}
                            <span class="brand-badge" style="font-size: 11px; display: inline-flex; align-items: center; gap: 4px;">
                              <Award size={11} /> {item.grade}
                            </span>
                          {/if}
                        </div>

                        <div class="timeline-meta-line">
                          <span class="work-company-name">
                            <Building size={13} />
                            <span>{item.itemType === 'experience' ? item.companyName : item.institutionName}</span>
                          </span>

                          {#if item.itemType === 'experience' && item.location}
                            <span class="timeline-meta-sep">•</span>
                            <span class="timeline-meta-sub">
                              <MapPin size={12} /> {item.location}
                            </span>
                          {/if}

                          {#if item.itemType === 'education' && item.fieldOfStudy}
                            <span class="timeline-meta-sep">•</span>
                            <span class="timeline-meta-sub">
                              <BookOpen size={12} /> {item.fieldOfStudy}
                            </span>
                          {/if}
                        </div>
                      </div>

                      <!-- Edit/Delete Actions -->
                      {#if isOwnProfile}
                        <div style="display: flex; align-items: center; gap: 4px; flex-shrink: 0;">
                          <button
                            class="btn-icon"
                            style="padding: 4px;"
                            onclick={() => item.itemType === 'experience' ? editExperience(item) : editEducation(item)}
                            title="Edit entry"
                          >
                            <Pencil size={13} />
                          </button>
                          <button
                            class="btn-icon"
                            style="padding: 4px; color: var(--accent-danger);"
                            onclick={() => handleDelete(item.itemType === 'experience' ? 'EXPERIENCE' : 'EDUCATION', item.id)}
                            title="Delete entry"
                          >
                            <Trash size={13} />
                          </button>
                        </div>
                      {/if}
                    </div>

                    <!-- Markdown Details -->
                    {#if item.descriptionMarkdown || item.activitiesMarkdown}
                      <div class="work-desc-markdown">
                        {@html marked.parse(item.descriptionMarkdown || item.activitiesMarkdown, { gfm: true, breaks: true })}
                      </div>
                    {/if}

                    <!-- Skills tags -->
                    {#if item.skills && item.skills.length > 0}
                      <div style="display: flex; gap: 5px; flex-wrap: wrap; margin-top: 4px;">
                        {#each item.skills as s}
                          <span class="search-skill-pill" style="font-size: 10px; padding: 1px 6px;">#{s}</span>
                        {/each}
                      </div>
                    {/if}
                  </div>

                  <!-- BOTTOM ANCHOR: For Multi-Year Spanning Cards -->
                  {#if item.isSpanning}
                    <div class="span-card-anchor-bottom">
                      <span class="span-anchor-badge">
                        <Calendar size={11} />
                        <span>Started <strong>{item.startDate}</strong></span>
                      </span>

                      <span style="font-size: 10px; color: var(--text-dim); text-transform: uppercase;">
                        {item.bottomYear}
                      </span>
                    </div>
                  {:else}
                    <div style="margin-top: 6px; font-size: 11px; color: var(--text-dim); display: flex; align-items: center; gap: 4px;">
                      <Calendar size={11} />
                      <span>{item.startDate}{item.endDate ? ` - ${item.endDate}` : ''}</span>
                    </div>
                  {/if}
                </div>
              {/each}
            </div>
          {/if}

        </div>

      {:else if activeFilter === 'highlights'}
        <!-- Filtered Highlights View -->
        <div class="timeline-single-lane">
          {#if filteredHighlights.length === 0}
            <div style="text-align: center; color: var(--text-dim); padding: 40px; background: var(--bg-surface-elevated); border-radius: var(--radius-md);">
              <Sparkles size={32} style="margin-bottom: 8px; color: var(--accent-primary);" />
              <p style="margin: 0; font-size: 14px; font-weight: 500;">
                No highlights recorded in this filter.
              </p>
            </div>
          {:else}
            {#each filteredHighlights as hl (hl.id)}
              <div class="timeline-card-left" style="margin-left: 0;">
                <div class="spine-dot">
                  {#if hl.type === 'PATENT'}
                    <ScrollText size={10} />
                  {:else if hl.type === 'PRIZE'}
                    <Medal size={10} />
                  {:else if hl.type === 'BOOK'}
                    <BookOpen size={10} />
                  {:else if hl.type === 'CONFERENCE'}
                    <Mic size={10} />
                  {:else}
                    <Trophy size={10} />
                  {/if}
                </div>

                <div class="work-role-header">
                  <div style="flex: 1; min-width: 200px;">
                    <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                      <span class="work-role-title">{hl.title}</span>
                      {#if hl.type === 'PATENT'}
                        <span class="highlight-type-badge patent"><ScrollText size={11} /> Patent</span>
                      {:else if hl.type === 'PRIZE'}
                        <span class="highlight-type-badge prize"><Medal size={11} /> Prize</span>
                      {:else if hl.type === 'BOOK'}
                        <span class="highlight-type-badge book"><BookOpen size={11} /> Book</span>
                      {:else if hl.type === 'CONFERENCE'}
                        <span class="highlight-type-badge conference"><Mic size={11} /> Conference</span>
                      {:else}
                        <span class="highlight-type-badge achievement"><Trophy size={11} /> Achievement</span>
                      {/if}
                    </div>

                    {#if hl.issuerOrVenue}
                      <div class="timeline-meta-line">
                        <span class="work-company-name">
                          <Award size={13} />
                          <span>{hl.issuerOrVenue}</span>
                        </span>
                      </div>
                    {/if}
                  </div>

                  <div style="display: flex; align-items: center; gap: 6px; flex-shrink: 0;">
                    <span class="work-date-badge">
                      <Calendar size={12} />
                      <span>{hl.dateOrYear}</span>
                    </span>

                    {#if isOwnProfile}
                      <button class="btn-icon" style="padding: 4px;" onclick={() => editHighlight(hl)} title="Edit highlight">
                        <Pencil size={13} />
                      </button>
                      <button class="btn-icon" style="padding: 4px; color: var(--accent-danger);" onclick={() => handleDelete('HIGHLIGHT', hl.id)} title="Delete highlight">
                        <Trash size={13} />
                      </button>
                    {/if}
                  </div>
                </div>

                {#if hl.descriptionMarkdown}
                  <div class="work-desc-markdown">
                    {@html marked.parse(hl.descriptionMarkdown, { gfm: true, breaks: true })}
                  </div>
                {/if}

                {#if hl.externalUrl}
                  <div style="margin-top: 4px;">
                    <a
                      href={hl.externalUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      style="font-size: 11px; color: var(--accent-primary); display: inline-flex; align-items: center; gap: 4px; text-decoration: none;"
                    >
                      <span>View Reference</span>
                      <ExternalLink size={11} />
                    </a>
                  </div>
                {/if}
              </div>
            {/each}
          {/if}
        </div>

      {:else if activeFilter === 'experience'}
        <!-- Filtered Work Experience View -->
        <div class="timeline-single-lane">
          {#if experiences.length === 0}
            <div style="text-align: center; color: var(--text-dim); padding: 40px; background: var(--bg-surface-elevated); border-radius: var(--radius-md);">
              <Briefcase size={32} style="margin-bottom: 8px; color: var(--accent-primary);" />
              <p style="margin: 0; font-size: 14px; font-weight: 500;">
                No career work experiences recorded yet.
              </p>
            </div>
          {:else}
            {#each experiences as exp (exp.id)}
              <div class="timeline-card-right" style="margin-left: 0;">
                <div class="spine-dot" class:current={exp.isCurrent}>
                  <Briefcase size={10} />
                </div>

                <div class="work-role-header">
                  <div style="flex: 1; min-width: 200px;">
                    <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                      <span class="work-role-title">{exp.title}</span>
                      <span class="timeline-type-pill"><Briefcase size={11} /> Role</span>
                    </div>

                    <div class="timeline-meta-line">
                      <span class="work-company-name">
                        <Building size={13} />
                        <span>{exp.companyName}</span>
                      </span>

                      {#if exp.location}
                        <span class="timeline-meta-sep">•</span>
                        <span class="timeline-meta-sub">
                          <MapPin size={12} /> {exp.location}
                        </span>
                      {/if}
                    </div>
                  </div>

                  <div style="display: flex; align-items: center; gap: 6px; flex-shrink: 0;">
                    <span class="work-date-badge" class:current={exp.isCurrent}>
                      <Calendar size={12} />
                      {#if exp.isCurrent}
                        <span style="width: 6px; height: 6px; border-radius: 50%; background: var(--accent-success);"></span>
                        <span>{exp.startDate} - Present</span>
                      {:else}
                        <span>{exp.startDate} - {exp.endDate || 'N/A'}</span>
                      {/if}
                    </span>

                    {#if isOwnProfile}
                      <button class="btn-icon" style="padding: 4px;" onclick={() => editExperience(exp)} title="Edit entry">
                        <Pencil size={13} />
                      </button>
                      <button class="btn-icon" style="padding: 4px; color: var(--accent-danger);" onclick={() => handleDelete('EXPERIENCE', exp.id)} title="Delete entry">
                        <Trash size={13} />
                      </button>
                    {/if}
                  </div>
                </div>

                {#if exp.descriptionMarkdown}
                  <div class="work-desc-markdown">
                    {@html marked.parse(exp.descriptionMarkdown, { gfm: true, breaks: true })}
                  </div>
                {/if}

                {#if exp.skills && exp.skills.length > 0}
                  <div style="display: flex; gap: 6px; flex-wrap: wrap; margin-top: 4px;">
                    {#each exp.skills as s}
                      <span class="search-skill-pill" style="font-size: 11px; padding: 2px 8px;">#{s}</span>
                    {/each}
                  </div>
                {/if}
              </div>
            {/each}
          {/if}
        </div>

      {:else if activeFilter === 'education'}
        <!-- Filtered Education View -->
        <div class="timeline-single-lane">
          {#if educations.length === 0}
            <div style="text-align: center; color: var(--text-dim); padding: 40px; background: var(--bg-surface-elevated); border-radius: var(--radius-md);">
              <GraduationCap size={32} style="margin-bottom: 8px; color: var(--accent-primary);" />
              <p style="margin: 0; font-size: 14px; font-weight: 500;">
                No academic education records yet.
              </p>
            </div>
          {:else}
            {#each educations as edu (edu.id)}
              <div class="timeline-card-right" style="margin-left: 0;">
                <div class="spine-dot" class:current={edu.isCurrent}>
                  <GraduationCap size={10} />
                </div>

                <div class="work-role-header">
                  <div style="flex: 1; min-width: 200px;">
                    <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                      <span class="work-role-title">{edu.degree}</span>
                      <span class="timeline-type-pill edu"><GraduationCap size={11} /> Degree</span>
                      {#if edu.grade}
                        <span class="brand-badge" style="font-size: 11px; display: inline-flex; align-items: center; gap: 4px;">
                          <Award size={11} /> {edu.grade}
                        </span>
                      {/if}
                    </div>

                    <div class="timeline-meta-line">
                      <span class="work-company-name">
                        <Building size={13} />
                        <span>{edu.institutionName}</span>
                      </span>

                      {#if edu.fieldOfStudy}
                        <span class="timeline-meta-sep">•</span>
                        <span class="timeline-meta-sub">
                          <BookOpen size={12} /> {edu.fieldOfStudy}
                        </span>
                      {/if}
                    </div>
                  </div>

                  <div style="display: flex; align-items: center; gap: 6px; flex-shrink: 0;">
                    <span class="work-date-badge" class:current={edu.isCurrent}>
                      <Calendar size={12} />
                      {#if edu.isCurrent}
                        <span style="width: 6px; height: 6px; border-radius: 50%; background: var(--accent-success);"></span>
                        <span>{edu.startDate} - Present</span>
                      {:else}
                        <span>{edu.startDate} - {edu.endDate || 'N/A'}</span>
                      {/if}
                    </span>

                    {#if isOwnProfile}
                      <button class="btn-icon" style="padding: 4px;" onclick={() => editEducation(edu)} title="Edit entry">
                        <Pencil size={13} />
                      </button>
                      <button class="btn-icon" style="padding: 4px; color: var(--accent-danger);" onclick={() => handleDelete('EDUCATION', edu.id)} title="Delete entry">
                        <Trash size={13} />
                      </button>
                    {/if}
                  </div>
                </div>

                {#if edu.activitiesMarkdown}
                  <div class="work-desc-markdown">
                    {@html marked.parse(edu.activitiesMarkdown, { gfm: true, breaks: true })}
                  </div>
                {/if}

                {#if edu.skills && edu.skills.length > 0}
                  <div style="display: flex; gap: 6px; flex-wrap: wrap; margin-top: 4px;">
                    {#each edu.skills as s}
                      <span class="search-skill-pill" style="font-size: 11px; padding: 2px 8px;">#{s}</span>
                    {/each}
                  </div>
                {/if}
              </div>
            {/each}
          {/if}
        </div>
      {/if}

    </div>
    {/if}
  </div>
</div>

<style>
  .timeline-meta-line {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
    margin-top: 4px;
    font-size: 13px;
  }

  .timeline-meta-sep {
    color: var(--text-dim);
    font-size: 11px;
  }

  .timeline-meta-sub {
    color: var(--text-dim);
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
  }

  .timeline-type-pill {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 10px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    padding: 1px 6px;
    border-radius: var(--radius-xs);
    background: var(--accent-subtle);
    color: var(--accent-primary);
    border: 1px solid var(--accent-border);
  }

  .timeline-type-pill.edu {
    background: rgba(147, 51, 234, 0.08);
    color: #9333ea;
    border-color: rgba(147, 51, 234, 0.25);
  }
</style>
