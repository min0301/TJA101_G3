// front-end/forumsys/forumListManager.js (完整最終版)

// --- 狀態管理 (State Management) ---
let currentForums = [];   // 儲存當前模式下（熱門或分類）的所有討論區
let currentIndex = 0;     // 追蹤已顯示的項目數量
const itemsPerLoad = 10;  // 每次顯示的項目數量，【可變】: 可依需求調整
let categoriesPopulated = false; // 標記分類下拉選單是否已載入過
let lastSelectedCategoryId = null; // 追蹤最後選擇的分類 ID

// --- DOM 元素快取 (在文件載入後初始化) ---
let listContainer;
let buttonContainer;
let categorySelectContainer;

// --- 渲染與 API 請求 (熱門 & 分類) ---

/**
 * 核心函式：根據模式和 ID 獲取並渲染討論區列表 (適用於熱門和分類)
 * @param {'hot' | 'category'} mode - 模式：'hot' 或 'category'
 * @param {number | null} id - 在 category 模式下，這是 catNo
 */
async function fetchAndRenderForums(mode, id = null) {
    listContainer.innerHTML = '<p class="text-muted p-3">載入中...</p>';
    buttonContainer.innerHTML = ''; // 清空"顯示更多"按鈕
    currentForums = [];
    currentIndex = 0;

    let apiUrl = '';
    // 【不可變】: 這些是後端定義的 API 端點
    if (mode === 'hot') {
        apiUrl = '/api/forums/hot';
    } else if (mode === 'category' && id) {
        apiUrl = `/api/category/${id}/forums`;
    } else {
        listContainer.innerHTML = '<p class="text-warning p-3">錯誤：未指定有效的瀏覽模式。</p>';
        return;
    }

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) throw new Error(`HTTP 錯誤！ 狀態: ${response.status}`);

        currentForums = await response.json();
        listContainer.innerHTML = ''; // 清空載入中提示

        if (!currentForums || currentForums.length === 0) {
            listContainer.innerHTML = '<p class="text-muted p-3">這裡還沒有討論區喔！</p>';
            return;
        }

        renderMoreForums(mode); // 渲染第一批
        setupShowMoreButton(mode); // 設定"顯示更多"按鈕

    } catch (error) {
        console.error(`無法獲取討論區列表 (mode: ${mode}, id: ${id}):`, error);
        listContainer.innerHTML = '<p class="text-danger p-3">資料載入失敗。</p>';
    }
}

/**
 * 根據模式渲染討論區項目 (分頁加載)
 * @param {'hot' | 'category'} mode
 */
function renderMoreForums(mode) {
    const fragment = document.createDocumentFragment();
    const end = currentIndex + itemsPerLoad;
    const forumsToRender = currentForums.slice(currentIndex, end);

    forumsToRender.forEach(forum => {
        // 【可變】: 這些 HTML 結構和 CSS class 名稱可依你的設計修改
        const imageUrl = forum.forImgUrl || 'assets/img/categories/1.png';
        const forumItem = document.createElement('div');
        forumItem.className = 'hot-forum-item forum-link';
        forumItem.dataset.forumId = forum.id;

        forumItem.innerHTML = `
            <div class="d-flex align-items-center flex-grow-1">
                <img src="${imageUrl}" class="forum-list-img" alt="${forum.forName}" onerror="this.src='assets/img/categories/1.png';">
                <span class="forum-name">${forum.forName}</span>
            </div>
            ${mode === 'hot' ? `
            <span class="hot-score">
                <i class="bi bi-fire"></i>
                ${forum.hotScore || 0}
            </span>` : ''}
        `;
        fragment.appendChild(forumItem);
    });

    listContainer.appendChild(fragment);
    currentIndex = end;
}

/**
 * 根據模式設定「顯示更多」按鈕
 * @param {'hot' | 'category'} mode
 */
function setupShowMoreButton(mode) {
    buttonContainer.innerHTML = '';
    if (currentIndex < currentForums.length) {
        const showMoreBtn = document.createElement('button');
        showMoreBtn.id = 'show-more-btn';
        showMoreBtn.className = 'btn btn-outline-secondary w-100';
        showMoreBtn.textContent = '顯示更多';
        showMoreBtn.addEventListener('click', () => {
            renderMoreForums(mode);
            // 如果已經沒有更多項目，隱藏按鈕
            if (currentIndex >= currentForums.length) {
                showMoreBtn.style.display = 'none';
            }
        });
        buttonContainer.appendChild(showMoreBtn);
    }
}

/**
 * 獲取並填充分類下拉選單
 */
async function fetchAndPopulateCategories() {
    if (categoriesPopulated) return; // 防止重複建立

    try {
        const response = await fetch('/api/categorys');
        if (!response.ok) throw new Error('無法獲取分類資料');
        const categories = await response.json();

        if (categories && categories.length > 0) {
            const dropdownContainer = document.createElement('div');
            // ... (這部分程式碼與你提供的一致，保持不變)
            dropdownContainer.className = 'dropdown w-100';

            dropdownContainer.innerHTML = `
                <button class="btn btn-outline-secondary dropdown-toggle w-100 text-start" type="button" id="categoryDropdownButton" data-bs-toggle="dropdown" aria-expanded="false">
                    <span id="selected-category-name"></span>
                </button>
                <ul class="dropdown-menu w-100" aria-labelledby="categoryDropdownButton" id="category-dropdown-menu"></ul>
            `;

            const menu = dropdownContainer.querySelector('#category-dropdown-menu');
            const buttonText = dropdownContainer.querySelector('#selected-category-name');

            categories.forEach(cat => {
                const li = document.createElement('li');
                li.innerHTML = `<a class="dropdown-item category-item" href="#" data-cat-id="${cat.id}" data-cat-name="${cat.catName}">
                                    <i class="bi bi-tag-fill me-2"></i>${cat.catName}
                                </a>`;
                menu.appendChild(li);
            });

            menu.addEventListener('click', (e) => {
                e.preventDefault();
                const target = e.target.closest('.category-item');
                if (target) {
                    const selectedCatId = target.dataset.catId;
                    const selectedCatName = target.dataset.catName;
                    buttonText.textContent = selectedCatName;
                    lastSelectedCategoryId = selectedCatId;
                    fetchAndRenderForums('category', selectedCatId);
                }
            });

            categorySelectContainer.innerHTML = '';
            categorySelectContainer.appendChild(dropdownContainer);

            const firstCategory = categories[0];
            buttonText.textContent = firstCategory.catName;
            lastSelectedCategoryId = firstCategory.id;
            categoriesPopulated = true;
        } else {
            categorySelectContainer.innerHTML = '<p class="text-muted">沒有可用的分類。</p>';
        }
    } catch (error) {
        console.error('分類載入失敗:', error);
        categorySelectContainer.innerHTML = '<p class="text-danger p-3">分類載入失敗。</p>';
    }
}

// --- 【【【新增】】】收藏列表功能 ---

/**
 * 【核心新增功能】
 * 呼叫 API 載入並顯示使用者收藏的討論區列表。
 */
async function loadCollectedForums() {
    listContainer.innerHTML = '<p class="text-muted p-3">讀取中...</p>';

    // 【可變】: 'jwtToken' 是你儲存在 localStorage 的 JWT 的 key 名稱
    const token = localStorage.getItem('jwt');
    if (!token) {
        listContainer.innerHTML = '<div class="alert alert-warning">請先登入以查看收藏。</div>';
        return;
    }

    try {
        // 【不可變】: '/api/forums/collect/me' 是後端定義的端點
        const response = await fetch('/api/forums/collect/me', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                // 【不可變】: 'Authorization' 是標準的 Header 名稱
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            // 處理如 401 (未授權) 或 403 (禁止) 等錯誤
            if (response.status === 401 || response.status === 403) {
                throw new Error('您需要登入才能查看此內容。');
            }
            throw new Error(`API 請求失敗，狀態碼: ${response.status}`);
        }

        const collectedForums = await response.json();
        listContainer.innerHTML = ''; // 清空列表準備渲染

        if (collectedForums.length === 0) {
            listContainer.innerHTML = '<div class="text-center p-3 text-muted">您尚未收藏任何討論區。</div>';
        } else {
            const fragment = document.createDocumentFragment();
            // 注意：這裡的變數名稱 (item.id, item.forName) 必須與你的 ForumDetailDTO 欄位對應
            collectedForums.forEach(item => {
                const forumItem = document.createElement('div');
                forumItem.className = 'hot-forum-item forum-link'; // 重用現有樣式
                forumItem.dataset.forumId = item.id;

                forumItem.innerHTML = `
                    <div class="d-flex align-items-center flex-grow-1">
                        <img src="${item.forImgUrl || 'assets/img/categories/1.png'}" class="forum-list-img" alt="${item.forName}" onerror="this.src='assets/img/categories/1.png';">
                        <span class="forum-name">${item.forName}</span>
                    </div>
                    <span class="hot-score">
                        <i class="bi bi-star-fill text-warning"></i>
                    </span>`;
                fragment.appendChild(forumItem);
            });
            listContainer.appendChild(fragment);
        }

    } catch (error) {
        console.error('載入收藏列表時發生錯誤:', error);
        listContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
    }
}


// --- 事件監聽與初始化 ---
document.addEventListener('DOMContentLoaded', () => {
    // DOM 元素快取
    listContainer = document.getElementById('forum-list-container');
    buttonContainer = document.getElementById('show-more-container');
    categorySelectContainer = document.getElementById('category-select-container');

    // 獲取所有 Tab 按鈕
    const hotTab = document.getElementById('hot-tab');
    const categoryTab = document.getElementById('category-tab');
    // 【【【新增】】】
    const myCollectionsTab = document.getElementById('my-collections-tab');

    // 熱門排行 Tab 點擊事件
    hotTab.addEventListener('click', () => {
        categorySelectContainer.style.display = 'none';
        fetchAndRenderForums('hot');
    });

    // 按類別瀏覽 Tab 點擊事件
    categoryTab.addEventListener('click', async () => {
        categorySelectContainer.style.display = 'block';
        buttonContainer.innerHTML = ''; // 切換時先清空按鈕
        await fetchAndPopulateCategories();
        if (lastSelectedCategoryId) {
            fetchAndRenderForums('category', lastSelectedCategoryId);
        }
    });

    // 【【【新增】】】我的收藏 Tab 點擊事件
    myCollectionsTab.addEventListener('click', () => {
        // 隱藏不相關的 UI 元素
        categorySelectContainer.style.display = 'none';
        buttonContainer.innerHTML = ''; // 收藏列表不使用"顯示更多"按鈕

        // 呼叫專門的函式來載入收藏列表
        loadCollectedForums();
    });

    // 事件委派：監聽整個列表容器的點擊，處理所有討論區項目的點擊
    if (listContainer) {
        listContainer.addEventListener('click', function (event) {
            const clickedLink = event.target.closest('.forum-link');
            if (!clickedLink) return;

            event.preventDefault();

            // 移除所有項目的 active 狀態
            const allLinks = document.querySelectorAll('.forum-link'); // 查詢整個文件以包含所有分頁的項目
            allLinks.forEach(link => link.classList.remove('active'));

            // 為當前點擊的項目加上 active
            clickedLink.classList.add('active');

            const forumId = clickedLink.dataset.forumId;

            // 呼叫外部函式來載入右側文章列表
            if (typeof showPostListView === 'function') {
                showPostListView(forumId);
            } else {
                console.error('錯誤：showPostListView 函式未定義，請檢查 allForum.js 是否正確載入。');
            }
        });
    }

    // 頁面初始載入時，預設顯示熱門排行
    fetchAndRenderForums('hot');
});