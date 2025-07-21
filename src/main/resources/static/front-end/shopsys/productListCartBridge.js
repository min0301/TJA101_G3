/**
 * 穩定版購物車橋接模組
 * 與現有的 addToCartFromList 函數完美整合
 * 替換 productListCartBridge.js 使用
 */

console.log('🔗 購物車功能整合啟動...');

// 延遲初始化，確保其他腳本先載入
setTimeout(function() {
    initStableBridge();
}, 3000);

function initStableBridge() {
    console.log('🔗 開始初始化穩定版橋接功能');
    
    try {
        // 檢查必要的函數是否存在
        if (typeof window.addToCartFromList === 'function') {
            enhanceAddToCartFunction();
            console.log('✅ 已增強 addToCartFromList 函數');
        } else {
            console.warn('⚠️ 找不到 addToCartFromList 函數，將等待載入');
            // 再等待一下
            setTimeout(function() {
                if (typeof window.addToCartFromList === 'function') {
                    enhanceAddToCartFunction();
                    console.log('✅ 延遲增強 addToCartFromList 函數成功');
                } else {
                    console.warn('⚠️ 仍然找不到 addToCartFromList 函數');
                }
            }, 2000);
        }
        
        // 設定事件監聽
        setupBridgeEventListeners();
        console.log('✅ 橋接事件監聽已設定');
        
        console.log('✅ 穩定版橋接模組初始化完成');
        
    } catch (error) {
        console.error('❌ 橋接模組初始化失敗:', error);
    }
}

function enhanceAddToCartFunction() {
    // 保存原始函數
    const originalAddToCart = window.addToCartFromList;
    
    console.log('🔧 增強 addToCartFromList 函數');
    
    // 創建增強版本
    window.addToCartFromList = async function(productId) {
        console.log('🛒 橋接模組：處理加購物車請求', productId);
        
        try {
            // 執行原始邏輯
            await originalAddToCart(productId);
            
            console.log('✅ 原始加購物車邏輯執行完成');
            
            // 延遲更新購物車徽章，給後端時間處理
            setTimeout(() => {
                // 更新購物車徽章
                if (typeof window.updateCartBadge === 'function') {
                    window.updateCartBadge();
                    console.log('🔄 購物車徽章已更新');
                }
                
                // 顯示成功提示
                const productName = getProductNameById(productId);
                if (typeof window.onCartItemAdded === 'function') {
                    window.onCartItemAdded(productName);
                } else if (typeof window.showCartToast === 'function') {
                    window.showCartToast(`${productName} 已加入購物車`, 'success');
                }
                
                // 觸發自定義事件
                window.dispatchEvent(new CustomEvent('cartUpdated', {
                    detail: { 
                        productId: productId,
                        productName: productName,
                        action: 'add'
                    }
                }));
                
                console.log('🎉 購物車更新流程完成');
                
            }, 1000); // 給後端 1 秒時間處理
            
        } catch (error) {
            console.error('❌ 橋接模組：加購物車失敗', error);
            
            // 顯示錯誤提示
            if (typeof window.showCartToast === 'function') {
                window.showCartToast('加入購物車失敗，請重試', 'error');
            }
            
            throw error; // 重新拋出錯誤，讓原始邏輯處理
        }
    };
}

function getProductNameById(productId) {
    try {
        // 方法 1: 從 productListManager 獲取
        if (window.productListManager && window.productListManager.productsData) {
            const product = window.productListManager.productsData.find(p => 
                (p.id || p.proNo) == productId
            );
            if (product && product.proName) {
                return product.proName;
            }
        }
        
        // 方法 2: 從 DOM 中尋找
        const productCard = document.querySelector(`[data-product-id="${productId}"]`);
        if (productCard) {
            const cardElement = productCard.closest('.product-card') || 
                               productCard.closest('.card');
            
            if (cardElement) {
                const nameElement = cardElement.querySelector('.card-title') ||
                                   cardElement.querySelector('h5') ||
                                   cardElement.querySelector('.product-name');
                
                if (nameElement) {
                    return nameElement.textContent.trim();
                }
            }
        }
        
        // 方法 3: 嘗試從頁面標題獲取（如果在商品詳情頁）
        if (window.location.pathname.includes('/product')) {
            const pageTitle = document.title;
            if (pageTitle && pageTitle.includes(' - ')) {
                const productName = pageTitle.split(' - ')[0];
                if (productName && productName !== '像素部落') {
                    return productName;
                }
            }
        }
        
        // 方法 4: 默認名稱
        return `商品 #${productId}`;
        
    } catch (error) {
        console.warn('獲取商品名稱失敗:', error);
        return '商品';
    }
}

function setupBridgeEventListeners() {
    // 監聽購物車更新事件
    window.addEventListener('cartUpdated', function(event) {
        console.log('🔄 橋接模組：收到購物車更新事件', event.detail);
        
        // 確保徽章更新
        if (typeof window.updateCartBadge === 'function') {
            setTimeout(() => {
                window.updateCartBadge();
            }, 500);
        }
    });
    
    // 監聽本地儲存變化
    window.addEventListener('storage', function(event) {
        if (event.key === 'shopping_cart') {
            console.log('🔄 橋接模組：購物車本地儲存已更新');
            
            if (typeof window.updateCartBadge === 'function') {
                window.updateCartBadge();
            }
        }
    });
    
    // 監聽登入狀態變化
    window.addEventListener('storage', function(event) {
        if (event.key === 'jwt') {
            console.log('🔄 橋接模組：登入狀態已變化');
            
            setTimeout(() => {
                if (typeof window.updateCartBadge === 'function') {
                    window.updateCartBadge();
                }
            }, 1000);
        }
    });
}

// 提供測試方法
window.testCartBridge = function(productId = '1') {
    console.log('🧪 測試購物車橋接功能');
    
    if (typeof window.addToCartFromList === 'function') {
        console.log('✅ addToCartFromList 函數存在，開始測試');
        
        // 模擬加入購物車
        const testProduct = {
            id: productId,
            name: '測試商品',
            price: 999
        };
        
        // 觸發更新事件
        window.dispatchEvent(new CustomEvent('cartUpdated', {
            detail: { 
                productId: productId,
                productName: testProduct.name,
                action: 'test'
            }
        }));
        
        // 顯示成功提示
        if (typeof window.onCartItemAdded === 'function') {
            window.onCartItemAdded(testProduct.name);
        }
        
        console.log('🎉 測試完成');
    } else {
        console.warn('❌ addToCartFromList 函數不存在');
    }
};

// 除錯方法
window.debugStableBridge = function() {
    console.log('🐛 穩定版橋接系統除錯資訊:');
    console.log('addToCartFromList 函數:', typeof window.addToCartFromList);
    console.log('updateCartBadge 函數:', typeof window.updateCartBadge);
    console.log('showCartToast 函數:', typeof window.showCartToast);
    console.log('onCartItemAdded 函數:', typeof window.onCartItemAdded);
    console.log('productListManager:', !!window.productListManager);
    
    if (window.productListManager && window.productListManager.productsData) {
        console.log('商品資料數量:', window.productListManager.productsData.length);
    }
};

console.log('📦 購物車功能整合完成');