// allForum.js (SPA功能 + 整合聊天室 最終版)

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

    if (postId) {
        showPostDetailView(postId);
    } else {
        showInitialView();
    }

    window.addEventListener('popstate', handlePopState);
    setupEventListeners();
});

/**
 * 處理瀏覽器上/下一頁的事件
 */
function handlePopState(event) {
    const params = new URLSearchParams(window.location.search);
    const postId = params.get('postId');

    if (postId) {
        showPostDetailView(postId);
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
    // 可變：變數名稱 forumId 可自行命名，代表傳入的討論區ID。
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

        const [forumRes, postsRes] = await Promise.all([
            fetch(`/api/forum/${forumId}`, {headers: headers}),
            fetch(`/api/forum/${forumId}/posts`)
        ]);

        if (!forumRes.ok || !postsRes.ok) {
            throw new Error('抓取討論區或文章資料失敗');
        }

        const forum = await forumRes.json();
        const posts = await postsRes.json();

        // 【主要修改區域】
        // 1. 首先渲染 Header
        renderForumHeader(dynamicContentContainer, forum);

        // 2. 接著渲染聊天室 (從底部移到此處)
        // 2.1. 每次切換討論區，都先嘗試斷開舊的聊天連線
        if (window.chatManager && typeof window.chatManager.disconnect === 'function') {
            window.chatManager.disconnect();
        }

        // 2.2. 建立聊天室的 UI 容器並插入頁面
        const chatRoomContainer = document.createElement('div');
        chatRoomContainer.id = 'chat-room-container';
        // 【註解】此處 className 從 'mt-5' 改為 'mb-4'，讓聊天室和下方文章列表間距更協調
        chatRoomContainer.className = 'mb-4';
        chatRoomContainer.innerHTML = `
            <div class="chat-room card">
                <div class="card-header bg-light d-flex justify-content-between align-items-center">
                    <h5 class="mb-0" ><i class="bi bi-chat-dots-fill me-2"></i>${forum.forName}-即時聊天室</h5>
                    <span id="chat-status" class="badge bg-secondary">未連接</span>
                </div>
                <div class="card-body">
                    <div id="chat-messages" class="chat-messages mb-3">
                        </div>
                    <form id="chat-form" class="d-flex gap-2">
                        <input type="text" id="chat-message-input" class="form-control" placeholder="輸入訊息..." autocomplete="off" disabled>
                        <button type="submit" class="btn btn-primary" disabled><i class="bi bi-send-fill"></i></button>
                    </form>
                </div>
            </div>
        `;
        dynamicContentContainer.appendChild(chatRoomContainer);

        // 2.3. 檢查登入狀態並連接到新的聊天室
        const memberInfo = JSON.parse(localStorage.getItem('memberInfo') || '{}');
        if (token && memberInfo.memNickName && window.chatManager) {
            window.chatManager.connect(forumId, memberInfo.memNickName, memberInfo.id);
        } else {
            const chatMessagesDiv = document.getElementById('chat-messages');
            if (chatMessagesDiv) {
                chatMessagesDiv.innerHTML = '<p class="text-muted text-center p-3">您需要<a href="/front-end/mem/MemberLogin.html">登入</a>才能使用聊天室。</p>';
            }
        }

        // 3. 最後渲染文章列表
        renderPosts(dynamicContentContainer, posts);

        // 更新瀏覽器歷史紀錄
        history.pushState({forumId: forumId}, ``, `?forumId=${forumId}`);

        // 初始化 AOS 動畫
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
    // 可變：變數名稱 postId 可自行命名。
    dynamicContentContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border" role="status"></div></div>`;

    try {
        const postRes = await fetch(`/api/forumpost/${postId}`);
        if (!postRes.ok) throw new Error('文章載入失敗');
        const post = await postRes.json();

        renderPostDetail(post);
        await loadAndRenderComments(postId);

        history.pushState({postId: postId}, ``, `?postId=${postId}`);

    } catch (error) {
        console.error('載入文章詳細頁出錯:', error);
        showError(dynamicContentContainer, '載入文章時發生錯誤。');
    }
}

// --- 組件渲染輔助函式 ---

/**
 * 渲染帶有橫幅圖片、標題、描述以及收藏按鈕的討論區頂部。
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
                        <button id="collect-btn" class="btn ${buttonClass} btn-sm">
                            <i class="bi bi-heart-fill"></i> ${buttonText}
                        </button>
                    </div>
                </div>
                 <p class="mb-0 d-md-none mt-2">${forum.forDes || ''}</p>
            </div>
        </div>
    `;
    container.innerHTML = ''; // 先清空
    container.appendChild(headerContainer);
    addCollectButtonListener(forum.id);
}

/**
 * 渲染文章列表。
 */
function renderPosts(container, posts) {
    const postsContainer = document.createElement('div');
    postsContainer.id = 'post-list-container';

    if (!posts || posts.length === 0) {
        postsContainer.innerHTML = '<div class="alert alert-info">這個討論區還沒有文章喔！</div>';
    } else {
        posts.forEach(post => {
            const fallbackImageUrl = '../../assets/img/categories/1.jpg';
            const postImageUrl = `/api/forumpost/image/${post.id}`;
            const title = post.postTitle || "（無標題）";
            const content = post.postCon || "";
            const memberId = post.memberId;

            const postCardHTML = `
                <div class="post-box d-flex mb-3" data-aos="fade-up">
                    <div class="card w-100">
                        <div class="card-body d-flex">
                            <div class="flex-shrink-0 me-3">
                                <img src="${postImageUrl}" alt="${title}" style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
                            </div>
                            <div class="flex-grow-1 d-flex flex-column">
                                <div>
                                    <h4 class="card-title fw-bold">
                                        <a href="#" class="post-link" data-post-id="${post.id}">${title}</a>
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
                                        <small>樓主: ${post.memberNickName || '匿名'}</small> 
                                    </div>
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
    }
    container.appendChild(postsContainer);
}


/**
 * 為收藏按鈕綁定點擊事件。
 */
function addCollectButtonListener(forumId) {
    const collectBtn = document.getElementById('collect-btn');
    if (!collectBtn) return;

    collectBtn.addEventListener('click', async () => {
        collectBtn.disabled = true;
        try {
            const token = localStorage.getItem('jwt');
            if (!token) {
                alert('請先登入才能使用收藏功能。');
                return;
            }
            const response = await fetch(`/api/forums/${forumId}/collect`, {
                method: 'PUT',
                headers: {'Authorization': `Bearer ${token}`}
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    alert('您的登入已過期或無效，請重新登入。');
                }
                throw new Error('API request failed');
            }
            const resultDTO = await response.json();
            if (resultDTO.collectStatus === 'COLLECT') {
                collectBtn.innerHTML = '<i class="bi bi-heart-fill"></i> 已收藏';
                collectBtn.classList.replace('btn-outline-light', 'btn-primary');
            } else {
                collectBtn.innerHTML = '<i class="bi bi-heart-fill"></i> 收藏';
                collectBtn.classList.replace('btn-primary', 'btn-outline-light');
            }
        } catch (error) {
            console.error('收藏/取消收藏操作失敗:', error);
        } finally {
            collectBtn.disabled = false;
        }
    });
}


/**
 * 渲染文章主體內容
 */
function renderPostDetail(post) {
    const postDate = new Date(post.postCrDate).toLocaleString('zh-TW');
    const postImageUrl = `/api/forumpost/image/${post.id}`;
    const fallbackImageUrl = '../../assets/img/categories/1.jpg';
    const memberId = post.memberId;

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
             <img src="${postImageUrl}" class="img-fluid rounded mb-4" alt="${post.postTitle}" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
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
        const likeBtn = target.closest('.like-btn, .dislike-btn');
        const backBtn = target.closest('.back-to-list-btn');

        if (postLink) {
            event.preventDefault();
            showPostDetailView(postLink.dataset.postId);
        }

        if (likeBtn) {
            handleLikeAction(likeBtn.dataset.commentId, likeBtn.dataset.action, likeBtn);
        }

        if (backBtn) {
            event.preventDefault();
            window.history.back();
        }
    });

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
 * 處理讚/倒讚的 API 請求與 UI 更新
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

        const currentPostId = new URLSearchParams(window.location.search).get('postId');
        if (currentPostId) {
            loadAndRenderComments(currentPostId);
        }

    } catch (error) {
        console.error('按讚/倒讚失敗:', error);
        alert('操作失敗，請稍後再試。');
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