// front-end/forumsys/allForum.js (高亮功能版)

/**
 * 根據傳入的 forumId，抓取文章並顯示在右欄
 * @param {string} forumId - 討論區的 ID
 */
async function showPostListView(forumId) {
    const rightContainer = document.getElementById('dynamic-content-container');
    if (!rightContainer) {
        console.error("錯誤：找不到右欄容器 #dynamic-content-container！");
        return;
    }

    rightContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>`;
    // 桌面版才平滑滾動，避免在小螢幕上行為怪異
    if (window.innerWidth > 992) {
        window.scrollTo({top: 0, behavior: 'smooth'});
    }

    try {
        const [forumRes, postsRes] = await Promise.all([
            fetch(`/api/forum/${forumId}`),
            fetch(`/api/forum/${forumId}/posts`)
        ]);

        if (!forumRes.ok || !postsRes.ok) {
            throw new Error('抓取討論區或文章資料失敗');
        }

        const forum = await forumRes.json();
        const posts = await postsRes.json();

        renderForumHeader(rightContainer, forum);
        renderPosts(rightContainer, posts);

        setTimeout(() => {
            rightContainer.querySelectorAll('[data-aos]').forEach(el => el.classList.add('aos-animate'));
        }, 100);

    } catch (error) {
        showError(rightContainer, `無法載入文章列表 (ID: ${forumId})`);
        console.error(error);
    }
}

// ... renderForumHeader, renderPosts, showError 函式保持不變 (此處省略以保持簡潔) ...
function renderForumHeader(container, forum) {
    const headerContainer = document.createElement('div');
    const imageUrl = forum.forImgUrl || '../assets/img/categories/1.jpg';
    const fallbackImageUrl = '../assets/img/categories/1.jpg';

    headerContainer.innerHTML = `
        <div class="position-relative text-white mb-4" style="width: 100%; height: 200px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            <img src="${imageUrl}" alt="${forum.forName}" class="w-100 h-100" style="object-fit: cover;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
            <div class="position-absolute top-0 start-0 w-100 h-100" style="background: linear-gradient(to top, rgba(0,0,0,0.7), rgba(0,0,0,0.1));"></div>
            <div class="position-absolute bottom-0 start-0 p-4">
                <h2 class="fw-bold">${forum.forName}</h2>
                <p class="mb-0">${forum.forDes}</p>
            </div>
        </div>
    `;
    container.innerHTML = '';
    container.appendChild(headerContainer);
}

function renderPosts(container, posts) {
    const postsContainer = document.createElement('div');
    postsContainer.id = 'post-list-container';

    if (!posts || posts.length === 0) {
        postsContainer.innerHTML = '<div class="alert alert-info">這個討論區還沒有文章喔！</div>';
        container.appendChild(postsContainer);
        return;
    }

    posts.forEach(post => {
        const fallbackImageUrl = '../assets/img/categories/1.jpg';
        const postImageUrl = `/api/forumpost/image/${post.id}`;
        const title = post.postTitle || "（無標題）";
        const content = post.postCon || "";

        // 【【此處為修改重點】】
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
                                    <span class="me-3" title="留言數">
                                        <i class="bi bi-chat-dots-fill"></i> 
                                        ${post.mesNumbers || 0}
                                    </span>
                                    <span class="me-3" title="喜歡">
                                        <i class="bi bi-hand-thumbs-up-fill text-success"></i> 
                                        ${post.postLikeCount || 0}
                                    </span>
                                    <span title="不喜歡">
                                        <i class="bi bi-hand-thumbs-down-fill text-danger"></i> 
                                        ${post.postLikeDlc || 0}
                                    </span>
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

function showError(container, message) {
    container.innerHTML = `<div class="alert alert-danger">${message}</div>`;
}


/**
 * 【【核心修改點】】
 * 事件監聽器中加入了處理 'active' class 的邏輯
 */
document.addEventListener('click', function (e) {
    const link = e.target.closest('.forum-link');
    if (link) {
        e.preventDefault();
        const forumId = link.dataset.forumId;

        if (forumId) {
            // --- 新增的 Highlight 邏輯 ---
            // 1. 找到所有在左欄的討論區項目
            const allForumItems = document.querySelectorAll('#hot-forum-list-container .forum-link');
            // 2. 移除所有項目上的 'active' class
            allForumItems.forEach(item => item.classList.remove('active'));
            // 3. 只在被點擊的項目上加上 'active' class
            link.classList.add('active');
            // --- Highlight 邏輯結束 ---

            showPostListView(forumId);
        }
    }
});