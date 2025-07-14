package com.pixeltribe.shopsys.order.model;

import java.util.Map;
import lombok.Data;

@Data
public class OrderStatisticsDTO {
    private Long totalOrders;
    private Long todayOrders;
    private Long thisMonthOrders;
    private Long periodOrders;
    private Long totalSales;
    private Long todaySales;
    private Long periodSales;
    private Double averageOrderValue;
    private Map<String, Long> statusCounts;
}