package com.pixeltribe.shopsys.cart.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.cart.exception.CartErrorCode;
import com.pixeltribe.shopsys.cart.exception.CartException;
import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;



@Service
@Transactional
public class CartService {
	
	@Autowired
	private CartRepository cartRepository;  // 注入 Redis Repository

	@Autowired    
	private ProductRepository productRepository;  
	
	@Autowired
	private MemRepository memRepository;
	
	@Autowired  
    private RedisTemplate<String, String> redisTemplate;
    
    // Redis Key 前綴常數
    private static final String PREORDER_STOCK_PREFIX = "preorder:stock:product:";
	
	// ============ 加入商品到購物車 ===========//
	public CartDTO addToCart(Integer memNo, Integer proNo, Integer proNum) {
		
		Product product = productRepository.findById(proNo).orElse(null);
        
        if (product == null) {
            throw new CartException(CartErrorCode.CART_006); // 商品不存在
        }
        
        boolean 已下架 = (product.getProIsmarket() == '1');
        if (已下架) {
        	throw new CartException(CartErrorCode.CART_002); // 商品不可購買
        }
        
        // ******* 檢查庫存 ************ //
        // 庫存檢查邏輯（軟性提醒）
        boolean hasStockIssue = false;
        String stockWarning = null;

        // 檢查庫存（不阻止加入，但提供警告）
        Integer availableStock = getProductStock(product);

        if (availableStock == 0) {
            hasStockIssue = true;
            stockWarning = getStockWarningMessage(product, proNum, availableStock);
        } else if (availableStock != Integer.MAX_VALUE && proNum > availableStock) {
            hasStockIssue = true;
            stockWarning = getStockWarningMessage(product, proNum, availableStock);
        } else if (availableStock <= 10 && !"預購".equals(product.getProStatus())) {
            hasStockIssue = true;
            stockWarning = getStockWarningMessage(product, proNum, availableStock);
        }
        

        
        
        // 使用資料庫裡的產品資訊
        String proName = product.getProName();
        Integer proPrice = product.getProPrice();
		
        
        // 取得現有購物車
        CartDTO cart = cartRepository.getCart(memNo);

		
		// 如果購物車不存在，就創建新的
		if (cart == null) {  
			cart = new CartDTO();
			cart.setMemNo(memNo);
			cart.setItem(new ArrayList<>());
		}
		
		// 檢查產品是否已存在
		CartDTO.CartItem existingItem = null;
		for (CartDTO.CartItem item : cart.getItem()) {
			if (item.getProNo().equals(proNo)) {
				existingItem = item;
				break;
			}
		}
		
		// 商品已存在，增加數量
		if (existingItem != null) {
			// 更新數量並重新檢查庫存
		    Integer newTotalQuantity = existingItem.getProNum() + proNum;
		    
		    // 重新檢查總數量的庫存狀況
		    if (availableStock != Integer.MAX_VALUE && newTotalQuantity > availableStock) {
		        hasStockIssue = true;
		        stockWarning = getStockWarningMessage(product, newTotalQuantity, availableStock);
		    }
			
			existingItem.setProNum(newTotalQuantity);
            existingItem.calculateTotal(); // 重新計算小計
            existingItem.setHasStockIssue(hasStockIssue);    
            existingItem.setStockWarning(stockWarning);
		} else {
			// 建立新商品項目
			CartDTO.CartItem newItem = new CartDTO.CartItem();
            newItem.setProNo(proNo);
            newItem.setProName(proName);
            newItem.setProPrice(proPrice);
            newItem.setProNum(proNum);
            newItem.setProStatus(product.getProStatus());  // 確認庫存部分
            newItem.setHasStockIssue(hasStockIssue);       // 確認庫存部分
            newItem.setStockWarning(stockWarning);         // 確認庫存部分
            newItem.calculateTotal(); // 計算小計
            cart.getItem().add(newItem);  //加入購物車
		}
		
		// 計算總價
		cart.calculateTotals();
		
		// 保存到Redis
		cartRepository.saveCart(memNo, cart);
		
		// 回傳到CartDTO
		return cart;  
	}
	
	
	// ============ 獲取會員購物車 ===========//
	public CartDTO getMemberCart(Integer memNo) {
		
		// 取得購物車
		CartDTO cart = cartRepository.getCart(memNo);
		
		if (cart == null) {
			// 購物車不存在，創建空的購物車
            cart = new CartDTO();
            cart.setMemNo(memNo);
            cart.setItem(new ArrayList<>());
            cart.calculateTotals();
        }	
		
		return cart;  // 回傳到CartDTO
	}
	
	
	// ============ 移除購物車的產品 ===========//
	public CartDTO removeFromCart(Integer memNo, Integer proNo) {
		
		// 取得購物車
		CartDTO cart = cartRepository.getCart(memNo);
        
        if (cart == null) {
            // 購物車不存在，創建空的購物車
            cart = new CartDTO();
            cart.setMemNo(memNo);
            cart.setItem(new ArrayList<>());
            cart.calculateTotals();
            return cart;
        }
        
        // 移除指定產品
        cart.getItem().removeIf(item -> item.getProNo().equals(proNo));
        
        // 重新計算總價
        cart.calculateTotals();
        
        // 存到Redis
        cartRepository.saveCart(memNo, cart);
        
        return cart;  // 回傳到CartDTO
	}
	
	
	// ============ 更新產品的數量 ===========//
	public CartDTO updateCartItemQuantity(Integer memNo, Integer proNo, Integer proNum) {

		
		// 取得購物車
		CartDTO cart = cartRepository.getCart(memNo);
		
		// 購物車不存在，創建空的購物車
		if (cart == null) {
		    cart = new CartDTO();
		    cart.setMemNo(memNo);
		    cart.setItem(new ArrayList<>());
		    cart.calculateTotals();
		    return cart;
		}
		
		// 找到指定商品並更新數量
		for (CartDTO.CartItem item : cart.getItem()) {
			if (item.getProNo().equals(proNo)) {
				item.setProNum(proNum);
                item.calculateTotal(); // 重新計算小計
                break;
			}
		}
		
		// 重新計算總價		
		cart.calculateTotals();
		
		// 存到Redis
		cartRepository.saveCart(memNo, cart);
		
		return cart;  // 回傳到CartDTO
	}
	
	
	// ============ 清空購物車 ===========//
	public void clearCart(Integer memNo) {
		
		// 直接從 Redis 刪除購物車
        cartRepository.deleteCart(memNo);
        
        System.out.println("已清空您的購物車");
		
	}
	
	// ******** 後台 ******** //
	// ============ 查詢所有購物車(後台) ===========//
	public AdminCartListResponse getAllCartsForAdmin(Integer page, Integer size, Integer memNo) {
		 try {
	            List<AdminCartDTO> carts = new ArrayList<>();
	            Integer totalCarts = 0;  // 先宣告總數變數
	            
	           // 如果指定會員編號，只查詢該會員
	            if (memNo != null) {
	                CartDTO cart = cartRepository.getCart(memNo);
	                if (cart != null) {
	                    AdminCartDTO adminCart = convertToAdminCartDTO(cart);
	                    carts.add(adminCart);
	                    totalCarts = 1;  // 指定會員時總數為1
	                } else {
	                    totalCarts = 0;  // 找不到時總數為0
	                }
	            } else {
	                // 先取得總購物車數量
	                totalCarts = cartRepository.getTotalCartsCount();
	                
	                // 再取得分頁資料
	                List<CartDTO> allCarts = cartRepository.getAllCarts(page, size);
	                for (CartDTO cart : allCarts) {
	                    AdminCartDTO adminCart = convertToAdminCartDTO(cart);
	                    carts.add(adminCart);
	                }
	            }
	            
	            // 計算分頁資訊
	            Integer totalPages = (totalCarts + size - 1) / size;  // 向上取整
	            
	            AdminCartListResponse.AdminCartData data = 
	                new AdminCartListResponse.AdminCartData(carts, totalCarts, totalPages, page);
	            
	            return new AdminCartListResponse(data);
	            
	        } catch (Exception e) {
	            throw new CartException(CartErrorCode.ADM_001);
	        } 
	    }
	
	// ============ 購物車統計 (後台) =========== //
	public CartStatisticsResponse getCartStatistics() {
        try {
             Integer totalCarts = cartRepository.getTotalCartsCount();
            
            CartStatisticsResponse.StatisticsData data = 
                new CartStatisticsResponse.StatisticsData(totalCarts);
            
            return new CartStatisticsResponse(data);
            
        } catch (Exception e) {
            throw new CartException(CartErrorCode.ADM_003);
        }
    }
	
	
	// ============ 轉換方法 =========== //
	private AdminCartDTO convertToAdminCartDTO(CartDTO cart) {
		
        AdminCartDTO adminCart = new AdminCartDTO();
        adminCart.setMemNo(cart.getMemNo());
        
        
        // 查詢會員名稱
        try {
            Optional<Member> memberOpt = memRepository.findById(cart.getMemNo());
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                adminCart.setMemName(member.getMemName());
            } else {
                // 拋出會員資料異常
                throw new CartException(CartErrorCode.CART_009);
            }
        } catch (CartException e) {
            // 重新拋出購物車異常，讓上層處理
            throw e;
        } catch (Exception e) {
            // 其他異常也轉為會員資料問題
            System.err.println("查詢會員名稱失敗: " + e.getMessage());
            throw new CartException(CartErrorCode.CART_009, e);
        }
        
        
        adminCart.setTotalItems(cart.getTotalItem());
        adminCart.setTotalQuantity(cart.getTotalQuantity());
        adminCart.setTotalPrice(cart.getTotalPrice());
        
        
        
        // 轉換商品清單
        List<AdminCartDTO.AdminCartItemDTO> adminItems = new ArrayList<>();
        for (CartDTO.CartItem item : cart.getItem()) {
            AdminCartDTO.AdminCartItemDTO adminItem = new AdminCartDTO.AdminCartItemDTO();
            adminItem.setProNo(item.getProNo());
            adminItem.setProName(item.getProName());
            adminItem.setProPrice(item.getProPrice());
            adminItem.setProNum(item.getProNum());
            adminItem.setSubtotal(item.getSubtotal());
            adminItems.add(adminItem);
        }
        adminCart.setItems(adminItems);
        
        return adminCart;
    }
	
	
	// ============ 查詢產品庫存資訊 (後台) =========== //
		public StockInfoResponse getStockInfo(Integer productId) {
			Product product = productRepository.findById(productId).orElse(null);
			if (product == null) {
				return null;
			}
			
			Integer stock = getProductStock(product);
			String stockSource = "預購".equals(product.getProStatus()) ? "Redis暫存數量" : "序號表計算";
			
			return new StockInfoResponse(
				productId, 
				product.getProName(), 
				product.getProStatus(), 
				stock, 
				stockSource
			);
		}
		
		// ============ 設定預購商品庫存 (後台) =========== //
		public void setPreOrderStock(Integer productId, Integer stock) {
			try {
				String key = PREORDER_STOCK_PREFIX + productId;
				redisTemplate.opsForValue().set(key, stock.toString());
				System.out.println("預購產品 " + productId + " 庫存已設定為: " + stock);
			} catch (Exception e) {
				System.err.println("設定預購產品庫存失敗: " + e.getMessage());
				throw new RuntimeException("設定預購庫存失敗", e);
			}
		}
	
	
	
	
	// ============ 私有方法 - 獲取產品庫存 =========== //
	// 獲取產品庫存
	private Integer getProductStock(Product product) {
	    if (product == null) {
	        return 0;
	    }
	    
	    // 根據產品狀態決定庫存來源
	    if ("預購".equals(product.getProStatus())) {
	        return getPreOrderStock(product.getId());
	    } else if ("上架".equals(product.getProStatus())) {
	        return getOnShelfStock(product.getId());
	    } else {
	        return 0; // 其他狀態
	    }
	}

	// 獲取預購商品庫存（從 Redis）
	private Integer getPreOrderStock(Integer productId) {
	    try {
	        String key = PREORDER_STOCK_PREFIX + productId;
	        String stockStr = redisTemplate.opsForValue().get(key);
	        
	        if (stockStr == null) {
	            return Integer.MAX_VALUE; // 預購商品預設無限制
	        }
	        
	        return Integer.parseInt(stockStr);
	    } catch (Exception e) {
	        System.err.println("查詢預購產品庫存失敗: " + e.getMessage());
	        return Integer.MAX_VALUE;
	    }
	}

	// 獲取上架商品庫存（從序號表）
	private Integer getOnShelfStock(Integer productId) {
	    try {
	        Product product = productRepository.findById(productId).orElse(null);
	        if (product == null) {
	            return 0;
	        }
	        
	        // 計算未分配給訂單的序號數量（orderItemNo 為 null）
	        Long availableCount = product.getProSerialNumbers().stream()
	            .filter(serialNumber -> serialNumber.getOrderItemNo() == null)
	            .count();
	            
	        return availableCount.intValue();
	        
	    } catch (Exception e) {
	        System.err.println("查詢上架產品庫存失敗: " + e.getMessage());
	        return 0;
	    }
	}

	// 生成庫存警告訊息
	private String getStockWarningMessage(Product product, Integer requestQuantity, Integer availableStock) {
	    String productType = "預購".equals(product.getProStatus()) ? "預購商品" : "現貨商品";
	    
	    if (availableStock == 0) {
	        return String.format("此%s目前缺貨", productType);
	    } else if (availableStock == Integer.MAX_VALUE) {
	        return null; // 無限制庫存，無警告
	    } else if (requestQuantity > availableStock) {
	        return String.format("%s庫存不足，目前僅剩 %d 個", productType, availableStock);
	    } else if (availableStock <= 10 && !"預購".equals(product.getProStatus())) {
	        return String.format("現貨庫存偏低，僅剩 %d 個", availableStock);
	    }
	    
	    return null; // 庫存充足，無警告
	}
	
	
	
	
}
	
	

