// front-end/forumsys/allForum.js (完整最終版)

/**
 * 【核心功能】
 * 根據傳入的 forumId，抓取文章並顯示在右欄。
 * @param {string} forumId - 討論區的 ID
 */
async function showPostListView(forumId) {
    const rightContainer = document.getElementById('dynamic-content-container');
    if (!rightContainer) {
        console.error("錯誤：找不到右欄容器 #dynamic-content-container！");
        return;
    }

    // 顯示讀取中動畫
    rightContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>`;

    // 桌面版才平滑滾動到頂部，避免在小螢幕上行為怪異
    if (window.innerWidth > 992) {
        window.scrollTo({top: 0, behavior: 'smooth'});
    }

    try {
        // 【【修改點】】
        // 1. 從 localStorage 取得 JWT Token
        const token = localStorage.getItem('jwt');

        // 2. 建立請求標頭 (Request Headers)
        const headers = {
            'Content-Type': 'application/json'
        };

        // 3. 如果 token 存在（使用者已登入），就將它加入到 Authorization 標頭中
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // 4. 在 fetch 中帶上 headers，讓後端能識別用戶身份
        //    這樣後端才能回傳正確的 isCollected 狀態
        const [forumRes, postsRes] = await Promise.all([
            fetch(`/api/forum/${forumId}`, {headers: headers}), // 主要修改處
            fetch(`/api/forum/${forumId}/posts`) // 取得文章列表通常不需要身份，可不加
        ]);

        if (!forumRes.ok || !postsRes.ok) {
            // 如果請求論壇資訊失敗且狀態碼為401/403，可能表示token無效
            if (forumRes.status === 401 || forumRes.status === 403) {
                console.warn('取得論壇資訊授權失敗，可能是Token過期。');
            }
            throw new Error('抓取討論區或文章資料失敗');
        }

        const forum = await forumRes.json();
        const posts = await postsRes.json();

        // 依序渲染 Header 和文章列表
        renderForumHeader(rightContainer, forum);
        renderPosts(rightContainer, posts);

        // 初始化 AOS 動畫
        setTimeout(() => {
            AOS.init({once: true});
            rightContainer.querySelectorAll('[data-aos]').forEach(el => el.classList.add('aos-animate'));
        }, 100);

    } catch (error) {
        showError(rightContainer, `無法載入文章列表 (ID: ${forumId})`);
        console.error(error);
    }
}

/**
 * 【最終整合版本】
 * 渲染帶有橫幅圖片、標題、描述以及收藏按鈕的討論區頂部。
 * @param {HTMLElement} container - 要渲染內容的容器元素
 * @param {object} forum - 從 API 取得的討論區物件
 */
function renderForumHeader(container, forum) {
    const headerContainer = document.createElement('div');

    // --- 邏輯部分：整合圖片和按鈕狀態 ---

    // 1. 圖片路徑邏輯
    const imageUrl = forum.forImgUrl || '../assets/img/categories/1.jpg';
    const fallbackImageUrl = '../assets/img/categories/1.jpg';

    // 2. 收藏按鈕狀態邏輯
    // 【【【核心修正點】】】 將 isCollected 改為 collected，與後端 JSON 欄位名保持一致
    const isCollected = forum.collected;
    const buttonText = isCollected ? '已收藏' : '收藏';
    // 為了在圖片上更顯眼，使用亮色系按鈕
    const buttonClass = isCollected ? 'btn-primary' : 'btn-outline-light';

    // --- HTML 結構：將按鈕整合進圖片標頭中 ---
    headerContainer.innerHTML = `
        <div class="position-relative text-white mb-4" style="width: 100%; height: 200px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            <img src="${imageUrl}" alt="${forum.forName}" class="w-100 h-100" style="object-fit: cover;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
            <div class="position-absolute top-0 start-0 w-100 h-100" style="background: linear-gradient(to top, rgba(0,0,0,0.7), rgba(0,0,0,0.1));"></div>
            
            <div class="position-absolute bottom-0 start-0 w-100 p-3 p-md-4">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="me-3">
                        <h2 class="fw-bold mb-1">${forum.forName}</h2>
                        <p class="mb-0 d-none d-md-block">${forum.forDes}</p>
                    </div>
                    <div class="flex-shrink-0">
                        <button id="collect-btn" class="btn ${buttonClass} btn-sm">
                            <i class="bi bi-heart-fill"></i> ${buttonText}
                        </button>
                    </div>
                </div>
                 <p class="mb-0 d-md-none mt-2">${forum.forDes}</p>
            </div>
        </div>
    `;

    // 將組合好的新標頭渲染到頁面上
    container.innerHTML = '';
    container.appendChild(headerContainer);

    // 為新產生的按鈕綁定點擊事件
    addCollectButtonListener(forum.id);
}

/**
 * 【新增的函式】
 * 為收藏按鈕綁定點擊事件，處理後端 API 呼叫。
 * @param {number} forumId - 當前討論區的 ID
 */
function addCollectButtonListener(forumId) {
    const collectBtn = document.getElementById('collect-btn');
    if (!collectBtn) return;

    collectBtn.addEventListener('click', async () => {
        try {
            collectBtn.disabled = true;

            // 【【關鍵修改點】】
            // 1. 從 localStorage (或您儲存 Token 的地方) 取得 JWT Token
            //    請確認您儲存 Token 的 key 正確，這裡假設是 'jwt'
            const token = localStorage.getItem('jwt');
            const headers = {'Content-Type': 'application/json'};
            // 2. 如果沒有 token，代表使用者未登入，直接提示並中斷操作
            if (!token) {
                alert('請先登入才能使用收藏功能。');
                collectBtn.disabled = false; // 恢復按鈕
                return; // 中斷函式執行
            }

            // 3. 發送 fetch 請求，並在 headers 中加入 Authorization
            const response = await fetch(`/api/forums/${forumId}/collect`, {
                method: 'PUT',
                headers: {
                    // Bearer 後面有一個空格，這是標準格式
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                // 如果後端回應 401 或 403，也可能是 token 過期或無效
                if (response.status === 401 || response.status === 403) {
                    alert('您的登入已過期或無效，請重新登入。');
                } else {
                    throw new Error('API request failed');
                }
                return;
            }

            const resultDTO = await response.json();

            if (resultDTO.collectStatus === 'COLLECT') {
                collectBtn.innerHTML = '<i class="bi bi-heart-fill"></i> 已收藏';
                collectBtn.classList.replace('btn-outline-light', 'btn-primary');
            } else { // 'NORMAL'
                collectBtn.innerHTML = '<i class="bi bi-heart-fill"></i> 收藏';
                collectBtn.classList.replace('btn-primary', 'btn-outline-light');
            }

        } catch (error) {
            console.error('收藏/取消收藏操作失敗:', error);
            alert('操作失敗，請檢查您的網路連線或稍後再試。');
        } finally {
            collectBtn.disabled = false;
        }
    });
}

/**
 * 【文章列表渲染】
 * 將文章列表渲染到容器中。
 * @param {HTMLElement} container - 外部容器
 * @param {Array} posts - 文章物件陣列
 */
function renderPosts(container, posts) {
    const postsContainer = document.createElement('div');
    postsContainer.id = 'post-list-container';

    if (!posts || posts.length === 0) {
        postsContainer.innerHTML = '<div class="alert alert-info">這個討論區還沒有文章喔！</div>';
        container.appendChild(postsContainer);
        return;
    }

    posts.forEach(post => {
        const fallbackImageUrl = '../../assets/img/categories/1.jpg';
        const postImageUrl = `/api/forumpost/image/${post.id}`;
        const title = post.postTitle || "（無標題）";
        const content = post.postCon || "";

        const postCardHTML = `
            <div class="post-box d-flex mb-3" data-aos="fade-up">
                <div class="card w-100">
                    <div class="card-body d-flex">
                        <div class="flex-shrink-0 me-3">
                            <img src="${postImageUrl}" alt="${title}" style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
                        </div>
                        
                        <div class="flex-grow-1 d-flex flex-column">
                            <div>
                                <h4 class="card-title fw-bold"><a>${title}</a></h4>
                                <p class="card-text post-summary">${content.substring(0, 80)}...</p>
                            </div>
                            
                            <div class="mt-auto d-flex justify-content-between align-items-center">
                                <small class="text-muted">樓主: ${post.memberName || '匿名'}</small>
                                <div class="post-stats">
                                    <span class="me-3" title="留言數"><i class="bi bi-chat-dots-fill"></i> ${post.mesNumbers || 0}</span>
                                    <span class="me-3" title="喜歡"><i class="bi bi-hand-thumbs-up-fill text-success"></i> ${post.postLikeCount || 0}</span>
                                    <span title="不喜歡"><i class="bi bi-hand-thumbs-down-fill text-danger"></i> ${post.postLikeDlc || 0}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        postsContainer.innerHTML += postCardHTML;
    });

    container.appendChild(postsContainer);
}

/**
 * 【錯誤處理】
 * 在容器中顯示錯誤訊息。
 * @param {HTMLElement} container - 要顯示錯誤的容器
 * @param {string} message - 錯誤訊息
 */
function showError(container, message) {
    container.innerHTML = `<div class="alert alert-danger">${message}</div>`;
}
