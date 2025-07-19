// static/front-end/minigame/api.js
export function api(path, options = {}) {
    // ① 取出 JWT（請確定登入流程已寫入 localStorage）
    const jwt = localStorage.getItem('jwt');
    if (!jwt) throw new Error('尚未登入或找不到 JWT');

    // ② 動態組出 Base URL（http/https + host:port）
    const base = window.location.origin;     // 例：http://localhost:8080

    // ③ 合併 headers
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwt}`,
        ...options.headers,                    // 允許覆寫
    };

    return fetch(`${base}${path}`, {
        credentials: 'include',                // 若只靠 JWT 可以拿掉
        ...options,
        headers,
    });
}
