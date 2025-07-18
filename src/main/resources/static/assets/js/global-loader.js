/**
 * 函數：初始化 Header 的搜尋功能。
 * 這是為了解決非同步載入問題，將 header-search.js 的邏輯整合進來。
 */
function initializeHeaderSearch() {
    // 【不可變】: 這些 ID 對應 _header.html 中的元素 ID。
    const searchForm = document.getElementById('header-search-form');
    const scopeBtn = document.getElementById('search-scope-btn');
    const keywordInput = document.getElementById('header-search-keyword');
    const scopeOptions = document.querySelectorAll('.search-scope-option');

    // 如果頁面上找不到搜尋表單，就靜默退出，不執行任何操作。
    if (!searchForm) {
        return;
    }

    // 為下拉選單選項綁定點擊事件
    scopeOptions.forEach(option => {
        option.addEventListener('click', function (e) {
            e.preventDefault();
            const selectedScope = this.dataset.scope;
            const selectedText = this.textContent;
            scopeBtn.textContent = selectedText;
            scopeBtn.dataset.scope = selectedScope;
        });
    });

    // 為表單提交綁定事件
    searchForm.addEventListener('submit', function (e) {
        e.preventDefault(); // 阻止表單預設的刷新頁面行為
        const scope = scopeBtn.dataset.scope;
        const keyword = keywordInput.value.trim();

        if (!keyword) {
            alert('請輸入要搜尋的關鍵字！');
            keywordInput.focus();
            return;
        }

        // 【重要】: 組裝跳轉的 URL
        const searchUrl = `/front-end/search/searchResults.html?scope=${encodeURIComponent(scope)}&keyword=${encodeURIComponent(keyword)}`;
        window.location.href = searchUrl;
    });
}

/**
 * 載入並注入 HTML 元件的通用函式
 * @param {string} placeholderId - HTML 中用於放置內容的元素 ID
 * @param {string} filePath - 要載入的 HTML 檔案的絕對路徑
 * @param {function} callback - (可選) 載入成功後要執行的回呼函數
 */
function loadComponent(placeholderId, filePath, callback) {
    const placeholder = document.getElementById(placeholderId);
    if (!placeholder) {
        return; // 如果頁面不存在該 placeholder，就直接返回
    }

    fetch(filePath)
        .then(response => {
            if (!response.ok) {
                throw new Error(`請求失敗: ${response.status} ${response.statusText}`);
            }
            return response.text();
        })
        .then(data => {
            placeholder.innerHTML = data;
            // 【關鍵】: 如果傳入了 callback 函數，就在這裡執行它
            if (callback) {
                callback();
            }
        })
        .catch(error => {
            console.error(`載入 ${filePath} 到 #${placeholderId} 時發生錯誤:`, error);
            placeholder.innerHTML = `<p style="color:red; text-align:center;">元件 ${filePath} 載入失敗。</p>`;
        });
}

// 當 DOM 載入完成後，執行我們的載入邏輯
document.addEventListener("DOMContentLoaded", function () {

    // 載入 Header，並在載入成功後，將 initializeHeaderSearch 作為回呼函數傳入
    loadComponent('header-placeholder', '/templates/_header.html', () => {
        // 在這個時間點，我們 100% 確定 Header 的 HTML 已載入
        // 所以在這裡才執行搜尋功能的初始化
        initializeHeaderSearch();

        // 同時，也執行原本就有的動態載入會員資訊的 script
        if (typeof initializeDynamicHeader === 'function') {
            initializeDynamicHeader();
        }
    });

    // 載入 Footer (它不需要回呼函數)
    loadComponent('footer-placeholder', '/templates/_footer.html');

    // 為了相容你原本的寫法，保留這個。雖然這行可能可以移除，但保留著比較保險。
    loadComponent('templates/header-placeholder', '/templates/_header.html');
});