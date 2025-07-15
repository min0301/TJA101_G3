package com.pixeltribe.shopsys.order.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // ========== 核心查詢方法 ========== //
    
    //*** 根據訂單編號查詢訂單 <<使用於：OrderService, AdminOrderService>> *** //
    Order findByOrderNo(Integer orderNo);
    
    // *** 根據會員編號查詢訂單，按時間降序排列（最新的在前）使用於：OrderService.getmemOrders() ***//
    @Query(value = "SELECT * FROM `order` WHERE MEM_NO = :memNo ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findByMemNoOrderByOrderDatetimeDesc(@Param("memNo") Integer memNo);
    
    // *** 根據狀態查詢訂單，按時間降序排列 << 保留以備未來使用>> *** //
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS = :orderStatus ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findByOrderStatusOrderByDatetimeDesc(@Param("orderStatus") String orderStatus);
    
    
    // ========== 統計查詢方法 ========== //
    
    // ***根據狀態統計訂單數量 ***//
    Long countByOrderStatus(String orderStatus);
    
    // *** 統計今日訂單數量  使用於：AdminOrderService統計功能 *** //
    @Query(value = "SELECT COUNT(*) FROM `order` WHERE DATE(ORDER_DATETIME) = CURDATE()", nativeQuery = true)
    Long countTodayOrders();
    
    // *** 統計本月訂單數量  使用於：AdminOrderService統計功能 *** //
    @Query(value = "SELECT COUNT(*) FROM `order` WHERE YEAR(ORDER_DATETIME) = YEAR(CURDATE()) AND MONTH(ORDER_DATETIME) = MONTH(CURDATE())", nativeQuery = true)
    Long countThisMonthOrders();
    
    // *** 統計總銷售額（已完成和已發貨的訂單）使用於：AdminOrderService統計功能 *** //
    @Query(value = "SELECT COALESCE(SUM(ORDER_TOTAL), 0) FROM `order` WHERE ORDER_STATUS IN ('COMPLETED', 'SHIPPED')", nativeQuery = true)
    Long getTotalSales();
    
    // ***  統計今日銷售額  使用於：AdminOrderService統計功能 *** //
    @Query(value = "SELECT COALESCE(SUM(ORDER_TOTAL), 0) FROM `order` WHERE ORDER_STATUS IN ('COMPLETED', 'SHIPPED') AND DATE(ORDER_DATETIME) = CURDATE()", nativeQuery = true)
    Long getTodaySales();
    
    // *** 計算平均訂單金額   使用於：AdminOrderService統計功能 *** //
    @Query(value = "SELECT COALESCE(AVG(ORDER_TOTAL), 0) FROM `order` WHERE ORDER_STATUS IN ('COMPLETED', 'SHIPPED')", nativeQuery = true)
    Double getAverageOrderValue();
    
    
    // ========== 特殊查詢方法 ========== //
    
    // *** 查詢會員的特定狀態訂單 *** //
    @Query(value = "SELECT * FROM `order` WHERE MEM_NO = :memNo AND ORDER_STATUS = :orderStatus ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findByMemNoAndOrderStatus(@Param("memNo") Integer memNo, @Param("orderStatus") String orderStatus);
    
    // *** 查詢進行中的付款訂單 使用於：PaymentService定時任務 *** //
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS = 'PAYING' ORDER BY ORDER_DATETIME ASC", nativeQuery = true)
    List<Order> findPayingOrders();
    
    // *** 查詢超過指定時間的未付款訂單（用於清理超時訂單） 使用於：PaymentService超時處理 *** //
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS IN ('PENDING', 'PAYING') AND ORDER_DATETIME < :cutoffTime", nativeQuery = true)
    List<Order> findTimeoutOrders(@Param("cutoffTime") Instant cutoffTime);
    
    // *** 查詢需要發貨的訂單（已付款但未發貨） 使用於：發貨流程 *** //
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS = 'PROCESSING' ORDER BY ORDER_DATETIME ASC", nativeQuery = true)
    List<Order> findOrdersToShip();
}