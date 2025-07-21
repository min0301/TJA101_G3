// checkout.js - 結帳頁面管理器
// 使用 IIFE 避免污染全域範疇

(function() {
    'use strict';

    window.CheckoutManager = {
        // 內部狀態
        cartData: null,
        memberInfo: null,
        isLoading: false,
        isProcessing: false,

        // DOM 元素
        elements: {
            loading: null,
            loginPrompt: null,
            emptyCart: null,
            error: null,
            container: null,
            
            // 表單元素
            checkoutForm: null,
            contactEmail: null,
            contactPhone: null,
            checkoutBtn: null,
            
            // 顯示元素
            cartItemsContainer: null,
            stockWarningContainer: null,
            stockWarningText: null,
            memberName: null,
            memberEmail: null,
            
            // 摘要元素
            summaryItems: null,
            summaryQuantity: null,
            summarySubtotal: null,
            summaryTotal: null,
            checkoutBtnText: null,
            
            // Modal
            paymentModal: null
        },

        // 初始化
        async init() {
            this.initElements();
            this.bindEvents();
            
            // 直接載入結帳資料，讓後端 API 處理驗證
            // 如果未登入，後端會回傳 401 錯誤，會自動導向登入頁面
            await this.loadCheckoutData();
        },

        // 初始化 DOM 元素
        initElements() {
            this.elements.loading = document.getElementById('loading-container');
            this.elements.loginPrompt = document.getElementById('login-prompt-container');
            this.elements.emptyCart = document.getElementById('empty-cart-container');
            this.elements.error = document.getElementById('error-container');
            this.elements.container = document.getElementById('checkout-container');
            
            // 表單元素
            this.elements.checkoutForm = document.getElementById('checkout-form');
            this.elements.contactEmail = document.getElementById('contact-email');
            this.elements.contactPhone = document.getElementById('contact-phone');
            this.elements.checkoutBtn = document.getElementById('checkout-btn');
            
            // 顯示元素
            this.elements.cartItemsContainer = document.getElementById('cart-items-container');
            this.elements.stockWarningContainer = document.getElementById('stock-warning-container');
            this.elements.stockWarningText = document.getElementById('stock-warning-text');
            this.elements.memberName = document.getElementById('member-name');
            this.elements.memberEmail = document.getElementById('member-email');
            
            // 摘要元素
            this.elements.summaryItems = document.getElementById('summary-items');
            this.elements.summaryQuantity = document.getElementById('summary-quantity');
            this.elements.summarySubtotal = document.getElementById('summary-subtotal');
            this.elements.summaryTotal = document.getElementById('summary-total');
            this.elements.checkoutBtnText = document.getElementById('checkout-btn-text');
            
            // Modal
            this.elements.paymentModal = new bootstrap.Modal(document.getElementById('payment-modal'));
        },

        // 綁定事件
        bindEvents() {
            // 結帳按鈕
            if (this.elements.checkoutBtn) {
                this.elements.checkoutBtn.addEventListener('click', () => this.handleCheckout());
            }
            
            // 表單驗證
            if (this.elements.checkoutForm) {
                this.elements.checkoutForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.handleCheckout();
                });
                
                // 即時驗證
                this.elements.contactEmail.addEventListener('blur', () => this.validateEmail());
                this.elements.contactPhone.addEventListener('blur', () => this.validatePhone());
            }
        },

        // 載入結帳資料
        async loadCheckoutData() {
            if (this.isLoading) return;
            
            try {
                this.isLoading = true;
                this.showLoading();
                
                // 載入會員資訊
                this.memberInfo = CheckoutApiClient.getCurrentMemberInfo();
                
                // 載入購物車資料
                this.cartData = await CheckoutApiClient.getCartData();
                
                console.log('載入的結帳資料:', {
                    memberInfo: this.memberInfo,
                    cartData: this.cartData
                });
                
                // 檢查購物車
                if (!this.cartData || !this.cartData.item || this.cartData.item.length === 0) {
                    this.showEmptyCart();
                    return;
                }
                
                // 渲染結帳頁面
                this.renderCheckoutPage();
                this.showContainer();
                
            } catch (error) {
                console.error('載入結帳資料錯誤:', error);
                
                // 如果是 401 錯誤（未登入），導向登入頁面
                if (error.message.includes('登入已過期') || error.message.includes('請先登入')) {
                    window.location.href = '/front-end/mem/MemberLogin.html?redirect=' + encodeURIComponent(window.location.pathname);
                    return;
                }
                
                this.showError('載入結帳資料失敗：' + error.message);
            } finally {
                this.isLoading = false;
            }
        },

        // 渲染結帳頁面
        renderCheckoutPage() {
            this.renderCartItems();
            this.renderMemberInfo();
            this.fillDefaultContactInfo();
            this.updateSummary();
            this.checkStockIssues();
        },

        // 渲染購物車商品
        renderCartItems() {
            if (!this.elements.cartItemsContainer || !this.cartData.item) return;
            
            const itemsHtml = this.cartData.item.map(item => {
                const isAvailable = !item.hasStockIssue;
                const statusText = CheckoutApiClient.getProductStatusText(item.proStatus);
                const typeText = CheckoutApiClient.getProductTypeText(item.proStatus);  // 傳入 proStatus
                const typeClass = CheckoutApiClient.getProductTypeClass(item.proStatus); // 傳入 proStatus
                
                return `
                    <div class="cart-item ${!isAvailable ? 'unavailable' : ''}" data-pro-no="${item.proNo}">
                        <div class="row align-items-center py-3">
                            <div class="col-md-6">
                                <div class="d-flex align-items-center">
                                    <div class="item-icon me-3">
                                        <i class="bi bi-controller fs-3 text-primary"></i>
                                    </div>
                                    <div>
                                        <h6 class="mb-1">${item.proName}</h6>
                                        <small class="text-muted">編號：${item.proNo}</small>
                                        <div class="mt-1">
                                            <span class="badge bg-primary">${statusText}</span>
                                            <span class="badge ${typeClass} ms-1">${typeText}</span>
                                            ${!isAvailable ? '<span class="badge bg-danger ms-1">庫存不足</span>' : ''}
                                        </div>
                                        ${item.stockWarning ? `
                                            <div class="stock-warning mt-1">
                                                <small class="text-warning">
                                                    <i class="bi bi-exclamation-triangle me-1"></i>
                                                    ${item.stockWarning}
                                                </small>
                                            </div>
                                        ` : ''}
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-2 text-center">
                                <span class="fw-bold">NT$ ${CheckoutApiClient.formatPrice(item.proPrice)}</span>
                            </div>
                            <div class="col-md-2 text-center">
                                <span class="quantity-badge">${item.proNum}</span>
                            </div>
                            <div class="col-md-2 text-end">
                                <span class="fw-bold text-primary">NT$ ${CheckoutApiClient.formatPrice(item.subtotal)}</span>
                            </div>
                        </div>
                    </div>
                `;
            }).join('');
            
            this.elements.cartItemsContainer.innerHTML = itemsHtml;
        },

        // 渲染會員資訊
        renderMemberInfo() {
            if (this.elements.memberName && this.memberInfo.memName) {
                this.elements.memberName.textContent = this.memberInfo.memName;
            }
            
            if (this.elements.memberEmail && this.memberInfo.memEmail) {
                this.elements.memberEmail.textContent = this.memberInfo.memEmail;
            }
        },

        // 填入預設聯絡資訊
        fillDefaultContactInfo() {
            // 預設使用會員註冊的 Email
            if (this.elements.contactEmail && this.memberInfo.memEmail) {
                this.elements.contactEmail.value = this.memberInfo.memEmail;
            }
        },

        // 更新訂單摘要
        updateSummary() {
            if (!this.cartData) return;
            
            const totalItems = this.cartData.item ? this.cartData.item.length : 0;
            const totalQuantity = this.cartData.totalQuantity || 0;
            const totalPrice = this.cartData.totalPrice || 0;
            
            if (this.elements.summaryItems) this.elements.summaryItems.textContent = `${totalItems} 種`;
            if (this.elements.summaryQuantity) this.elements.summaryQuantity.textContent = `${totalQuantity} 件`;
            if (this.elements.summarySubtotal) this.elements.summarySubtotal.textContent = CheckoutApiClient.formatPrice(totalPrice);
            if (this.elements.summaryTotal) this.elements.summaryTotal.textContent = CheckoutApiClient.formatPrice(totalPrice);
        },

        // 檢查庫存問題
        checkStockIssues() {
            if (!this.cartData || !this.cartData.item) return;
            
            const unavailableItems = this.cartData.item.filter(item => item.hasStockIssue);
            const warningItems = this.cartData.item.filter(item => item.stockWarning && !item.hasStockIssue);
            
            if (unavailableItems.length > 0) {
                // 有無法購買的商品
                this.showStockWarning(`有 ${unavailableItems.length} 項商品庫存不足，無法結帳。請回到購物車調整數量。`);
                this.disableCheckout('商品庫存不足');
            } else if (warningItems.length > 0) {
                // 有庫存警告但仍可購買
                this.showStockWarning(`有 ${warningItems.length} 項商品庫存偏低，但仍可正常購買。`);
                this.enableCheckout();
            } else {
                // 無庫存問題
                this.hideStockWarning();
                this.enableCheckout();
            }
        },

        // 顯示庫存警告
        showStockWarning(message) {
            if (this.elements.stockWarningContainer && this.elements.stockWarningText) {
                this.elements.stockWarningText.textContent = message;
                this.elements.stockWarningContainer.classList.remove('d-none');
            }
        },

        // 隱藏庫存警告
        hideStockWarning() {
            if (this.elements.stockWarningContainer) {
                this.elements.stockWarningContainer.classList.add('d-none');
            }
        },

        // 啟用結帳
        enableCheckout() {
            if (this.elements.checkoutBtn) {
                this.elements.checkoutBtn.disabled = false;
                this.elements.checkoutBtn.classList.remove('btn-secondary');
                this.elements.checkoutBtn.classList.add('btn-primary');
            }
            if (this.elements.checkoutBtnText) {
                this.elements.checkoutBtnText.textContent = '前往付款';
            }
        },

        // 禁用結帳
        disableCheckout(reason) {
            if (this.elements.checkoutBtn) {
                this.elements.checkoutBtn.disabled = true;
                this.elements.checkoutBtn.classList.remove('btn-primary');
                this.elements.checkoutBtn.classList.add('btn-secondary');
            }
            if (this.elements.checkoutBtnText) {
                this.elements.checkoutBtnText.textContent = reason || '無法結帳';
            }
        },

        // 表單驗證
        validateForm() {
            let isValid = true;
            
            // 清除之前的驗證狀態
            this.elements.checkoutForm.classList.remove('was-validated');
            
            // 驗證 Email
            if (!this.validateEmail()) {
                isValid = false;
            }
            
            // 驗證電話（選填）
            if (!this.validatePhone()) {
                isValid = false;
            }
            
            // 顯示驗證結果
            this.elements.checkoutForm.classList.add('was-validated');
            
            return isValid;
        },

        // 驗證 Email
        validateEmail() {
            const email = this.elements.contactEmail.value.trim();
            const isValid = email && CheckoutApiClient.validateEmail(email);
            
            if (isValid) {
                this.elements.contactEmail.classList.remove('is-invalid');
                this.elements.contactEmail.classList.add('is-valid');
            } else {
                this.elements.contactEmail.classList.remove('is-valid');
                this.elements.contactEmail.classList.add('is-invalid');
            }
            
            return isValid;
        },

        // 驗證電話
        validatePhone() {
            const phone = this.elements.contactPhone.value.trim();
            
            if (phone === '') {
                // 空值是允許的
                this.elements.contactPhone.classList.remove('is-invalid', 'is-valid');
                return true;
            }
            
            const isValid = CheckoutApiClient.validatePhone(phone);
            
            if (isValid) {
                this.elements.contactPhone.classList.remove('is-invalid');
                this.elements.contactPhone.classList.add('is-valid');
                
                // 檢查是否為台灣格式，給予友善提示
                const cleanPhone = phone.replace(/[\s\-\(\)\+]/g, '');
                const taiwanRegex = /^(09\d{8}|0[2-8]\d{7,8})$/;
                
                if (!taiwanRegex.test(cleanPhone) && cleanPhone.length >= 7) {
                    // 非台灣格式但有效，顯示提示
                    this.showAlert('info', 'ℹ️', '偵測到非台灣電話格式，如有問題請聯繫客服');
                }
                
                return true;
            } else {
                this.elements.contactPhone.classList.remove('is-valid');
                this.elements.contactPhone.classList.add('is-invalid');
                return false;
            }
        },

        // 處理結帳
        async handleCheckout() {
            if (this.isProcessing) return;
            
            try {
                // 驗證表單
                if (!this.validateForm()) {
                    this.showError('請檢查並修正表單錯誤');
                    return;
                }
                
                // 檢查庫存
                const unavailableItems = this.cartData.item.filter(item => item.hasStockIssue);
                if (unavailableItems.length > 0) {
                    this.showError('購物車中有商品庫存不足，請先調整數量');
                    return;
                }
                
                this.isProcessing = true;
                this.showPaymentModal();
                
                // 取得表單資料
                const contactEmail = this.elements.contactEmail.value.trim();
                const contactPhone = this.elements.contactPhone.value.trim() || null;
                
                console.log('提交結帳資料:', { contactEmail, contactPhone });
                
                // 1. 建立訂單
                const orderResult = await CheckoutApiClient.checkoutFromCart(contactEmail, contactPhone);
                
                if (!orderResult.success) {
                    throw new Error(orderResult.message || '建立訂單失敗');
                }
                
                console.log('訂單建立成功:', orderResult);
                
                // 2. 發起付款
                const paymentForm = await CheckoutApiClient.initiatePayment(orderResult.orderNo);
                
                // 3. 自動提交綠界付款表單
                this.submitPaymentForm(paymentForm);
                
            } catch (error) {
                console.error('結帳處理錯誤:', error);
                this.hidePaymentModal();
                this.showError('結帳失敗：' + error.message);
            } finally {
                this.isProcessing = false;
            }
        },

        // 提交付款表單
        submitPaymentForm(htmlForm) {
            try {
                // 隱藏 Modal
                this.hidePaymentModal();
                
                // 建立臨時容器
                const tempContainer = document.createElement('div');
                tempContainer.innerHTML = htmlForm;
                tempContainer.style.display = 'none';
                document.body.appendChild(tempContainer);
                
                // 找到表單並提交
                const form = tempContainer.querySelector('form');
                if (form) {
                    console.log('準備提交綠界付款表單');
                    
                    // 顯示跳轉訊息
                    this.showSuccess('訂單建立成功，正在跳轉到付款頁面...');
                    
                    // 提交表單
                    setTimeout(() => {
                        form.submit();
                    }, 1000);
                } else {
                    throw new Error('無法找到付款表單');
                }
                
            } catch (error) {
                console.error('提交付款表單錯誤:', error);
                this.showError('跳轉付款頁面失敗：' + error.message);
            }
        },

        // 顯示付款處理 Modal
        showPaymentModal() {
            if (this.elements.paymentModal) {
                this.elements.paymentModal.show();
            }
        },

        // 隱藏付款處理 Modal
        hidePaymentModal() {
            if (this.elements.paymentModal) {
                this.elements.paymentModal.hide();
            }
        },

        // 顯示狀態管理
        hideAllContainers() {
            [this.elements.loading, this.elements.loginPrompt, 
             this.elements.emptyCart, this.elements.error, this.elements.container]
            .forEach(el => {
                if (el) {
                    el.classList.add('d-none');
                }
            });
        },

        showLoading() {
            this.hideAllContainers();
            if (this.elements.loading) {
                this.elements.loading.classList.remove('d-none');
            }
        },

        showLoginPrompt() {
            this.hideAllContainers();
            if (this.elements.loginPrompt) {
                this.elements.loginPrompt.classList.remove('d-none');
            }
        },

        showEmptyCart() {
            this.hideAllContainers();
            if (this.elements.emptyCart) {
                this.elements.emptyCart.classList.remove('d-none');
            }
        },

        showError(message) {
            this.hideAllContainers();
            const errorMessage = document.getElementById('error-message');
            if (errorMessage) {
                errorMessage.textContent = message;
            }
            if (this.elements.error) {
                this.elements.error.classList.remove('d-none');
            }
        },

        showContainer() {
            this.hideAllContainers();
            if (this.elements.container) {
                this.elements.container.classList.remove('d-none');
            }
        },

        // 訊息提示
        showSuccess(message) {
            this.showAlert('success', '✅', message);
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

})();