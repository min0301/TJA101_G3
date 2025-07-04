/**
 * script.js
 * 負責處理頁面動態內容載入
 */
document.addEventListener('DOMContentLoaded', function() {

    const mainContentArea = document.getElementById('main-content-area');
    const navLinks = document.querySelectorAll('.sidebar a');

    async function loadContent(pageName) {
        mainContentArea.innerHTML = '<p style="text-align:center; padding: 40px;">內容載入中...</p>';

        let apiUrl = '';
        switch (pageName) {
            case 'hot-discussions':
                apiUrl = '/api/discussions/hot';
                break;
            case 'hot-products':
                apiUrl = '/api/products/hot';
                break;
            case 'news':
                apiUrl = '/api/news/latest';
                break;
            default:
                mainContentArea.innerHTML = '<p>找不到對應的內容。</p>';
                return;
        }

        try {
            const response = await fetch(apiUrl);
            if (!response.ok) {
                throw new Error(`API 請求失敗，狀態碼: ${response.status}`);
            }
            const data = await response.json();
            const htmlToRender = generateHtmlForPage(pageName, data);
            mainContentArea.innerHTML = htmlToRender;
        } catch (error) {
            console.error('載入內容時發生錯誤:', error);
            mainContentArea.innerHTML = '<p style="color: red; text-align:center;">內容載入失敗，請檢查主控台(F12)錯誤訊息。</p>';
        }
    }

    function generateHtmlForPage(pageName, data) {
        let html = '';
        switch (pageName) {
            case 'hot-discussions':
                const discussionCards = data.map(item => `
                    <div class="forum-card">
                        <div class="thumb"></div>
                        <div class="forum-card-title">${item.title}</div>
                        <div class="forum-card-desc">${item.description}</div>
                    </div>
                `).join('');
                html = `<div class="forum-section"><div class="forum-category-title">熱門討論</div><div class="card-row">${discussionCards}</div></div>`;
                break;

            case 'hot-products':
                const productCards = data.map(product => `
                    <div class="forum-card">
                         <div class="thumb"></div>
                         <div class="forum-card-title">${product.name}</div>
                         <div class="forum-card-desc">價格: $${product.price.toFixed(2)}</div>
                    </div>
                `).join('');
                html = `<div class="forum-section"><div class="forum-category-title">熱門商品</div><div class="card-row">${productCards}</div></div>`;
                break;

            case 'news':
                const newsItems = data.map(news => `
                    <div class="forum-card" style="width: 100%;">
                         <div class="forum-card-title">${news.title}</div>
                         <div class="forum-card-desc">${news.summary}</div>
                    </div>
                 `).join('');
                html = `<div class="forum-section"><div class="forum-category-title">最新新聞</div><div class="card-row">${newsItems}</div></div>`;
                break;

            default:
                html = '<h3>未知內容類型</h3>';
        }
        return html;
    }

    navLinks.forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();
            document.querySelector('.sidebar li.active')?.classList.remove('active');
            this.closest('li').classList.add('active');
            const page = this.dataset.page;
            loadContent(page);
        });
    });

    loadContent('hot-discussions');
});