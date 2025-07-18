document.addEventListener('DOMContentLoaded', () => {
  const menuButtons = document.querySelectorAll('#menu button');

  
  // 修正：使用完整的絕對路徑，與 shopindex.html 保持一致
  	const pageMap = {
	order: `/front-end/shopsys/order/order-list.html`,
		coupon: `/front-end/shopsys/coupon.html`,
	    product: `/front-end/shopsys/product.html`,
	    favorite: '/front-end/shopsys/favorite.html',
	    cart: `/front-end/shopsys/cart.html`,
	    shop: `/front-end/shopsys/shopindex.html`  // 新增：返回商城首頁
  	};

  	menuButtons.forEach(btn => {
    	btn.addEventListener('click', () => {
      		const key = btn.dataset.link;
			
      	if (pageMap[key]) {
			console.log(`導航到：${pageMap[key]}`);
			window.location.href = pageMap[key];
      	} else {
        	console.warn(`無對應頁面：${key}`);
			showNavigationError(key);
      }
    });
  });
  
  
  // 顯示導航錯誤提示
  function showNavigationError(key) {
          // 創建簡單的錯誤提示
          const toast = document.createElement('div');
          toast.style.cssText = `
              position: fixed;
              top: 20px;
              right: 20px;
              background: #dc3545;
              color: white;
              padding: 12px 20px;
              border-radius: 8px;
              z-index: 9999;
              font-weight: 500;
              box-shadow: 0 4px 12px rgba(0,0,0,0.15);
          `;
          toast.textContent = `頁面 "${key}" 尚未建立或路徑錯誤`;

          document.body.appendChild(toast);

          // 3秒後自動移除
          setTimeout(() => {
              if (toast.parentNode) {
                  toast.parentNode.removeChild(toast);
              }
          }, 3000);
      }
	  
	  // 高亮當前頁面對應的按鈕
	  function highlightCurrentPage() {
	          const currentPath = window.location.pathname;
	          
	          // 根據當前路徑判斷應該高亮哪個按鈕
	          let currentKey = null;
	          
	          if (currentPath.includes('/order/')) {
	              currentKey = 'order';
	          } else if (currentPath.includes('/coupon')) {
	              currentKey = 'coupon';
	          } else if (currentPath.includes('/product')) {
	              currentKey = 'product';
	          } else if (currentPath.includes('/favorite')) {
	              currentKey = 'favorite';
	          } else if (currentPath.includes('/cart')) {
	              currentKey = 'cart';
	          } else if (currentPath.includes('/shopindex')) {
	              currentKey = 'shop';
	          }

	          // 高亮對應按鈕
	          if (currentKey) {
	              const targetButton = document.querySelector(`#menu button[data-link="${currentKey}"]`);
	              if (targetButton) {
	                  targetButton.classList.add('active');
	              }
	          }
	      }

	      // 初始化：高亮當前頁面
	      highlightCurrentPage();

	      console.log('選單導航系統初始化完成');
	  });
  
  
  

