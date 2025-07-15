package com.pixeltribe.shopsys.orderItem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.orderItem.model.AdminCommentDTO;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService.AdminStatistics;

import jakarta.validation.Valid;


//~~~~~~~ << 後台 >> ~~~~~~~ //

@RestController
@RequestMapping("/api/admin/orderitem")
public class AdminOrderItemController {
    
    @Autowired
    private OrderItemService orderItemService;
    
    
    //** 查詢訂單明細（後台管理用） **//
    @GetMapping("/orderno/{orderNo}")
    public ResponseEntity<List<OrderItemDTO>> getOrderItemsByOrderNo(@PathVariable Integer orderNo) {
        List<OrderItemDTO> orderItems = orderItemService.getOrderItemsByOrderNo(orderNo);
        return ResponseEntity.ok(orderItems);
    }
    
    // ========== 評價管理功能 ========== //
    //** 查詢所有評價（分頁） **//
    @GetMapping("/comments")
    public ResponseEntity<Page<AdminCommentDTO>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AdminCommentDTO> comments = orderItemService.getAllComments(page, size);
        return ResponseEntity.ok(comments);
    }
    
	    
	//** 根據狀態查詢評價（分頁） **//
    @GetMapping("/comments/status/{status}")
    public ResponseEntity<Page<AdminCommentDTO>> getCommentsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AdminCommentDTO> comments = orderItemService.getCommentsByStatus(status, page, size);
        return ResponseEntity.ok(comments);
    }
    
    //** 更新評價狀態 **//
    @PutMapping("/{orderItemNo}/status")
    public ResponseEntity<String> updateCommentStatus(
            @PathVariable Integer orderItemNo,
            @Valid @RequestBody CommentStatusRequest request) {
        
        orderItemService.updateCommentStatus(orderItemNo, request.getStatus());
        
        String message = getStatusMessage(request.getStatus());
        return ResponseEntity.ok(message);
    }
    
    
    //** 批量更新評價狀態 **//
    @PutMapping("/status/batch")
    public ResponseEntity<String> batchUpdateCommentStatus(
            @Valid @RequestBody BatchCommentStatusRequest request) {
        
        orderItemService.batchUpdateCommentStatus(request.getOrderItemNos(), request.getStatus());
        
        String message = String.format("已成功更新 %d 個評價狀態為: %s", 
                                      request.getOrderItemNos().size(), 
                                      getStatusMessage(request.getStatus()));
        return ResponseEntity.ok(message);
    }
    
    
    // ========== 統計分析功能 ========== //
    //** 取得後台統計資訊 **//
    @GetMapping("/statistics")
    public ResponseEntity<AdminStatistics> getAdminStatistics() {
        AdminStatistics statistics = orderItemService.getAdminStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    
    //** 查詢高分評價（4星以上） **//
    @GetMapping("/comments/high-rating")
    public ResponseEntity<Page<AdminCommentDTO>> getHighRatingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // 注意：這個方法需要在 Service 中實作
        // Page<AdminCommentDTO> comments = orderItemService.getHighRatingComments(page, size);
        // return ResponseEntity.ok(comments);
        
        // 暫時回傳空結果，等 Service 實作後再啟用
        return ResponseEntity.ok(Page.empty());
    }
    
    
    //** 查詢低分評價（2星以下） **//
    @GetMapping("/comments/low-rating")
    public ResponseEntity<Page<AdminCommentDTO>> getLowRatingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // 注意：這個方法需要在 Service 中實作
        // Page<AdminCommentDTO> comments = orderItemService.getLowRatingComments(page, size);
        // return ResponseEntity.ok(comments);
        
        // 暫時回傳空結果，等 Service 實作後再啟用
        return ResponseEntity.ok(Page.empty());
    }
    
    
    
    // ========== 私有輔助方法 ========== //
    //** 根據狀態代碼取得中文說明 **//
    private String getStatusMessage(String status) {
        switch (status) {
            case "1":
                return "正常";
            case "0":
                return "停權";
            default:
                return "待審核";
        }
    }
    
    
    // ========== 內部類別：請求參數 ========== //
    //** 評價狀態更新請求 **//
    public static class CommentStatusRequest {
        private String status;
        private String reason; // 操作原因（可選）
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    //** 批量評價狀態更新請求 **//
    public static class BatchCommentStatusRequest {
        private List<Integer> orderItemNos;
        private String status;
        private String reason; // 批量操作原因（可選）
        
        // Getters and Setters
        public List<Integer> getOrderItemNos() { return orderItemNos; }
        public void setOrderItemNos(List<Integer> orderItemNos) { this.orderItemNos = orderItemNos; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    
    
    
    
}