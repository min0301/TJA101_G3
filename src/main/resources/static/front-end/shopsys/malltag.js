/**
 * malltag.js - 商城標籤管理系統
 * 負責載入和顯示商城標籤分類，並在右側顯示商品
 */

class MallTagManager {
	constructor() {
		
		// << 修正成使用動態API URL，自動適應環境
		this.apiBaseUrl = `${location.origin}/api`;
		this.malltagContainer = null;
		this.productsContainer = null;
		this.currentSelectedTag = null;
		this.malltagData = [];
		this.isInitialized = false;

		// 不在構造函數中自動調用 init()，改由外部控制
	}

	/**
	 * 初始化標籤管理器
	 */
	async init() {
		try {
			// 如果已經初始化過，直接返回
			if (this.isInitialized) {
				return;
			}

			// 等待 DOM 載入完成
			if (document.readyState === 'loading') {
				return new Promise((resolve) => {
					document.addEventListener('DOMContentLoaded', async () => {
						await this.setup();
						resolve();
					});
				});
			} else {
				await this.setup();
			}
		} catch (error) {
			console.error('MallTag 初始化失敗:', error);
		}
	}

	/**
	 * 設定標籤系統
	 */
	async setup() {
		try {
			this.createMalltagContainer();
			this.setupProductsContainer();
			await this.loadMalltagData();
			this.renderMalltags(); // 這裡會自動載入"全部"
			this.setupEventListeners();
			this.isInitialized = true;
			console.log('MallTagManager 初始化完成');
		} catch (error) {
			console.error('MallTag 設定失敗:', error);
		}
	}

	/**
	 * 創建標籤容器
	 */
	createMalltagContainer() {
		// 尋找現有的 malltag-list 容器
		this.malltagContainer = document.getElementById('malltag-list');

		if (!this.malltagContainer) {
			// 如果不存在，在分類側邊欄下方創建
			const sidebarContainer = document.querySelector('.col-lg-3 .position-sticky');
			if (sidebarContainer) {
				const malltagSection = document.createElement('div');
				malltagSection.className = 'malltag-section mt-4';
				malltagSection.innerHTML = `
                    <h5 class="mb-3"><i class="bi bi-tags me-2"></i>平台分類</h5>
                    <div id="malltag-list" class="malltag-container">
                        <div class="loading-malltags">
                            <span class="loading-spinner"></span>
                            載入標籤中...
                        </div>
                    </div>
                `;
				sidebarContainer.appendChild(malltagSection);
				this.malltagContainer = document.getElementById('malltag-list');
			} else {
				console.warn('找不到分類側邊欄容器');
				return;
			}
		}

		// 添加必要的 CSS 樣式
		this.injectStyles();
	}

	/**
	 * 設定商品顯示容器
	 */
	setupProductsContainer() {
		// 先找 dynamic-content-container
		let targetContainer = document.getElementById('dynamic-content-container');

		if (!targetContainer) {
			// 如果沒有，找到右側主要內容區域
			const mainContent = document.querySelector('.col-lg-9');
			if (mainContent) {
				targetContainer = mainContent;
			}
		}

		if (targetContainer) {
			// 創建商品顯示結構
			const productsSection = document.createElement('div');
			productsSection.id = 'products-section';
			productsSection.innerHTML = `
                <div class="products-header mb-4" style="display: none;">
                    <div class="d-flex justify-content-between align-items-center">
                        <h4 id="products-title">商品列表</h4>
                        <div class="products-meta">
                            <span id="results-count" class="badge bg-primary"></span>
                        </div>
                    </div>
                </div>
                
                <div id="products-loading" class="text-center py-5" style="display: none;">
                    <div class="spinner-border text-primary me-2" role="status">
                        <span class="visually-hidden">載入中...</span>
                    </div>
                    <span>載入商品中...</span>
                </div>

                <div id="products-error" class="alert alert-danger" style="display: none;"></div>

                <div id="products-container" class="products-grid"></div>

                <div id="no-results" class="text-center py-5" style="display: none;">
                    <div class="text-muted">
                        <i class="bi bi-search fs-1 mb-3 d-block"></i>
                        <h5>找不到相關商品</h5>
                        <p>請嘗試選擇其他標籤或調整篩選條件</p>
                    </div>
                </div>
            `;

			// 清空容器並插入商品區域
			targetContainer.innerHTML = '';
			targetContainer.appendChild(productsSection);

			this.productsContainer = document.getElementById('products-container');
		}
	}

	/**
	 * 注入必要的 CSS 樣式
	 */
	injectStyles() {
		const styleId = 'malltag-styles';

		if (document.getElementById(styleId)) return;

		const style = document.createElement('style');
		style.id = styleId;
		style.textContent = `
		        /* 側邊欄容器樣式 */
		        .mall-category-sidebar {
		            background: transparent;
		            border-radius: 0;
		            padding: 0;
		            border: none;
		            box-shadow: none;
		            width: 100%;
		        }

		        .mall-category-sidebar h5 {
		            color: var(--text-color);
		            font-weight: 600;
		            margin-bottom: 1rem;
		            padding-bottom: 0.5rem;
		            border-bottom: none;
		            font-size: 1.1rem;
		            text-align: center;
		        }

		        .malltag-container {
		            display: flex;
		            flex-direction: column;
		            gap: 0.3rem;
		            max-height: 400px;
		            overflow-y: auto;
		            width: 100%;
		        }

		        .malltag-item {
		            display: flex;
		            align-items: center;
		            justify-content: center;
		            padding: 1rem 1.5rem;
		            background: var(--theme-white);
		            border: 1px solid var(--border-color);
		            border-radius: 10px;
		            text-decoration: none;
		            color: var(--text-color);
		            transition: all 0.3s ease;
		            cursor: pointer;
		            position: relative;
		            margin-bottom: 0.3rem;
		            width: 100%;
		            min-height: 60px;
		            box-sizing: border-box;
		        }

		        .malltag-item:hover {
		            background: var(--bg-icons-hover);
		            color: var(--text-color);
		            transform: none;
		            box-shadow: 0 4px 10px 2px rgba(0, 0, 0, 0.2);
		            text-decoration: none;
		        }

		        .malltag-item.active {
		            background: var(--bg-icons-hover);
		            color: var(--text-color);
		            border-color: var(--bg-icons-border-hover);
		        }

		        .malltag-name {
		            font-weight: 600;
		            font-size: 1.2rem;
		            color: var(--text-color);
		            text-align: center;
		            width: 100%;
		            line-height: 1.4;
		        }

		        .loading-malltags {
		            display: flex;
		            align-items: center;
		            justify-content: center;
		            padding: 2rem;
		            color: var(--text-color);
		            font-size: 1rem;
		            width: 100%;
		        }

		        .loading-spinner {
		            width: 20px;
		            height: 20px;
		            border: 2px solid var(--border-color);
		            border-top: 2px solid var(--green-color);
		            border-radius: 50%;
		            animation: spin 1s linear infinite;
		            margin-right: 0.5rem;
		        }

		        /* 商品顯示區域樣式 - 修正佈局 */
		        .products-grid {
		            display: grid;
		            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		            gap: 1.5rem;
		            margin-bottom: 2rem;
		        }

		        /* 修正商品卡片 - 確保購物車按鈕不被裁切 */
		        .product-card {
		            background: var(--theme-white);
		            border: 1px solid var(--border-color);
		            border-radius: 13px;
		            overflow: hidden; /* 保持 overflow: hidden */
		            transition: all 0.3s ease;
		            box-shadow: 0px 30px 40px rgb(2 45 62 / 8%);
		            display: flex;
		            flex-direction: column;
		            height: 100%;
		        }

		        .product-card:hover {
		            transform: translateY(-4px);
		            box-shadow: 0 16px 30px 2px rgba(0, 0, 0, 0.26);
		        }

		        /* 商品卡片點擊區域 */
		        .product-clickable-area {
		            cursor: pointer;
		            flex-grow: 1;
		            display: flex;
		            flex-direction: column;
		        }

		        .product-clickable-area:hover {
		            opacity: 0.9;
		        }

		        /* 修正圖片樣式 */
		        .product-image {
		            width: 100%;
		            height: 200px;
		            object-fit: contain;
		            background: var(--bg-color);
		            flex-shrink: 0;
		            padding: 0.5rem;
		            box-sizing: border-box;
		        }

		        /* 修正商品資訊區域 */
		        .product-info {
		            padding: 1.25rem;
		            position: relative;
		            display: flex;
		            flex-direction: column;
		            flex-grow: 1;
		        }

		        .product-name {
		            font-weight: 600;
		            font-size: 1.1rem;
		            color: var(--text-color);
		            margin-bottom: 0.5rem;
		            line-height: 1.4;
		            display: -webkit-box;
		            -webkit-line-clamp: 2;
		            -webkit-box-orient: vertical;
		            overflow: hidden;
		            flex-grow: 1;
		        }

		        /* 修正價格樣式 - 移除底部間距 */
		        .product-price {
		            font-size: 1.2rem;
		            font-weight: 700;
		            color: var(--text-color);
		            margin-bottom: 0;
		            margin-top: auto;
		        }

		        /* 修正購物車按鈕 - 不使用負邊距 */
		        .product-cart-area {
		            background: var(--green-color);
		            margin: 0; /* 移除所有負邊距 */
		            padding: 0.75rem 1.25rem;
		            display: flex;
		            align-items: center;
		            justify-content: center;
		            cursor: pointer;
		            transition: all 0.3s ease;
		            border-radius: 0 0 13px 13px; /* 只讓底部圓角 */
		        }

		        .product-cart-icon {
		            color: var(--white);
		            font-size: 1.2rem;
		            margin-right: 0.5rem;
		        }

		        .product-cart-text {
		            color: var(--white);
		            font-weight: 500;
		            font-size: 0.9rem;
		        }

		        .products-header h4 {
		            color: var(--text-color);
		            margin: 0;
		            font-size: 1.3rem;
		        }

		        .malltag-error,
		        .products-error {
		            text-align: center;
		            padding: 1.5rem;
		            color: var(--theme);
		            font-size: 1rem;
		            width: 100%;
		        }

		        /* 滾動條樣式 */
		        .malltag-container::-webkit-scrollbar {
		            width: 8px;
		        }

		        .malltag-container::-webkit-scrollbar-track {
		            background: var(--bg-color);
		            border-radius: 4px;
		        }

		        .malltag-container::-webkit-scrollbar-thumb {
		            background: var(--green-color);
		            border-radius: 4px;
		        }

		        @keyframes spin {
		            to { transform: rotate(360deg); }
		        }

		        /* 響應式設計 */
		        @media (max-width: 768px) {
		            .mall-category-sidebar {
		                margin-bottom: 1.5rem;
		            }
		            
		            .products-grid {
		                grid-template-columns: 1fr;
		            }
		            
		            .malltag-item {
		                padding: 0.8rem 1.2rem;
		                min-height: 50px;
		            }
		            
		            .malltag-name {
		                font-size: 1.1rem;
		            }
		            
		            .mall-category-sidebar h5 {
		                font-size: 1rem;
		                text-align: center;
		            }
		        }
		    `;

		document.head.appendChild(style);
	}

	/**
	 * 從資料庫載入標籤資料
	 */
	async loadMalltagData() {
		try {
			this.showLoading();

			const response = await fetch(`${this.apiBaseUrl}/malltag`);

			if (!response.ok) {
				throw new Error(`HTTP error! status: ${response.status}`);
			}

			const mallTags = await response.json();
			this.malltagData = this.processMalltagData(mallTags);

		} catch (error) {
			console.error('載入 malltag 資料失敗:', error);
			this.showError('載入商城標籤失敗，請檢查後端服務是否正常運行');
			this.malltagData = [];
		}
	}

	/**
	 * 處理 API 回傳的標籤資料
	 */
	processMalltagData(rawData) {
		let processedData = [];

		// 首先添加"全部"選項
		processedData.push({
			id: 'all',
			name: '全部',
			icon: '全'
		});

		// 然後添加從API獲取的標籤
		if (Array.isArray(rawData)) {
			const apiTags = rawData.map(tag => ({
				id: tag.id,
				name: tag.mallTagName,
				icon: tag.mallTagName.charAt(0).toUpperCase()
			}));
			processedData = processedData.concat(apiTags);
		}

		return processedData;
	}

	/**
	 * 從 URL 獲取分類參數
	 */
	getCategoryFromUrl() {
		const urlParams = new URLSearchParams(window.location.search);
		const categoryId = urlParams.get('category');

		if (categoryId && categoryId !== 'all') {
			return categoryId;
		}

		return null;
	}

	/**
	 * 根據 URL 參數自動選擇分類
	 */
	autoSelectCategoryFromUrl() {
		const categoryId = this.getCategoryFromUrl();

		if (categoryId) {
			console.log('從 URL 獲取分類ID:', categoryId);

			// 尋找對應的分類項目
			const targetTag = this.malltagContainer.querySelector(`[data-malltag-id="${categoryId}"]`);

			if (targetTag) {
				console.log('找到目標分類，自動選擇');
				this.handleMalltagClick(targetTag);
				return true;
			} else {
				console.warn('URL 中的分類ID不存在，使用預設選項');
			}
		}

		// 如果沒有 URL 參數或找不到對應分類，選擇"全部"
		this.autoSelectAllOption();
		return false;
	}

	/**
	 * 更新 URL 中的分類參數
	 */
	updateUrlWithCategory(categoryId) {
		if (!window.history || !window.history.pushState) {
			return; // 不支援 History API
		}

		const url = new URL(window.location);

		if (categoryId === 'all') {
			// 移除 category 參數
			url.searchParams.delete('category');
		} else {
			// 設定 category 參數
			url.searchParams.set('category', categoryId);
		}

		// 更新 URL（不重新載入頁面）
		window.history.pushState({}, '', url);
		console.log('URL 已更新:', url.toString());
	}

	/**
	 * 渲染標籤列表
	 */
	renderMalltags() {
		console.log('開始渲染標籤列表...');

		if (!this.malltagContainer) {
			console.error('標籤容器不存在');
			return;
		}

		if (this.malltagData.length === 0) {
			console.warn('沒有標籤資料');
			this.showError('沒有可用的商城標籤');
			return;
		}

		const malltagList = this.malltagData.map(tag => this.createMalltagItem(tag)).join('');
		this.malltagContainer.innerHTML = malltagList;
		console.log('標籤列表渲染完成');

		// 根據 URL 參數或預設選擇分類
		this.autoSelectCategoryFromUrl();
	}

	/**
	 * 創建單個標籤項目
	 */
	createMalltagItem(tag) {
		return `
			       <div class="malltag-item" 
			            data-malltag-id="${tag.id}"
			            data-malltag-name="${tag.name}"
			            title="點擊查看 ${tag.name} 相關商品">
			           <span class="malltag-name">${tag.name}</span>
			       </div>
			   `;
	}

	/**
	 * 設定事件監聽器
	 */
	setupEventListeners() {
		if (!this.malltagContainer) return;

		// 標籤點擊事件
		this.malltagContainer.addEventListener('click', (e) => {
			const malltagItem = e.target.closest('.malltag-item');
			if (malltagItem) {
				this.handleMalltagClick(malltagItem);
			}
		});
	}

	/**
	 * 處理標籤點擊
	 */
	handleMalltagClick(malltagItem) {
		const tagId = malltagItem.dataset.malltagId;
		const tagName = malltagItem.dataset.malltagName;

		// 更新選中狀態
		this.updateSelectedTag(malltagItem);

		// 顯示商品標題
		this.showProductsHeader(tagName);

		// 更新 URL（不重新載入頁面）
		this.updateUrlWithCategory(tagId);

		// 根據選擇的標籤ID決定搜尋方式
		if (tagId === 'all') {
			this.searchAllProducts();
		} else {
			this.searchProducts(tagId);
		}

		console.log(`選擇了標籤: ${tagName} (ID: ${tagId})`);
	}

	async searchAllProducts() {
		try {
			this.showProductsLoading();

			const url = `${this.apiBaseUrl}/product/searchall`;
			const response = await fetch(url);

			if (!response.ok) {
				throw new Error(`HTTP error! status: ${response.status}`);
			}

			const products = await response.json();
			this.displayProducts(products);

		} catch (error) {
			console.error('搜尋所有商品失敗:', error);
			this.showProductsError('搜尋失敗，請檢查網路連接或後端服務是否正常運行');
		} finally {
			this.hideProductsLoading();
		}
	}

	autoSelectAllOption() {
		const allOption = this.malltagContainer.querySelector('[data-malltag-id="all"]');
		if (allOption) {
			// 設置為選中狀態
			allOption.classList.add('active');
			this.currentSelectedTag = allOption;

			// 顯示標題
			this.showProductsHeader('全部');

			// 載入所有商品
			this.searchAllProducts();
		}
	}

	/**
	 * 更新選中標籤的視覺狀態
	 */
	updateSelectedTag(selectedItem) {
		// 移除所有活動狀態
		this.malltagContainer.querySelectorAll('.malltag-item').forEach(item => {
			item.classList.remove('active');
		});

		// 添加到選中項目
		selectedItem.classList.add('active');
		this.currentSelectedTag = selectedItem;
	}

	/**
	 * 顯示商品區域標題
	 */
	showProductsHeader(tagName) {
		const productsHeader = document.querySelector('.products-header');
		const productsTitle = document.getElementById('products-title');

		if (productsHeader && productsTitle) {
			if (tagName === '全部') {
				productsTitle.textContent = '所有商品';
			} else {
				productsTitle.textContent = `${tagName} - 商品列表`;
			}
			productsHeader.style.display = 'block';
		}
	}

	/**
	 * 搜尋商品
	 */
	async searchProducts(mallTagId) {
		if (!mallTagId) return;

		try {
			this.showProductsLoading();

			const url = `${this.apiBaseUrl}/product/search/${mallTagId}`;
			const response = await fetch(url);

			if (!response.ok) {
				throw new Error(`HTTP error! status: ${response.status}`);
			}

			const products = await response.json();
			this.displayProducts(products);

		} catch (error) {
			console.error('搜尋商品失敗:', error);
			this.showProductsError('搜尋失敗，請檢查網路連接或後端服務是否正常運行');
		} finally {
			this.hideProductsLoading();
		}
	}

	/**
	 * 顯示商品列表
	 */
	displayProducts(products) {
		this.hideAllProductMessages();

		if (!products || products.length === 0) {
			this.showNoResults();
			return;
		}

		// 顯示結果數量
		const resultsCount = document.getElementById('results-count');
		if (resultsCount) {
			resultsCount.textContent = `${products.length} 個商品`;
		}

		// 生成商品卡片
		if (this.productsContainer) {
			this.productsContainer.innerHTML = products.map(product => this.createProductCard(product)).join('');
		}
	}

	/**
	 * 創建商品卡片
	 */
	createProductCard(product) {
		console.log('創建商品卡片，ID:', product.id);

		return `
	        <div class="product-card" data-product-id="${product.id}">
	            <div class="product-clickable-area" onclick="window.location.href='/front-end/shopsys/product.html?id=${product.id}'">
	                <img src="${this.apiBaseUrl}/product/cover/${product.id}" 
	                     alt="${this.escapeHtml(product.proName)}" 
	                     class="product-image"
	                     onerror="this.src='${this.generatePlaceholderImage()}'">
	                <div class="product-info">
	                    <div class="product-name">${this.escapeHtml(product.proName)}</div>
	                    <div class="product-price">NT$ ${this.formatPrice(product.proPrice)}</div>
	                </div>
	            </div>
	            
	            <!-- 購物車按鈕區域 -->
	            <div class="product-cart-area" onclick="event.stopPropagation(); addToCart('${product.id}')">
	                <i class="bi bi-cart-plus product-cart-icon"></i>
	                <span class="product-cart-text">加入購物車</span>
	            </div>
	        </div>
	    `;
	}

	/**
	 * 生成佔位圖片
	 */
	generatePlaceholderImage() {
		const svg = `
            <svg xmlns="http://www.w3.org/2000/svg" width="280" height="200" viewBox="0 0 280 200">
                <rect width="280" height="200" fill="#e2e8f0"/>
                <text x="140" y="100" text-anchor="middle" dy="0.3em" font-family="Arial, sans-serif" font-size="14" fill="#64748b">
                    圖片無法載入
                </text>
            </svg>
        `;
		return 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svg)));
	}

	/**
	 * 格式化價格
	 */
	formatPrice(price) {
		return new Intl.NumberFormat('zh-TW').format(price);
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
	 * 顯示載入狀態
	 */
	showLoading() {
		if (this.malltagContainer) {
			this.malltagContainer.innerHTML = `
                <div class="loading-malltags">
                    <span class="loading-spinner"></span>
                    載入標籤中...
                </div>
            `;
		}
	}

	/**
	 * 顯示商品載入狀態
	 */
	showProductsLoading() {
		const loading = document.getElementById('products-loading');
		if (loading) {
			loading.style.display = 'block';
		}
		this.hideAllProductMessages();
	}

	/**
	 * 隱藏商品載入狀態
	 */
	hideProductsLoading() {
		const loading = document.getElementById('products-loading');
		if (loading) {
			loading.style.display = 'none';
		}
	}

	/**
	 * 顯示錯誤訊息
	 */
	showError(message) {
		if (this.malltagContainer) {
			this.malltagContainer.innerHTML = `
                <div class="malltag-error">
                    <i class="bi bi-exclamation-triangle mb-2"></i><br>
                    ${message}
                </div>
            `;
		}
	}

	/**
	 * 顯示商品錯誤訊息
	 */
	showProductsError(message) {
		const errorDiv = document.getElementById('products-error');
		if (errorDiv) {
			errorDiv.textContent = message;
			errorDiv.style.display = 'block';
		}
		this.hideAllProductMessages();
	}

	/**
	 * 顯示無結果
	 */
	showNoResults() {
		const noResults = document.getElementById('no-results');
		if (noResults) {
			noResults.style.display = 'block';
		}
	}

	/**
	 * 隱藏所有商品訊息
	 */
	hideAllProductMessages() {
		const elements = ['products-error', 'no-results'];
		elements.forEach(id => {
			const element = document.getElementById(id);
			if (element) {
				element.style.display = 'none';
			}
		});

		if (this.productsContainer) {
			this.productsContainer.innerHTML = '';
		}
	}

	/**
	 * 檢查是否已初始化
	 */
	isReady() {
		return this.isInitialized;
	}
}

// 全域函數 - 處理商品點擊（與 HTML 中的 handleProductClick 保持一致）
window.handleProductClick = function(productId) {
	console.log('跳轉到商品詳情頁面，ID:', productId);
	window.location.href = `/front-end/shopsys/product.html?id=${productId}`;
};

// 臨時的購物車功能（先顯示提示，後續可以擴展）
function addToCart(productId) {
	console.log('加入購物車:', productId);

	// 顯示提示訊息
	const toast = document.createElement('div');
	toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: var(--green-color);
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        z-index: 9999;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideIn 0.3s ease;
    `;
	toast.textContent = '商品已加入購物車！';

	// 添加滑入動畫
	const keyframes = `
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
    `;

	if (!document.querySelector('#toast-styles')) {
		const style = document.createElement('style');
		style.id = 'toast-styles';
		style.textContent = keyframes;
		document.head.appendChild(style);
	}

	document.body.appendChild(toast);

	// 3秒後自動移除提示
	setTimeout(() => {
		toast.style.animation = 'slideIn 0.3s ease reverse';
		setTimeout(() => {
			if (toast.parentNode) {
				toast.parentNode.removeChild(toast);
			}
		}, 300);
	}, 3000);
}

// 導出到全域範圍供其他腳本使用
window.MallTagManager = MallTagManager;

// 創建實例但不自動初始化（由 HTML 控制）
window.mallTagManager = new MallTagManager();