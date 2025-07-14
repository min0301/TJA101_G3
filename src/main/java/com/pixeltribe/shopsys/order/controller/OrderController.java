package com.pixeltribe.shopsys.order.controller;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.pixeltribe.shopsys.order.model.*;
import com.pixeltribe.shopsys.orderItem.model.CreateOrderItemRequest;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderService orderService;   // 前台訂單服務
	
	@Autowired
    private AdminOrderService adminOrderService; // 後台管理服務
    
    @Autowired
    private PaymentService paymentService;       // 付款處理服務
    
    
    private static final String FIXED_ADMIN_ID = "ADMIN_USER";
	
	
	@GetMapping("/orders")
    public List<Map<String, Object>> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                	Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderNo", order.getOrderNo());
                    // 只處理 Member 部分
                    orderMap.put("memNo", order.getMemNo() != null ? order.getMemNo().getId() : null);
                    // CouponWallet 暫時設為 null，等類別建立後再修改
                    orderMap.put("couponWalletNo", null);
                    orderMap.put("orderDatetime", order.getOrderDatetime());
                    orderMap.put("orderStatus", order.getOrderStatus());
                    orderMap.put("orderTotal", order.getOrderTotal());
                    return orderMap;
                })
                .collect(Collectors.toList());
    }
	
	@GetMapping("/orders/status/{status}")
	public List<Map<String, Object>> getOrdersByStatus(@PathVariable String status) {
	    return orderRepository.findByOrderStatusOrderByDatetimeDesc(status).stream()  // ✅ 修改這一行
	            .map(order -> {
	                Map<String, Object> orderMap = new HashMap<>();
	                orderMap.put("orderNo", order.getOrderNo());
	                orderMap.put("memNo", order.getMemNo() != null ? order.getMemNo().getId() : null);
	                orderMap.put("couponWalletNo", null);
	                orderMap.put("orderDatetime", order.getOrderDatetime());
	                orderMap.put("orderStatus", order.getOrderStatus());
	                orderMap.put("orderTotal", order.getOrderTotal());
	                return orderMap;
	            })
	            .collect(Collectors.toList());
	}
	
	
	@GetMapping("/orders/{orderNo}")
	public Map<String, Object> getOrderByNo(@PathVariable Integer orderNo) {
	    // 根據訂單編號查詢訂單
	    Order order = orderRepository.findByOrderNo(orderNo);
	    
	    if (order == null) {
	        throw new RuntimeException("訂單不存在");
	    }
	    
	    Map<String, Object> orderMap = new HashMap<>();
	    orderMap.put("orderNo", order.getOrderNo());
	    orderMap.put("memNo", order.getMemNo() != null ? order.getMemNo().getId() : null);
	    orderMap.put("couponWalletNo", null); // CouponWallet 還沒做
	    orderMap.put("orderDatetime", order.getOrderDatetime());
	    orderMap.put("orderStatus", order.getOrderStatus());
	    orderMap.put("orderTotal", order.getOrderTotal());
	    orderMap.put("pointUsed", 0); // 積分功能暫不開放
	    orderMap.put("orderItems", new ArrayList<>()); // 訂單項目，等 OrderItem 做好後再加入
	    
	    return orderMap;
	}
	
	@GetMapping("/orders/member/{memNo}")
	public List<OrderDTO> getOrderByMemNo(@PathVariable Integer memNo){
	    List<OrderDTO> orders = orderService.getmemOrders(memNo);
	    return orders;
	}
	
	
	// ========== 購物車結帳串接 ========== //
	@PostMapping("/checkout-from-cart")
	public ResponseEntity<Map<String, Object>> checkoutFromCart(
	        @RequestBody CheckoutFromCartRequest request,
	        HttpSession session) {
	    try {
	        // 1. 獲取當前登入會員
	        Integer memNo = getCurrentMemNo(session);
	        if (memNo == null) {
	            Map<String, Object> errorResponse = new HashMap<>();
	            errorResponse.put("success", false);
	            errorResponse.put("message", "請先登入");
	            return ResponseEntity.status(401).body(errorResponse);
	        }
	        
	        log.info("購物車結帳請求：memNo={}", memNo);
	        
	        // 2. 驗證請求參數
	        if (request.getContactEmail() == null || request.getContactEmail().trim().isEmpty()) {
	            Map<String, Object> errorResponse = new HashMap<>();
	            errorResponse.put("success", false);
	            errorResponse.put("message", "請提供聯絡信箱");
	            return ResponseEntity.badRequest().body(errorResponse);
	        }
	        
	        // 3. 從購物車建立訂單
	        OrderDTO order = orderService.createOrderFromCart(
	            memNo, 
	            request.getContactEmail(), 
	            request.getContactPhone()
	        );
	        
	        // 4. 建立成功回應
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "訂單建立成功");
	        response.put("order", order);
	        response.put("orderNo", order.getOrderNo());
	        response.put("totalAmount", order.getOrderTotal());
	        
	        log.info("購物車結帳成功：memNo={}, orderNo={}", memNo, order.getOrderNo());
	        return ResponseEntity.ok(response);
	        
	    } catch (RuntimeException e) {
	        log.error("購物車結帳失敗：error={}", e.getMessage());
	        
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("success", false);
	        errorResponse.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(errorResponse);
	        
	    } catch (Exception e) {
	        log.error("購物車結帳系統錯誤", e);
	        
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("success", false);
	        errorResponse.put("message", "系統錯誤，請稍後再試");
	        return ResponseEntity.status(500).body(errorResponse);
	    }
	}
	
	
	// ========== 前台會員訂單功能 (OrderService) ========== //
	// ***** 查詢訂單詳情 ***** //
	@GetMapping("/orders/{orderNo}/detail")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable Integer orderNo) {
        try {
            log.info("查詢訂單詳情：orderNo={}", orderNo);
            OrderDTO order = orderService.getOrderDetail(orderNo);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("查詢訂單詳情失敗：orderNo={}", orderNo, e);
            return ResponseEntity.badRequest().build();
        }
    }
	
	
	// ***** 查詢會員的所有訂單 ***** //
	@GetMapping("/member/{memNo}")
    public ResponseEntity<List<OrderDTO>> getMemberOrders(@PathVariable Integer memNo) {
        try {
            log.info("查詢會員訂單：memNo={}", memNo);
            List<OrderDTO> orders = orderService.getmemOrders(memNo);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("查詢會員訂單失敗：memNo={}", memNo, e);
            return ResponseEntity.badRequest().build();
        }
    }
	
	// ***** 建立新訂單 (通用版本) ***** //
	@PostMapping("/create")
	public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequestDTO request) {
	    try {
	        log.info("建立新訂單：memNo={}", request.getMemNo());
	        
	        // 轉換為 Service 需要的格式
	        CreateOrderRequest serviceRequest = new CreateOrderRequest();
	        serviceRequest.setContactEmail(request.getContactEmail());
	        serviceRequest.setContactPhone(request.getContactPhone());
	        
	        // 轉換 DTO 類型
	        List<CreateOrderItemRequest> serviceOrderItems = request.getOrderItems().stream()
	                .map(dto -> {
	                    CreateOrderItemRequest serviceItem = new CreateOrderItemRequest();
	                    serviceItem.setProNo(dto.getProNo());
	                    serviceItem.setQuantity(dto.getQuantity());
	                    return serviceItem;
	                })
	                .collect(Collectors.toList());
	        
	        serviceRequest.setOrderItems(serviceOrderItems);
	        
	        OrderDTO order = orderService.createOrder(serviceRequest, request.getMemNo());
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "訂單建立成功");
	        response.put("order", order);
	        
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        log.error("建立訂單失敗：memNo={}", request.getMemNo(), e);
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", false);
	        response.put("message", "建立訂單失敗：" + e.getMessage());
	        
	        return ResponseEntity.badRequest().body(response);
	    }
	}
	
	
	
	// ***** 從購物車建立新訂單 ***** //
	@PostMapping("/create-from-cart")
    public ResponseEntity<Map<String, Object>> createOrderFromCart(
            @RequestBody CreateOrderFromCartRequest request) {
        try {
            log.info("從購物車建立訂單：memNo={}", request.getMemNo());
            
            OrderDTO order = orderService.createOrderFromCart(
                request.getMemNo(), 
                request.getContactEmail(), 
                request.getContactPhone()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "訂單建立成功");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("從購物車建立訂單失敗：memNo={}", request.getMemNo(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "建立訂單失敗：" + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
	
	// ***** 會員取消訂單 ***** //
	@PostMapping("/{orderNo}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Integer orderNo,
            @RequestBody MemberCancelRequest request) {
        try {
            log.info("會員取消訂單：orderNo={}, memNo={}", orderNo, request.getMemNo());
            
            boolean success = orderService.cancelOrder(orderNo, request.getMemNo(), request.getReason());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "訂單取消成功" : "訂單取消失敗");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("會員取消訂單失敗：orderNo={}", orderNo, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消訂單失敗：" + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
	
	
	// ***** OrderService 檢查訂單發貨狀態 ***** //
	@PostMapping("/admin/{orderNo}/check-shipping")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> checkOrderShipping(@PathVariable Integer orderNo) {
		   try {
		       log.info("檢查訂單發貨狀態：orderNo={}", orderNo);
		            
		       boolean allShipped = orderService.checkAndUpdateShippingStatus(orderNo);
		            
		       Map<String, Object> response = new HashMap<>();
		       response.put("success", true);
		       response.put("allShipped", allShipped);
		       response.put("message", allShipped ? "所有商品已發貨" : "部分商品尚未發貨");
		            
		       return ResponseEntity.ok(response);
		   } catch (Exception e) {
		       log.error("檢查訂單發貨狀態失敗：orderNo={}", orderNo, e);
		            
		       Map<String, Object> response = new HashMap<>();
		       response.put("success", false);
		       response.put("message", "檢查發貨狀態失敗：" + e.getMessage());
		            
		       return ResponseEntity.badRequest().body(response);
		   }
	}
	
	
	
	// ========== 付款相關功能 (PaymentService) ========== //
	
	// ***** 綠界付款回調 (系統對系統通訊) ***** //
	@PostMapping("/payment/callback")
    public ResponseEntity<String> handlePaymentCallback(@RequestParam Map<String, String> params) {
        try {
            log.info("收到付款回調：tradeNo={}", params.get("MerchantTradeNo"));
            String result = paymentService.handlePaymentCallback(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("處理付款回調失敗", e);
            return ResponseEntity.ok("0|系統錯誤");
        }
    }
	
	// ***** 綠界付款通知回調（系統對系統） 對應 application.properties 中的 ecpay.notify.url ***** //
	@PostMapping("/payment/notify")
	public ResponseEntity<String> handlePaymentNotify(@RequestParam Map<String, String> params) {
	    try {
	        log.info("收到綠界付款通知：tradeNo={}", params.get("MerchantTradeNo"));
	        
	        // 重用現有的處理邏輯
	        String result = paymentService.handlePaymentCallback(params);
	        return ResponseEntity.ok(result);
	        
	    } catch (Exception e) {
	        log.error("處理付款通知失敗", e);
	        return ResponseEntity.ok("0|系統錯誤");
	    }
	}
	
	// ***** 綠界付款完成返回（用戶瀏覽器跳轉） 對應 application.properties 中的 ecpay.return.url ***** //
	@GetMapping("/payment/return")
	public ResponseEntity<Map<String, Object>> handlePaymentReturn(
	        @RequestParam Map<String, String> params) {
	    try {
	        String tradeNo = params.get("MerchantTradeNo");
	        String rtnCode = params.get("RtnCode");
	        String rtnMsg = params.get("RtnMsg");
	        
	        log.info("用戶付款完成返回：tradeNo={}, rtnCode={}", tradeNo, rtnCode);
	        
	        // 建立返回結果，使用所有變數
	        Map<String, Object> result = new HashMap<>();
	        result.put("tradeNo", tradeNo);          // ✅ 使用 tradeNo
	        result.put("rtnCode", rtnCode);          // ✅ 使用 rtnCode  
	        result.put("success", "1".equals(rtnCode));
	        result.put("timestamp", System.currentTimeMillis());
	        
	        if ("1".equals(rtnCode)) {
	            result.put("message", "付款成功！");
	            result.put("status", "SUCCESS");
	        } else {
	            result.put("message", "付款失敗：" + (rtnMsg != null ? rtnMsg : "未知錯誤"));
	            result.put("status", "FAILED");
	            result.put("error", rtnMsg);         // ✅ 使用 rtnMsg
	        }
	        
	        return ResponseEntity.ok(result);
	        
	    } catch (Exception e) {
	        log.error("處理付款返回失敗", e);
	        
	        Map<String, Object> errorResult = new HashMap<>();
	        errorResult.put("success", false);
	        errorResult.put("status", "ERROR");
	        errorResult.put("message", "系統錯誤，請聯繫客服");
	        return ResponseEntity.ok(errorResult);
	    }
	}
	 
	 
	// ***** 查詢預購產品等待狀態 ***** //
	 @GetMapping("/payment/preorder/waiting")
	    public ResponseEntity<Map<String, Object>> getPreOrderWaiting() {
	        try {
	            log.info("查詢預購商品等待狀況");
	            Map<String, Object> waitingInfo = paymentService.getAllPreOrderWaitingInfo();
	            return ResponseEntity.ok(waitingInfo);
	        } catch (Exception e) {
	            log.error("查詢預購等待狀況失敗", e);
	            return ResponseEntity.badRequest().build();
	        }
	    }
	 
	 
	// ***** 查詢產品序號庫存 ***** //
	 @GetMapping("/payment/serial-stock/{proNo}")
	    public ResponseEntity<Map<String, Object>> getProductSerialStock(@PathVariable Integer proNo) {
	        try {
	            log.info("查詢產品序號庫存：proNo={}", proNo);
	            Map<String, Object> stock = paymentService.getProductSerialStock(proNo);
	            
	            if (stock != null) {
	                return ResponseEntity.ok(stock);
	            } else {
	                return ResponseEntity.notFound().build();
	            }
	        } catch (Exception e) {
	            log.error("查詢產品序號庫存失敗：proNo={}", proNo, e);
	            return ResponseEntity.badRequest().build();
	        }
	    }
	
	
	 // ***** 發起付款 (訂單詳情頁的「立即付款」按鈕) ***** //
	 @PostMapping("/{orderNo}/payment")
	    public ResponseEntity<?> initiatePayment(
	            @PathVariable Integer orderNo, 
	            HttpSession session) {
	        try {
	            Integer memNo = getMemNoFromSession(session);
	            String paymentForm = paymentService.createPayment(orderNo, memNo);
	            
	            return ResponseEntity.ok()
	                    .header("Content-Type", "text/html; charset=UTF-8")
	                    .body(paymentForm);
	                    
	        } catch (Exception e) {
	            log.error("發起付款失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 
	 // ***** 查詢付款狀態 (付款頁面的 AJAX 輪詢) ***** //
	 @GetMapping("/{orderNo}/payment/status")
	    public ResponseEntity<?> getPaymentStatus(
	            @PathVariable Integer orderNo,
	            HttpSession session) {
	        try {
	            Integer memNo = getMemNoFromSession(session);
	            
	            // 驗證訂單所有權
	            OrderDTO order = orderService.getOrderDetail(orderNo);
	            if (!order.getMemNo().equals(memNo)) {
	                return ResponseEntity.status(403)
	                        .body(Map.of("success", false, "message", "無權限查看此訂單"));
	            }
	            
	            Map<String, Object> paymentStatus = paymentService.getPaymentStatus(orderNo);
	            
	            if (paymentStatus != null) {
	                return ResponseEntity.ok(Map.of(
	                    "success", true,
	                    "orderNo", orderNo,
	                    "paymentStatus", paymentStatus,
	                    "orderStatus", order.getOrderStatus()
	                ));
	            } else {
	                return ResponseEntity.ok(Map.of(
	                    "success", false,
	                    "message", "找不到付款資訊",
	                    "orderNo", orderNo,
	                    "orderStatus", order.getOrderStatus()
	                ));
	            }
	            
	        } catch (Exception e) {
	            log.error("查詢付款狀態失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 重新付款 (付款失敗後的重試) ***** //
	 @PostMapping("/{orderNo}/payment/retry")
	    public ResponseEntity<?> retryPayment(
	            @PathVariable Integer orderNo,
	            HttpSession session) {
	        try {
	            Integer memNo = getMemNoFromSession(session);
	            String paymentForm = paymentService.retryPayment(orderNo, memNo);
	            
	            return ResponseEntity.ok()
	                    .header("Content-Type", "text/html; charset=UTF-8")
	                    .body(paymentForm);
	                    
	        } catch (Exception e) {
	            log.error("重新付款失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 取消付款 ***** //
	 @DeleteMapping("/{orderNo}/payment")
	    public ResponseEntity<?> cancelPayment(
	            @PathVariable Integer orderNo,
	            @RequestParam(required = false) String reason,
	            HttpSession session) {
	        try {
	            Integer memNo = getMemNoFromSession(session);
	            boolean success = paymentService.cancelPayment(orderNo, memNo, reason);
	            
	            return ResponseEntity.ok(Map.of(
	                "success", success,
	                "message", success ? "取消成功" : "取消失敗",
	                "orderNo", orderNo
	            ));
	            
	        } catch (Exception e) {
	            log.error("取消付款失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 查詢付款詳情 (可選功能，在訂單詳情頁顯示付款詳細資訊) ***** //
	 @GetMapping("/{orderNo}/payment/detail")
	    public ResponseEntity<?> getPaymentDetail(
	            @PathVariable Integer orderNo,
	            HttpSession session) {
	        try {
	            Integer memNo = getMemNoFromSession(session);
	            Map<String, Object> detail = paymentService.getOrderPaymentDetail(orderNo, memNo);
	            
	            return ResponseEntity.ok(detail);
	            
	        } catch (Exception e) {
	            log.error("查詢付款詳情失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 
	 // ***** 查詢付款進度 (可選功能，顯示付款流程進度條) ***** //
	 @GetMapping("/{orderNo}/payment/progress")
	    public ResponseEntity<?> getPaymentProgress(
	            @PathVariable Integer orderNo,
	            HttpSession session) {
	        try {
	            Integer memNo = getMemNoFromSession(session);
	            Map<String, Object> progress = paymentService.getPaymentProgress(orderNo, memNo);
	            
	            return ResponseEntity.ok(progress);
	            
	        } catch (Exception e) {
	            log.error("查詢付款進度失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 
	 
	
	// ========== 後台管理功能 (AdminOrderService) ========== //
	// ***** 後台查詢所有訂單 ***** //
	 @GetMapping("/admin/all")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<List<AdminOrderDTO>> getAllOrdersForAdmin() {
	        try {
	            log.info("後台查詢所有訂單");
	            List<AdminOrderDTO> orders = adminOrderService.getAllOrders();
	            return ResponseEntity.ok(orders);
	        } catch (Exception e) {
	            log.error("後台查詢所有訂單失敗", e);
	            return ResponseEntity.badRequest().build();
	        }
	    }
	
	// ***** 後台分頁查詢訂單 ***** //
	 @GetMapping("/admin/paged")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Map<String, Object>> getPagedOrdersForAdmin(
	         @RequestParam(defaultValue = "0") Integer page,
	         @RequestParam(defaultValue = "10") Integer size,
	         @RequestParam(defaultValue = "orderDatetime") String sortBy,
	         @RequestParam(defaultValue = "DESC") String sortDir) {
	     try {
	         log.info("後台分頁查詢訂單：page={}, size={}", page, size);
	         
	         // 用 Object 接收，避免類型衝突
	         Object response = adminOrderService.getPagedOrders(page, size, sortBy, sortDir);
	         
	         // 包裝為 Map 回傳
	         Map<String, Object> result = new HashMap<>();
	         result.put("success", true);
	         result.put("data", response);
	         
	         return ResponseEntity.ok(result);
	     } catch (Exception e) {
	         log.error("後台分頁查詢訂單失敗：page={}, size={}", page, size, e);
	         
	         Map<String, Object> errorResult = new HashMap<>();
	         errorResult.put("success", false);
	         errorResult.put("message", "查詢失敗：" + e.getMessage());
	         
	         return ResponseEntity.badRequest().body(errorResult);
	     }
	 }
	
	// ***** 後台按狀態查詢訂單 ***** //
	 @GetMapping("/admin/status/{status}")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<List<AdminOrderDTO>> getOrdersByStatusForAdmin(@PathVariable String status) {
	        try {
	            log.info("後台按狀態查詢訂單：status={}", status);
	            List<AdminOrderDTO> orders = adminOrderService.getOrdersByStatus(status);
	            return ResponseEntity.ok(orders);
	        } catch (Exception e) {
	            log.error("後台按狀態查詢訂單失敗：status={}", status, e);
	            return ResponseEntity.badRequest().build();
	        }
	    }
	 
	
	// ***** 後台多條件搜尋訂單 ***** //
	 @PostMapping("/admin/search")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Map<String, Object>> searchOrdersForAdmin(@RequestBody Map<String, Object> criteria) {
	     try {
	         log.info("後台多條件搜尋訂單：{}", criteria);
	         
	         // 直接傳入 Map
	         Map<String, Object> result = adminOrderService.searchOrders(criteria);
	         
	         return ResponseEntity.ok(result);
	     } catch (Exception e) {
	         log.error("後台多條件搜尋訂單失敗：{}", criteria, e);
	         
	         Map<String, Object> errorResponse = new HashMap<>();
	         errorResponse.put("success", false);
	         errorResponse.put("message", "搜尋失敗：" + e.getMessage());
	         
	         return ResponseEntity.badRequest().body(errorResponse);
	     }
	 }
	
	// ***** 後台訂單統計 ***** //
	 @GetMapping("/admin/statistics")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<OrderStatisticsDTO> getOrderStatisticsForAdmin() {
	        try {
	            log.info("後台查詢訂單統計");
	            OrderStatisticsDTO statistics = adminOrderService.getOrderStatistics();
	            return ResponseEntity.ok(statistics);
	        } catch (Exception e) {
	            log.error("後台查詢訂單統計失敗", e);
	            return ResponseEntity.badRequest().build();
	        }
	    }
	
	// ***** 後台期間訂單統計 ***** //
	 @GetMapping("/admin/statistics/period")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<OrderStatisticsDTO> getPeriodOrderStatisticsForAdmin(
	            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
	        try {
	            log.info("後台查詢期間訂單統計：{} ~ {}", startDate, endDate);
	            OrderStatisticsDTO statistics = adminOrderService.getOrderStatistics(startDate, endDate);
	            return ResponseEntity.ok(statistics);
	        } catch (Exception e) {
	            log.error("後台查詢期間訂單統計失敗：{} ~ {}", startDate, endDate, e);
	            return ResponseEntity.badRequest().build();
	        }
	    }
	
	// ***** 管理員取消訂單 ***** //
	 @PostMapping("/admin/{orderNo}/cancel")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<Map<String, Object>> adminCancelOrder(
	            @PathVariable Integer orderNo,
	            @RequestBody AdminCancelRequest request) {
	        try {
	            log.info("管理員取消訂單：orderNo={}, admin={}", orderNo, request.getAdminId());
	            
	            boolean success = adminOrderService.adminCancelOrder(orderNo, request.getReason(), request.getAdminId());
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", success);
	            response.put("message", success ? "管理員取消訂單成功" : "取消訂單失敗");
	            
	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            log.error("管理員取消訂單失敗：orderNo={}", orderNo, e);
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", false);
	            response.put("message", "取消訂單失敗：" + e.getMessage());
	            
	            return ResponseEntity.badRequest().body(response);
	        }
	    }
	 
	
	// ***** 管理員強制完成訂單 ***** //
	 @PostMapping("/admin/{orderNo}/force-complete")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<Map<String, Object>> forceCompleteOrder(
	            @PathVariable Integer orderNo,
	            @RequestBody AdminCompleteRequest request) {
	        try {
	            log.info("管理員強制完成訂單：orderNo={}, admin={}", orderNo, request.getAdminId());
	            
	            boolean success = adminOrderService.forceCompleteOrder(orderNo, request.getAdminId(), request.getReason());
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", success);
	            response.put("message", success ? "強制完成訂單成功" : "操作失敗");
	            
	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            log.error("管理員強制完成訂單失敗：orderNo={}", orderNo, e);
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", false);
	            response.put("message", "強制完成失敗：" + e.getMessage());
	            
	            return ResponseEntity.badRequest().body(response);
	        }
	    }
	
	// ***** 批量更新訂單狀態 ***** //
	 @PostMapping("/admin/batch-update-status")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Map<String, Object>> batchUpdateOrderStatus(
	         @RequestBody BatchUpdateRequest request) {
	     try {
	         log.info("批量更新訂單狀態：orderIds={}, newStatus={}, admin={}", 
	                 request.getOrderIds(), request.getNewStatus(), request.getAdminId());
	         
	         // 用 Object 接收
	         Object result = adminOrderService.bulkUpdateStatus(
	             request.getOrderIds(), 
	             request.getNewStatus(), 
	             request.getAdminId()
	         );
	         
	         // 包裝為 Map 回傳
	         Map<String, Object> response = new HashMap<>();
	         response.put("success", true);
	         response.put("data", result);
	         
	         return ResponseEntity.ok(response);
	     } catch (Exception e) {
	         log.error("批量更新訂單狀態失敗：{}", request, e);
	         
	         // 建立錯誤回應
	         Map<String, Object> errorResponse = new HashMap<>();
	         errorResponse.put("success", false);
	         errorResponse.put("totalCount", request.getOrderIds().size());
	         errorResponse.put("successCount", 0);
	         errorResponse.put("failureCount", request.getOrderIds().size());
	         errorResponse.put("message", "批量更新失敗：" + e.getMessage());
	         
	         return ResponseEntity.badRequest().body(errorResponse);
	     }
	 }
	 
	 
	 // ***** 付款統計儀表板 ***** //
	 @GetMapping("/admin/payment/dashboard")
	    public ResponseEntity<?> getPaymentDashboard(HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            
	            Map<String, Object> dashboard = Map.of(
	                "statistics", paymentService.getPaymentStatistics(),
	                "trends", paymentService.getPaymentTrends(7), // 最近7天趨勢
	                "preorderWaiting", paymentService.getAllPreOrderWaitingInfo()
	            );
	            
	            return ResponseEntity.ok(dashboard);
	            
	        } catch (Exception e) {
	            log.error("查詢付款儀表板失敗", e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 查詢付款統計 ***** //
	 @GetMapping("/admin/payment/statistics")
	    public ResponseEntity<?> getPaymentStatistics(HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            Map<String, Object> stats = paymentService.getPaymentStatistics();
	            
	            return ResponseEntity.ok(stats);
	            
	        } catch (Exception e) {
	            log.error("查詢付款統計失敗", e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 查詢付款趨勢 ***** //
	 @GetMapping("/admin/payment/trends")
	    public ResponseEntity<?> getPaymentTrends(
	            @RequestParam(defaultValue = "30") Integer days,
	            HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            List<Map<String, Object>> trends = paymentService.getPaymentTrends(days);
	            
	            return ResponseEntity.ok(Map.of(
	                "success", true,
	                "trends", trends,
	                "days", days
	            ));
	            
	        } catch (Exception e) {
	            log.error("查詢付款趨勢失敗", e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 查詢所有付款記錄 ***** //
	 @GetMapping("/admin/payment/records")
	    public ResponseEntity<?> getAllPaymentRecords(
	            @RequestParam(defaultValue = "0") Integer page,
	            @RequestParam(defaultValue = "20") Integer size,
	            @RequestParam Map<String, Object> filters,
	            HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            
	            // 移除分頁參數，只保留篩選條件
	            filters.remove("page");
	            filters.remove("size");
	            
	            Map<String, Object> result = paymentService.getAllPaymentRecords(filters, page, size);
	            return ResponseEntity.ok(result);
	            
	        } catch (Exception e) {
	            log.error("查詢所有付款記錄失敗", e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 重置卡單付款 ***** //
	 @PostMapping("/admin/{orderNo}/payment/reset")
	    public ResponseEntity<?> resetStuckPayment(
	            @PathVariable Integer orderNo,
	            @RequestParam String reason,
	            HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            
	            // ✅ 使用 PaymentService 原有方法 (不需要 adminId)
	            boolean success = paymentService.resetStuckPayment(orderNo, reason);
	            
	            return ResponseEntity.ok(Map.of(
	                "success", success,
	                "message", success ? "重置成功" : "重置失敗",
	                "orderNo", orderNo
	            ));
	            
	        } catch (Exception e) {
	            log.error("重置卡單付款失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 手動標記付款成功 ***** //
	 @PostMapping("/admin/{orderNo}/payment/mark-success")
	    public ResponseEntity<?> markPaymentSuccess(
	            @PathVariable Integer orderNo,
	            @RequestParam String reason,
	            HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            
	            // ✅ 傳入固定的 adminId
	            boolean success = paymentService.adminMarkPaymentSuccess(orderNo, FIXED_ADMIN_ID, reason);
	            
	            return ResponseEntity.ok(Map.of(
	                "success", success,
	                "message", success ? "標記成功" : "標記失敗",
	                "orderNo", orderNo
	            ));
	            
	        } catch (Exception e) {
	            log.error("手動標記付款成功失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 批量處理異常訂單 ***** //
	 @PostMapping("/admin/payment/batch-process")
	    public ResponseEntity<?> batchProcessOrders(
	            @RequestParam List<Integer> orderNos,
	            @RequestParam String action,
	            HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            
	            // ✅ 傳入固定的 adminId
	            Map<String, Object> result = paymentService.batchProcessAbnormalOrders(orderNos, action, FIXED_ADMIN_ID);
	            
	            return ResponseEntity.ok(result);
	            
	        } catch (Exception e) {
	            log.error("批量處理異常訂單失敗：orderNos={}", orderNos, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 // ***** 更新付款方式狀態 ***** //
	 @PostMapping("/admin/payment/method/{method}/toggle")
	    public ResponseEntity<?> togglePaymentMethod(
	            @PathVariable String method,
	            @RequestParam Boolean enabled,
	            HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            
	            // ✅ 傳入固定的 adminId
	            boolean success = paymentService.updatePaymentMethodStatus(method, enabled, FIXED_ADMIN_ID);
	            
	            return ResponseEntity.ok(Map.of(
	                "success", success,
	                "message", success ? "更新成功" : "更新失敗",
	                "method", method,
	                "enabled", enabled
	            ));
	            
	        } catch (Exception e) {
	            log.error("更新付款方式狀態失敗：method={}", method, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 系統清理 ***** //
	 @PostMapping("/admin/payment/system/cleanup")
	    public ResponseEntity<?> cleanupExpiredPayments(HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            
	            // ✅ 傳入固定的 adminId
	            Map<String, Object> result = paymentService.cleanupExpiredPayments(FIXED_ADMIN_ID);
	            
	            return ResponseEntity.ok(result);
	            
	        } catch (Exception e) {
	            log.error("清理過期付款記錄失敗", e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 查詢預購商品等待狀況 ***** //
	 @GetMapping("/admin/payment/preorder-waiting")
	    public ResponseEntity<?> getPreOrderWaitingInfo(HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            Map<String, Object> waitingInfo = paymentService.getAllPreOrderWaitingInfo();
	            
	            return ResponseEntity.ok(waitingInfo);
	            
	        } catch (Exception e) {
	            log.error("查詢預購等待狀況失敗", e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 // ***** 查詢商品序號庫存 ***** //
	 @GetMapping("/admin/payment/serial-stock/{proNo}")
	    public ResponseEntity<?> getProductSerialStock(
	            @PathVariable Integer proNo,
	            HttpSession session) {
	        try {
	            validateAdminPermission(session);
	            Map<String, Object> stockInfo = paymentService.getProductSerialStock(proNo);
	            
	            return ResponseEntity.ok(stockInfo);
	            
	        } catch (Exception e) {
	            log.error("查詢商品序號庫存失敗：proNo={}", proNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }

	
	
	// ========== 系統維護功能 (跨 Service 操作) ========== //
	// ***** PaymentService + AdminOrderService  手動觸發預購商品檢查 ***** //
	 @PostMapping("/admin/maintenance/check-preorder/{proNo}")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<Map<String, Object>> manualCheckPreOrder(@PathVariable Integer proNo) {
	        try {
	            log.info("管理員手動觸發預購商品檢查：proNo={}", proNo);
	            
	            // 調用 PaymentService 的預購處理
	            paymentService.handlePreOrderProductAvailable(proNo);
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("message", "預購商品檢查已觸發");
	            
	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            log.error("手動觸發預購商品檢查失敗：proNo={}", proNo, e);
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", false);
	            response.put("message", "預購檢查失敗：" + e.getMessage());
	            
	            return ResponseEntity.badRequest().body(response);
	        }
	    }
	 
	 
	 
	// ========== 輔助方法 ========== //
	// **** 用Session取得會員編號 (前台) **** // 
	 private Integer getMemNoFromSession(HttpSession session) {
	        Integer memNo = (Integer) session.getAttribute("memNo");
	        if (memNo == null) {
	            throw new IllegalStateException("會員未登入");
	        }
	        return memNo;
	    }
	 
	// **** 用Session中獲取當前登入會員編號 **** //
	 private Integer getCurrentMemNo(HttpSession session) {
		    try {
		        // 方法1：從 session attribute 中獲取
		        Object memNoObj = session.getAttribute("memNo");
		        if (memNoObj instanceof Integer) {
		            return (Integer) memNoObj;
		        }
		        
		        // 方法2：從會員物件中獲取
		        Object memberObj = session.getAttribute("member");
		        if (memberObj != null) {
		            // 假設您有 Member 類別，並且有 getId() 方法
		            // return ((Member) memberObj).getId();
		        }
		        
		        // 方法3：從其他可能的 session key 獲取
		        Object loginUserObj = session.getAttribute("loginUser");
		        if (loginUserObj instanceof Integer) {
		            return (Integer) loginUserObj;
		        }
		        
		        return null; // 未登入
		        
		    } catch (Exception e) {
		        log.warn("獲取當前會員編號失敗", e);
		        return null;
		    }
		}
	 
	// **** 管理員權限檢查 **** // 
	 private void validateAdminPermission(HttpSession session) {
	        // 檢查 Session 中是否有管理員標記
	        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
	        if (isAdmin == null || !isAdmin) {
	            throw new IllegalStateException("需要管理員權限");
	        }
	
	 }
	 
	 
	 
	// ========== Request/Response DTO 類別 ========== //
	// ** 從購物車建立訂單請求 **//
	@Data
	class CreateOrderFromCartRequest {
	    private Integer memNo;
	    private String contactEmail;
	    private String contactPhone;
	}
	
	@Data
	public static class CheckoutFromCartRequest {
	    private String contactEmail;
	    private String contactPhone;
	}
	

	//** 會員取消訂單請求 **//
	@Data
	class MemberCancelRequest {
	    private Integer memNo;
	    private String reason;
	}

	//** 管理員取消訂單請求 **//
	@Data
	class AdminCancelRequest {
	    private String adminId;
	    private String reason;
	}

	//** 管理員強制完成訂單請求 **//
	@Data
	class AdminCompleteRequest {
	    private String adminId;
	    private String reason;
	}

	//** 批量更新訂單狀態請求 **//
	@Data
	class BatchUpdateRequest {
	    private List<Integer> orderIds;
	    private String newStatus;
	    private String adminId;
	}

	//** 發送取消郵件請求 **//
	@Data
	class SendCancelEmailRequest {
	    private String reason;
	}
	


	//========== 輔助DTO類別 ========== //
	//***** 分頁訂單回應 ***** //
	@Data
	class PagedOrderResponse {
	    private List<AdminOrderDTO> orders;
	    private int currentPage;
	    private int totalPages;
	    private long totalElements;
	    private int pageSize;
	    private boolean hasNext;
	    private boolean hasPrevious;
	}
	
	//***** 訂單搜尋條件 ***** //
	@Data
	class OrderSearchCriteria {
	    private Integer orderNo;
	    private Integer memNo;
	    private String orderStatus;
	    private LocalDate startDate;
	    private LocalDate endDate;
	    private Integer minAmount;
	    private Integer maxAmount;
	    private String contactEmail;
	}
	
	

	
	
	//***** 批量操作結果 ***** //
	@Data
	class BatchOperationResult {
	    private int totalCount;
	    private int successCount;
	    private int failureCount;
	    private List<String> errors = new ArrayList<>();
	    
	    public boolean isAllSuccess() {
	        return failureCount == 0;
	    }
	    
	    public double getSuccessRate() {
	        if (totalCount == 0) return 0.0;
	        return (double) successCount / totalCount * 100;
	    }
	    
	}
	    
	@Data
	class CreateOrderRequestDTO {
	    private Integer memNo;
	    private String contactEmail;
	    private String contactPhone;
	    private List<CreateOrderItemRequestDTO> orderItems;
	}

	@Data  
	class CreateOrderItemRequestDTO {
	    private Integer proNo;
	    private Integer quantity;
	}
	
	
}
	    
	    