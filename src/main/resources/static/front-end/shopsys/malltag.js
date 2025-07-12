// API 基礎 URL - 請根據你的後端地址調整
const API_BASE_URL = 'http://localhost:8080/api';

// DOM 元素 - 移到函數內部獲取，避免初始化時元素不存在的問題
// const malltagContainer = document.getElementById('malltag-container');
// const malltagButtonsContainer = document.getElementById('malltag-buttons');

// 當前選中的商城標籤
let selectedMallTag = null;

// 載入商城標籤
async function loadMallTags() {
    const malltagButtonsContainer = document.getElementById('malltag-buttons');
    
    // 檢查元素是否存在
    if (!malltagButtonsContainer) {
        console.error('找不到 malltag-buttons 元素');
        return;
    }
    
    try {
        console.log('開始載入商城標籤...');
        const response = await fetch(`${API_BASE_URL}/malltag`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const mallTags = await response.json();
        console.log('成功載入商城標籤:', mallTags);
        
        // 清空現有按鈕
        malltagButtonsContainer.innerHTML = '';
        
        // 檢查是否有標籤資料
        if (!mallTags || mallTags.length === 0) {
            malltagButtonsContainer.innerHTML = '<p class="text-muted small">暫無可用的商城標籤</p>';
            return;
        }
        
        // 生成標籤按鈕
        mallTags.forEach(tag => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'btn btn-outline-primary btn-sm malltag-btn';
            button.dataset.malltagId = tag.id;
            button.innerHTML = `
                <i class="bi bi-tag me-1"></i>
                ${escapeHtml(tag.mallTagName)}
            `;
            
            // 添加點擊事件
            button.addEventListener('click', () => {
                selectMallTag(tag.id, tag.mallTagName, button);
            });
            
            malltagButtonsContainer.appendChild(button);
        });
        
        console.log('商城標籤按鈕生成完成');
        
    } catch (error) {
        console.error('載入商城標籤失敗:', error);
        if (malltagButtonsContainer) {
            malltagButtonsContainer.innerHTML = `
                <div class="alert alert-warning alert-sm" role="alert">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    載入商城標籤失敗：${error.message}
                </div>
            `;
        }
    }
}

// 選擇商城標籤
function selectMallTag(tagId, tagName, buttonElement) {
    // 移除所有按鈕的 active 狀態
    document.querySelectorAll('.malltag-btn').forEach(btn => {
        btn.classList.remove('active');
        btn.classList.add('btn-outline-primary');
        btn.classList.remove('btn-primary');
    });
    
    // 設置當前按鈕為 active
    buttonElement.classList.add('active');
    buttonElement.classList.remove('btn-outline-primary');
    buttonElement.classList.add('btn-primary');
    
    // 更新選中狀態
    selectedMallTag = {
        id: tagId,
        name: tagName
    };
    
    console.log('選中商城標籤:', selectedMallTag);
    
    // 在這裡可以添加其他邏輯，例如更新動態內容區域
    updateDynamicContent(tagName);
}

// 更新動態內容區域
function updateDynamicContent(tagName) {
    const dynamicContainer = document.getElementById('dynamic-content-container');
    
    dynamicContainer.innerHTML = `
        <div class="selected-malltag-info p-4 bg-light rounded">
            <h5 class="mb-3">
                <i class="bi bi-shop me-2"></i>
                已選擇商城標籤：${escapeHtml(tagName)}
            </h5>
            <div class="text-center">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">載入中...</span>
                </div>
                <p class="mt-2 text-muted">正在載入相關商品...</p>
            </div>
        </div>
    `;
    
    // 這裡可以添加載入相關商品的邏輯
    // 例如：loadProductsByMallTag(tagId);
}

// HTML 轉義函數
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 初始化商城標籤
document.addEventListener('DOMContentLoaded', () => {
    // 確保 DOM 元素存在後再載入
    const checkAndLoadMallTags = () => {
        const malltagButtonsContainer = document.getElementById('malltag-buttons');
        if (malltagButtonsContainer) {
            console.log('DOM 元素已找到，開始載入商城標籤');
            loadMallTags();
        } else {
            console.log('DOM 元素尚未找到，500ms 後重試');
            setTimeout(checkAndLoadMallTags, 500);
        }
    };
    
    // 延遲執行以確保所有元素都已載入
    setTimeout(checkAndLoadMallTags, 100);
});