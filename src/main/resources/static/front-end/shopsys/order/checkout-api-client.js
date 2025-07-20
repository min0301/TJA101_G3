// checkout-api-client.js - 結帳 API 客戶端
// 使用 IIFE 避免污染全域範疇

(function() {
    'use strict';
    
    window.CheckoutApiClient = {
        baseUrl: '/api',
        
        // 取得認證資訊
        getAuthToken() {
            return localStorage.getItem('jwt');
        },
        
        getCurrentMemNo() {
            const memberInfo = JSON.parse(localStorage.getItem('memberInfo') || '{}');
            return memberInfo.id || null;
        },
        
        getCurrentMemberInfo() {
            return JSON.parse(localStorage.getItem('memberInfo') || '{}');
        },
        
        isLoggedIn() {
            const token = this.getAuthToken();
            const memberInfo = this.getCurrentMemNo();
            
            if (!token || !memberInfo) return false;
            
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                return payload.role === 'ROLE_USER';
            } catch (error) {
                console.error('JWT 解析錯誤:', error);
                return false;
            }
        },
        
        // 通用請求方法
        async request(url, options = {}) {
            const token = this.getAuthToken();
            const headers = { ...options.headers };
            
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            try {
                const response = await fetch(url, { ...options, headers });
                
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
        
        // ========== 結帳相關 API ========== //
        
        // 取得購物車資料（CartService 已包含完整庫存資訊）
        async getCartData() {
            const memNo = this.getCurrentMemNo();
            if (!memNo) throw new Error('未登入');
            
            const url = `${this.baseUrl}/cart/${memNo}`;
            const cartData = await this.request(url, {
                headers: { 'Content-Type': 'application/json' }
            });
            
            // CartService 已經提供完整的庫存資訊，包括：
            // - hasStockIssue: 是否有庫存問題
            // - stockWarning: 庫存警告訊息  
            // - proStatus: 商品狀態 (0=上架, 1=下架)
            // 
            // 根據 CartService 邏輯：
            // - 預購商品：proStatus="預購" 或 "預購中"，庫存來自 Redis
            // - 現貨商品：proStatus="上架" 或 "已發售"，庫存來自序號表
            
            // 為每個商品補充 hasStock 欄位（用於 UI 顯示）
            if (cartData.item && cartData.item.length > 0) {
                for (let item of cartData.item) {
                    // 根據 proStatus 判斷是否為預購商品
                    const isPreOrder = item.proStatus === "預購" || item.proStatus === "預購中";
                    
                    // 如果有庫存問題，通常是預購商品或庫存不足的現貨
                    // 這裡我們根據 stockWarning 的內容來推斷
                    if (item.stockWarning) {
                        if (item.stockWarning.includes("預購")) {
                            item.hasStock = false; // 預購商品
                        } else {
                            item.hasStock = true;  // 現貨但庫存不足
                        }
                    } else {
                        // 沒有警告，根據是否為預購商品判斷
                        item.hasStock = !isPreOrder;
                    }
                }
            }
            
            return cartData;
        },
        
        // 查詢單一商品庫存
        async getProductStock(productId) {
            const url = `${this.baseUrl}/cart/stock/${productId}`;
            return await this.request(url);
        },
        
        // 從購物車建立訂單並付款
        async checkoutFromCart(contactEmail, contactPhone) {
            const url = `${this.baseUrl}/checkout-from-cart`;
            
            const requestData = {
                contactEmail: contactEmail,
                contactPhone: contactPhone || null
            };
            
            console.log('發送結帳請求:', requestData);
            
            return await this.request(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData)
            });
        },
        
        // 發起付款（取得綠界表單）
        async initiatePayment(orderNo) {
            const url = `${this.baseUrl}/orders/${orderNo}/payment`;
            
            // 這個 API 回傳 HTML 表單，不是 JSON
            const token = this.getAuthToken();
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || '發起付款失敗');
            }
            
            // 回傳 HTML 內容
            return await response.text();
        },
        
        // 查詢付款狀態
        async getPaymentStatus(orderNo) {
            const url = `${this.baseUrl}/orders/${orderNo}/payment/status`;
            return await this.request(url);
        },
        
        // ========== 工具方法 ========== //
        
        // 價格格式化
        formatPrice(price) {
            return price.toLocaleString();
        },
        
        // 日期格式化
        formatDate(dateString) {
            return new Date(dateString).toLocaleString('zh-TW');
        },
        
        // Email 格式驗證
        validateEmail(email) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return emailRegex.test(email);
        },
        
        // 電話格式驗證（寬鬆驗證）
        validatePhone(phone) {
            if (!phone) return true; // 電話是選填
            
            // 移除所有空格和特殊字符
            const cleanPhone = phone.replace(/[\s\-\(\)\+]/g, '');
            
            // 寬鬆驗證：至少7位數字，最多15位數字（國際標準）
            const phoneRegex = /^\d{7,15}$/;
            
            if (!phoneRegex.test(cleanPhone)) {
                return false;
            }
            
            // 額外檢查：台灣格式給予提示但不強制
            const taiwanRegex = /^(09\d{8}|0[2-8]\d{7,8})$/;
            if (taiwanRegex.test(cleanPhone)) {
                return true; // 台灣格式，完全正確
            }
            
            return true; // 其他格式也接受，但會顯示提示
        },
        
        // 取得商品狀態文字 (根據 PRO_STATUS 欄位，實際是中文)
        getProductStatusText(status) {
            const statusMap = {
                '預購': '預購中',        // PRO_STATUS="預購" → 顯示"預購中"
                '預購中': '預購中',      // PRO_STATUS="預購中" → 顯示"預購中" 
                '上架': '上架中',        // PRO_STATUS="上架" → 顯示"上架中"
                '已發售': '上架中',      // PRO_STATUS="已發售" → 顯示"上架中"
                '下架': '已下架',        // PRO_STATUS="下架" → 顯示"已下架"
                
                // 備用：如果是數字格式（但實際應該是中文）
                '0': '上架中',
                '1': '已下架'
            };
            return statusMap[status] || status;
        },
        
        // 取得商品類型文字（根據 PRO_STATUS 判斷現貨/預購）
        getProductTypeText(proStatus) {
            // 根據 CartService 的邏輯判斷
            if (proStatus === "預購" || proStatus === "預購中") {
                return "預購商品";
            } else if (proStatus === "上架" || proStatus === "已發售") {
                return "現貨商品";
            } else {
                return "商品";
            }
        },
        
        // 取得商品類型樣式
        getProductTypeClass(proStatus) {
            if (proStatus === "預購" || proStatus === "預購中") {
                return "bg-warning";  // 黃色 - 預購
            } else if (proStatus === "上架" || proStatus === "已發售") {
                return "bg-success";  // 綠色 - 現貨
            } else {
                return "bg-secondary"; // 灰色 - 其他
            }
        },
        
        // 取得庫存警告樣式
        getStockWarningClass(hasStockIssue) {
            return hasStockIssue ? 'text-danger' : 'text-warning';
        }
    };
    
})();