package com.pixeltribe.shopsys.orderItem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.orderItem.model.OrderItem;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;
import com.pixeltribe.shopsys.orderItem.model.OrderItemService;

@RestController
@RequestMapping("/api/orderitems")
@CrossOrigin(origins = "*")
public class OrderItemController {
	
	@Autowired
    private OrderItemService orderItemService;
	
	// 主要 API：根據訂單編號查詢訂單項目（支援 Integer）- 使用 DTO
    @GetMapping("/orderno/{orderNo}")
    public ResponseEntity<?> getOrderItemsByOrderNo(@PathVariable Integer orderNo) {
        try {
            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderNo(orderNo);
            // 使用簡化版 DTO，避免懶加載問題
            List<OrderItemDTO> dtos = OrderItemDTO.fromListSimple(orderItems);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace(); // 印出錯誤到 console
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 根據訂單編號查詢訂單項目（String 版本）- 使用 DTO
    @GetMapping("/orderno/str/{orderNo}")
    public ResponseEntity<?> getOrderItemsByOrderNo(@PathVariable String orderNo) {
        try {
            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderNo(orderNo);
            // 使用簡化版 DTO，避免懶加載問題
            List<OrderItemDTO> dtos = OrderItemDTO.fromListSimple(orderItems);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 取得所有訂單項目 - 使用 DTO
    @GetMapping
    public ResponseEntity<?> getAllOrderItems() {
        try {
            List<OrderItem> orderItems = orderItemService.getAllOrderItems();
            // 使用簡化版 DTO，避免懶加載問題
            List<OrderItemDTO> dtos = OrderItemDTO.fromListSimple(orderItems);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 根據訂單項目編號查詢 - 使用 DTO
    @GetMapping("/{orderItemNo}")
    public ResponseEntity<?> getOrderItemById(@PathVariable Integer orderItemNo) {
        try {
            OrderItem orderItem = orderItemService.getOrderItemById(orderItemNo);
            if (orderItem == null) {
                return ResponseEntity.notFound().build();
            }
            // 使用簡化版 DTO
            OrderItemDTO dto = OrderItemDTO.fromSimple(orderItem);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 新增或更新訂單項目（這個保持原樣，因為是接收資料）
    @PostMapping
    public ResponseEntity<?> saveOrderItem(@RequestBody OrderItem orderItem) {
        try {
            OrderItem savedItem = orderItemService.saveOrderItem(orderItem);
            // 回傳時使用 DTO
            OrderItemDTO dto = OrderItemDTO.fromSimple(savedItem);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 刪除訂單項目
    @DeleteMapping("/{orderItemNo}")
    public ResponseEntity<?> deleteOrderItem(@PathVariable Integer orderItemNo) {
        try {
            orderItemService.deleteOrderItem(orderItemNo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 測試用的安全方法（如果上面還有問題的話）
    @GetMapping("/test/orderno/{orderNo}")
    public ResponseEntity<?> testGetOrderItems(@PathVariable Integer orderNo) {
        try {
            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderNo(orderNo);
            
            // 最基本的回傳，只使用 OrderItem 自己的欄位
            List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
            for (OrderItem item : orderItems) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("orderItemNo", item.getOrderItemNo());
                map.put("orderNo", item.getOrderNo());
                map.put("proName", item.getProName());
                map.put("proPrice", item.getProPrice());
                map.put("orderAmount", item.getOrderAmount());
                map.put("productComment", item.getProductComment());
                map.put("proStar", item.getProStar());
                result.add(map);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}