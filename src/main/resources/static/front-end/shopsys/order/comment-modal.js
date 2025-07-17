// comment-modal.js - 評價彈窗組件
// 使用 IIFE 避免污染全域範疇

(function() {
    'use strict';

    /**
     * 評價 Modal 組件
     */
    window.CommentModal = {
        
        // 內部資料
        currentItem: null,
        currentRating: 0,
        isEditMode: false,
        callback: null,

        // Modal 實例
        modal: null,

        /**
         * 開啟評價 Modal
         * @param {Object} item - 訂單商品項目
         * @param {Function} callback - 完成後的回調函數
         */
        open(item, callback = null) {
            this.currentItem = item;
            this.callback = callback;
            this.currentRating = 0;
            this.isEditMode = false;

            // 檢查是否已有評價
            this.checkExistingComment().then(() => {
                this.renderModalContent();
                this.showModal();
            });
        },

        /**
         * 檢查是否已有評價
         */
        async checkExistingComment() {
            try {
                const comment = await OrderApiClient.request(`/api/orderitem/${this.currentItem.orderItemNo}`);
                
                if (comment && comment.proStar && comment.proStar > 0) {
                    this.isEditMode = true;
                    this.currentRating = comment.proStar;
                    this.currentItem.existingComment = comment.productComment || '';
                }
            } catch (error) {
                console.log('查詢評價失敗，將以新增模式開啟:', error);
                this.isEditMode = false;
            }
        },

        /**
         * 渲染 Modal 內容
         */
        renderModalContent() {
            const modalBody = document.querySelector('#commentModal .modal-body');
            const modalTitle = document.querySelector('#commentModal .modal-title');
            
            // 設定標題
            modalTitle.textContent = this.isEditMode ? '修改商品評價' : '評價商品';

            // 渲染內容
            modalBody.innerHTML = this.createModalContent();

            // 初始化事件
            this.initializeEvents();

            // 如果是編輯模式，設定現有評價
            if (this.isEditMode) {
                this.setExistingRating();
            }
        },

        /**
         * 建立 Modal 內容 HTML
         */
        createModalContent() {
            const item = this.currentItem;
            const itemPrice = OrderApiClient.formatPrice(item.proPrice);

            return `
                <div class="comment-form-container">
                    <!-- 商品資訊 -->
                    <div class="card mb-4">
                        <div class="card-body">
                            <div class="row align-items-center">
                                <div class="col-md-3">
                                    <img src="/api/product/cover/${item.proNo}" 
                                         class="img-fluid rounded" 
                                         alt="${item.productName}"
                                         style="max-height: 100px; object-fit: cover;"
                                         onerror="console.log('圖片載入失敗:', '/api/product/image/${item.proNo}'); this.style.display='none'; this.parentElement.innerHTML='<div class=\\'text-muted text-center\\' style=\\'display: flex; align-items: center; justify-content: center; height: 100%; background: #f8f9fa; border: 2px dashed #dee2e6; border-radius: 8px;\\'><i class=\\'bi bi-image\\' style=\\'font-size: 1.5rem;\\'></i></div>'"
                                </div>
                                <div class="col-md-9">
                                    <h6 class="mb-1">${item.productName}</h6>
                                    <p class="text-muted mb-1">商品編號：${item.proNo}</p>
                                    <p class="text-muted mb-0">購買價格：${itemPrice} × ${item.orderAmount}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 評價表單 -->
                    <form id="comment-form">
                        <!-- 星級評分 -->
                        <div class="mb-4">
                            <label class="form-label fw-bold">
                                <i class="bi bi-star me-1"></i>
                                給商品評分 <span class="text-danger">*</span>
                            </label>
                            <div class="star-rating d-flex align-items-center">
                                ${this.createStarRating()}
                                <span class="ms-3 rating-text text-muted">請選擇評分</span>
                            </div>
                            <small class="text-muted">點擊星星來評分，1星=很差，5星=非常好</small>
                        </div>

                        <!-- 評價內容 -->
                        <div class="mb-4">
                            <label for="comment-text" class="form-label fw-bold">
                                <i class="bi bi-chat-text me-1"></i>
                                評價內容
                            </label>
                            <textarea 
                                id="comment-text" 
                                class="form-control" 
                                rows="4" 
                                placeholder="分享您對這個商品的使用心得..."
                                maxlength="255">${this.isEditMode ? (this.currentItem.existingComment || '') : ''}</textarea>
                            <div class="d-flex justify-content-between mt-1">
                                <small class="text-muted">可輸入最多255個字</small>
                                <small class="text-muted character-count">0/255</small>
                            </div>
                        </div>

                        <!-- 評價提示 -->
                        <div class="alert alert-info">
                            <i class="bi bi-info-circle me-2"></i>
                            <strong>評價提示：</strong>
                            <ul class="mb-0 mt-2">
                                <li>請根據實際使用體驗進行評價</li>
                                <li>評價內容將公開顯示在商品頁面</li>
                                <li>${this.isEditMode ? '修改後的評價將會覆蓋原有評價' : '提交後可以再次修改評價'}</li>
                            </ul>
                        </div>

                        <!-- 按鈕區域 -->
                        <div class="d-flex justify-content-end gap-2">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                                <i class="bi bi-x-lg me-1"></i>取消
                            </button>
                            <button type="submit" class="btn btn-primary" id="submit-comment-btn">
                                <i class="bi bi-send me-1"></i>
                                ${this.isEditMode ? '更新評價' : '提交評價'}
                            </button>
                        </div>
                    </form>
                </div>
            `;
        },

        /**
         * 建立星級評分 HTML
         */
        createStarRating() {
            let starsHtml = '';
            for (let i = 1; i <= 5; i++) {
                starsHtml += `
                    <i class="bi bi-star star-btn me-1" 
                       data-rating="${i}"
                       style="font-size: 2rem; cursor: pointer; color: #ddd; transition: color 0.2s;">
                    </i>
                `;
            }
            return starsHtml;
        },

        /**
         * 初始化事件監聽器
         */
        initializeEvents() {
            // 星級評分點擊事件
            this.initStarRatingEvents();

            // 文字計數器
            this.initCharacterCounter();

            // 表單提交事件
            this.initFormSubmission();
        },

        /**
         * 初始化星級評分事件
         */
        initStarRatingEvents() {
            const stars = document.querySelectorAll('.star-btn');
            const ratingText = document.querySelector('.rating-text');

            // 點擊評分
            stars.forEach(star => {
                star.addEventListener('click', () => {
                    const rating = parseInt(star.dataset.rating);
                    this.setRating(rating);
                });

                // 滑鼠懸停效果
                star.addEventListener('mouseenter', () => {
                    const rating = parseInt(star.dataset.rating);
                    this.highlightStars(rating);
                });
            });

            // 滑鼠離開時恢復當前評分
            document.querySelector('.star-rating').addEventListener('mouseleave', () => {
                this.highlightStars(this.currentRating);
            });
        },

        /**
         * 設定評分
         */
        setRating(rating) {
            this.currentRating = rating;
            this.highlightStars(rating);
            this.updateRatingText(rating);
        },

        /**
         * 高亮星星
         */
        highlightStars(rating) {
            const stars = document.querySelectorAll('.star-btn');
            stars.forEach((star, index) => {
                const starRating = index + 1;
                if (starRating <= rating) {
                    star.style.color = '#ffc107';
                    star.classList.remove('bi-star');
                    star.classList.add('bi-star-fill');
                } else {
                    star.style.color = '#ddd';
                    star.classList.remove('bi-star-fill');
                    star.classList.add('bi-star');
                }
            });
        },

        /**
         * 更新評分文字
         */
        updateRatingText(rating) {
            const ratingText = document.querySelector('.rating-text');
            
            if (rating > 0) {
                // 使用星星顯示評分
                const stars = OrderApiClient.getStarDisplay(rating);
                ratingText.innerHTML = `${stars} <span class="text-muted">(${rating}/5)</span>`;
                ratingText.className = 'ms-3 rating-text text-warning fw-bold';
            } else {
                ratingText.textContent = '請選擇評分';
                ratingText.className = 'ms-3 rating-text text-muted';
            }
        },

        /**
         * 設定現有評分 (編輯模式)
         */
        setExistingRating() {
            if (this.currentRating > 0) {
                this.setRating(this.currentRating);
            }
        },

        /**
         * 初始化字符計數器
         */
        initCharacterCounter() {
            const textarea = document.getElementById('comment-text');
            const counter = document.querySelector('.character-count');

            const updateCounter = () => {
                const length = textarea.value.length;
                counter.textContent = `${length}/255`;
                
                if (length > 200) {
                    counter.className = 'text-warning character-count';
                } else if (length > 240) {
                    counter.className = 'text-danger character-count';
                } else {
                    counter.className = 'text-muted character-count';
                }
            };

            textarea.addEventListener('input', updateCounter);
            updateCounter(); // 初始化
        },

        /**
         * 初始化表單提交
         */
        initFormSubmission() {
            const form = document.getElementById('comment-form');
            
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleSubmit();
            });
        },

        /**
         * 處理表單提交
         */
        async handleSubmit() {
            // 驗證評分
            if (this.currentRating === 0) {
                alert('請先選擇評分！');
                return;
            }

            const commentText = document.getElementById('comment-text').value.trim();
            const submitBtn = document.getElementById('submit-comment-btn');

            try {
                // 更新按鈕狀態
                submitBtn.disabled = true;
                submitBtn.innerHTML = `
                    <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                    ${this.isEditMode ? '更新中...' : '提交中...'}
                `;

                // 準備資料
                const commentData = {
                    proStar: this.currentRating,
                    productComment: commentText
                };

                // 呼叫 API
                const response = this.isEditMode 
                    ? await OrderApiClient.updateComment(this.currentItem.orderItemNo, commentData)
                    : await OrderApiClient.addComment(this.currentItem.orderItemNo, commentData);

                // 成功處理
                this.showSuccessMessage();
                
                // 延遲關閉 Modal
                setTimeout(() => {
                    this.hideModal();
                    if (this.callback) {
                        this.callback(true);
                    }
                }, 1500);

            } catch (error) {
                console.error('提交評價失敗:', error);
                this.showErrorMessage(error.message);
                
                // 恢復按鈕狀態
                submitBtn.disabled = false;
                submitBtn.innerHTML = `
                    <i class="bi bi-send me-1"></i>
                    ${this.isEditMode ? '更新評價' : '提交評價'}
                `;
            }
        },

        /**
         * 顯示成功訊息
         */
        showSuccessMessage() {
            const modalBody = document.querySelector('#commentModal .modal-body');
            
            modalBody.innerHTML = `
                <div class="text-center p-5">
                    <div class="mb-4">
                        <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
                    </div>
                    <h4 class="text-success mb-3">
                        ${this.isEditMode ? '評價更新成功！' : '評價提交成功！'}
                    </h4>
                    <p class="text-muted mb-3">感謝您的評價，這將幫助其他用戶做出更好的選擇。</p>
                    <div class="alert alert-success">
                        <div class="d-flex align-items-center">
                            <div class="me-3">
                                <img src="/api/product/image/${this.currentItem.proNo}" 
                                     class="rounded" 
                                     style="width: 50px; height: 50px; object-fit: cover;">
                            </div>
                            <div class="text-start">
                                <div class="fw-bold">${this.currentItem.productName}</div>
                                <div class="text-warning">
                                    ${OrderApiClient.getStarDisplay(this.currentRating)}
                                    <span class="text-muted ms-2">${this.currentRating}/5</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <p class="text-muted small">視窗將自動關閉...</p>
                </div>
            `;
        },

        /**
         * 顯示錯誤訊息
         */
        showErrorMessage(message) {
            const alertContainer = document.querySelector('.comment-form-container');
            
            // 移除舊的錯誤訊息
            const oldAlert = alertContainer.querySelector('.alert-danger');
            if (oldAlert) {
                oldAlert.remove();
            }

            // 新增錯誤訊息
            const errorAlert = document.createElement('div');
            errorAlert.className = 'alert alert-danger alert-dismissible';
            errorAlert.innerHTML = `
                <i class="bi bi-exclamation-triangle me-2"></i>
                <strong>提交失敗：</strong>${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            `;

            alertContainer.insertBefore(errorAlert, alertContainer.firstChild);
        },

        /**
         * 顯示 Modal
         */
        showModal() {
            const modalElement = document.getElementById('commentModal');
            this.modal = new bootstrap.Modal(modalElement);
            this.modal.show();

            // 綁定關閉事件
            modalElement.addEventListener('hidden.bs.modal', () => {
                this.cleanup();
            });
        },

        /**
         * 隱藏 Modal
         */
        hideModal() {
            if (this.modal) {
                this.modal.hide();
            }
        },

        /**
         * 清理資源
         */
        cleanup() {
            this.currentItem = null;
            this.currentRating = 0;
            this.isEditMode = false;
            this.callback = null;
            this.modal = null;
        }
    };

})();