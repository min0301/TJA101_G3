/**
 * searchResults.js
 * 處理搜尋結果頁面的邏輯
 */
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. 獲取 DOM 容器 ---
    // 【不可變】: 這些 ID 對應 searchResults.html 中的元素 ID。
    const resultsContainer = document.getElementById('search-results-container');
    const titleContainer = document.getElementById('search-title-container');

    // --- 2. 解析 URL 參數 ---
    // 【不可變】: 這是標準的 URL 參數解析方法。
    const urlParams = new URLSearchParams(window.location.search);
    const scope = urlParams.get('scope');
    const keyword = urlParams.get('keyword');

    // 如果缺少參數，顯示錯誤訊息
    if (!scope || !keyword) {
        titleContainer.innerHTML = '<h1>無效的搜尋請求</h1>';
        resultsContainer.innerHTML = '<p class="alert alert-danger">缺少搜尋範圍或關鍵字。</p>';
        return;
    }

    // 動態更新網頁標題
    document.title = `搜尋 "${keyword}" - 像素部落`;

    // --- 3. 根據 scope 決定要呼叫哪個函式來獲取並渲染資料 ---
    switch (scope) {
        case 'forum':
            renderSearchTitle('討論區', keyword);
            fetchAndRenderForumResults(keyword);
            break;
        case 'news':
            renderSearchTitle('新聞', keyword);
            fetchAndRenderNewsResults(keyword); // 未來可擴充
            break;
        default:
            titleContainer.innerHTML = `<h1>未知的搜尋範圍</h1>`;
            resultsContainer.innerHTML = `<p class="alert alert-warning">不支援的搜尋範圍: ${scope}</p>`;
    }

    /**
     * 渲染搜尋結果頁面的大標題
     */
    function renderSearchTitle(scopeText, keywordText) {
        // 【可變】: h2, small 的文字內容和樣式都可以客製化
        titleContainer.innerHTML = `
            <h2 class="fw-bold">
                搜尋 <span class="text-primary">"${keywordText}"</span> 的結果
                <small class="text-muted fs-6 ms-2">(範圍: ${scopeText})</small>
            </h2>
            <hr>
        `;
    }

    /**
     * 獲取並渲染討論區的搜尋結果
     */
    async function fetchAndRenderForumResults(keyword) {
        // 顯示載入中的提示 (良好 UX)
        resultsContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border text-primary" role="status"></div><p class="mt-2">正在努力搜尋中...</p></div>`;

        try {
            // 【不可變】: fetch 是標準 API，路徑對應你的 ForumController。
            const response = await fetch(`/api/search/forums?keyword=${encodeURIComponent(keyword)}`);
            if (!response.ok) {
                throw new Error(`伺服器錯誤: ${response.status}`);
            }
            const forums = await response.json(); // 解析回傳的 JSON

            // 檢查是否有結果
            if (forums.length === 0) {
                resultsContainer.innerHTML = `
                    <div class="alert alert-info text-center">
                        <i class="bi bi-search fs-3"></i>
                        <h4 class="alert-heading mt-2">找不到結果</h4>
                        <p>找不到與 "${keyword}" 相關的討論區，請試試其他關鍵字。</p>
                    </div>
                `;
                return;
            }

            // 使用 map 和 join 來產生所有結果的 HTML
            const resultsHTML = forums.map(forum => {
                // 【可變】: 整個卡片的 HTML 結構和樣式都可以修改。
                const fallbackImg = '../../assets/img/categories/1.jpg'; // 預設圖片
                // 這是關鍵！產生指向該討論區的正確連結
                const forumLink = `/front-end/forumsys/forumindex.html?forumId=${forum.id}`;

                return `
                <a href="${forumLink}" class="card search-result-card text-decoration-none text-dark mb-3">
                    <div class="card-body d-flex align-items-center">
                        <img src="${forum.forImgUrl || fallbackImg}" alt="${forum.forName}" class="forum-img me-3" onerror="this.src='${fallbackImg}'">
                        <div class="flex-grow-1">
                            <h5 class="card-title mb-1">${forum.forName}</h5>
                            <p class="card-text text-muted mb-2">${forum.forDes || '此討論區沒有描述。'}</p>
                            <span class="badge rounded-pill text-bg-secondary">${forum.categoryName || '未分類'}</span>
                        </div>
                        <i class="bi bi-chevron-right fs-4 text-muted ms-3"></i>
                    </div>
                </a>
                `;
            }).join('');

            resultsContainer.innerHTML = resultsHTML;

        } catch (error) {
            console.error('搜尋討論區失敗:', error);
            resultsContainer.innerHTML = `<div class="alert alert-danger">搜尋時發生錯誤，請稍後再試。</div>`;
        }
    }

    async function fetchAndRenderNewsResults(keyword) {
        resultsContainer.innerHTML = `<div class="text-center p-5"><div class="spinner-border text-success" role="status"></div><p class="mt-2">正在搜尋新聞中...</p></div>`;
        try {
            const response = await fetch(`/api/News/search?keyword=${encodeURIComponent(keyword)}`);
            if (!response.ok) throw new Error(`伺服器錯誤: ${response.status}`);
            const result = await response.json();
            const newsList = result || [];

            if (newsList.length === 0) {
                resultsContainer.innerHTML = `
                    <div class="alert alert-info text-center">
                        <i class="bi bi-search fs-3"></i>
                        <h4 class="alert-heading mt-2">找不到結果</h4>
                        <p>找不到與 "${keyword}" 相關的新聞。</p>
                    </div>`;
                return;
            }

            const resultsHTML = newsList.map(news => {
                const newsLink = `/front-end/news/NewsDetail.html?newsId=${news.id}`;
                return `
                    <a href="${newsLink}" class="card mb-3 text-decoration-none text-dark">
                        <div class="card-body">
                            <h5 class="card-title">${news.newsTit}</h5>
                            <p class="card-text text-muted">${news.newsCon.slice(0, 100)}...</p>
                            <span class="badge bg-secondary">${news.categoryTags?.join(', ') || '未分類'}</span>
                        </div>
                    </a>`;
            }).join('');
            resultsContainer.innerHTML = resultsHTML;

        } catch (error) {
            console.error('搜尋新聞失敗:', error);
            resultsContainer.innerHTML = `<div class="alert alert-danger">搜尋時發生錯誤，請稍後再試。</div>`;
        }
    }
});