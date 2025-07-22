package com.pixeltribe.shopsys.cart.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.cart.exception.CartErrorCode;
import com.pixeltribe.shopsys.cart.exception.CartException;
import com.pixeltribe.shopsys.cart.model.AdminCartListResponse;
import com.pixeltribe.shopsys.cart.model.CartDTO;
import com.pixeltribe.shopsys.cart.model.CartRequests;
import com.pixeltribe.shopsys.cart.model.CartService;
import com.pixeltribe.shopsys.cart.model.CartStatisticsResponse;
import com.pixeltribe.shopsys.cart.model.CartValidationResponse;
import com.pixeltribe.shopsys.cart.model.StockInfoResponse;
import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;




@RestController
@RequestMapping("/api")
public class CartController {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private ProductRepository productRepository;
	
	// ******** 前台API (會員對購物車的操作) ******** //
	// ==========  將商品加到購物車 ============ //
	@PostMapping("/cart/add")
	public ResponseEntity<CartDTO> addToCart(
			@RequestParam Integer proNo,
			@RequestParam Integer proNum,
			HttpServletRequest request) {
		
		
		// 取得會員ID
		Integer memNo = (Integer) request.getAttribute("currentId");
	    if (memNo == null) {       // 加入登入驗證
	        throw new CartException(CartErrorCode.ADM_001);
	    }
		
		// 呼叫CartService
	    CartDTO cart = cartService.addToCart(memNo, proNo, proNum);
	    
	    return ResponseEntity.ok(cart);   // 配對成功
	}
	
	
	// ==========  查詢購物車 ============ //
	@GetMapping("/cart/{memNo}")
	public ResponseEntity<CartDTO> getCart(@PathVariable Integer memNo,
											HttpServletRequest request) {
	
		
		Integer currentMemNo = (Integer) request.getAttribute("currentId");
	    if (!memNo.equals(currentMemNo)) {
	        // 拋出權限不足的例外
	        throw new CartException(CartErrorCode.ADM_001);
	    }
	    CartDTO cart = cartService.getMemberCart(memNo);
	    return ResponseEntity.ok(cart);
	}
	
	
	// ========== 移除購物車商品 ============ //
	@DeleteMapping("/cart/remove/{proNo}")
	public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable Integer proNo,
            HttpServletRequest request) {
		
		Integer memNo = (Integer) request.getAttribute("currentId");
	    if (memNo == null) {  // 加入登入驗證
	        throw new CartException(CartErrorCode.ADM_001);
	    }
	    CartDTO cart = cartService.removeFromCart(memNo, proNo);
	    
	    return ResponseEntity.ok(cart);
	}
	
	
	// ========== 更新商品數量 ============ //
	@PutMapping("/cart/update/{proNo}")
	public ResponseEntity<CartDTO> updateQuantity(
            @PathVariable Integer proNo,
            @RequestParam Integer proNum,
            HttpServletRequest request) {
		
		Integer memNo = (Integer) request.getAttribute("currentId");
		if (memNo == null) {  // 加入登入驗證
	        throw new CartException(CartErrorCode.ADM_001);
	    }
        CartDTO cart = cartService.updateCartItemQuantity(memNo, proNo, proNum);
        
        return ResponseEntity.ok(cart);
	}
	
	
	// ========== 清空購物車 ============ //
	@PostMapping("/cart/clear")
	public ResponseEntity<String> clearCart(HttpServletRequest request) {
		
		Integer memNo = (Integer) request.getAttribute("currentId");
		if (memNo == null) {  // 加入登入驗證
	        throw new CartException(CartErrorCode.ADM_001);
	    }
		cartService.clearCart(memNo);
		
		return ResponseEntity.ok("購物車已清空");
	}
	
	
	
	// ========== 批量移除商品 ============ //
	@DeleteMapping("/cart/remove/batch")
	public ResponseEntity<CartDTO> removeMultipleItems(
	        @RequestBody List<Integer> proNos,
	        HttpServletRequest request) {
	    
	    Integer memNo = (Integer) request.getAttribute("currentId");
	    if (memNo == null) {
	        throw new CartException(CartErrorCode.ADM_001);
	    }
	    
	    CartDTO cart = cartService.removeMultipleItems(memNo, proNos);
	    return ResponseEntity.ok(cart);
	}

	// ========== 批量更新商品數量 ============ //
	@PutMapping("/cart/update/batch")
	public ResponseEntity<CartDTO> updateMultipleItemsQuantity(
	        @RequestBody List<CartService.CartUpdateItem> updateItems,
	        HttpServletRequest request) {
	    
	    Integer memNo = (Integer) request.getAttribute("currentId");
	    if (memNo == null) {
	        throw new CartException(CartErrorCode.ADM_001);
	    }
	    
	    CartDTO cart = cartService.updateMultipleItemsQuantity(memNo, updateItems);
	    return ResponseEntity.ok(cart);
	}

	// ========== 獲取選中商品購物車 ============ //
	@PostMapping("/cart/selected")
	public ResponseEntity<CartDTO> getSelectedItemsCart(
	        @Valid @RequestBody CartRequests.SelectedItems request,
	        HttpServletRequest httpRequest) {
	    
	    Integer memNo = (Integer) httpRequest.getAttribute("currentId");
	    if (memNo == null) {
	        throw new CartException(CartErrorCode.ADM_001);
	    }
	    
	    CartDTO cart = cartService.getSelectedItemsCart(memNo, request.getSelectedProNos());
	    return ResponseEntity.ok(cart);
	}

	// ========== 結帳前驗證 ============ //
	@PostMapping("/cart/validate")
	public ResponseEntity<CartValidationResponse> validateCartForCheckout(
	        @RequestBody(required = false) CartRequests.CheckoutValidation request,
	        HttpServletRequest httpRequest) {
	    
	    Integer memNo = (Integer) httpRequest.getAttribute("currentId");
	    if (memNo == null) {
	        throw new CartException(CartErrorCode.ADM_001);
	    }
	    
	    List<Integer> selectedProNos = (request != null) ? request.getSelectedProNos() : null;
	    CartValidationResponse response = cartService.validateCartForCheckout(memNo, selectedProNos);
	    return ResponseEntity.ok(response);
	}
	
	
	// ========== 讀取可購買的庫存數量(包含現貨跟預購) ============ //
	@GetMapping("/cart/stock/{productId}")
	public ResponseEntity<StockInfoResponse> getProductStock(@PathVariable Integer productId) {
	    StockInfoResponse stockInfo = cartService.getStockInfo(productId);
	    return ResponseEntity.ok(stockInfo);
	}
	
	
	
	
	// ******** 後台API (管理員查看數據) ******** //
	// ========== 查詢所有購物車 ============ //
	@GetMapping("/admin/cart/all")
	public ResponseEntity<AdminCartListResponse> getAllCarts( 
		@RequestParam(defaultValue = "1") Integer page,
		@RequestParam(defaultValue = "10") Integer size,
		@RequestParam(required = false) Integer memNo) {
		
		AdminCartListResponse response = cartService.getAllCartsForAdmin(page, size, memNo);
        return ResponseEntity.ok(response);
	}
	
	// ========== 購物車統計 ============ //
	@GetMapping("/admin/cart/statistics")
    public ResponseEntity<CartStatisticsResponse> getCartStatistics() {
        
        CartStatisticsResponse response = cartService.getCartStatistics();
        return ResponseEntity.ok(response);
    }
	
	
	// ========== 查詢產品庫存 ============ //
	@GetMapping("/admin/cart/stock/{productId}")
	public ResponseEntity<StockInfoResponse> getAdminProductStock(@PathVariable Integer productId) {
	    
	    StockInfoResponse response = cartService.getStockInfo(productId);
	    if (response == null) {
	        return ResponseEntity.notFound().build();
	    }
	    return ResponseEntity.ok(response);
	}

	// ========== 設定預購商品庫存 ============ //
	@PostMapping("/admin/cart/stock/preorder/{productId}")
	public ResponseEntity<String> setPreOrderStock(
	        @PathVariable Integer productId,
	        @RequestParam Integer stock) {
	    
	    try {
	        cartService.setPreOrderStock(productId, stock);
	        return ResponseEntity.ok("預購產品庫存設定成功");
	    } catch (Exception e) {
	        return ResponseEntity.status(500).body("設定失敗: " + e.getMessage());
	    }
	}
	
	
	// ========== 🔥 新增：商品類型檢查 API (調試用) ============ //
	@GetMapping("/admin/cart/product-type/{productId}")
	public ResponseEntity<Map<String, Object>> checkProductType(@PathVariable Integer productId) {
	    try {
	        // 取得商品資訊
	        Optional<Product> productOpt = productRepository.findById(productId);
	        if (!productOpt.isPresent()) {
	            Map<String, Object> notFoundResponse = new HashMap<>();
	            notFoundResponse.put("error", "商品不存在");
	            notFoundResponse.put("proNo", productId);
	            return ResponseEntity.notFound().build();
	        }
	        
	        Product product = productOpt.get();
	        
	        // 取得庫存資訊
	        StockInfoResponse stockInfo = cartService.getStockInfo(productId);
	        
	        // 檢查商品類型
	        String productType = cartService.checkProductType(productId);
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("proNo", productId);                    // 對應您的字段名
	        response.put("proName", product.getProName());       // 對應您的字段名
	        response.put("proStatus", product.getProStatus());   // 對應您的字段名
	        response.put("proIsmarket", product.getProIsmarket());
	        response.put("stockInfo", stockInfo);
	        response.put("productType", productType);
	        response.put("checkTime", new Date());
	        response.put("message", "商品類型檢查完成");
	        
	        return ResponseEntity.ok(response);
	        
	    } catch (Exception e) {
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("error", "檢查失敗: " + e.getMessage());
	        errorResponse.put("proNo", productId);
	        errorResponse.put("timestamp", new Date());
	        return ResponseEntity.status(500).body(errorResponse);
	    }
	}

	// ========== 🔥 新增：批量檢查商品類型 API ============ //
	@PostMapping("/admin/cart/batch-check-types")
	public ResponseEntity<Map<String, Object>> batchCheckProductTypes(
	        @RequestBody List<Integer> productIds) {
	    
	    List<Map<String, Object>> results = new ArrayList<>();
	    int successCount = 0;
	    int errorCount = 0;
	    
	    for (Integer productId : productIds) {
	        try {
	            Optional<Product> productOpt = productRepository.findById(productId);
	            if (!productOpt.isPresent()) {
	                Map<String, Object> notFoundResult = new HashMap<>();
	                notFoundResult.put("proNo", productId);
	                notFoundResult.put("error", "商品不存在");
	                results.add(notFoundResult);
	                errorCount++;
	                continue;
	            }
	            
	            Product product = productOpt.get();
	            StockInfoResponse stockInfo = cartService.getStockInfo(productId);
	            String productType = cartService.checkProductType(productId);
	            
	            Map<String, Object> result = new HashMap<>();
	            result.put("proNo", productId);                    // 對應您的字段名
	            result.put("proName", product.getProName());       // 對應您的字段名
	            result.put("proStatus", product.getProStatus());   // 對應您的字段名
	            result.put("proIsmarket", product.getProIsmarket());
	            result.put("stockInfo", stockInfo);
	            result.put("productType", productType);
	            result.put("success", true);
	            
	            results.add(result);
	            successCount++;
	            
	        } catch (Exception e) {
	            Map<String, Object> errorResult = new HashMap<>();
	            errorResult.put("proNo", productId);
	            errorResult.put("error", e.getMessage());
	            errorResult.put("success", false);
	            results.add(errorResult);
	            errorCount++;
	        }
	    }
	    
	    // 建立總結回應
	    Map<String, Object> summaryResponse = new HashMap<>();
	    summaryResponse.put("results", results);
	    summaryResponse.put("summary", Map.of(
	        "total", productIds.size(),
	        "success", successCount,
	        "error", errorCount,
	        "checkTime", new Date()
	    ));
	    
	    return ResponseEntity.ok(summaryResponse);
	}

	// ========== 🔥 新增：快速測試單一商品的庫存判斷 ============ //
	@GetMapping("/admin/cart/quick-test/{productId}")
	public ResponseEntity<Map<String, Object>> quickTestProductStock(@PathVariable Integer productId) {
	    try {
	        // 直接呼叫 getStockInfo 並返回詳細資訊
	        StockInfoResponse stockInfo = cartService.getStockInfo(productId);
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("proNo", productId);
	        response.put("stockInfo", stockInfo);
	        response.put("testTime", new Date());
	        response.put("message", "快速庫存測試完成");
	        
	        return ResponseEntity.ok(response);
	        
	    } catch (Exception e) {
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("error", "測試失敗: " + e.getMessage());
	        errorResponse.put("proNo", productId);
	        return ResponseEntity.status(500).body(errorResponse);
	    }
	}
	
	
	
	
}