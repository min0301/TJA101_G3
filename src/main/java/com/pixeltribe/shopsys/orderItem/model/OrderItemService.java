package com.pixeltribe.shopsys.orderItem.model;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.order.model.Order;
import com.pixeltribe.shopsys.order.model.OrderRepository;
import com.pixeltribe.shopsys.orderItem.exception.OrderItemErrorCode;
import com.pixeltribe.shopsys.orderItem.exception.OrderItemException;
import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductRepository;

import lombok.Getter;
import lombok.Setter;

@Service
public class OrderItemService {
	
	@Autowired
	private OrderItemRepository orderItemRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
    private OrderRepository orderRepository;
    
	
	
	// ========== 基本的 CRUD 操作 ========== //
    
    // *** 建立訂單明細 *** //
    public OrderItemDTO createOrderItem(CreateOrderItemRequest request) {
        try {
            // 1. 驗證訂單是否存在
            Order order = orderRepository.findById(request.getOrderNo())
                .orElseThrow(() -> new OrderItemException(OrderItemErrorCode.ORDER_NOT_FOUND));
            
            // 2. 驗證產品是否存在
            Product product = productRepository.findById(request.getProNo())
                .orElseThrow(() -> new OrderItemException(OrderItemErrorCode.PRODUCT_NOT_FOUND));
            
            // 3. 創建 OrderItem 實體
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProNo(product);
            orderItem.setOrderAmount(request.getQuantity());
            orderItem.setProPrice(product.getProPrice());
            orderItem.setProName(product.getProName());
            
            // 4. 保存到資料庫
            OrderItem saved = orderItemRepository.save(orderItem);
            
            // 5. 轉換為 DTO 回傳
            return OrderItemDTO.from(saved);
            
        } catch (OrderItemException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderItemException(OrderItemErrorCode.CREATE_FAILED, e);
        }
    }
    
    
    // *** 根據訂單編號查詢訂單明細 *** //
    public List<OrderItemDTO> getOrderItemsByOrderNo(Integer orderNo) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderNo(orderNo);
        return OrderItemDTO.fromList(orderItems);
    }
    
    
    // *** 根據訂單編號和會員編號查詢訂單明細 *** //
    public List<OrderItemDTO> getOrderItemsByOrderNo(Integer orderNo, Integer memNo) {
        // 首先驗證這個訂單是否屬於該會員
        List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderNo(orderNo);
        if (orderItems.isEmpty()) {
            throw new OrderItemException(OrderItemErrorCode.ORDERITEM_NOT_FOUND);
        }
        
        // 檢查權限
        if (!orderItems.get(0).getOrder().getMemNo().getId().equals(memNo)) {
            throw new OrderItemException(OrderItemErrorCode.ACCESS_DENIED);
        }
        
        return OrderItemDTO.fromList(orderItems);
    }
    
    
    
    // *** 查詢會員的特定訂單明細 <<要用於評價功能上的>> *** //
    public OrderItemDTO getMemberOrderItem(Integer orderItemNo, Integer memNo) {
        OrderItem orderItem = orderItemRepository.findByOrderItemNoAndMemberNo(orderItemNo, memNo)
            .orElseThrow(() -> new OrderItemException(OrderItemErrorCode.ORDERITEM_NOT_FOUND));
        
        return OrderItemDTO.from(orderItem);
    }
    
    
    // ========== 評價相關功能 ========== //
    // *** 新增評價 (前台功能) *** //
    public OrderItemDTO addComment(Integer orderItemNo, Integer memNo, CommentRequest request) {
        // 1. 查詢訂單明細並驗證權限
    	OrderItem orderItem = orderItemRepository.findByOrderItemNoAndMemberNo(orderItemNo, memNo)
    	        .orElseThrow(() -> new OrderItemException(OrderItemErrorCode.ORDERITEM_NOT_FOUND));
    	    
        // 2. 檢查訂單狀態是否允許評價
    	String orderStatus = orderItem.getOrder().getOrderStatus();
    	if (!isOrderCompletedForComment(orderStatus)) {
    	    throw new OrderItemException(OrderItemErrorCode.ORDER_NOT_COMPLETED);
    	}
        
        // 3. 檢查是否已經評價過
        if (orderItem.getProductComment() != null || orderItem.getProStar() != null) {
            throw new OrderItemException(OrderItemErrorCode.ALREADY_COMMENTED);
        }
        
        // 4. 設定評價資訊
        orderItem.setProStar(request.getProStar());
        orderItem.setProductComment(request.getProductComment());
        orderItem.setProductCommentCrdate(Instant.now());
        
        // 前台新增評價時，系統自動設定為正常狀態 <<前台用戶無法控制這個狀態，只有後台管理員可以修改>>
        orderItem.setProComStatus('1');  // 系統預設為正常狀態
        
        // 5. 保存並回傳
        OrderItem saved = orderItemRepository.save(orderItem);
        return OrderItemDTO.from(saved);
    }
    
    // 新增：在 OrderItemService 類別中新增狀態檢查方法
    private boolean isOrderCompletedForComment(String orderStatus) {
        if (orderStatus == null) {
            return false;
        }
        
        // ✅ 支援多種完成狀態格式
        String status = orderStatus.trim();
        
        // 支援數字狀態
        if ("2".equals(status)) {
            return true;
        }
        
        // 支援中文狀態
        if ("已完成".equals(status) || "已發貨".equals(status) || "已出貨".equals(status)) {
            return true;
        }
        
        // 支援英文狀態（不區分大小寫）
        String upperStatus = status.toUpperCase();
        if ("COMPLETED".equals(upperStatus) || "SHIPPED".equals(upperStatus)) {
            return true;
        }
        
        return false;
    }
    
    
    
    
    
    // *** 修改評價（前台功能） *** //
    public OrderItemDTO updateComment(Integer orderItemNo, Integer memNo, CommentRequest request) {
        // 1. 查詢訂單明細並驗證權限
        OrderItem orderItem = orderItemRepository.findByOrderItemNoAndMemberNo(orderItemNo, memNo)
            .orElseThrow(() -> new OrderItemException(OrderItemErrorCode.ORDERITEM_NOT_FOUND));
        
        // 2. 檢查是否已經評價過
        String orderStatus = orderItem.getOrder().getOrderStatus();
        if (!isOrderCompletedForComment(orderStatus)) {
            throw new OrderItemException(OrderItemErrorCode.ORDER_NOT_COMPLETED);
        }
        
        // 3. 檢查評價是否被停權
        if (orderItem.getProComStatus() != null && orderItem.getProComStatus() == '0') {
            throw new OrderItemException(OrderItemErrorCode.COMMENT_BLOCKED);
        }
        
        // 4. 更新評價資訊
        orderItem.setProStar(request.getProStar());
        orderItem.setProductComment(request.getProductComment());
        orderItem.setProductCommentCrdate(Instant.now());
        
        // 修改評價時，不改變評價狀態 <<保持原有的 proComStatus，前台無法修改狀態>>
        // 如果原本是正常('1')，繼續保持正常；如果原本被停權('0')，上面已經擋掉了
        
        // 5. 保存並回傳
        OrderItem saved = orderItemRepository.save(orderItem);
        return OrderItemDTO.from(saved);
    }
    
    
    
    

    // *** 查詢會員可以評價的訂單明細（基於特定訂單） *** //
    // *** 使用場景：會員在訂單詳情頁面，查看該訂單中已評價的商品 *** //
    public List<OrderItemDTO> getMemberCommentsByOrder(Integer orderNo, Integer memNo) {
        // 1. 先獲取該訂單的所有明細（含權限檢查）
        List<OrderItemDTO> orderItems = getOrderItemsByOrderNo(orderNo, memNo);
        
        // 2. 過濾出已評價的項目
        return orderItems.stream()
            .filter(item -> item.getHasCommented() != null && item.getHasCommented())
            .collect(java.util.stream.Collectors.toList());
    }
    
    
    
    
    
    // ========== 產品評價查詢（前台） ========== //
    // *** 查詢產品的正常評價 *** //
    public List<OrderItemDTO> getProductComments(Integer proNo) {
        List<OrderItem> orderItems = orderItemRepository.findProductNormalComments(proNo);
        return OrderItemDTO.fromList(orderItems);
    }

    
    // *** 查詢產品的正常評價（分頁） *** //
    public Page<OrderItemDTO> getProductComments(Integer proNo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderItem> orderItems = orderItemRepository.findProductNormalCommentsWithPage(proNo, pageable);
        return orderItems.map(OrderItemDTO::from);
    }
    
    
    
    // *** 產品評價統計 *** //
    public ProductCommentStatistics getProductCommentStatistics(Integer proNo) {
        Long totalComments = orderItemRepository.countProductNormalComments(proNo);
        Double averageRating = orderItemRepository.calculateProductAverageRating(proNo);
        List<Object[]> ratingDistribution = orderItemRepository.countProductRatingDistribution(proNo);
        
        return new ProductCommentStatistics(totalComments, averageRating, ratingDistribution);
    }
    
    
    
    
    // ========== 後台管理功能 ========== //
    // *** 查詢所有評價（後台用） *** //
    public Page<AdminCommentDTO> getAllComments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderItem> orderItems = orderItemRepository.findAllCommentsWithPage(pageable);
        return orderItems.map(this::convertToAdminCommentDTO);
    }
    
    
    // *** 根據狀態查詢評價（後台用，查看所有正常評價//查所有被停權的） *** //
    public Page<AdminCommentDTO> getCommentsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderItem> orderItems = orderItemRepository.findCommentsByStatus(status, pageable);
        return orderItems.map(this::convertToAdminCommentDTO);
    }
    
    // *** 更新評價狀態 *** //
    public void updateCommentStatus(Integer orderItemNo, String status) {
        // 驗證訂單明細是否存在
        if (!orderItemRepository.existsById(orderItemNo)) {
            throw new OrderItemException(OrderItemErrorCode.ORDERITEM_NOT_FOUND);
        }
        
        // 更新狀態
        int updated = orderItemRepository.updateCommentStatus(orderItemNo, status);
        if (updated == 0) {
            throw new OrderItemException(OrderItemErrorCode.UPDATE_FAILED);
        }
    }

    
    
    // *** 批量更新評價狀態 <<後台發現多個不當評價，選取多個評價後 全部一起停權>>*** //
    public void batchUpdateCommentStatus(List<Integer> orderItemNos, String status) {
        int updated = orderItemRepository.batchUpdateCommentStatus(orderItemNos, status);
        if (updated == 0) {
            throw new OrderItemException(OrderItemErrorCode.UPDATE_FAILED);
        }
    }
    
    
    // *** 後台統計資訊 *** //
    public AdminStatistics getAdminStatistics() {
        
        Long blockedComments = orderItemRepository.countBlockedComments();
        Long pendingComments = orderItemRepository.countPendingComments();
        Long totalComments = blockedComments + pendingComments;
        
        return new AdminStatistics(totalComments, blockedComments, pendingComments);
    }
    
    
    
    
    // ========== 私有方法 ========== //
    // *** 轉換為後台評價DTO *** //
    private AdminCommentDTO convertToAdminCommentDTO(OrderItem orderItem) {
        AdminCommentDTO dto = new AdminCommentDTO();
        
        // 基本資訊
        dto.setOrderItemNo(orderItem.getOrderItemNo());
        dto.setOrderNo(orderItem.getOrderNo());
        dto.setProNo(orderItem.getProNo().getId());
        dto.setProName(orderItem.getProName());
        dto.setProStar(orderItem.getProStar());
        dto.setProductComment(orderItem.getProductComment());
        dto.setProductCommentCrdate(orderItem.getProductCommentCrdate());
        dto.setProComStatus(orderItem.getProComStatus());
        
        // 訂單資訊
        if (orderItem.getOrder() != null) {
            dto.setOrderDate(orderItem.getOrder().getOrderDatetime());
            dto.setOrderStatus(orderItem.getOrder().getOrderStatus());
           
            
            // 會員資訊
            if (orderItem.getOrder().getMemNo() != null) {
                Member member = orderItem.getOrder().getMemNo();
                dto.setMemNo(member.getId());
                dto.setMemName(member.getMemName());
                dto.setMemEmail(member.getMemEmail());
            }
        }

        // 產品資訊
        if (orderItem.getProNo() != null) {
            dto.setProductPrice(orderItem.getProPrice());
        }

        return dto;
     }
    
    
    // ========== 內部類別：統計資料 ========== //
    
    @Getter
    @Setter
    public static class ProductCommentStatistics {
        private Long totalComments;
        private Double averageRating;
        private List<Object[]> ratingDistribution;
        
        public ProductCommentStatistics(Long totalComments, Double averageRating, List<Object[]> ratingDistribution) {
            this.totalComments = totalComments;
            this.averageRating = averageRating;
            this.ratingDistribution = ratingDistribution;
        }
    }
    
    @Getter
    @Setter
    public static class AdminStatistics {
        private Long totalComments;
        private Long blockedComments;
        private Long pendingComments;
        
        public AdminStatistics(Long totalComments, Long blockedComments, Long pendingComments) {
            this.totalComments = totalComments;
            this.blockedComments = blockedComments;
            this.pendingComments = pendingComments;
        }
    }
    
    
}