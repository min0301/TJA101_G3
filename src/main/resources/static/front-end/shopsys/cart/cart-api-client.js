// cart-api-client.js - 購物車功能專用的 API 客戶端工具

// cart-api-client.js - 購物車功能專用的 API 客戶端工具

(function() {
    'use strict';
    
    window.CartApiClient = {
        baseUrl: '/api',
        
        // 取得認證資訊
        getAuthToken() {
            return localStorage.getItem('jwt');
        },
        
        getCurrentMemNo() {
            const memberInfo = JSON.parse(localStorage.getItem('memberInfo') || '{}');
            return memberInfo.id || null;
        },
        
        isLoggedIn() {
            const token = this.getAuthToken();
            const memberInfo = this.getCurrentMemNo();
            
            // 確保是會員登入（不是管理員）
            if (!token || !memberInfo) return false;
            
            try {
                // 解析 JWT 檢查角色
                const payload = JSON.parse(atob(token.split('.')[1]));
                return payload.role === 'ROLE_USER'; // 只允許會員使用購物車
            } catch (error) {
                console.error('JWT 解析錯誤:', error);
                return false;
            }
        },
        
        // 通用請求方法 - 移除預設 Content-Type
        async request(url, options = {}) {
            const token = this.getAuthToken();
            const headers = {
                // 移除預設的 Content-Type，讓各個方法自己決定
                ...options.headers
            };
            
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            try {
                const response = await fetch(url, {
                    ...options,
                    headers
                });
                
                // 處理各種錯誤狀況
                if (response.status === 401) {
                    this.handleAuthError();
                    throw new Error('登入已過期，請重新登入');
                }
                
                if (response.status === 403) {
                    throw new Error('權限不足');
                }
                
                if (response.status === 404) {
                    throw new Error('請求的資源不存在');
                }
                
                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({}));
                    throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
                }
                
                return await response.json();
            } catch (error) {
                console.error('API 請求錯誤:', error);
                throw error;
            }
        },
        
        // 處理認證錯誤
        handleAuthError() {
            localStorage.removeItem('jwt');
            localStorage.removeItem('memberInfo');
            
            // 顯示登入過期提示
            this.showPixelModal('登入已過期，請重新登入', () => {
                this.redirectToLogin();
            });
        },
        
        // 像素風格提示框
        showPixelModal(message, callback) {
            let modal = document.getElementById('pixel-modal');
            if (modal) modal.remove();

            modal = document.createElement('div');
            modal.id = 'pixel-modal';
            modal.innerHTML = `
                <div style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; 
                            background: rgba(30,28,38,0.88); display: flex; align-items: center; 
                            justify-content: center; z-index: 9999;">
                    <div style="background: #18122B; border: 4px solid #FFD700; border-radius: 18px; 
                                padding: 44px; text-align: center; max-width: 90vw;">
                        <div style="font-family: 'Press Start 2P', monospace; font-size: 1.5rem; 
                                    color: #FFD700; margin-bottom: 32px;">${message}</div>
                        <button onclick="this.closest('#pixel-modal').remove(); if(window.tempCallback) window.tempCallback();"
                                style="font-family: 'Press Start 2P', monospace; font-size: 1rem; 
                                       padding: 10px 32px; background: #FFD700; color: #18122B; 
                                       border: none; border-radius: 8px; cursor: pointer;">OK</button>
                    </div>
                </div>
            `;
            
            window.tempCallback = callback;
            document.body.appendChild(modal);
        },
        
        // 跳轉登入頁面
        redirectToLogin() {
            const currentUrl = encodeURIComponent(window.location.pathname);
            window.location.href = `/front-end/mem/MemberLogin.html?redirect=${currentUrl}`;
        },
        
        // ========== 購物車 API 方法 ========== //
        
        // 加入商品到購物車
        async addToCart(proNo, proNum) {
            const url = `${this.baseUrl}/cart/add`;
            const formData = new URLSearchParams({
                proNo: proNo,
                proNum: proNum
            });
            
            return await this.request(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });
        },
        
        // 取得購物車
        async getCart() {
            const memNo = this.getCurrentMemNo();
            if (!memNo) throw new Error('未登入');
            
            const url = `${this.baseUrl}/cart/${memNo}`;
            return await this.request(url, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        },
        
        // 更新商品數量
        async updateQuantity(proNo, proNum) {
            const url = `${this.baseUrl}/cart/update/${proNo}`;
            const formData = new URLSearchParams({ proNum: proNum });
            
            return await this.request(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });
        },
        
        // 移除商品
        async removeItem(proNo) {
            const url = `${this.baseUrl}/cart/remove/${proNo}`;
            return await this.request(url, { 
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        },
        
        // 清空購物車
		async clearCart() {
		    const token = this.getAuthToken();
		    const headers = {
		        'Content-Type': 'application/x-www-form-urlencoded'
		    };
		    
		    if (token) {
		        headers['Authorization'] = `Bearer ${token}`;
		    }
		    
		    const response = await fetch('/api/cart/clear', {
		        method: 'POST',
		        headers: headers
		    });
		    
		    if (response.ok) {
		        // 後端回傳純文字，不解析 JSON
		        return { success: true };
		    } else {
		        const errorText = await response.text();
		        throw new Error(errorText || '清空購物車失敗');
		    }
		},
		
		
		
        
        // 價格格式化
        formatPrice(price) {
            return `NT$ ${price.toLocaleString()}`;
        },
        
        // 日期格式化
        formatDate(dateString) {
            return new Date(dateString).toLocaleString('zh-TW');
        }
    };
})();