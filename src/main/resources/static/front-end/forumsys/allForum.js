// allForum.js (SPA功能 + 整合聊天室 + 文章按讚功能 + 新增文章功能)

// --- 全域變數 ---
// 可變：變數名稱 dynamicContentContainer 可依需求自行命名，但需確保與 HTML 中的 ID 一致。
// 不可變：document.getElementById 是固定的 DOM API。
const dynamicContentContainer = document.getElementById('dynamic-content-container');

// --- 初始化與路由管理 ---

/**
 * 頁面載入完成後執行的主函式
 */
document.addEventListener('DOMContentLoaded', () => {
    populateReportTypes();

    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get('postId');
    const view = urlParams.get('view');

    if (postId) {
        showPostDetailView(postId);
    } else if (view === 'collected') {
        showCollectedPostsView();
    } else {
        showInitialView();
    }

    window.addEventListener('popstate', handlePopState);
    setupEventListeners();
    // 【修正點 1】在 DOMContentLoaded 時就初始化一次新增文章的 Modal 和表單事件
    setupNewPostModal();
});

/**
 * 處理瀏覽器上/下一頁的事件
 */
function handlePopState(event) {
    const params = new URLSearchParams(window.location.search);
    const postId = params.get('postId');
    const view = params.get('view');

    if (postId) {
        showPostDetailView(postId);
    } else if (view === 'collected') {
        showCollectedPostsView();
    } else {
        const activeForum = document.querySelector('.forum-link.active');
        if (activeForum) {
            showPostListView(activeForum.dataset.forumId);
        } else {
            showInitialView();
        }
    }
}

// --- 視圖渲染核心函式 ---

/**
 * 顯示初始畫面 (提示使用者從左側選擇討論區)
 */
function showInitialView() {
    dynamicContentContainer.innerHTML = `
        <div class="original-content text-center p-5 bg-light rounded">
            <i class="bi bi-arrow-left-circle-fill fs-1 text-muted"></i>
            <h4 class="mt-3">請從左側選擇一個討論區</h4>
            <p class="text-muted">點擊後，文章列表將會顯示在此處。</p>
        </div>
    `;
}

/**
 * 顯示指定討論區的文章列表 (並整合聊天室)
 * @param {string} forumId - 討論區 ID
 */
window.showPostListView = async function (forumId) {
    dynamicContentContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>`;
    if (window.innerWidth > 992) {
        window.scrollTo({top: 0, behavior: 'smooth'});
    }

    try {
        const token = localStorage.getItem('jwt');
        const headers = {'Content-Type': 'application/json'};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // 這裡需要獲取文章類別資料，用於新增文章 Modal 的下拉選單
        const [forumRes, postsRes, forumTagsRes] = await Promise.all([
            fetch(`/api/forum/${forumId}`, {headers: headers}),
            fetch(`/api/forum/${forumId}/posts/sorted`, {headers: headers}),
            fetch(`/api/forumtag`, {headers: headers}) // 獲取所有文章類別，路徑根據 ForumTagController.java
        ]);

        if (!forumRes.ok || !postsRes.ok || !forumTagsRes.ok) {
            // 嘗試獲取更多錯誤信息
            const forumError = !forumRes.ok ? await forumRes.text() : '';
            const postsError = !postsRes.ok ? await postsRes.text() : '';
            const tagsError = !forumTagsRes.ok ? await forumTagsRes.text() : '';
            console.error('API Fetch Errors:', { forumError, postsError, tagsError });
            throw new Error('抓取討論區、文章或文章類別資料失敗');
        }

        const forum = await forumRes.json();
        const posts = await postsRes.json();
        const forumTags = await forumTagsRes.json(); // 解析文章類別資料

        renderForumHeader(dynamicContentContainer, forum);

        if (window.chatManager && typeof window.chatManager.disconnect === 'function') {
            window.chatManager.disconnect();
        }

        const chatRoomContainer = document.createElement('div');
        chatRoomContainer.id = 'chat-room-container';
        chatRoomContainer.className = 'mb-4';
        chatRoomContainer.innerHTML = `
            <div class="chat-room card">
                <div class="card-header bg-light d-flex justify-content-between align-items-center">
                    <h5 class="mb-0" ><i class="bi bi-chat-dots-fill me-2"></i>${forum.forName}-即時聊天室</h5>
                    <span id="chat-status" class="badge bg-secondary">未連接</span>
                </div>
                <div class="card-body">
                    <div id="chat-messages" class="chat-messages mb-3"></div>
                    <form id="chat-form" class="d-flex gap-2">
                        <input type="text" id="chat-message-input" class="form-control" placeholder="輸入訊息..." autocomplete="off" disabled>
                        <button type="submit" class="btn btn-primary" disabled><i class="bi bi-send-fill"></i></button>
                    </form>
                </div>
            </div>
        `;
        dynamicContentContainer.appendChild(chatRoomContainer);

        const memberInfo = JSON.parse(localStorage.getItem('memberInfo') || '{}');
        if (token && memberInfo.memNickName && window.chatManager) {
            window.chatManager.connect(forumId, memberInfo.memNickName, memberInfo.id);
        } else {
            const chatMessagesDiv = document.getElementById('chat-messages');
            if (chatMessagesDiv) {
                chatMessagesDiv.innerHTML = '<p class="text-muted text-center p-3">您需要<a href="/front-end/mem/MemberLogin.html">登入</a>才能使用聊天室。</p>';
            }
        }

        // 新增文章按鈕容器
        const newPostButtonContainer = document.createElement('div');
        newPostButtonContainer.className = 'd-flex justify-content-end mb-3'; // 靠右對齊
        newPostButtonContainer.innerHTML = `
            <button class="btn btn-success" data-bs-toggle="modal" data-bs-target="#newPostModal" data-forum-id="${forumId}">
                <i class="bi bi-plus-circle me-2"></i>新增文章
            </button>
        `;
        dynamicContentContainer.appendChild(newPostButtonContainer);

        renderPosts(dynamicContentContainer, posts, false);

        history.pushState({forumId: forumId}, ``, `?forumId=${forumId}`);

        // 【修正點 2】將 forumTags 和 currentForumId 傳遞給 setupNewPostModal 中的 Modal 顯示事件
        // 這裡不再直接呼叫 setupNewPostModal，因為它只會在 DOMContentLoaded 時初始化一次。
        // 相反，我們會在 Modal 顯示時，透過事件監聽器將這些資料傳入。

        // 【修正點 3】更新 Modal 的 show.bs.modal 事件監聽器邏輯
        const newPostModal = document.getElementById('newPostModal');
        if (newPostModal) {
            // 移除舊的 show.bs.modal 監聽器以避免重複設定邏輯 (雖然因為它只被呼叫一次，這裡可以省略)
            // newPostModal.removeEventListener('show.bs.modal', newPostModalShowHandler);
            // newPostModal.addEventListener('show.bs.modal', newPostModalShowHandler);
            // newPostModalShowHandler 會在 setupNewPostModal 中處理，並將 forumTags 和 currentForumId 設為 Modal 內部的狀態。
        }


        setTimeout(() => {
            AOS.init({once: true});
            dynamicContentContainer.querySelectorAll('[data-aos]').forEach(el => el.classList.add('aos-animate'));
        }, 100);

    } catch (error) {
        showError(dynamicContentContainer, `無法載入文章列表 (ID: ${forumId})`);
        console.error(error);
    }
}


/**
 * 顯示單一文章的詳細內容與留言
 * @param {string} postId - 文章 ID
 */
async function showPostDetailView(postId) {
    dynamicContentContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border" role="status"></div></div>`;

    try {
        const token = localStorage.getItem('jwt');
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        const postRes = await fetch(`/api/forumpost/${postId}`, { headers: headers });
        if (!postRes.ok) throw new Error('文章載入失敗');
        const post = await postRes.json();

        // 收藏狀態直接從 post 物件中取得
        const isCollected = post.collected;

        // 按讚狀態不再從獨立 API 獲取，因為後端未提供此接口
        // 按讚按鈕的顏色將固定，不反映會員個人狀態
        // memberPostLikeStatus 參數將不再用於按鈕顏色變化
        const memberPostLikeStatus = 'NEUTRAL'; // 固定為中立，僅為參數佔位

        // 傳遞按讚狀態和收藏狀態給 renderPostDetail
        renderPostDetail(post, memberPostLikeStatus, isCollected);
        await loadAndRenderComments(postId);

        const forumIdForHistory = post.forumNo || post.forumId;

        let newUrl = `?postId=${postId}`;
        let historyState = {postId: postId};

        if (forumIdForHistory) {
            newUrl += `&forumId=${forumIdForHistory}`;
            historyState.forumId = forumIdForHistory;
        }

        history.pushState(historyState, ``, newUrl); // 將 forumId 也存入 history state 和 URL
        // --- 修改的重點結束 ---

    } catch (error) {
        console.error('載入文章詳細頁出錯:', error);
        showError(dynamicContentContainer, '載入文章時發生錯誤。');
    }
}

/**
 * 顯示使用者收藏的文章列表
 */
window.showCollectedPostsView = async function () {
    dynamicContentContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>`;
    if (window.innerWidth > 992) {
        window.scrollTo({top: 0, behavior: 'smooth'});
    }

    try {
        const token = localStorage.getItem('jwt');
        if (!token) {
            dynamicContentContainer.innerHTML = `
                <div class="alert alert-info text-center p-5">
                    <i class="bi bi-person-fill-lock fs-1 text-muted mb-3"></i>
                    <h4>您尚未登入</h4>
                    <p>請 <a href="/front-end/mem/MemberLogin.html" class="alert-link">登入</a> 以查看您的收藏文章。</p>
                </div>
            `;
            return;
        }

        const headers = {'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`};
        const response = await fetch(`/api/posts/collect/me`, {headers: headers});

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                alert('您的登入已過期或無效，請重新登入。');
            }
            throw new Error('無法獲取收藏文章列表');
        }

        const collectedPosts = await response.json();

        const headerHtml = `
            <div class="d-flex align-items-center mb-4">
                <i class="bi bi-bookmark-heart-fill fs-2 me-3 text-danger"></i>
                <h2 class="fw-bold mb-0">我的收藏文章</h2>
            </div>
        `;
        dynamicContentContainer.innerHTML = headerHtml;

        renderPosts(dynamicContentContainer, collectedPosts, false); // 收藏列表頁，不顯示收藏按鈕 (因為已經是收藏的了)

        history.pushState({view: 'collected'}, ``, `?view=collected`);

        setTimeout(() => {
            AOS.init({once: true});
            dynamicContentContainer.querySelectorAll('[data-aos]').forEach(el => el.classList.add('aos-animate'));
        }, 100);

    } catch (error) {
        showError(dynamicContentContainer, `無法載入我的收藏文章`);
        console.error(error);
    }
};


// --- 組件渲染輔助函式 ---

/**
 * 渲染帶有橫幅圖片、標題、描述以及討論區收藏按鈕的討論區頂部。
 */
function renderForumHeader(container, forum) {
    const headerContainer = document.createElement('div');
    const imageUrl = forum.forImgUrl || '../assets/img/categories/1.jpg';
    const fallbackImageUrl = '../assets/img/categories/1.jpg';
    const isCollected = forum.collected;
    const buttonText = isCollected ? '已收藏' : '收藏';
    const buttonClass = isCollected ? 'btn-primary' : 'btn-outline-light';

    headerContainer.innerHTML = `
        <div class="position-relative text-white mb-4" style="width: 100%; height: 200px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            <img src="${imageUrl}" alt="${forum.forName}" class="w-100 h-100" style="object-fit: cover;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
            <div class="position-absolute top-0 start-0 w-100 h-100" style="background: linear-gradient(to top, rgba(0,0,0,0.7), rgba(0,0,0,0.1));"></div>
            <div class="position-absolute bottom-0 start-0 w-100 p-3 p-md-4">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="me-3">
                        <h2 class="fw-bold mb-1">${forum.forName}</h2>
                        <p class="mb-0 d-none d-md-block">${forum.forDes || ''}</p>
                    </div>
                    <div class="flex-shrink-0">
                        <button id="collect-forum-btn" class="btn ${buttonClass} btn-sm" data-forum-id="${forum.id}" data-is-collected="${isCollected}">
                            <i class="bi bi-heart-fill"></i> ${buttonText}
                        </button>
                    </div>
                </div>
                 <p class="mb-0 d-md-none mt-2">${forum.forDes || ''}</p>
            </div>
        </div>
    `;
    container.innerHTML = '';
    container.appendChild(headerContainer);
    addForumCollectButtonListener();
}

/**
 * 渲染文章列表。
 * @param {HTMLElement} container - 渲染文章的容器元素。
 * @param {Array<Object>} posts - 文章資料陣列。
 * @param {boolean} showPostActions - 此參數將被忽略，因為列表頁不應顯示操作按鈕。
 */
function renderPosts(container, posts, showPostActions = false) { // 預設為 false，且在列表頁將會忽略此參數
    const postsContainer = document.createElement('div');
    postsContainer.id = 'post-list-container';

    if (!posts || posts.length === 0) {
        postsContainer.innerHTML = '<div class="alert alert-info">目前沒有文章喔！</div>';
    } else {
        posts.forEach(post => {
            const fallbackImageUrl = '../../assets/img/categories/1.jpg';
            const postImageUrl = post.postImageUrl;
            const title = post.postTitle || "（無標題）";
            const content = post.postCon || "";
            const memberId = post.memberNo || post.memberId;
            const memberNickName = post.memberNickName || '匿名';
            const postUpdate = new Date(post.pcollUpdate || post.postUpdate).toLocaleString('zh-TW');
            const currentPostId = post.postNo || post.id;

            // 在文章列表頁，不顯示按讚/倒讚和收藏按鈕，只顯示統計數字
            const postStatsHtml = `
                <div class="post-stats d-flex align-items-center gap-2 text-muted">
                    <span title="留言數"><i class="bi bi-chat-dots-fill"></i> ${post.mesNumbers || 0}</span>
                    <span title="讚數"><i class="bi bi-hand-thumbs-up"></i> ${post.postLikeCount || 0}</span>
                    <span title="倒讚數"><i class="bi bi-hand-thumbs-down"></i> ${post.postLikeDlc || 0}</span>
                </div>
            `;

            const postCardHTML = `
                <div class="post-box d-flex mb-3" data-aos="fade-up" data-post-id="${currentPostId}">
                    <div class="card w-100">
                        <div class="card-body d-flex">
                            <div class="flex-shrink-0 me-3">
                                <img src="${postImageUrl}" alt="${title}" style="width: 120px; height: 120px; object-fit: contain; border-radius: 8px; background-color: #f0f0f0;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
                            </div>
                            <div class="flex-grow-1 d-flex flex-column">
                                <div>
                                    <h4 class="card-title fw-bold">
                                        <a href="#" class="post-link" data-post-id="${currentPostId}">${title}</a>
                                    </h4>
                                    <p class="card-text post-summary">${content.substring(0, 80)}...</p>
                                </div>
                                <div class="mt-auto d-flex justify-content-between align-items-center">
                                    <div class="d-flex align-items-center gap-2 text-muted">
                                        <img src="/images/memberAvatar/mem${memberId}.png"
                                             class="rounded-circle"
                                             alt="author avatar"
                                             style="width: 24px; height: 24px; object-fit: cover;"
                                             onerror="this.src='/images/memberAvatar/defaultmem.png'">
                                        <small>樓主: ${memberNickName}</small>
                                        <small class="ms-3">最後更新於: ${postUpdate}</small>
                                    </div>
                                    <div class="d-flex align-items-center gap-3">
                                        ${postStatsHtml}
                                        </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            postsContainer.innerHTML += postCardHTML;
        });
    }

    const existingPostsContainer = container.querySelector('#post-list-container');
    if (existingPostsContainer) {
        existingPostsContainer.innerHTML = '';
        Array.from(postsContainer.children).forEach(child => existingPostsContainer.appendChild(child));
    } else {
        container.appendChild(postsContainer);
    }
}

/**
 * 為討論區收藏按鈕綁定點擊事件。
 */
function addForumCollectButtonListener() {
    const collectBtn = document.getElementById('collect-forum-btn');
    if (!collectBtn) return;

    collectBtn.removeEventListener('click', handleForumCollect);
    collectBtn.addEventListener('click', handleForumCollect);
}

/**
 * 處理討論區收藏/取消收藏的邏輯
 */
async function handleForumCollect(event) {
    const button = event.currentTarget;
    const forumId = button.dataset.forumId;
    let isCollected = button.dataset.isCollected === 'true';

    const token = localStorage.getItem('jwt');
    if (!token) {
        alert('請先登入才能使用收藏功能。');
        return;
    }
    button.disabled = true;

    try {
        const response = await fetch(`/api/forums/${forumId}/collect`, {
            method: 'PUT',
            headers: {'Authorization': `Bearer ${token}`},
            body: JSON.stringify({})
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                alert('您的登入已過期或無效，請重新登入。');
            }
            throw new Error('API request failed');
        }
        const resultDTO = await response.json();
        const newStatus = resultDTO.collectStatus;

        if (newStatus === 'COLLECT') {
            button.innerHTML = '<i class="bi bi-heart-fill"></i> 已收藏';
            button.classList.replace('btn-outline-light', 'btn-primary');
            button.dataset.isCollected = 'true';
        } else {
            button.innerHTML = '<i class="bi bi-heart-fill"></i> 收藏';
            button.classList.replace('btn-primary', 'btn-outline-light');
            button.dataset.isCollected = 'false';
        }
    } catch (error) {
        console.error('收藏/取消收藏討論區操作失敗:', error);
        alert('操作失敗，請稍後再試。');
    } finally {
        button.disabled = false;
    }
}


/**
 * 為收藏文章按鈕綁定點擊事件。
 * 註：由於現在收藏狀態直接從 post 物件取得，這個函式在文章列表頁已不被呼叫。
 * 但在單篇文章詳情頁會直接綁定事件。
 */
function addPostCollectButtonListeners() {
    // 這個函式現在可能不再被直接呼叫，因為詳情頁的綁定是直接在 renderPostDetail 內部完成的。
    // 留著以防萬一或作為範例。
    document.querySelectorAll('.collect-post-btn').forEach(button => {
        button.removeEventListener('click', handlePostCollect);
        button.addEventListener('click', handlePostCollect);
    });
}

/**
 * 處理文章收藏/取消收藏的邏輯
 * 註：此函式現在更新的是單篇文章詳情頁的收藏按鈕狀態。
 */
async function handlePostCollect(event) {
    const button = event.currentTarget;
    const postId = button.dataset.postId;
    // let isCollected = button.dataset.isCollected === 'true'; // 不再從 data 屬性讀取初始狀態，因為狀態從後端更新後會重新渲染

    const token = localStorage.getItem('jwt');
    if (!token) {
        alert('請先登入才能使用收藏功能。');
        return;
    }

    button.disabled = true;

    try {
        const response = await fetch(`/api/posts/${postId}/collect`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({})
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                alert('您的登入已過期或無效，請重新登入。');
            }
            throw new Error('API request failed');
        }

        const resultDTO = await response.json();
        const newStatus = resultDTO.postCollectStatus;

        // 直接更新被點擊的按鈕狀態
        if (newStatus === 'COLLECT') {
            button.innerHTML = '<i class="bi bi-bookmark-heart-fill"></i> 已收藏';
            button.classList.replace('btn-outline-secondary', 'btn-primary');
            button.dataset.isCollected = 'true'; // 更新 data 屬性
        } else {
            button.innerHTML = '<i class="bi bi-bookmark-heart-fill"></i> 收藏';
            button.classList.replace('btn-primary', 'btn-outline-secondary');
            button.dataset.isCollected = 'false'; // 更新 data 屬性
        }

        // 優化：這裡不再重新載入整個列表，而是直接更新單篇文章的收藏狀態
        // 如果在文章列表頁，找到對應的文章卡片並更新其收藏按鈕狀態 (如果列表頁有顯示收藏按鈕的話)
        const postCard = document.querySelector(`.post-box[data-post-id="${postId}"]`);
        if (postCard) {
            const collectBtn = postCard.querySelector('.collect-post-btn');
            if (collectBtn) {
                if (newStatus === 'COLLECT') {
                    collectBtn.innerHTML = '<i class="bi bi-bookmark-heart-fill"></i> 已收藏';
                    collectBtn.classList.replace('btn-outline-secondary', 'btn-primary');
                    collectBtn.dataset.isCollected = 'true';
                } else {
                    collectBtn.innerHTML = '<i class="bi bi-bookmark-heart-fill"></i> 收藏';
                    collectBtn.classList.replace('btn-primary', 'btn-outline-secondary');
                    collectBtn.dataset.isCollected = 'false';
                }
            }
        }
        // 如果在單篇文章詳情頁，會自動更新，因為 renderPostDetail 會根據傳入的 isCollected 參數渲染按鈕狀態。
        // 當執行 handlePostCollect 後，通常會觸發 UI 重新渲染，例如重新呼叫 showPostDetailView，
        // 或者像上面這樣直接更新按鈕。這裡已經直接更新了按鈕，所以不用再呼叫 showPostDetailView。

    } catch (error) {
        console.error('收藏/取消收藏文章操作失敗:', error);
        alert('操作失敗，請稍後再試。');
    } finally {
        button.disabled = false;
    }
}


/**
 * 渲染文章主體內容
 * @param {Object} post - 文章資料
 * @param {string} memberPostLikeStatus - 當前會員對該文章的按讚狀態 ('LIKE', 'DISLIKE', 'NEUTRAL')
 * @param {boolean} isCollected - 當前會員是否收藏了該文章 (直接從 post 物件取得)
 */
function renderPostDetail(post, memberPostLikeStatus, isCollected) {
    const postDate = new Date(post.postCrdate).toLocaleString('zh-TW');
    const postImageUrl = post.postImageUrl;
    const fallbackImageUrl = '../../assets/img/categories/1.jpg';
    const memberId = post.memberId;
    const currentPostId = post.postNo || post.id; // 確保取得正確的 postId

    // 固定按讚和倒讚按鈕的樣式，不再根據會員個人狀態變化顏色
    const likeBtnClass = 'btn-outline-success'; // 固定為綠色外框
    const dislikeBtnClass = 'btn-outline-danger'; // 固定為紅色外框

    // 收藏按鈕樣式 (直接使用傳入的 isCollected 參數)
    const collectBtnClass = isCollected ? 'btn-primary' : 'btn-outline-secondary';
    const collectBtnText = isCollected ? '已收藏' : '收藏';

    const detailHTML = `
        <article class="post-content">
            <a href="#" class="btn btn-outline-secondary btn-sm mb-4 back-to-list-btn"><i class="bi bi-arrow-left"></i> 返回文章列表</a>
            <h1 class="display-6 fw-bold mb-3">${post.postTitle}</h1>
            <div class="d-flex align-items-center text-muted mb-4 gap-3">
                <div class="d-flex align-items-center gap-2">
                    <img src="/images/memberAvatar/mem${memberId}.png"
                         class="rounded-circle"
                         alt="author avatar"
                         style="width: 32px; height: 32px; object-fit: cover;"
                         onerror="this.src='/images/memberAvatar/defaultmem.png'">
                    <span>樓主：${post.memberNickName || '匿名'}</span>
                </div>
                <span class="mx-2">|</span>
                <span>發布時間：${postDate}</span>
            </div>

            <div class="d-flex justify-content-end align-items-center gap-3 mb-4">
                <button class="btn btn-lg ${likeBtnClass} post-like-btn" data-post-id="${currentPostId}" data-action="LIKE">
                    <i class="bi bi-hand-thumbs-up"></i> <span class="like-count">${post.postLikeCount || 0}</span>
                </button>
                <button class="btn btn-lg ${dislikeBtnClass} post-dislike-btn" data-post-id="${currentPostId}" data-action="DISLIKE">
                    <i class="bi bi-hand-thumbs-down"></i> <span class="dislike-count">${post.postLikeDlc || 0}</span>
                </button>
                <button class="btn btn-lg ${collectBtnClass} collect-post-btn"
                        data-post-id="${currentPostId}" data-is-collected="${isCollected}">
                    <i class="bi bi-bookmark-heart-fill"></i> ${collectBtnText}
                </button>
            </div>
            <img src="${postImageUrl}" class="img-fluid rounded post-detail-main-img" alt="${post.postTitle}" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
            <div class="fs-5 lh-lg">${post.postCon}</div>
        </article>
        <hr class="my-5">
        <div class="comments-section">
            <h4 class="mb-4"><i class="bi bi-chat-square-text-fill"></i> 留言</h4>
            <div id="add-comment-area" class="mb-4"></div>
            <div id="comments-list-container"></div>
        </div>
    `;
    dynamicContentContainer.innerHTML = detailHTML;

    // 確保單篇文章詳情頁的按讚/收藏按鈕事件正確綁定
    const postLikeButtons = dynamicContentContainer.querySelectorAll('.post-like-btn, .post-dislike-btn');
    postLikeButtons.forEach(button => {
        button.removeEventListener('click', handlePostLikeAction); // 避免重複綁定
        button.addEventListener('click', (event) => {
            const btn = event.currentTarget;
            handlePostLikeAction(btn.dataset.postId, btn.dataset.action, btn);
        });
    });

    const collectPostButton = dynamicContentContainer.querySelector('.collect-post-btn');
    if (collectPostButton) {
        collectPostButton.removeEventListener('click', handlePostCollect); // 避免重複綁定
        collectPostButton.addEventListener('click', handlePostCollect);
    }
}

/**
 * 載入並渲染留言列表
 */
async function loadAndRenderComments(postId) {
    const listContainer = document.getElementById('comments-list-container');
    const formContainer = document.getElementById('add-comment-area');
    listContainer.innerHTML = '<p>正在載入留言...</p>';

    setupCommentForm(formContainer, postId);

    try {
        const response = await fetch(`/api/posts/${postId}/messages`);
        if (!response.ok) throw new Error('獲取留言失敗');
        const comments = await response.json();

        if (!comments || comments.length === 0) {
            listContainer.innerHTML = '<div class="alert alert-secondary text-center">還沒有人留言，快來搶頭香！</div>';
            return;
        }

        listContainer.innerHTML = comments.map(comment => {
            const mesDate = new Date(comment.mesCrdate).toLocaleString('zh-TW');
            const memberId = comment.memberId;
            return `
                <div class="card mb-3" id="comment-${comment.id}">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <div class="d-flex gap-3 align-items-start">
                                <img src="/images/memberAvatar/mem${memberId}.png" alt="User Avatar"
                                     class="rounded-circle" width="50" height="50" style="object-fit: cover;"
                                     onerror="this.src='/images/memberAvatar/defaultmem.png'">
                                <div>
                                    <h6 class="card-title fw-bold mb-0">${comment.memberNickName || '匿名'}</h6>
                                    <small class="text-muted">${mesDate}</small>
                                </div>
                            </div>
                            <button class="btn btn-sm btn-outline-secondary report-btn"
                                    data-bs-toggle="modal" data-bs-target="#reportModal"
                                    data-comment-id="${comment.id}">
                                <i class="bi bi-flag"></i> 檢舉
                            </button>
                        </div>
                        <p class="card-text my-2">${comment.mesCon}</p>
                        <div class="comment-actions text-end">
                            <button class="btn btn-sm btn-outline-success like-btn" data-comment-id="${comment.id}" data-action="LIKE">
                                <i class="bi bi-hand-thumbs-up"></i> <span class="like-count">${comment.mesLikeLc || 0}</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger dislike-btn" data-comment-id="${comment.id}" data-action="DISLIKE">
                                <i class="bi bi-hand-thumbs-down"></i> <span class="dislike-count">${comment.mesLikeDlc || 0}</span>
                            </button>
                        </div>
                    </div>
                </div>`;
        }).join('');
    } catch (error) {
        console.error('載入留言出錯:', error);
        listContainer.innerHTML = `<div class="alert alert-danger">無法載入留言列表。</div>`;
    }
}

/**
 * 設定留言表單
 */
function setupCommentForm(container, postId) {
    const token = localStorage.getItem('jwt');
    if (!token) {
        container.innerHTML = `<div class="alert alert-info">您需要 <a href="/front-end/mem/MemberLogin.html">登入</a> 才能留言。</div>`;
        return;
    }

    const rawMemberInfo = localStorage.getItem('memberInfo');
    const memberInfo = JSON.parse(rawMemberInfo || '{}');
    const memberId = memberInfo.id;

    container.innerHTML = `
        <form id="comment-form" class="d-flex gap-3 align-items-start">
            <img src="/images/memberAvatar/mem${memberId}.png" alt="Your Avatar"
                 class="rounded-circle" width="50" height="50" style="object-fit: cover;"
                 onerror="this.src='/images/memberAvatar/defaultmem.png'">
            <div class="flex-grow-1">
                <textarea id="comment-content" class="form-control" rows="3" placeholder="輸入您的留言..." required></textarea>
                <div class="text-end mt-2">
                    <button type="submit" class="btn btn-primary">送出留言</button>
                </div>
            </div>
        </form>
    `;

    document.getElementById('comment-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const content = document.getElementById('comment-content').value;
        const submitButton = e.target.querySelector('button[type="submit"]');
        submitButton.disabled = true;
        submitButton.textContent = '傳送中...';

        try {
            const response = await fetch(`/api/posts/${postId}/messages/`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`},
                body: JSON.stringify({mesCon: content})
            });
            if (response.status !== 202) throw new Error('留言失敗');
            document.getElementById('comment-content').value = '';
            setTimeout(() => loadAndRenderComments(postId), 500);
        } catch (error) {
            alert('留言失敗，請稍後再試。');
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = '送出留言';
        }
    });
}

/**
 * 設定全域事件監聽器 (使用事件委派)
 */
function setupEventListeners() {
    dynamicContentContainer.addEventListener('click', (event) => {
        const target = event.target;
        const postLink = target.closest('.post-link');
        const backBtn = target.closest('.back-to-list-btn');

        // 留言的讚/倒讚按鈕
        const commentLikeBtn = target.closest('.like-btn, .dislike-btn');

        if (postLink) {
            event.preventDefault();
            const postId = postLink.dataset.postId;
            if (postId && postId !== 'undefined') {
                showPostDetailView(postId);
            } else {
                console.error('點擊文章連結時，postId 為 undefined 或無效。');
            }
        }

        // 處理留言按讚
        if (commentLikeBtn) {
            handleLikeAction(commentLikeBtn.dataset.commentId, commentLikeBtn.dataset.action, commentLikeBtn);
        }

        if (backBtn) {
            event.preventDefault();
            // 判斷是從哪個視圖返回
            const urlParams = new URLSearchParams(window.location.search);
            const forumId = urlParams.get('forumId');
            const view = urlParams.get('view');

            if (view === 'collected') {
                // 如果是從收藏頁進入單篇文章，返回到收藏列表
                showCollectedPostsView();
            } else if (forumId) {
                // 如果是從某個討論區進入單篇文章，返回到該討論區的文章列表
                showPostListView(forumId);
            } else {
                // 預設返回到初始頁面
                showInitialView();
            }
        }
    });

    const myCollectionsTab = document.getElementById('my-collections-tab');
    if (myCollectionsTab) {
        myCollectionsTab.addEventListener('click', (event) => {
            event.preventDefault();
            showCollectedPostsView();
        });
    }

    const reportModal = document.getElementById('reportModal');
    if (reportModal) {
        reportModal.addEventListener('show.bs.modal', (event) => {
            const button = event.relatedTarget;
            const commentId = button.dataset.commentId;
            reportModal.querySelector('#report-message-id').value = commentId;
        });
    }

    const reportForm = document.getElementById('report-form');
    if (reportForm) {
        reportForm.addEventListener('submit', handleReportSubmit);
    }
}

/**
 * 處理文章讚/倒讚的 API 請求與 UI 更新
 * @param {string} postId - 文章ID
 * @param {string} action - 'LIKE' 或 'DISLIKE' (這個參數決定了要發送給後端的狀態)
 * @param {HTMLElement} buttonElement - 被點擊的按鈕元素 (用於找到其父容器)
 */
async function handlePostLikeAction(postId, action, buttonElement) {
    console.log('--- 開始處理文章按讚/倒讚動作 ---'); // 偵錯日誌
    console.log('文章ID:', postId); // 偵錯日誌
    console.log('動作:', action); // 偵錯日誌

    const token = localStorage.getItem('jwt');
    if (!token) {
        alert('請先登入才能按讚喔！');
        console.log('錯誤: 未找到 JWT Token，已提示用戶登入。'); // 偵錯日誌
        return;
    }
    console.log('已獲取 JWT Token (前10字元):', token.substring(0, 10) + '...'); // 偵錯日誌

    const postContainer = buttonElement.closest('.post-box') || buttonElement.closest('.post-content');
    if (!postContainer) {
        console.error('錯誤: 找不到文章容器元素。'); // 偵錯日誌
        return;
    }

    // 禁用所有相關按鈕，防止重複點擊
    postContainer.querySelectorAll('.post-like-btn, .post-dislike-btn, .collect-post-btn').forEach(btn => btn.disabled = true);
    console.log('按鈕已禁用。'); // 偵錯日誌

    try {
        const requestBody = JSON.stringify({ pLikeStatus: action });
        const apiUrl = `/api/posts/${postId}/like`;
        console.log('發送請求到:', apiUrl); // 偵錯日誌
        console.log('請求方法:', 'POST'); // 偵錯日誌
        console.log('請求內容 (body):', requestBody); // 偵錯日誌

        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
                // 'Content-Type': 'multipart/form-data' 不需要手動設定，FormData 會自動設定
            },
            body: requestBody
        });

        console.log('接收到回應，狀態碼:', response.status, 'OK:', response.ok); // 偵錯日誌

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                alert('您的登入已過期或無效，請重新登入。');
                console.warn('警告: 登入狀態無效 (401/403)。'); // 偵錯日誌
            } else {
                alert('操作失敗，伺服器返回錯誤。');
                const errorText = await response.text(); // 嘗試獲取錯誤回應的文本
                console.error('錯誤: API 請求失敗，狀態碼:', response.status, '錯誤回應:', errorText); // 偵錯日誌
            }
            throw new Error('操作失敗');
        }

        console.log('API 請求成功。'); // 偵錯日誌
        // 成功後，重新載入文章詳細頁面以獲取最新數量
        // 這會觸發 showPostDetailView -> fetch /api/forumpost/{id} -> renderPostDetail
        // 確保顯示的讚/倒讚數量是最新的
        showPostDetailView(postId);
        console.log('已觸發文章詳情頁面重新載入以更新數量。'); // 偵錯日誌

    } catch (error) {
        console.error('捕獲到文章按讚/倒讚失敗的錯誤:', error); // 偵錯日誌
        alert(`操作失敗：${error.message || '未知錯誤'}`); // 顯示更詳細的錯誤訊息
    } finally {
        // 因為會重新載入頁面，所以這裡不需要手動啟用按鈕
        // 但如果 showPostDetailView 失敗，按鈕可能會保持禁用狀態，
        // 為了健壯性，可以考慮在這裡重新啟用，或在 showPostDetailView 的錯誤處理中處理。
        // 這裡暫時不變動，因為重新載入會重建按鈕。
        console.log('--- 文章按讚/倒讚動作處理結束 ---'); // 偵錯日誌
    }
}


/**
 * 處理"留言"讚/倒讚的 API 請求與 UI 更新
 */
async function handleLikeAction(commentId, action, buttonElement) {
    const token = localStorage.getItem('jwt');
    if (!token) {
        alert('請先登入才能按讚喔！');
        return;
    }
    buttonElement.disabled = true;

    try {
        const response = await fetch(`/api/posts/message/${commentId}/like`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`},
            body: JSON.stringify({fmlikeStatus: action})
        });
        if (!response.ok) throw new Error('操作失敗');

        // 成功後，只重新載入留言區塊
        const currentPostId = new URLSearchParams(window.location.search).get('postId');
        if (currentPostId) {
            loadAndRenderComments(currentPostId);
        }

    } catch (error) {
        console.error('按讚/倒讚失敗:', error);
        alert('操作失敗，請稍後再試。');
    } finally {
        // 因為留言區是局部刷新，所以不論成功失敗都應該在 finally 中 re-enable
        buttonElement.disabled = false;
    }
}

/**
 * 處理檢舉表單提交
 */
async function handleReportSubmit(event) {
    event.preventDefault();
    const token = localStorage.getItem('jwt');
    if (!token) {
        alert('請先登入！');
        return;
    }

    const form = event.target;
    const messageId = form.querySelector('#report-message-id').value;
    const reportTypeId = form.querySelector('#report-type').value;
    if (!reportTypeId) {
        alert('請選擇檢舉原因！');
        return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    try {
        const response = await fetch('/api/posts/message/report', {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`},
            body: JSON.stringify({
                messageNo: parseInt(messageId, 10),
                reportTypeNo: parseInt(reportTypeId, 10)
            })
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '檢舉提交失敗');
        }
        alert('檢舉已送出，感謝您的回報！');
        bootstrap.Modal.getInstance(document.getElementById('reportModal')).hide();
    } catch (error) {
        console.error('檢舉失敗:', error);
        alert(`檢舉失敗：${error.message}`);
    } finally {
        submitBtn.disabled = false;
    }
}

/**
 * 動態載入檢舉類型到 Modal
 */
async function populateReportTypes() {
    const selectElement = document.getElementById('report-type');
    if (!selectElement) return;

    try {
        const response = await fetch('/api/report-types');
        if (!response.ok) throw new Error('無法獲取檢舉類型');
        const reportTypes = await response.json();
        selectElement.innerHTML = '<option value="" selected disabled>請選擇檢舉原因...</option>';
        reportTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type.id;
            option.textContent = type.rpiType;
            selectElement.appendChild(option);
        });
    } catch (error) {
        console.error('載入檢舉類型失敗:', error);
        selectElement.innerHTML += '<option value="" disabled>無法載入選項</option>';
    }
}

/**
 * 顯示錯誤訊息
 */
function showError(container, message) {
    container.innerHTML = `<div class="alert alert-danger">${message}</div>`;
}

// --- 新增文章功能相關 ---

// 【重要】將這些變數移到 setupNewPostModal 外部，讓它們在整個模組中只被初始化一次。
let newPostModal = null;
let newPostForm = null;
let forumIdInput = null;
let tagSelect = null;
let imageUploadInput = null;
let imagePreview = null;
let submitBtn = null;
let imageUploadOptionRadio = null;
let forumTagsCache = []; // 用於儲存文章類別資料，避免重複獲取
let currentForumIdForModal = null; // 用於儲存當前發文的討論區 ID

/**
 * 設定新增文章 Modal
 *
 * @param {Array<Object>} [forumTags=[]] - 文章類別資料陣列 (只在第一次載入時傳入)
 * @param {string} [currentForumId=null] - 當前討論區 ID (只在第一次載入時傳入)
 */
async function setupNewPostModal() {
    let modalContainer = document.getElementById('newPostModalContainer');

    // 如果 Modal 元素不存在，則動態創建它
    if (!newPostModal) { // 變數名稱 `newPostModal` 不可變，因為其指涉 DOM 元素
        if (!modalContainer) { // 如果連容器都沒有，則先創建容器
            modalContainer = document.createElement('div');
            modalContainer.id = 'newPostModalContainer';
            document.body.appendChild(modalContainer);
        }
        // 更新 Modal 的 HTML 結構，移除「使用預設圖片」的 radio button 和下拉選單
        const modalHtml = `
            <div class="modal fade" id="newPostModal" tabindex="-1" aria-labelledby="newPostModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="newPostModalLabel">新增文章</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <form id="new-post-form" enctype="multipart/form-data">
                                <input type="hidden" id="new-post-forum-id" name="forNoId">

                                <div class="mb-3">
                                    <label for="new-post-title" class="form-label">文章標題 <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="new-post-title" name="postTitle" required maxlength="50">
                                    <div class="form-text text-muted">標題長度限制50字。</div>
                                </div>

                                <div class="mb-3">
                                    <label for="new-post-tag" class="form-label">文章類別 <span class="text-danger">*</span></label>
                                    <select class="form-select" id="new-post-tag" name="ftagNoId" required>
                                        <option value="" selected disabled>請選擇文章類別...</option>
                                    </select>
                                </div>

                                <div class="mb-3">
                                    <label for="new-post-content" class="form-label">文章內容 <span class="text-danger">*</span></label>
                                    <textarea class="form-control" id="new-post-content" name="postCon" rows="10" required minlength="10" maxlength="5000"></textarea>
                                    <div class="form-text text-muted">內容長度限制10到5000字。</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label d-block">文章封面圖片</label>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" name="imageOption" id="image-upload-option" value="upload" checked>
                                        <label class="form-check-label" for="image-upload-option">自訂圖片</label>
                                    </div>
                                    <div id="image-upload-area" class="mt-2">
                                        <input class="form-control" type="file" id="new-post-image-upload" name="imageFile" accept="image/*">
                                        <div class="form-text text-muted">建議圖片比例為1:1，大小不超過2MB。</div>
                                    </div>

                                    <div class="mt-3 text-center">
                                        <img id="new-post-image-preview" src="" alt="圖片預覽" class="img-fluid rounded" style="max-width: 200px; max-height: 200px; object-fit: contain; border: 1px solid #ddd; display: none;">
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                    <button type="submit" class="btn btn-primary" id="submit-new-post-btn">送出文章</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        `;
        modalContainer.insertAdjacentHTML('beforeend', modalHtml);
        newPostModal = document.getElementById('newPostModal'); // 重新獲取新創建的 Modal 元素

        // 【修正點 4】在 Modal 首次創建時，就獲取這些 DOM 元素並綁定事件
        newPostForm = document.getElementById('new-post-form');
        forumIdInput = document.getElementById('new-post-forum-id');
        tagSelect = document.getElementById('new-post-tag');
        imageUploadInput = document.getElementById('new-post-image-upload');
        imagePreview = document.getElementById('new-post-image-preview');
        submitBtn = document.getElementById('submit-new-post-btn');
        imageUploadOptionRadio = document.getElementById('image-upload-option');

        // 監聽圖片選項切換 (現在只有「自訂圖片」一個選項)
        imageUploadOptionRadio.addEventListener('change', toggleImageOptions);

        // 圖片預覽功能 (上傳自訂圖片)
        imageUploadInput.addEventListener('change', (event) => {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    imagePreview.src = e.target.result;
                    imagePreview.style.display = 'block';
                };
                reader.readAsDataURL(file);
            } else {
                imagePreview.src = '';
                imagePreview.style.display = 'none';
            }
        });

        // 監聽文章類別下拉選單的變化
        tagSelect.addEventListener('change', updateImagePreviewBasedOnCategory); // 即使只有自訂圖片選項，選了類別後也應該清除預覽

        // 處理表單提交，只綁定一次
        newPostForm.addEventListener('submit', handleNewPostSubmit);

        // 【修正點 5】將 Modal 顯示時的邏輯移到單獨的監聽器中
        newPostModal.addEventListener('show.bs.modal', async (event) => {
            const button = event.relatedTarget; // 觸發 Modal 的按鈕
            const forumId = button.dataset.forumId;
            currentForumIdForModal = forumId; // 儲存當前討論區 ID
            forumIdInput.value = forumId;

            // 每次 Modal 顯示時都重置表單
            newPostForm.reset();
            imagePreview.src = ''; // 清空圖片預覽
            imagePreview.style.display = 'none';
            imageUploadInput.value = ''; // 清空檔案選擇

            // 確保「自訂圖片」選項被選中
            imageUploadOptionRadio.checked = true;
            toggleImageOptions(); // 執行圖片選項切換邏輯

            // 每次 Modal 顯示時都重新獲取文章類別資料並填充下拉選單
            await fetchForumTagsForModal();
        });
    }
}

/**
 * 處理圖片選項切換
 */
function toggleImageOptions() {
    const uploadArea = document.getElementById('image-upload-area');
    const isCustomUploadSelected = imageUploadOptionRadio.checked;

    if (isCustomUploadSelected) {
        uploadArea.style.display = 'block';
        imagePreview.src = ''; // 清空預覽
        imagePreview.style.display = 'none'; // 隱藏預覽
        imageUploadInput.required = false; // 上傳模式下圖片不強制必填，讓後端處理
        imageUploadInput.value = ''; // 清空檔案選擇
    } else {
        uploadArea.style.display = 'none';
        imageUploadInput.required = false;
        imageUploadInput.value = '';
        // 如果未來又加回預設圖片選項，這裡需要調用 updateImagePreviewBasedOnCategory();
    }
}

/**
 * 根據選定的文章類別 ID 獲取並顯示預設圖片
 */
async function updateImagePreviewBasedOnCategory() {
    const selectedTagId = tagSelect.value;
    if (selectedTagId && !imageUploadOptionRadio.checked) { // 只有當未選擇自訂圖片時才更新預覽
        try {
            // 呼叫後端 API 獲取預設圖片 URL
            const response = await fetch(`/api/forumtag/default-image/${selectedTagId}`);
            if (!response.ok) {
                console.error(`無法獲取預設圖片URL，狀態碼: ${response.status}`);
                imagePreview.style.display = 'none';
                imagePreview.src = '';
                return;
            }
            const imageUrl = await response.text(); // 預期返回純文字的 URL
            imagePreview.src = imageUrl;
            imagePreview.style.display = 'block';
        } catch (error) {
            console.error('載入預設圖片失敗:', error);
            imagePreview.style.display = 'none';
            imagePreview.src = ''; // 清除圖片源
        }
    } else {
        imagePreview.src = '';
        imagePreview.style.display = 'none';
    }
}

/**
 * 獲取並填充文章類別下拉選單
 */
async function fetchForumTagsForModal() {
    try {
        const token = localStorage.getItem('jwt');
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        const response = await fetch(`/api/forumtag`, {headers: headers});
        if (!response.ok) throw new Error('獲取文章類別資料失敗');
        const forumTags = await response.json();

        // 填充文章類別下拉選單
        tagSelect.innerHTML = '<option value="" selected disabled>請選擇文章類別...</option>';
        forumTags.forEach(tag => {
            const option = document.createElement('option');
            option.value = tag.id;
            option.textContent = tag.ftagName;
            tagSelect.appendChild(option);
        });
        forumTagsCache = forumTags; // 緩存資料
    } catch (error) {
        console.error('載入文章類別失敗:', error);
        tagSelect.innerHTML = '<option value="" disabled>無法載入選項</option>';
    }
}


/**
 * 處理新增文章表單提交
 */
async function handleNewPostSubmit(e) { // 變數名稱 `handleNewPostSubmit` 可變
    e.preventDefault();

    const token = localStorage.getItem('jwt');
    if (!token) {
        alert('請先登入才能新增文章！');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = '送出中...';

    const formData = new FormData();
    const forumPostUpdateDTO = {
        forNoId: parseInt(forumIdInput.value, 10),
        ftagNoId: parseInt(tagSelect.value, 10),
        postTitle: document.getElementById('new-post-title').value,
        postCon: document.getElementById('new-post-content').value,
        // 這裡不傳遞 postPin 和 postStatus，讓後端 Service 層設定預設值
    };

    // 將 DTO 轉換為 JSON 字串並添加到 FormData
    formData.append('forumPostUpdate', new Blob([JSON.stringify(forumPostUpdateDTO)], { type: 'application/json' }));

    // 處理圖片檔案
    const imageFile = imageUploadInput.files[0];

    // 根據 radio button 的選擇處理圖片邏輯
    if (imageUploadOptionRadio.checked) { // 選擇了「自訂圖片」
        if (imageFile) {
            formData.append('imageFile', imageFile);
        } else {
            // 如果選擇了自訂圖片但未上傳檔案，則明確傳遞 defaultImageUrl 為空字串，讓後端根據 ftagNoId 處理
            formData.append('defaultImageUrl', '');
        }
    } else { // 實際上現在只有一個選項，但保留彈性
        // 如果沒有選擇自訂圖片 (理論上這種情況不會發生，除非前端邏輯改變)
        // 則嘗試根據 ftagNoId 獲取預設圖片 URL，並作為 defaultImageUrl 傳遞
        // 如果沒有選擇類別，這裡也會是空字串，後端會處理
        const selectedTagId = tagSelect.value;
        const defaultImageUrl = selectedTagId ? await getCategoryDefaultImageUrlFrontend(selectedTagId) : ''; // 前端輔助函數
        formData.append('defaultImageUrl', defaultImageUrl);
    }


    try {
        const response = await fetch('/api/forumpost/insert', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
                // 'Content-Type': 'multipart/form-data' 不需要手動設定，FormData 會自動設定
            },
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            let errorMessage = '新增文章失敗';
            if (errorData.message) {
                errorMessage = errorData.message;
            } else if (errorData.details) {
                errorMessage += `: ${errorData.details}`;
            } else if (typeof errorData === 'object') {
                // 如果是驗證錯誤，會有多個欄位錯誤
                const fieldErrors = Object.values(errorData).join('\n');
                errorMessage = `表單驗證失敗:\n${fieldErrors}`;
            }
            throw new Error(errorMessage);
        }

        alert('文章新增成功！');
        bootstrap.Modal.getInstance(newPostModal).hide(); // 關閉 Modal
        showPostListView(currentForumIdForModal); // 重新載入文章列表以顯示新文章

    } catch (error) {
        console.error('新增文章失敗:', error);
        alert(`新增文章失敗：${error.message}`);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = '送出文章';
    }
}

/**
 * 【新增輔助函式】在前端模擬 Service 層的 getCategoryDefaultImageUrl
 * 從緩存的 forumTagsCache 中找到對應的預設圖片 URL
 */
async function getCategoryDefaultImageUrlFrontend(categoryId) {
    // 這裡可以直接從後端獲取，或者如果 `forumTagsCache` 已經包含了這些資訊，可以直接從緩存中取
    // 為了精確匹配後端邏輯，建議還是透過 API 獲取
    try {
        const response = await fetch(`/api/forumtag/default-image/${categoryId}`);
        if (!response.ok) {
            console.error(`無法獲取類別預設圖片URL: ${response.status}`);
            return ''; // 返回空字串或預設的 fallback URL
        }
        return await response.text();
    } catch (error) {
        console.error('獲取類別預設圖片URL失敗:', error);
        return '';
    }
}