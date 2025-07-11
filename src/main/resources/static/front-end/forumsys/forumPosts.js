// front-end/forumsys/forumPosts.js

document.addEventListener('DOMContentLoaded', () => {
    // 1. 從 URL 取得討論區 ID (邏輯不變)
    const urlParams = new URLSearchParams(window.location.search);
    const forumId = urlParams.get('id');

    const headerContainer = document.getElementById('forum-header');
    const postsContainer = document.getElementById('post-list-container');
    const errorContainer = document.getElementById('error-message');

    if (!forumId) {
        showError("錯誤：缺少討論區 ID。");
        return;
    }

    loadForumData(forumId);

    async function loadForumData(id) {
        try {
            // 2. Fetch API 的路徑是絕對路徑，從網域根目錄開始，所以不受當前 HTML 位置影響，不需修改。
            const [forumRes, postsRes] = await Promise.all([
                fetch(`/api/forum/${id}`),
                fetch(`/api/forum/${id}/posts`)
            ]);

            if (!forumRes.ok) throw new Error(`無法載入討論區資訊 (ID: ${id})`);
            if (!postsRes.ok) throw new Error(`無法載入文章列表 (ID: ${id})`);

            const forum = await forumRes.json();
            const posts = await postsRes.json();

            renderForumHeader(forum);
            renderPosts(posts);

        } catch (error) {
            console.error('載入資料時發生錯誤:', error);
            showError(error.message);
        }
    }

    function renderForumHeader(forum) {
        headerContainer.innerHTML = `
            <div class="p-4 rounded" style="background-color: var(--bs-gray-100);">
                <h1 class="fw-bold">${forum.forName}</h1>
                <p class="lead">${forum.forDes}</p>
                <a href="forumindex.html" class="btn btn-outline-secondary btn-sm">
                    <i class="bi bi-arrow-left"></i> 返回首頁
                </a>
            </div>
        `;
    }

    function renderPosts(posts) {
        if (!posts || posts.length === 0) {
            postsContainer.innerHTML = '<div class="alert alert-info">這個討論區還沒有文章喔！</div>';
            return;
        }

        postsContainer.innerHTML = '';
        posts.forEach(post => {
            const fallbackImage = `../../assets/img/categories/1.jpg`;
            const postImage = `/api/forumpost/image/${post.id}`;

            // 【【此處為修改重點】】
            const postCard = `
            <div class="post-box d-flex mb-3" data-aos="fade-up">
                <div class="card w-100">
                    <div class="card-body">
                       <div class="d-flex">
                            <div class="flex-shrink-0 me-3">
                                <img src="${postImage}" 
                                     alt="${post.postTitle}" 
                                     style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px;"
                                     onerror="this.onerror=null;this.src='${fallbackImage}';">
                            </div>
                            <div class="flex-grow-1">
                                <h4 class="card-title fw-bold">
                                    <a href="#" class="text-decoration-none">${post.postTitle}</a>
                                </h4>
                                <p class="card-text text-muted">${(post.postCon || '').substring(0, 100)}...</p>
                                
                                {/* */}
                                <div class="d-flex justify-content-between align-items-center mt-3">
                                    <small class="text-muted">
                                        作者：${post.memberName || '匿名'} | 發表於：${new Date(post.postCrdate).toLocaleString()}
                                    </small>
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
                                {/* */}
                                
                            </div>
                       </div>
                    </div>
                </div>
            </div>
        `;
            postsContainer.innerHTML += postCard;
        });

        AOS.init({once: true});
    }

    function showError(message) {
        postsContainer.style.display = 'none';
        headerContainer.style.display = 'none';
        errorContainer.textContent = message;
        errorContainer.style.display = 'block';
    }
});