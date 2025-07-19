/* ---------------- 共用 API Helper（自動帶 JWT） ---------------- */
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

/* ---------------- 解析 JWT 取得 memberId ---------------- */
function parseJwt(token) {
    try { return JSON.parse(atob(token.split('.')[1])); }
    catch { return {}; }
}

let myId   = null;   // 會員主鍵
let myBest = 0;      // 後端最高分

const jwt = localStorage.getItem('jwt');
if (jwt) {
    const payload = parseJwt(jwt);
    myId = payload.id || payload.memberId || payload.sub;  // 依實際欄位調整
}

/* ---------- 讀取自己的最高分 ---------- */
if (myId) {
    api(`/api/members/game-score/${myId}`, { method: 'GET' })
        .then(r => r.ok ? r.json() : Promise.reject())
        .then(dto => { myBest = dto.point ?? 0; })
        .catch(console.error);
}

/* ==================== 遊戲主程式以下 ==================== */
const HADOUKEN = ['ArrowDown','ArrowRight','ArrowDown','ArrowRight','KeyP'];
let buffer = [];

window.addEventListener('keydown', e => {
    buffer.push(e.code);
    if (buffer.length > HADOUKEN.length) buffer.shift();

    if (buffer.join() === HADOUKEN.join()) {
        if (!myId) { alert('請先登入！'); return; }
        toggleGame();
    }
    if (e.code === 'Escape') closeGame();
});

/* ---------- 捲軸鎖定 / Canvas 變數 ---------- */
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

const cvs = document.getElementById('miniGameCanvas');
const ctx = cvs.getContext('2d');
const W = cvs.width, H = cvs.height;
const BIRD_W=34,BIRD_H=24,PIPE_W=52,GAP=140;

/* ---------- 遊戲狀態 ---------- */
let bird,pipes,score,best=0,running;
function init(){ bird={x:60,y:H/2,vy:0};pipes=[];score=0;best=myBest;running=true;}

/* ---------- 操作 ---------- */
window.addEventListener('keydown',e=>{ if(e.code==='Space') flap(); });
cvs.addEventListener('click',flap);
function flap(){ if(running) bird.vy=-6; }

/* ---------- 主迴圈 ---------- */
let lastTime=0;
function loop(ts){
    const dt=ts-lastTime; lastTime=ts;
    if(running) update(dt/16.666);
    draw();
    if(document.body.classList.contains('game-show')) requestAnimationFrame(loop);
}

/* ---------- 更新 ---------- */
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

/* ---------- 繪圖 ---------- */
function draw(){
    ctx.clearRect(0,0,W,H);
    ctx.fillStyle='#d0f4f7'; ctx.fillRect(0,0,W,H);
    ctx.fillStyle='#5fb257';
    pipes.forEach(p=>{
        ctx.fillRect(p.x,0,PIPE_W,p.top);
        ctx.fillRect(p.x,p.top+GAP,PIPE_W,H-p.top-GAP);
    });
    ctx.fillStyle='#ffeb3b';
    ctx.fillRect(bird.x,bird.y,BIRD_W,BIRD_H);
    ctx.font='28px "Press Start 2P", monospace';
    ctx.fillStyle='#000'; ctx.fillText(score,20,40);

    if(!running){
        ctx.fillStyle='rgba(0,0,0,0.6)';
        ctx.fillRect(0,H/2-80,W,160);
        ctx.fillStyle='#fff'; ctx.textAlign='center';
        ctx.fillText('遊戲結束',W/2,H/2-20);
        ctx.fillText(`本局 ${score} ‧ 最高 ${best}`,W/2,H/2+30);
        ctx.fillText('按空白鍵再來一次',W/2,H/2+80);
    }
}

/* ---------- 結束 ---------- */
function gameOver(){
    best = Math.max(best, score);
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

/* ---------- 提交分數 ---------- */
function submitScore(s){
    if(!myId) return;
    api('/api/members/update-game-score', {
        method:'PATCH',
        body  : JSON.stringify({ id: myId, score: s })
    })
        .then(()=>{ if(s>myBest) myBest=s; })
        .catch(console.error);
}
