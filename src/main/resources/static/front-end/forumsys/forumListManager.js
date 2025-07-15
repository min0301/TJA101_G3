// front-end/forumsys/forumListManager.js (修正版)

// --- 狀態管理 (State Management) ---
let currentForums = [];   // 儲存當前模式下（熱門或分類）的所有討論區
let currentIndex = 0;     // 追蹤已顯示的項目數量
const itemsPerLoad = 10;  // 每次顯示的項目數量，可以調整
let categoriesPopulated = false; // 標記分類下拉選單是否已載入過
let lastSelectedCategoryId = null; // 【【新增此行】】 追蹤最後選擇的分類 ID

// --- DOM 元素快取 ---
const listContainer = document.getElementById('forum-list-container');
const buttonContainer = document.getElementById('show-more-container');
const categorySelectContainer = document.getElementById('category-select-container');

// --- 渲染與 API 請求 ---

/**
 * 核心函式：根據模式和 ID 獲取並渲染討論區列表
 * @param {'hot' | 'category'} mode - 模式：'hot' 或 'category'
 * @param {number | null} id - 在 category 模式下，這是 catNo
 */
async function fetchAndRenderForums(mode, id = null) {
    listContainer.innerHTML = '<p class="text-muted p-3">載入中...</p>';
    buttonContainer.innerHTML = '';
    currentForums = [];
    currentIndex = 0;

    let apiUrl = '';
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
        listContainer.innerHTML = '';

        if (!currentForums || currentForums.length === 0) {
            listContainer.innerHTML = '<p class="text-muted p-3">這裡還沒有討論區喔！</p>';
            return;
        }

        renderMoreForums(mode);
        setupShowMoreButton(mode);

    } catch (error) {
        console.error(`無法獲取討論區列表 (mode: ${mode}, id: ${id}):`, error);
        listContainer.innerHTML = '<p class="text-danger p-3">資料載入失敗。</p>';
    }
}

/**
 * 根據模式渲染討論區項目
 * @param {'hot' | 'category'} mode
 */
function renderMoreForums(mode) {
    const fragment = document.createDocumentFragment();
    const end = currentIndex + itemsPerLoad;
    const forumsToRender = currentForums.slice(currentIndex, end);

    forumsToRender.forEach(forum => {
        const imageUrl = forum.forImgUrl || 'assets/img/categories/default.png';
        const forumItem = document.createElement('div');
        forumItem.className = 'hot-forum-item forum-link';
        forumItem.dataset.forumId = forum.id;

        forumItem.innerHTML = `
            <div class="d-flex align-items-center flex-grow-1">
                <img src="${imageUrl}" class="forum-list-img" alt="${forum.forName}" onerror="this.src='assets/img/categories/default.png';">
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
    // 這個防呆機制依然重要，確保選單只會被建立一次
    if (categoriesPopulated) return;

    try {
        const response = await fetch('/api/categorys');
        if (!response.ok) throw new Error('無法獲取分類資料');
        const categories = await response.json();

        if (categories && categories.length > 0) {
            const dropdownContainer = document.createElement('div');
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

            // 當使用者從下拉選單中選擇一個新分類時
            menu.addEventListener('click', (e) => {
                e.preventDefault();
                const target = e.target.closest('.category-item');
                if (target) {
                    const selectedCatId = target.dataset.catId;
                    const selectedCatName = target.dataset.catName;
                    buttonText.textContent = selectedCatName;

                    // 【修改點】更新全域狀態
                    lastSelectedCategoryId = selectedCatId;

                    // 直接根據新的選擇更新列表
                    fetchAndRenderForums('category', selectedCatId);
                }
            });

            categorySelectContainer.innerHTML = '';
            categorySelectContainer.appendChild(dropdownContainer);

            // --- 設定初始狀態 ---
            const firstCategory = categories[0];
            buttonText.textContent = firstCategory.catName;

            // 【修改點】設定預設的分類ID，並標記為已載入
            lastSelectedCategoryId = firstCategory.id;
            categoriesPopulated = true;

            // 【注意】我們把 fetchAndRenderForums 從這裡移除了

        } else {
            categorySelectContainer.innerHTML = '<p class="text-muted">沒有可用的分類。</p>';
        }
    } catch (error) {
        console.error('分類載入失敗:', error);
        categorySelectContainer.innerHTML = '<p class="text-danger p-3">分類載入失敗。</p>';
    }
}

// --- 事件監聽與初始化 ---
document.addEventListener('DOMContentLoaded', () => {
    const hotTab = document.getElementById('hot-tab');
    const categoryTab = document.getElementById('category-tab');
    const listContainer = document.getElementById('forum-list-container'); // << 確保在此處獲取
    const categorySelectContainer = document.getElementById('category-select-container'); // << 確保在此處獲取

    hotTab.addEventListener('click', () => {
        categorySelectContainer.style.display = 'none';
        fetchAndRenderForums('hot');
    });

    // 【【【核心修改點】】】
    categoryTab.addEventListener('click', async () => {
        categorySelectContainer.style.display = 'block';

        // 1. 確保分類選單已建立 (只會在第一次真正執行)
        await fetchAndPopulateCategories();

        // 2. 每次點擊，都根據「最後選擇的ID」重新渲染討論區列表
        //    這樣即使用戶切換回來，也能顯示正確的分類內容
        if (lastSelectedCategoryId) {
            fetchAndRenderForums('category', lastSelectedCategoryId);
        }
    });

    if (listContainer) {
        // 使用「事件委派」，監聽整個列表容器的點擊事件
        listContainer.addEventListener('click', function (event) {

            // 判斷點擊的是否為一個 .forum-link 元素
            const clickedLink = event.target.closest('.forum-link');

            // 如果點的不是連結，則不執行任何動作
            if (!clickedLink) {
                return;
            }

            // 阻止 <a> 連結的預設跳轉行為 (如果你的 .forum-link 是 <a>)
            event.preventDefault();

            // 1. 移除所有兄弟連結的 'active' class
            const allLinks = listContainer.querySelectorAll('.forum-link');
            allLinks.forEach(link => {
                link.classList.remove('active');
            });

            // 2. 只在當前被點擊的連結上加上 'active' class
            clickedLink.classList.add('active');

            // 3. 從點擊的連結上取得 data-forum-id
            const forumId = clickedLink.dataset.forumId;

            // 4. 呼叫在 forumindex.html 中定義的函式來載入右側內容
            //    (這個函式應由 allForum.js 提供)
            if (typeof showPostListView === 'function') {
                showPostListView(forumId);
            } else {
                console.error('錯誤：showPostListView 函式未定義，請檢查 allForum.js 是否正確載入。');
            }
        });
    }

    // 預設載入熱門排行
    fetchAndRenderForums('hot');
});