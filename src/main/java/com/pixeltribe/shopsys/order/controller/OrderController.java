package com.pixeltribe.shopsys.order.controller;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.member.dto.MemberAdminDto;
import com.pixeltribe.membersys.member.model.MemService;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.cart.model.CartService;
import com.pixeltribe.shopsys.cart.model.CartValidationResponse;
import com.pixeltribe.shopsys.order.model.*;
import com.pixeltribe.shopsys.orderItem.model.CreateOrderItemRequest;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService;
import com.pixeltribe.shopsys.product.model.ProductManageDTO;
import com.pixeltribe.shopsys.product.model.ProductRepository;
import com.pixeltribe.shopsys.product.model.ProductService;
import com.pixeltribe.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController

@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {
	
	@Autowired
    private CartService cartService;
	
	@Autowired
	private MemService memberService;  // 會員服務

	@Autowired  
	private ProductService productService;  // 商品服務
	
	@Autowired
    private ProductRepository productRepository;

	@Autowired
	private OrderItemService orderItemService;  // 訂單項目服務
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderService orderService;   // 前台訂單服務
	
	@Autowired
    private AdminOrderService adminOrderService; // 後台管理服務
    
    @Autowired
    private PaymentService paymentService;       // 付款處理服務
    
    @Autowired
    private JwtUtil jwtUtil;  // 驗證用
    
    @Autowired
    private EmailService emailService;  // 驗證用
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    
    private static final String FIXED_ADMIN_ID = "ADMIN_USER";
    
    public OrderController() {
        System.out.println("🔥🔥🔥 OrderController 被載入了！🔥🔥🔥");
    }
    
    
    // 在類別開頭加入這個測試方法
    @GetMapping("/test-payment")
    public ResponseEntity<String> testPayment() {
        System.out.println("🔥🔥🔥 測試路由被調用了！🔥🔥🔥");
        return ResponseEntity.ok("測試成功");
    }
    
 // *** 測試郵件功能 (開發測試用) *** //
    @GetMapping("/check-order-email/{orderNo}")
    @ResponseBody
    public ResponseEntity<String> checkOrderEmail(@PathVariable Integer orderNo) {
        try {
            log.info("🔍 檢查訂單信箱：orderNo={}", orderNo);
            
            // 1. 從訂單取得信箱
            OrderDTO order = orderService.getOrderDetail(orderNo);
            String orderEmail = order.getContactEmail();
            
            // 2. 從 Redis 取得客戶指定信箱
            String redisKey = "order:contact:" + orderNo;
            String redisEmail = (String) redisTemplate.opsForValue().get(redisKey);
            
            String response = String.format(
                "📧 信箱來源檢查報告\n" +
                "═══════════════════════\n" +
                "📋 訂單編號：%d\n" +
                "📊 訂單狀態：%s\n" +
                "📧 目前使用信箱：%s\n" +
                "💾 Redis儲存信箱：%s\n" +
                "═══════════════════════\n" +
                "🎯 結論：%s",
                orderNo,
                order.getOrderStatus(),
                orderEmail,
                redisEmail != null ? redisEmail : "無",
                redisEmail != null && redisEmail.equals(orderEmail) ? 
                    "✅ 正確使用客戶指定信箱" : "❌ 信箱來源有問題"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("檢查訂單信箱失敗：orderNo={}", orderNo, e);
            return ResponseEntity.ok("❌ 檢查失敗：" + e.getMessage());
        }
    }

    // *** 測試付款回調功能 (開發測試用) *** //
    @GetMapping("/test-payment-callback/{orderNo}")
    @ResponseBody
    public ResponseEntity<String> testPaymentCallback(@PathVariable Integer orderNo) {
        try {
            log.info("🧪 測試付款回調：orderNo={}", orderNo);
            
            // 1. 檢查訂單狀態
            OrderDTO order = orderService.getOrderDetail(orderNo);
            if (!"PENDING".equals(order.getOrderStatus())) {
                return ResponseEntity.ok("❌ 訂單狀態不是 PENDING：" + order.getOrderStatus() + 
                                       "\n如需重複測試，請先重置訂單狀態");
            }
            
            // 🔍 1.5 詳細檢查信箱來源
            String contactEmail = order.getContactEmail();
            String emailSource = checkEmailSource(orderNo, contactEmail);
            
            log.info("📧 信箱檢查：orderNo={}, email={}, source={}", orderNo, contactEmail, emailSource);
            
            // 2. 更新為付款中
            orderService.updateOrderStatus(orderNo, "PAYING");
            log.info("📝 訂單狀態已更新為：PAYING");
            
            // 3. 模擬綠界回調參數
            String mockTradeNo = "O" + order.getOrderNo();
            Map<String, Object> paymentInfo = new HashMap<>();
            paymentInfo.put("orderNo", orderNo);
            paymentInfo.put("tradeNo", mockTradeNo);
            paymentInfo.put("amount", order.getOrderTotal());
            paymentInfo.put("status", "PAYING");
            paymentInfo.put("method", "ECPAY");
            paymentInfo.put("createdAt", System.currentTimeMillis());
            paymentInfo.put("memNo", order.getMemNo());

            String redisKey = "payment:" + orderNo;
            redisTemplate.opsForHash().putAll(redisKey, paymentInfo);
            redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
            log.info("🧪 測試：已手動存入付款資訊到 Redis，tradeNo={}", mockTradeNo);
            
            // 4. 直接調用付款成功處理
            Map<String, String> fakeEcpayResponse = new HashMap<>();
            fakeEcpayResponse.put("MerchantID", "2000132");
            fakeEcpayResponse.put("MerchantTradeNo", mockTradeNo);
            fakeEcpayResponse.put("RtnCode", "1");  // 👈 加入這行！
            fakeEcpayResponse.put("RtnMsg", "Succeeded");  // 👈 加入這行！
            fakeEcpayResponse.put("TradeAmt", order.getOrderTotal().toString());  // 👈 加入這行！
            fakeEcpayResponse.put("PaymentType", "Test_CreditCard");  // 👈 加入這行！
            fakeEcpayResponse.put("PaymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));  // 👈 加入這行！
            fakeEcpayResponse.put("TradeNo", "TEST" + System.currentTimeMillis());  // 👈 加入這行！
            fakeEcpayResponse.put("CheckMacValue", "TEST_CHECKSUM");  // 👈 最重要！

            String result = paymentService.handlePaymentCallback(fakeEcpayResponse);
            
            log.info("🎭 模擬綠界回調參數：{}", fakeEcpayResponse);
            
            // 5. 重新查詢訂單狀態
            OrderDTO updatedOrder = orderService.getOrderDetail(orderNo);
            
            String response = String.format(
                "🎉 測試付款回調完成！\n" +
                "═══════════════════════\n" +
                "📋 訂單編號：%d\n" +
                "📧 聯絡信箱：%s\n" +
                "🔍 信箱來源：%s\n" +
                "💰 訂單金額：NT$ %d\n" +
                "📊 最終狀態：%s\n" +
                "🔄 處理結果：%s\n" +
                "═══════════════════════\n" +
                "✅ 請檢查信箱 %s 是否收到序號郵件！\n" +
                "📱 (記得檢查垃圾郵件匣)",
                orderNo,
                updatedOrder.getContactEmail(),
                emailSource,
                updatedOrder.getOrderTotal(),
                updatedOrder.getOrderStatus(),
                result,
                updatedOrder.getContactEmail()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("測試付款回調失敗：orderNo={}", orderNo, e);
            return ResponseEntity.ok("❌ 測試失敗：" + e.getMessage() + 
                                   "\n💡 提示：請確認訂單存在且狀態為 PENDING");
        }
    }

    // *** 重置訂單狀態 (方便重複測試) *** //
    @GetMapping("/reset-order-status/{orderNo}")
    @ResponseBody  
    public ResponseEntity<String> resetOrderStatus(@PathVariable Integer orderNo) {
        try {
            log.info("🔄 重置訂單狀態：orderNo={}", orderNo);
            
            // 1. 檢查訂單是否存在
            OrderDTO order = orderService.getOrderDetail(orderNo);
            
            // 2. 🔥 強制重置為待付款狀態（繞過狀態轉換檢查）
            // 直接調用 OrderRepository 更新，不走 updateOrderStatus
            Order orderEntity = orderRepository.findByOrderNo(orderNo);
            if (orderEntity != null) {
                orderEntity.setOrderStatus("PENDING");
                orderRepository.save(orderEntity);
                log.info("強制重置訂單狀態為 PENDING：orderNo={}", orderNo);
            }
            
            // 3. 清理 Redis 付款記錄
            String paymentRedisKey = "payment:" + orderNo;
            Boolean paymentDeleted = redisTemplate.delete(paymentRedisKey);
            
            String response = String.format(
                "✅ 訂單重置完成！\n" +
                "═══════════════════════\n" +
                "📋 訂單編號：%d\n" +
                "📊 新狀態：PENDING (強制重置)\n" +
                "🗑️ Redis清理：%s\n" +
                "═══════════════════════\n" +
                "🎯 現在可以重新測試付款流程了！",
                orderNo,
                paymentDeleted ? "成功" : "無需清理"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("重置訂單狀態失敗：orderNo={}", orderNo, e);
            return ResponseEntity.ok("❌ 重置失敗：" + e.getMessage());
        }
    }

    // 🔍 檢查信箱來源的輔助方法
    private String checkEmailSource(Integer orderNo, String currentEmail) {
        try {
            // 檢查 Redis 中是否有儲存客戶指定信箱
            String redisKey = "order:contact:" + orderNo;
            String redisEmail = (String) redisTemplate.opsForValue().get(redisKey);
            
            if (redisEmail != null && !redisEmail.trim().isEmpty()) {
                if (redisEmail.equals(currentEmail)) {
                    return "✅ Redis儲存的客戶指定信箱";
                } else {
                    return "⚠️ Redis有信箱但不一致: " + redisEmail;
                }
            } else {
                return "❌ Redis中無客戶信箱，使用會員預設信箱";
            }
        } catch (Exception e) {
            return "🔥 Redis查詢失敗: " + e.getMessage();
        }
    }
    
    
    
    
    
    
    
    
    
    
    // 手動驗證 JWT 的方法（不影響 Security 設定）
    private Integer extractMemNoFromRequest(HttpServletRequest request) {
        try {
            // 1. 嘗試從 Authorization Header 取得 JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    Integer memberId = jwtUtil.extractMemberIdFromMemberToken(token);
                    if (memberId != null) {
                        log.debug("從 JWT 取得會員編號：{}", memberId);
                        return memberId;
                    }
                }
            }
            
            // 2. 備用方案：從 LoginInterceptor 取得（如果有的話）
            Object currentIdObj = request.getAttribute("currentId");
            if (currentIdObj instanceof Integer) {
                Integer memberId = (Integer) currentIdObj;
                log.debug("從 Interceptor 取得會員編號：{}", memberId);
                return memberId;
            }
            
            return null;
            
        } catch (Exception e) {
            log.warn("提取會員編號失敗", e);
            return null;
        }
    }
	
	
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
	    
		System.out.println("=== OrderController.getOrderByNo 被調用，orderNo=" + orderNo + " ===");
		
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
	        HttpServletRequest httpRequest) {  // 改用 HttpServletRequest
	    try {
	        // 1. 手動驗證並取得會員編號
	        Integer memNo = extractMemNoFromRequest(httpRequest);
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
	        
	        
	        // 結帳前強制驗證購物車庫存
	        try {
	        	CartValidationResponse validation = cartService.validateCartForCheckout(memNo, null);
	            
	            if (!validation.isValid()) {
	            	log.warn("購物車庫存驗證失敗：memNo={}, issues={}", memNo, validation.getIssues());
	                
	                Map<String, Object> errorResponse = new HashMap<>();
	                errorResponse.put("success", false);
	                errorResponse.put("message", "購物車商品庫存不足，無法結帳");
	                errorResponse.put("issues", validation.getIssues());
	                
	                return ResponseEntity.badRequest().body(errorResponse);
	            }
	        } catch (Exception e) {
	        	log.error("購物車庫存驗證異常：memNo={}, error={}", memNo, e.getMessage());
	            
	            Map<String, Object> errorResponse = new HashMap<>();
	            errorResponse.put("success", false);
	            errorResponse.put("message", "庫存驗證失敗，請重新整理購物車");
	            
	            return ResponseEntity.badRequest().body(errorResponse);
	        }
	        

	        
	        // 3. 建立訂單（只有通過庫存驗證才會執行到這裡）
	        OrderDTO order = orderService.createOrderFromCart(
	            memNo, 
	            request.getContactEmail(), 
	            request.getContactPhone()
	        );
	        
	        // 4. 回應
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
        
		System.out.println("=== getOrderDetail 被調用，orderNo=" + orderNo + " ===");
		
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
        
		System.out.println("=== getMemberOrders 被調用，memNo=" + memNo + " ===");
		
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
	// *** 移到PaymentCallbackController *** //
	
	
	// ***** 綠界付款完成返回（用戶瀏覽器跳轉） 對應 application.properties 中的 ecpay.return.url ***** //
	// *** 移到PaymentCallbackController *** //
	 
	 
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
	 @PostMapping("/orders/{orderNo}/payment")
	    public ResponseEntity<?> initiatePayment(
	            @PathVariable Integer orderNo, 
	            HttpServletRequest httpRequest) {
		 
		 	// ✅ 最基本的除錯 - 一定要看到這行！
		    System.out.println("🔥🔥🔥 OrderController.initiatePayment 被調用了！orderNo=" + orderNo + " 🔥🔥🔥");
		    
		 
	        try {
	        	
	        	// 加入詳細除錯日誌
	            log.info("=== 開始發起付款流程 ===");
	            log.info("訂單編號：{}", orderNo);
	        	
	        	
	        	Integer memNo = extractMemNoFromRequest(httpRequest);
	        	if (memNo == null) {
	        		log.error("JWT 驗證失敗，無法取得會員編號");
	                return ResponseEntity.status(401)
	                        .body(Map.of("success", false, "message", "請先登入"));
	            }
	        	
	        	log.info("會員編號驗證成功：{}", memNo);
	            
	            // ✅ 檢查 PaymentService 是否正常
	            log.info("PaymentService 狀態檢查：{}", paymentService != null ? "正常" : "NULL");
	            
	            if (paymentService == null) {
	                log.error("PaymentService 注入失敗！");
	                return ResponseEntity.status(500)
	                        .body(Map.of("success", false, "message", "付款服務初始化失敗"));
	            }
	            
	         // ✅ 調用付款服務
	            log.info("準備調用 PaymentService.initiatePayment()");
	            String paymentForm = paymentService.initiatePayment(orderNo);
	            log.info("PaymentService 調用完成，返回表單長度：{}", 
	                    paymentForm != null ? paymentForm.length() : "NULL");
	            
	            if (paymentForm == null || paymentForm.trim().isEmpty()) {
	                log.error("PaymentService 返回空的付款表單");
	                return ResponseEntity.badRequest()
	                        .body(Map.of("success", false, "message", "生成付款表單失敗"));
	            }
	            
	            log.info("=== 付款流程成功完成 ===");
	            return ResponseEntity.ok()
	                    .header("Content-Type", "text/html; charset=UTF-8")
	                    .body(paymentForm);
	                    
	        } catch (Exception e) {
	            log.error("=== 發起付款失敗 ===");
	            log.error("訂單編號：{}", orderNo);
	            log.error("錯誤類型：{}", e.getClass().getSimpleName());
	            log.error("錯誤訊息：{}", e.getMessage());
	            log.error("完整堆疊：", e);
	            
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	            
	        	
	 
	 
	 // ***** 查詢付款狀態 (付款頁面的 AJAX 輪詢) ***** //
	 @GetMapping("/orders/{orderNo}/payment/status")
	    public ResponseEntity<?> getPaymentStatus(
	            @PathVariable Integer orderNo,
	            HttpServletRequest httpRequest) {
	        try {
	        	Integer memNo = extractMemNoFromRequest(httpRequest);
	            if (memNo == null) {
	                return ResponseEntity.status(401)
	                        .body(Map.of("success", false, "message", "請先登入"));
	            }
	            
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
	 
	// ***** 手動觸發付款超時檢查 ***** //
	 @GetMapping("/debug/check-timeout")
	 public ResponseEntity<?> manualCheckTimeout() {
	     try {
	         log.info("🔧 手動觸發付款超時檢查");
	         
	         // 直接調用 PaymentService 的超時檢查方法
	         paymentService.handlePaymentTimeout();
	         
	         // 查看當前所有付款狀態
	         Set<String> paymentKeys = redisTemplate.keys("payment:*");
	         List<Map<String, Object>> payments = new ArrayList<>();
	         
	         if (paymentKeys != null) {
	             for (String key : paymentKeys) {
	                 Map<Object, Object> paymentInfo = redisTemplate.opsForHash().entries(key);
	                 if (!paymentInfo.isEmpty()) {
	                     Map<String, Object> payment = new HashMap<>();
	                     payment.put("orderNo", paymentInfo.get("orderNo"));
	                     payment.put("status", paymentInfo.get("status"));
	                     payment.put("createdAt", paymentInfo.get("createdAt"));
	                     
	                     // 計算時間差
	                     Long createdAt = (Long) paymentInfo.get("createdAt");
	                     if (createdAt != null) {
	                         long diffMinutes = (System.currentTimeMillis() - createdAt) / 60000;
	                         payment.put("ageMinutes", diffMinutes);
	                         payment.put("isTimeout", diffMinutes > 30);
	                     }
	                     
	                     payments.add(payment);
	                 }
	             }
	         }
	         
	         Map<String, Object> result = Map.of(
	             "message", "超時檢查完成",
	             "totalPayments", payments.size(),
	             "payments", payments,
	             "timestamp", System.currentTimeMillis()
	         );
	         
	         return ResponseEntity.ok(result);
	         
	     } catch (Exception e) {
	         log.error("手動檢查超時失敗", e);
	         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	     }
	 }
	 
	 
	 
	 
	 // ***** 重新付款 (付款失敗後的重試) ***** //
	 @PostMapping("/orders/{orderNo}/payment/retry")
	    public ResponseEntity<?> retryPayment(
	            @PathVariable Integer orderNo,
	            HttpServletRequest httpRequest) {
	        try {
	        	Integer memNo = extractMemNoFromRequest(httpRequest);
	            if (memNo == null) {
	                return ResponseEntity.status(401)
	                        .body(Map.of("success", false, "message", "請先登入"));
	            }

	            
	            String paymentForm = paymentService.initiatePayment(orderNo);
	            
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
	 @DeleteMapping("/orders/{orderNo}/payment")
	    public ResponseEntity<?> cancelPayment(
	            @PathVariable Integer orderNo,
	            @RequestParam(required = false) String reason,
	            HttpServletRequest httpRequest) {
	        try {
	        	Integer memNo = extractMemNoFromRequest(httpRequest);
	            if (memNo == null) {
	                return ResponseEntity.status(401)
	                        .body(Map.of("success", false, "message", "請先登入"));
	            }
	        	
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
	 @GetMapping("/orders/{orderNo}/payment/detail")
	    public ResponseEntity<?> getPaymentDetail(
	            @PathVariable Integer orderNo,
	            HttpServletRequest httpRequest) {
	        try {
	        	Integer memNo = extractMemNoFromRequest(httpRequest);
	            if (memNo == null) {
	                return ResponseEntity.status(401)
	                        .body(Map.of("success", false, "message", "請先登入"));
	            }
	        	
	            Map<String, Object> detail = paymentService.getOrderPaymentDetail(orderNo, memNo);
	            
	            return ResponseEntity.ok(detail);
	            
	        } catch (Exception e) {
	            log.error("查詢付款詳情失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 
	 // ***** 查詢付款進度 (可選功能，顯示付款流程進度條) ***** //
	 @GetMapping("/orders/{orderNo}/payment/progress")
	    public ResponseEntity<?> getPaymentProgress(
	            @PathVariable Integer orderNo,
	            HttpServletRequest httpRequest) {
	        try {
	        	Integer memNo = extractMemNoFromRequest(httpRequest);
	            if (memNo == null) {
	                return ResponseEntity.status(401)
	                        .body(Map.of("success", false, "message", "請先登入"));
	            }
	            Map<String, Object> progress = paymentService.getPaymentProgress(orderNo, memNo);
	            
	            return ResponseEntity.ok(progress);
	            
	        } catch (Exception e) {
	            log.error("查詢付款進度失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	 
	 
	
	// ========== 後台管理功能 (AdminOrderService) ========== //
	// ***** 會員搜尋 API (新增訂單功能需要) ***** //
	 @GetMapping("/admin/members/search")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<List<Map<String, Object>>> searchMembers(@RequestParam String q) {
	     try {
	         log.info("搜尋會員：query={}", q);
	         
	         // 使用 MemberService 進行分頁搜尋，取第一頁的結果
	         // 主要搜尋會員帳號，也搜尋姓名和信箱作為備用
	         Pageable pageable = PageRequest.of(0, 10); // 搜尋前10筆結果
	         Page<MemberAdminDto> memberPage = memberService.findAllAdminMembers(q, pageable);
	         
	         // 轉換為前端需要的格式，重新排序讓帳號匹配的優先顯示
	         List<Map<String, Object>> members = memberPage.getContent().stream()
	             .map(member -> {
	                 Map<String, Object> memberMap = new HashMap<>();
	                 memberMap.put("id", member.getId());
	                 memberMap.put("memName", member.getMemName());
	                 memberMap.put("memEmail", member.getMemEmail());
	                 memberMap.put("memAccount", member.getMemAccount());
	                 
	                 // 計算匹配優先級（帳號完全匹配 > 帳號包含 > 姓名包含 > 信箱包含）
	                 int priority = 0;
	                 String queryLower = q.toLowerCase();
	                 if (member.getMemAccount() != null) {
	                     if (member.getMemAccount().toLowerCase().equals(queryLower)) {
	                         priority = 1000; // 帳號完全匹配最優先
	                     } else if (member.getMemAccount().toLowerCase().contains(queryLower)) {
	                         priority = 800; // 帳號部分匹配
	                     }
	                 }
	                 if (priority == 0 && member.getMemName() != null && member.getMemName().toLowerCase().contains(queryLower)) {
	                     priority = 600; // 姓名匹配
	                 }
	                 if (priority == 0 && member.getMemEmail() != null && member.getMemEmail().toLowerCase().contains(queryLower)) {
	                     priority = 400; // 信箱匹配
	                 }
	                 
	                 memberMap.put("priority", priority);
	                 return memberMap;
	             })
	             .sorted((m1, m2) -> Integer.compare((Integer)m2.get("priority"), (Integer)m1.get("priority")))
	             .peek(memberMap -> memberMap.remove("priority")) // 移除排序用的priority
	             .collect(Collectors.toList());
	         
	         return ResponseEntity.ok(members);
	         
	     } catch (Exception e) {
	         log.error("搜尋會員失敗：query={}", q, e);
	         return ResponseEntity.badRequest().body(new ArrayList<>());
	     }
	 }

	 // ***** 商品搜尋 API (新增訂單功能需要) ***** //
	 @GetMapping("/admin/products/search")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<List<Map<String, Object>>> searchProducts(@RequestParam String q) {
	     try {
	         log.info("搜尋商品：query={}", q);
	         
	         // 使用 ProductService 的複合查詢方法
	         // 搜尋商品名稱包含關鍵字、已上架的商品
	         List<ProductManageDTO> products = productService.findProductsByComplexQuery(
	             q,        // proName - 商品名稱關鍵字
	             null,     // minPrice - 不限制最低價格
	             null,     // maxPrice - 不限制最高價格  
	             null,     // proStatus - 不限制商品狀態
	             null,     // mallTagNo - 不限制分類
	             '0'       // proIsMarket - 只搜尋已上架的商品
	         );
	         
	         // 轉換為前端需要的格式，只取前20筆
	         List<Map<String, Object>> productList = products.stream()
	             .limit(20) // 限制搜尋結果數量
	             .map(product -> {
	                 Map<String, Object> productMap = new HashMap<>();
	                 productMap.put("proNo", product.getId()); // 使用 getProNo()
	                 productMap.put("proName", product.getProName()); // 使用 getProName()
	                 productMap.put("proPrice", product.getProPrice() != null ? product.getProPrice() : 0);
	                 return productMap;
	             })
	             .collect(Collectors.toList());
	         
	         return ResponseEntity.ok(productList);
	         
	     } catch (Exception e) {
	         log.error("搜尋商品失敗：query={}", q, e);
	         return ResponseEntity.badRequest().body(new ArrayList<>());
	     }
	 }

	 // ***** 會員詳細資訊 API (修改訂單功能需要) ***** //
	 @GetMapping("/admin/members/{memNo}")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Map<String, Object>> getMemberDetail(@PathVariable Integer memNo) {
	     try {
	         log.info("查詢會員詳細資訊：memNo={}", memNo);
	         
	         // 使用 MemberService 查詢會員資料
	         Member member = memberService.getOneMem(memNo);
	         
	         if (member == null) {
	             return ResponseEntity.notFound().build();
	         }
	         
	         // 轉換為前端需要的格式
	         Map<String, Object> memberInfo = new HashMap<>();
	         memberInfo.put("id", member.getId());
	         memberInfo.put("memName", member.getMemName() != null ? member.getMemName() : "未知");
	         memberInfo.put("memEmail", member.getMemEmail() != null ? member.getMemEmail() : "無");
	         memberInfo.put("memPhone", member.getMemPhone() != null ? member.getMemPhone() : "無");
	         memberInfo.put("memAccount", member.getMemAccount() != null ? member.getMemAccount() : "無");
	         
	         return ResponseEntity.ok(memberInfo);
	         
	     } catch (Exception e) {
	         log.error("查詢會員詳細資訊失敗：memNo={}", memNo, e);
	         return ResponseEntity.badRequest().build();
	     }
	 }

	 // ***** 修改訂單 API (修改訂單功能需要) ***** //
	 @PutMapping("/admin/orders/{orderNo}")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Map<String, Object>> updateOrder(
	         @PathVariable Integer orderNo,
	         @RequestBody Map<String, Object> updateData) {
	     try {
	         log.info("修改訂單：orderNo={}, data={}", orderNo, updateData);
	         
	         // 檢查訂單是否存在
	         Order order = orderRepository.findByOrderNo(orderNo);
	         if (order == null) {
	             Map<String, Object> errorResponse = new HashMap<>();
	             errorResponse.put("success", false);
	             errorResponse.put("message", "訂單不存在");
	             return ResponseEntity.badRequest().body(errorResponse);
	         }
	         
	         // 記錄聯絡資訊修改（只記錄，不儲存到資料庫）
	         String contactEmail = (String) updateData.get("contactEmail");
	         String contactPhone = (String) updateData.get("contactPhone");
	         
	         // 記錄管理員操作（用於日誌追蹤）
	         log.info("ADMIN_ORDER_UPDATE|orderNo={}|admin={}|contactEmail={}|contactPhone={}|timestamp={}", 
	                 orderNo, FIXED_ADMIN_ID, contactEmail, contactPhone, System.currentTimeMillis());
	         
	         // 由於不能修改資料庫結構，聯絡資訊的修改只做記錄
	         // 實際的聯絡資訊應該儲存在其他地方（如 Redis 或其他表格）
	         
	         Map<String, Object> response = new HashMap<>();
	         response.put("success", true);
	         response.put("message", "訂單聯絡資訊已記錄");
	         response.put("orderNo", orderNo);
	         response.put("note", "聯絡資訊已記錄到系統日誌中");
	         
	         return ResponseEntity.ok(response);
	         
	     } catch (Exception e) {
	         log.error("修改訂單失敗：orderNo={}", orderNo, e);
	         
	         Map<String, Object> errorResponse = new HashMap<>();
	         errorResponse.put("success", false);
	         errorResponse.put("message", "修改訂單失敗：" + e.getMessage());
	         
	         return ResponseEntity.badRequest().body(errorResponse);
	     }
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> getPaymentDashboard() {
	        try {
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> getPaymentStatistics() {
	        try {
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> getPaymentTrends(
	            @RequestParam(defaultValue = "30") Integer days) {
	        try {
	            
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
	 
	// ***** 查看特定訂單的詳細狀態 ***** //
	 @GetMapping("/debug/order-status/{orderNo}")
	 public ResponseEntity<?> getOrderStatus(@PathVariable Integer orderNo) {
	     try {
	         // 從資料庫查詢訂單狀態
	         OrderDTO order = orderService.getOrderDetail(orderNo);
	         
	         // 從 Redis 查詢付款狀態
	         Map<String, Object> paymentStatus = paymentService.getPaymentStatus(orderNo);
	         
	         // 從 Redis 直接查詢原始資料
	         String redisKey = "payment:" + orderNo;
	         Map<Object, Object> rawRedisData = redisTemplate.opsForHash().entries(redisKey);
	         
	         Map<String, Object> result = new HashMap<>();
	         result.put("orderNo", orderNo);
	         result.put("databaseStatus", order.getOrderStatus());
	         result.put("paymentStatus", paymentStatus);
	         result.put("rawRedisData", rawRedisData);
	         result.put("timestamp", System.currentTimeMillis());
	         
	         // 檢查是否超時
	         if (rawRedisData.containsKey("createdAt")) {
	             Long createdAt = (Long) rawRedisData.get("createdAt");
	             long ageMinutes = (System.currentTimeMillis() - createdAt) / 60000;
	             result.put("ageMinutes", ageMinutes);
	             result.put("shouldTimeout", ageMinutes > 30);
	         }
	         
	         return ResponseEntity.ok(result);
	         
	     } catch (Exception e) {
	         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	     }
	 }
	 
	 
	 
	 // ***** 查詢所有付款記錄 ***** //
	 @GetMapping("/admin/payment/records")
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> getAllPaymentRecords(
	            @RequestParam(defaultValue = "0") Integer page,
	            @RequestParam(defaultValue = "20") Integer size,
	            @RequestParam Map<String, Object> filters) {
	        try {
	            
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> resetStuckPayment(
	            @PathVariable Integer orderNo,
	            @RequestParam String reason) {
	        try {
	            
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> markPaymentSuccess(
	            @PathVariable Integer orderNo,
	            @RequestParam String reason) {
	        try {
	            
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> batchProcessOrders(
	            @RequestParam List<Integer> orderNos,
	            @RequestParam String action) {
	        try {
	            
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> togglePaymentMethod(
	            @PathVariable String method,
	            @RequestParam Boolean enabled) {
	        try {
	            
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> cleanupExpiredPayments() {
	        try {
	            
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> getPreOrderWaitingInfo() {
	        try {
	            
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
	 @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> getProductSerialStockForAdmin(
	            @PathVariable Integer proNo) {
	        try {
	            
	            Map<String, Object> stockInfo = paymentService.getProductSerialStock(proNo);
	            
	            return ResponseEntity.ok(stockInfo);
	            
	        } catch (Exception e) {
	            log.error("查詢商品序號庫存失敗：proNo={}", proNo, e);
	            return ResponseEntity.badRequest()
	                    .body(Map.of("success", false, "message", e.getMessage()));
	        }
	    }
	 
	 
	// ========== 商城儀表板統計 API ========== //
	// ***** 取得商品總數 ***** // 
	 @GetMapping("/admin/products/count")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Long> getProductCount() {
	     try {
	         log.info("查詢商品總數");
	         
	         // 使用已注入的 ProductRepository
	         Long productCount = productRepository.count();
	         
	         log.info("商品總數查詢成功：{}", productCount);
	         return ResponseEntity.ok(productCount);
	         
	     } catch (Exception e) {
	         log.error("取得商品總數失敗", e);
	         return ResponseEntity.status(500).body(0L);
	     }
	 } 
	 
	 
	 
	 
	// ***** 取得待處理訂單數量 ***** //  
	 @GetMapping("/admin/orders/pending/count")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Long> getPendingOrderCount() {
	     try {
	         log.info("查詢待處理訂單數量");
	         
	         // 使用已注入的 OrderRepository，查詢狀態為 "PENDING" 的訂單
	         Long pendingCount = orderRepository.countOrdersByStatus("PENDING");
	         
	         log.info("待處理訂單數量查詢成功：{}", pendingCount);
	         return ResponseEntity.ok(pendingCount);
	         
	     } catch (Exception e) {
	         log.error("取得待處理訂單數量失敗", e);
	         return ResponseEntity.status(500).body(0L);
	     }
	 }
	 
	 
	// ***** 取得本月營收  ***** // 
	 @GetMapping("/admin/orders/monthly-revenue")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<BigDecimal> getMonthlyRevenue() {
	     try {
	         log.info("查詢本月營收");
	         
	         // 取得本月第一天和最後一天
	         LocalDate now = LocalDate.now();
	         LocalDate firstDayOfMonth = now.withDayOfMonth(1);
	         LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
	         
	         // 轉換為 LocalDateTime
	         LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
	         LocalDateTime endOfMonth = lastDayOfMonth.atTime(23, 59, 59);
	         
	         // 查詢本月已完成訂單的總金額
	         // 你需要在 OrderRepository 中新增這個方法
	         BigDecimal monthlyRevenue = orderRepository.calculateMonthlyRevenue(startOfMonth, endOfMonth);
	         
	         // 如果為 null，回傳 0
	         if (monthlyRevenue == null) {
	             monthlyRevenue = BigDecimal.ZERO;
	         }
	         
	         log.info("本月營收查詢成功：NT${}", monthlyRevenue);
	         return ResponseEntity.ok(monthlyRevenue);
	         
	     } catch (Exception e) {
	         log.error("取得本月營收失敗", e);
	         return ResponseEntity.status(500).body(BigDecimal.ZERO);
	     }
	 }
	 
	 
	// ***** 取得評論總數 (使用現有的 OrderItemService) ***** // 
	 @GetMapping("/admin/comments/count")
	 @PreAuthorize("hasRole('ADMIN')")
	 public ResponseEntity<Long> getCommentCount() {
	     try {
	         log.info("查詢評論總數");
	         
	         // 使用現有的 OrderItemService 取得統計
	         var statistics = orderItemService.getAdminStatistics();
	         Long commentCount = statistics.getTotalComments();
	         
	         log.info("評論總數查詢成功：{}", commentCount);
	         return ResponseEntity.ok(commentCount);
	         
	     } catch (Exception e) {
	         log.error("取得評論總數失敗", e);
	         return ResponseEntity.status(500).body(0L);
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
	 
	 
	// ***** 強制更新訂單狀態 ***** //
	 @PostMapping("/debug/force-update-status/{orderNo}")
	 public ResponseEntity<?> forceUpdateStatus(
	         @PathVariable Integer orderNo,
	         @RequestParam String status) {
	     try {
	         // 直接更新資料庫
	         orderService.updateOrderStatus(orderNo, status);
	         
	         // 更新 Redis 狀態
	         String redisKey = "payment:" + orderNo;
	         if (redisTemplate.hasKey(redisKey)) {
	             redisTemplate.opsForHash().put(redisKey, "status", 
	                 status.equals("FAILED") ? "TIMEOUT" : status);
	             redisTemplate.opsForHash().put(redisKey, "updatedAt", System.currentTimeMillis());
	         }
	         
	         Map<String, Object> result = Map.of(
	             "message", "訂單狀態已更新",
	             "orderNo", orderNo,
	             "newStatus", status
	         );
	         
	         return ResponseEntity.ok(result);
	         
	     } catch (Exception e) {
	         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	     }
	 }

	 // ***** 檢查定時任務是否正常 ***** //
	 @GetMapping("/debug/scheduler-info")
	 public ResponseEntity<?> getSchedulerInfo() {
	     try {
	         // 檢查是否有 @EnableScheduling 註解
	         boolean schedulingEnabled = true; // 假設已啟用
	         
	         // 模擬執行一次定時任務並記錄
	         long startTime = System.currentTimeMillis();
	         paymentService.handlePaymentTimeout();
	         long executionTime = System.currentTimeMillis() - startTime;
	         
	         Map<String, Object> info = Map.of(
	             "schedulingEnabled", schedulingEnabled,
	             "lastManualExecution", new Date(),
	             "executionTimeMs", executionTime,
	             "message", "定時任務手動執行完成"
	         );
	         
	         return ResponseEntity.ok(info);
	         
	     } catch (Exception e) {
	         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
	    
	    