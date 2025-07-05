package com.pixeltribe.shopsys.cart.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.shopsys.cart.exception.CartErrorCode;
import com.pixeltribe.shopsys.cart.exception.CartException;


@Service
@Transactional
public class CartService {
	
	@Autowired
	private CartRepository cartRepository;  // 注入 Redis Repository

//	@Autowired    // ProductRepository未完成，所以先註解掉(未來要import)
//	private ProductRepository productRepository;  // 注入產品資料庫
	
	// ============ 加入商品到購物車 ===========//
	public CartDTO addToCart(Integer memNo, Integer proNo, Integer proNum) {

		

		// 只先查詢產品是否存在，暫不檢查狀態 (未來要import)
//		Product product = productRepository.findByProNo(proNo).orElse(null);
//        
//        if (product == null) {
//            throw new CartException(CartErrorCode.CART_002); // 商品不存在
//        }
        
        // 等產品完成後,再把檢查狀態打開
//        if (!"Y".equals(product.getProIsmarket()) || !"可販售".equals(product.getProStatus())) {
//        	throw new CartException(CartErrorCode.CART_002); // 商品不可購買
//        }
        
        // 使用資料庫裡的產品資訊  (等產品完成後，在打開)
//        String proName = product.getProName();
//        Integer proPrice = product.getProPrice();
		
		// 暫時先用固定值去跑
		String proName = "商品" + proNo;
        Integer proPrice = 100;
        
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
			existingItem.setProNum(existingItem.getProNum() + proNum);
            existingItem.calculateTotal(); // 重新計算小計
		} else {
			// 建立新商品項目
			CartDTO.CartItem newItem = new CartDTO.CartItem();
            newItem.setProNo(proNo);
            newItem.setProName(proName);
            newItem.setProPrice(proPrice);
            newItem.setProNum(proNum);
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
	            
	           // 如果指定會員編號，只查詢該會員
	            if (memNo != null) {
	                CartDTO cart = cartRepository.getCart(memNo);
	                if (cart != null) {
	                    AdminCartDTO adminCart = convertToAdminCartDTO(cart);
	                    carts.add(adminCart);
	                }
	            } else {
	               
	                 List<CartDTO> allCarts = cartRepository.getAllCarts(page, size);
	                 for (CartDTO cart : allCarts) {
	                     AdminCartDTO adminCart = convertToAdminCartDTO(cart);
	                     carts.add(adminCart);
	                 }
	            }
	            
	            // 計算分頁資訊
	            Integer totalCarts = carts.size();
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
}
	
	

