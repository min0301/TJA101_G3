package com.pixeltribe.shopsys.order.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.cart.model.CartService;
import com.pixeltribe.shopsys.cart.model.CartDTO;
import com.pixeltribe.shopsys.order.exception.OrderNotFoundException;
import com.pixeltribe.shopsys.order.model.CreateOrderRequest;
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
	/*  
	1. 取得訂單詳情 (PaymentService 使用)
	2. 返回包含完整資訊的 OrderDTO
	*/
	public OrderDTO getOrderDetail(Integer orderNo) {
        try {
            log.debug("查詢訂單詳情：orderNo={}", orderNo);
            
            // 1. 查詢訂單基本資訊
            Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new OrderNotFoundException("訂單不存在：" + orderNo));
            
            // 2. 查詢訂單項目
            List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderNo(orderNo);
            
            // 3. 轉換為 DTO
            OrderDTO orderDTO = convertToOrderDTO(order, orderItems);
            
            log.debug("訂單查詢成功：orderNo={}, status={}, total={}", 
                     orderNo, orderDTO.getOrderStatus(), orderDTO.getOrderTotal());
            
            return orderDTO;
            
        } catch (Exception e) {
            log.error("查詢訂單詳情失敗：orderNo={}", orderNo, e);
            throw new RuntimeException("查詢訂單失敗：" + e.getMessage());
        }
    }
	
	/*  
	1. 更新訂單狀態 (PaymentService 調用)
	2. 支援付款流程的狀態轉換
	*/
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
		    
		    /*
		     1. 檢查訂單所有商品是否都已發貨
		     2. @param orderNo 訂單編號
		     3. @return 是否全部發貨
		     */
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

		    /*
		     1. 檢查並更新訂單發貨狀態
		     2. @param orderNo 訂單編號
		     3. @return 是否已完全發貨
		     */
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
	
	
	
	/*  
	1. 查詢會員的所有訂單
	*/
	public List<OrderDTO> getmemOrders(Integer memNo) {
        try {
            log.debug("查詢會員訂單：memNo={}", memNo);
            
            List<Order> orders = orderRepository.findByMemNoOrderByOrderDatetimeDesc(memNo);
            return orders.stream()
                        .map(order -> {
                            List<OrderItem> items = orderItemRepository.findByOrder_OrderNo(order.getOrderNo());
                            return convertToOrderDTO(order, items);
                        })
                        .collect(Collectors.toList());
                        
        } catch (Exception e) {
            log.error("查詢會員訂單失敗：memNo={}", memNo, e);
            throw new RuntimeException("查詢會員訂單失敗：" + e.getMessage());
        }
    }
	
	/*  
	1. 建立新訂單 (完整版本)
	*/
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

	        // 計算總金額
	        Integer totalAmount = calculateTotalAmount(createOrderRequest.getOrderItems());
	        order.setOrderTotal(totalAmount);

	        // 保存訂單
	        Order savedOrder = orderRepository.save(order);

	        // 3. 記錄創建訂單的審計日誌（包含客戶指定的聯絡資訊）
	        log.info("ORDER_CREATED|orderNo={}|memNo={}|total={}|contactEmail={}|contactPhone={}|timestamp={}", 
	                savedOrder.getOrderNo(), memNo, totalAmount,
	                createOrderRequest.getContactEmail(), 
	                createOrderRequest.getContactPhone(),
	                System.currentTimeMillis());

	        // 4. 建立訂單項目
	        List<OrderItem> orderItems = createOrderItems(savedOrder.getOrderNo(), createOrderRequest.getOrderItems());

	        // 5. 轉換為 DTO（使用會員預設聯絡資訊）
	        OrderDTO orderDTO = convertToOrderDTO(savedOrder, orderItems);

	        // 6. DTO 層覆蓋：如果客戶指定了不同的聯絡資訊，就覆蓋預設值
	        if (createOrderRequest.getContactEmail() != null && 
	            !createOrderRequest.getContactEmail().trim().isEmpty()) {
	            orderDTO.setContactEmail(createOrderRequest.getContactEmail());
	            log.debug("使用客戶指定的信箱：{}", createOrderRequest.getContactEmail());
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
		/*  
		1. 從購物車建立訂單 (完整版本)
		2. 取得購物車資料並轉換為訂單
		*/
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
	
	/*  
	1. 取消訂單
	*/
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
	private OrderDTO convertToOrderDTO(Order order, List<OrderItem> orderItems) {
        OrderDTO dto = new OrderDTO();
        
        // 基本訂單資訊
        dto.setOrderNo(order.getOrderNo());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderTotal(order.getOrderTotal());
        dto.setOrderDatetime(order.getOrderDatetime());
        
        // 從Member關聯物件取得會員資料
        if (order.getMemNo() != null) {
            dto.setMemNo(order.getMemNo().getId());              // 取得會員編號
            dto.setContactEmail(order.getMemNo().getMemEmail()); // 取得會員真實信箱
        } else {
            dto.setContactEmail("guest@pixeltribe.com");         // 預設信箱
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

	                    // 使用 OrderItem 的建構子
	                    OrderItem item = new OrderItem(order, product, request.getQuantity());
	                    
	                    return orderItemRepository.save(item);

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
	    // 相同狀態不需要轉換
	    if (currentStatus.equals(newStatus)) {
	        return true;
	    }
	    
	    log.debug("檢查狀態轉換：{} → {}", currentStatus, newStatus);
	    
	    // 定義合法的狀態轉換規則
	    switch (currentStatus) {
	        case "PENDING":
	            // 待付款 → 可以轉換到：付款中、取消、失敗
	            return "PAYING".equals(newStatus) || 
	                   "CANCELLED".equals(newStatus) || 
	                   "FAILED".equals(newStatus);
	                   
	        case "PAYING":
	            // 付款中 → 可以轉換到：處理中、失敗、取消
	            return "PROCESSING".equals(newStatus) || 
	                   "FAILED".equals(newStatus) || 
	                   "CANCELLED".equals(newStatus);
	                   
	        case "PROCESSING":
	            // 處理中 → 可以轉換到：已出貨、已完成、取消
	            return "SHIPPED".equals(newStatus) || 
	                   "COMPLETED".equals(newStatus) || 
	                   "CANCELLED".equals(newStatus);
	                   
	        case "SHIPPED":
	            // 已出貨 → 可以轉換到：已完成
	            return "COMPLETED".equals(newStatus);
	            
	        case "COMPLETED":
	            // 已完成 → 終結狀態，不能轉換
	            log.warn("訂單已完成，無法變更狀態：currentStatus={}, newStatus={}", currentStatus, newStatus);
	            return false;
	            
	        case "CANCELLED":
	            // 已取消 → 終結狀態，不能轉換  
	            log.warn("訂單已取消，無法變更狀態：currentStatus={}, newStatus={}", currentStatus, newStatus);
	            return false;
	            
	        case "FAILED":
	            // 失敗 → 可以重新開始：待付款、取消
	            return "PENDING".equals(newStatus) || 
	                   "CANCELLED".equals(newStatus);
	                   
	        default:
	            // 未知狀態，記錄警告但允許轉換（向後兼容）
	            log.warn("未知的訂單狀態：{}", currentStatus);
	            return true;
	    }
	}
}
	
