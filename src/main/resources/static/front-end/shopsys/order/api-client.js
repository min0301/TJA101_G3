// api-client.js - 訂單功能專用的 API 客戶端工具
// 基於 allForum.js 的模式設計

(function() {
    'use strict';
	

    // *** 訂單 API 客戶端 - 使用 IIFE 避免污染全域範疇 *** //
    window.OrderApiClient = {

        // ========== 工具方法 ========== //
		
        // *** 取得儲存在 localStorage 的 JWT Token *** //
        getToken() {
            return localStorage.getItem('jwt');
        },

		
        // *** 取得會員資訊 *** //
        getMemberInfo() {
            const rawMemberInfo = localStorage.getItem('memberInfo');
            return JSON.parse(rawMemberInfo || '{}');
        },

		
        // *** 取得會員編號 (從 JWT Token 中解析) *** //
        getMemberNo() {
            const token = this.getToken();
            if (!token) return null;

            try {
                // 解析 JWT Token 取得 memId
                const payload = JSON.parse(atob(token.split('.')[1]));
                return payload.memId || null;
            } catch (error) {
                console.error('解析 Token 失敗:', error);
                return null;
            }
        },

        // *** 檢查是否已登入 *** //
        isLoggedIn() {
            const token = this.getToken();
            const memNo = this.getMemberNo();
            return !!(token && memNo);
        },

        // *** 建立請求 Headers *** //
        createHeaders(includeAuth = true) {
            const headers = {
                'Content-Type': 'application/json'
            };

            if (includeAuth) {
                const token = this.getToken();
                if (token) {
                    headers['Authorization'] = `Bearer ${token}`;
                }
            }

            return headers;
        },

		
        // *** 基礎 API 請求方法 ***//
		async request(url, options = {}) {
		            try {
		                // 確保使用相對路徑，這樣在任何環境都能正常工作
		                let requestUrl = url;
		                
		                // 如果 URL 以 / 開頭，保持原樣（相對於根目錄）
		                // 如果專案部署在子路徑，可能需要額外處理
		                console.log('Debug - API 請求路徑:', requestUrl);
		                
		                const defaultOptions = {
		                    headers: this.createHeaders()
		                };

		                const finalOptions = {
		                    ...defaultOptions,
		                    ...options,
		                    headers: {
		                        ...defaultOptions.headers,
		                        ...options.headers
		                    }
		                };

		                const response = await fetch(requestUrl, finalOptions);
		                
		                console.log('Debug - API 回應狀態:', response.status, response.statusText);

		                // 處理認證失敗
		                if (response.status === 401 || response.status === 403) {
		                    throw new Error('您的登入已過期或無效，請重新登入');
		                }

		                if (!response.ok) {
		                    // 嘗試取得錯誤訊息
		                    let errorMessage = `請求失敗 (${response.status})`;
		                    try {
		                        const errorData = await response.json();
		                        errorMessage = errorData.errorMessage || errorData.message || errorMessage;
		                    } catch (e) {
		                        errorMessage = `HTTP ${response.status}: ${response.statusText}`;
		                    }
		                    throw new Error(errorMessage);
		                }

		                const responseData = await response.json();
		                console.log('Debug - API 回應資料:', responseData);
		                return responseData;
		                
		            } catch (error) {
		                console.error('API 請求失敗:', error);
		                throw error;
		            }
		        },

		
		
        // ========== 訂單相關 API ========== //

        // *** 取得會員的所有訂單列表 *** //
        async getOrderList() {
            if (!this.isLoggedIn()) {
                throw new Error('請先登入才能查看訂單');
            }

            const memNo = this.getMemberNo();
            return await this.request(`/api/member/${memNo}`);
        },

		
        // *** 取得單一訂單詳情 (包含訂單明細) *** //
		async getOrderDetail(orderNo) {
			if (!this.isLoggedIn()) {
		    	throw new Error('請先登入才能查看訂單詳情');
		    }

		    console.log('Debug - 開始載入訂單詳情:', orderNo);

		    // 只使用你實際存在的 API
		    const orderItems = await this.request(`/api/orderitem/order/${orderNo}`);
		            
		    console.log('Debug - 取得商品明細:', orderItems);

		    // 從商品明細中構建訂單基本資訊
		    let orderInfo = {
		    	orderNo: orderNo,
		        orderStatus: '已完成', // 暫時固定為已完成以測試評價功能
		        orderDatetime: '2025-07-11T11:45:00',
		        orderTotal: 0,
		        contactEmail: 'v4w5x6y@TJA101.com.tw',
		        contactPhone: '未提供'
		    };

		   // 如果有商品項目，計算總金額並取得訂單相關資訊
		   if (orderItems && orderItems.length > 0) {
		   		// 計算總金額
		        orderInfo.orderTotal = orderItems.reduce((total, item) => {
		        	return total + (item.proPrice * item.orderAmount);
		        }, 0);
		                
		        // 如果商品項目中有訂單相關資訊，使用它們
		        const firstItem = orderItems[0];
		        if (firstItem.orderDate) {
		        	orderInfo.orderDatetime = firstItem.orderDate;
		        }
		        if (firstItem.orderStatus) {
		        	orderInfo.orderStatus = firstItem.orderStatus;
		        }
		        // 如果有其他訂單相關欄位，也可以在這裡取得
		        if (firstItem.contactEmail) {
		        	orderInfo.contactEmail = firstItem.contactEmail;
		        }
		        if (firstItem.contactPhone) {
		        	orderInfo.contactPhone = firstItem.contactPhone;
		        }
		    }

		    console.log('Debug - 構建的訂單資訊:', orderInfo);

		    return {
		    	orderInfo,
		        orderItems
		    };
		},
				

		
        // *** 取得訂單明細列表 (根據訂單編號) *** //
        async getOrderItems(orderNo) {
            return await this.request(`/api/orderitem/order/${orderNo}`);
        },

		
        // *** 取得單一訂單明細 *** //
        async getOrderItem(orderItemNo) {
            return await this.request(`/api/orderitem/${orderItemNo}`);
        },

		
		
        // ========== 評價相關 API ========== //

        // *** 新增商品評價 *** //
        async addComment(orderItemNo, commentData) {
            if (!this.isLoggedIn()) {
                throw new Error('請先登入才能評價商品');
            }

            return await this.request(`/api/orderitem/${orderItemNo}/comment`, {
                method: 'POST',
                body: JSON.stringify(commentData)
            });
        },

        // *** 修改商品評價 *** //
        async updateComment(orderItemNo, commentData) {
            if (!this.isLoggedIn()) {
                throw new Error('請先登入才能修改評價');
            }

            return await this.request(`/api/orderitem/${orderItemNo}/comment`, {
                method: 'PUT',
                body: JSON.stringify(commentData)
            });
        },

        // *** 取得會員在特定訂單中的評價記錄 *** //
        async getMemberCommentsByOrder(orderNo) {
            return await this.request(`/api/orderitem/order/${orderNo}/comments`);
        },

        // *** 取得產品的所有評價 (用於商品詳情頁面) *** //
        async getProductComments(proNo, page = 0, size = 10) {
            return await this.request(`/api/orderitem/product/${proNo}/comments/page?page=${page}&size=${size}`);
        },

        // *** 取得產品評價統計資訊 *** //
        async getProductCommentStatistics(proNo) {
            return await this.request(`/api/orderitem/product/${proNo}/statistics`);
        },

		
		
        // ========== 工具方法 ========== //

        // *** 格式化日期顯示 *** //
        formatDate(dateString) {
            if (!dateString) return '未知日期';
            
            try {
                return new Date(dateString).toLocaleString('zh-TW', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            } catch (error) {
                return '日期格式錯誤';
            }
        },
		

        // *** 格式化金額顯示 *** //
         formatPrice(amount) {
            if (typeof amount !== 'number') return '價格錯誤';
            return `NT$ ${amount.toLocaleString()}`;
        },

		
        // *** 取得訂單狀態文字 *** //
         getOrderStatusText(status) {
            const statusMap = {
				'PENDING': '等待付款',
				'PAYING': '付款處理中',
				'PROCESSING': '處理中',
				'SHIPPED': '已出貨',
				'COMPLETED': '已完成',
				'FAILED': '處理失敗',
				'CANCELLED': '已取消',
				
				// 新增中文狀態，目的是讓資料庫的假資料可以串接 <<不影響未來功能>>
				'處理中': '處理中',
				'已完成': '已完成',
				'已出貨': '已出貨',
				'等待付款': '等待付款',
				'已取消': '已取消'
				
            };
            return statusMap[status] || '未知狀態';
        },

		
        // *** 取得訂單狀態樣式類別 *** //
        getOrderStatusClass(status) {
            const classMap = {
				'PENDING': 'badge bg-warning text-dark',
				'PAYING': 'badge bg-primary',
				'PROCESSING': 'badge bg-info',
				'SHIPPED': 'badge bg-purple',
				'COMPLETED': 'badge bg-success',
				'FAILED': 'badge bg-danger',
				'CANCELLED': 'badge bg-secondary',
				
				// 新增中文狀態，目的是讓資料庫的假資料可以串接 <<不影響未來功能>>
				'處理中': '處理中',
				'已完成': '已完成',
				'已出貨': '已出貨',
				'等待付款': '等待付款',
				'已取消': '已取消'
				
            };
            return classMap[status] || 'badge bg-light text-dark';
        },
		
		
		// *** 取得訂單狀態對應的圖標 *** //
		getOrderStatusIcon(status) {
		    const iconMap = {
		        'PENDING': 'bi-clock',
		        'PAYING': 'bi-credit-card', 
		        'PROCESSING': 'bi-gear',
		        'SHIPPED': 'bi-truck',
		        'COMPLETED': 'bi-check-circle',
		        'FAILED': 'bi-exclamation-triangle',
		        'CANCELLED': 'bi-x-circle',
				
				// 中文狀態兼容  
				'等待付款': 'bi-clock',
				'付款處理中': 'bi-credit-card', 
				'處理中': 'bi-gear',
				'已出貨': 'bi-truck',
				'已完成': 'bi-check-circle',
				'處理失敗': 'bi-exclamation-triangle',
				'已取消': 'bi-x-circle'
				
				
		    };
		    return iconMap[status] || 'bi-question-circle';
		},

		
        // *** 顯示星級評分 *** //
		getStarDisplay(rating) {
		    if (!rating) return '<span class="text-muted">未評價</span>';
		    
		    let starsHtml = '';
		    for (let i = 1; i <= 5; i++) {
		        if (i <= rating) {
		            starsHtml += '<i class="bi bi-star-fill text-warning"></i>';
		        } else {
		            starsHtml += '<i class="bi bi-star text-muted"></i>';
		        }
		    }
		    return starsHtml;
		}
    };

})();