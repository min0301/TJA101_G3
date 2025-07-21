/* ============== 共用 API Helper（自動帶 JWT） ============== */
function api(path, options = {}) {
    const jwt  = localStorage.getItem('jwt');
    const base = window.location.origin;
    return fetch(`${base}${path}`, {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwt}`,
            ...(options.headers || {})
        },
        ...options
    });
}

/* ---------- 會員貢獻榜載入函式 ---------- */
async function refreshTopUsers() {                               // ★ 新增
    try {
        const res = await api('/api/members/game-score');
        if (!res.ok) throw new Error(res.status);
        const list = await res.json();

        const top10 = list
            .filter(u => u.point && u.point > 0)
            .sort((a,b) => b.point - a.point)
            .slice(0, 10);

        const ul = document.getElementById('top-users-list');
        if (!ul) return;

        ul.innerHTML = top10.map(u => `
      <li class="list-group-item border-0">
        <div class="media align-items-center">
          <div class="media-head me-3">
            <div class="avatar avatar-sm">
              <img src="/images/memberAvatar/mem${u.id}.png"
                   alt="${u.memNickName ?? u.memName}"
                   class="avatar-img avatar-rounded"
                   onerror="this.onerror=null;this.src='/images/memberAvatar/defaultmem.png';">
            </div>
          </div>
          <div class="media-body flex-grow-1">
            <a href="/member/${u.id}/profile" class="text-white text-decoration-none">
              ${u.memNickName ?? u.memName}
            </a>
          </div>
          <p class="mb-0"><i class="bi bi-trophy-fill text-warning me-1"></i>${u.point}</p>
        </div>
      </li>
    `).join('');
    } catch (e) {
        console.error('刷新排行榜失敗', e);
    }
}

/* ============== 解析 JWT，取得 memId ============== */
let myId   = null;          // 會員主鍵
let myBest = 0;             // 最高分

const jwt = localStorage.getItem('jwt');
if (jwt) {
    try {
        const payload = JSON.parse(atob(jwt.split('.')[1]));
        // JWT 裡存的是數字主鍵 memId
        if (Number.isInteger(payload.memId)) myId = payload.memId;
    } catch { /* 無效 JWT */ }
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
    document.body.classList.add('game-lock-scroll','game-show');
    init(); requestAnimationFrame(loop);
}
function closeGame() {
    document.body.classList.remove('game-lock-scroll','game-show');
    document.body.style.top = ''; window.scrollTo(0, savedScrollY);
}

/* ---- Canvas & 常數 ---- */
const cvs = document.getElementById('miniGameCanvas');
const ctx = cvs.getContext('2d');
const W = cvs.width, H = cvs.height;
const BIRD_W=34,BIRD_H=24,PIPE_W=52,GAP=140;

/* ---- 遊戲狀態 ---- */
let bird,pipes,score,best=0,running;
function init(){ bird={x:60,y:H/2,vy:0};pipes=[];score=0;best=myBest;running=true;}

/* ---- 操作 ---- */
window.addEventListener('keydown',e=>{ if(e.code==='Space') flap(); });
cvs.addEventListener('click',flap);
function flap(){ if(running) bird.vy=-6; }

/* ---- 主迴圈 ---- */
let lastTime=0;
function loop(ts){
    const dt=ts-lastTime; lastTime=ts;
    if(running) update(dt/16.666);
    draw();
    if(document.body.classList.contains('game-show')) requestAnimationFrame(loop);
}

/* ---- 更新邏輯 ---- */
function update(step){
    bird.vy+=0.35*step; bird.y+=bird.vy*step;
    if(!pipes.length||W-pipes[pipes.length-1].x>200)
        pipes.push({x:W,top:60+Math.random()*(H-GAP-120)});
    pipes.forEach(p=>p.x-=2.5*step);
    pipes=pipes.filter(p=>p.x+PIPE_W>0);
    pipes.forEach(p=>{
        if(p.x+PIPE_W<bird.x && !p.passed){ ++score; p.passed=true; }
        if(bird.x+BIRD_W>p.x && bird.x<p.x+PIPE_W &&
            (bird.y<p.top || bird.y+BIRD_H>p.top+GAP)) running=false;
    });
    if(bird.y+BIRD_H>H || bird.y<0) running=false;
    if(!running) gameOver();
}

/* ---- 繪圖 ---- */
function draw(){
    ctx.clearRect(0,0,W,H);
    ctx.fillStyle='#d0f4f7'; ctx.fillRect(0,0,W,H);
    ctx.fillStyle='#5fb257';
    pipes.forEach(p=>{
        ctx.fillRect(p.x,0,PIPE_W,p.top);
        ctx.fillRect(p.x,p.top+GAP,PIPE_W,H-p.top-GAP);
    });
    ctx.fillStyle='#ffeb3b'; ctx.fillRect(bird.x,bird.y,BIRD_W,BIRD_H);
    ctx.font='28px "Press Start 2P", monospace';
    ctx.fillStyle='#000'; ctx.fillText(score,20,40);
    if(!running){
        ctx.fillStyle='rgba(0,0,0,0.6)';
        ctx.fillRect(0,H/2-80,W,160);
        ctx.fillStyle='#fff'; ctx.textAlign='center';
        ctx.fillText('遊戲結束',W/2,H/2-40);
        ctx.fillText(`本局 ${score} ‧ 最高 ${best}`,W/2,H/2);
        ctx.fillText('按空白鍵再來一次',W/2,H/2+35);
        ctx.fillText('按ESC離開遊戲',W/2,H/2+70);
    }
}

/* ---- 結束處理 ---- */
function gameOver(){
    best=Math.max(best,score);
    submitScore(score);
    window.addEventListener('keydown',restartOnce);
    cvs.addEventListener('click',restartOnce);
}
function restartOnce(e){
    if(e.code && e.code!=='Space' && e.type==='keydown') return;
    window.removeEventListener('keydown',restartOnce);
    cvs.removeEventListener('click',restartOnce);
    init();
}

/* ---- 送分數 ---- */
function submitScore(s){
    api('/api/members/update-game-score', {
        method:'PATCH',
        body: JSON.stringify({ id: myId, score: s })
    })
        .then(()=>{ if(s>myBest) myBest=s;
            refreshTopUsers();    })
        .catch(console.error);
}
