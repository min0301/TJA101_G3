/**
 * 穩定版購物車 Header 系統
 * 基於測試版本，加入完整功能但保持穩定性
 * 替換 mallCartHeader.js 使用
 */

console.log('🚀 購物車功能啟動中...');

// 全域變量
let cartIcon = null;
let cartBadge = null;

// 立即執行初始化
(function() {
    console.log('📍 當前路徑:', window.location.pathname);
    
    // 檢查是否在商城頁面
    const currentPath = window.location.pathname;
    const isInMallPages = currentPath.includes('/shopsys/') || 
                         currentPath.includes('/shopindex') || 
                         currentPath.includes('/product') || 
                         currentPath.includes('/cart') || 
                         currentPath.includes('/order');
    
    console.log('🔍 是否在商城頁面:', isInMallPages);
    
    if (!isInMallPages) {
        console.log('❌ 不在商城頁面，跳過購物車圖示創建');
        return;
    }
    
    // 延遲創建圖示，確保頁面載入完成
    setTimeout(function() {
        createStableCartIcon();
    }, 2000);
})();

function createStableCartIcon() {
    console.log('🛒 開始創建穩定版購物車圖示');
    
    // 檢查是否已經存在
    if (document.querySelector('.stable-cart-icon')) {
        console.log('購物車圖示已存在');
        return;
    }
    
    try {
        // 創建購物車容器
        const cartContainer = document.createElement('div');
        cartContainer.className = 'stable-cart-icon';
        cartContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            width: 60px;
            height: 60px;
            background: #007bff;
            color: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            z-index: 9999;
            font-size: 24px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            transition: all 0.3s ease;
        `;
        
        // 購物車圖示
        const iconElement = document.createElement('span');
        iconElement.innerHTML = '🛒';
        iconElement.style.cssText = `
            font-size: 24px;
            line-height: 1;
        `;
        
        // 購物車數量徽章
        const badgeElement = document.createElement('span');
        badgeElement.className = 'cart-badge';
        badgeElement.style.cssText = `
            position: absolute;
            top: -8px;
            right: -8px;
            background: #dc3545;
            color: white;
            border-radius: 50%;
            width: 24px;
            height: 24px;
            font-size: 12px;
            font-weight: bold;
            display: none;
            align-items: center;
            justify-content: center;
            border: 2px solid white;
            box-shadow: 0 2px 4px rgba(0,0,0,0.2);
        `;
        badgeElement.textContent = '0';
        
        // 組裝元素
        cartContainer.appendChild(iconElement);
        cartContainer.appendChild(badgeElement);
        
        // 添加懸停效果
        cartContainer.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.1)';
            this.style.backgroundColor = '#0056b3';
        });
        
        cartContainer.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
            this.style.backgroundColor = '#007bff';
        });
        
        // 添加點擊事件
        cartContainer.addEventListener('click', function() {
            handleCartClick();
        });
        
        // 插入到頁面
        document.body.appendChild(cartContainer);
        
        // 保存引用
        cartIcon = cartContainer;
        cartBadge = badgeElement;
        
        console.log('✅ 穩定版購物車圖示創建成功');
        
        // 初始化購物車數量
        updateCartBadge();
        
        // 設定事件監聽
        setupEventListeners();
        
        // 購物車圖示創建完成，不顯示系統提示
        
    } catch (error) {
        console.error('❌ 創建購物車圖示失敗:', error);
    }
}

function handleCartClick() {
    console.log('🛒 購物車圖示被點擊');
    
    const currentPath = window.location.pathname;
    
    if (currentPath.includes('/cart')) {
        // 如果已經在購物車頁面，顯示提示
        showCartPreview();
    } else {
        // 跳轉到購物車頁面
        window.location.href = '/front-end/shopsys/cart/cart.html';
    }
}

function showCartPreview() {
    const totalItems = getCartItemCount();
    
    const message = totalItems > 0 
        ? `購物車中有 ${totalItems} 個商品\n點擊頁面其他地方關閉此預覽`
        : '購物車是空的\n快去挑選喜歡的商品吧！';
    
    // 創建簡單的預覽提示
    const preview = document.createElement('div');
    preview.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: white;
        border: 2px solid #007bff;
        border-radius: 12px;
        padding: 24px;
        z-index: 10000;
        box-shadow: 0 8px 24px rgba(0,0,0,0.3);
        text-align: center;
        max-width: 300px;
    `;
    
    preview.innerHTML = `
        <div style="font-size: 48px; margin-bottom: 16px;">🛒</div>
        <h4 style="color: #007bff; margin-bottom: 12px;">購物車預覽</h4>
        <p style="margin-bottom: 16px; line-height: 1.5;">${message.replace('\n', '<br>')}</p>
        <button onclick="this.parentElement.remove()" style="
            background: #007bff; 
            color: white; 
            border: none; 
            padding: 8px 16px; 
            border-radius: 6px; 
            cursor: pointer;
        ">關閉</button>
    `;
    
    // 點擊背景關閉
    preview.addEventListener('click', function(e) {
        if (e.target === this) {
            this.remove();
        }
    });
    
    document.body.appendChild(preview);
    
    // 3秒後自動關閉
    setTimeout(() => {
        if (preview.parentNode) {
            preview.remove();
        }
    }, 3000);
}

function updateCartBadge() {
    if (!cartBadge) return;
    
    try {
        const totalItems = getCartItemCount();
        
        console.log('🔄 更新購物車徽章:', totalItems);
        
        if (totalItems > 0) {
            cartBadge.textContent = totalItems > 99 ? '99+' : totalItems.toString();
            cartBadge.style.display = 'flex';
        } else {
            cartBadge.style.display = 'none';
        }
        
    } catch (error) {
        console.error('❌ 更新購物車徽章失敗:', error);
    }
}

function getCartItemCount() {
    try {
        const cartData = localStorage.getItem('shopping_cart');
        if (!cartData) return 0;
        
        const items = JSON.parse(cartData);
        if (!Array.isArray(items)) return 0;
        
        return items.reduce((total, item) => total + (item.quantity || 1), 0);
    } catch (error) {
        console.warn('讀取購物車資料失敗:', error);
        return 0;
    }
}

function setupEventListeners() {
    // 監聽本地儲存變化
    window.addEventListener('storage', function(e) {
        if (e.key === 'shopping_cart') {
            console.log('🔄 購物車本地儲存已更新');
            updateCartBadge();
        }
    });
    
    // 監聽自定義購物車事件
    window.addEventListener('cartUpdated', function() {
        console.log('🔄 收到購物車更新事件');
        updateCartBadge();
    });
    
    console.log('✅ 事件監聽器已設定');
}

function showToast(message, type = 'success') {
    const colors = {
        success: '#28a745',
        info: '#007bff',
        warning: '#ffc107',
        error: '#dc3545'
    };
    
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        top: 90px;
        right: 20px;
        background: ${colors[type] || colors.success};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        z-index: 10000;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
        max-width: 300px;
        font-size: 14px;
    `;
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    // 動畫顯示
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    }, 100);
    
    // 自動移除
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// 提供全域方法
window.updateCartBadge = updateCartBadge;
window.showCartToast = showToast;

// 當商品加入購物車時呼叫
window.onCartItemAdded = function(productName) {
    updateCartBadge();
    showToast(`${productName || '商品'} 已加入購物車`, 'success');
};

// 除錯方法
window.debugStableCart = function() {
    console.log('🐛 穩定版購物車系統除錯資訊:');
    console.log('購物車圖示元素:', cartIcon);
    console.log('購物車徽章元素:', cartBadge);
    console.log('當前購物車數量:', getCartItemCount());
    
    try {
        const cartData = localStorage.getItem('shopping_cart');
        console.log('本地購物車資料:', cartData ? JSON.parse(cartData) : '無資料');
    } catch (e) {
        console.log('讀取購物車資料失敗:', e);
    }
};

console.log('📦 購物車功能載入完成');