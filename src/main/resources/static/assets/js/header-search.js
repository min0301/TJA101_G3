/**
 * header-search.js
 * 負責處理全站共用 header 中的搜尋功能
 */
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. 獲取必要的 DOM 元素 ---
    // 【不可變】: 這些 ID 對應 _header.html 中定義的元素 ID，是互動的基礎。
    const searchForm = document.getElementById('header-search-form');
    const scopeBtn = document.getElementById('search-scope-btn');
    const keywordInput = document.getElementById('header-search-keyword');
    const scopeOptions = document.querySelectorAll('.search-scope-option');

    // --- 2. 檢查元素是否存在，若不存在則不執行後續程式 ---
    if (!searchForm || !scopeBtn || !keywordInput || scopeOptions.length === 0) {
        // console.error('Header 搜尋功能的必要 HTML 元素缺失。');
        return; // 靜默失敗，因為在非主要頁面可能沒有這些元素
    }

    // --- 3. 綁定下拉選單選項的點擊事件 ---
    scopeOptions.forEach(option => {
        option.addEventListener('click', function (e) {
            e.preventDefault(); // 防止 <a> 標籤的預設跳轉行為

            // 從點擊的選項中獲取 data-scope 值和文字
            // 【可變】: 'scope' 是我們自訂的 data attribute 名稱，可依需求更改。
            const selectedScope = this.dataset.scope;
            const selectedText = this.textContent;

            // 更新按鈕的文字和 data-scope 屬性
            scopeBtn.textContent = selectedText;
            scopeBtn.dataset.scope = selectedScope;
        });
    });

    // --- 4. 綁定表單的提交事件 ---
    searchForm.addEventListener('submit', function (e) {
        e.preventDefault(); // 阻止表單的傳統提交方式

        // 獲取當前選擇的範圍和輸入的關鍵字
        const scope = scopeBtn.dataset.scope;
        // .trim() 是為了去除使用者輸入的前後空白
        const keyword = keywordInput.value.trim();

        // 簡單的前端驗證，如果沒輸入關鍵字則不進行搜尋
        if (!keyword) {
            alert('請輸入要搜尋的關鍵字！');
            keywordInput.focus(); // 讓使用者可以馬上繼續輸入
            return;
        }

        // 組裝目標 URL，並進行頁面跳轉
        // 【可變】: searchResults.html 的路徑可根據你的專案結構修改
        const searchUrl = `/front-end/search/searchResults.html?scope=${encodeURIComponent(scope)}&keyword=${encodeURIComponent(keyword)}`;
        // 【不可變】: window.location.href 是瀏覽器提供的標準跳轉頁面方法。
        window.location.href = searchUrl;
    });
});