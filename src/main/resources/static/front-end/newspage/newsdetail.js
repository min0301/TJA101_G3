/* ---------- 小工具 ---------- */
const $ = (s, p = document) => p.querySelector(s);
const icon = p => {
    const s = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    s.setAttribute('viewBox', '0 0 24 24');
    s.innerHTML = p;
    return s
};
const tagSpan = t => {
    const s = document.createElement('span');
    s.className = 'tag';
    s.textContent = t;
    return s
};

/* ---------- 載入整頁 ---------- */
async function loadPage(id) {
    /* 文章 ------------------------------------------------ */
    const news = await fetch(`/api/News/${id}`).then(r => r.json());
    const box = $('#news-box');
    document.title = `${news.newsTit}`;
    box.innerHTML = `
    <h1 class="news-title">${news.newsTit}</h1>
    <div class="news-meta">
      發佈：${new Date(news.newsCrdate).toLocaleString()}
      ${news.newsUpdate ? ` · 更新：${new Date(news.newsUpdate).toLocaleString()}` : ''}
    </div>
    <div class="tags"></div>`;
    box.append(renderPlatformTags(news.categoryTags || []));
    // news.categoryTags.forEach(t => $('.tags', box).append(tagSpan(t)));

    /* 圖片 ------------------------------------------------ */
    const imgs = await fetch(`/api/news/image/${id}`).then(r => r.json());
    if (imgs.length) {
        const wrap = document.createElement('div');
        wrap.className = 'news-images';

        // ① 取得放置燈箱用的容器；若沒有就另外建立
        const modalContainer = document.getElementById('newsLightboxes') ||
            document.body.appendChild(Object.assign(
                document.createElement('div'), {id: 'newsLightboxes'}));

        imgs.forEach((i, idx) => {
            /* === 產生圖片縮圖連結（點擊開燈箱） === */
            const a = document.createElement('a');
            a.href = 'javascript:void(0)';
            a.setAttribute('data-bs-toggle', 'modal');
            a.setAttribute('data-bs-target', `#imgModal-${idx}`);
            a.innerHTML = `<img src="${i.imgUrl}" alt="news image">`;
            wrap.append(a);

            /* === 產生對應 Modal（燈箱本體） === */
            const modal = document.createElement('div');
            modal.className = 'modal fade';
            modal.id = `imgModal-${idx}`;
            modal.tabIndex = -1;
            modal.setAttribute('aria-hidden', 'true');
            modal.innerHTML = `
          <div class="modal-dialog modal-dialog-centered modal-xl">
            <div class="modal-content bg-transparent border-0">
              <img src="${i.imgUrl}" class="w-100 rounded-3" alt="news image">
            </div>
          </div>
        `;
            modalContainer.appendChild(modal);
        });

        box.append(wrap);
    }

    /* 內文 */
    const art = document.createElement('div');
    art.className = 'news-content';
    art.textContent = news.newsCon;
    box.append(art);

    /* 留言 ------------------------------------------------ */
    const comments = await fetch(`/api/NewsComment/${id}`).then(r => r.json());
    const area = document.createElement('section');
    area.className = 'comment-area';

    box.append(area);
    reloadCommentArea(id);

}

function makeCommentCard(c) {
    const card = document.createElement('div');
    card.className = 'border rounded p-3 mb-4 shadow-sm';
    card.dataset.commentId = c.id;
    card.innerHTML = `
      <div class="d-flex justify-content-between">
        <div class="d-flex gap-3">
          <img src="/images/memberAvatar/mem${c.memNoMemNo}.png" alt="User"
          class="rounded-circle user-avatar" width="50" height="50"
          style="pointer-events: none;object-fit: cover"
          onerror="this.src='/images/memberAvatar/defaultmem.png'">
          <div>
            <h6 class="fw-bold text-dark mb-1" style="pointer-events: none;">${c.memNoMemNickName || '匿名'}</h6>
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
          <button class="btn btn-link text-muted p-0" type="button" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="bi bi-three-dots-vertical fs-5"></i>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li><a class="dropdown-item report-btn" href="#">檢舉</a></li>
          </ul>
        </div>
      </div>
    `;

    const upBtn = card.querySelector('.up-btn');
    const downBtn = card.querySelector('.down-btn');
    const upCount = card.querySelector('.up-count');
    const downCount = card.querySelector('.down-count');

    const raw = localStorage.getItem('memberInfo');
    const memberInfo = JSON.parse(raw || '{}');
    const currentUserId = memberInfo?.id;

    let currentStatus = '1'; // 預設為中立

// 初始化按鈕狀態
    if (currentUserId) {
        fetch(`/api/NewsLikeByMember?memNoId=${currentUserId}&ncomNoId=${c.id}`)
            .then(res => res.json())
            .then(data => {
                currentStatus = data.nlikeStatus;
                updateButtonsUI(currentStatus);
            })
            .catch(err => console.error('取得讚狀態失敗', err));
    }

// 更新按鈕樣式
    function updateButtonsUI(status) {
        upBtn.classList.remove('btn-success', 'active');
        downBtn.classList.remove('btn-danger', 'active');
        if (status === '2') upBtn.classList.add('btn-success', 'active');
        if (status === '3') downBtn.classList.add('btn-danger', 'active');
    }

// 統一發送更新 API
    async function updateLikeStatus(newStatus) {
        try {
            const res = await fetch('/api/NewsLike/update', {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    memNoId: currentUserId,
                    ncomNoId: c.id,
                    nlikeStatus: newStatus
                })
            });
            if (!res.ok) throw await res.json();
            currentStatus = newStatus;
            return true;
        } catch (e) {
            alert(e.message || '更新失敗');
            return false;
        }
    }

// 👍 按讚邏輯
    upBtn.addEventListener('click', async () => {
        if (!currentUserId) return alert("請先登入");

        let newStatus;
        if (currentStatus === '2') {
            // 從讚 → 中立
            newStatus = '1';
            upCount.textContent = parseInt(upCount.textContent) - 1;
        } else if (currentStatus === '3') {
            // 從倒讚 → 改按讚
            newStatus = '2';
            upCount.textContent = parseInt(upCount.textContent) + 1;
            downCount.textContent = parseInt(downCount.textContent) - 1;
        } else {
            // 中立 → 按讚
            newStatus = '2';
            upCount.textContent = parseInt(upCount.textContent) + 1;
        }

        const ok = await updateLikeStatus(newStatus);
        if (ok) updateButtonsUI(newStatus);
    });

// 👎 倒讚邏輯
    downBtn.addEventListener('click', async () => {
        if (!currentUserId) return alert("請先登入");

        let newStatus;
        if (currentStatus === '3') {
            // 從倒讚 → 中立
            newStatus = '1';
            downCount.textContent = parseInt(downCount.textContent) - 1;
        } else if (currentStatus === '2') {
            // 從按讚 → 改倒讚
            newStatus = '3';
            downCount.textContent = parseInt(downCount.textContent) + 1;
            upCount.textContent = parseInt(upCount.textContent) - 1;
        } else {
            // 中立 → 倒讚
            newStatus = '3';
            downCount.textContent = parseInt(downCount.textContent) + 1;
        }

        const ok = await updateLikeStatus(newStatus);
        if (ok) updateButtonsUI(newStatus);
    });


    return card;
}


function renderPlatformTags(tags = []) {
    const wrap = document.createElement('div');
    wrap.className = 'news-platform mb-3';
    wrap.innerHTML = tags.map(p => {
        let cls = 'badge bg-secondary';
        if (p.includes('PC')) cls = 'badge bg-primary';
        else if (p.includes('PS4')) cls = 'badge bg-info text-dark';
        else if (p.includes('PS5')) cls = 'badge bg-dark';
        else if (p.includes('Xbox')) cls = 'badge bg-success';
        else if (p.includes('Switch')) cls = 'badge bg-warning text-dark';
        else if (p.includes('Mobile')) cls = 'badge bg-danger';

        return `<span class="${cls} me-2">${p}</span>`;
    }).join('');
    return wrap;
}

/* ---------- 新留言輸入盒 ---------- */
function makeNewCommentBox(newsId) {
    const wrap = document.createElement('div');
    wrap.className = 'comment-form d-flex gap-3 align-items-start mb-5';
    wrap.id = 'new-comment';
    const memberInfo = localStorage.getItem('memberInfo');
    const mem = JSON.parse(memberInfo || '{}');

    wrap.innerHTML = `
        <img src="/images/memberAvatar/mem${mem.id}.png" alt="User" class="rounded-circle" width="60" height="60">
        <div class="flex-grow-1">
            <textarea class="form-control mb-2" rows="4" placeholder="發表你的看法…" id="c-input"></textarea>
            <div class="text-end">
                <button type="button" class="btn btn-primary btn-sm rounded-pill" id="c-send" disabled>送出</button>
            </div>
        </div>
    `;

    const ta = $('#c-input', wrap), btn = $('#c-send', wrap);

    ta.addEventListener('input', () => {
        btn.disabled = !ta.value.trim();
    });

    ta.addEventListener('keydown', e => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            if (!btn.disabled) btn.click();
        }
    });

    btn.addEventListener('click', async () => {
        const txt = ta.value.trim();
        if (!txt) return;
        btn.disabled = true;

        const raw = localStorage.getItem('memberInfo');
        const memberinfo = JSON.parse(raw || '{}');
        const currentUser = memberinfo.id;

        if (!currentUser) {
            alert("請先登入後再留言");
            btn.disabled = false;
            return;
        }

        try {
            const payload = {
                ncomCon: txt,
                newsNoId: parseInt(newsId), // ✅ 確保是整數
                memNoId: parseInt(currentUser)
            };
            console.log("送出 JSON:", JSON.stringify(payload, null, 2));

            const res = await fetch('/api/NewsComment/add', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });

            if (!res.ok) throw await res.json();

            // ✅ 成功後重新撈留言區
            await reloadCommentArea(newsId);

        } catch (err) {
            console.error('留言失敗', err);
            alert(err?.errors?.[0]?.message ?? '留言失敗，請稍後再試');
        } finally {
            ta.value = '';
            btn.disabled = false;
        }
    });

    return wrap;
}


async function reloadCommentArea(newsId) {
    const area = $('.comment-area');
    area.innerHTML = ''; // 清空區塊

    // 重新加上輸入框
    area.append(makeNewCommentBox(newsId));

    // 撈留言
    const comments = await fetch(`/api/NewsComment/${newsId}`).then(r => r.json());
    comments.forEach(c => {
        const commentCard = makeCommentCard(c);
        area.append(commentCard);
    });

    area.addEventListener('click', e => {
        if (e.target.closest('.report-btn')) {
            const commentCard = e.target.closest('.border.rounded');
            const commentId = commentCard?.dataset.commentId;
            if (!commentId) return alert("找不到留言 ID");
            showReportModal(commentId);
        }
    });
}


/* ---------- 把新留言插回列表 ---------- */
function prependComment(area, c) {
    const inputBox = $('#new-comment', area);
    area.insertBefore(makeCommentCard(c), inputBox.nextSibling);
}

function showReportModal(commentId) {
    $('#reportCommentId').value = commentId;
    const modal = new bootstrap.Modal(document.getElementById('reportModal'));
    modal.show();
}

async function loadReportReasons() {
    try {
        const res = await fetch('/api/report-types');
        const types = await res.json();
        const select = document.getElementById('reportReasonSelect');
        types.forEach(t => {
            const option = document.createElement('option');
            option.value = t.id;
            option.textContent = t.rpiType;
            select.appendChild(option);
        });
    } catch (err) {
        console.error("無法載入檢舉原因", err);
        alert("載入檢舉原因失敗");
    }
}

/* ---------- 啟動 ---------- */
document.addEventListener('DOMContentLoaded', () => {
    // loadPage(new URLSearchParams(location.search).get('newsId'));
    const id = new URLSearchParams(location.search).get('newsId');
    loadPage(id);
    loadReportReasons(); // ✅ 加入這行載入檢舉原因
    document.getElementById('reportForm')?.addEventListener('submit', async e => {
        e.preventDefault();
        const commentId = $('#reportCommentId').value;
        const reasonId = $('#reportReasonSelect')?.value;

        if (!reasonId) return alert("請選擇檢舉原因");

        try {
            const res = await fetch('/api/create/newscommentreport', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    reporterId: JSON.parse(localStorage.getItem('memberInfo'))?.id,
                    reportTypeId: parseInt(reasonId),
                    ncomNoId: parseInt(commentId)
                })

            });
            if (!res.ok) throw await res.json();
            alert("檢舉成功，感謝你的協助");
            const select = document.querySelector('#reportReasonSelect');
            if (select) {
                select.value = '';
            }

            bootstrap.Modal.getInstance(document.getElementById('reportModal')).hide();

        } catch (err) {
            console.error("檢舉失敗", err);
            alert(err?.message || '檢舉失敗，請稍後再試');
        }
    });
});