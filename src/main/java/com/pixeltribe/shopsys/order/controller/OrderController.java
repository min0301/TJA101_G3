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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.order.model.*;

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
        return orderRepository.findByOrderStatus(status).stream()
                .map(order -> {
                	Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderNo", order.getOrderNo());
                    // 只處理 Member 部分
                    orderMap.put("memNo", order.getMemNo() != null ? order.getMemNo().getId() : null);
                    // CouponWallet 暫時設為 null
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
	
	@GetMapping("/orders/member/{id}")
	public List<OrderDTO> getOrderByMemId(@PathVariable Integer id){
	    List<OrderDTO> orders = orderService.getmemOrders(id);
	    return orders;
	}
	
	
	// ========== 前台會員訂單功能 (OrderService) ========== //
	// ***** 查詢訂單詳情 ***** //
	@GetMapping("/{orderNo}")
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
	// ***** 發起付款 ***** //
	@PostMapping("/{orderNo}/payment/initiate")
    public ResponseEntity<String> initiatePayment(@PathVariable Integer orderNo) {
        try {
            log.info("發起付款：orderNo={}", orderNo);
            String paymentForm = paymentService.initiatePayment(orderNo);
            return ResponseEntity.ok(paymentForm);
        } catch (Exception e) {
            log.error("發起付款失敗：orderNo={}", orderNo, e);
            return ResponseEntity.badRequest().body("發起付款失敗：" + e.getMessage());
        }
    }
	
	
	// ***** 綠界付款回調 ***** //
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
	
	
	// ***** 查詢付款狀態 ***** //
	 @GetMapping("/{orderNo}/payment/status")
	    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable Integer orderNo) {
	        try {
	            log.info("查詢付款狀態：orderNo={}", orderNo);
	            Map<String, Object> status = paymentService.getPaymentStatus(orderNo);
	            
	            if (status != null) {
	                return ResponseEntity.ok(status);
	            } else {
	                Map<String, Object> response = new HashMap<>();
	                response.put("message", "找不到付款資訊");
	                return ResponseEntity.notFound().build();
	            }
	        } catch (Exception e) {
	            log.error("查詢付款狀態失敗：orderNo={}", orderNo, e);
	            return ResponseEntity.badRequest().build();
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
	 
	 
	
	
	
	
	// ========== Request/Response DTO 類別 ========== //
	// ** 從購物車建立訂單請求 **//
	@Data
	class CreateOrderFromCartRequest {
	    private Integer memNo;
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