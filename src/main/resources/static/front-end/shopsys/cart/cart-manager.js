// cart-manager.js - 購物車頁面管理器，處理所有的UI邏輯

(function() {
    'use strict';
    
    window.CartManager = {
        // 內部狀態
        cartData: null,
        isLoading: false,
        
        // DOM 元素
        elements: {
            loading: null,
            loginPrompt: null,
            emptyCart: null,
            cartContainer: null,
            tableBody: null,
            totalItems: null,
            totalQuantity: null,
            subtotal: null,
            totalPrice: null,
            checkoutBtn: null,
            unavailableAlert: null,
            unavailableCount: null
        },
        
        // 初始化
        async init() {
            this.initElements();
            this.bindEvents();
            
            if (!CartApiClient.isLoggedIn()) {
                this.showLoginPrompt();
                return;
            }
            
            await this.loadCart();
        },
        
        // 初始化 DOM 元素
        initElements() {
            this.elements.loading = document.getElementById('loading');
            this.elements.loginPrompt = document.getElementById('login-prompt-container');
            this.elements.emptyCart = document.getElementById('empty-cart-container');
            this.elements.cartContainer = document.getElementById('cart-container');
            this.elements.tableBody = document.getElementById('cart-table-body');
            this.elements.totalItems = document.getElementById('total-items');
            this.elements.totalQuantity = document.getElementById('total-quantity');
            this.elements.subtotal = document.getElementById('subtotal');
            this.elements.totalPrice = document.getElementById('total-price');
            this.elements.checkoutBtn = document.getElementById('checkout-btn');
            this.elements.unavailableAlert = document.getElementById('unavailable-alert');
            this.elements.unavailableCount = document.getElementById('unavailable-count');
        },
        
        // 綁定事件
        bindEvents() {
            // 結帳按鈕
            if (this.elements.checkoutBtn) {
                this.elements.checkoutBtn.addEventListener('click', () => this.proceedToCheckout());
            }
            
            // 購物車表格點擊事件 (事件委派)
            if (this.elements.tableBody) {
                this.elements.tableBody.addEventListener('click', (e) => this.handleTableClick(e));
            }
        },
        
        // 載入購物車
        async loadCart() {
            if (this.isLoading) return;
            
            try {
                this.isLoading = true;
                this.showLoading(true);
                
                this.cartData = await CartApiClient.getCart();
                this.renderCart();
                
            } catch (error) {
                console.error('載入購物車錯誤:', error);
                this.showError('載入購物車失敗：' + error.message);
            } finally {
                this.isLoading = false;
                this.showLoading(false);
            }
        },
        
        // 渲染購物車
        renderCart() {
            this.hideAllContainers();
            
            if (!this.cartData || !this.cartData.item || this.cartData.item.length === 0) {
                this.showEmptyCart();
                return;
            }
            
            this.showCartContainer();
            this.renderCartItems();
            this.updateSummary();
            this.checkUnavailableItems();
        },
        
        // 渲染購物車項目
		renderCartItems() {
		    if (!this.elements.tableBody) return;
		    
		    const items = this.cartData.item || [];
		    
		    this.elements.tableBody.innerHTML = items.map(item => `
		        <tr class="${!item.available ? 'unavailable' : ''}" data-pro-no="${item.proNo}">
		            <!-- 商品資訊欄位 -->
		            <td>
		                <div class="product-info">
		                    <div class="product-name">
		                        ${item.proName}
		                        ${!item.available ? '<span class="unavailable-badge">缺貨</span>' : ''}
		                    </div>
		                    <div class="product-no">編號：${item.proNo}</div>
		                </div>
		            </td>
		            
		            <!-- 單價欄位 -->
		            <td style="text-align: center;">
		                <div class="product-price">${CartApiClient.formatPrice(item.proPrice)}</div>
		            </td>
		            
		            <!-- 數量欄位 -->
		            <td>
		                <div class="quantity-controls">
						
		                    <button class="quantity-btn decrease-btn" 
		                            data-pro-no="${item.proNo}" 
		                            data-quantity="${item.proNum - 1}"
		                            title="${item.proNum <= 1 ? '移除商品' : '減少數量'}">-</button>
		                    <input type="number" class="quantity-input" 
		                           value="${item.proNum}" 
		                           min="1" 
		                           data-pro-no="${item.proNo}">
		                    <button class="quantity-btn increase-btn" 
		                            data-pro-no="${item.proNo}" 
		                            data-quantity="${item.proNum + 1}">+</button>
		                </div>
		            </td>
		            
		            <!-- 小計欄位 -->
		            <td>
		                <div class="subtotal-price">${CartApiClient.formatPrice(item.subtotal)}</div>
		            </td>
		            
		            <!-- 操作欄位 -->
		            <td style="text-align: center;">
					
		                <button class="forum-btn danger remove-item-btn" 
		                        data-pro-no="${item.proNo}" 
		                        title="移除商品">🗑</button>
		            </td>
		        </tr>
		    `).join('');
		    
		    // 顯示表格
		    const table = document.getElementById('cart-table');
		    if (table) table.style.display = 'table';
		},
	
		
		
        
        // 處理表格點擊事件
		handleTableClick(event) {
		    const target = event.target.closest('button, input');
		    if (!target) return;
		    
		    const proNo = parseInt(target.dataset.proNo);
		    
		    if (target.classList.contains('decrease-btn')) {
		        const newQuantity = parseInt(target.dataset.quantity);
		        // 數量為0時直接移除商品
		        if (newQuantity <= 0) {
		            this.removeItem(proNo);
		        } else {
		            this.updateQuantity(proNo, newQuantity);
		        }
		    } else if (target.classList.contains('increase-btn')) {
		        const newQuantity = parseInt(target.dataset.quantity);
		        this.updateQuantity(proNo, newQuantity);
		    } else if (target.classList.contains('remove-item-btn')) {
		        // 使用 remove-item-btn 來移除單個商品
		        this.removeItem(proNo);
		    } else if (target.classList.contains('quantity-input')) {
		        // 處理手動輸入數量
		        target.addEventListener('change', () => {
		            const newQuantity = parseInt(target.value);
		            if (newQuantity >= 1) {
		                this.updateQuantity(proNo, newQuantity);
		            } else {
		                // 輸入0或負數時移除商品
		                this.removeItem(proNo);
		            }
		        });
		    }
		},
		
		
		
		
		
        
        // 更新商品數量
        async updateQuantity(proNo, newQuantity) {
            if (newQuantity < 1) return;
            
            try {
                await CartApiClient.updateQuantity(proNo, newQuantity);
                await this.loadCart();
                this.showSuccess('商品數量已更新');
            } catch (error) {
                this.showError('更新數量失敗：' + error.message);
            }
        },
        
        // 移除商品
        async removeItem(proNo) {
            if (!confirm('確定要移除此商品嗎？')) return;
            
            try {
                await CartApiClient.removeItem(proNo);
                await this.loadCart();
                this.showSuccess('商品已移除');
            } catch (error) {
                this.showError('移除商品失敗：' + error.message);
            }
        },
        
        // 清空購物車
        async clearCart() {
            if (!confirm('確定要清空購物車嗎？')) return;
            
            try {
                await CartApiClient.clearCart();
                await this.loadCart();
                this.showSuccess('購物車已清空');
            } catch (error) {
                this.showError('清空購物車失敗：' + error.message);
            }
        },
        
        // 前往結帳
        proceedToCheckout() {
            if (!this.cartData || !this.cartData.item || this.cartData.item.length === 0) {
                this.showError('購物車是空的');
                return;
            }
            
            // 檢查是否有可購買的商品
            const availableItems = this.cartData.item.filter(item => item.available);
            if (availableItems.length === 0) {
                this.showError('購物車中沒有可購買的商品');
                return;
            }
            
            // 跳轉到結帳頁面 (根據專案結構調整路徑)
            window.location.href = '/front-end/shopsys/order/checkout.html';
        },
        
        // 更新摘要
        updateSummary() {
            if (!this.cartData) return;
            
            const totalItems = this.cartData.item ? this.cartData.item.length : 0;
            const totalQuantity = this.cartData.totalQuantity || 0;
            const totalPrice = this.cartData.totalPrice || 0;
            
            if (this.elements.totalItems) this.elements.totalItems.textContent = `${totalItems} 種`;
            if (this.elements.totalQuantity) this.elements.totalQuantity.textContent = `${totalQuantity} 件`;
            if (this.elements.subtotal) this.elements.subtotal.textContent = totalPrice.toLocaleString();
            if (this.elements.totalPrice) this.elements.totalPrice.textContent = totalPrice.toLocaleString();
        },
        
        // 檢查無法購買的商品
        checkUnavailableItems() {
            if (!this.cartData || !this.cartData.item) return;
            
            const unavailableItems = this.cartData.item.filter(item => !item.available);
            
            if (unavailableItems.length > 0) {
                if (this.elements.unavailableAlert) {
                    this.elements.unavailableAlert.style.display = 'block';
                }
                if (this.elements.unavailableCount) {
                    this.elements.unavailableCount.textContent = unavailableItems.length;
                }
            } else {
                if (this.elements.unavailableAlert) {
                    this.elements.unavailableAlert.style.display = 'none';
                }
            }
        },
        
        // 顯示狀態管理
		hideAllContainers() {
		    [this.elements.loading, this.elements.loginPrompt, 
		     this.elements.emptyCart, this.elements.cartContainer]
		    .forEach(el => {
		        if (el) {
		            el.style.display = 'none';
		            el.classList.add('d-none');
		        }
		    });
		},

		showLoading(show) {
		    if (this.elements.loading) {
		        if (show) {
		            this.elements.loading.style.display = 'block';
		            this.elements.loading.classList.remove('d-none');
		        } else {
		            this.elements.loading.style.display = 'none';
		            this.elements.loading.classList.add('d-none');
		        }
		    }
		},

		showLoginPrompt() {
		    this.hideAllContainers();
		    if (this.elements.loginPrompt) {
		        this.elements.loginPrompt.style.display = 'block';
		        this.elements.loginPrompt.classList.remove('d-none');
		    }
		},

		showEmptyCart() {
		    this.hideAllContainers();
		    if (this.elements.emptyCart) {
		        this.elements.emptyCart.style.display = 'block';
		        this.elements.emptyCart.classList.remove('d-none');
		    }
		},

		showCartContainer() {
		    this.hideAllContainers();
		    if (this.elements.cartContainer) {
		        this.elements.cartContainer.style.display = 'block';
		        this.elements.cartContainer.classList.remove('d-none');
		    }
		},
		

		
        
        // 訊息提示
        showSuccess(message) {
            this.showAlert('success', '✅', message);
        },
        
        showError(message) {
            this.showAlert('error', '❌', message);
        },
        
        showAlert(type, icon, message) {
            const alert = document.getElementById('cute-alert');
            if (!alert) return;
            
            const alertIcon = document.getElementById('alert-icon');
            const alertMessage = document.getElementById('alert-message');
            
            alert.className = `cute-alert ${type}`;
            if (alertIcon) alertIcon.textContent = icon;
            if (alertMessage) alertMessage.textContent = message;
            
            alert.style.display = 'block';
            
            setTimeout(() => {
                alert.style.display = 'none';
            }, 3000);
        }
    };
    
    // 全域方法 (供 HTML 中的 onclick 使用)
    window.clearCart = () => CartManager.clearCart();
    window.proceedToCheckout = () => CartManager.proceedToCheckout();
    
    // 導航方法
    window.goToShop = () => window.location.href = '/front-end/shopsys/shopindex.html';
    window.continueShopping = () => window.location.href = '/front-end/shopsys/product.html';
    window.goToLogin = () => {
        const currentUrl = encodeURIComponent(window.location.pathname);
        window.location.href = `/front-end/mem/MemberLogin.html?redirect=${currentUrl}`;    //登入頁面
    };
    window.goToRegister = () => window.location.href = '/front-end/mem/MemRegisterPage.html';  //註冊頁面
    
})();