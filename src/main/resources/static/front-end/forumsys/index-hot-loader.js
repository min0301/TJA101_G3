// front-end/index-hot-loader.js (雙欄佈局優化版)

/**
 * 程式進入點：當 DOM 載入完成後，立即執行。
 */
(async function () {
    document.addEventListener('DOMContentLoaded', () => {
        fetchAndRenderHotForums();
    });
})();

/**
 * 從後端 API 獲取熱門討論區資料並渲染到頁面上
 */
async function fetchAndRenderHotForums() {
    const apiUrl = '/api/forums/hot';
    const ITEMS_TO_SHOW = 10;

    // 【【修改點 1】】: 一次獲取左右兩個欄位的容器及初始 spinner
    const containerCol1 = document.getElementById('hot-forums-col-1');
    const containerCol2 = document.getElementById('hot-forums-col-2');
    const spinner = document.getElementById('initial-spinner');

    if (!containerCol1 || !containerCol2) {
        console.error("錯誤：找不到 #hot-forums-col-1 或 #hot-forums-col-2 容器！");
        return;
    }

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
            throw new Error(`API 請求失敗，狀態碼: ${response.status}`);
        }
        const forums = await response.json();

        // 【【修改點 2】】: 隱藏 spinner，清空可能存在的舊內容
        if(spinner) spinner.remove();
        containerCol1.innerHTML = '';
        containerCol2.innerHTML = '';

        if (!forums || forums.length === 0) {
            // 如果沒有資料，可以在第一欄顯示提示
            containerCol1.innerHTML = '<p class="text-muted col-12">目前沒有熱門討論區。</p>';
            return;
        }

        const forumsToRender = forums.slice(0, ITEMS_TO_SHOW);
        // 【【修改點 3】】: 計算分割點，確保即使總數是奇數也能正確分配
        const midPoint = Math.ceil(forumsToRender.length / 2);

        // 渲染左欄 (1-5)
        forumsToRender.slice(0, midPoint).forEach((forum, index) => {
            const rank = index + 1; // 排名 1, 2, 3...
            const forumElement = createForumElement(forum, rank);
            containerCol1.appendChild(forumElement);
        });

        // 渲染右欄 (6-10)
        forumsToRender.slice(midPoint).forEach((forum, index) => {
            const rank = index + midPoint + 1; // 排名 6, 7, 8...
            const forumElement = createForumElement(forum, rank);
            containerCol2.appendChild(forumElement);
        });

    } catch (error) {
        console.error("無法載入熱門討論區:", error);
        if(spinner) spinner.remove();
        containerCol1.innerHTML = `
            <div class="alert alert-danger" role="alert">
              熱門討論區載入失敗，請稍後再試。
            </div>`;
    }
}

/**
 * 根據單一討論區物件和排名，建立對應的 HTML 元素 (卡片本身)
 * @param {object} forum - 從 API 獲取的討論區物件
 * @param {number} rank - 該討論區的排名
 * @returns {HTMLElement} - 代表單一討論區卡片的 a 連結元素
 */
function createForumElement(forum, rank) {
    const forumId = forum.id;
    const forumName = forum.forName || "（未命名）";
    const hotScore = forum.hotScore || 0;
    const imageUrl = forum.forImgUrl || 'assets/img/categories/default.png';
    const fallbackImageUrl = 'assets/img/categories/default.png';

    // 【【修改點 4】】: 不再建立 'col-lg-6' 外層，直接建立卡片本身
    const cardLink = document.createElement('a');
    cardLink.href = `/front-end/forumsys/forumindex.html?forumId=${forumId}`;
    cardLink.className = 'hot-item-card d-flex align-items-center text-decoration-none';
    cardLink.innerHTML = `
        <span class="hot-item-rank">${rank}</span>
        <img src="${imageUrl}" class="hot-item-img" alt="${forumName}" 
             onerror="this.onerror=null; this.src='${fallbackImageUrl}';">
        <div class="hot-item-details">
            <h6 class="hot-item-title mb-1">${forumName}</h6>
            <small class="hot-item-score">
                <i class="bi bi-fire"></i>
                ${hotScore.toLocaleString()}
            </small>
        </div>
        <div class="ms-auto">
            <i class="bi bi-heart hot-item-like"></i>
        </div>
    `;

    return cardLink;
}