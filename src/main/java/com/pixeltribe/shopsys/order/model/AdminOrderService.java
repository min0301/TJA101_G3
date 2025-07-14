package com.pixeltribe.shopsys.order.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.pixeltribe.shopsys.coupon.model.Coupon;
import com.pixeltribe.shopsys.couponWallet.model.CouponWallet;
import com.pixeltribe.shopsys.order.exception.OrderNotFoundException;
import com.pixeltribe.shopsys.orderItem.model.OrderItem;
import com.pixeltribe.shopsys.orderItem.model.OrderItemRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AdminOrderService {
	
	@Autowired
    private OrderService orderService;  // 重用前台服務
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // ========== 基本查詢功能 ========== //
    // ***** 查所有訂單 ***** //
    public List<AdminOrderDTO> getAllOrders() {
        try {
            log.debug("後台查詢所有訂單");
            
            List<Order> orders = orderRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "orderDatetime")
                );
            return orders.stream()
                        .map(this::convertToAdminOrderDTO)
                        .collect(Collectors.toList());
                        
        } catch (Exception e) {
            log.error("後台查詢所有訂單失敗", e);
            throw new RuntimeException("查詢訂單失敗：" + e.getMessage());
        }
    }
    
    
    // ***** 分頁查詢訂單訂單 ***** //
    public Map<String, Object> getPagedOrders(Integer page, Integer size, String sortBy, String sortDir) {
        try {
            log.debug("後台分頁查詢訂單：page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
            
            // 設定排序
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 執行查詢
            Page<Order> orderPage = orderRepository.findAll(pageable);
            
            // 轉換為後台 DTO
            List<AdminOrderDTO> orderDTOs = orderPage.getContent().stream()
                                                   .map(this::convertToAdminOrderDTO)
                                                   .collect(Collectors.toList());
            
            // 建立 Map 回應
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderDTOs);
            response.put("currentPage", page);
            response.put("totalPages", orderPage.getTotalPages());
            response.put("totalElements", orderPage.getTotalElements());
            response.put("pageSize", size);
            response.put("hasNext", orderPage.hasNext());
            response.put("hasPrevious", orderPage.hasPrevious());
            
            log.debug("分頁查詢完成：total={}, pages={}", orderPage.getTotalElements(), orderPage.getTotalPages());
            
            return response;
            
        } catch (Exception e) {
            log.error("後台分頁查詢訂單失敗：page={}, size={}", page, size, e);
            throw new RuntimeException("分頁查詢訂單失敗：" + e.getMessage());
        }
    }
    
    
    // ***** 按狀態查詢訂單 ***** //
    public List<AdminOrderDTO> getOrdersByStatus(String status) {
        try {
            log.debug("按狀態查詢訂單：status={}", status);
            
            List<Order> allOrders = orderRepository.findAll();
            List<Order> orders = allOrders.stream()
                    .filter(order -> status.equals(order.getOrderStatus()))
                    .sorted((o1, o2) -> o2.getOrderDatetime().compareTo(o1.getOrderDatetime()))
                    .collect(Collectors.toList());
            
            return orders.stream()
                        .map(this::convertToAdminOrderDTO)
                        .collect(Collectors.toList());
                        
        } catch (Exception e) {
            log.error("按狀態查詢訂單失敗：status={}", status, e);
            throw new RuntimeException("查詢訂單失敗：" + e.getMessage());
        }
    }
    
    // ***** 使用多種條件查詢訂單 ***** //
    public Map<String, Object> searchOrders(Map<String, Object> searchCriteria) {
        try {
            log.debug("多條件搜尋訂單：{}", searchCriteria);
            
            // 直接使用 Repository 查詢，避免 JdbcTemplate 的 deprecated 方法
            List<Order> allOrders = orderRepository.findAll();
            
            // 使用 Stream 進行條件過濾
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order -> {
                        // 訂單編號過濾
                        Object orderNoObj = searchCriteria.get("orderNo");
                        if (orderNoObj != null) {
                            Integer orderNo = (orderNoObj instanceof Integer) ? (Integer) orderNoObj : 
                                             Integer.parseInt(orderNoObj.toString());
                            return order.getOrderNo().equals(orderNo);
                        }
                        return true;
                    })
                    .filter(order -> {
                        // 會員編號過濾
                        Object memNoObj = searchCriteria.get("memNo");
                        if (memNoObj != null) {
                            Integer memNo = (memNoObj instanceof Integer) ? (Integer) memNoObj : 
                                           Integer.parseInt(memNoObj.toString());
                            return order.getMemNo() != null && 
                                   order.getMemNo().getId().equals(memNo);
                        }
                        return true;
                    })
                    .filter(order -> {
                        // 訂單狀態過濾
                        Object orderStatusObj = searchCriteria.get("orderStatus");
                        if (orderStatusObj != null) {
                            String orderStatus = orderStatusObj.toString();
                            return orderStatus.equals(order.getOrderStatus());
                        }
                        return true;
                    })
                    .filter(order -> {
                        // 開始日期過濾
                        Object startDateObj = searchCriteria.get("startDate");
                        if (startDateObj != null) {
                            try {
                                LocalDate startDate = LocalDate.parse(startDateObj.toString());
                                LocalDate orderDate = order.getOrderDatetime()
                                        .atZone(ZoneId.systemDefault()).toLocalDate();
                                return !orderDate.isBefore(startDate);
                            } catch (Exception e) {
                                log.warn("解析開始日期失敗：{}", startDateObj);
                                return true;
                            }
                        }
                        return true;
                    })
                    .filter(order -> {
                        // 結束日期過濾
                        Object endDateObj = searchCriteria.get("endDate");
                        if (endDateObj != null) {
                            try {
                                LocalDate endDate = LocalDate.parse(endDateObj.toString());
                                LocalDate orderDate = order.getOrderDatetime()
                                        .atZone(ZoneId.systemDefault()).toLocalDate();
                                return !orderDate.isAfter(endDate);
                            } catch (Exception e) {
                                log.warn("解析結束日期失敗：{}", endDateObj);
                                return true;
                            }
                        }
                        return true;
                    })
                    .filter(order -> {
                        // 最小金額過濾
                        Object minAmountObj = searchCriteria.get("minAmount");
                        if (minAmountObj != null) {
                            try {
                                Integer minAmount = (minAmountObj instanceof Integer) ? (Integer) minAmountObj : 
                                                  Integer.parseInt(minAmountObj.toString());
                                return order.getOrderTotal() >= minAmount;
                            } catch (Exception e) {
                                log.warn("解析最小金額失敗：{}", minAmountObj);
                                return true;
                            }
                        }
                        return true;
                    })
                    .filter(order -> {
                        // 最大金額過濾
                        Object maxAmountObj = searchCriteria.get("maxAmount");
                        if (maxAmountObj != null) {
                            try {
                                Integer maxAmount = (maxAmountObj instanceof Integer) ? (Integer) maxAmountObj : 
                                                  Integer.parseInt(maxAmountObj.toString());
                                return order.getOrderTotal() <= maxAmount;
                            } catch (Exception e) {
                                log.warn("解析最大金額失敗：{}", maxAmountObj);
                                return true;
                            }
                        }
                        return true;
                    })
                    .filter(order -> {
                        // 會員信箱過濾
                        Object contactEmailObj = searchCriteria.get("contactEmail");
                        if (contactEmailObj != null) {
                            String contactEmail = contactEmailObj.toString().toLowerCase();
                            return order.getMemNo() != null && 
                                   order.getMemNo().getMemEmail() != null &&
                                   order.getMemNo().getMemEmail().toLowerCase().contains(contactEmail);
                        }
                        return true;
                    })
                    .sorted((o1, o2) -> o2.getOrderDatetime().compareTo(o1.getOrderDatetime())) // 按時間降序排列
                    .collect(Collectors.toList());
            
            // 轉換為 AdminOrderDTO
            List<AdminOrderDTO> orderDTOs = filteredOrders.stream()
                        .map(this::convertToAdminOrderDTO)
                        .collect(Collectors.toList());
            
            // 建立 Map 回應
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderDTOs);
            response.put("totalCount", orderDTOs.size());
            response.put("success", true);
            
            log.debug("多條件搜尋完成：找到 {} 筆訂單", orderDTOs.size());
            
            return response;
                            
        } catch (Exception e) {
            log.error("多條件搜尋訂單失敗：{}", searchCriteria, e);
            
            // 建立錯誤回應
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "搜尋訂單失敗：" + e.getMessage());
            errorResponse.put("orders", new ArrayList<>());
            errorResponse.put("totalCount", 0);
            
            return errorResponse;
        }
    }
    
    
    // ========== 訂單統計功能 ========== //
    // ***** 獲取訂單統計資料 ***** //
    public OrderStatisticsDTO getOrderStatistics() {
        try {
            log.debug("計算訂單統計資料");
            
            OrderStatisticsDTO statistics = new OrderStatisticsDTO();
            
            // 總訂單數
            String totalSQL = "SELECT COUNT(*) FROM `order`";
            statistics.setTotalOrders(jdbcTemplate.queryForObject(totalSQL, Long.class));
            
            // 今日訂單數
            String todaySQL = "SELECT COUNT(*) FROM `order` WHERE DATE(order_datetime) = CURDATE()";
            statistics.setTodayOrders(jdbcTemplate.queryForObject(todaySQL, Long.class));
            
            // 本月訂單數
            String monthSQL = "SELECT COUNT(*) FROM `order` WHERE YEAR(order_datetime) = YEAR(CURDATE()) AND MONTH(order_datetime) = MONTH(CURDATE())";
            statistics.setThisMonthOrders(jdbcTemplate.queryForObject(monthSQL, Long.class));
            
            // 各狀態訂單數
            Map<String, Long> statusCounts = new HashMap<>();
            String statusSQL = "SELECT order_status, COUNT(*) as count FROM `order` GROUP BY order_status";
            List<Map<String, Object>> statusResults = jdbcTemplate.queryForList(statusSQL);
            for (Map<String, Object> row : statusResults) {
                statusCounts.put((String) row.get("order_status"), ((Number) row.get("count")).longValue());
            }
            statistics.setStatusCounts(statusCounts);
            
            // 總銷售額
            String totalSalesSQL = "SELECT COALESCE(SUM(order_total), 0) FROM `order` WHERE order_status IN ('COMPLETED', 'SHIPPED')";
            statistics.setTotalSales(jdbcTemplate.queryForObject(totalSalesSQL, Long.class));
            
            // 今日銷售額
            String todaySalesSQL = "SELECT COALESCE(SUM(order_total), 0) FROM `order` WHERE order_status IN ('COMPLETED', 'SHIPPED') AND DATE(order_datetime) = CURDATE()";
            statistics.setTodaySales(jdbcTemplate.queryForObject(todaySalesSQL, Long.class));
            
            // 平均訂單金額
            String avgOrderSQL = "SELECT COALESCE(AVG(order_total), 0) FROM `order` WHERE order_status IN ('COMPLETED', 'SHIPPED')";
            statistics.setAverageOrderValue(jdbcTemplate.queryForObject(avgOrderSQL, Double.class));
            
            log.debug("訂單統計計算完成：total={}, today={}", statistics.getTotalOrders(), statistics.getTodayOrders());
            
            return statistics;
            
        } catch (Exception e) {
            log.error("計算訂單統計失敗", e);
            throw new RuntimeException("計算統計資料失敗：" + e.getMessage());
        }
    }
    
    
    // ***** 獲取指定期間的訂單統計 ***** //
    public OrderStatisticsDTO getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("計算期間訂單統計：{} ~ {}", startDate, endDate);
            
            OrderStatisticsDTO statistics = new OrderStatisticsDTO();
            
            String startDateStr = startDate.toString();
            String endDateStr = endDate.toString();
            
            // 期間訂單數
            String countSQL = "SELECT COUNT(*) FROM `order` WHERE DATE(order_datetime) BETWEEN ? AND ?";
            Long periodOrders = jdbcTemplate.queryForObject(countSQL, Long.class, startDateStr, endDateStr);
            statistics.setPeriodOrders(periodOrders);
            
            // 期間各狀態訂單數
            Map<String, Long> statusCounts = new HashMap<>();
            String statusCountSQL = "SELECT order_status, COUNT(*) as count FROM `order` WHERE DATE(order_datetime) BETWEEN ? AND ? GROUP BY order_status";
            
            List<Map<String, Object>> statusResults = jdbcTemplate.queryForList(statusCountSQL, startDateStr, endDateStr);
            for (Map<String, Object> row : statusResults) {
                statusCounts.put((String) row.get("order_status"), ((Number) row.get("count")).longValue());
            }
            statistics.setStatusCounts(statusCounts);
            
            // 期間銷售額
            String periodSalesSQL = "SELECT COALESCE(SUM(order_total), 0) FROM `order` WHERE order_status IN ('COMPLETED', 'SHIPPED') AND DATE(order_datetime) BETWEEN ? AND ?";
            statistics.setPeriodSales(jdbcTemplate.queryForObject(periodSalesSQL, Long.class, startDateStr, endDateStr));
            
            return statistics;
            
        } catch (Exception e) {
            log.error("計算期間訂單統計失敗：{} ~ {}", startDate, endDate, e);
            throw new RuntimeException("計算期間統計失敗：" + e.getMessage());
        }
    }
    
    
    // ========== 訂單管理功能 ========== //
    // ***** 管理員取消訂單 ***** //
    public boolean adminCancelOrder(Integer orderNo, String adminReason, String adminId) {
        try {
            log.info("管理員取消訂單：orderNo={}, admin={}, reason={}", orderNo, adminId, adminReason);
            
            Order order = orderRepository.findByOrderNo(orderNo);
            if (order == null) {
                throw new OrderNotFoundException("訂單不存在：" + orderNo);
            }
            
            // 檢查訂單是否可以取消
            if (!canAdminCancelOrder(order.getOrderStatus())) {
                log.warn("訂單狀態不允許管理員取消：orderNo={}, status={}", orderNo, order.getOrderStatus());
                return false;
            }
            
            // 更新訂單狀態
            orderService.updateOrderStatus(orderNo, "CANCELLED");
            
            // 記錄管理員操作日誌
            log.info("ADMIN_ORDER_CANCEL|orderNo={}|admin={}|reason={}|timestamp={}", 
                    orderNo, adminId, adminReason, System.currentTimeMillis());
            
            return true;
            
        } catch (Exception e) {
            log.error("管理員取消訂單失敗：orderNo={}", orderNo, e);
            return false;
        }
    }
    
    // ***** 批量更新訂單狀態 ***** //
    public Map<String, Object> bulkUpdateStatus(List<Integer> orderIds, String newStatus, String adminId) {
        try {
            log.info("批量更新訂單狀態：orderIds={}, newStatus={}, admin={}", orderIds, newStatus, adminId);
            
            int totalCount = orderIds.size();
            int successCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Integer orderNo : orderIds) {
                try {
                    // 檢查訂單是否存在
                    Order order = orderRepository.findByOrderNo(orderNo);
                    if (order == null) {
                        errors.add("訂單 " + orderNo + " 不存在");
                        continue;
                    }
                    
                    // 檢查狀態轉換是否合法
                    if (!isValidStatusTransition(order.getOrderStatus(), newStatus)) {
                        errors.add("訂單 " + orderNo + " 狀態轉換不合法：" + order.getOrderStatus() + " -> " + newStatus);
                        continue;
                    }
                    
                    // 更新狀態
                    orderService.updateOrderStatus(orderNo, newStatus);
                    successCount++;
                    
                    // 記錄管理員操作
                    log.info("ADMIN_BULK_STATUS_UPDATE|orderNo={}|admin={}|oldStatus={}|newStatus={}|timestamp={}", 
                            orderNo, adminId, order.getOrderStatus(), newStatus, System.currentTimeMillis());
                    
                } catch (Exception e) {
                    errors.add("訂單 " + orderNo + " 更新失敗：" + e.getMessage());
                    log.error("批量更新單個訂單失敗：orderNo={}", orderNo, e);
                }
            }
            
            // 建立 Map 回應
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("failureCount", totalCount - successCount);
            result.put("errors", errors);
            result.put("allSuccess", (totalCount - successCount) == 0);
            
            log.info("批量更新完成：success={}, failure={}", successCount, totalCount - successCount);
            
            return result;
            
        } catch (Exception e) {
            log.error("批量更新訂單狀態失敗：orderIds={}", orderIds, e);
            throw new RuntimeException("批量更新失敗：" + e.getMessage());
        }
    }
    
    
    // ***** 強制完成訂單 ***** //
    public boolean forceCompleteOrder(Integer orderNo, String adminId, String reason) {
        try {
            log.info("管理員強制完成訂單：orderNo={}, admin={}, reason={}", orderNo, adminId, reason);
            
            Order order = orderRepository.findByOrderNo(orderNo);
            if (order == null) {
                throw new OrderNotFoundException("訂單不存在：" + orderNo);
            }
            
            // 更新訂單狀態為已完成
            orderService.updateOrderStatus(orderNo, "COMPLETED");
            
            // 記錄管理員強制操作
            log.info("ADMIN_FORCE_COMPLETE|orderNo={}|admin={}|reason={}|timestamp={}", 
                    orderNo, adminId, reason, System.currentTimeMillis());
            
            return true;
            
        } catch (Exception e) {
            log.error("管理員強制完成訂單失敗：orderNo={}", orderNo, e);
            return false;
        }
    }
    
    
    // ========== 私有轉換方法 ========== //
    // ***** 轉換為後台訂單DTO ((重用前台的 OrderService 邏輯，然後擴展為後台版本)) ***** //
    private AdminOrderDTO convertToAdminOrderDTO(Order order) {
        try {
            AdminOrderDTO adminDTO = new AdminOrderDTO();
            
            // 基本訂單資訊
            adminDTO.setOrderNo(order.getOrderNo());
            adminDTO.setOrderStatus(order.getOrderStatus());
            adminDTO.setOrderTotal(order.getOrderTotal());
            adminDTO.setOrderDatetime(order.getOrderDatetime());
            
            // 會員資訊
            if (order.getMemNo() != null) {
                adminDTO.setMemNo(order.getMemNo().getId());
                adminDTO.setMemEmail(order.getMemNo().getMemEmail());
                adminDTO.setMemName(order.getMemNo().getMemName());
            }
            
            // 優惠券資訊
            if (order.getCouponWalletNo() != null) {
                try {
                    CouponWallet couponWallet = order.getCouponWalletNo();
                    
                    // 設定優惠券錢包代碼
                    adminDTO.setCouponWalletCode("WALLET_" + couponWallet.getId());
                    
                    // 從關聯的 Coupon 取得詳細資訊
                    if (couponWallet.getCouNo() != null) {
                        Coupon coupon = couponWallet.getCouNo();
                        
                        // 使用優惠券的實際名稱和代碼
                        adminDTO.setCouponWalletName(coupon.getCouName() != null ? coupon.getCouName() : "優惠券");
                        
                        // 可以選擇使用優惠券代碼而不是錢包ID
                        if (coupon.getCouCode() != null) {
                            adminDTO.setCouponWalletCode(coupon.getCouCode());
                        }
                    } else {
                        adminDTO.setCouponWalletName("優惠券");
                    }
                    
                } catch (Exception e) {
                    log.debug("無法取得優惠券資訊：{}", e.getMessage());
                    adminDTO.setCouponWalletCode("COUPON_UNKNOWN");
                    adminDTO.setCouponWalletName("優惠券");
                }
            }
            
            // 設定管理操作權限
            adminDTO.setCanCancel(canAdminCancelOrder(order.getOrderStatus()));
            adminDTO.setCanComplete(canForceComplete(order.getOrderStatus()));
            adminDTO.setCanEdit(!isFinalStatus(order.getOrderStatus()));
            
            // 統計資訊
            List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderNo(order.getOrderNo());
            adminDTO.setItemCount(orderItems.size());
            adminDTO.setSerialCount(getSerialCount(order.getOrderNo()));
            adminDTO.setAllSerialsAllocated(checkAllSerialsAllocated(order.getOrderNo()));
            
            enrichWithAdminData(adminDTO, order);
            
            return adminDTO;
            
        } catch (Exception e) {
            log.error("轉換訂單 DTO 失敗：orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException("轉換訂單資料失敗");
        }
    }
    
    
    // ***** 後台專用資料 ***** //
    private void enrichWithAdminData(AdminOrderDTO adminDTO, Order order) {
        // 會員詳細資訊
        if (order.getMemNo() != null) {
            adminDTO.setMemName(order.getMemNo().getMemName());
            adminDTO.setMemEmail(order.getMemNo().getMemEmail());
        }
        
        // 管理操作權限
        String status = order.getOrderStatus();
        adminDTO.setCanCancel(canAdminCancelOrder(status));
        adminDTO.setCanComplete(canForceComplete(status));
        adminDTO.setCanEdit(!isFinalStatus(status));
        
        // 統計資訊
        adminDTO.setSerialCount(getSerialCount(order.getOrderNo()));
        adminDTO.setAllSerialsAllocated(checkAllSerialsAllocated(order.getOrderNo()));
        
        // 優惠券資訊
        if (order.getCouponWalletNo() != null) {
            try {
                CouponWallet couponWallet = order.getCouponWalletNo();
                
                // 設定優惠券錢包代碼（使用 ID）
                adminDTO.setCouponWalletCode("WALLET_" + couponWallet.getId());
                
                // 從關聯的 Coupon 取得名稱
                if (couponWallet.getCouNo() != null) {
                    Coupon coupon = couponWallet.getCouNo();
                    
                    // 使用優惠券的實際名稱
                    String couponName = coupon.getCouName() != null ? coupon.getCouName() : "優惠券";
                    adminDTO.setCouponWalletName(couponName);
                    
                    // 如果有優惠券代碼，優先使用代碼
                    if (coupon.getCouCode() != null && !coupon.getCouCode().trim().isEmpty()) {
                        adminDTO.setCouponWalletCode(coupon.getCouCode());
                    }
                } else {
                    adminDTO.setCouponWalletName("優惠券");
                }
                
            } catch (Exception e) {
                log.debug("無法取得優惠券資訊：{}", e.getMessage());
                adminDTO.setCouponWalletCode("UNKNOWN");
                adminDTO.setCouponWalletName("優惠券");
            }
        }
    }
    
    // ========== 私有輔助方法 ========== //
    private boolean canAdminCancelOrder(String status) {
        return !"COMPLETED".equals(status) && !"CANCELLED".equals(status);
    }
    
    private boolean canForceComplete(String status) {
        return "PROCESSING".equals(status) || "SHIPPED".equals(status);
    }
    
    private boolean isFinalStatus(String status) {
        return "COMPLETED".equals(status) || "CANCELLED".equals(status);
    }
    
    private Integer getSerialCount(Integer orderNo) {
        try {
            String sql = "SELECT COUNT(*) FROM pro_serial_numbers psn " +
                        "INNER JOIN order_item oi ON psn.order_item_no = oi.order_item_no " +
                        "WHERE oi.order_no = ? AND psn.product_sn IS NOT NULL";
            return jdbcTemplate.queryForObject(sql, Integer.class, orderNo);
        } catch (Exception e) {
            log.warn("查詢序號數量失敗：orderNo={}", orderNo, e);
            return 0;
        }
    }
    
    private Boolean checkAllSerialsAllocated(Integer orderNo) {
        try {
            return orderService.areAllItemsShipped(orderNo);
        } catch (Exception e) {
            log.warn("檢查序號分配狀態失敗：orderNo={}", orderNo, e);
            return false;
        }
    }
    
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // 簡化的狀態轉換規則
        if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus)) {
            return false; // 已完成或已取消的訂單不能再改變狀態
        }
        return true;
    }
    
    
}
    
    
	
