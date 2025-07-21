/* ---------- 小工具 ---------- */
const $      = (s, p = document) => p.querySelector(s);
const isNull = v => v === null || v === undefined;

/* ---------- 檢查登入 ---------- */
function getCurrentUser() {
    try { return JSON.parse(localStorage.getItem('memberInfo')) || {}; }
    catch { return {}; }
}
function requireLogin() {
    if (isNull(getCurrentUser().id)) {
        alert('請先登入後再操作');
        return false;
    }
    return true;
}

/* ---------- 載入整頁 ---------- */
async function loadPage(id) {
    /* 文章 ------------------------------------------------ */
    const news = await fetch(`/api/News/${id}`).then(r => r.json());
    const box  = $('#news-box');
    document.title = news.newsTit;
    box.innerHTML  = `
      <h1 class="news-title text-break">${news.newsTit}</h1>
      <div class="news-meta">
        發佈：${new Date(news.newsCrdate).toLocaleString()}
        ${news.newsUpdate ? ` · 更新：${new Date(news.newsUpdate).toLocaleString()}` : ''}
      </div>
      <div class="tags"></div>`;

    box.append(renderPlatformTags(news.categoryTags || []));

    /* 內文 ------------------------------------------------ */
    const art = document.createElement('div');
    art.className = 'news-content';
    art.innerHTML = news.newsCon;          // 允許 HTML
    box.append(art);

    // 內文圖片 → 燈箱
    const imgs  = art.querySelectorAll('img');
    const modalContainer = $('#newsLightboxes') ||
        document.body.appendChild(Object.assign(document.createElement('div'), { id:'newsLightboxes' }));

    imgs.forEach((img, idx) => {
        const modalId = `htmlImgModal-${idx}`;
        const modal   = document.createElement('div');
        modal.className = 'modal fade';
        modal.id        = modalId;
        modal.tabIndex  = -1;
        modal.setAttribute('aria-hidden', 'true');
        modal.innerHTML = `
          <div class="modal-dialog modal-dialog-centered modal-xl">
            <div class="modal-content bg-transparent border-0">
              <img src="${img.src}" class="w-100 rounded-3" alt="news image">
            </div>
          </div>`;
        modalContainer.appendChild(modal);

        const wrapper = document.createElement('a');
        wrapper.href = 'javascript:void(0)';
        wrapper.dataset.bsToggle = 'modal';
        wrapper.dataset.bsTarget = `#${modalId}`;
        img.parentNode?.insertBefore(wrapper, img);
        wrapper.appendChild(img);
    });

    /* 留言 ------------------------------------------------ */
    reloadCommentArea(id);           // 會自動把輸入框跟留言塞進 box
}

/* ---------- 留言卡片 ---------- */
function makeCommentCard(c) {
    const card = document.createElement('div');
    card.className = 'border rounded p-3 mb-4 shadow-sm';
    card.dataset.commentId = c.id;
    card.innerHTML = `
      <div class="d-flex justify-content-between">
        <div class="d-flex gap-3">
          <img src="/images/memberAvatar/mem${c.memNoMemNo}.png"
               onerror="this.src='/images/memberAvatar/defaultmem.png'"
               class="rounded-circle user-avatar" width="50" height="50" alt="User">

          <div>
            <h6 class="fw-bold text-dark mb-1">${c.memNoMemNickName || '匿名'}</h6>
            <p class="mb-2 text-dark">${c.ncomCon}</p>
            <div class="d-flex gap-3">
              <button class="btn btn-outline-success btn-sm rounded-pill up-btn">
                <i class="bi bi-hand-thumbs-up"></i> <span class="up-count">${c.ncomLikeLc}</span>
              </button>
              <button class="btn btn-outline-danger btn-sm rounded-pill down-btn">
                <i class="bi bi-hand-thumbs-down"></i> <span class="down-count">${c.ncomLikeDlc}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- ︙ 按鈕與選單 -->
        <div class="dropdown">
          <button class="btn btn-link text-muted p-0" data-bs-toggle="dropdown">
            <i class="bi bi-three-dots-vertical fs-5"></i>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li><a class="dropdown-item report-btn" href="#">檢舉</a></li>
          </ul>
        </div>
      </div>`;

    /* 讚／倒讚邏輯 -------------------------------------- */
    const upBtn     = $('.up-btn',   card);
    const downBtn   = $('.down-btn', card);
    const upCount   = $('.up-count',   card);
    const downCount = $('.down-count', card);
    const uid       = getCurrentUser().id;
    let status      = '1'; // 預設中立

    if (uid) fetch(`/api/NewsLikeByMember?memNoId=${uid}&ncomNoId=${c.id}`)
        .then(r => r.json())
        .then(d => { status = d.nlikeStatus; ui(status); })
        .catch(()  => {});

    function ui(s) {
        upBtn.classList.toggle('btn-success', s === '2');
        upBtn.classList.toggle('active',      s === '2');
        downBtn.classList.toggle('btn-danger', s === '3');
        downBtn.classList.toggle('active',     s === '3');
    }
    async function update(newS) {
        const r = await fetch('/api/NewsLike/update', {
            method:'PUT',
            headers:{'Content-Type':'application/json'},
            body:JSON.stringify({ memNoId:uid, ncomNoId:c.id, nlikeStatus:newS })
        });
        if (!r.ok) throw await r.json();
        status = newS;
        ui(newS);
    }

    upBtn.onclick = async ()=> {
        if (!requireLogin()) return;
        let newS = status === '2' ? '1' : '2';
        if (status === '3') { upCount.textContent++; downCount.textContent--; }
        if (status === '1') { upCount.textContent++; }
        if (status === '2') { upCount.textContent--; }
        try { await update(newS); }
        catch (e){ alert(e.message||'失敗'); location.reload(); }
    };

    downBtn.onclick = async ()=> {
        if (!requireLogin()) return;
        let newS = status === '3' ? '1' : '3';
        if (status === '2') { upCount.textContent--; downCount.textContent++; }
        if (status === '1') { downCount.textContent++; }
        if (status === '3') { downCount.textContent--; }
        try { await update(newS); }
        catch (e){ alert(e.message||'失敗'); location.reload(); }
    };

    return card;
}

/* ---------- 平台 TAG ---------- */
function renderPlatformTags(tags = []) {
    const wrap = document.createElement('div');
    wrap.className = 'news-platform mb-3';
    const html = tags.map(p => {
        let cls = 'badge bg-secondary';
        if (/PC/.test(p))       cls = 'badge bg-primary';
        else if (/PS4/.test(p)) cls = 'badge bg-info text-dark';
        else if (/PS5/.test(p)) cls = 'badge bg-dark';
        else if (/Xbox/.test(p))cls = 'badge bg-success';
        else if (/Switch/.test(p))cls= 'badge bg-warning text-dark';
        else if (/Mobile/.test(p))cls= 'badge bg-danger';
        return `<span class="${cls} me-2">${p}</span>`;
    }).join('');
    wrap.innerHTML = html;
    return wrap;
}

/* ---------- 新留言輸入盒 ---------- */
function makeNewCommentBox(newsId) {
    const wrap = document.createElement('div');
    wrap.className = 'comment-form d-flex gap-3 align-items-start mb-5';
    wrap.id = 'new-comment';

    const mem = getCurrentUser();
    wrap.innerHTML = `
      <img src="/images/memberAvatar/${mem.id ? `mem${mem.id}` : 'defaultmem' }.png"
           onerror="this.src='/images/memberAvatar/defaultmem.png'"
           class="rounded-circle" width="60" height="60" alt="User">
      <div class="flex-grow-1">
        <textarea class="form-control mb-2" rows="4"
                  placeholder="發表你的看法…" id="c-input"></textarea>
        <div class="text-end">
          <button type="button" class="btn btn-primary btn-sm rounded-pill" id="c-send" disabled>送出</button>
        </div>
      </div>`;

    const ta  = $('#c-input', wrap);
    const btn = $('#c-send', wrap);

    ta.oninput = () => btn.disabled = !ta.value.trim();
    ta.onkeydown = e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); btn.click(); } };

    btn.onclick = async () => {
        if (!requireLogin()) return;
        const txt = ta.value.trim();
        if (!txt) return;
        btn.disabled = true;

        try {
            const body = {
                ncomCon : txt,
                newsNoId: parseInt(newsId),
                memNoId : mem.id
            };
            const r = await fetch('/api/NewsComment/add', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body:JSON.stringify(body)
            });
            if (!r.ok) throw await r.json();
            await reloadCommentArea(newsId);
        } catch (e) {
            alert(e?.errors?.[0]?.message || '留言失敗');
        } finally {
            ta.value = ''; btn.disabled = false;
        }
    };
    return wrap;
}

/* ---------- 重新載入留言區 ---------- */
async function reloadCommentArea(newsId) {
    const area = $('.comment-area') || document.querySelector('#news-box').appendChild(document.createElement('section'));
    area.className = 'comment-area';
    area.innerHTML = '';
    area.append(makeNewCommentBox(newsId));

    const comments = await fetch(`/api/NewsComment/${newsId}`).then(r => r.json());
    comments.forEach(c => area.append(makeCommentCard(c)));

    area.onclick = e => {
        if (e.target.closest('.report-btn')) {
            const card = e.target.closest('[data-comment-id]');
            if (card) showReportModal(card.dataset.commentId);
        }
    };
}

/* ---------- 檢舉 ---------- */
function showReportModal(commentId) {
    if (!requireLogin()) return;

    $('#reportCommentId').value = commentId;

    const modalEl   = $('#reportModal');
    const modalIns  = bootstrap.Modal.getOrCreateInstance(modalEl);
    modalIns.show();
}

/* 取得檢舉原因下拉 */
async function loadReportReasons() {
    try {
        const types = await fetch('/api/report-types').then(r => r.json());
        const sel   = $('#reportReasonSelect');
        types.forEach(t => {
            const opt = document.createElement('option');
            opt.value = t.id;
            opt.textContent = t.rpiType;
            sel.appendChild(opt);
        });
    } catch {
        alert('檢舉原因載入失敗');
    }
}

/* ---------- DOM Ready ---------- */
document.addEventListener('DOMContentLoaded', () => {
    const id = new URLSearchParams(location.search).get('newsId');
    loadPage(id);
    loadReportReasons();

    /* 檢舉表單提交 ------------------ */
    $('#reportForm')?.addEventListener('submit', async e => {
        e.preventDefault();
        const commentId = parseInt($('#reportCommentId').value);
        const reasonId  = parseInt($('#reportReasonSelect').value || 0);
        const userId    = getCurrentUser().id;

        if (!requireLogin()) return;
        if (!reasonId)      return alert('請選擇檢舉原因');

        const submitBtn = $('#reportForm button[type="submit"]');
        submitBtn.disabled = true;

        try {
            const r = await fetch('/api/create/newscommentreport', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body:JSON.stringify({
                    reporterId  : userId,
                    reportTypeId: reasonId,
                    ncomNoId    : commentId
                })
            });
            if (!r.ok) throw await r.json();

            alert('檢舉成功，感謝你的協助');
            $('#reportReasonSelect').value = '';

            bootstrap.Modal.getOrCreateInstance($('#reportModal')).hide();
        } catch (err) {
            alert(err?.message || '檢舉失敗，請稍後再試');
        } finally {
            /*  保險：確定 backdrop 被移除 */
            document.querySelectorAll('.modal-backdrop').forEach(bd => bd.remove());
            document.body.classList.remove('modal-open');
            submitBtn.disabled = false;
        }
    });
});
