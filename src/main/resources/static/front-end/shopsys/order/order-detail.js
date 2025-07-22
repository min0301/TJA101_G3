// order-detail.js - 訂單詳情頁面功能
// 使用 IIFE 避免污染全域範疇

(function() {
    'use strict';

    /**
     * 訂單詳情管理器
     */
    window.OrderDetailManager = {
        
        // 內部資料
        currentOrderNo: null,
        orderData: null,
        orderItems: null,

        // DOM 元素
        elements: {
            loading: null,
            error: null,
            container: null,
            orderInfoSection: null,
            orderItemsSection: null,
            commentSection: null,
            orderProgress: null,
            paymentBtn: null,
            cancelBtn: null,
            printBtn: null
        },

        /**
         * 初始化
         */
        async init(orderNo) {
            this.currentOrderNo = orderNo;
            this.initElements();
            this.bindEvents();
            await this.loadOrderDetail();
        },

        /**
         * 初始化 DOM 元素引用
         */
        initElements() {
            this.elements.loading = document.getElementById('loading-container');
            this.elements.error = document.getElementById('error-container');
            this.elements.container = document.getElementById('order-detail-container');
            this.elements.orderInfoSection = document.getElementById('order-info-section');
            this.elements.orderItemsSection = document.getElementById('order-items-section');
            this.elements.commentSection = document.getElementById('comment-section');
            this.elements.orderProgress = document.getElementById('order-progress');
            this.elements.paymentBtn = document.getElementById('payment-btn');
            this.elements.cancelBtn = document.getElementById('cancel-btn');
            this.elements.printBtn = document.getElementById('print-btn');
        },

        /**
         * 綁定事件監聽器
         */
        bindEvents() {
            // 付款按鈕
            if (this.elements.paymentBtn) {
                this.elements.paymentBtn.addEventListener('click', () => {
                    this.handlePayment();
                });
            }

            // 取消訂單按鈕
            if (this.elements.cancelBtn) {
                this.elements.cancelBtn.addEventListener('click', () => {
                    this.openCancelModal();
                });
            }

            // 列印按鈕
            if (this.elements.printBtn) {
                this.elements.printBtn.addEventListener('click', () => {
                    this.printOrder();
                });
            }

            // 取消訂單 Modal 事件
            const cancelModal = document.getElementById('cancelModal');
            if (cancelModal) {
                const confirmCancelBtn = document.getElementById('confirm-cancel-btn');
                if (confirmCancelBtn) {
                    confirmCancelBtn.addEventListener('click', () => {
                        this.handleCancelOrder();
                    });
                }
            }

            // 動態內容點擊事件 (事件委派)
            this.elements.container.addEventListener('click', (e) => {
                this.handleDynamicClick(e);
            });
        },

        /**
         * 載入訂單詳情
         */
        async loadOrderDetail() {
            try {
                this.showLoading();
                
                const result = await OrderApiClient.getOrderDetail(this.currentOrderNo);
                this.orderData = result.orderInfo;
                this.orderItems = result.orderItems;
                
                this.renderOrderDetail();
                this.showContainer();

            } catch (error) {
                console.error('載入訂單詳情失敗:', error);
                this.showError(error.message);
            }
        },

        // *** 渲染訂單詳情 *** //
		renderOrderDetail() {
		    console.log('🔍 開始渲染訂單詳情');
		    this.renderOrderInfo();
		    this.renderOrderItems();
		    this.renderOrderProgress();
		    this.updateActionButtons();
		    
		    // ✅ 確保評價功能檢查被執行
		    console.log('🔍 即將檢查評價功能');
		    this.checkCommentSection();
		    
		    // ✅ 額外的調試
		    setTimeout(() => {
		        const commentSection = document.getElementById('comment-section');
		        if (commentSection) {
		            console.log('🔍 評價區域狀態:', {
		                exists: !!commentSection,
		                display: commentSection.style.display,
		                classes: commentSection.className,
		                hidden: commentSection.classList.contains('d-none')
		            });
		        }
		    }, 100);
		},
		
		
		
        /**
         * 渲染訂單基本資訊
         */
        renderOrderInfo() {
            const order = this.orderData;
            const orderDate = OrderApiClient.formatDate(order.orderDatetime);
            const totalPrice = OrderApiClient.formatPrice(order.orderTotal);
            const statusText = OrderApiClient.getOrderStatusText(order.orderStatus);
            const statusClass = OrderApiClient.getOrderStatusClass(order.orderStatus);
            const statusIcon = OrderApiClient.getOrderStatusIcon(order.orderStatus);

            this.elements.orderInfoSection.innerHTML = `
                <div class="card" data-aos="fade-up">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">
                            <i class="bi bi-receipt me-2"></i>訂單資訊
                        </h5>
                        <span class="${statusClass}">
                            <i class="bi ${statusIcon} me-1"></i>${statusText}
                        </span>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <table class="table table-borderless">
                                    <tr>
                                        <td class="text-muted" width="120">訂單編號：</td>
                                        <td><strong>#${order.orderNo}</strong></td>
                                    </tr>
                                    <tr>
                                        <td class="text-muted">下單時間：</td>
                                        <td>${orderDate}</td>
                                    </tr>
                                    <tr>
                                        <td class="text-muted">訂單狀態：</td>
                                        <td>
                                            <span class="${statusClass}">
                                                <i class="bi ${statusIcon} me-1"></i>${statusText}
                                            </span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="text-muted">訂單總額：</td>
                                        <td><strong class="fs-5 text-primary">${totalPrice}</strong></td>
                                    </tr>
                                </table>
                            </div>
                            <div class="col-md-6">
                                <table class="table table-borderless">
                                    <tr>
                                        <td class="text-muted" width="120">聯絡信箱：</td>
                                        <td>${order.contactEmail || '未提供'}</td>
                                    </tr>
                                    <tr>
                                        <td class="text-muted">聯絡電話：</td>
                                        <td>${order.contactPhone || '未提供'}</td>
                                    </tr>
                                    <tr>
                                        <td class="text-muted">商品類型：</td>
                                        <td><span class="badge bg-info">虛擬遊戲產品</span></td>
                                    </tr>
                                    <tr>
                                        <td class="text-muted">交付方式：</td>
                                        <td><i class="bi bi-envelope me-1"></i>序號郵件發送</td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                        ${this.getOrderStatusDescription(order.orderStatus)}
                    </div>
                </div>
            `;
        },

        // *** 渲染訂單商品明細 *** //
		renderOrderItems() {
		    const itemsHtml = this.orderItems.map(item => {
		        const itemPrice = OrderApiClient.formatPrice(item.proPrice);
		        const totalPrice = OrderApiClient.formatPrice(item.proPrice * item.orderAmount);
		        
		        return `
		            <div class="card mb-3" data-aos="fade-up" data-item-no="${item.orderItemNo}">
		                <div class="card-body">
		                    <div class="row align-items-center">
		                        <div class="col-md-2">
		                            <img src="/api/product/cover/${item.proNo}" 
		                                 class="img-fluid rounded" 
		                                 alt="${item.productName}"
		                                 style="max-height: 100px; object-fit: cover;"
		                                 onerror="console.log('圖片載入失敗'); this.style.display='none'; this.parentElement.innerHTML='<div class=\\'text-center p-3 bg-light rounded\\'>圖片載入失敗</div>'"> 
		                        </div>
		                        <div class="col-md-5">
		                            <h6 class="mb-1">${item.productName}</h6>
		                            <p class="text-muted mb-1">商品編號：${item.proNo}</p>
		                            <p class="text-muted mb-0">單價：${itemPrice}</p>
		                        </div>
		                        <div class="col-md-2 text-center">
		                            <span class="badge bg-secondary fs-6">×${item.orderAmount}</span>
		                        </div>
		                        <div class="col-md-2 text-end">
		                            <strong class="fs-6">${totalPrice}</strong>
		                            ${this.getItemStatusBadge(item)}
		                        </div>
		                        <div class="col-md-1 text-end">
		                            ${this.renderItemCommentButton(item)}
		                        </div>
		                    </div>
		                </div>
		            </div>
		        `;
		    }).join('');

		    document.getElementById('order-items-container').innerHTML = itemsHtml;
		},
		
		
		// *** 新增：在商品列表中渲染評價按鈕的方法 *** //
		renderItemCommentButton(item) {
		    const order = this.orderData;
			
			// 修正：使用統一的狀態標準化
			const normalizedStatus = this.normalizeOrderStatus(order.orderStatus);
		   
			const canCommentStatuses = ['COMPLETED', 'SHIPPED'];
		    // 只有已完成的訂單才顯示評價按鈕
			if (!canCommentStatuses.includes(normalizedStatus)) {
			        return `
			            <small class="text-muted">
			                <i class="bi bi-clock me-1"></i>
			                完成後可評價
			            </small>
		        `;
		    }
		    
		    // 檢查是否已評價
			const hasComment = item.proStar && item.proStar > 0;
			    
			    if (hasComment) {
			        return `
			            <button class="btn btn-outline-success btn-sm comment-btn w-100" 
			                    data-item-no="${item.orderItemNo}">
			                <i class="bi bi-check-circle me-1"></i>已評論
			            </button>
			        `;
			    } else {
			        return `
			            <button class="btn btn-warning btn-sm comment-btn w-100" 
			                    data-item-no="${item.orderItemNo}">
			                <i class="bi bi-star me-1"></i>評價商品
			            </button>
			        `;
		    }
		},
		
		

        // *** 渲染訂單進度 *** //
		renderOrderProgress() {
		    const order = this.orderData;
		    const steps = this.createProgressSteps(order);
		    const currentStatus = this.normalizeOrderStatus(order.orderStatus);
		    
		    // ✅ 添加進度百分比計算
		    let progressPercentage = 0;
		    const normalStatuses = ['PENDING', 'PAYING', 'PROCESSING', 'SHIPPED', 'COMPLETED'];
		    const currentIndex = normalStatuses.indexOf(currentStatus);
		    
		    if (currentIndex !== -1) {
		        progressPercentage = Math.round(((currentIndex + 1) / normalStatuses.length) * 100);
		    } else if (currentStatus === 'FAILED') {
		        progressPercentage = 40; // 失敗在付款階段
		    } else if (currentStatus === 'CANCELLED') {
		        progressPercentage = 20; // 取消在第一階段
		    }
		    
		    const progressHtml = `
		        <!-- 進度條標題 -->
		        <div class="d-flex justify-content-between align-items-center mb-3">
		            <h6 class="mb-0">訂單進度</h6>
		            <small class="text-muted">${progressPercentage}%</small>
		        </div>
		        
		        <!-- 進度條 -->
		        <div class="progress mb-4" style="height: 8px;">
		            <div class="progress-bar ${this.getProgressBarClass(currentStatus)}" 
		                 role="progressbar" 
		                 style="width: ${progressPercentage}%"
		                 aria-valuenow="${progressPercentage}" 
		                 aria-valuemin="0" 
		                 aria-valuemax="100">
		            </div>
		        </div>
		        
		        <!-- 步驟列表 -->
		        ${steps.map((step, index) => {
		            let stepClass = '';
		            let iconClass = '';
		            let bgClass = '';
		            
		            // ✅ 完整的狀態樣式映射
		            switch (step.status) {
		                case 'completed':
		                    stepClass = 'text-success';
		                    iconClass = 'bi-check-circle-fill';
		                    bgClass = 'bg-light-success';
		                    break;
		                case 'processing':
		                    stepClass = 'text-primary';
		                    iconClass = 'bi-hourglass-split';
		                    bgClass = 'bg-light-primary';
		                    break;
		                case 'failed':
		                    stepClass = 'text-danger';
		                    iconClass = 'bi-x-circle-fill';
		                    bgClass = 'bg-light-danger';
		                    break;
		                case 'cancelled':
		                    stepClass = 'text-secondary';
		                    iconClass = 'bi-dash-circle-fill';
		                    bgClass = 'bg-light-secondary';
		                    break;
		                default:
		                    stepClass = 'text-muted';
		                    iconClass = 'bi-circle';
		                    bgClass = '';
		            }

		            return `
		                <div class="d-flex align-items-start mb-3 p-2 rounded ${bgClass}" 
		                     style="transition: all 0.3s ease;">
		                    <div class="flex-shrink-0 me-3">
		                        <i class="bi ${iconClass} ${stepClass}" style="font-size: 1.4rem;"></i>
		                    </div>
		                    <div class="flex-grow-1">
		                        <div class="fw-medium ${stepClass} mb-1">${step.name}</div>
		                        ${step.time ? `<small class="text-muted d-block mb-1">
		                            <i class="bi bi-clock me-1"></i>${OrderApiClient.formatDate(step.time)}
		                        </small>` : ''}
		                        <small class="text-muted d-block">${step.description}</small>
		                        ${step.status === 'processing' ? `
		                            <div class="mt-2">
		                                <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
		                                <small class="text-primary fw-medium">處理中...</small>
		                            </div>
		                        ` : ''}
		                    </div>
		                </div>
		            `;
		        }).join('')}
		        
		        <!-- 狀態摘要 -->
		        <div class="mt-3 p-3 rounded" style="background-color: #f8f9fa;">
		            <div class="d-flex align-items-center">
		                <i class="bi ${this.getStatusIcon(currentStatus)} ${this.getStatusClass(currentStatus)} me-2"></i>
		                <div>
		                    <strong>當前狀態：${this.getStatusText(currentStatus)}</strong>
		                    <div class="small text-muted mt-1">${this.getStatusDescription(currentStatus)}</div>
		                </div>
		            </div>
		        </div>
		    `;

		    this.elements.orderProgress.innerHTML = progressHtml;
		},
		
		
		// *** 新增：取得進度條樣式 *** //
		getProgressBarClass(status) {
		    switch (status) {
		        case 'COMPLETED': return 'bg-success';
		        case 'FAILED': return 'bg-danger';
		        case 'CANCELLED': return 'bg-secondary';
		        default: return 'bg-primary';
		    }
		},
		
		
		// *** 新增：取得狀態圖標 *** //
		getStatusIcon(status) {
		    const iconMap = {
		        'PENDING': 'bi-clock',
		        'PAYING': 'bi-credit-card',
		        'PROCESSING': 'bi-gear',
		        'SHIPPED': 'bi-truck',
		        'COMPLETED': 'bi-check-circle',
		        'FAILED': 'bi-exclamation-triangle',
		        'CANCELLED': 'bi-x-circle'
		    };
		    return iconMap[status] || 'bi-question-circle';
		},
		
		// *** 新增：取得狀態樣式 *** //
		getStatusClass(status) {
		    const classMap = {
		        'PENDING': 'text-warning',
		        'PAYING': 'text-info',
		        'PROCESSING': 'text-primary',
		        'SHIPPED': 'text-purple',
		        'COMPLETED': 'text-success',
		        'FAILED': 'text-danger',
		        'CANCELLED': 'text-secondary'
		    };
		    return classMap[status] || 'text-muted';
		},
		
		
		// *** 新增：取得狀態文字 *** //
		getStatusText(status) {
		    const textMap = {
		        'PENDING': '等待付款',
		        'PAYING': '付款處理中',
		        'PROCESSING': '訂單處理中',
		        'SHIPPED': '已發貨',
		        'COMPLETED': '已完成',
		        'FAILED': '處理失敗',
		        'CANCELLED': '已取消'
		    };
		    return textMap[status] || '未知狀態';
		},
		
		// *** 新增：取得狀態描述 *** //
		getStatusDescription(status) {
		    const descMap = {
		        'PENDING': '請盡快完成付款，避免訂單被自動取消',
		        'PAYING': '正在處理您的付款，請稍候片刻',
		        'PROCESSING': '付款成功，正在為您準備商品',
		        'SHIPPED': '商品已發送，請查收您的信箱',
		        'COMPLETED': '交易完成，感謝您的購買',
		        'FAILED': '處理過程中發生問題，可重新嘗試',
		        'CANCELLED': '訂單已取消，如有疑問請聯繫客服'
		    };
		    return descMap[status] || '';
		},
		
		

        // *** 更新操作按鈕 *** //
		updateActionButtons() {
		    const order = this.orderData;
		    
				// 修正：使用統一的狀態標準化
			    const normalizedStatus = this.normalizeOrderStatus(order.orderStatus);
			    
			    // 付款按鈕 - 等待付款或處理失敗狀態
			    const showPaymentStatuses = ['PENDING', 'FAILED'];
			    if (showPaymentStatuses.includes(normalizedStatus)) {
			        this.elements.paymentBtn.classList.remove('d-none');
			        this.elements.paymentBtn.innerHTML = normalizedStatus === 'FAILED'
			            ? '<i class="bi bi-arrow-clockwise me-2"></i>重新付款'
			            : '<i class="bi bi-credit-card me-2"></i>立即付款';
			    } else {
			        this.elements.paymentBtn.classList.add('d-none');
			    }

			    // 取消按鈕 - 等待付款或付款處理中狀態
			    const cancelableStatuses = ['PENDING', 'PAYING'];
			    if (cancelableStatuses.includes(normalizedStatus)) {
			        this.elements.cancelBtn.classList.remove('d-none');
			    } else {
			        this.elements.cancelBtn.classList.add('d-none');
			    }
			},
		
		
		

        // *** 檢查是否顯示評價區域 *** //
		checkCommentSection() {
		    const order = this.orderData;
		    
		    console.log('🔍 檢查評價功能 - 原始訂單資料:', order);
		    
		    // ✅ 使用統一的狀態標準化
		    const normalizedStatus = this.normalizeOrderStatus(order.orderStatus);
		    const completedStatuses = ['COMPLETED', 'SHIPPED']; 
		    
		    console.log('🔍 標準化後狀態:', normalizedStatus);
		    console.log('🔍 可評價狀態:', completedStatuses);
		    console.log('🔍 是否可評價:', completedStatuses.includes(normalizedStatus));
		    
		    if (completedStatuses.includes(normalizedStatus)) {
		        this.elements.commentSection.classList.remove('d-none');
		        this.loadCommentSection();
		        console.log('✅ 評價功能已啟用');
		        
		        // ✅ 強制顯示評價區域
		        const commentSection = document.getElementById('comment-section');
		        if (commentSection) {
		            commentSection.style.display = 'block';
		            console.log('✅ 強制顯示評價區域');
		        }
		    } else {
		        this.elements.commentSection.classList.add('d-none');
		        console.log('❌ 評價功能未啟用 - 狀態不符合');
		    }
		},
		
		
		

        /**
         * 載入評價區域
         */
        async loadCommentSection() {
            try {
                // 檢查是否已有評價
				const comments = await OrderApiClient.getMemberCommentsByOrder(this.currentOrderNo);
				       const commentsHtml = this.orderItems.map(item => {
				           const existingComment = comments.find(c => c.orderItemNo === item.orderItemNo);
				           return `
				               <div class="card mb-3" data-item-no="${item.orderItemNo}">
				                   <div class="card-body">
				                       <div class="row align-items-center">
				                           <div class="col-md-8">
				                               <h6 class="mb-2">${item.productName}</h6>
				                               <p class="text-muted mb-0">購買數量：${item.orderAmount}</p>
				                           </div>
				                           <div class="col-md-4">
				                               ${existingComment ? 
				                                   this.renderExistingCommentDisplay(existingComment) : 
				                                   this.renderCommentButton(item.orderItemNo)
				                               }
				                           </div>
				                       </div>
				                   </div>
				               </div>
				           `;
				       }).join('');

				       document.getElementById('comments-container').innerHTML = commentsHtml;
				   } catch (error) {
				       console.error('載入評價區域失敗:', error);
				       document.getElementById('comments-container').innerHTML = `
				           <div class="alert alert-warning">
				               <i class="bi bi-exclamation-triangle me-2"></i>
				               載入評價資訊失敗，請稍後再試。
				           </div>
                `;
            }
        },
		
		
		
		// *** 專門顯示已有評價的方法（包含星星和評價內容） *** //
		renderExistingCommentDisplay(comment) {
		    const stars = OrderApiClient.getStarDisplay(comment.proStar);
		    const commentDate = comment.commentDate ? OrderApiClient.formatDate(comment.commentDate) : '';
		    
		    return `
		        <div class="text-end">
		            <div class="mb-2">
		                <div class="d-flex justify-content-end align-items-center mb-1">
		                    <span class="me-2">${stars}</span>
		                    <small class="text-muted">${comment.proStar}/5</small>
		                </div>
		                ${commentDate ? `<small class="text-muted d-block">評價時間：${commentDate}</small>` : ''}
		            </div>
		            
		            ${comment.productComment ? `
		                <div class="alert alert-light p-2 mb-2 text-start">
		                    <small class="text-dark">"${comment.productComment}"</small>
		                </div>
		            ` : ''}
		            
		            <button class="btn btn-outline-secondary btn-sm comment-btn" 
		                    data-item-no="${comment.orderItemNo}">
		                <i class="bi bi-pencil me-1"></i>修改評價
		            </button>
		        </div>
		    `;
		},
		
		
		
		

        // *** 處理動態點擊事件 *** //
        handleDynamicClick(event) {
            const target = event.target.closest('button');
            if (!target) return;

            if (target.classList.contains('comment-btn')) {
                const itemNo = target.dataset.itemNo;
                this.openCommentModal(itemNo);
            } else if (target.classList.contains('payment-status-btn')) {
                this.checkPaymentStatus();
            } else if (target.classList.contains('reorder-btn')) {
                this.handleReorder();
            }
        },

        //*** 處理付款 *** //
        handlePayment() {
            if (confirm('確定要前往付款頁面嗎？')) {
                window.location.href = `/api/orders/${this.currentOrderNo}/payment`;
            }
        },

        //*** 查看付款狀態 *** //
        async checkPaymentStatus() {
            try {
                const response = await OrderApiClient.request(`/api/orders/${this.currentOrderNo}/payment/status`);
                
                if (response.success) {
                    const status = response.paymentStatus.status;
                    const orderStatus = response.orderStatus;
                    
                    if (status === 'SUCCESS') {
                        alert(`付款成功！\n訂單狀態：${OrderApiClient.getOrderStatusText(orderStatus)}\n\n頁面將重新載入以更新狀態。`);
                        window.location.reload();
                    } else if (status === 'FAILED') {
                        alert('付款失敗，您可以重新嘗試付款或聯繫客服。');
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

        /**
         * 開啟取消訂單 Modal
         */
        openCancelModal() {
            const modal = new bootstrap.Modal(document.getElementById('cancelModal'));
            modal.show();
        },

        /**
         * 處理取消訂單
         */
        async handleCancelOrder() {
            const reason = document.getElementById('cancel-reason').value;
            const note = document.getElementById('cancel-note').value;
            
            if (!reason) {
                alert('請選擇取消原因！');
                return;
            }

            const confirmCancelBtn = document.getElementById('confirm-cancel-btn');
            
            try {
                confirmCancelBtn.disabled = true;
                confirmCancelBtn.innerHTML = '<i class="spinner-border spinner-border-sm me-1"></i>處理中...';

                const cancelReason = note ? `${reason}：${note}` : reason;
                const memNo = OrderApiClient.getMemberNo();
                
                const response = await OrderApiClient.request(`/api/orders/${this.currentOrderNo}/cancel`, {
                    method: 'POST',
                    body: JSON.stringify({
                        memNo: memNo,
                        reason: cancelReason
                    })
                });

                if (response.success) {
                    alert('訂單取消成功！');
                    bootstrap.Modal.getInstance(document.getElementById('cancelModal')).hide();
                    window.location.reload();
                } else {
                    alert('取消訂單失敗：' + response.message);
                }

            } catch (error) {
                console.error('取消訂單失敗:', error);
                alert('取消訂單失敗：' + error.message);
            } finally {
                confirmCancelBtn.disabled = false;
                confirmCancelBtn.innerHTML = '確認取消訂單';
            }
        },

        /**
         * 列印訂單
         */
        printOrder() {
            const printContent = this.generatePrintContent();
            
            const printWindow = window.open('', '_blank');
            printWindow.document.write(printContent);
            printWindow.document.close();
            printWindow.print();
        },

        /**
         * 開啟評價 Modal
         */
        openCommentModal(itemNo) {
            const item = this.orderItems.find(i => i.orderItemNo == itemNo);
            if (!item) {
                alert('找不到商品資訊！');
                return;
            }

            // 使用 CommentModal 組件
            if (window.CommentModal) {
                window.CommentModal.open(item, (success) => {
                    if (success) {
                        this.loadCommentSection(); // 重新載入評價區域
                    }
                });
            } else {
                alert('評價功能載入中，請稍後再試！');
            }
        },

        /**
         * 再次購買
         */
		async handleReorder() {
		    if (!confirm('確定要將此訂單中的商品加入購物車嗎？')) return;
		    
		    try {
		        // 檢查是否登入
		        if (!CartApiClient.isLoggedIn()) {
		            alert('請先登入才能使用購物車功能');
		            return;
		        }
		        
		        let successCount = 0;
		        let errorCount = 0;
		        const errors = [];
		        
		        // 逐一加入每個商品到購物車
		        for (const item of this.orderItems) {
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

        // ========== 輔助方法 ========== //

        /**
         * 取得商品狀態徽章
         */
        getItemStatusBadge(item) {
            // 這裡可以根據商品的序號發放狀態來顯示不同的徽章
            if (item.serialNumber) {
                return '<div class="mt-1"><span class="badge bg-success">序號已發放</span></div>';
            } else if (this.orderData.orderStatus === 'Processing') {
                return '<div class="mt-1"><span class="badge bg-info">準備中</span></div>';
            } else if (this.orderData.orderStatus === 'PENDING' || this.orderData.orderStatus === 'Paying') {
                return '<div class="mt-1"><span class="badge bg-warning">等待付款</span></div>';
            }
            return '';
        },

        /**
         * 取得訂單狀態描述
         */
        getOrderStatusDescription(status) {
			// 修正：使用統一的狀態標準化
			const normalizedStatus = this.normalizeOrderStatus(status);
			
			const descriptions = {
			        'PENDING': `
			            <div class="alert alert-warning mt-3">
			                <i class="bi bi-clock me-2"></i>
			                <strong>等待付款</strong><br>
			                請盡快完成付款，超過30分鐘未付款將自動取消訂單。
			            </div>
			        `,
			        'PAYING': `
			            <div class="alert alert-info mt-3">
			                <i class="bi bi-hourglass-split me-2"></i>
			                <strong>付款處理中</strong><br>
			                正在等待付款結果，請勿重複操作。如長時間未更新請重新查看狀態。
			            </div>
			        `,
			        'PROCESSING': `
			            <div class="alert alert-info mt-3">
			                <i class="bi bi-gear me-2"></i>
			                <strong>訂單處理中</strong><br>
			                付款成功！我們正在為您準備遊戲序號，序號將發送至您的信箱。
			            </div>
			        `,
			        'SHIPPED': `
			            <div class="alert alert-success mt-3">
			                <i class="bi bi-envelope-check me-2"></i>
			                <strong>已發貨</strong><br>
			                遊戲序號已發送至您的信箱，請查收。如未收到請檢查垃圾信件匣。
			            </div>
			        `,
			        'COMPLETED': `
			            <div class="alert alert-success mt-3">
			                <i class="bi bi-check-circle me-2"></i>
			                <strong>已完成</strong><br>
			                感謝您的購買！如對商品滿意，歡迎為商品評價。
			            </div>
			        `,
			        'FAILED': `
			            <div class="alert alert-danger mt-3">
			                <i class="bi bi-exclamation-triangle me-2"></i>
			                <strong>處理失敗</strong><br>
			                訂單處理失敗，您可以重新嘗試付款或聯繫客服協助處理。
			            </div>
			        `,
			        'CANCELLED': `
			            <div class="alert alert-secondary mt-3">
			                <i class="bi bi-x-circle me-2"></i>
			                <strong>已取消</strong><br>
			                此訂單已被取消，如有疑問請聯繫客服。
			            </div>
			        `
			    };
			    return descriptions[normalizedStatus] || '';
        },

		
		
        // *** 建立進度步驟 *** //
        createProgressSteps(order) {
			
			// 統一狀態格式處理（支援中英文）
			let currentStatus = this.normalizeOrderStatus(order.orderStatus);
			    
			const allSteps = [
			        {
			            name: '等待付款',
			            statusKey: 'PENDING',
			            description: '請盡快完成付款，超過30分鐘未付款將自動取消訂單',
			            icon: 'bi-clock'
			        },
			        {
			            name: '付款處理中',
			            statusKey: 'PAYING',
			            description: '正在綠界付款頁面處理，請勿重複操作',
			            icon: 'bi-credit-card'
			        },
			        {
			            name: '訂單處理中',
			            statusKey: 'PROCESSING',
			            description: '付款成功！正在為您分配序號，請稍候',
			            icon: 'bi-gear'
			        },
			        {
			            name: '已發貨',
			            statusKey: 'SHIPPED',
			            description: '遊戲序號已發送至您的信箱，請查收',
			            icon: 'bi-truck'
			        },
			        {
			            name: '已完成',
			            statusKey: 'COMPLETED',
			            description: '感謝您的購買！歡迎為商品評價',
			            icon: 'bi-check-circle'
			        },
			        {
			            name: '處理失敗',
			            statusKey: 'FAILED',
			            description: '付款超時或處理失敗，可重新嘗試付款',
			            icon: 'bi-exclamation-triangle'
			        },
			        {
			            name: '已取消',
			            statusKey: 'CANCELLED',
			            description: '訂單已取消，如有疑問請聯繫客服',
			            icon: 'bi-x-circle'
			        }
			    ];

			    // ✅ 根據當前狀態設定每個步驟的顯示狀態
			    return allSteps.map((step, index) => {
			        let stepStatus = 'waiting';
			        let time = null;
			        
			        // 當前狀態
			        if (currentStatus === step.statusKey) {
			            stepStatus = 'processing';
			            time = order.orderDatetime;
			        } 
			        // 已完成的狀態
			        else if (this.isStatusReached(currentStatus, step.statusKey)) {
			            stepStatus = 'completed';
			            if (step.statusKey === 'PENDING') {
			                time = order.orderDatetime; // 下單時間
			            }
			        }
			        
			        // ✅ 處理失敗和取消狀態的特殊邏輯
			        if (currentStatus === 'FAILED') {
			            if (['PENDING', 'PAYING'].includes(step.statusKey)) {
			                stepStatus = 'completed';
			            } else if (step.statusKey === 'FAILED') {
			                stepStatus = 'processing';
			                time = order.orderDatetime;
			            } else {
			                stepStatus = 'waiting';
			            }
			        } else if (currentStatus === 'CANCELLED') {
			            if (step.statusKey === 'PENDING') {
			                stepStatus = 'completed';
			            } else if (step.statusKey === 'CANCELLED') {
			                stepStatus = 'processing';
			                time = order.orderDatetime;
			            } else if (['PAYING', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'FAILED'].includes(step.statusKey)) {
			                stepStatus = 'waiting';
			            }
			        }
			        
			        return {
			            name: step.name,
			            status: stepStatus,
			            time: time,
			            description: step.description,
			            icon: step.icon
			        };
			    });
			},
			
			
			// 新增：狀態標準化方法（支援中英文）
			normalizeOrderStatus(status) {
			    if (!status) {
			        console.warn('訂單狀態為空，預設為 PENDING');
			        return 'PENDING';
			    }
			    
			    console.log('🔍 原始訂單狀態:', status, '類型:', typeof status);
			    
			    // ✅ 中文狀態映射到英文大寫
			    const chineseToEnglish = {
			        '等待付款': 'PENDING',
			        '付款處理中': 'PAYING', 
			        '處理中': 'PROCESSING',
			        '已發貨': 'SHIPPED',
			        '已出貨': 'SHIPPED',
			        '已完成': 'COMPLETED',
			        '處理失敗': 'FAILED',
			        '已取消': 'CANCELLED'
			    };
			    
			    // 如果是中文狀態，轉換為英文
			    if (chineseToEnglish[status]) {
			        const normalized = chineseToEnglish[status];
			        console.log('✅ 中文狀態轉換:', status, '->', normalized);
			        return normalized;
			    }
			    
			    // 如果是英文，轉為大寫
			    const normalized = status.toString().toUpperCase();
			    console.log('✅ 英文狀態標準化:', status, '->', normalized);
			    return normalized;
			},

			
		
		
			// *** 判斷狀態是否已達到 *** //
			isStatusReached(currentStatus, targetStatus) {
			    // 完整的7步驟順序
			    const statusOrder = ['PENDING', 'PAYING', 'PROCESSING', 'SHIPPED', 'COMPLETED'];
			    const currentIndex = statusOrder.indexOf(currentStatus);
			    const targetIndex = statusOrder.indexOf(targetStatus);
			    
			    // 特殊狀態處理
			    if (currentStatus === 'FAILED') {
			        // 失敗狀態：只有前面的步驟算完成
			        return ['PENDING', 'PAYING'].includes(targetStatus);
			    }
			    
			    if (currentStatus === 'CANCELLED') {
			        // 取消狀態：只有第一步算完成
			        return targetStatus === 'PENDING';
			    }
			    
			    // 正常流程判斷
			    return currentIndex >= targetIndex && currentIndex !== -1 && targetIndex !== -1;
			},

			
			
			
			
			
			// *** 判斷是否為當前狀態 *** //
			isCurrentStatus(currentStatus, targetStatus) {
			    return currentStatus === targetStatus;
			},
			
			
			


        /**
         * 渲染現有評價
         */
        renderExistingComment(comment) {
            const stars = OrderApiClient.getStarDisplay(comment.proStar);
            return `
                <div class="text-end">
                    <div class="mb-1">
                        <span class="text-warning">${stars}</span>
                        <small class="text-muted ms-2">${comment.proStar}/5</small>
                    </div>
                    <div class="text-muted small mb-2">${comment.productComment || '無文字評價'}</div>
                    <button class="btn btn-outline-secondary btn-sm comment-btn" 
                            data-item-no="${comment.orderItemNo}">
                        <i class="bi bi-pencil me-1"></i>修改評價
                    </button>
                </div>
            `;
        },

        /**
         * 渲染評價按鈕
         */
        renderCommentButton(itemNo) {
            return `
                <button class="btn btn-primary comment-btn" 
                        data-item-no="${itemNo}">
                    <i class="bi bi-star me-1"></i>評價商品
                </button>
            `;
        },

        /**
         * 生成列印內容
         */
        generatePrintContent() {
            const order = this.orderData;
            const orderDate = OrderApiClient.formatDate(order.orderDatetime);
            const totalPrice = OrderApiClient.formatPrice(order.orderTotal);
            
            return `
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>訂單 #${order.orderNo}</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .header { text-align: center; margin-bottom: 30px; }
                        .order-info { margin-bottom: 20px; }
                        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f5f5f5; }
                        .total { font-weight: bold; font-size: 1.2em; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>像素部落(Pixel Tribe)</h1>
                        <h2>訂單詳情</h2>
                    </div>
                    <div class="order-info">
                        <p><strong>訂單編號：</strong>#${order.orderNo}</p>
                        <p><strong>下單時間：</strong>${orderDate}</p>
                        <p><strong>聯絡信箱：</strong>${order.contactEmail}</p>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th>商品名稱</th>
                                <th>數量</th>
                                <th>單價</th>
                                <th>小計</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${this.orderItems.map(item => `
                                <tr>
                                    <td>${item.productName}</td>
                                    <td>${item.orderAmount}</td>
                                    <td>NT$ ${item.proPrice}</td>
                                    <td>NT$ ${item.proPrice * item.orderAmount}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                    <div class="total">
                        <p>訂單總額：${totalPrice}</p>
                    </div>
                </body>
                </html>
            `;
        },

        /**
         * 顯示載入狀態
         */
        showLoading() {
            this.hideAllContainers();
            this.elements.loading.classList.remove('d-none');
        },

        /**
         * 顯示錯誤狀態
         */
        showError(message) {
            this.hideAllContainers();
            document.getElementById('error-message').textContent = message;
            this.elements.error.classList.remove('d-none');
        },

        /**
         * 顯示主容器
         */
        showContainer() {
            this.hideAllContainers();
            this.elements.container.classList.remove('d-none');
        },

        /**
         * 隱藏所有容器
         */
        hideAllContainers() {
            [this.elements.loading, this.elements.error, this.elements.container]
                .forEach(el => el.classList.add('d-none'));
        }
    };

})();