/**
 * productDetail.js - 商品詳情頁面管理器
 * 專門處理 product.html 頁面，支援 URL 參數 ?id=1
 * 只在有商品ID時才運作，否則跳轉到商品列表頁
 */

class ProductDetailManager {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api';
        this.currentProductId = null;
        this.mainContainer = null;
        this.mallTagManager = null;
        this.isInitialized = false;
        
        // 立即檢查是否應該在此頁面運作
        this.checkAndInit();
    }

    /**
     * 根據 mallTagNo 獲取平台名稱
     */
    getPlatformName(mallTagNo) {
        // 這裡可以根據 mallTagNo 返回對應的平台名稱
        // 您可能需要根據實際的資料結構調整
        if (mallTagNo && mallTagNo.malltagName) {
            return mallTagNo.malltagName;
        } else if (typeof mallTagNo === 'number') {
            // 如果 mallTagNo 是數字，可以定義一個映射
            const platformMap = {
                1: 'Steam',
                2: 'PlayStation',
                3: 'Xbox',
                4: 'Nintendo Switch',
                5: 'Epic Games'
            };
            return platformMap[mallTagNo] || 'Unknown Platform';
        }
        return 'Steam'; // 默認值
    }

    /**
     * 高亮對應的左側標籤
     */
    highlightCorrespondingTag(mallTagNo) {
        try {
            if (!this.mallTagManager) return;
            
            // 首先移除所有標籤的高亮樣式
            this.clearAllTagHighlights();
            
            let tagId;
            if (mallTagNo && mallTagNo.id) {
                tagId = mallTagNo.id;
            } else if (typeof mallTagNo === 'number') {
                tagId = mallTagNo;
            } else if (mallTagNo && mallTagNo.malltagId) {
                tagId = mallTagNo.malltagId;
            }
            
            if (tagId) {
                // 高亮對應的標籤
                const targetTag = document.querySelector(`[data-malltag-id="${tagId}"]`);
                if (targetTag) {
                    targetTag.classList.add('active-product-tag');
                    console.log(`已高亮標籤 ID: ${tagId}`);
                }
            }
        } catch (error) {
            console.error('高亮標籤失敗:', error);
        }
    }

    /**
     * 清除所有標籤的高亮樣式
     */
    clearAllTagHighlights() {
        try {
            const allTags = document.querySelectorAll('.malltag-item, [data-malltag-id]');
            allTags.forEach(tag => {
                // 移除我們添加的高亮樣式
                tag.classList.remove('active-product-tag');
                
                // 移除原有的選中樣式
                tag.classList.remove('active');
                tag.classList.remove('malltag-active');
                
                // 清除內聯樣式
                tag.style.backgroundColor = '';
                tag.style.color = '';
            });
            console.log('已清除所有標籤的高亮樣式');
        } catch (error) {
            console.error('清除標籤高亮樣式失敗:', error);
        }
    }

    /**
     * 檢查並決定是否初始化
     */
    checkAndInit() {
        // 先檢查 URL 是否有有效的商品 ID
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        
        if (!id || isNaN(id) || parseInt(id) <= 0) {
            // 沒有有效的商品ID，直接跳轉
            console.log('沒有有效的商品ID，跳轉到商品列表頁');
            window.location.replace('product.html');
            return;
        }
        
        // 有效的商品ID，開始初始化
        console.log('檢測到有效商品ID:', id);
        this.init();
    }

    /**
     * 初始化頁面管理器
     */
    async init() {
        try {
            if (this.isInitialized) {
                console.log('ProductDetailManager 已經初始化過了');
                return;
            }

            console.log('ProductDetailManager 開始初始化...');
            
            // 等待 DOM 載入完成
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', () => this.setup());
            } else {
                await this.setup();
            }
        } catch (error) {
            console.error('ProductDetailManager 初始化失敗:', error);
        }
    }

    /**
     * 設定頁面
     */
    async setup() {
        try {
            if (this.isInitialized) {
                console.log('頁面已經設定過了');
                return;
            }

            console.log('開始設定商品詳情頁面...');

            // 注入樣式
            this.injectStyles();
            
            // 設定主容器（先設定容器再初始化其他組件）
            this.setupMainContainer();
            
            // 先初始化 MallTagManager（用於左側分類）
            await this.initMallTagManager();
            
            // 從 URL 獲取商品 ID
            const productId = this.getProductIdFromUrl();
            
            if (productId) {
                console.log('載入商品ID:', productId);
                await this.loadProductDetail(productId);
            } else {
                console.log('商品ID無效，顯示錯誤頁面');
                this.showNoProductError();
            }

            this.isInitialized = true;
            console.log('ProductDetailManager 設定完成');
            
        } catch (error) {
            console.error('頁面設定失敗:', error);
            this.showError('頁面載入失敗');
        }
    }

    /**
     * 初始化左側分類管理器
     */
    async initMallTagManager() {
        try {
            // 如果 MallTagManager 類別存在，創建實例
            if (window.MallTagManager) {
                console.log('初始化左側分類管理器...');
                this.mallTagManager = new window.MallTagManager();
                
                // 等待初始化完成
                await this.mallTagManager.init();
                
                // 重要：阻止 MallTagManager 控制右側內容區域
                this.disableMallTagProductDisplay();
                
                // 修改點擊行為：點擊分類時回到商品列表頁
                this.modifyMallTagBehavior();
                
                console.log('左側分類管理器初始化完成');
            } else {
                console.warn('MallTagManager 類別未找到');
            }
        } catch (error) {
            console.error('初始化分類管理器失敗:', error);
        }
    }

    /**
     * 阻止 MallTagManager 控制右側商品顯示區域
     */
    disableMallTagProductDisplay() {
        if (this.mallTagManager) {
            // 清空右側的商品容器，防止 MallTagManager 在其中顯示商品
            if (this.mallTagManager.productsContainer) {
                this.mallTagManager.productsContainer.innerHTML = '';
            }
            
            // 覆蓋 MallTagManager 的商品顯示方法，讓它不執行
            const originalDisplayProducts = this.mallTagManager.displayProducts;
            this.mallTagManager.displayProducts = function(products) {
                console.log('MallTagManager 的商品顯示被阻止，因為當前在商品詳情頁面');
                // 不執行任何操作
            };
            
            // 覆蓋搜尋方法
            const originalSearchAllProducts = this.mallTagManager.searchAllProducts;
            this.mallTagManager.searchAllProducts = function() {
                console.log('MallTagManager 的搜尋被阻止，因為當前在商品詳情頁面');
                // 不執行任何操作
            };
            
            const originalSearchProducts = this.mallTagManager.searchProducts;
            this.mallTagManager.searchProducts = function() {
                console.log('MallTagManager 的搜尋被阻止，因為當前在商品詳情頁面');
                // 不執行任何操作
            };
            
            // 移除"全部"按鈕的選中樣式
            this.removeAllTagActiveState();
            
            console.log('已阻止 MallTagManager 控制右側內容區域');
        }
    }

    /**
     * 移除"全部"按鈕的選中樣式
     */
    removeAllTagActiveState() {
        try {
            const allButton = document.querySelector('[data-malltag-id="all"]');
            if (allButton) {
                allButton.classList.remove('active');
                allButton.classList.remove('malltag-active');
                // 移除可能的內聯樣式
                allButton.style.backgroundColor = '';
                allButton.style.color = '';
                console.log('已移除"全部"按鈕的選中樣式');
            }
        } catch (error) {
            console.error('移除"全部"按鈕樣式失敗:', error);
        }
    }

    /**
     * 修改分類標籤的點擊行為
     */
    modifyMallTagBehavior() {
        if (this.mallTagManager) {
            const originalHandleClick = this.mallTagManager.handleMalltagClick;
            this.mallTagManager.handleMalltagClick = (malltagItem) => {
                const tagId = malltagItem.dataset.malltagId;
                const tagName = malltagItem.dataset.malltagName;
                
                console.log(`分類點擊: ${tagName} (ID: ${tagId})`);
                
                // 在商品詳情頁面點擊分類時，跳轉回商品列表頁面
                if (tagId === 'all') {
                    window.location.href = 'product.html';
                } else {
                    window.location.href = `product.html?category=${tagId}`;
                }
            };
        }

        // 確保全域的商品點擊函數正確
        window.handleProductClick = function(productId) {
            window.location.href = `product.html?id=${productId}`;
        };
    }

    /**
     * 從 URL 獲取商品 ID
     */
    getProductIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        
        if (id && !isNaN(id) && parseInt(id) > 0) {
            return parseInt(id);
        }
        
        return null;
    }

    /**
     * 設定主容器
     */
    setupMainContainer() {
        let targetContainer = document.getElementById('dynamic-content-container');
        
        if (!targetContainer) {
            const mainContent = document.querySelector('.col-lg-9');
            if (mainContent) {
                targetContainer = mainContent;
            }
        }

        if (targetContainer) {
            this.mainContainer = targetContainer;
            
            // 立即清空容器，防止其他內容干擾
            this.mainContainer.innerHTML = '';
            
            console.log('找到並清空主容器');
        } else {
            console.error('找不到主容器');
        }
    }

    /**
     * 載入商品詳情
     */
	/**
	 * 載入商品詳情
	 */
	async loadProductDetail(productId) {
	    try {
	        this.currentProductId = productId;
	        
	        // 顯示載入狀態
	        this.showLoading();
	        
	        // 更新頁面標題
	        document.title = `商品詳情 - 像素部落`;
	        
	        // 請求商品資料
	        const url = `${this.apiBaseUrl}/product/${productId}/search`;
	        console.log('請求 URL:', url);
	        
	        const response = await fetch(url);
	        console.log('API 回應狀態:', response.status);
	        
	        if (!response.ok) {
	            const errorText = await response.text();
	            
	            if (response.status === 404) {
	                throw new Error('商品不存在');
	            } else if (response.status === 500) {
	                throw new Error('服務器錯誤');
	            } else {
	                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
	            }
	        }
	        
	        const responseData = await response.json();
	        console.log('API 完整回應:', responseData);

	        let productData;
	        let inventoryData;

	        if (Array.isArray(responseData)) {
	            console.log('處理陣列格式回應');
	            if (responseData.length > 0) {
	                // 如果是陣列，取第一個元素
	                const firstItem = responseData[0];
	                productData = firstItem.product;
	                inventoryData = firstItem.inventory;
	            } else {
	                throw new Error('商品不存在');
	            }
	        } else {
	            console.log('處理物件格式回應');
	            // 如果是物件，從物件中取得 product 和 inventory
	            productData = responseData.product;
	            inventoryData = responseData.inventory;
	        }

	        console.log('解析後的商品資料:', productData);
	        console.log('解析後的庫存資料:', inventoryData);

	        // 檢查資料是否存在
	        if (!productData) {
	            throw new Error('商品資料不存在');
	        }
	        
	        // 確認商品資料有效
	        if (!productData || !productData.id) {
	            throw new Error('商品資料無效');
	        }
	        
	        // 更新頁面標題為商品名稱
	        document.title = `${productData.proName} - 像素部落`;
	        
	        // 🔥 重要修改：同時傳遞商品資料和庫存資料
	        this.displayProductDetail(productData, inventoryData);
	        
	        // 高亮對應的左側標籤
	        this.highlightCorrespondingTag(productData.mallTagNo);
	        
	    } catch (error) {
	        console.error('載入商品詳情失敗:', error);
	        
	        if (error.message === '商品不存在' || error.message === '商品資料無效') {
	            this.showProductNotFound();
	        } else {
	            this.showError('載入商品詳情失敗，請稍後再試');
	        }
	    }
	}
    /**
     * 顯示商品詳情
     */
	/**
	 * 顯示商品詳情
	 * @param {Object} product - 商品資料
	 * @param {Object} inventoryData - 庫存資料
	 */
	displayProductDetail(product, inventoryData = null) {
	    console.log('=== displayProductDetail 被呼叫 ===');
	    console.log('接收到的商品資料:', product);
	    console.log('接收到的庫存資料:', inventoryData);
	    
	    if (!this.mainContainer) {
	        console.error('主容器不存在');
	        return;
	    }

	    // 驗證商品資料
	    if (!product || !product.id) {
	        console.error('商品資料無效:', product);
	        this.showError('商品資料無效');
	        return;
	    }

	    console.log('顯示商品詳情:', product);

	    // 生成庫存區域 HTML
	    console.log('=== 生成庫存區域 ===');
	    const inventoryHtml = this.generateInventorySection(inventoryData);
	    console.log('生成的庫存 HTML:', inventoryHtml);

	    // 生成操作按鈕區域 HTML
	    console.log('=== 生成操作按鈕區域 ===');
	    const actionHtml = this.generateActionSection(product, inventoryData);
	    console.log('生成的操作按鈕 HTML:', actionHtml);

	    const detailHtml = `
	        <div class="product-detail-page">
	            <!-- 返回按鈕 -->
	            <div class="page-header mb-4">
	                <button class="back-button" onclick="window.productDetailManager.goBackToList()">
	                    <i class="bi bi-arrow-left"></i> 返回商品列表
	                </button>
	            </div>

	            <!-- 主要商品詳情區域 -->
	            <div class="product-main-section">
	                <div class="product-main-content">
	                    <!-- 左側：商品圖片 -->
	                    <div class="product-image-section">
	                        <div class="product-image-wrapper">
	                            <img src="${this.apiBaseUrl}/product/cover/${product.id}" 
	                                 alt="${this.escapeHtml(product.proName || '商品')}" 
	                                 class="product-main-image"
	                                 onerror="this.src='${this.generatePlaceholderImage()}'">
	                        </div>
	                    </div>
	                    
	                    <!-- 右側：商品資訊 -->
	                    <div class="product-info-main">
	                        <h1 class="product-title">${this.escapeHtml(product.proName || '未知商品')}</h1>
	                        <div class="product-price-display">NT$ ${this.formatPrice(product.proPrice || 0)}</div>
	                        
	                        <!-- 商品基本資訊 -->
	                        <div class="product-specs">
	                            <div class="spec-item">
	                                <span class="spec-label">狀態</span>
	                                <span class="spec-value">${this.escapeHtml(product.proStatus || '未知')}</span>
	                            </div>
	                            
	                            <div class="spec-item">
	                                <span class="spec-label">版本</span>
	                                <span class="spec-value">${this.escapeHtml(product.proVersion || '標準版')}</span>
	                            </div>
	                            
	                            <div class="spec-item">
	                                <span class="spec-label">發布日期</span>
	                                <span class="spec-value">${this.formatDate(product.proDate)}</span>
	                            </div>
	                        </div>
	                        
	                        <!-- 庫存顯示區域 - 動態生成 -->
	                        ${inventoryHtml}
	                        
	                        <!-- 操作按鈕區域 - 動態生成 -->
	                        ${actionHtml}
	                    </div>
	                </div>
	            </div>

	            <!-- 商品詳細內容區域 -->
	            <div class="product-content-section">
	                <!-- 商品內容 -->
	                <div class="content-block">
	                    <h3 class="content-title">商品內容：</h3>
	                    <div class="content-text">
	                        ${this.escapeHtml(product.proInclude || '商品內容資訊暫未提供')}
	                    </div>
	                </div>

	                <!-- 遊戲簡介 -->
	                <div class="content-block">
	                    <h3 class="content-title">遊戲簡介：</h3>
	                    <div class="content-text">
	                        ${this.escapeHtml(product.proDetails || '遊戲簡介暫未提供')}
	                    </div>
	                </div>
	            </div>

	            <!-- 遊戲畫面展示區域 -->
	            <div class="game-gallery-section">
	                <div class="game-screenshots">
	                    <!-- 這裡可以添加遊戲截圖輪播 -->
	                    <div class="screenshot-placeholder">
	                        <p>遊戲畫面展示區域</p>
	                        <small>此功能可在後續開發中擴展</small>
	                    </div>
	                </div>
	            </div>
	        </div>
	    `;

	    this.mainContainer.innerHTML = detailHtml;
	    console.log('=== 商品詳情顯示完成 ===');
	}
	
	displayInventoryInfo(inventoryData) {
	       if (!inventoryData) {
	           this.showInventoryError();
	           return;
	       }
	       
	       const statusBadge = document.getElementById('statusBadge');
	       const inventoryCount = document.getElementById('inventoryCount');
	       const inventoryText = document.getElementById('inventoryText');
	       const buyButton = document.getElementById('buyButton');
	       
	       // 顯示商品狀態
	       statusBadge.textContent = inventoryData.proStatus || '未知狀態';
	       
	       // 顯示庫存數量
	       inventoryCount.textContent = `庫存：${inventoryData.inventory || 0} 件`;
	       
	       // 顯示庫存狀態文字
	       inventoryText.textContent = inventoryData.displayText || '庫存狀態未知';
	       
	       // 根據商品狀態設置樣式
	       this.setInventoryStyle(inventoryData, statusBadge, buyButton);
	   }
	   setInventoryStyle(inventoryData, statusBadge, buyButton) {
	           // 清除之前的樣式
	           statusBadge.classList.remove('preorder', 'released', 'unavailable');
	           buyButton.classList.remove('available', 'unavailable');
	           
	           // 根據商品狀態設置樣式
	           switch (inventoryData.proStatus) {
	               case '預購中':
	                   statusBadge.classList.add('preorder');
	                   break;
	               case '已發售':
	                   statusBadge.classList.add('released');
	                   break;
	               default:
	                   statusBadge.classList.add('unavailable');
	           }
	           
	           // 根據可用性設置購買按鈕
	           if (inventoryData.isAvailable && inventoryData.inventory > 0) {
	               buyButton.classList.add('available');
	               buyButton.disabled = false;
	               buyButton.textContent = inventoryData.proStatus === '預購中' ? '立即預購' : '加入購物車';
	           } else {
	               buyButton.classList.add('unavailable');
	               buyButton.disabled = true;
	               buyButton.textContent = this.getUnavailableButtonText(inventoryData);
	           }
	       }
		   getUnavailableButtonText(inventoryData) {
		           switch (inventoryData.proStatus) {
		               case '預購中':
		                   return '預購已滿';
		               case '已發售':
		                   return '暫時缺貨';
		               default:
		                   return '無法購買';
		           }
		       }
		       
		       showInventoryError() {
		           document.getElementById('statusBadge').textContent = '狀態未知';
		           document.getElementById('inventoryCount').textContent = '庫存：-- 件';
		           document.getElementById('inventoryText').textContent = '無法取得庫存資訊';
		           
		           const buyButton = document.getElementById('buyButton');
		           buyButton.classList.add('unavailable');
		           buyButton.disabled = true;
		           buyButton.textContent = '無法購買';
		       }
			   
			   /**
			    * 生成庫存區域 HTML (簡化版)
			    */
			   generateInventorySection(inventoryData) {
			       console.log('生成庫存區域，資料:', inventoryData);
			       
			       if (!inventoryData) {
			           return `
			               <div class="inventory-info">
			                   <div class="inventory-count no-stock">庫存：-- 件</div>
			                   <div class="inventory-text no-stock">無法取得庫存資訊</div>
			               </div>
			           `;
			       }

			       // 判斷是否有庫存 (庫存數量大於 0)
			       const hasStock = inventoryData.inventory > 0;
			       const stockClass = hasStock ? 'has-stock' : 'no-stock';
			       
			       console.log(`庫存數量: ${inventoryData.inventory}, 有庫存: ${hasStock}, CSS類別: ${stockClass}`);

			       return `
			           <div class="inventory-info">
			               <div class="inventory-text ${stockClass}">${this.escapeHtml(inventoryData.displayText || '庫存狀態未知')}</div>
						   <div class="inventory-count ${stockClass}">庫存：${inventoryData.inventory || 0} 件</div>
						</div>
			       `;
			   }

			   /**
			    * 生成操作按鈕區域 HTML (簡化版)
			    */
			   generateActionSection(product, inventoryData) {
			       // 判斷是否有庫存
			       const hasStock = inventoryData && inventoryData.inventory > 0;
			       const buttonClass = hasStock ? 'available' : 'unavailable';
			       const buttonDisabled = hasStock ? '' : 'disabled';

			       return `
			           <div class="action-section">
			               <div class="action-buttons">
			                   <button class="btn-add-cart ${buttonClass}" ${buttonDisabled}
			                           onclick="window.productDetailManager.addToCart('${product.id}')">
			                       <i class="bi bi-cart-plus"></i> 加入購物車
			                   </button>
			                   <button class="btn-buy-now ${buttonClass}" ${buttonDisabled}
			                           onclick="window.productDetailManager.buyNow('${product.id}')">
			                       <i class="bi bi-lightning-fill"></i> 立即購買
			                   </button>
			                   <button class="btn-share" onclick="window.productDetailManager.copyLink()" title="分享商品">
			                       <i class="bi bi-share"></i>
			                   </button>
			               </div>
			               
			               <input type="text" class="share-url-hidden" readonly 
			                      value="${window.location.href}" 
			                      id="shareUrl" style="position: absolute; left: -9999px;">
			           </div>
			       `;
			   }
    /**
     * 返回商品列表頁面
     */
    goBackToList() {
        window.location.href = 'product.html';
    }

    /**
     * 複製分享連結
     */
    async copyLink() {
        try {
            const shareUrl = document.getElementById('shareUrl');
            
            if (navigator.clipboard) {
                await navigator.clipboard.writeText(shareUrl.value);
            } else {
                shareUrl.select();
                document.execCommand('copy');
            }
            
            this.showToast('連結已複製到剪貼簿！', 'success');
        } catch (error) {
            console.error('複製連結失敗:', error);
            this.showToast('複製失敗，請手動複製', 'error');
        }
    }

    /**
     * 加入購物車
     */
    addToCart(productId) {
        console.log('加入購物車:', productId);
        this.showToast('商品已加入購物車！', 'success');
    }

    /**
     * 立即購買
     */
    buyNow(productId) {
        console.log('立即購買:', productId);
        this.showToast('跳轉到結帳頁面...', 'info');
    }

    /**
     * 顯示載入狀態
     */
    showLoading() {
        if (this.mainContainer) {
            this.mainContainer.innerHTML = `
                <div class="loading-container">
                    <div class="loading-content">
                        <div class="spinner-border text-primary" role="status"></div>
                        <h4 class="mt-3">載入商品詳情中...</h4>
                        <p class="text-muted">請稍候</p>
                    </div>
                </div>
            `;
        }
    }

    /**
     * 顯示商品不存在錯誤
     */
    showProductNotFound() {
        if (this.mainContainer) {
            this.mainContainer.innerHTML = `
                <div class="error-container">
                    <div class="error-content">
                        <i class="bi bi-exclamation-triangle fs-1 text-warning"></i>
                        <h3 class="mt-3">商品不存在</h3>
                        <p class="text-muted">抱歉，您要查看的商品不存在或已被移除。</p>
                        <button class="btn btn-primary" onclick="window.productDetailManager.goBackToList()">
                            <i class="bi bi-arrow-left"></i> 返回商品列表
                        </button>
                    </div>
                </div>
            `;
        }
    }

    /**
     * 顯示沒有商品ID錯誤
     */
    showNoProductError() {
        if (this.mainContainer) {
            this.mainContainer.innerHTML = `
                <div class="error-container">
                    <div class="error-content">
                        <i class="bi bi-question-circle fs-1 text-info"></i>
                        <h3 class="mt-3">缺少商品參數</h3>
                        <p class="text-muted">請提供有效的商品ID參數。</p>
                        <p class="small text-muted">正確格式：product.html?id=1</p>
                        <button class="btn btn-primary" onclick="window.productDetailManager.goBackToList()">
                            <i class="bi bi-arrow-left"></i> 返回商品列表
                        </button>
                    </div>
                </div>
            `;
        }
    }

    /**
     * 顯示一般錯誤
     */
    showError(message) {
        if (this.mainContainer) {
            this.mainContainer.innerHTML = `
                <div class="error-container">
                    <div class="error-content">
                        <i class="bi bi-exclamation-triangle fs-1 text-danger"></i>
                        <h3 class="mt-3">載入失敗</h3>
                        <p class="text-muted">${message}</p>
                        <div class="mt-3">
                            <button class="btn btn-primary me-2" onclick="window.location.reload()">
                                <i class="bi bi-arrow-clockwise"></i> 重新載入
                            </button>
                            <button class="btn btn-secondary" onclick="window.productDetailManager.goBackToList()">
                                <i class="bi bi-arrow-left"></i> 返回列表
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }
    }

    /**
     * 顯示提示訊息
     */
    showToast(message, type = 'success') {
        const toast = document.createElement('div');
        let bgColor;
        
        switch (type) {
            case 'success':
                bgColor = 'var(--green-color)';
                break;
            case 'error':
                bgColor = 'var(--red-color, #dc3545)';
                break;
            case 'info':
                bgColor = 'var(--blue-color, #007bff)';
                break;
            default:
                bgColor = 'var(--green-color)';
        }
        
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${bgColor};
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            z-index: 9999;
            font-weight: 500;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideInToast 0.3s ease;
        `;
        toast.textContent = message;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideOutToast 0.3s ease';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }, 3000);
    }

    /**
     * 獲取狀態樣式類別
     */
    getStatusClass(proStatus) {
		switch (proStatus) {
		        case '預購中':
		            return 'preorder';
		        case '已發售':
		            return 'released';
		        default:
		            return 'unavailable';
		    }
    }

    /**
     * 格式化價格
     */
    formatPrice(price) {
        return new Intl.NumberFormat('zh-TW').format(price);
    }

    /**
     * 格式化日期
     */
    formatDate(dateString) {
        if (!dateString) return '未知';
        
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('zh-TW', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        } catch (error) {
            return dateString;
        }
    }

    /**
     * HTML 轉義
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 生成佔位圖片
     */
    generatePlaceholderImage() {
        const svg = `
            <svg xmlns="http://www.w3.org/2000/svg" width="500" height="400" viewBox="0 0 500 400">
                <rect width="500" height="400" fill="#e2e8f0"/>
                <text x="250" y="200" text-anchor="middle" dy="0.3em" font-family="Arial, sans-serif" font-size="18" fill="#64748b">
                    圖片無法載入
                </text>
            </svg>
        `;
        return 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svg)));
    }

    /**
     * 注入樣式
     */
    injectStyles() {
        const styleId = 'product-detail-styles';
        if (document.getElementById(styleId)) return;

        const style = document.createElement('style');
        style.id = styleId;
        style.textContent = `
            /* 頁面主要樣式 */
            .product-detail-page {
                min-height: 70vh;
                max-width: 1200px;
                margin: 0 auto;
                padding: 0 1rem;
            }

            .page-header {
                display: flex;
                align-items: center;
                margin-bottom: 2rem;
            }

            .back-button {
                background: var(--green-color);
                color: white;
                border: none;
                padding: 0.75rem 1.5rem;
                border-radius: 10px;
                cursor: pointer;
                transition: all 0.3s ease;
                font-weight: 500;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                gap: 0.5rem;
            }

            .back-button:hover {
                background: var(--green-color-hover);
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0,0,0,0.2);
            }

            /* 主要商品資訊區域 */
            .product-main-section {
                background: var(--theme-white);
                border-radius: 15px;
                padding: 2rem;
                margin-bottom: 2rem;
                box-shadow: 0px 10px 30px rgba(0, 0, 0, 0.1);
            }

            .product-main-content {
                display: grid;
                grid-template-columns: 360px 1fr;
                gap: 2rem;
                align-items: flex-start;
            }

            /* 左側圖片區域 */
            .product-image-section {
                position: relative;
            }

            .product-image-wrapper {
                background: #f5f5f5;
                border-radius: 8px;
                padding: 1rem;
                text-align: center;
            }

            .product-main-image {
                width: 100%;
                max-height: 420px;
                object-fit: cover;
                border-radius: 6px;
            }

            /* 右側商品資訊 */
            .product-info-main {
                padding-left: 0.5rem;
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
            }

            .product-title {
                font-size: 1.8rem;
                font-weight: 700;
                color: var(--text-color);
                margin-bottom: 1rem;
                line-height: 1.2;
            }

            .product-price-display {
                font-size: 2.2rem;
                font-weight: 800;
                color: #4CAF50;
                margin-bottom: 1.5rem;
                letter-spacing: 1px;
            }

            /* 商品規格 */
            .spec-item {
                display: flex;
                justify-content: space-between;
                padding: 1rem 0;
                border-bottom: 1px solid #eee;
            }

            .spec-item:last-child {
                border-bottom: none;
            }

            .spec-label {
                font-weight: 600;
                color: var(--text-color);
                min-width: 100px;
            }

            .spec-value {
                color: #666;
                font-weight: 500;
            }

            /* 操作按鈕區域 */
            .action-section {
                margin-bottom: 1.5rem;
            }

            .action-buttons {
                display: grid;
                grid-template-columns: 1fr 1fr auto;
                gap: 0.8rem;
                align-items: center;
            }

            .btn-add-cart, .btn-buy-now {
                padding: 0.8rem 1.5rem;
                border: none;
                border-radius: 6px;
                font-weight: 600;
                font-size: 0.95rem;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 0.4rem;
            }

            .btn-share {
                width: 45px;
                height: 45px;
                border: none;
                border-radius: 6px;
                background: #6c757d;
                color: white;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 1.1rem;
            }

            .btn-share:hover {
                background: #5a6268;
                transform: translateY(-1px);
                box-shadow: 0 3px 8px rgba(108, 117, 125, 0.3);
            }

            .btn-add-cart {
                background: #4CAF50;
                color: white;
            }

            .btn-add-cart:hover {
                background: #45a049;
                transform: translateY(-1px);
                box-shadow: 0 4px 10px rgba(76, 175, 80, 0.3);
            }

            .btn-buy-now {
                background: #FF5722;
                color: white;
            }

            .btn-buy-now:hover {
                background: #E64A19;
                transform: translateY(-1px);
                box-shadow: 0 4px 10px rgba(255, 87, 34, 0.3);
            }

            /* 左側標籤高亮樣式 - 與 malltag.js 保持一致 */
            .active-product-tag {
                background: var(--bg-icons-hover) !important;
                color: var(--text-color) !important;
                border-color: var(--bg-icons-border-hover) !important;
                transform: none !important;
                box-shadow: 0 4px 10px 2px rgba(0, 0, 0, 0.2) !important;
            }

            /* 確保標籤在hover時也保持選中樣式 */
            .active-product-tag:hover {
                background: var(--bg-icons-hover) !important;
                color: var(--text-color) !important;
                border-color: var(--bg-icons-border-hover) !important;
                box-shadow: 0 4px 10px 2px rgba(0, 0, 0, 0.2) !important;
            }

            /* 確保"全部"和其他標籤在商品詳情頁面時保持正常樣式 */
            .malltag-item:not(.active-product-tag) {
                background: var(--theme-white) !important;
                color: var(--text-color) !important;
                border-color: var(--border-color) !important;
            }

            /* "全部"按鈕特別處理 - 移除 active 狀態的樣式 */
            [data-malltag-id="all"]:not(.active-product-tag) {
                background: var(--theme-white) !important;
                color: var(--text-color) !important;
                border-color: var(--border-color) !important;
            }

            /* 隱藏原來的分享區域樣式 */
            .share-url-hidden {
                opacity: 0;
                pointer-events: none;
            }

            /* 內容區域 */
            .product-content-section {
                display: grid;
                gap: 2rem;
                margin-bottom: 2rem;
            }

            .content-block {
                background: var(--theme-white);
                border-radius: 10px;
                padding: 2rem;
                box-shadow: 0px 5px 15px rgba(0, 0, 0, 0.05);
            }

            .content-title {
                color: var(--text-color);
                font-size: 1.3rem;
                font-weight: 700;
                margin-bottom: 1rem;
                border-left: 4px solid #4CAF50;
                padding-left: 1rem;
            }

            .content-text {
                color: #555;
                line-height: 1.7;
                font-size: 1rem;
            }

            /* 遊戲畫面展示區域 */
            .game-gallery-section {
                background: var(--theme-white);
                border-radius: 10px;
                padding: 2rem;
                box-shadow: 0px 5px 15px rgba(0, 0, 0, 0.05);
                margin-bottom: 2rem;
            }

            .gallery-title {
                color: var(--text-color);
                font-size: 1.5rem;
                font-weight: 700;
                margin-bottom: 1.5rem;
                text-align: center;
            }

            .game-screenshots {
                min-height: 300px;
                background: #f8f9fa;
                border-radius: 10px;
                display: flex;
                align-items: center;
                justify-content: center;
                border: 2px dashed #ddd;
            }

            .screenshot-placeholder {
                text-align: center;
                color: #6c757d;
            }

            .screenshot-placeholder p {
                font-size: 1.2rem;
                margin-bottom: 0.5rem;
            }

            .screenshot-placeholder small {
                font-size: 0.9rem;
                opacity: 0.7;
            }

            /* 載入和錯誤狀態 */
            .loading-container,
            .error-container {
                min-height: 60vh;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .loading-content,
            .error-content {
                text-align: center;
                padding: 3rem;
            }

            .error-content i {
                font-size: 4rem;
                margin-bottom: 1rem;
            }

            /* Toast 動畫 */
            @keyframes slideInToast {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }

            @keyframes slideOutToast {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }

            /* 響應式設計 */
            @media (max-width: 768px) {
                .product-main-content {
                    grid-template-columns: 1fr;
                    gap: 2rem;
                }
                
                .product-info-main {
                    padding-left: 0;
                }
                
                .action-buttons {
                    grid-template-columns: 1fr;
                    gap: 0.8rem;
                }
                
                .btn-share {
                    width: 100%;
                    height: 45px;
                    justify-self: stretch;
                }
                
                .product-title {
                    font-size: 1.5rem;
                }

                .product-price-display {
                    font-size: 2rem;
                }

                .spec-item {
                    flex-direction: column;
                    gap: 0.5rem;
                }
            }
			
			/*庫存樣式*/

			.inventory-count {
			    font-weight: bold;
			    font-size: 14px;
			    margin-bottom: 1px;  /* 減少間距 */
			}

			.inventory-text {
			    font-size: 16px;
			}

			/* 有庫存 - 綠色文字 */
			.inventory-count.has-stock,
			.inventory-text.has-stock {
			    color: #4CAF50;
			}

			/* 無庫存 - 紅色文字 */
			.inventory-count.no-stock,
			.inventory-text.no-stock {
			    color: #f44336;
			}

			/* 按鈕狀態樣式保持不變 */
			.btn-add-cart.available, .btn-buy-now.available {
			    opacity: 1;
			    cursor: pointer;
			}

			.btn-add-cart.unavailable, .btn-buy-now.unavailable {
			    background-color: #ccc !important;
			    color: #666 !important;
			    cursor: not-allowed;
			    opacity: 0.6;
			}

			.btn-add-cart.unavailable:hover, .btn-buy-now.unavailable:hover {
			    background-color: #ccc !important;
			    transform: none !important;
			    box-shadow: none !important;
			}
			`;

        document.head.appendChild(style);
    }
}

// 防止重複創建實例的全域檢查
if (!window.productDetailManagerCreated) {
    window.productDetailManagerCreated = true;
    window.ProductDetailManager = ProductDetailManager;
    
    // 只有在當前頁面是 product.html 且有商品ID時才創建實例
    const currentPage = window.location.pathname;
    const urlParams = new URLSearchParams(window.location.search);
    const hasProductId = urlParams.has('id') && urlParams.get('id');
    
    if (currentPage.includes('product.html')) {
        console.log('在 product.html 頁面，創建 ProductDetailManager 實例');
        window.productDetailManager = new ProductDetailManager();
    } else {
        console.log('不在 product.html 頁面，跳過 ProductDetailManager 創建');
    }
} else {
    console.log('ProductDetailManager 已經創建過了');
}