package com.pixeltribe.shopsys.orderItem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.orderItem.model.CommentRequest;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService.ProductCommentStatistics;
import com.pixeltribe.util.JwtUtil;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

//  ~~~~~~~ << 前台 >> ~~~~~~~ //


@RestController
@RequestMapping("/api/orderitem")
public class OrderItemController {
	
	@Autowired
    private OrderItemService orderItemService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	
	// ========== 訂單明細查詢 ========== //
    
    //** 查詢指定訂單的所有明細 <<會員查看訂單詳情>> **//
    @GetMapping("/order/{orderNo}")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(
            @PathVariable Integer orderNo,
            HttpServletRequest request) {
        
        // 從 session 或 JWT 中取得當前登入的會員編號
        Integer currentMemNo = getCurrentMemberNo(request);
        
        List<OrderItemDTO> orderItems = orderItemService.getOrderItemsByOrderNo(orderNo, currentMemNo);
        return ResponseEntity.ok(orderItems);
    }

    
    //** 查詢會員的特定訂單明細 <<評價功能的資料驗證>> **//
    @GetMapping("/{orderItemNo}")
    public ResponseEntity<OrderItemDTO> getOrderItem(
            @PathVariable Integer orderItemNo,
            HttpServletRequest request) {
        
        Integer currentMemNo = getCurrentMemberNo(request);
        
        OrderItemDTO orderItem = orderItemService.getMemberOrderItem(orderItemNo, currentMemNo);
        return ResponseEntity.ok(orderItem);
    }
    
    
    
    // ========== 評價功能 ========== //
    //** 新增商品評價 <<會員對已購買的商品進行評價>> **//
    @PostMapping("/{orderItemNo}/comment")
    public ResponseEntity<OrderItemDTO> addComment(
            @PathVariable Integer orderItemNo,
            @Valid @RequestBody CommentRequest commentRequest,
            HttpServletRequest request) {
        
        Integer currentMemNo = getCurrentMemberNo(request);
        
        OrderItemDTO result = orderItemService.addComment(orderItemNo, currentMemNo, commentRequest);
        return ResponseEntity.ok(result);
    }
    
    
    //** 修改商品評價 <<會員修改已提交的評價>> **//
    @PutMapping("/{orderItemNo}/comment")
    public ResponseEntity<OrderItemDTO> updateComment(
            @PathVariable Integer orderItemNo,
            @Valid @RequestBody CommentRequest commentRequest,
            HttpServletRequest request) {
        
        Integer currentMemNo = getCurrentMemberNo(request);
        
        OrderItemDTO result = orderItemService.updateComment(orderItemNo, currentMemNo, commentRequest);
        return ResponseEntity.ok(result);
    }
    
    
    //** 查詢會員在特定訂單中的評價記錄 <<訂單詳情頁顯示已評價的商品>> **//
    @GetMapping("/order/{orderNo}/comments")
    public ResponseEntity<List<OrderItemDTO>> getMemberCommentsByOrder(
            @PathVariable Integer orderNo,
            HttpServletRequest request) {
        
        Integer currentMemNo = getCurrentMemberNo(request);
        
        List<OrderItemDTO> comments = orderItemService.getMemberCommentsByOrder(orderNo, currentMemNo);
        return ResponseEntity.ok(comments);
    }
    
    
    
    // ========== 產品評價查詢（公開資訊） ========== //
    //** 查詢產品的所有正常評價 <<產品詳情頁顯示其他買家評價>> **//
    @GetMapping("/product/{proNo}/comments")
    public ResponseEntity<List<OrderItemDTO>> getProductComments(@PathVariable Integer proNo) {
        List<OrderItemDTO> comments = orderItemService.getProductComments(proNo);
        return ResponseEntity.ok(comments);
    }
    
    
    //** 查詢產品的正常評價（分頁） **//
    @GetMapping("/product/{proNo}/comments/page")
    public ResponseEntity<Page<OrderItemDTO>> getProductCommentsWithPage(
            @PathVariable Integer proNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<OrderItemDTO> comments = orderItemService.getProductComments(proNo, page, size);
        return ResponseEntity.ok(comments);
    }
    
    //** 查詢產品評價統計資訊 <<產品詳情頁顯示評價統計（平均分、評價數量、星級分布)>>**//
    @GetMapping("/product/{proNo}/statistics")
    public ResponseEntity<ProductCommentStatistics> getProductCommentStatistics(@PathVariable Integer proNo) {
        ProductCommentStatistics statistics = orderItemService.getProductCommentStatistics(proNo);
        return ResponseEntity.ok(statistics);
    }
    
 	// ========== 私有輔助方法 ========== //
    //** 從 HTTP 請求中取得當前登入會員的編號 **//
    
    private Integer getCurrentMemberNo(HttpServletRequest request) {
    	// 從 JWT Token 中獲取 
    	// 方法 1：從 JWT Token 中獲取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 檢查 Token 是否有效
                if (!jwtUtil.validateToken(token)) {
                    throw new RuntimeException("Token 已過期或無效");
                }
                
                // 取得角色
                String role = jwtUtil.extractRole(token);
                
                if ("ROLE_USER".equals(role)) {
                    // 一般會員 - 提取會員 ID
                    Integer memId = extractMemberIdFromToken(token);
                    if (memId != null) {
                        return memId;
                    }
                } else if ("ROLE_ADMIN".equals(role)) {
                    // 管理員可以訪問，但返回 null（讓 Service 處理）
                    return null;
                }
                
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                }
                throw new RuntimeException("Token 解析失敗: " + e.getMessage());
            }
        }
        
        // 方法 2：備案 - 從 Session 獲取
        Integer sessionMemNo = (Integer) request.getSession().getAttribute("currentId");
        if (sessionMemNo != null) {
            return sessionMemNo;
        }
        
        throw new RuntimeException("會員未登入或認證信息無效");
    }
    
    
    // 添加這個輔助方法
    private Integer extractMemberIdFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey("PPPPPIIIIIXXXXXEEEEELLLLL_TTTTTRRRRRIIIIIBBBBBEEEEE".getBytes())
                    .parseClaimsJws(token)
                    .getBody()
                    .get("memId", Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("無法從 Token 中獲取會員 ID");
        }
    }
    
     
}