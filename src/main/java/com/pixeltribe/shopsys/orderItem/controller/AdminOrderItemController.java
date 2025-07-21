package com.pixeltribe.shopsys.orderItem.controller;

import java.util.List;
import java.util.Map;

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
import com.pixeltribe.shopsys.orderItem.model.OrderItem;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;
import com.pixeltribe.shopsys.orderItem.model.OrderItemRepository;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService.AdminStatistics;
import com.pixeltribe.util.JwtUtil;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

//~~~~~~~ << 後台 >> ~~~~~~~ //

@RestController
@RequestMapping("/api/admin/orderitem")
public class AdminOrderItemController {
    
    @Autowired
    private OrderItemService orderItemService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    
    
    //** 查詢訂單明細（後台管理用） **//
    @GetMapping("/orderno/{orderNo}")
    public ResponseEntity<List<OrderItemDTO>> getOrderItemsByOrderNo(
            @PathVariable Integer orderNo,
            HttpServletRequest request) {
        
//        validateAdminPermission(request);
        
        List<OrderItemDTO> orderItems = orderItemService.getOrderItemsByOrderNo(orderNo);
        return ResponseEntity.ok(orderItems);
    }
    
    
    //** 查詢訂單明細（新路徑） **//
    @GetMapping("/order/{orderNo}")
    public ResponseEntity<List<OrderItemDTO>> getOrderItemsByOrder(
            @PathVariable Integer orderNo,
            HttpServletRequest request) {
        
        // 暫時註解掉認證檢查
        // validateAdminPermission(request);
        
        List<OrderItemDTO> orderItems = orderItemService.getOrderItemsByOrderNo(orderNo);
        return ResponseEntity.ok(orderItems);
    }
    
    
    
    
    // ========== 評價管理功能 ========== //
    //** 查詢所有評價（分頁） **//
    @GetMapping("/comments")
    public ResponseEntity<Page<AdminCommentDTO>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        validateAdminPermission(request);
        
        Page<AdminCommentDTO> comments = orderItemService.getAllComments(page, size);
        return ResponseEntity.ok(comments);
    }
    
    //** 根據狀態查詢評價（分頁） **//
    @GetMapping("/comments/status/{status}")
    public ResponseEntity<Page<AdminCommentDTO>> getCommentsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        validateAdminPermission(request);
        
        Page<AdminCommentDTO> comments = orderItemService.getCommentsByStatus(status, page, size);
        return ResponseEntity.ok(comments);
    }
    
 // 添加批量更新 API
    @PutMapping("/status/batch")
    public ResponseEntity<Map<String, Object>> batchUpdateCommentStatus(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        validateAdminPermission(httpRequest);
        
        try {
            @SuppressWarnings("unchecked")
            List<Integer> orderItemNos = (List<Integer>) request.get("orderItemNos");
            Object statusObj = request.get("status");
            String status = String.valueOf(statusObj);
            
            if (orderItemNos == null || orderItemNos.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "請選擇要操作的評論"
                ));
            }
            
            orderItemService.batchUpdateCommentStatus(orderItemNos, status);
            
            String message = String.format("成功更新 %d 個評論狀態", orderItemNos.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量操作失敗: " + e.getMessage()
            ));
        }
    }
    
    
    
    
 // 在 AdminOrderItemController 中添加這個方法
    @GetMapping("/all")
    public ResponseEntity<?> getAllCommentsForManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        validateAdminPermission(request);
        
        // 使用既有的分頁方法
        Page<AdminCommentDTO> comments = orderItemService.getAllComments(page, size);
        
        return ResponseEntity.ok(comments);
    }
    
    // 修改更新狀態的方法，支援前端格式
    @PutMapping("/{orderItemNo}/status")
    public ResponseEntity<Map<String, Object>> updateCommentStatus(
            @PathVariable Integer orderItemNo,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        validateAdminPermission(httpRequest);
        
        try {
            // 支援兩種格式：commentStatus 或 status
            Object statusObj = request.get("commentStatus");
            if (statusObj == null) {
                statusObj = request.get("status");
            }
            
            String status = String.valueOf(statusObj);
            orderItemService.updateCommentStatus(orderItemNo, status);
            
            String message = getStatusMessage(status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    
    
    
    // ========== 統計分析功能 ========== //
    //** 取得後台統計資訊 **//
    @GetMapping("/statistics")
    public ResponseEntity<AdminStatistics> getAdminStatistics(HttpServletRequest request) {
        
        validateAdminPermission(request);
        
        AdminStatistics statistics = orderItemService.getAdminStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    //** 查詢高分評價（4星以上） **//
    @GetMapping("/comments/high-rating")
    public ResponseEntity<Page<AdminCommentDTO>> getHighRatingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        validateAdminPermission(request);
        
        // 暫時回傳空結果，等 Service 實作後再啟用
        return ResponseEntity.ok(Page.empty());
    }
    
    //** 查詢低分評價（2星以下） **//
    @GetMapping("/comments/low-rating")
    public ResponseEntity<Page<AdminCommentDTO>> getLowRatingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        validateAdminPermission(request);
        
        // 暫時回傳空結果，等 Service 實作後再啟用
        return ResponseEntity.ok(Page.empty());
    }
    
    // ========== 私有輔助方法 ========== //
    
    //** 驗證管理員權限 **//
    private void validateAdminPermission(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 檢查 Token 是否有效
                if (!jwtUtil.validateToken(token)) {
                    throw new RuntimeException("Token 已過期或無效");
                }
                
                // 檢查角色是否為管理員
                String role = jwtUtil.extractRole(token);
                if (!"ROLE_ADMIN".equals(role)) {
                    throw new RuntimeException("權限不足，需要管理員權限");
                }
                
                // 確認管理員 ID 存在
                Integer adminId = extractAdminIdFromToken(token);
                if (adminId == null) {
                    throw new RuntimeException("無法獲取管理員信息");
                }
                
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                }
                throw new RuntimeException("權限驗證失敗: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("未提供有效的認證信息");
        }
    }
    
    //** 從 Token 中提取管理員 ID **//
    private Integer extractAdminIdFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey("PPPPPIIIIIXXXXXEEEEELLLLL_TTTTTRRRRRIIIIIBBBBBEEEEE".getBytes())
                    .parseClaimsJws(token)
                    .getBody()
                    .get("admId", Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("無法從 Token 中獲取管理員 ID");
        }
    }
    
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
    
    private AdminCommentDTO convertToAdminCommentDTO(OrderItem orderItem) {
        return orderItemService.convertToAdminCommentDTO(orderItem);
    }
    
}