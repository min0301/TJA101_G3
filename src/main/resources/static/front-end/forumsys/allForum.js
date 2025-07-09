async function renderForumTable() {
    const postsContainer = document.getElementById('forum-list-posts');
    if (!postsContainer) return;
    postsContainer.innerHTML = '';
    try {
        const res = await fetch('/api/forums');
        const forumList = await res.json();
        forumList.forEach(forum => {
            // 用 a 包住整個卡片，方便整塊點擊
            const postBox = document.createElement('a');
            postBox.className = 'post-box d-flex forum-link';
            postBox.setAttribute('href', `/forum/${forum.id}`);
            postBox.setAttribute('style', 'text-decoration:none;color:inherit;');
            postBox.setAttribute('data-aos', 'fade-up');
            postBox.setAttribute('data-aos-easing', 'linear');
            postBox.innerHTML = `
                <div class="forum-img-wrap" style="min-width:220px;max-width:220px;">
                    <img src="${forum.forImgUrl || 'assets/img/categories/1.jpg'}" alt="forum" style="width:100%;height:100px;object-fit:cover;border-radius:8px;">
                </div>
                <div class="card flex-grow-1 border-0">
                    <div class="card-body py-2 px-3">
                        <div class="d-flex align-items-center mb-2">
                            <h5 class="mb-0 fw-bold" style="font-size:1.3rem;">${forum.forName}</h5>
                        </div>
                        <div class="mb-1 text-muted" style="font-size:1rem;">${forum.forDes || ''}</div>
                        <div class="d-flex flex-wrap align-items-center mt-2" style="font-size:0.95rem;">
                            <span class="me-3 text-secondary">分類：${forum.categoryName || ''}</span>
                            <span class="me-3 text-secondary">板主：暫無</span>
                            <span class="me-3 text-secondary">更新：${forum.forUpdate ? new Date(forum.forUpdate).toLocaleString() : ''}</span>
                        </div>
                    </div>
                </div>
            `;
            postsContainer.appendChild(postBox);
        });
    } catch (e) {
        postsContainer.innerHTML = '<div class="alert alert-danger">資料載入失敗</div>';
    }
}
renderForumTable();