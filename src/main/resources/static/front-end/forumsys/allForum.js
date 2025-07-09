// front-end/forumsys/allForum.js

const dynamicContainer = document.getElementById('dynamic-content-container');

function triggerAOS() {
    setTimeout(() => {
        const aosElements = dynamicContainer.querySelectorAll('[data-aos]');
        aosElements.forEach(el => el.classList.add('aos-animate'));
    }, 100);
}

async function showForumListView() {
    try {
        const listTemplate = await fetch('../front-end/forumsys/forum-list.html').then(res => res.text());
        dynamicContainer.innerHTML = listTemplate;
        window.scrollTo({ top: 0, behavior: 'smooth' });
        await renderForumTable();
        triggerAOS();
    } catch (error) {
        showError('無法載入討論區介面。');
        console.error(error);
    }
}

async function showPostListView(forumId) {
    try {
        const postTemplate = await fetch('../front-end/forumsys/forum-posts-template.html').then(res => res.text());
        dynamicContainer.innerHTML = postTemplate;
        window.scrollTo({ top: 0, behavior: 'smooth' });

        console.log(`正在抓取 forumId: ${forumId} 的資料...`);
        const [forum, posts] = await Promise.all([
            fetch(`/api/forum/${forumId}`).then(res => res.json()),
            fetch(`/api/forum/${forumId}/posts`).then(res => res.json())
        ]);
        console.log("成功抓取到 Header 資料:", forum);
        console.log("成功抓取到 Posts 資料:", posts);

        renderForumHeader(forum);
        renderPosts(posts); // <--- 問題點很可能在這裡面
        triggerAOS();

    } catch (error) {
        showError(`無法載入文章列表 (ID: ${forumId})`);
        console.error(error);
    }
}

function renderForumHeader(forum) {
    const headerContainer = dynamicContainer.querySelector('#forum-header');
    const imageUrl = forum.forImgUrl || '../assets/img/categories/1.jpg';
    const fallbackImageUrl = '../assets/img/categories/1.jpg';
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
        postBox.setAttribute('data-aos', 'fade-up');
        const fallbackImageUrl = '../assets/img/categories/1.jpg';
        postBox.innerHTML = `
            <div class="forum-img-wrap" style="min-width:220px;max-width:220px;">
                <img src="${forum.forImgUrl || fallbackImageUrl}" alt="forum" style="width:100%;height:100px;object-fit:cover;border-radius:8px;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
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

// 【【【 已加入大量 console.log 的版本 】】】
function renderPosts(posts) {
    console.log("進入 renderPosts 函式，收到的資料:", posts);

    const postsContainer = dynamicContainer.querySelector('#post-list-container');
    if (!postsContainer) {
        console.error("錯誤：找不到 #post-list-container 元素！");
        return;
    }

    if (!posts || posts.length === 0) {
        console.log("沒有文章資料，顯示提示訊息。");
        postsContainer.innerHTML = '<div class="alert alert-info">這個討論區還沒有文章喔！</div>';
        return;
    }

    console.log(`準備渲染 ${posts.length} 篇文章...`);
    postsContainer.innerHTML = '';

    posts.forEach((post, index) => {
        console.log(`正在處理第 ${index + 1} 篇文章:`, post);
        // 增加一個保護，防止 post 是 null 或沒有 id
        if (!post || typeof post.id === 'undefined') {
            console.error(`第 ${index + 1} 篇文章的資料格式有誤或缺少 id`, post);
            return; // 跳過這筆錯誤的資料，繼續下一筆
        }

        const fallbackImageUrl = '../assets/img/categories/1.jpg';
        const postImageUrl = `/api/forumpost/image/${post.id}`;

        // 把 postTitle 和 postCon 先取出來，避免 undefined 問題
        const title = post.postTitle || "（無標題）";
        const content = post.postCon || "";

        const postCard = `
            <div class="post-box d-flex mb-3" data-aos="fade-up">
                <div class="card w-100">
                    <div class="card-body d-flex">
                         <div class="flex-shrink-0 me-3">
                            <img src="${postImageUrl}" alt="${title}" style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px;" onerror="this.onerror=null;this.src='${fallbackImageUrl}';">
                        </div>
                        <div class="flex-grow-1">
                            <h4 class="card-title fw-bold"><a>${title}</a></h4>
                            <p class="card-text text-muted">${content.substring(0, 100)}...</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        postsContainer.innerHTML += postCard;
    });
    console.log("所有文章渲染完畢。");
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