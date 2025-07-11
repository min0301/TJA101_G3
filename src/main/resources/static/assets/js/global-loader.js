/**
 * 載入並注入 HTML 元件的通用函式
 * @param {string} placeholderId - HTML 中用於放置內容的元素 ID
 * @param {string} filePath - 要載入的 HTML 檔案的絕對路徑
 */
function loadComponent(placeholderId, filePath) {
    // 註解(不可變): placeholderId 和 filePath 是呼叫時傳入的參數

    const placeholder = document.getElementById(placeholderId);
    if (!placeholder) {
        // 如果頁面中不存在該 placeholder，就直接返回，不執行任何操作
        return;
    }

    fetch(filePath)
        .then(response => {
            if (!response.ok) {
                // 如果 HTTP 狀態碼不是 200-299，就拋出錯誤
                throw new Error(`請求失敗: ${response.status} ${response.statusText}`);
            }
            return response.text();
        })
        .then(data => {
            placeholder.innerHTML = data;
            // 延遲一小段時間，確保 DOM 更新後再觸發動畫
            setTimeout(() => triggerAosAnimation(placeholder), 100);
        })
        .catch(error => {
            console.error(`載入 ${filePath} 到 #${placeholderId} 時發生錯誤:`, error);
            // 發生錯誤時，在 placeholder 中顯示錯誤訊息
            placeholder.innerHTML = `<p style="color:red; text-align:center;">元件 ${filePath} 載入失敗。</p>`;
        });
}

/**
 * 手動觸發指定容器內的 AOS 動畫
 * @param {HTMLElement} container - 要尋找 AOS 元素的容器
 */
function triggerAosAnimation(container) {
    // 註解(不可變): container 是觸發動畫的 DOM 範圍

    const aosElements = container.querySelectorAll('[data-aos]');
    if (aosElements.length > 0) {
        aosElements.forEach(el => el.classList.add('aos-animate'));
    }
}

// 當 DOM 載入完成後，執行我們的載入邏輯
document.addEventListener("DOMContentLoaded", function () {
    // 註解(可變): 如果你有更多元件，只需在這裡新增一行 loadComponent 呼叫即可
    loadComponent('header-placeholder', '/templates/_header.html');
    loadComponent('footer-placeholder', '/templates/_footer.html');

    // 為了相容 index.html 中那個特殊的 ID
    loadComponent('templates/header-placeholder', '/templates/_header.html');
});