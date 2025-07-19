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
	
	private static final int MAX_ITEMS_PER_CART = 50;          // 購物車最大商品種類
	private static final int MAX_QUANTITY_PER_ITEM = 99;       // 單項商品最大數量
	private static final int STOCK_WARNING_THRESHOLD = 10;     // 庫存警告閾值
	private static final String PREORDER_STATUS = "預購";      // 預購狀態
	private static final int MAX_CART_TOTAL_PRICE = 200000;    // 購物車最大總價
	
    
    // Redis Key 前綴常數
    private static final String PREORDER_STOCK_PREFIX = "preorder:stock:product:";
	
	// ============ 加入商品到購物車 ===========//
	public CartDTO addToCart(Integer memNo, Integer proNo, Integer proNum) {
		
		// 新增：驗證商品數量限制
	    validateItemQuantityLimit(proNum);
		
		Product product = productRepository.findById(proNo).orElse(null);
        
        if (product == null) {
            throw new CartException(CartErrorCode.CART_006); // 商品不存在
        }
        
        boolean 已下架 = (product.getProIsmarket() == '1');
        if (已下架) {
        	throw new CartException(CartErrorCode.CART_002); // 商品不可購買
        }
        
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
            Integer newTotalQuantity = existingItem.getProNum() + proNum;
            
            // 🔥 新增：驗證更新後的數量是否超限
            validateItemQuantityLimit(newTotalQuantity);
            
            // 檢查庫存邏輯...
            Integer availableStock = getProductStock(product);
            boolean hasStockIssue = false;
            String stockWarning = null;
            
            if (availableStock == 0) {
                hasStockIssue = true;
                stockWarning = getStockWarningMessage(product, newTotalQuantity, availableStock);
            } else if (availableStock != Integer.MAX_VALUE && newTotalQuantity > availableStock) {
                hasStockIssue = true;
                stockWarning = getStockWarningMessage(product, newTotalQuantity, availableStock);
            } else if (availableStock <= STOCK_WARNING_THRESHOLD && !PREORDER_STATUS.equals(product.getProStatus())) {
                hasStockIssue = true;
                stockWarning = getStockWarningMessage(product, newTotalQuantity, availableStock);
            }
            
            existingItem.setProNum(newTotalQuantity);
            existingItem.calculateTotal();
            existingItem.setHasStockIssue(hasStockIssue);    
            existingItem.setStockWarning(stockWarning);
            
        } else {
            // 🔥 新增：驗證是否會超過購物車商品種類限制
            validateCartItemsLimit(cart, true);
            
            // 建立新商品項目
            String proName = product.getProName();
            Integer proPrice = product.getProPrice();
            
            // 檢查庫存邏輯...
            Integer availableStock = getProductStock(product);
            boolean hasStockIssue = false;
            String stockWarning = null;
            
            if (availableStock == 0) {
                hasStockIssue = true;
                stockWarning = getStockWarningMessage(product, proNum, availableStock);
            } else if (availableStock != Integer.MAX_VALUE && proNum > availableStock) {
                hasStockIssue = true;
                stockWarning = getStockWarningMessage(product, proNum, availableStock);
            } else if (availableStock <= STOCK_WARNING_THRESHOLD && !PREORDER_STATUS.equals(product.getProStatus())) {
                hasStockIssue = true;
                stockWarning = getStockWarningMessage(product, proNum, availableStock);
            }
            
            CartDTO.CartItem newItem = new CartDTO.CartItem();
            newItem.setProNo(proNo);
            newItem.setProName(proName);
            newItem.setProPrice(proPrice);
            newItem.setProNum(proNum);
            newItem.setProStatus(product.getProStatus());
            newItem.setHasStockIssue(hasStockIssue);
            newItem.setStockWarning(stockWarning);
            newItem.calculateTotal();
            cart.getItem().add(newItem);
        }
        
        // 計算總價
        cart.calculateTotals();
        
        // 新增：驗證購物車總限制
        validateCartTotalLimits(cart);

		
		// 保存到Redis
		cartRepository.saveCart(memNo, cart);
		
		// 回傳到CartDTO
		return cart;  
	}
	
	
	// ============ 獲取會員購物車 ===========//
	public CartDTO getMemberCart(Integer memNo) {
		
		// 取得購物車
		CartDTO cart = cartRepository.getCart(memNo);
		
		// 加入debug log
		System.out.println("=== 購物車 Debug ===");
	    System.out.println("會員編號: " + memNo);
	    System.out.println("Redis 回傳的 cart: " + cart);
		
		
		if (cart == null) {
			System.out.println("購物車為 null，創建新的空購物車");
			
			// 購物車不存在，創建空的購物車
            cart = new CartDTO();
            cart.setMemNo(memNo);
            cart.setItem(new ArrayList<>());
            cart.calculateTotals();
        } else {
            System.out.println("購物車存在，商品數量: " + (cart.getItem() != null ? cart.getItem().size() : "item為null"));
            // 確保 item 不是 null
            if (cart.getItem() == null) {
                cart.setItem(new ArrayList<>());
            }
            cart.calculateTotals();
        }
        
        // 🔥 回傳前再次 debug
        System.out.println("最終回傳的購物車: " + cart);
        System.out.println("總商品數: " + cart.getTotalItem());
        System.out.println("總價格: " + cart.getTotalPrice());
        System.out.println("=== Debug 結束 ===");	
		
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

		// 修正：支援數量為0（代表移除商品）
	    if (proNum == null || proNum < 0) {
	        throw new CartException(CartErrorCode.CART_012); // 數量不能為負數
	    }
	    
	    // 如果數量為0，直接移除商品
	    if (proNum == 0) {
	        return removeFromCart(memNo, proNo);
	    }
		
		// 新增：驗證商品數量限制
	    validateItemQuantityLimit(proNum);
		
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
	            // 重新檢查庫存狀況
	            Product product = productRepository.findById(proNo).orElse(null);
	            if (product != null) {
	                Integer availableStock = getProductStock(product);
	                boolean hasStockIssue = false;
	                String stockWarning = null;
	                
	                if (availableStock == 0) {
	                    hasStockIssue = true;
	                    stockWarning = getStockWarningMessage(product, proNum, availableStock);
	                } else if (availableStock != Integer.MAX_VALUE && proNum > availableStock) {
	                    hasStockIssue = true;
	                    stockWarning = getStockWarningMessage(product, proNum, availableStock);
	                } else if (availableStock <= STOCK_WARNING_THRESHOLD && !PREORDER_STATUS.equals(product.getProStatus())) {
	                    hasStockIssue = true;
	                    stockWarning = getStockWarningMessage(product, proNum, availableStock);
	                }
	                
	                item.setHasStockIssue(hasStockIssue);
	                item.setStockWarning(stockWarning);
	                item.setProStatus(product.getProStatus());
	            }
	            
	            item.setProNum(proNum);
	            item.calculateTotal();
	            break;
	        }
	    }
		
		// 重新計算總價		
		cart.calculateTotals();
		
		// 新增：驗證購物車總限制
	    validateCartTotalLimits(cart);
		
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
	
	
	// ============ 購物車限制驗證方法 =========== //
	// ***** 驗證購物車是否超過商品種類限制 ***** //
	private void validateCartItemsLimit(CartDTO cart, boolean isAddingNew) {
	    if (cart.getItem() == null) return;
	    
	    int currentItemCount = cart.getItem().size();
	    int maxAllowed = isAddingNew ? currentItemCount + 1 : currentItemCount;
	    
	    if (maxAllowed > MAX_ITEMS_PER_CART) {
	        throw new CartException(CartErrorCode.CART_011); // 需要新增這個錯誤碼
	    }
	}
	
	
	// ***** 驗證單項商品數量是否超過限制 ***** //
	private void validateItemQuantityLimit(Integer quantity) {
	    if (quantity == null || quantity < 1) {
	        throw new CartException(CartErrorCode.CART_012); // 數量必須大於0
	    }
	    
	    if (quantity > MAX_QUANTITY_PER_ITEM) {
	        throw new CartException(CartErrorCode.CART_013); // 數量超過上限
	    }
	}
	
	
	// ***** 驗證購物車總重量/價值限制 ***** //
	private void validateCartTotalLimits(CartDTO cart) {
	    if (cart.getTotalPrice() != null && cart.getTotalPrice() > MAX_CART_TOTAL_PRICE) {
	        throw new CartException(CartErrorCode.CART_014); // 購物車總價超限
	    }
	}
	
	
	// ============ 批量操作功能 (前台) ===========//
	
	// *** 批量移除購物車商品 *** //
	public CartDTO removeMultipleItems(Integer memNo, List<Integer> proNos) {
	    if (proNos == null || proNos.isEmpty()) {
	        throw new CartException(CartErrorCode.CART_015); // 批量操作參數錯誤
	    }
	    
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
	    
	    // 批量移除指定產品
	    cart.getItem().removeIf(item -> proNos.contains(item.getProNo()));
	    
	    // 重新計算總價
	    cart.calculateTotals();
	    
	    // 存到Redis
	    cartRepository.saveCart(memNo, cart);
	    
	    return cart;
	}
	

	// *** 批量更新購物車商品數量 *** //
	public CartDTO updateMultipleItemsQuantity(Integer memNo, List<CartUpdateItem> updateItems) {
	    if (updateItems == null || updateItems.isEmpty()) {
	        throw new CartException(CartErrorCode.CART_015);
	    }
	    
	    // 取得購物車
	    CartDTO cart = cartRepository.getCart(memNo);
	    
	    if (cart == null) {
	        cart = new CartDTO();
	        cart.setMemNo(memNo);
	        cart.setItem(new ArrayList<>());
	        cart.calculateTotals();
	        return cart;
	    }
	    
	    // 收集需要移除的商品（數量為0）
	    List<Integer> itemsToRemove = new ArrayList<>();
	    
	    // 批量更新商品數量
	    for (CartUpdateItem updateItem : updateItems) {
	        // 驗證數量
	        if (updateItem.getQuantity() == null || updateItem.getQuantity() < 0) {
	            throw new CartException(CartErrorCode.CART_012);
	        }
	        
	        // 如果數量為0，加入移除清單
	        if (updateItem.getQuantity() == 0) {
	            itemsToRemove.add(updateItem.getProNo());
	            continue;
	        }
	        
	        // 驗證非0數量的限制
	        validateItemQuantityLimit(updateItem.getQuantity());
	        
	        // 更新商品數量
	        for (CartDTO.CartItem item : cart.getItem()) {
	            if (item.getProNo().equals(updateItem.getProNo())) {
	                // 重新檢查庫存狀況
	                Product product = productRepository.findById(updateItem.getProNo()).orElse(null);
	                if (product != null) {
	                    Integer availableStock = getProductStock(product);
	                    boolean hasStockIssue = false;
	                    String stockWarning = null;
	                    
	                    if (availableStock == 0) {
	                        hasStockIssue = true;
	                        stockWarning = getStockWarningMessage(product, updateItem.getQuantity(), availableStock);
	                    } else if (availableStock != Integer.MAX_VALUE && updateItem.getQuantity() > availableStock) {
	                        hasStockIssue = true;
	                        stockWarning = getStockWarningMessage(product, updateItem.getQuantity(), availableStock);
	                    } else if (availableStock <= STOCK_WARNING_THRESHOLD && !PREORDER_STATUS.equals(product.getProStatus())) {
	                        hasStockIssue = true;
	                        stockWarning = getStockWarningMessage(product, updateItem.getQuantity(), availableStock);
	                    }
	                    
	                    item.setHasStockIssue(hasStockIssue);
	                    item.setStockWarning(stockWarning);
	                    item.setProStatus(product.getProStatus());
	                }
	                
	                item.setProNum(updateItem.getQuantity());
	                item.calculateTotal();
	                break;
	            }
	        }
	    }
	    
	    // 移除數量為0的商品
	    if (!itemsToRemove.isEmpty()) {
	        cart.getItem().removeIf(item -> itemsToRemove.contains(item.getProNo()));
	    }
	    
	    cart.calculateTotals();
	    
	    // 驗證購物車總限制
	    validateCartTotalLimits(cart);
	    
	    cartRepository.saveCart(memNo, cart);
	    return cart;
	}
	
	
	
	// *** 獲取選中商品的購物車資訊（結帳用） *** //
	public CartDTO getSelectedItemsCart(Integer memNo, List<Integer> selectedProNos) {
	    if (selectedProNos == null || selectedProNos.isEmpty()) {
	        throw new CartException(CartErrorCode.CART_015); // 批量操作參數錯誤
	    }
	    
	    // 取得完整購物車
	    CartDTO fullCart = cartRepository.getCart(memNo);
	    
	    if (fullCart == null || fullCart.getItem().isEmpty()) {
	        // 創建空的購物車回傳
	        CartDTO emptyCart = new CartDTO();
	        emptyCart.setMemNo(memNo);
	        emptyCart.setItem(new ArrayList<>());
	        emptyCart.calculateTotals();
	        return emptyCart;
	    }
	    
	    // 創建包含選中商品的新購物車
	    CartDTO selectedCart = new CartDTO();
	    selectedCart.setMemNo(memNo);
	    selectedCart.setItem(new ArrayList<>());
	    
	    // 只加入選中的商品
	    for (CartDTO.CartItem item : fullCart.getItem()) {
	        if (selectedProNos.contains(item.getProNo())) {
	            selectedCart.getItem().add(item);
	        }
	    }
	    
	    // 重新計算總價
	    selectedCart.calculateTotals();
	    
	    return selectedCart;
	}
	
	
	// *** 驗證結帳前的購物車狀態 *** //
	public CartValidationResponse validateCartForCheckout(Integer memNo, List<Integer> selectedProNos) {
	    CartDTO cart;
	    
	    if (selectedProNos != null && !selectedProNos.isEmpty()) {
	        // 驗證選中商品
	        cart = getSelectedItemsCart(memNo, selectedProNos);
	    } else {
	        // 驗證整個購物車
	        cart = getMemberCart(memNo);
	    }
	    
	    CartValidationResponse response = new CartValidationResponse();
	    response.setMemNo(memNo);
	    response.setValid(true);
	    response.setIssues(new ArrayList<>());
	    
	    if (cart.getItem().isEmpty()) {
	        response.setValid(false);
	        response.getIssues().add("購物車是空的，小精靈不知道要結帳什麼");
	        return response;
	    }
	    
	    // 檢查每個商品的狀態
	    for (CartDTO.CartItem item : cart.getItem()) {
	        // 重新檢查產品是否仍可購買
	        Product product = productRepository.findById(item.getProNo()).orElse(null);
	        
	        if (product == null) {
	            response.setValid(false);
	            response.getIssues().add(String.format("商品 %s 已不存在", item.getProName()));
	            continue;
	        }
	        
	        // 檢查是否下架
	        if (product.getProIsmarket() == '1') {
	            response.setValid(false);
	            response.getIssues().add(String.format("商品 %s 已下架", item.getProName()));
	            continue;
	        }
	        
	        // 檢查庫存
	        Integer availableStock = getProductStock(product);
	        if (availableStock == 0) {
	            response.setValid(false);
	            response.getIssues().add(String.format("商品 %s 目前缺貨", item.getProName()));
	        } else if (availableStock != Integer.MAX_VALUE && item.getProNum() > availableStock) {
	            response.setValid(false);
	            response.getIssues().add(String.format("商品 %s 庫存不足，僅剩 %d 個", item.getProName(), availableStock));
	        }
	    }
	    
	    response.setTotalItems(cart.getTotalItem());
	    response.setTotalQuantity(cart.getTotalQuantity());
	    response.setTotalPrice(cart.getTotalPrice());
	    
	    return response;
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
	        // 1. 取得所有購物車數據
	        List<CartDTO> allCarts = cartRepository.getAllCarts(1, Integer.MAX_VALUE);
	        Integer totalCarts = allCarts.size();
	        
	        // 2. 初始化統計變數
	        Integer totalActiveUsers = 0;
	        Integer totalProducts = 0;
	        Long totalCartValue = 0L;

	        Integer cartsWithStockIssues = 0;
	        Integer preOrderProductCount = 0;
	        Integer onShelfProductCount = 0;
	        
	        // 3. 遍歷所有購物車進行統計
	        for (CartDTO cart : allCarts) {
	            if (cart.getItem() == null || cart.getItem().isEmpty()) {
	                continue; // 跳過空購物車，不計入統計
	            }
	            
	            totalActiveUsers++;
	            
	            // 統計每個購物車的商品
	            boolean hasStockIssue = false;
	            for (CartDTO.CartItem item : cart.getItem()) {
	                totalProducts += item.getProNum();
	                totalCartValue += (long) item.getSubtotal();
	                
	                // 檢查庫存問題
	                if (Boolean.TRUE.equals(item.getHasStockIssue())) {
	                    hasStockIssue = true;
	                }
	                
	                // 統計商品狀態
	                if ("預購中".equals(item.getProStatus()) || "預購".equals(item.getProStatus())) {
	                    preOrderProductCount += item.getProNum();
	                } else if ("已發售".equals(item.getProStatus()) || "上架".equals(item.getProStatus())) {
	                    onShelfProductCount += item.getProNum();
	                }
	            }
	            
	            if (hasStockIssue) {
	                cartsWithStockIssues++;
	            }
	        }
	        
	        // 4. 計算平均值
	        Integer averageItemsPerCart = totalActiveUsers > 0 ? 
	            (totalProducts / totalActiveUsers) : 0;
	        
	        // 5. 建立統計數據對象
	        CartStatisticsResponse.StatisticsData data = new CartStatisticsResponse.StatisticsData();
	        data.setTotalCarts(totalCarts);
	        data.setTotalActiveUsers(totalActiveUsers);
	        data.setTotalProducts(totalProducts);
	        data.setAverageItemsPerCart(averageItemsPerCart);
	        data.setTotalCartValue(totalCartValue);
	        data.setCartsWithStockIssues(cartsWithStockIssues);
	        data.setPreOrderProductCount(preOrderProductCount);
	        data.setOnShelfProductCount(onShelfProductCount);
	        
	        return new CartStatisticsResponse(data);
	        
	    } catch (Exception e) {
	        System.err.println("統計購物車數據失敗: " + e.getMessage());
	        throw new CartException(CartErrorCode.ADM_003, e);
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
            
            // 基本商品資訊
            adminItem.setProNo(item.getProNo());
            adminItem.setProName(item.getProName());
            adminItem.setProPrice(item.getProPrice());
            adminItem.setProNum(item.getProNum());
            adminItem.setSubtotal(item.getSubtotal());
            
            // 新增：庫存相關資訊
            adminItem.setProStatus(item.getProStatus());
            adminItem.setHasStockIssue(item.getHasStockIssue());
            adminItem.setStockWarning(item.getStockWarning());
            
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
		// ***** 同時設定兩種格式的 key，確保相容性 ***** //
		public void setPreOrderStock(Integer productId, Integer stock) {
		    try {
		        // 設定 ProductPreorderService 使用的格式
		        String simpleKey = productId.toString();
		        redisTemplate.opsForValue().set(simpleKey, stock.toString());  // 改為 stock.toString() 避免泛型
		        
		        // 同時設定原本的格式（向後相容）
		        String originalKey = PREORDER_STOCK_PREFIX + productId;
		        redisTemplate.opsForValue().set(originalKey, stock.toString());
		        
		        System.out.println("預購產品 " + productId + " 庫存已設定為: " + stock);
		    } catch (Exception e) {
		        System.err.println("設定預購產品庫存失敗: " + e.getMessage());
		        throw new RuntimeException("設定預購庫存失敗", e);
		    }
		}
		
		
		// ============ 檢查預購庫存資料一致性 =========== //
		public void checkPreorderStockConsistency(Integer productId) {
		    try {
		        // 檢查兩種格式的 Redis key
		        String simpleKey = productId.toString();
		        String originalKey = PREORDER_STOCK_PREFIX + productId;
		        
		        String simpleValue = redisTemplate.opsForValue().get(simpleKey);
		        String originalValue = redisTemplate.opsForValue().get(originalKey);
		        
		        System.out.println("=== 預購庫存一致性檢查 ===");
		        System.out.println("商品ID: " + productId);
		        System.out.println("簡單格式 (" + simpleKey + "): " + simpleValue);
		        System.out.println("原始格式 (" + originalKey + "): " + originalValue);
		        
		        if (simpleValue != null && originalValue != null) {
		            boolean consistent = simpleValue.equals(originalValue);
		            System.out.println("資料一致性: " + (consistent ? "✅ 一致" : "❌ 不一致"));
		        } else if (simpleValue == null && originalValue == null) {
		            System.out.println("狀態: 兩種格式都沒有資料");
		        } else {
		            System.out.println("狀態: ⚠️ 只有其中一種格式有資料");
		        }
		        
		    } catch (Exception e) {
		        System.err.println("檢查預購庫存一致性失敗: " + e.getMessage());
		    }
		}
		
		
		// ============ 同步預購庫存資料（修正版） =========== //
		public void syncPreorderStockData(Integer productId) {
		    try {
		        String simpleKey = productId.toString();
		        String originalKey = PREORDER_STOCK_PREFIX + productId;
		        
		        String simpleValue = redisTemplate.opsForValue().get(simpleKey);
		        String originalValue = redisTemplate.opsForValue().get(originalKey);
		        
		        if (simpleValue != null && originalValue == null) {
		            // 簡單格式有資料，原始格式沒有 -> 同步到原始格式
		            redisTemplate.opsForValue().set(originalKey, simpleValue);
		            System.out.println("已同步庫存資料: " + simpleKey + " -> " + originalKey);
		            
		        } else if (simpleValue == null && originalValue != null) {
		            // 原始格式有資料，簡單格式沒有 -> 同步到簡單格式
		            redisTemplate.opsForValue().set(simpleKey, originalValue);
		            System.out.println("已同步庫存資料: " + originalKey + " -> " + simpleKey);
		            
		        } else if (simpleValue != null && originalValue != null) {
		            // 兩種格式都有資料，檢查是否一致
		            if (!simpleValue.equals(originalValue)) {
		                // 不一致時，以簡單格式為準（因為 ProductPreorderService 使用此格式）
		                redisTemplate.opsForValue().set(originalKey, simpleValue);
		                System.out.println("已修正不一致的庫存資料，以簡單格式為準: " + simpleValue);
		            }
		        }
		        
		    } catch (Exception e) {
		        System.err.println("同步預購庫存資料失敗: " + e.getMessage());
		    }
		}
	
	
	
	// ============ 私有方法 - 獲取產品庫存 =========== //
	// 獲取產品庫存
	private Integer getProductStock(Product product) {
	    if (product == null) {
	        return 0;
	    }
	    
	    String status = product.getProStatus();
	    
	    // 根據產品狀態決定庫存來源  (同時支援 "預購" 和 "預購中" 兩種狀態)
	    if ("預購".equals(status) || "預購中".equals(status)) {
	        return getPreOrderStock(product.getId());
	    } else if ("上架".equals(status) || "已發售".equals(status)) {
	        return getOnShelfStock(product.getId());
	    } else {
	        return 0; // 其他狀態
	    }
	}

	// 獲取預購商品庫存（從 Redis）
	private Integer getPreOrderStock(Integer productId) {
	    try {
	    	
	    	// 修改成跟ProductPreorderService 使用的格式
	    	String simpleKey = productId.toString();
	    	String simpleValue = redisTemplate.opsForValue().get(simpleKey);
	        
	        if (simpleValue != null && !simpleValue.isEmpty()) {
	            // 找到了！使用 ProductPreorderService 的資料
	        	Integer stock = Integer.parseInt(simpleValue);
	            return stock > 0 ? stock : 0; // 確保不會是負數
	        }
	        
	        // 如果 ProductPreorderService 格式沒有資料，再嘗試原本的格式
	        String originalKey = PREORDER_STOCK_PREFIX + productId;
	        String stockStr = redisTemplate.opsForValue().get(originalKey);
	        
	        if (stockStr != null && !stockStr.isEmpty()) {
	            return Integer.parseInt(stockStr);
	        }
	        
	        // 兩種格式都沒有資料，回傳預設值
	        // 修改：改為回傳 0 而不是 Integer.MAX_VALUE，與 ProductPreorderService 一致
	        return 0;
	        
	    } catch (NumberFormatException e) {
	        System.err.println("Redis 中的庫存資料格式錯誤，商品ID: " + productId + ", 錯誤: " + e.getMessage());
	        return 0;
	    } catch (Exception e) {
	        System.err.println("查詢預購產品庫存失敗: " + e.getMessage());
	        return 0;
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
	    String status = product.getProStatus();
	    String productType;
	    
	    // 統一狀態判斷
	    if ("預購".equals(status) || "預購中".equals(status)) {
	        productType = "預購商品";
	    } else {
	        productType = "現貨商品";
	    }
	    
	    if (availableStock == 0) {
	        return String.format("此%s目前缺貨", productType);
	    } else if (availableStock != Integer.MAX_VALUE && requestQuantity > availableStock) {
	        return String.format("%s庫存不足，目前僅剩 %d 個", productType, availableStock);
	    } else if (availableStock <= 10 && !"預購".equals(status) && !"預購中".equals(status)) {
	        return String.format("現貨庫存偏低，僅剩 %d 個", availableStock);
	    }
	    
	    return null; // 庫存充足，無警告
	}
	
	
	
	// ============ 輔助類別 =========== //
	// *** 批量更新商品數量的輔助類別 *** //
	public static class CartUpdateItem {
	    private Integer proNo;
	    private Integer quantity;
	    
	    // 建構子
	    public CartUpdateItem() {}
	    
	    public CartUpdateItem(Integer proNo, Integer quantity) {
	        this.proNo = proNo;
	        this.quantity = quantity;
	    }
	    
	    // Getter & Setter
	    public Integer getProNo() { return proNo; }
	    public void setProNo(Integer proNo) { this.proNo = proNo; }
	    public Integer getQuantity() { return quantity; }
	    public void setQuantity(Integer quantity) { this.quantity = quantity; }
	}
	
	
}
	
	

