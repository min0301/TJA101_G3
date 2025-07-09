// front-end/forumsys/allForum.js

// 這是唯一的動態內容容器，從主頁面取得
const dynamicContainer = document.getElementById('dynamic-content-container');

/**
 * 顯示「討論區列表」視圖
 */
async function showForumListView() {
    try {
        const listTemplate = await fetch('front-end/forumsys/forum-list.html').then(res => res.text());
        dynamicContainer.innerHTML = listTemplate;

        // 【【【 修改程式碼 START 】】】
        // 直接將整個視窗滾動到最頂部 (座標 0)
        window.scrollTo({ top: 0, behavior: 'smooth' });
        // 【【【 修改程式碼 END 】】】

        await renderForumTable();

    } catch (error) {
        showError('無法載入討論區介面。');
        console.error(error);
    }
}

/**
 * 顯示特定討論區的「文章列表」視圖
 */
async function showPostListView(forumId) {
    try {
        const postTemplate = await fetch('front-end/forumsys/forum-posts-template.html').then(res => res.text());
        dynamicContainer.innerHTML = postTemplate;

        // 【【【 修改程式碼 START 】】】
        // 同樣地，直接將整個視窗滾動到最頂部
        window.scrollTo({ top: 0, behavior: 'smooth' });
        // 【【【 修改程式碼 END 】】】

        const [forum, posts] = await Promise.all([
            fetch(`/api/forum/${forumId}`).then(res => res.json()),
            fetch(`/api/forum/${forumId}/posts`).then(res => res.json())
        ]);

        renderForumHeader(forum);
        renderPosts(posts);

    } catch (error) {
        showError(`無法載入文章列表 (ID: ${forumId})`);
        console.error(error);
    }
}

// ===================================================================
// 渲染函式 (以下部分無需修改，保持原樣即可)
// ===================================================================

function renderForumHeader(forum) {
    const headerContainer = dynamicContainer.querySelector('#forum-header');
    const imageUrl = forum.forImgUrl || 'assets/img/categories/1.jpg';
    const fallbackImageUrl = 'assets/img/categories/1.jpg';
    headerContainer.innerHTML = `
        <div class="position-relative text-white" style="width: 100%; height: 250px; border-radius: 12px; overflow: hidden; margin-bottom: 1.5rem; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            <img src="${imageUrl}" alt="${forum.forName}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
            <div class="position-absolute top-0 start-0 w-100 h-100" style="background: linear-gradient(to top, rgba(0,0,0,0.7), rgba(0,0,0,0.1));"></div>
            <div class="position-absolute bottom-0 start-0 p-4">
                <h1 class="fw-bold">${forum.forName}</h1>
                <p class="mb-0">${forum.forDes}</p>
            </div>
            <button id="back-to-forums-btn" class="btn btn-light btn-sm position-absolute top-0 end-0 m-3">
                <i class="bi bi-arrow-left"></i> 返回列表
            </button>
        </div>
    `;
}

async function renderForumTable() {
    const postsContainer = dynamicContainer.querySelector('#forum-list-posts');
    const res = await fetch('/api/forums');
    const forumList = await res.json();
    postsContainer.innerHTML = '';
    forumList.forEach(forum => {
        const postBox = document.createElement('div');
        postBox.className = 'post-box d-flex forum-link';
        postBox.style.cursor = 'pointer';
        postBox.dataset.forumId = forum.id;
        postBox.innerHTML = `
            <div class="forum-img-wrap" style="min-width:220px;max-width:220px;">
                <img src="${forum.forImgUrl || 'assets/img/categories/1.jpg'}" alt="forum" style="width:100%;height:100px;object-fit:cover;border-radius:8px;">
            </div>
            <div class="card flex-grow-1 border-0">
                <div class="card-body py-2 px-3">
                    <h5 class="mb-0 fw-bold" style="font-size:1.3rem;">${forum.forName}</h5>
                    <div class="mb-1 text-muted" style="font-size:1rem;">${forum.forDes || ''}</div>
                </div>
            </div>
        `;
        postsContainer.appendChild(postBox);
    });
}

function renderPosts(posts) {
    const postsContainer = dynamicContainer.querySelector('#post-list-container');
    if (!posts || posts.length === 0) {
        postsContainer.innerHTML = '<div class="alert alert-info">這個討論區還沒有文章喔！</div>';
        return;
    }
    postsContainer.innerHTML = '';
    posts.forEach(post => {
        const postCard = `
            <div class="post-box d-flex mb-3" data-aos="fade-up">
                <div class="card w-100">
                    <div class="card-body d-flex">
                         <div class="flex-shrink-0 me-3">
                            <img src="/api/forumpost/image/${post.id}" alt="${post.postTitle}" style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px;" onerror="this.onerror=null;this.src='assets/img/categories/1.jpg';">
                        </div>
                        <div class="flex-grow-1">
                            <h4 class="card-title fw-bold"><a>${post.postTitle}</a></h4>
                            <p class="card-text text-muted">${(post.postCon || '').substring(0, 100)}...</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        postsContainer.innerHTML += postCard;
    });
    if (AOS) AOS.refresh();
}

dynamicContainer.addEventListener('click', function(e) {
    if (e.target.id === 'back-to-forums-btn') {
        showForumListView();
        return;
    }
    const link = e.target.closest('.forum-link');
    if (link) {
        const forumId = link.dataset.forumId;
        if (forumId) {
            showPostListView(forumId);
        }
    }
});

function showError(message) {
    dynamicContainer.innerHTML = `<div class="alert alert-danger">${message}</div>`;
}