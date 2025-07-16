/**
 * productList.js - 商城首頁商品列表管理器
 * 專門在商城首頁右側動態顯示商品列表
 */

class ProductListManager {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api';
        this.currentContainer = null;
        this.productsData = [];
        this.currentPage = 1;
        this.itemsPerPage = 12;
        this.isLoading = false;
    }

    /**
     * 初始化並顯示商品列表
     */
    async init(container) {
        this.currentContainer = container;
        this.injectStyles();
        await this.loadAndDisplayProducts();
    }

    /**
     * 載入並顯示商品 - 加強除錯
     */
    async loadAndDisplayProducts() {
        try {
            this.showLoading();
            
            // 直接呼叫 API 獲取商品資料
            const response = await fetch(`${this.apiBaseUrl}/product/searchall?proIsMarket=0`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const productsData = await response.json();
            console.log('載入商品資料:', productsData);
            
            // 加強除錯：看第一個商品的完整結構
            if (productsData && productsData.length > 0) {
                console.log('第一個商品的完整資料結構:', productsData[0]);
                console.log('所有欄位名稱:', Object.keys(productsData[0]));
            }
            
            if (Array.isArray(productsData)) {
                this.productsData = productsData;
                this.displayProductGrid();
            } else {
                throw new Error('商品資料格式錯誤');
            }
            
        } catch (error) {
            console.error('載入商品失敗:', error);
            this.showError('載入商品列表失敗，請檢查後端 API 是否正常運行');
        }
    }

    /**
     * 生成測試資料
     */
    generateTestData() {
        return [
            {
                proNo: 1,
                proName: "薩爾達傳說：王國之淚",
                proPrice: 1690,
                proStatus: "已發售",
                proVersion: "標準版",
                proPlatform: "Nintendo Switch",
                proCover: true
            },
            {
                proNo: 2,
                proName: "巫師3：狂獵 完全版",
                proPrice: 599,
                proStatus: "已發售",
                proVersion: "完全版",
                proPlatform: "Steam",
                proCover: true
            },
            {
                proNo: 3,
                proName: "艾爾登法環",
                proPrice: 1490,
                proStatus: "已發售",
                proVersion: "標準版",
                proPlatform: "PlayStation 5",
                proCover: true
            },
            {
                proNo: 4,
                proName: "賽博朋克 2077",
                proPrice: 990,
                proStatus: "已發售",
                proVersion: "終極版",
                proPlatform: "Xbox Series X",
                proCover: true
            },
            {
                proNo: 5,
                proName: "原神",
                proPrice: 0,
                proStatus: "免費遊玩",
                proVersion: "基礎版",
                proPlatform: "多平台",
                proCover: true
            },
            {
                proNo: 6,
                proName: "最後生還者 第二部",
                proPrice: 1290,
                proStatus: "已發售",
                proVersion: "重製版",
                proPlatform: "PlayStation 5",
                proCover: true
            }
        ];
    }

    /**
     * 顯示載入狀態
     */
    showLoading() {
        if (this.currentContainer) {
            this.currentContainer.innerHTML = `
                <div class="loading-container">
                    <div class="loading-content">
                        <div class="spinner-border text-primary" role="status"></div>
                        <h4 class="mt-3">載入商品中...</h4>
                        <p class="text-muted">請稍候</p>
                    </div>
                </div>
            `;
        }
    }

    /**
     * 顯示商品網格 - 使用 Bootstrap 類別
     */
    displayProductGrid() {
        console.log('開始顯示商品網格...');
        
        if (!this.currentContainer) {
            console.error('currentContainer 不存在');
            return;
        }

        const startIndex = (this.currentPage - 1) * this.itemsPerPage;
        const endIndex = startIndex + this.itemsPerPage;
        const currentProducts = this.productsData.slice(startIndex, endIndex);
        const totalPages = Math.ceil(this.productsData.length / this.itemsPerPage);

        try {
            const gridHtml = `
                <!-- 使用 Bootstrap 卡片樣式的標題 -->
                <div class="card mb-4 border-0 shadow-sm">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <h3 class="card-title mb-0 fw-bold text-success">
                            <i class="bi bi-shop me-2"></i>商品列表
                            <span class="badge bg-light text-dark ms-2">${this.productsData.length} 項商品</span>
                        </h3>
                        <div class="btn-group">
                            <button class="btn btn-outline-primary btn-sm" onclick="window.productListManager.refreshProducts()">
                                <i class="bi bi-arrow-clockwise me-1"></i>重新載入
                            </button>
                            <a href="/product.html" class="btn btn-primary btn-sm">
                                <i class="bi bi-grid-3x3-gap me-1"></i>完整頁面
                            </a>
                        </div>
                    </div>
                </div>

                <!-- 商品網格，使用 Bootstrap -->
                <div class="products-grid">
                    ${currentProducts.map(product => this.createProductCard(product)).join('')}
                </div>

                ${totalPages > 1 ? this.createPagination(totalPages) : ''}
            `;

            this.currentContainer.innerHTML = gridHtml;
            console.log('商品網格顯示完成');
            
        } catch (error) {
            console.error('顯示商品網格時發生錯誤:', error);
            this.currentContainer.innerHTML = `
                <div class="alert alert-danger">
                    顯示商品時發生錯誤: ${error.message}
                </div>
            `;
        }
    }

    /**
     * 建立商品卡片 - 修正狀態邏輯
     */
    createProductCard(product) {
        console.log('=== 商品卡片除錯資訊 ===');
        console.log('完整商品物件:', product);
        console.log('所有欄位名稱:', Object.keys(product));
        
        try {
            // 根據您的 Entity 結構調整欄位
            const productName = product.proName || '未知商品';
            const productId = product.id || product.proNo;
            const productPrice = product.proPrice || 0;
            
            // 狀態判斷邏輯 - 正確版本
            const proIsmarket = product.proIsmarket; // Character: '0'=上架, '1'=下架
            const proStatus = product.proStatus; // String: 已發售/預購中
            
            console.log('proIsmarket 值:', proIsmarket, '(0=上架, 1=下架)');
            console.log('proStatus 值:', proStatus);
            
            // 修正的狀態顯示邏輯
            let displayStatus;
            let statusType; // 用於決定顏色
            
            if (proIsmarket === '0' || proIsmarket === 0) {
                // 上架 = 已發售
                displayStatus = proStatus || '已發售'; // 優先使用 proStatus，沒有的話預設為"已發售"
                statusType = 'available';
            } else if (proIsmarket === '1' || proIsmarket === 1) {
                // 下架 = 預購中
                displayStatus = proStatus || '預購中'; // 優先使用 proStatus，沒有的話預設為"預購中"
                statusType = 'preorder';
            } else {
                displayStatus = '狀態未知';
                statusType = 'unknown';
            }
            
            const productVersion = product.proVersion || '';
            const productPlatform = product.proPlatform || '';
            
            const price = productPrice ? this.formatPrice(productPrice) : '價格洽詢';
            
            // 使用您後端的圖片 API
            const imageSrc = `${this.apiBaseUrl}/product/cover/${productId}`;
            console.log('圖片路徑:', imageSrc);
            
            const safeProductName = this.escapeHtml(productName);
            
            // 使用 Bootstrap 的狀態樣式
            const statusClass = this.getBootstrapStatusClass(statusType);
            console.log('最終狀態:', displayStatus, '類型:', statusType, '→ CSS類別:', statusClass);
            
            return `
                <div class="product-card card h-100 border-0 shadow-sm position-relative" style="cursor: pointer; transition: transform 0.3s ease, box-shadow 0.3s ease;" onclick="window.productListManager.viewProductDetail(${productId})" onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 8px 24px rgba(0,0,0,0.15)'" onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='0 2px 8px rgba(0,0,0,0.1)'">
                    <div class="product-image-wrapper position-relative overflow-hidden" style="height: 200px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center;">
                        <img src="${imageSrc}" alt="${safeProductName}" class="product-image" style="width: 90%; height: 90%; object-fit: cover; border-radius: 8px; transition: transform 0.3s ease;" onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'" onerror="console.log('圖片載入失敗:', '${imageSrc}'); this.style.display='none'; this.parentElement.innerHTML='<div class=\\'text-white text-center\\' style=\\'display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column;\\'><i class=\\'bi bi-image display-4\\' style=\\'opacity: 0.7;\\'></i><small style=\\'opacity: 0.8;\\'>${safeProductName}</small></div>'">
                        
                        <!-- Hover 覆蓋層 -->
                        <div class="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center product-overlay" style="background: rgba(0,0,0,0.6); opacity: 0; transition: opacity 0.3s;">
                            <span class="text-white fw-bold px-3 py-2 rounded-pill" style="background: rgba(255,255,255,0.2); backdrop-filter: blur(10px);">查看詳情</span>
                        </div>
                        
                        <!-- 平台標籤 -->
                        ${productPlatform ? `<span class="position-absolute top-0 start-0 m-2 badge text-uppercase" style="background: rgba(0,0,0,0.8); font-size: 0.7rem; letter-spacing: 0.5px;">${this.escapeHtml(productPlatform)}</span>` : ''}
                        
                        <!-- 快速操作按鈕 -->
                        <div class="position-absolute top-0 end-0 m-2 d-flex gap-1 product-actions" style="opacity: 0; transition: opacity 0.3s;">
                            <button class="btn btn-sm btn-light rounded-circle" style="width: 36px; height: 36px; backdrop-filter: blur(10px);" onclick="event.stopPropagation(); window.productListManager.addToCart(${productId})" title="加入購物車">
                                <i class="bi bi-cart-plus"></i>
                            </button>
                            <button class="btn btn-sm btn-primary rounded-circle" style="width: 36px; height: 36px;" onclick="event.stopPropagation(); window.productListManager.viewProductDetail(${productId})" title="查看詳情">
                                <i class="bi bi-eye"></i>
                            </button>
                        </div>
                    </div>
                    
                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title fw-bold mb-2 flex-grow-1" style="line-height: 1.4; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;" title="${safeProductName}">${safeProductName}</h5>
                        ${productVersion ? `<p class="card-text text-muted small mb-2 fst-italic">${this.escapeHtml(productVersion)}</p>` : ''}
                        <div class="mt-auto">
                            <p class="card-text mb-2">
                                <span class="h5 text-success fw-bold">NT$ ${price}</span>
                            </p>
                            <span class="badge ${statusClass} text-uppercase" style="font-size: 0.75rem; letter-spacing: 0.5px;">${this.escapeHtml(displayStatus)}</span>
                        </div>
                    </div>
                </div>
            `;
        } catch (error) {
            console.error('建立商品卡片失敗:', error, product);
            return `<div class="alert alert-warning">商品卡片建立失敗: ${product.proName || '未知商品'}</div>`;
        }
    }

    /**
     * 取得 Bootstrap 狀態樣式類別 - 根據狀態類型
     */
    getBootstrapStatusClass(statusType) {
        switch (statusType) {
            case 'available':  // 已發售
                return 'bg-success text-white';
            case 'preorder':   // 預購中
                return 'bg-warning text-dark';
            case 'offline':    // 已下架
                return 'bg-danger text-white';
            case 'unknown':    // 狀態未知
            default:
                return 'bg-secondary text-white';
        }
    }

    /**
     * 建立分頁
     */
    createPagination(totalPages) {
        let paginationHtml = '<div class="pagination-container"><nav><ul class="pagination">';
        
        // 上一頁
        if (this.currentPage > 1) {
            paginationHtml += `
                <li class="page-item">
                    <button class="page-link" onclick="window.productListManager.goToPage(${this.currentPage - 1})">
                        <i class="bi bi-chevron-left"></i>
                    </button>
                </li>
            `;
        }
        
        // 頁碼
        for (let i = 1; i <= totalPages; i++) {
            if (i === this.currentPage) {
                paginationHtml += `<li class="page-item active"><span class="page-link">${i}</span></li>`;
            } else {
                paginationHtml += `
                    <li class="page-item">
                        <button class="page-link" onclick="window.productListManager.goToPage(${i})">${i}</button>
                    </li>
                `;
            }
        }
        
        // 下一頁
        if (this.currentPage < totalPages) {
            paginationHtml += `
                <li class="page-item">
                    <button class="page-link" onclick="window.productListManager.goToPage(${this.currentPage + 1})">
                        <i class="bi bi-chevron-right"></i>
                    </button>
                </li>
            `;
        }
        
        paginationHtml += '</ul></nav></div>';
        return paginationHtml;
    }

    /**
     * 跳轉到指定頁面
     */
    goToPage(page) {
        this.currentPage = page;
        this.displayProductGrid();
    }

    /**
     * 重新載入商品
     */
    async refreshProducts() {
        await this.loadAndDisplayProducts();
        this.showToast('商品列表已更新！', 'success');
    }

    /**
     * 查看商品詳情 - 使用 PRO_NO
     */
    viewProductDetail(productId) {
        console.log('查看商品詳情:', productId);
        // 跳轉到商品詳情頁面，使用 PRO_NO
        window.location.href = `/product.html?id=${productId}`;
    }

    /**
     * 加入購物車 - 使用 PRO_NO
     */
    addToCart(productId) {
        console.log('加入購物車:', productId);
        this.showToast('商品已加入購物車！', 'success');
    }

    /**
     * 快速搜尋功能 - 修正版
     */
    quickSearch(searchTerm) {
        if (!this.productsData || this.productsData.length === 0) {
            return; // 如果沒有商品資料，直接返回
        }

        if (!searchTerm || searchTerm.trim() === '') {
            // 如果搜尋欄為空，顯示所有商品
            this.currentPage = 1;
            this.displayProductGrid();
            return;
        }

        // 過濾商品
        const filteredProducts = this.productsData.filter(product => {
            const name = (product.proName || '').toLowerCase();
            const platform = (product.proPlatform || '').toLowerCase();
            const version = (product.proVersion || '').toLowerCase();
            const search = searchTerm.toLowerCase();
            
            return name.includes(search) || 
                   platform.includes(search) || 
                   version.includes(search);
        });

        // 建立臨時顯示的搜尋結果
        this.displaySearchResults(filteredProducts, searchTerm);
    }

    /**
     * 顯示搜尋結果
     */
    displaySearchResults(filteredProducts, searchTerm) {
        if (!this.currentContainer) return;

        const resultHtml = `
            <div class="product-list-header">
                <h3 class="list-title">
                    <i class="bi bi-search me-2"></i>搜尋結果
                    <span class="product-count">(找到 ${filteredProducts.length} 項商品)</span>
                </h3>
                <div class="list-actions">
                    <button class="btn btn-outline-secondary btn-sm" onclick="document.getElementById('quick-search').value=''; window.productListManager.quickSearch('')">
                        <i class="bi bi-x-circle me-1"></i>清除搜尋
                    </button>
                    <a href="/product.html" class="btn btn-primary btn-sm">
                        <i class="bi bi-grid-3x3-gap me-1"></i>完整頁面
                    </a>
                </div>
            </div>

            ${filteredProducts.length > 0 ? `
                <div class="products-grid">
                    ${filteredProducts.map(product => this.createProductCard(product)).join('')}
                </div>
            ` : `
                <div class="no-results">
                    <div class="text-center p-5">
                        <i class="bi bi-search fs-1 text-muted"></i>
                        <h4 class="mt-3">找不到相關商品</h4>
                        <p class="text-muted">請嘗試其他關鍵字：「${searchTerm}」</p>
                        <button class="btn btn-outline-primary" onclick="document.getElementById('quick-search').value=''; window.productListManager.quickSearch('')">
                            <i class="bi bi-arrow-clockwise me-1"></i>重新搜尋
                        </button>
                    </div>
                </div>
            `}
        `;

        this.currentContainer.innerHTML = resultHtml;
    }

    /**
     * 顯示錯誤 - 移除測試資料按鈕
     */
    showError(message) {
        if (this.currentContainer) {
            this.currentContainer.innerHTML = `
                <div class="error-container">
                    <div class="error-content text-center p-4">
                        <i class="bi bi-exclamation-triangle display-1 text-danger"></i>
                        <h3 class="mt-3">載入失敗</h3>
                        <p class="text-muted">${message}</p>
                        <div class="mt-3">
                            <p class="small text-info">
                                <i class="bi bi-info-circle me-1"></i>
                                請確認後端服務是否在 <code>http://localhost:8080</code> 運行
                            </p>
                        </div>
                        <button class="btn btn-primary" onclick="window.productListManager.refreshProducts()">
                            <i class="bi bi-arrow-clockwise"></i> 重試
                        </button>
                    </div>
                </div>
            `;
        }
    }

    /**
     * 載入測試資料 - 修正版
     */
    loadTestData() {
        console.log('開始載入測試資料...');
        
        try {
            this.showLoading();
            
            // 立即載入測試資料，不用 setTimeout
            this.productsData = this.generateTestData();
            console.log('測試資料已生成:', this.productsData);
            
            // 立即顯示
            this.displayProductGrid();
            this.showToast('已載入測試資料！', 'info');
            
            console.log('測試資料載入完成');
            
        } catch (error) {
            console.error('載入測試資料失敗:', error);
            this.showError('載入測試資料失敗');
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
                bgColor = '#28a745';
                break;
            case 'error':
                bgColor = '#dc3545';
                break;
            case 'info':
                bgColor = '#007bff';
                break;
            default:
                bgColor = '#28a745';
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
     * 格式化價格
     */
    formatPrice(price) {
        return new Intl.NumberFormat('zh-TW').format(price);
    }

    /**
     * 取得狀態樣式類別
     */
    getStatusClass(status) {
        if (!status) return 'unknown';
        
        const statusLower = status.toLowerCase();
        if (statusLower.includes('已發售') || statusLower.includes('available')) {
            return 'available';
        } else if (statusLower.includes('預購') || statusLower.includes('preorder')) {
            return 'preorder';
        } else if (statusLower.includes('缺貨') || statusLower.includes('sold')) {
            return 'soldout';
        }
        return 'unknown';
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
     * 生成佔位圖片 - 移除 btoa 問題
     */
    generatePlaceholderImage() {
        // 使用線上佔位圖片服務，避免 btoa 編碼問題
        return 'https://via.placeholder.com/300x200/667eea/ffffff?text=Loading...';
    }

    /**
     * 注入樣式
     */
    injectStyles() {
        const styleId = 'product-list-styles';
        if (document.getElementById(styleId)) return;

        const style = document.createElement('style');
        style.id = styleId;
        style.textContent = `
            /* 商品列表頁面樣式 - 美化標題 */
            .product-list-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 2rem;
                padding: 1.5rem 1rem;
                background: var(--theme-white);
                border-radius: 16px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.08);
                border: 1px solid #e5e7eb;
            }

            .list-title {
                color: var(--text-color);
                font-size: 1.6rem;
                font-weight: 800;
                margin: 0;
                display: flex;
                align-items: center;
                gap: 0.5rem;
            }

            .list-title i {
                color: #059669;
            }

            .product-count {
                color: #6b7280;
                font-size: 0.9rem;
                font-weight: 500;
                margin-left: 0.5rem;
                padding: 0.25rem 0.75rem;
                background: #f3f4f6;
                border-radius: 20px;
            }

            .list-actions {
                display: flex;
                gap: 0.5rem;
            }

            /* 商品網格 - 調整間距 */
            .products-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                gap: 1.5rem;
                margin-bottom: 2rem;
                padding: 0.5rem;
            }

            /* 商品卡片 - 改成類似參考圖片的樣式 */
            .product-card {
                background: var(--theme-white);
                border-radius: 16px;
                overflow: hidden;
                box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                transition: all 0.3s ease;
                cursor: pointer;
                position: relative;
                border: 1px solid #e5e7eb;
            }

            .product-card:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 24px rgba(0,0,0,0.15);
                border-color: #d1d5db;
            }

            .product-image-wrapper {
                position: relative;
                height: 200px;
                overflow: hidden;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .product-image {
                width: 80%;
                height: 80%;
                object-fit: cover;
                border-radius: 8px;
                transition: transform 0.3s ease;
            }

            .product-card:hover .product-image {
                transform: scale(1.05);
            }

            .product-overlay {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0,0,0,0.6);
                display: flex;
                align-items: center;
                justify-content: center;
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .product-card:hover .product-overlay {
                opacity: 1;
            }

            .view-detail {
                color: white;
                font-weight: 600;
                font-size: 1rem;
                padding: 8px 16px;
                background: rgba(255,255,255,0.2);
                border-radius: 20px;
                backdrop-filter: blur(10px);
            }

            .product-info {
                padding: 1.25rem;
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }

            .product-name {
                font-size: 1.1rem;
                font-weight: 700;
                color: var(--text-color);
                margin: 0;
                height: auto;
                line-height: 1.4;
                overflow: hidden;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
            }

            .product-price {
                font-size: 1.3rem;
                font-weight: 800;
                color: #059669;
                margin: 0.25rem 0;
            }

            .product-status {
                font-size: 0.8rem;
                font-weight: 600;
                padding: 0.3rem 0.8rem;
                border-radius: 20px;
                display: inline-block;
                width: fit-content;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }

            .product-status.available {
                background: #dcfce7;
                color: #166534;
            }

            .product-status.preorder {
                background: #fef3c7;
                color: #92400e;
            }

            .product-status.soldout {
                background: #fee2e2;
                color: #991b1b;
            }

            .product-status.unknown {
                background: #f3f4f6;
                color: #6b7280;
            }

            .product-platform {
                position: absolute;
                top: 0.75rem;
                left: 0.75rem;
                background: rgba(0,0,0,0.8);
                color: white;
                font-size: 0.7rem;
                font-weight: 600;
                padding: 0.3rem 0.6rem;
                border-radius: 12px;
                backdrop-filter: blur(10px);
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }

            .product-version {
                font-size: 0.85rem;
                color: #6b7280;
                font-weight: 500;
                margin: 0;
                font-style: normal;
            }

            .product-actions {
                position: absolute;
                top: 0.75rem;
                right: 0.75rem;
                display: flex;
                gap: 0.5rem;
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .product-card:hover .product-actions {
                opacity: 1;
            }

            .btn-quick-add,
            .btn-quick-view {
                width: 36px;
                height: 36px;
                border: none;
                border-radius: 50%;
                background: rgba(255,255,255,0.95);
                color: #374151;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 1rem;
                box-shadow: 0 2px 8px rgba(0,0,0,0.15);
                backdrop-filter: blur(10px);
            }

            .btn-quick-add:hover {
                background: #059669;
                color: white;
                transform: scale(1.1);
                box-shadow: 0 4px 12px rgba(5, 150, 105, 0.4);
            }

            .btn-quick-view:hover {
                background: #3b82f6;
                color: white;
                transform: scale(1.1);
                box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
            }

            /* 分頁 */
            .pagination-container {
                display: flex;
                justify-content: center;
                margin-top: 2rem;
            }

            .pagination .page-link {
                color: var(--text-color);
                border-color: var(--border-color);
                background-color: var(--theme-white);
            }

            .pagination .page-link:hover {
                background-color: var(--bg-icons-hover);
                border-color: var(--border-color);
            }

            .pagination .page-item.active .page-link {
                background-color: var(--green-color);
                border-color: var(--green-color);
                color: white;
            }

            .no-results {
                min-height: 300px;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .product-version {
                font-size: 0.85rem;
                color: #6c757d;
                margin-bottom: 0.5rem;
                font-style: italic;
            }

            .product-platform {
                position: absolute;
                top: 0.5rem;
                left: 0.5rem;
                background: rgba(0,0,0,0.7);
                color: white;
                font-size: 0.75rem;
                padding: 0.25rem 0.5rem;
                border-radius: 4px;
                font-weight: 500;
            }

            /* 載入和錯誤狀態 */
            .loading-container,
            .error-container {
                min-height: 400px;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .loading-content,
            .error-content {
                text-align: center;
                padding: 2rem;
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
                .products-grid {
                    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                    gap: 1rem;
                }

                .product-list-header {
                    flex-direction: column;
                    gap: 1rem;
                    align-items: stretch;
                }

                .list-actions {
                    justify-content: center;
                }
            }

            @media (max-width: 576px) {
                .products-grid {
                    grid-template-columns: 1fr;
                }
            }
        `;

        document.head.appendChild(style);
    }
}

// 創建全域實例和函式
if (!window.productListManager) {
    window.ProductListManager = ProductListManager;
    window.productListManager = new ProductListManager();
}

// 全域函式供 HTML 呼叫
window.showProductList = function(container) {
    if (window.productListManager) {
        window.productListManager.init(container);
    } else {
        console.error('ProductListManager 未初始化');
    }
};