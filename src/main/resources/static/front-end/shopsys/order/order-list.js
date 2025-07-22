// order-list.js - 訂單列表頁面功能
// 使用 IIFE 避免污染全域範疇

(function() {
    'use strict';

    // ***訂單列表管理器 *** //
    window.OrderListManager = {
        
        // 內部資料
        allOrders: [],
        filteredOrders: [],
        currentFilter: 'all',
        currentSort: 'newest',

        // DOM 元素
        elements: {
            loading: null,
            error: null,
            empty: null,
            container: null,
            ordersContainer: null,
            filterButtons: null,
            sortSelect: null,
            filterTitle: null,
            orderCount: null
        },

		
        // *** 初始化 *** //
        async init() {
            this.initElements();
            this.bindEvents();
            await this.loadOrders();
        },

		
        // ***初始化 DOM 元素引用 *** //
        initElements() {
            this.elements.loading = document.getElementById('loading-container');
            this.elements.error = document.getElementById('error-container');
            this.elements.empty = document.getElementById('empty-container');
            this.elements.container = document.getElementById('order-list-container');
            this.elements.ordersContainer = document.getElementById('orders-container');
            this.elements.filterButtons = document.querySelectorAll('.order-filter-btn');
            this.elements.sortSelect = document.getElementById('sort-select');
            this.elements.filterTitle = document.getElementById('filter-title');
            this.elements.orderCount = document.getElementById('order-count');
        },

		
        // *** 綁定事件監聽器 *** //
        bindEvents() {
            // 篩選按鈕事件
            this.elements.filterButtons.forEach(btn => {
                btn.addEventListener('click', (e) => {
                    this.handleFilterChange(e.target.dataset.status);
                });
            });

            // 排序選擇事件
            this.elements.sortSelect.addEventListener('change', (e) => {
                this.handleSortChange(e.target.value);
            });

            // 訂單列表點擊事件 (事件委派)
            this.elements.ordersContainer.addEventListener('click', (e) => {
                this.handleOrderAction(e);
            });
        },

		
        // *** 載入訂單列表 *** //
        async loadOrders() {
            try {
                this.showLoading();
                
                const orders = await OrderApiClient.getOrderList();
                this.allOrders = orders || [];
                
                if (this.allOrders.length === 0) {
                    this.showEmpty();
                } else {
                    this.applyFiltersAndSort();
                    this.showOrderList();
                }

            } catch (error) {
                console.error('載入訂單失敗:', error);
                this.showError(error.message);
            }
        },

		
        // *** 處理篩選變更 *** //
		handleFilterChange(status) {
		    this.currentFilter = status;
		    
		    // 更新按鈕狀態
		    this.elements.filterButtons.forEach(btn => {
		        btn.classList.remove('btn-primary', 'active');
		        btn.classList.add('btn-outline-' + this.getButtonColor(btn.dataset.status));
		    });
		    
		    const activeBtn = document.querySelector(`[data-status="${status}"]`);
		    activeBtn.classList.remove('btn-outline-' + this.getButtonColor(status));
		    activeBtn.classList.add('btn-primary', 'active');

		    this.applyFiltersAndSort();
		    this.renderOrders();
		},

		
        // *** 處理排序變更 *** //
        handleSortChange(sortType) {
            this.currentSort = sortType;
            this.applyFiltersAndSort();
            this.renderOrders();
        },

		
        // *** 應用篩選和排序 *** //
		applyFiltersAndSort() {
		    // 篩選
		    if (this.currentFilter === 'all') {
		        this.filteredOrders = [...this.allOrders];
		    } else {
		        this.filteredOrders = this.allOrders.filter(order => {
		            // 修正：統一狀態比較邏輯
		            const normalizedOrderStatus = order.orderStatus.toUpperCase();
		            const normalizedFilterStatus = this.currentFilter.toUpperCase();
		            return normalizedOrderStatus === normalizedFilterStatus;
		        });
		    }

		    // 排序邏輯保持不變...
		    this.filteredOrders.sort((a, b) => {
		        switch (this.currentSort) {
		            case 'newest':
		                return new Date(b.orderDatetime) - new Date(a.orderDatetime);
		            case 'oldest':
		                return new Date(a.orderDatetime) - new Date(b.orderDatetime);
		            case 'amount-high':
		                return b.totalPrice - a.totalPrice;
		            case 'amount-low':
		                return a.totalPrice - b.totalPrice;
		            default:
		                return 0;
		        }
		    });

		    this.updateDisplayInfo();
		},

		
        // *** 更新顯示資訊 *** //
        updateDisplayInfo() {
            const filterTitles = {
                'all': '所有訂單',
                'PENDING': '等待付款訂單',
                'Paying': '付款處理中訂單',
                'Processing': '處理中訂單',
                'SHIPPED': '已出貨訂單',
                'Completed': '已完成訂單',
                'FAILED': '處理失敗訂單',
                'CANCELLED': '已取消訂單'
            };

            this.elements.filterTitle.textContent = filterTitles[this.currentFilter];
            this.elements.orderCount.textContent = `共 ${this.filteredOrders.length} 筆訂單`;
        },

		
        // *** 渲染訂單列表 *** //
        renderOrders() {
            if (this.filteredOrders.length === 0) {
                this.elements.ordersContainer.innerHTML = `
                    <div class="text-center p-5">
                        <i class="bi bi-search" style="font-size: 3rem; color: #6c757d;"></i>
                        <h5 class="mt-3 text-muted">沒有符合條件的訂單</h5>
                        <p class="text-muted">請嘗試其他篩選條件</p>
                    </div>
                `;
                return;
            }

            this.elements.ordersContainer.innerHTML = this.filteredOrders
                .map(order => this.createOrderCard(order))
                .join('');

            // 初始化 AOS 動畫
            setTimeout(() => {
                if (typeof AOS !== 'undefined') {
                    AOS.init({once: true});
                }
            }, 100);
        },

		
        // *** 建立訂單卡片 HTML *** //
        createOrderCard(order) {
            const orderDate = OrderApiClient.formatDate(order.orderDatetime);
            const totalPrice = OrderApiClient.formatPrice(order.orderTotal);
            const statusText = OrderApiClient.getOrderStatusText(order.orderStatus);
            const statusClass = OrderApiClient.getOrderStatusClass(order.orderStatus);
            const statusIcon = OrderApiClient.getOrderStatusIcon(order.orderStatus);

            return `
                <div class="card mb-4" data-aos="fade-up" data-order-no="${order.orderNo}">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="mb-0">
                                <i class="bi bi-receipt me-2"></i>
                                訂單編號：#${order.orderNo}
                            </h6>
                            <small class="text-muted">下單時間：${orderDate}</small>
                        </div>
                        <span class="${statusClass}">
                            <i class="bi ${statusIcon} me-1"></i>${statusText}
                        </span>
                    </div>
                    <div class="card-body">
                        <div class="row align-items-center">
                            <div class="col-md-8">
                                <div class="d-flex align-items-center mb-2">
                                    <i class="bi bi-envelope me-2 text-muted"></i>
                                    <span>聯絡信箱：${order.contactEmail || '未提供'}</span>
                                </div>
                                <div class="d-flex align-items-center mb-2">
                                    <i class="bi bi-telephone me-2 text-muted"></i>
                                    <span>聯絡電話：${order.contactPhone || '未提供'}</span>
                                </div>
                                <div class="d-flex align-items-center">
                                    <i class="bi bi-box me-2 text-muted"></i>
                                    <span>商品類型：虛擬遊戲產品 (序號發送)</span>
                                </div>
                                ${this.getOrderStatusDescription(order.orderStatus)}
                            </div>
                            <div class="col-md-4 text-md-end">
                                <div class="mb-2">
                                    <strong class="fs-5 text-primary">${totalPrice}</strong>
                                </div>
                                <div class="d-grid gap-2">
                                    <button class="btn btn-outline-primary btn-sm order-detail-btn" 
                                            data-order-no="${order.orderNo}">
                                        <i class="bi bi-eye me-1"></i>查看詳情
                                    </button>
                                    ${this.getOrderActionButtons(order)}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        },

		
        // *** 取得訂單狀態描述 (針對虛擬商品) *** //
		getOrderStatusDescription(status) {
		    // 修正：統一轉為大寫處理
		    const normalizedStatus = status.toUpperCase();
		    
		    const descriptions = {
		        'PENDING': '<div class="alert alert-warning alert-sm mt-2 mb-0"><i class="bi bi-clock me-1"></i>等待您完成付款</div>',
		        'PAYING': '<div class="alert alert-info alert-sm mt-2 mb-0"><i class="bi bi-hourglass-split me-1"></i>付款處理中，請稍候</div>',
		        'PROCESSING': '<div class="alert alert-info alert-sm mt-2 mb-0"><i class="bi bi-gear me-1"></i>付款成功，正在為您準備序號</div>',
		        'SHIPPED': '<div class="alert alert-success alert-sm mt-2 mb-0"><i class="bi bi-envelope-check me-1"></i>遊戲序號已發送至您的信箱</div>',
		        'COMPLETED': '<div class="alert alert-success alert-sm mt-2 mb-0"><i class="bi bi-check-circle me-1"></i>訂單完成，感謝您的購買</div>',
		        'FAILED': '<div class="alert alert-danger alert-sm mt-2 mb-0"><i class="bi bi-exclamation-triangle me-1"></i>付款或處理失敗，可重新嘗試</div>',
		        'CANCELLED': '<div class="alert alert-secondary alert-sm mt-2 mb-0"><i class="bi bi-x-circle me-1"></i>訂單已取消</div>'
		    };
		    return descriptions[normalizedStatus] || '';
		},

		
        // *** 根據訂單狀態取得對應的操作按鈕 *** //
		getOrderActionButtons(order) {
		    // 修正：統一狀態判斷
		    const normalizedStatus = order.orderStatus.toUpperCase();
		    
		    switch (normalizedStatus) {
		        case 'PENDING': // 等待付款
		            return `
		                <button class="btn btn-warning btn-sm payment-btn" 
		                        data-order-no="${order.orderNo}">
		                    <i class="bi bi-credit-card me-1"></i>立即付款
		                </button>
		            `;
		        case 'PAYING': // 付款處理中
		            return `
		                <button class="btn btn-primary btn-sm payment-status-btn" 
		                        data-order-no="${order.orderNo}">
		                    <i class="bi bi-hourglass-split me-1"></i>查看付款狀態
		                </button>
		            `;
		        case 'PROCESSING': // 處理中 (序號準備中)
		            return `
		                <span class="text-info">
		                    <i class="bi bi-gear me-1"></i>序號準備中
		                </span>
		            `;
		        case 'SHIPPED': // 已出貨 (序號已發送)
		            return `
		                <button class="btn btn-info btn-sm email-check-btn" 
		                        data-order-no="${order.orderNo}">
		                    <i class="bi bi-envelope-check me-1"></i>查看序號
		                </button>
		            `;
		        case 'COMPLETED': // 已完成
		            return `
		                <button class="btn btn-success btn-sm reorder-btn" 
		                        data-order-no="${order.orderNo}">
		                    <i class="bi bi-arrow-repeat me-1"></i>再次購買
		                </button>
		            `;
		        case 'FAILED': // 處理失敗
		            return `
		                <button class="btn btn-outline-warning btn-sm payment-retry-btn" 
		                        data-order-no="${order.orderNo}">
		                    <i class="bi bi-arrow-clockwise me-1"></i>重新付款
		                </button>
		                <button class="btn btn-outline-danger btn-sm contact-support-btn mt-1" 
		                        data-order-no="${order.orderNo}">
		                    <i class="bi bi-headset me-1"></i>聯繫客服
		                </button>
		            `;
		        case 'CANCELLED': // 已取消
		            return `
		                <span class="text-muted">
		                    <i class="bi bi-x-circle me-1"></i>訂單已取消
		                </span>
		            `;
		        default:
		            return '';
		    }
		},

		
        // *** 處理訂單相關操作 *** //
        handleOrderAction(event) {
            const target = event.target.closest('button');
            if (!target) return;

            const orderNo = target.dataset.orderNo;

            if (target.classList.contains('order-detail-btn')) {
                this.viewOrderDetail(orderNo);
            } else if (target.classList.contains('payment-btn')) {
                this.handlePayment(orderNo);
            } else if (target.classList.contains('payment-status-btn')) {
                this.checkPaymentStatus(orderNo);
            } else if (target.classList.contains('payment-retry-btn')) {
                this.handlePaymentRetry(orderNo);
            } else if (target.classList.contains('reorder-btn')) {
                this.handleReorder(orderNo);
            } else if (target.classList.contains('email-check-btn')) {
                this.handleEmailCheck(orderNo);
            } else if (target.classList.contains('contact-support-btn')) {
                this.handleContactSupport(orderNo);
            }
        },

		
        // *** 查看訂單詳情 *** //
        viewOrderDetail(orderNo) {
            // 跳轉到訂單詳情頁面
            window.location.href = `order-detail.html?orderNo=${orderNo}`;
        },

		
        // *** 處理付款 (跳轉到綠界) *** //
        handlePayment(orderNo) {
            if (confirm('確定要前往付款頁面嗎？')) {
                // 直接跳轉到後端付款 API，會返回綠界付款表單
                window.location.href = `/api/orders/${orderNo}/payment`;
            }
        },

		
        // *** 查看付款狀態 (AJAX 輪詢) *** //
        async checkPaymentStatus(orderNo) {
            try {
                const response = await OrderApiClient.request(`/api/orders/${orderNo}/payment/status`);
                
                if (response.success) {
                    const status = response.paymentStatus.status;
                    const orderStatus = response.orderStatus;
                    
                    if (status === 'SUCCESS') {
                        alert(`付款成功！\n訂單狀態：${OrderApiClient.getOrderStatusText(orderStatus)}\n\n頁面將重新載入以更新狀態。`);
                        window.location.reload();
                    } else if (status === 'FAILED') {
                        alert('付款失敗，請重新嘗試或聯繫客服。');
                        window.location.reload();
                    } else {
                        alert('付款仍在處理中，請稍後再查看。');
                    }
                } else {
                    alert('無法取得付款狀態，請稍後再試。');
                }
            } catch (error) {
                console.error('查詢付款狀態失敗:', error);
                alert('查詢付款狀態失敗：' + error.message);
            }
        },

		
        // *** 重新付款 *** //
        handlePaymentRetry(orderNo) {
            if (confirm('確定要重新付款嗎？')) {
                // 使用重新付款 API
                window.location.href = `/api/orders/${orderNo}/payment/retry`;
            }
        },

		
        // *** 再次購買 *** //
		async handleReorder(orderNo) {
		    if (!confirm('確定要將此訂單中的商品加入購物車嗎？')) return;
		    
		    try {
		        // 檢查是否登入
		        if (!CartApiClient.isLoggedIn()) {
		            alert('請先登入才能使用購物車功能');
		            return;
		        }
		        
		        // 先取得訂單詳情
		        const orderDetail = await OrderApiClient.getOrderDetail(orderNo);
		        const orderItems = orderDetail.orderItems;
		        
		        if (!orderItems || orderItems.length === 0) {
		            alert('此訂單沒有商品資訊');
		            return;
		        }
		        
		        let successCount = 0;
		        let errorCount = 0;
		        const errors = [];
		        
		        // 逐一加入每個商品到購物車
		        for (const item of orderItems) {
		            try {
		                await CartApiClient.addToCart(item.proNo, item.orderAmount);
		                successCount++;
		                console.log(`成功加入商品：${item.productName} x${item.orderAmount}`);
		            } catch (error) {
		                errorCount++;
		                errors.push(`${item.productName}: ${error.message}`);
		                console.error(`加入商品失敗：${item.productName}`, error);
		            }
		        }
		        
		        // 顯示結果
		        if (successCount > 0 && errorCount === 0) {
		            // 全部成功
		            alert(`成功將 ${successCount} 項商品加入購物車！\n\n點擊確定後將跳轉到購物車頁面。`);
		            window.location.href = '/front-end/shopsys/cart/cart.html';
		        } else if (successCount > 0 && errorCount > 0) {
		            // 部分成功
		            alert(`已成功加入 ${successCount} 項商品到購物車\n\n${errorCount} 項商品加入失敗：\n${errors.join('\n')}\n\n點擊確定後將跳轉到購物車頁面。`);
		            window.location.href = '/front-end/shopsys/cart/cart.html';
		        } else {
		            // 全部失敗
		            alert(`所有商品都加入失敗：\n${errors.join('\n')}\n\n請檢查商品庫存狀態或聯繫客服。`);
		        }
		        
		    } catch (error) {
		        console.error('再次購買失敗:', error);
		        alert('再次購買失敗：' + error.message);
		    }
		},

		
        // 查看序號 (序號已發送)
		
		handleEmailCheck(orderNo) {
		    alert(`遊戲序號已發送至您的信箱！\n\n請檢查您的信箱（包含垃圾信件匣）\n如未收到，請聯繫客服。\n\n訂單編號：${orderNo}\n\n💡 提示：您也可以在訂單詳情頁面查看更多資訊。`);
		},
	

		
        // *** 聯繫客服 *** //
        handleContactSupport(orderNo) {
            const supportMessage = `訂單編號：${orderNo}\n\n請客服協助處理此訂單問題。\n\n問題類型：付款或訂單處理問題\n發生時間：${new Date().toLocaleString()}\n\n詳細說明：（請描述遇到的問題）`;
            
            // 開啟客服對話或郵件
            if (confirm('將為您轉接至客服，是否繼續？')) {
                // 可以整合客服系統或開啟郵件
                window.open(`mailto:support@pixeltribe.com?subject=訂單問題諮詢-${orderNo}&body=${encodeURIComponent(supportMessage)}`);
            }
        },

		
        // *** 顯示載入狀態 *** //
        showLoading() {
            this.hideAllContainers();
            this.elements.loading.classList.remove('d-none');
        },

		
        // *** 顯示錯誤狀態 *** //
        showError(message) {
            this.hideAllContainers();
            document.getElementById('error-message').textContent = message;
            this.elements.error.classList.remove('d-none');
        },

		
        // *** 顯示空狀態 *** //
        showEmpty() {
            this.hideAllContainers();
            this.elements.empty.classList.remove('d-none');
        },

		
        // *** 顯示訂單列表 *** //
        showOrderList() {
            this.hideAllContainers();
            this.elements.container.classList.remove('d-none');
            this.renderOrders();
        },

		
        // *** 隱藏所有容器 *** //
        hideAllContainers() {
            [this.elements.loading, this.elements.error, this.elements.empty, this.elements.container]
                .forEach(el => el.classList.add('d-none'));
        },

		
        // *** 取得按鈕顏色類別 *** //
        getButtonColor(status) {
            const colorMap = {
                'ALL': 'primary',
                'PENDING': 'warning',
                'PAYING': 'primary',
                'PROCESSING': 'info',
                'SHIPPED': 'purple',
                'COMPLETED': 'success',
                'FAILED': 'danger',
                'CANCELLED': 'secondary'
            };
            return colorMap[status] || 'primary';
        }
    };

})();