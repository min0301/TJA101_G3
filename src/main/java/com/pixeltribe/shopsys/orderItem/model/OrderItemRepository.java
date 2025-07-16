package com.pixeltribe.shopsys.orderItem.model;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    
    @Query(value = "SELECT * FROM order_item WHERE order_no = :orderNo", nativeQuery = true)
    List<OrderItem> findByOrderNoNative(@Param("orderNo") Integer orderNo);
    
    List<OrderItem> findByOrder_OrderNo(Integer orderNo);
    
    // ========== 簡單的基礎查詢 ========== //
    
    // 根據產品編號查詢
    List<OrderItem> findByProNo_Id(Integer proNo);
    
    // 根據會員編號查詢（通過訂單)
    @Query(value = "SELECT oi.* FROM order_item oi " +
            "JOIN orders o ON oi.order_no = o.order_no " +
            "WHERE o.mem_no = :memNo " +
            "ORDER BY o.order_date DESC", 
            nativeQuery = true)
    List<OrderItem> findByMemberNo(@Param("memNo") Integer memNo);
    
    // 根據會員編號查詢（分頁）
    @Query(value = "SELECT oi.* FROM order_item oi " +
            "JOIN orders o ON oi.order_no = o.order_no " +
            "WHERE o.mem_no = :memNo " +
            "ORDER BY o.order_date DESC", 
    nativeQuery = true)
    Page<OrderItem> findByMemberNoWithPage(@Param("memNo") Integer memNo, Pageable pageable);
    
    // 根據訂單狀態查詢
    List<OrderItem> findByOrder_OrderStatus(String orderStatus);
    
    // 查詢特定會員的特定訂單明細
    @Query(value = "SELECT oi.* FROM order_item oi " +
            "JOIN orders o ON oi.order_no = o.order_no " +
            "WHERE oi.order_item_no = :orderItemNo " +
            "AND o.mem_no = :memNo", 
    nativeQuery = true)
    Optional<OrderItem> findByOrderItemNoAndMemberNo(@Param("orderItemNo") Integer orderItemNo, @Param("memNo") Integer memNo);
    
    
    
    // ========== 評價相關查詢 ========== //
    
    // *** 查詢產品的所有正常評價（前台用）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE pro_no = :proNo " +
                   "AND (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "AND pro_com_status = '1' " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    List<OrderItem> findProductNormalComments(@Param("proNo") Integer proNo);
    
    // *** 查詢產品的所有評價（包含停權，後台用）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE pro_no = :proNo " +
                   "AND (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    List<OrderItem> findProductComments(@Param("proNo") Integer proNo);
    
    // *** 查詢產品的正常評價（分頁）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE pro_no = :proNo " +
                   "AND (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "AND pro_com_status = '1' " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    Page<OrderItem> findProductNormalCommentsWithPage(@Param("proNo") Integer proNo, Pageable pageable);
    
    // *** 查詢所有評價（後台用）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    List<OrderItem> findAllComments();
    
    // *** 查詢所有評價（後台用，分頁）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    Page<OrderItem> findAllCommentsWithPage(Pageable pageable);
    
    // *** 根據評價狀態查詢評價（分頁）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "AND pro_com_status = :status " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    Page<OrderItem> findCommentsByStatus(@Param("status") String status, Pageable pageable);
    
    // *** 查詢會員的評價記錄 *** //
    @Query(value = "SELECT oi.* FROM order_item oi " +
                   "JOIN orders o ON oi.order_no = o.order_no " +
                   "WHERE o.mem_no = :memNo " +
                   "AND (oi.product_comment IS NOT NULL OR oi.pro_star IS NOT NULL) " +
                   "ORDER BY oi.product_comment_crdate DESC", 
           nativeQuery = true)
    List<OrderItem> findMemberComments(@Param("memNo") Integer memNo);
    
    // *** 查詢會員可以評價的訂單明細（已完成且未評價）*** //
    @Query(value = "SELECT oi.* FROM order_item oi " +
                   "JOIN orders o ON oi.order_no = o.order_no " +
                   "WHERE o.mem_no = :memNo " +
                   "AND o.order_status = '2' " +
                   "AND oi.product_comment IS NULL " +
                   "AND oi.pro_star IS NULL " +
                   "ORDER BY o.order_date DESC", 
           nativeQuery = true)
    List<OrderItem> findMemberCanCommentItems(@Param("memNo") Integer memNo);
    
    
    
    // ========== 統計查詢 ========== //
    
    // *** 統計產品的評價總數 *** //
    @Query(value = "SELECT COUNT(*) FROM order_item " +
                   "WHERE pro_no = :proNo " +
                   "AND (product_comment IS NOT NULL OR pro_star IS NOT NULL)", 
           nativeQuery = true)
    Long countProductComments(@Param("proNo") Integer proNo);
    
    // *** 統計產品的正常評價數量 *** //
    @Query(value = "SELECT COUNT(*) FROM order_item " +
                   "WHERE pro_no = :proNo " +
                   "AND (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "AND pro_com_status = '1'", 
           nativeQuery = true)
    Long countProductNormalComments(@Param("proNo") Integer proNo);
    
    // *** 計算產品的平均評分 *** //
    @Query(value = "SELECT AVG(pro_star) FROM order_item " +
                   "WHERE pro_no = :proNo " +
                   "AND pro_star IS NOT NULL " +
                   "AND pro_com_status = '1'", 
           nativeQuery = true)
    Double calculateProductAverageRating(@Param("proNo") Integer proNo);
    
    // *** 統計各星級評價數量 *** //
    @Query(value = "SELECT pro_star, COUNT(*) FROM order_item " +
                   "WHERE pro_no = :proNo " +
                   "AND pro_star IS NOT NULL " +
                   "AND pro_com_status = '1' " +
                   "GROUP BY pro_star " +
                   "ORDER BY pro_star DESC", 
           nativeQuery = true)
    List<Object[]> countProductRatingDistribution(@Param("proNo") Integer proNo);
    
    // *** 統計會員的評價總數 *** //
    @Query(value = "SELECT COUNT(*) FROM order_item oi " +
                   "JOIN orders o ON oi.order_no = o.order_no " +
                   "WHERE o.mem_no = :memNo " +
                   "AND (oi.product_comment IS NOT NULL OR oi.pro_star IS NOT NULL)", 
           nativeQuery = true)
    Long countMemberComments(@Param("memNo") Integer memNo);
    
    // *** 統計停權評價數量 *** //
    @Query(value = "SELECT COUNT(*) FROM order_item " +
                   "WHERE pro_com_status = '0'", 
           nativeQuery = true)
    Long countBlockedComments();
    
    // *** 統計待審核評價數量（狀態為空的評價）*** //
    @Query(value = "SELECT COUNT(*) FROM order_item " +
                   "WHERE (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "AND pro_com_status IS NULL", 
           nativeQuery = true)
    Long countPendingComments();
    
    
    
    // ========== 更新操作 ========== //
    
    // *** 更新評價狀態 *** //
    @Modifying
    @Query(value = "UPDATE order_item SET pro_com_status = :status " +
                   "WHERE order_item_no = :orderItemNo", 
           nativeQuery = true)
    int updateCommentStatus(@Param("orderItemNo") Integer orderItemNo, 
                           @Param("status") String status);
    
    // *** 批量更新評價狀態 *** //
    @Modifying
    @Query(value = "UPDATE order_item SET pro_com_status = :status " +
                   "WHERE order_item_no IN (:orderItemNos)", 
           nativeQuery = true)
    int batchUpdateCommentStatus(@Param("orderItemNos") List<Integer> orderItemNos, 
                                @Param("status") String status);
    
    
    
    // ========== 複合查詢 ========== //
    
    // *** 查詢特定時間範圍的評價 *** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE (product_comment IS NOT NULL OR pro_star IS NOT NULL) " +
                   "AND product_comment_crdate BETWEEN :startDate AND :endDate " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    List<OrderItem> findCommentsByDateRange(@Param("startDate") java.time.Instant startDate, 
                                           @Param("endDate") java.time.Instant endDate);
    
    // *** 查詢高分評價（4星以上，分頁）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE pro_star >= 4 " +
                   "AND pro_com_status = '1' " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    Page<OrderItem> findHighRatingCommentsWithPage(Pageable pageable);

    // *** 查詢低分評價（2星以下，分頁）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE pro_star <= 2 " +
                   "AND pro_com_status = '1' " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    Page<OrderItem> findLowRatingCommentsWithPage(Pageable pageable);
    
    // *** 搜尋評價內容（關鍵字搜尋）*** //
    @Query(value = "SELECT * FROM order_item " +
                   "WHERE product_comment LIKE %:keyword% " +
                   "AND pro_com_status = '1' " +
                   "ORDER BY product_comment_crdate DESC", 
           nativeQuery = true)
    Page<OrderItem> searchCommentsByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    
    
    // ========== 關聯查詢 ========== //
    
    // *** 查詢訂單明細並載入所有關聯資料 *** //
    @Query(value = "SELECT oi.*, o.*, p.* FROM order_item oi " +
                   "LEFT JOIN orders o ON oi.order_no = o.order_no " +
                   "LEFT JOIN product p ON oi.pro_no = p.id " +
                   "WHERE oi.order_item_no = :orderItemNo", 
           nativeQuery = true)
    Optional<OrderItem> findWithAllAssociations(@Param("orderItemNo") Integer orderItemNo);
    
    // *** 查詢會員的訂單明細並載入關聯資料 *** //
    @Query(value = "SELECT oi.*, o.*, p.* FROM order_item oi " +
                   "LEFT JOIN orders o ON oi.order_no = o.order_no " +
                   "LEFT JOIN product p ON oi.pro_no = p.id " +
                   "WHERE o.mem_no = :memNo " +
                   "ORDER BY o.order_date DESC", 
           nativeQuery = true)
    List<OrderItem> findMemberOrderItemsWithAssociations(@Param("memNo") Integer memNo);
    
    // *** 查詢產品評價並載入會員資訊 *** //
    @Query(value = "SELECT oi.*, o.*, p.* FROM order_item oi " +
                   "LEFT JOIN orders o ON oi.order_no = o.order_no " +
                   "LEFT JOIN product p ON oi.pro_no = p.id " +
                   "WHERE oi.pro_no = :proNo " +
                   "AND (oi.product_comment IS NOT NULL OR oi.pro_star IS NOT NULL) " +
                   "AND oi.pro_com_status = '1' " +
                   "ORDER BY oi.product_comment_crdate DESC", 
           nativeQuery = true)
    List<OrderItem> findProductCommentsWithMemberInfo(@Param("proNo") Integer proNo);
}