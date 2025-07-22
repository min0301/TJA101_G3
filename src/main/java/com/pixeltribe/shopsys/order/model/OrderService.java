package com.pixeltribe.shopsys.order.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.cart.model.CartService;
import com.pixeltribe.shopsys.cart.model.CartDTO;
import com.pixeltribe.shopsys.order.exception.OrderNotFoundException;

import com.pixeltribe.shopsys.orderItem.model.CreateOrderItemRequest;
import com.pixeltribe.shopsys.orderItem.model.OrderItem;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;
import com.pixeltribe.shopsys.orderItem.model.OrderItemRepository;
import com.pixeltribe.shopsys.product.exception.ProductNotFoundException;
import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductRepository;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OrderService {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MemRepository memRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    

	
	//  ========== 新增 ========== //
	//  訂單編號、會員編號、優惠票夾代碼、訂購時間、訂購狀態、訂單總額、使用積分
	public void add(Order order) {
		orderRepository.save(order);
	}
	
	 public Order addOrder(Order order) {
	        return orderRepository.save(order);
	    }
	
	//  ========== 查-單一查詢 ========== //
	public Order getOneOrder(Integer orderNo) {
		Optional<Order> optional = orderRepository.findById(orderNo);
		return optional.orElse(null);
	}
	
	//  ========== 查-查全部 ========== //
	
	
	//  ========== PaymentService 需要的功能 ========== //
	// ***取得訂單詳情 (PaymentService 使用)  返回包含完整資訊的 OrderDTO *** //
	public OrderDTO getOrderDetail(Integer orderNo) {
        try {
            log.debug("查詢訂單詳情：orderNo={}", orderNo);
            
            // 1. 查詢訂單基本資訊
            Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new OrderNotFoundException("訂單不存在：" + orderNo));
            
            // 2. 查詢訂單項目
            List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderNo(orderNo);
            
            // 3. 轉換為 DTO
            OrderDTO orderDTO = convertToOrderDTO(order, orderItems, null);
            
            log.debug("訂單查詢成功：orderNo={}, status={}, total={}", 
                     orderNo, orderDTO.getOrderStatus(), orderDTO.getOrderTotal());
            
            return orderDTO;
            
        } catch (Exception e) {
            log.error("查詢訂單詳情失敗：orderNo={}", orderNo, e);
            throw new RuntimeException("查詢訂單失敗：" + e.getMessage());
        }
    }
	
	// *** 更新訂單狀態 (PaymentService 調用)  支援付款流程的狀態轉換 *** //
	public void updateOrderStatus(Integer orderNo, String newStatus) {
        try {
            log.info("更新訂單狀態：orderNo={}, newStatus={}", orderNo, newStatus);
            
            Order order = orderRepository.findByOrderNo(orderNo);
                if (order == null) {
                	throw new OrderNotFoundException("訂單不存在：" + orderNo);
                }
                
                String oldStatus = order.getOrderStatus();
                long timestamp = System.currentTimeMillis();
            
            // 加入狀態轉換驗證
            if (!isValidStatusTransition(oldStatus, newStatus)) {
                String errorMsg = String.format("不合法的狀態轉換：%s → %s (訂單編號：%d)", 
                                               oldStatus, newStatus, orderNo);
                log.warn(errorMsg);
                throw new IllegalStateException(errorMsg);
                }
                
              
            // 更新狀態
            order.setOrderStatus(newStatus);
            
            // 根據狀態更新特定時間
            switch (newStatus) {
            case "PAYING":
                log.info("PAYMENT_AUDIT|orderNo={}|action=START_PAYMENT|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                break;
                
            case "PROCESSING":
                log.info("PAYMENT_AUDIT|orderNo={}|action=PAYMENT_SUCCESS|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                // 付款成功時，清空購物車
                try {
                    cartService.clearCart(order.getMemNo().getId());
                    log.info("付款成功，已清空購物車：orderNo={}, memNo={}", 
                            orderNo, order.getMemNo().getId());
                } catch (Exception e) {
                    log.error("清空購物車失敗：orderNo={}, memNo={}", 
                             orderNo, order.getMemNo().getId(), e);
                }
                break;
                
            case "SHIPPED":  // 序號已發送狀態
                log.info("PAYMENT_AUDIT|orderNo={}|action=SERIAL_DELIVERED|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                log.info("數位商品序號已發送：orderNo={}", orderNo);
                break;    
                
            case "COMPLETED":
                log.info("PAYMENT_AUDIT|orderNo={}|action=ORDER_COMPLETED|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                break;
                
            case "FAILED":
                log.info("PAYMENT_AUDIT|orderNo={}|action=PAYMENT_FAILED|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                break;
            
            case "CANCELLED":
                log.info("PAYMENT_AUDIT|orderNo={}|action=ORDER_CANCELLED|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                break;   
                
            default:
                // 記錄其他狀態變更
                log.info("PAYMENT_AUDIT|orderNo={}|action=STATUS_CHANGE|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                break;
            }
            
            orderRepository.save(order);
            
            log.info("訂單狀態更新成功：orderNo={}, {} → {}", orderNo, oldStatus, newStatus);
            
        } catch (Exception e) {
            log.error("更新訂單狀態失敗：orderNo={}, newStatus={}", orderNo, newStatus, e);
            throw new RuntimeException("更新訂單狀態失敗：" + e.getMessage());
        }
    }
	
	
	// ========== 發貨檢查方法 ========== //
		    
		    // *** 檢查訂單所有商品是否都已發貨 *** //
		    public boolean areAllItemsShipped(Integer orderNo) {
		        try {
		            // 檢查該訂單的所有商品項目是否都已分配序號
		            String sql = "SELECT COUNT(*) FROM order_item oi " +
		                        "LEFT JOIN pro_serial_numbers psn ON oi.order_item_no = psn.order_item_no " +
		                        "WHERE oi.order_no = ? AND psn.product_sn IS NULL";
		            
		            Integer unshippedCount = jdbcTemplate.queryForObject(sql, Integer.class, orderNo);
		            
		            boolean allShipped = (unshippedCount != null && unshippedCount == 0);
		            log.debug("訂單發貨檢查：orderNo={}, 未發貨商品數={}, 全部發貨={}", 
		                     orderNo, unshippedCount, allShipped);
		            
		            return allShipped;
		            
		        } catch (Exception e) {
		            log.error("檢查訂單發貨狀態失敗：orderNo={}", orderNo, e);
		            return false;
		        }
		    }

		    // *** 檢查並更新訂單發貨狀態 ***//
		    public boolean checkAndUpdateShippingStatus(Integer orderNo) {
		        try {
		            if (areAllItemsShipped(orderNo)) {
		                // 如果全部商品都已發貨，更新訂單狀態
		                updateOrderStatus(orderNo, "SHIPPED");
		                log.info("訂單全部商品已發貨，狀態已更新：orderNo={}", orderNo);
		                return true;
		            }
		            return false;
		        } catch (Exception e) {
		            log.error("檢查並更新訂單發貨狀態失敗：orderNo={}", orderNo, e);
		            return false;
		        }
		    }
	
	
	
	// *** 查詢會員的所有訂單 *** //
	public List<OrderDTO> getmemOrders(Integer memNo) {
        try {
            log.debug("查詢會員訂單：memNo={}", memNo);
            
            List<Order> orders = orderRepository.findByMemNoOrderByOrderDatetimeDesc(memNo);
            return orders.stream()
                        .map(order -> {
                            List<OrderItem> items = orderItemRepository.findByOrder_OrderNo(order.getOrderNo());
                            return convertToOrderDTO(order, items, null);
                        })
                        .collect(Collectors.toList());
                        
        } catch (Exception e) {
            log.error("查詢會員訂單失敗：memNo={}", memNo, e);
            throw new RuntimeException("查詢會員訂單失敗：" + e.getMessage());
        }
    }
	
	// ***建立新訂單 (完整版本) *** //
	public OrderDTO createOrder(CreateOrderRequest createOrderRequest, Integer memNo) {
	    try {
	        log.info("開始建立訂單：memNo={}", memNo);

	        // 1. 查詢會員資料
	        Member member = memRepository.findById(memNo)
	                .orElseThrow(() -> new RuntimeException("會員不存在：" + memNo));

	        // 2. 建立訂單主體
	        Order order = new Order();
	        order.setMemNo(member);
	        order.setOrderStatus("PENDING"); // 初始狀態：待付款
	        
	        // 先設定訂單編號（讓 MySQL 觸發器覆蓋）
	        order.setOrderNo(0);  // 臨時編號，MySQL 觸發器會覆蓋
	        

	        // 計算總金額
	        Integer totalAmount = calculateTotalAmount(createOrderRequest.getOrderItems());
	        order.setOrderTotal(totalAmount);

	        // 保存訂單
	        Order savedOrder = orderRepository.save(order);
	        
	        //  重新查詢，獲得 MySQL 觸發器生成的真實訂單編號
	        try {
	            List<Order> recentOrders = orderRepository.findByMemNoOrderByOrderDatetimeDesc(memNo);
	            if (!recentOrders.isEmpty()) {
	                savedOrder = recentOrders.get(0);
	                log.debug("觸發器生成的訂單編號：{}", savedOrder.getOrderNo());
	            } else {
	                log.warn("無法找到剛建立的訂單，使用原始值：{}", savedOrder.getOrderNo());
	            }
	        } catch (Exception e) {
	            log.warn("重新查詢訂單編號失敗：{}", e.getMessage());
	        }
	        
	        

	        // 3. 記錄創建訂單的審計日誌（包含客戶指定的聯絡資訊）
	        log.info("ORDER_CREATED|orderNo={}|memNo={}|total={}|contactEmail={}|contactPhone={}|timestamp={}", 
	                savedOrder.getOrderNo(), memNo, totalAmount,
	                createOrderRequest.getContactEmail(), 
	                createOrderRequest.getContactPhone(),
	                System.currentTimeMillis());

	        // 4. 建立訂單項目
	        List<OrderItem> orderItems = createOrderItems(savedOrder.getOrderNo(), createOrderRequest.getOrderItems());

	        // 5. 轉換為 DTO（使用會員預設聯絡資訊）
	        OrderDTO orderDTO = convertToOrderDTO(savedOrder, orderItems, createOrderRequest.getContactEmail());

	        // 6. 將客戶指定信箱存到 Redis（7天過期）
	        if (createOrderRequest.getContactEmail() != null && 
	                !createOrderRequest.getContactEmail().trim().isEmpty()) {
	                String redisKey = "order:contact:" + savedOrder.getOrderNo();
	                redisTemplate.opsForValue().set(redisKey, createOrderRequest.getContactEmail(), 7, TimeUnit.DAYS);
	                log.debug("客戶聯絡信箱已存入 Redis：orderNo={}, email={}", 
	                        savedOrder.getOrderNo(), createOrderRequest.getContactEmail());
	            }
	        
	        

	        log.info("訂單建立成功：orderNo={}, total={}, contactEmail={}", 
	                savedOrder.getOrderNo(), totalAmount, orderDTO.getContactEmail());

	        return orderDTO;

	    } catch (Exception e) {
	        log.error("建立訂單失敗：memNo={}", memNo, e);
	        throw new RuntimeException("建立訂單失敗：" + e.getMessage());
	    }
	}
	
	// 從購物車建立訂單的方法
		// *** 從購物車建立訂單 (完整版本)  取得購物車資料並轉換為訂單 ***//
	public OrderDTO createOrderFromCart(Integer memNo, String contactEmail, String contactPhone) {
	    try {
	        log.info("從購物車建立訂單：memNo={}", memNo);

	        // 1. 取得購物車資料
	        CartDTO cart = cartService.getMemberCart(memNo);
	        
	        // 2. 驗證購物車不為空
	        if (cart.getItem() == null || cart.getItem().isEmpty()) {
	            throw new RuntimeException("購物車為空，無法建立訂單");
	        }
	        
	        log.info("購物車商品數量：{}", cart.getItem().size());
	        
	        // 3. 轉換購物車商品為訂單項目
	        List<CreateOrderItemRequest> orderItems = cart.getItem().stream()
	                .map(item -> {
	                    CreateOrderItemRequest orderItem = new CreateOrderItemRequest();
	                    orderItem.setProNo(item.getProNo());
	                    orderItem.setQuantity(item.getProNum());
	                    return orderItem;
	                })
	                .collect(Collectors.toList());
	        
	        // 4. 建立訂單請求
	        CreateOrderRequest orderRequest = new CreateOrderRequest();
	        orderRequest.setContactEmail(contactEmail);
	        orderRequest.setContactPhone(contactPhone);
	        orderRequest.setOrderItems(orderItems);
	        
	        // 5. 建立訂單（使用現有邏輯）
	        OrderDTO order = createOrder(orderRequest, memNo);
	        
	        // 6. 訂單建立成功，購物車將在付款成功時清空
	        log.info("從購物車建立訂單成功：orderNo={}, 購物車保留待付款完成後清空", order.getOrderNo());
	        return order;
	        
	    } catch (Exception e) {
	        log.error("從購物車建立訂單失敗：memNo={}", memNo, e);
	        throw new RuntimeException("建立訂單失敗：" + e.getMessage());
	    }
	}
	
	// *** 取消訂單 *** //
	public boolean cancelOrder(Integer orderNo, Integer memNo, String reason) {
        try {
            log.info("會員取消訂單：orderNo={}, memNo={}, reason={}", orderNo, memNo, reason);
            
            Order order = orderRepository.findByOrderNo(orderNo);
            if (order == null) {
            	throw new OrderNotFoundException("訂單不存在：" + orderNo);
            }
            
            // 檢查訂單所有權  (正確比較會員編號)
            if (!order.getMemNo().getId().equals(memNo)) {
                log.warn("無權限取消訂單：orderNo={}, memNo={}", orderNo, memNo);
                return false;
            }
            
            // 檢查是否可以取消
            if (!canCancelOrder(order.getOrderStatus())) {
                log.warn("訂單狀態不允許取消：orderNo={}, status={}", orderNo, order.getOrderStatus());
                return false;
            }
            
            // 更新訂單狀態
            try {
                updateOrderStatus(orderNo, "CANCELLED");
                log.info("訂單取消成功：orderNo={}", orderNo);
                return true;
            } catch (IllegalStateException e) {
                // 如果狀態轉換失敗，記錄錯誤
                log.error("訂單狀態轉換失敗：orderNo={}, error={}", orderNo, e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("取消訂單失敗：orderNo={}", orderNo, e);
            return false;
        }
    }
	
	
	//  ========== 私有輔助方法 ========== //
	// ***** 轉換 Order 和 OrderItems 為 OrderDTO ***** //
	private OrderDTO convertToOrderDTO(Order order, List<OrderItem> orderItems, String customContactEmail) {
        OrderDTO dto = new OrderDTO();
        
        // 基本訂單資訊
        dto.setOrderNo(order.getOrderNo());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderTotal(order.getOrderTotal());
        dto.setOrderDatetime(order.getOrderDatetime());
        
        
        // 從Member關聯物件取得會員資料
        if (order.getMemNo() != null) {
            dto.setMemNo(order.getMemNo().getId());
            
            // 關鍵修正：優先順序處理
            String contactEmail = null;
            
            // 關鍵修正：優先使用傳入的自訂信箱
            if (customContactEmail != null && !customContactEmail.trim().isEmpty()) {
                contactEmail = customContactEmail;
                log.debug("使用傳入的客戶指定信箱：{}", customContactEmail);
            } else {
                // 2. 從 Redis 查詢客戶指定信箱
                try {
                    String redisKey = "order:contact:" + order.getOrderNo();
                    String redisEmail = (String) redisTemplate.opsForValue().get(redisKey);
                    if (redisEmail != null && !redisEmail.trim().isEmpty()) {
                        contactEmail = redisEmail;
                        log.debug("使用 Redis 儲存的客戶信箱：{}", redisEmail);
                    } else {
                        // 3. 最後使用會員預設信箱
                        contactEmail = order.getMemNo().getMemEmail();
                        log.debug("使用會員預設信箱：{}", contactEmail);
                    }
                } catch (Exception e) {
                    log.warn("從 Redis 取得客戶信箱失敗，使用會員預設信箱：orderNo={}", order.getOrderNo(), e);
                    contactEmail = order.getMemNo().getMemEmail();
                }
            }
            
            dto.setContactEmail(contactEmail);
        } else {
            dto.setContactEmail("guest@pixeltribe.com");
        }
       
        
        
        
        // 訂單項目
        List<OrderItemDTO> itemDTOs = orderItems.stream()
                                                .map(this::convertToOrderItemDTO)
                                                .collect(Collectors.toList());
        dto.setOrderItems(itemDTOs);
        
        return dto;
    }
	
	
	// ***** 轉換 轉換 OrderItem 為 OrderItemDTO ***** //
	private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        
        dto.setOrderItemNo(orderItem.getOrderItemNo());
        dto.setOrderNo(orderItem.getOrderNo());
        dto.setProNo(orderItem.getProNo().getId());
        dto.setOrderAmount(orderItem.getOrderAmount());
        dto.setProPrice(orderItem.getProPrice());
        dto.setProName(orderItem.getProName());
        
        return dto;
    }
	
	
	
	// ***** 取得訂單狀態資訊 ***** //
	@SuppressWarnings("unused")
	private OrderDTO.OrderStatusInfo getOrderStatusInfo(String status) {
	    switch (status) {
	        case "PENDING":
	            return new OrderDTO.OrderStatusInfo("等待付款", 20);
	        case "PAYING":
	            return new OrderDTO.OrderStatusInfo("付款處理中", 40);
	        case "PROCESSING":
	            return new OrderDTO.OrderStatusInfo("處理中", 60);
	        case "SHIPPED":
	            return new OrderDTO.OrderStatusInfo("已出貨", 80);
	        case "COMPLETED":
	            return new OrderDTO.OrderStatusInfo("已完成", 100);
	        case "FAILED":
	            return new OrderDTO.OrderStatusInfo("處理失敗", 0);
	        case "CANCELLED":
	            return new OrderDTO.OrderStatusInfo("已取消", 0);
	        default:
	            return new OrderDTO.OrderStatusInfo(status, 0);
	    }
	}
	
	// ***** 計算訂單總金額 ***** //
	private Integer calculateTotalAmount(List<CreateOrderItemRequest> items) {
        return items.stream()
                   .mapToInt(item -> {
                       try {
                           Product product = productRepository.findById(item.getProNo())
                               .orElseThrow(() -> new ProductNotFoundException("產品不存在：" + item.getProNo()));
                           return product.getProPrice() * item.getQuantity();
                       } catch (Exception e) {
                           log.error("計算產品價格失敗：proNo={}", item.getProNo(), e);
                           return 0;
                       }
                   })
                   .sum();
    }
	
	// ***** 建立訂單項目 ***** //
	private List<OrderItem> createOrderItems(Integer orderNo, List<CreateOrderItemRequest> itemRequests) {
	    // 先取得 Order 物件
	    Order order = orderRepository.findByOrderNo(orderNo);
	    if (order == null) {
	        throw new OrderNotFoundException("訂單不存在：" + orderNo);
	    }
	    
	    return itemRequests.stream()
	            .map(request -> {
	                try {
	                    Product product = productRepository.findById(request.getProNo())
	                            .orElseThrow(() -> new ProductNotFoundException("產品不存在：" + request.getProNo()));

	                    // 🔥 除錯：檢查產品名稱
	                    log.debug("=== 建立OrderItem除錯 ===");
	                    log.debug("Product ID: {}, Product Name: '{}'", product.getId(), product.getProName());

	                    // 使用 OrderItem 的建構子
	                    OrderItem item = new OrderItem(order, product, request.getQuantity());
	                    
	                    // 🔥 關鍵修正：確保產品名稱被正確設定
	                    if (item.getProName() == null || item.getProName().trim().isEmpty()) {
	                        log.warn("OrderItem.proName 為空，手動設定：productId={}, productName={}", 
	                                product.getId(), product.getProName());
	                        item.setProName(product.getProName());
	                    }
	                    
	                    // 🔥 除錯：檢查 OrderItem 建立後的產品名稱
	                    log.debug("OrderItem 建立後 - proName: '{}'", item.getProName());
	                    
	                    OrderItem savedItem = orderItemRepository.save(item);
	                    
	                    // 🔥 除錯：檢查儲存後的產品名稱
	                    log.debug("OrderItem 儲存後 - proName: '{}'", savedItem.getProName());
	                    log.debug("=== 建立OrderItem除錯結束 ===");
	                    
	                    return savedItem;

	                } catch (Exception e) {
	                    log.error("建立訂單項目失敗：productId={}", request.getProNo(), e);
	                    throw new RuntimeException("建立訂單項目失敗：" + e.getMessage());
	                }
	            })
	            .collect(Collectors.toList());
	}
	    
	
	
	
	
	
	
	
	// ***** 檢查是否可以取消訂單 ***** //
	private boolean canCancelOrder(String orderStatus) {
        return "PENDING".equals(orderStatus) || "FAILED".equals(orderStatus);
    }
	
	
	// ***** 檢查狀態轉換是否合法 ***** //
	private boolean isValidStatusTransition(String currentStatus, String newStatus) {
	    // 相同狀態允許（冪等操作）
	    if (currentStatus.equals(newStatus)) {
	        return true;
	    }
	    
	    log.debug("檢查狀態轉換：{} → {}", currentStatus, newStatus);
	    
	    // 定義合法的狀態轉換規則
	    Map<String, Set<String>> validTransitions = Map.of(
	            "PENDING", Set.of("PAYING", "CANCELLED", "FAILED"),
	            "PAYING", Set.of("PROCESSING", "FAILED", "CANCELLED"),
	            "PROCESSING", Set.of("SHIPPED", "COMPLETED", "FAILED", "CANCELLED"), // 🔧 加入 COMPLETED
	            "SHIPPED", Set.of("COMPLETED", "FAILED"),
	            "COMPLETED", Set.of(), // 完成狀態不能轉換
	            "FAILED", Set.of("PENDING", "CANCELLED"), // 失敗可重試或取消
	            "CANCELLED", Set.of() // 取消狀態不能轉換
	        );
	        
	        Set<String> allowedNext = validTransitions.get(currentStatus);
	        return allowedNext != null && allowedNext.contains(newStatus);
	    }

	    // 🔧 修正：狀態變更審計日誌
//	    private void logStatusChange(Integer orderNo, String oldStatus, String newStatus) {
//	        String action = determineAction(oldStatus, newStatus);
//	        log.info("PAYMENT_AUDIT|orderNo={}|action={}|oldStatus={}|newStatus={}|timestamp={}", 
//	                 orderNo, action, oldStatus, newStatus, System.currentTimeMillis());
//	    }

	    // 🔧 新增：根據狀態轉換判斷動作類型
//	    private String determineAction(String oldStatus, String newStatus) {
//	        if ("PENDING".equals(oldStatus) && "PAYING".equals(newStatus)) {
//	            return "START_PAYMENT";
//	        } else if ("PAYING".equals(oldStatus) && "PROCESSING".equals(newStatus)) {
//	            return "PAYMENT_SUCCESS";
//	        } else if ("PROCESSING".equals(oldStatus) && "COMPLETED".equals(newStatus)) {
//	            return "ORDER_COMPLETED"; // 🔧 新增完成動作
//	        } else if ("PROCESSING".equals(oldStatus) && "SHIPPED".equals(newStatus)) {
//	            return "GOODS_SHIPPED";
//	        } else if ("SHIPPED".equals(oldStatus) && "COMPLETED".equals(newStatus)) {
//	            return "DELIVERY_CONFIRMED";
//	        } else if (newStatus.equals("CANCELLED")) {
//	            return "ORDER_CANCELLED";
//	        } else if (newStatus.equals("FAILED")) {
//	            return "PROCESS_FAILED";
//	        } else {
//	            return "STATUS_CHANGE";
//	        }
//	    }
}
	
