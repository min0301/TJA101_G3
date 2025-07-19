// cart-popup.js - 購物車談窗管理器，負責裡加入購物車成功的提示

(function() {
    'use strict';
    
    window.CartPopup = {
        // 顯示加入購物車成功彈窗
        showAddSuccess(productName, quantity, totalItems) {
            this.showModal({
                type: 'success',
                title: '加入購物車成功！',
                message: `已將 <strong>${productName}</strong> x${quantity} 加入購物車`,
                buttons: [
                    {
                        text: '繼續購物',
                        class: 'btn-outline-primary',
                        action: () => this.hideModal()
                    },
                    {
                        text: '查看購物車',
                        class: 'btn-primary',
                        action: () => {
                            this.hideModal();
                            window.location.href = '/front-end/shopsys/cart/cart.html';
                        }
                    }
                ]
            });
            
            // 更新購物車數量顯示
            this.updateCartCount(totalItems);
        },
        
        // 顯示錯誤彈窗
        showError(message) {
            this.showModal({
                type: 'error',
                title: '操作失敗',
                message: message,
                buttons: [
                    {
                        text: '確定',
                        class: 'btn-primary',
                        action: () => this.hideModal()
                    }
                ]
            });
        },
        
        // 通用彈窗顯示
        showModal(config) {
            let modal = document.getElementById('cart-popup-modal');
            if (modal) modal.remove();
            
            const iconMap = {
                success: '🎉',
                error: '❌',
                info: 'ℹ️'
            };
            
            const colorMap = {
                success: '#51cf66',
                error: '#ff6b6b',
                info: '#339af0'
            };
            
            modal = document.createElement('div');
            modal.id = 'cart-popup-modal';
            modal.innerHTML = `
                <div style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; 
                            background: rgba(0,0,0,0.5); display: flex; align-items: center; 
                            justify-content: center; z-index: 9999;">
                    <div style="background: white; border-radius: 12px; padding: 30px; 
                                max-width: 400px; width: 90%; text-align: center; 
                                border: 3px solid ${colorMap[config.type]};">
                        <div style="font-size: 3rem; margin-bottom: 16px;">
                            ${iconMap[config.type]}
                        </div>
                        <h4 style="margin-bottom: 16px; color: #333;">${config.title}</h4>
                        <p style="margin-bottom: 24px; color: #666;">${config.message}</p>
                        <div class="d-grid gap-2 d-md-flex justify-content-md-center" id="modal-buttons">
                            <!-- 按鈕會動態插入 -->
                        </div>
                    </div>
                </div>
            `;
            
            document.body.appendChild(modal);
            
            // 動態建立按鈕並綁定事件
            const buttonsContainer = modal.querySelector('#modal-buttons');
            config.buttons.forEach((btn, index) => {
                const button = document.createElement('button');
                button.className = `btn ${btn.class}`;
                button.textContent = btn.text;
                
                // 直接綁定事件處理器
                button.addEventListener('click', () => {
                    btn.action();
                });
                
                buttonsContainer.appendChild(button);
            });
        },
        
        // 隱藏彈窗
        hideModal() {
            const modal = document.getElementById('cart-popup-modal');
            if (modal) modal.remove();
        },
        
        // 更新購物車數量顯示 (如果頁面有購物車圖示)
        updateCartCount(totalItems) {
            const cartBadges = document.querySelectorAll('.cart-count, .cart-badge');
            cartBadges.forEach(badge => {
                badge.textContent = totalItems || 0;
                badge.style.display = totalItems > 0 ? 'inline' : 'none';
            });
        }
    };
    
    // 全域方法供產品頁面使用
    window.addToCartWithPopup = async function(proNo, proNum = 1) {
        try {
            if (!CartApiClient.isLoggedIn()) {
                CartApiClient.redirectToLogin();
                return;
            }
            
            const result = await CartApiClient.addToCart(proNo, proNum);
            
            // 假設回傳包含商品資訊
            CartPopup.showAddSuccess(
                result.productName || '商品',
                proNum,
                result.totalItems || 0
            );
            
        } catch (error) {
            CartPopup.showError(error.message);
        }
    };
    
})();