/* ============== 共用 API Helper（自動帶 JWT） ============== */
function api(path, options = {}) {
    const jwt  = localStorage.getItem('jwt');
    const base = window.location.origin;
    return fetch(`${base}${path}`, {
        credentials: 'include',      // 若只靠 JWT，可移除
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwt}`,
            ...(options.headers || {})
        },
        ...options
    });
}

/* ---------- 會員貢獻榜載入函式 ---------- */
async function refreshTopUsers() {
    try {
        const res = await api('/api/members/game-score');
        if (!res.ok) throw new Error(res.status);
        const list = await res.json();

        const top10 = list
            .filter(u => u.point && u.point > 0)
            .sort((a, b) => b.point - a.point)
            .slice(0, 10);

        const ul = document.getElementById('top-users-list');
        if (!ul) return;

        const trophyColors = ['gold', 'silver', '#cd7f32']; // 金銀銅

        ul.innerHTML = top10.map((u, i) => `
<li class="list-group-item border-0">
  <div class="d-flex align-items-center justify-content-between" style="min-height:56px;">
    <!-- 頭像 + 名稱 -->
    <div class="d-flex align-items-center">
      <img src="/images/memberAvatar/mem${u.id}.png"
           alt="${u.memNickName ?? u.memName}"
           class="rounded-circle me-3 d-block"
           width="48" height="48"
           onerror="this.onerror=null;this.src='/images/memberAvatar/defaultmem.png';">

      <p class="mb-0 fw-bold text-dark d-inline-flex align-items-center">
        ${u.memNickName ?? u.memName}
      </p>
    </div>

    <!-- 排名獎盃 -->
    <div class="d-flex align-items-center"
         style="color:${i < 3 ? trophyColors[i] : '#aaa'};">
      <i class="bi bi-trophy-fill me-1"></i>${u.point}
    </div>
  </div>
</li>
`).join('');

    } catch (e) {
        console.error('刷新排行榜失敗', e);
    }
}

/* ============== 解析 JWT，取得 memId ============== */
let myId   = null;  // 會員主鍵
let myBest = 0;     // 最高分

const jwt = localStorage.getItem('jwt');
if (jwt) {
    try {
        const payload = JSON.parse(atob(jwt.split('.')[1]));
        if (Number.isInteger(payload.memId)) myId = payload.memId;
    } catch {/* 無效 JWT */}
}

/* ============== 取得自己最高分 ============== */
if (myId !== null) {
    api(`/api/members/game-score/${myId}`, { method: 'GET' })
        .then(r => r.ok ? r.json() : Promise.reject(r))
        .then(dto => { myBest = dto.point ?? 0; })
        .catch(console.error);
}

/* ============== 小遊戲主程式 ============== */
const HADOUKEN = ['ArrowDown','ArrowRight','ArrowDown','ArrowRight','KeyP'];
let buffer = [];

/* ---- 秘技觸發 ---- */
window.addEventListener('keydown', e => {
    buffer.push(e.code);
    if (buffer.length > HADOUKEN.length) buffer.shift();

    if (buffer.join() === HADOUKEN.join()) {
        if (myId === null) { alert('請先登入！'); return; }
        toggleGame();
    }
    if (e.code === 'Escape') closeGame();
});

/* ---- 捲軸鎖定 ---- */
let savedScrollY = 0;
function toggleGame() {
    savedScrollY = window.scrollY || document.documentElement.scrollTop;
    document.body.style.top = `-${savedScrollY}px`;
    document.body.classList.add('game-lock-scroll', 'game-show');
    init(); requestAnimationFrame(loop);
}
function closeGame() {
    document.body.classList.remove('game-lock-scroll', 'game-show');
    document.body.style.top = '';
    window.scrollTo(0, savedScrollY);
}

/* ---- Canvas & 常數 ---- */
const cvs     = document.getElementById('miniGameCanvas');
const ctx     = cvs.getContext('2d');
const W       = cvs.width,  H       = cvs.height;
const BIRD_W  = 34,         BIRD_H  = 24;
const PIPE_W  = 52,         GAP     = 140;

/* ---- 遊戲狀態 ---- */
let bird, pipes, score, best = 0, running;
function init() {
    bird = { x:60, y:H/2, vy:0 };
    pipes = [];
    score = 0;
    best  = myBest;
    running = true;
}

/* ---- 操作 ---- */
window.addEventListener('keydown', e => { if (e.code === 'Space') flap(); });
cvs.addEventListener('click', flap);
function flap(){ if (running) bird.vy = -6; }

/* ---- 主迴圈 ---- */
let lastTime = 0;
function loop(ts) {
    const dt = ts - lastTime; lastTime = ts;
    if (running) update(dt / 16.666);
    draw();
    if (document.body.classList.contains('game-show')) requestAnimationFrame(loop);
}

/* ---- 更新邏輯 ---- */
function update(step) {
    bird.vy += 0.35 * step;
    bird.y  += bird.vy * step;

    if (!pipes.length || W - pipes[pipes.length-1].x > 200)
        pipes.push({ x:W, top:60 + Math.random() * (H - GAP - 120) });

    pipes.forEach(p => p.x -= 2.5 * step);
    pipes = pipes.filter(p => p.x + PIPE_W > 0);

    pipes.forEach(p => {
        if (p.x + PIPE_W < bird.x && !p.passed) { ++score; p.passed = true; }
        if (bird.x + BIRD_W > p.x && bird.x < p.x + PIPE_W &&
            (bird.y < p.top || bird.y + BIRD_H > p.top + GAP)) running = false;
    });

    if (bird.y + BIRD_H > H || bird.y < 0) running = false;
    if (!running) gameOver();
}

/* ---- 繪圖 ---- */
function draw() {
    ctx.clearRect(0,0,W,H);
    ctx.fillStyle = '#d0f4f7'; ctx.fillRect(0,0,W,H);
    ctx.fillStyle = '#5fb257';
    pipes.forEach(p => {
        ctx.fillRect(p.x, 0, PIPE_W, p.top);
        ctx.fillRect(p.x, p.top + GAP, PIPE_W, H - p.top - GAP);
    });
    ctx.fillStyle = '#ffeb3b';
    ctx.fillRect(bird.x, bird.y, BIRD_W, BIRD_H);

    ctx.font = '28px "Press Start 2P", monospace';
    ctx.fillStyle = '#000';
    ctx.fillText(score, 20, 40);

    if (!running) {
        ctx.fillStyle = 'rgba(0,0,0,0.6)';
        ctx.fillRect(0, H/2-80, W, 160);

        ctx.fillStyle = '#fff';
        ctx.textAlign = 'center';
        ctx.fillText('遊戲結束',               W/2, H/2-40);
        ctx.fillText(`本局 ${score} ‧ 最高 ${best}`, W/2, H/2);
        ctx.fillText('按空白鍵再來一次',      W/2, H/2+35);
        ctx.fillText('按 ESC 離開遊戲',       W/2, H/2+70);
    }
}

/* ---- 結束處理 ---- */
function gameOver() {
    best = Math.max(best, score);
    submitScore(score);
    window.addEventListener('keydown', restartOnce);
    cvs.addEventListener('click', restartOnce);
}
function restartOnce(e) {
    if (e.code && e.code !== 'Space' && e.type === 'keydown') return;
    window.removeEventListener('keydown', restartOnce);
    cvs.removeEventListener('click',    restartOnce);
    init();
}

/* ---- 送分數 ---- */
function submitScore(s){
    api('/api/members/update-game-score', {
        method:'PATCH',
        body: JSON.stringify({ id: myId, score: s })
    })
        .then(() => {
            if (s > myBest) myBest = s;
            refreshTopUsers();
        })
        .catch(console.error);
}

/* ---- 將函式輸出到全域供頁面呼叫 ---- */
window.refreshTopUsers = refreshTopUsers;
