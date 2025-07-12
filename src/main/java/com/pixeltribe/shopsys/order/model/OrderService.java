package com.pixeltribe.shopsys.order.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;

import com.pixeltribe.shopsys.order.exception.OrderNotFoundException;
import com.pixeltribe.shopsys.order.model.OrderDTO.OrderStatusInfo;
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
	private OrderRepository orderRepository;
	
	@Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MemRepository memRepository;
	
	//  ========== 新增 ========== //
	//  訂單編號、會員編號、優惠票夾代碼、訂購時間、訂購狀態、訂單總額、使用積分
	public void add(Order order) {
		orderRepository.save(order);
	}
	
	 public Order addOrder(Order order) {
	        return orderRepository.save(order);
	    }
	
	//  ========== 改 ========== //
	public void update() {
		
	}
	
	//  ========== 查-單一查詢 ========== //
	public Order getOneOrder(Integer orderNo) {
		Optional<Order> optional = orderRepository.findById(orderNo);
		return optional.orElse(null);
	}
	
	//  ========== 查-查全部 ========== //
	public List<Order> getAllOrder(){
		return orderRepository.findAll();
	}
	
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
                break;
            case "COMPLETED":
                log.info("PAYMENT_AUDIT|orderNo={}|action=ORDER_COMPLETED|oldStatus={}|newStatus={}|timestamp={}", 
                        orderNo, oldStatus, newStatus, timestamp);
                break;
            case "FAILED":
                log.info("PAYMENT_AUDIT|orderNo={}|action=PAYMENT_FAILED|oldStatus={}|newStatus={}|timestamp={}", 
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
	
	/*  
	1. 查詢會員的所有訂單
	*/
	public List<OrderDTO> getmenOrders(Integer memNo) {
        try {
            log.debug("查詢會員訂單：memNo={}", memNo);
            
            List<Order> orders = orderRepository.findByMemNo_MemNoOrderByOrderDatetimeDesc(memNo);
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
            order.setOrderStatus("CANCELLED");
            
            orderRepository.save(order);
            
            log.info("訂單取消成功：orderNo={}", orderNo);
            return true;
            
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
}
	
//========== 相關的 DTO 和 Request 類別 ========== //
//***** 建立訂單請求 ***** //
class CreateOrderRequest {
    private String contactEmail;
    private String contactPhone;
    private List<CreateOrderItemRequest> orderItems;
    
    // getters and setters
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public List<CreateOrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<CreateOrderItemRequest> orderItems) { this.orderItems = orderItems; }
}	
	
