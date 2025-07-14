package com.pixeltribe.shopsys.order.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
	

	// 跟訂單查詢狀態查詢
	List<Order> findByOrderStatus(String Status);
	Order findByOrderNo(Integer orderNo);
	
    // AdminOrderService
    List<Order> findAllByOrderByOrderDatetimeDesc();
    
    // AdminOrderService
    List<Order> findByOrderStatusOrderByOrderDatetimeDesc(String orderStatus);

    
    // ========== 會員相關查詢方法 ========== //
    // ** 根據會員編號查詢訂單，按時間降序排列（最新的在前） ** //
    @Query(value = "SELECT * FROM `order` o WHERE o.MEM_NO = :memNo ORDER BY o.ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findByMemNoOrderByOrderDatetimeDesc(@Param("memNo") Integer memNo);
	
    // ** 根據會員編號查詢訂單，按訂單編號降序排列 ** //
    @Query(value = "SELECT * FROM `order` o WHERE o.MEM_NO = :memNo ORDER BY o.ORDER_NO DESC", nativeQuery = true)
    List<Order> findByMemNoOrderByOrderNoDesc(@Param("memNo") Integer memNo);
    
    // ** 根據會員編號查詢訂單 ** //
    @Query(value = "SELECT * FROM `order` o WHERE o.MEM_NO = :memId ORDER BY o.ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findOrdersByMemberId(@Param("memId") Integer memNo);
    
    
    
    // ========== 後台管理查詢方法 ========== //
    // ** 查詢所有訂單，按時間降序排列（後台管理用） ** //
    @Query(value = "SELECT * FROM `order` ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findAllOrdersByDatetimeDesc();
    
    // ** 根據狀態查詢訂單，按時間降序排列（後台管理用） ** //
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS = :orderStatus ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findByOrderStatusOrderByDatetimeDesc(@Param("orderStatus") String orderStatus);
    
    
    // ** 查詢指定日期範圍內的訂單 ** //
    @Query(value = "SELECT * FROM `order` WHERE DATE(ORDER_DATETIME) BETWEEN :startDate AND :endDate ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findOrdersByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    // ** 查詢指定金額範圍內的訂單 ** //
    @Query(value = "SELECT * FROM `order` WHERE ORDER_TOTAL BETWEEN :minAmount AND :maxAmount ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findOrdersByAmountRange(@Param("minAmount") Integer minAmount, @Param("maxAmount") Integer maxAmount);
    
    
    
    // ========== 統計查詢方法 ========== //
    // ** 根據時間範圍統計訂單數量 ** //
    @Query(value = "SELECT COUNT(*) FROM `order` WHERE ORDER_DATETIME BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countByOrderDatetimeBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
    
    // ** 根據狀態統計訂單數量 ** //
    Long countByOrderStatus(String orderStatus);
    
    //** 統計今日訂單數量 ** //
    @Query(value = "SELECT COUNT(*) FROM `order` WHERE DATE(ORDER_DATETIME) = CURDATE()", nativeQuery = true)
    Long countTodayOrders();
    
    //** 統計本月訂單數量 ** //
    @Query(value = "SELECT COUNT(*) FROM `order` WHERE YEAR(ORDER_DATETIME) = YEAR(CURDATE()) AND MONTH(ORDER_DATETIME) = MONTH(CURDATE())", nativeQuery = true)
    Long countThisMonthOrders();
    
    //** 統計總銷售額（已完成和已發貨的訂單）**//
    @Query(value = "SELECT COALESCE(SUM(ORDER_TOTAL), 0) FROM `order` WHERE ORDER_STATUS IN ('COMPLETED', 'SHIPPED')", nativeQuery = true)
    Long getTotalSales();
    
    //** 統計今日銷售額 **//
    @Query(value = "SELECT COALESCE(SUM(ORDER_TOTAL), 0) FROM `order` WHERE ORDER_STATUS IN ('COMPLETED', 'SHIPPED') AND DATE(ORDER_DATETIME) = CURDATE()", nativeQuery = true)
    Long getTodaySales();
    
    //** 計算平均訂單金額 **//
    @Query(value = "SELECT COALESCE(AVG(ORDER_TOTAL), 0) FROM `order` WHERE ORDER_STATUS IN ('COMPLETED', 'SHIPPED')", nativeQuery = true)
    Double getAverageOrderValue();
    
    
    // ========== 其他查詢方法 ========== //
    //** 查詢會員的特定狀態訂單 **//
    @Query(value = "SELECT * FROM `order` WHERE MEM_NO = :memNo AND ORDER_STATUS = :orderStatus ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findByMemNoAndOrderStatus(@Param("memNo") Integer memNo, @Param("orderStatus") String orderStatus);
    
    
    //** 查詢可取消的訂單（待付款或失敗狀態） **//
    @Query(value = "SELECT * FROM `order` WHERE MEM_NO = :memNo AND ORDER_STATUS IN ('PENDING', 'FAILED') ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findCancellableOrdersByMemNo(@Param("memNo") Integer memNo);
    
    //** 查詢進行中的付款訂單 **//
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS = 'PAYING' ORDER BY ORDER_DATETIME ASC", nativeQuery = true)
    List<Order> findPayingOrders();
    
    
    //** 查詢超過指定時間的未付款訂單（用於清理超時訂單） **//
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS IN ('PENDING', 'PAYING') AND ORDER_DATETIME < :cutoffTime", nativeQuery = true)
    List<Order> findTimeoutOrders(@Param("cutoffTime") Instant cutoffTime);
    
    //** 查詢需要發貨的訂單（已付款但未發貨） **//
    @Query(value = "SELECT * FROM `order` WHERE ORDER_STATUS = 'PROCESSING' ORDER BY ORDER_DATETIME ASC", nativeQuery = true)
    List<Order> findOrdersToShip();
    
    //** 根據訂單編號範圍查詢 **//
    @Query(value = "SELECT * FROM `order` WHERE ORDER_NO BETWEEN :startOrderNo AND :endOrderNo ORDER BY ORDER_NO DESC", nativeQuery = true)
    List<Order> findByOrderNoRange(@Param("startOrderNo") Integer startOrderNo, @Param("endOrderNo") Integer endOrderNo);
    
    
    //** 查詢使用了優惠券的訂單 **//
    @Query(value = "SELECT * FROM `order` WHERE COUPON_WALLET_NO IS NOT NULL ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findOrdersWithCoupons();
    
    //** 查詢指定會員的優惠券使用記錄 **//
    @Query(value = "SELECT * FROM `order` WHERE MEM_NO = :memNo AND COUPON_WALLET_NO IS NOT NULL ORDER BY ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findCouponUsageByMember(@Param("memNo") Integer memNo);
    

    
    // ========== 複合查詢方法（多條件） ========== //
    //** 複合查詢：根據會員、狀態、日期範圍查詢訂單 **//
    @Query(value = "SELECT * FROM `order` o WHERE " +
            "(:memNo IS NULL OR o.MEM_NO = :memNo) AND " +
            "(:orderStatus IS NULL OR o.ORDER_STATUS = :orderStatus) AND " +
            "(:startDate IS NULL OR DATE(o.ORDER_DATETIME) >= :startDate) AND " +
            "(:endDate IS NULL OR DATE(o.ORDER_DATETIME) <= :endDate) " +
            "ORDER BY o.ORDER_DATETIME DESC", nativeQuery = true)
     List<Order> findOrdersByMultipleConditions(
         @Param("memNo") Integer memNo,
         @Param("orderStatus") String orderStatus,
         @Param("startDate") String startDate,
         @Param("endDate") String endDate
     );
    
    
  
}