package com.pixeltribe.shopsys.order.model;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PaymentService {
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	// ***** 綠界設定(從application.properties讀取) ***** //
	@Value("${ecpay.merchant.id}")
    private String merchantId;
    
    @Value("${ecpay.hash.key}")
    private String hashKey;
    
    @Value("${ecpay.hash.iv}")
    private String hashIV;
    
    @Value("${ecpay.payment.url}")
    private String ecpayUrl;
    
    @Value("${ecpay.return.url}")
    private String returnUrl;
    
    @Value("${ecpay.notify.url}")
    private String notifyUrl;
    
    @Value("${server.base-url}")
    private String baseUrl;
    
    // ========== 啟動時檢查綠界設定  ========== //
    @PostConstruct
    public void validateECPayConfig() {
    	log.info("=== 綠界付款設定檢查 ===");
        log.info("Merchant ID: {}", merchantId);
        log.info("Payment URL: {}", ecpayUrl);
        log.info("Return URL: {}", returnUrl);
        log.info("Notify URL: {}", notifyUrl);
        
        if ("2000132".equals(merchantId)) {
            log.info("✅ 使用測試環境設定");
        } else {
            log.info("🔥 使用正式環境設定");
        }
        
        // 檢查必要參數
        if (merchantId == null || hashKey == null || hashIV == null) {
            log.error("❌ 綠界設定不完整，請檢查 application.properties");
        } else {
            log.info("✅ 綠界設定載入完成");
        }
    }
    
    
    // ========== 核心付款功能  ========== //
    
    // ***** 啟動付款 (@param orderNo // @return付款頁面HTML)  ***** //
    public String initiatePayment(Integer orderNo) {
    	try {
    		log.info("開始付款:orderNo={}", orderNo);
    		
    		// 1. 檢查訂單狀態
    		OrderDTO order = orderService.getOrderDetail(orderNo);
            if (!order.canRetryPayment()) {
                throw new IllegalStateException("訂單狀態不允許付款：" + order.getOrderStatusInfo().getDisplayName());
            }
            log.debug("訂單檢查通過：金額={}", order.getOrderTotal());
            
            // 2. 檢查是否已經有進行中的付款
            if (hasOngoingPayment(orderNo)) {
                throw new IllegalStateException("訂單已有進行中的付款，請勿重複操作");
            }
            
            // 3. 更新訂單狀態為付款中
            orderService.updateOrderStatus(orderNo, "PAYING");
            log.info("訂單狀態更新為：PAYING");
            
            // 4. 生成交易編號
            String tradeNo = generateTradeNo(orderNo);
            log.debug("生成交易編號：{}", tradeNo);
            
            // 5. 存儲付款資訊到 Redis
            storePaymentInfoToRedis(orderNo, tradeNo, order);
            log.debug("付款資訊已存入 Redis");
            
            // 6. 生成綠界付款表單
            String paymentForm = generateECPayForm(order, tradeNo);
            log.info("付款表單生成成功，準備跳轉綠界");
            
            return paymentForm;
    	} catch (Exception e) {
            log.error("發起付款失敗：orderNo={}", orderNo, e);
            // 恢復訂單狀態
            try {
                orderService.updateOrderStatus(orderNo, "PENDING");
                log.info("已恢復訂單狀態為：PENDING");
            } catch (Exception ex) {
                log.error("恢復訂單狀態失敗：orderNo={}", orderNo, ex);
            }
            throw new RuntimeException("發起付款失敗：" + e.getMessage());
        }
    }
    
    // ***** 處理綠界的付款回調 (@param params 綠界回調參數 // @return處理結果)  ***** //
    public String handlePaymentCallback(Map<String, String> params) {
        String tradeNo = params.get("MerchantTradeNo");
        String rtnCode = params.get("RtnCode");
        
        try {
            log.info("收到綠界付款回調：tradeNo={}, rtnCode={}", tradeNo, rtnCode);
            
            // 1. 驗證回調資料
            if (!validateECPayCallback(params)) {
                log.error("綠界回調驗證失敗：tradeNo={}", tradeNo);
                return "0|驗證失敗";
            }
            log.debug("綠界回調驗證成功");
            
            // 2. 根據付款結果處理
            if ("1".equals(rtnCode)) {
                // 付款成功
                handlePaymentSuccess(tradeNo, params);
                log.info("付款成功處理完成：tradeNo={}", tradeNo);
            } else {
                // 付款失敗
                String rtnMsg = params.get("RtnMsg");
                handlePaymentFailure(tradeNo, rtnMsg);
                log.warn("付款失敗處理完成：tradeNo={}, reason={}", tradeNo, rtnMsg);
            }
            
            return "1|OK";
            
        } catch (Exception e) {
            log.error("處理綠界回調失敗：tradeNo={}", tradeNo, e);
            return "0|系統錯誤";
        }
    }
    
    
    // ========== 定時任務設定  ========== //
    // ***** 處理付款超時：每5分鐘檢查一次 超過30分鐘就付款失敗  ***** //
    
    @Scheduled(fixedRate = 300000)   // 排成器每5分鐘檢查一次付款狀態   5*60*1000
    public void handlePaymentTimeout() {
        try {
            Set<String> paymentKeys = redisTemplate.keys("payment:*");
            if (paymentKeys == null || paymentKeys.isEmpty()) {
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            int timeoutCount = 0;
            
            for (String key : paymentKeys) {
                try {
                    Map<Object, Object> paymentInfo = redisTemplate.opsForHash().entries(key);
                    if (paymentInfo.isEmpty()) continue;
                    
                    String status = (String) paymentInfo.get("status");
                    Long createdAt = (Long) paymentInfo.get("createdAt");
    
                    // ***** 檢查是否超過30分鐘 ***** //
                    if ("PAYING".equals(status) && 
                            createdAt != null && 
                            (currentTime - createdAt) > 1800000) { // 30分鐘 =60*30*1000
                            
                            Integer orderNo = (Integer) paymentInfo.get("orderNo");
                            
                            // ***** 設定訂單為失敗狀態 ***** //
                            orderService.updateOrderStatus(orderNo, "FAILED");        
                            
                            // ***** 更新Redis狀態 ***** //
                            Map<String, String> timeoutInfo = new HashMap<>();
                            timeoutInfo.put("reason", "付款超時");
                            timeoutInfo.put("timeoutTime", LocalDateTime.now().toString());
                            updatePaymentStatusInRedis(orderNo, "TIMEOUT", timeoutInfo);
                            
                            timeoutCount++;
                            log.info("訂單付款超時，已設為失敗：orderNo={}", orderNo);
                        }
                        
                    } catch (Exception e) {
                        log.error("處理超時付款記錄失敗：key={}", key, e);
                    }
                }
                
                if (timeoutCount > 0) {
                    log.info("本次處理了 {} 筆超時付款", timeoutCount);
                }
                
            } catch (Exception e) {
                log.error("執行付款超時檢查失敗", e);
            }
        }

    
    	/* 
    	 1. 定時任務：檢查預購商品是否上架 (每天晚上8點檢查)
    	 2. 透過檢查 pro_serial_numbers 表中的序號庫存判斷商品是否可發貨
    	 */
    	
	    @Scheduled(cron = "0 0 20 * * ?")   // 每天晚上8點執行
	    public void checkPreOrderProductsAvailable() {
	        try {
	            log.info("開始每日預購產品序號庫存檢查(晚上8點)");
	            
	            // 1. 從Redis找到所有預購商品的等待列表
	            Set<String> preOrderKeys = redisTemplate.keys("preorder:*");
	            
	            if (preOrderKeys == null || preOrderKeys.isEmpty()) {
	                log.info("沒有待處理的預購產品");
	                return;
	            }
	            
	            log.info("發現{}個預購產品需要檢查", preOrderKeys.size());
	            int processedCount = 0;
	            int totalWaitingOrders = 0;
	            
	            // 2. 逐一檢查每個預購商品的序號庫存
	            for (String key : preOrderKeys) {
	                try {
	                    Integer proNo = extractProNoFromKey(key);
	                    Set<Object> waitingOrders = redisTemplate.opsForSet().members(key);
	                    int waitingCount = waitingOrders != null ? waitingOrders.size() : 0;
	                    totalWaitingOrders += waitingCount;
	                    
	                    log.info("檢查產品：proNo={}, 等待訂單數={}", proNo, waitingCount);
	                    
	                    // 3. 檢查該產品是否有可用序號
	                    if (isProductInStock(proNo)) {
	                        log.info("發現預購產品有序號庫存：proNo={}", proNo);
	                        
	                        // 4. 處理該產品的所有等待訂單
	                        handlePreOrderProductAvailable(proNo);
	                        processedCount++;
	                    } else {
	                        log.info("產品仍無庫存：proNo={}", proNo);
	                    }
	                    
	                } catch (Exception e) {
	                    log.error("檢查單個預購產品失敗：key={}", key, e);
	                }
	            }
	            
	            log.info("每日預購檢查完成：檢查產品數={}, 處理產品數={}, 總等待訂單={}", 
	                    preOrderKeys.size(), processedCount, totalWaitingOrders);
	            
	            if (processedCount == 0) {
	                log.info("本次檢查無預購產品有新序號，將於明天晚上8點繼續檢查");
	            } else {
	                log.info("已自動發送 {} 個商品的預購到貨通知郵件", processedCount);
	            }
	            
	        } catch (Exception e) {
	            log.error("每日預購產品序號庫存檢查失敗", e);
	        }
	    }
	    
	    
	    // ========== 序號庫存管理  ========== //
	    
	    /* 
	   	 1. @param proNo 產品編號 (PRO_NO)
	   	 2. @return 是否有可用序號
	   	 */
	    private boolean isProductInStock(Integer proNo) {
	        try {
	            // 查詢該商品未分配的序號數量 (ORDER_ITEM_NO 為 NULL 表示未使用)
	            String sql = "SELECT COUNT(*) FROM pro_serial_numbers WHERE pro_no = ? AND order_item_no IS NULL";
	            Integer availableCount = jdbcTemplate.queryForObject(sql, Integer.class, proNo);
	            
	            boolean hasStock = availableCount != null && availableCount > 0;
	            
	            if (hasStock) {
	                log.debug("產品有庫存：proNo={}, 可用序號數量={}", proNo, availableCount);
	            } else {
	                log.debug("產品無庫存：proNo={}", proNo);
	            }
	            
	            return hasStock;
	            
	        } catch (Exception e) {
	            log.error("檢查產品序號庫存失敗：productId={}", proNo, e);
	            return false;
	        }
	    }
	    
	    /* 
	   	 1. 從資料庫分配一個序號給訂單項目
	   	 2. @param proNo 產品編號 (PRO_NO)
	   	 3. @param orderItemNo 訂單項目編號 (ORDER_ITEM_NO)
	   	 4. @return 分配的序號 (PRODUCT_SN)
	   	 */
	    private String allocateSerialNumber(Integer proNo, Integer orderItemNo) {
	        try {
	            // 1. 查詢一個未分配的序號
	            String selectSql = "SELECT product_sn_no, product_sn FROM pro_serial_numbers " +
	                              "WHERE pro_no = ? AND order_item_no IS NULL " +
	                              "ORDER BY product_sn_no ASC LIMIT 1";
	            
	            List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSql, proNo);
	            
	            if (!results.isEmpty()) {
	                Map<String, Object> result = results.get(0);
	                Integer productSnNo = (Integer) result.get("product_sn_no");
	                String productSn = (String) result.get("product_sn");
	                
	                // 2. 更新序號，標記為已分配給此訂單項目
	                String updateSql = "UPDATE pro_serial_numbers SET order_item_no = ? " +
	                                  "WHERE product_sn_no = ? AND order_item_no IS NULL";
	                
	                int updatedRows = jdbcTemplate.update(updateSql, orderItemNo, productSnNo);
	                
	                if (updatedRows > 0) {
	                    log.debug("序號已分配：proNo={}, orderItemNo={}, serial={}", 
	                    		proNo, orderItemNo, productSn);
	                    return productSn;
	                } else {
	                    log.warn("序號分配失敗，可能被其他請求搶先分配：proNo={}", proNo);
	                    return null;
	                }
	            } else {
	                log.warn("無可用序號：proNo={}", proNo);
	                return null;
	            }
	            
	        } catch (Exception e) {
	            log.error("分配序號失敗：proNo={}, orderItemNo={}", proNo, orderItemNo, e);
	            return null;
	        }
	    }
	    
	    
	    /* 
	   	 1. 查詢商品的序號庫存狀況
	   	 2. @param proNo 產品編號
	   	 3. @return 庫存詳情
	   	 */
	    public Map<String, Object> getProductSerialStock(Integer proNo) {
	        try {
	            // 總序號數量
	            String totalSql = "SELECT COUNT(*) FROM pro_serial_numbers WHERE pro_no = ?";
	            Integer totalCount = jdbcTemplate.queryForObject(totalSql, Integer.class, proNo);
	            
	            // 可用序號數量
	            String availableSql = "SELECT COUNT(*) FROM pro_serial_numbers WHERE pro_no = ? AND order_item_no IS NULL";
	            Integer availableCount = jdbcTemplate.queryForObject(availableSql, Integer.class, proNo);
	            
	            // 已使用序號數量
	            Integer usedCount = (totalCount != null ? totalCount : 0) - (availableCount != null ? availableCount : 0);
	            
	            Map<String, Object> result = new HashMap<>();
	            result.put("productId", proNo);
	            result.put("totalSerials", totalCount != null ? totalCount : 0);
	            result.put("availableSerials", availableCount != null ? availableCount : 0);
	            result.put("usedSerials", usedCount);
	            result.put("hasStock", availableCount != null && availableCount > 0);
	            
	            return result;
	            
	        } catch (Exception e) {
	            log.error("查詢商品序號庫存失敗：proNo={}", proNo, e);
	            return null;
	        }
	    }
	    
	    // ========== 預購商品處理  ========== //
	    /* 
	   	 1. 處理預購商品上架 (有序號庫存時觸發)
	   	 2. @param proNo 產品編號
	   	 */
	    public void handlePreOrderProductAvailable(Integer proNo) {
	        try {
	            log.info("開始處理預購商品上架：proNo={}", proNo);
	            
	            // 1. 從 Redis 找出等待此產品的訂單
	            List<Integer> waitingOrderNos = findWaitingPreOrders(proNo);
	            
	            if (waitingOrderNos.isEmpty()) {
	                log.info("沒有等待此產品的預購訂單：proNo={}", proNo);
	                cleanupPreOrderRecords(proNo);
	                return;
	            }
	            
	            log.info("找到 {} 筆等待的預購訂單", waitingOrderNos.size());
	            
	            // 2. 為每個訂單發放序號並發送郵件
	            int successCount = 0;
	            for (Integer orderNo : waitingOrderNos) {
	                try {
	                    processPreOrderDelivery(orderNo, proNo);
	                    successCount++;
	                } catch (Exception e) {
	                    log.error("處理預購訂單失敗：orderNo={}, proNo={}", orderNo, proNo, e);
	                }
	            }
	            
	            // 3. 清理 Redis 中的預購記錄
	            cleanupPreOrderRecords(proNo);
	            
	            log.info("預購商品上架處理完成：proNo={}, 成功處理={}/{}", 
	            		proNo, successCount, waitingOrderNos.size());
	            
	        } catch (Exception e) {
	            log.error("處理預購商品上架失敗：proNo={}", proNo, e);
	        }
	    }
	    
	    
	    /* 
	   	 1. 處理單筆預購訂單交貨 (分配真實序號)
	   	 2. @param orderNo 訂單編號
	   	 3. @param proNo 產品編號
	   	 */
	    private void processPreOrderDelivery(Integer orderNo, Integer proNo) {
	        try {
	            log.info("處理預購訂單交貨：orderNo={}, proNo={}", orderNo, proNo);
	            
	            // 1. 獲取訂單信息
	            OrderDTO order = orderService.getOrderDetail(orderNo);
	            
	            // 2. 找到該訂單中對應的預購商品項目
	            OrderItemDTO preOrderItem = null;
	            for (OrderItemDTO item : order.getOrderItems()) {
	                if (item.getProNo().equals(proNo) && item.getOrderItemNo() != null) {
	                    preOrderItem = item;
	                    break;
	                }
	            }
	            
	            if (preOrderItem == null) {
	                log.error("找不到對應的預購商品項目：orderNo={}, proNo={}", orderNo, proNo);
	                return;
	            }
	            
	            // 3. 分配序號給此訂單項目
	            String serialNumber = allocateSerialNumber(proNo, preOrderItem.getOrderItemNo());
	            
	            if (serialNumber == null) {
	                log.error("無可用序號：proNo={}", proNo);
	                return;
	            }
	            
	            // 4. 發送預購到貨郵件
	            sendPreOrderDeliveryEmail(order, preOrderItem.getProductName(), serialNumber);
	            
	            log.info("預購訂單處理完成：orderNo={}, orderItemNo={}, serial={}", 
	                    orderNo, preOrderItem.getOrderItemNo(), serialNumber);
	            
	        } catch (Exception e) {
	            log.error("處理預購訂單交貨失敗：orderNo={}, proNo={}", orderNo, proNo, e);
	            throw e;
	        }
	    }
	   
	    // ========== 訂單處理流程 ========== //
	    /* 
	   	 1. 觸發訂單後續處理 (付款成功後調用)
	   	 2. 分離現貨和預購商品，進行不同的處理流程
	   	 */
	    private void triggerOrderProcessing(OrderDTO order) {
	        try {
	            log.info("觸發訂單後續處理：orderNo={}", order.getOrderNo());
	            
	            // 1. 分離現貨和預購商品
	            Map<String, List<OrderItemDTO>> itemGroups = separateOrderItems(order);
	            List<OrderItemDTO> inStockItems = itemGroups.get("inStock");
	            List<OrderItemDTO> preOrderItems = itemGroups.get("preOrder");
	            
	            // 2. 處理現貨商品序號 (立即分配)
	            List<String> inStockSerials = processInStockItems(inStockItems);
	            
	            // 3. 記錄預購商品到 Redis (等待序號庫存)
	            if (!preOrderItems.isEmpty()) {
	                recordPreOrderItems(order.getOrderNo(), preOrderItems);
	                log.info("預購商品已記錄，等待序號庫存：數量={}", preOrderItems.size());
	            }
	            
	            // 4. 發送付款成功郵件 (含現貨序號 + 預購說明)
	            sendPaymentSuccessEmail(order, inStockSerials, preOrderItems);
	            
	            // 5. 更新訂單狀態為已完成
	            orderService.updateOrderStatus(order.getOrderNo(), "COMPLETED");
	            log.info("訂單處理完成：orderNo={}", order.getOrderNo());
	            
	        } catch (Exception e) {
	            log.error("觸發訂單後續處理失敗：orderNo={}", order.getOrderNo(), e);
	        }
	    }
	    
	    /* 
	   	 1. 分離現貨和預購商品
	   	 2. @param order 訂單
	   	 3. @return 分離後的商品列表
	   	 */
	    private Map<String, List<OrderItemDTO>> separateOrderItems(OrderDTO order) {
	        List<OrderItemDTO> inStockItems = new ArrayList<>();
	        List<OrderItemDTO> preOrderItems = new ArrayList<>();
	        
	        for (OrderItemDTO item : order.getOrderItems()) {
	            // 根據序號庫存判斷是否為現貨
	            if (isProductInStock(item.getProNo())) {
	                inStockItems.add(item);
	            } else {
	                preOrderItems.add(item);
	            }
	        }
	        
	        Map<String, List<OrderItemDTO>> result = new HashMap<>();
	        result.put("inStock", inStockItems);
	        result.put("preOrder", preOrderItems);
	        
	        log.debug("商品分類完成：已發售={}，預購中={}", inStockItems.size(), preOrderItems.size());
	        return result;
	    }
	    
	    /* 
	   	 1. 處理現貨商品序號發放 (立即從序號表分配)
	   	 2. @param inStockItems 現貨商品列表
	   	 3. @return 分配的序號列表
	   	 */
	    private List<String> processInStockItems(List<OrderItemDTO> inStockItems) {
	        List<String> serialNumbers = new ArrayList<>();
	        
	        for (OrderItemDTO item : inStockItems) {
	            try {
	                // 從資料庫分配序號給這個訂單項目
	                String serialNumber = allocateSerialNumber(item.getProNo(), item.getOrderItemNo());
	                
	                if (serialNumber != null) {
	                    serialNumbers.add(serialNumber);
	                    log.debug("現貨序號發放：{} -> {}", item.getProName(), serialNumber);
	                } else {
	                    // 如果沒有可用序號，記錄錯誤
	                    serialNumbers.add("暫無序號，請聯繫客服");
	                    log.error("序號發放失敗：proNo={}, orderItemNo={}", 
	                             item.getProNo(), item.getOrderItemNo());
	                }
	                
	            } catch (Exception e) {
	                log.error("處理現貨序號失敗：proNo={}", item.getProNo(), e);
	                serialNumbers.add("序號處理中，請聯繫客服");
	            }
	        }
	        
	        return serialNumbers;
	    }
	    
	    /* 
	   	 1. 記錄預購商品到 Redis (等待序號庫存)
	   	 2. @param orderNo 訂單編號
	   	 3. @param preOrderItems 預購商品列表
	   	 */
	    private void recordPreOrderItems(Integer orderNo, List<OrderItemDTO> preOrderItems) {
	        for (OrderItemDTO item : preOrderItems) {
	            String redisKey = "preorder:" + item.getProNo();
	            
	            // 將訂單號加入該商品的等待列表
	            redisTemplate.opsForSet().add(redisKey, orderNo);
	            
	            // 設定過期時間 (90天)
	            redisTemplate.expire(redisKey, 90, TimeUnit.DAYS);
	            
	            log.debug("預購記錄已保存：productId={}, orderNo={}", item.getProNo(), orderNo);
	        }
	    }
    
	    // ========== Email流程處理 ========== //
	    /* 
	   	 1. 發送付款成功郵件 (含現貨序號 + 預購說明)
	   	 2. @param order 訂單
	   	 3. @param inStockSerials 現貨序號列表
	   	 4. @param preOrderItems 預購商品列表
	   	 */
	    private void sendPaymentSuccessEmail(OrderDTO order, List<String> inStockSerials, List<OrderItemDTO> preOrderItems) {
	        try {
	            log.info("開始發送付款成功郵件：orderNo={}", order.getOrderNo());
	            
	            MimeMessage message = mailSender.createMimeMessage();
	            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
	            
	            helper.setTo(order.getContactEmail());
	            helper.setSubject("付款成功確認 - 訂單 #" + order.getOrderNo());
	            
	            String emailContent = buildPaymentSuccessEmailContent(order, inStockSerials, preOrderItems);
	            helper.setText(emailContent, true);
	            
	            mailSender.send(message);
	            log.info("付款成功郵件發送完成：orderNo={}", order.getOrderNo());
	            
	        } catch (Exception e) {
	            log.error("發送付款成功郵件失敗：orderNo={}", order.getOrderNo(), e);
	        }
	    }
	    
	    /* 
	   	 1. 發送預購產品已上架並寄出序號
	   	 2. @param order 訂單
	   	 3. @param proName 商品名稱
	   	 4. @param serialNumber 序號
	   	 */
	    private void sendPreOrderDeliveryEmail(OrderDTO order, String proName, String productSn) {
	        try {
	            log.info("開始發送預購到貨E-mail郵件：orderNo={}, product={}", order.getOrderNo(), proName);
	            
	            MimeMessage message = mailSender.createMimeMessage();
	            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
	            
	            helper.setTo(order.getContactEmail());
	            helper.setSubject("預購產品到貨通知 - " + proName);
	            
	            String emailContent = buildPreOrderDeliveryEmailContent(order, proName, productSn);
	            helper.setText(emailContent, true);
	            
	            mailSender.send(message);
	            log.info("預購到貨郵件發送完成：orderNo={}", order.getOrderNo());
	            
	        } catch (Exception e) {
	            log.error("發送預購到貨郵件失敗：orderNo={}", order.getOrderNo(), e);
	        }
	    }
	    
	    
	    /* 
	   	 1. 建立付款成功郵件內容
	   	 2. @param order 訂單
	   	 3. @param inStockSerials 現貨序號列表
	   	 4. @param preOrderItems 預購商品列表
	   	 5. @return HTML 郵件內容
	   	 */
	    private String buildPaymentSuccessEmailContent(OrderDTO order, List<String> inStockSerials, List<OrderItemDTO> preOrderItems) {
	        StringBuilder content = new StringBuilder();
	        
	        content.append("<html><head><meta charset='UTF-8'></head><body>");
	        content.append("<h2>付款成功確認</h2>");
	        content.append("<p>親愛的客戶您好，您的訂單已付款成功！</p>");
	        
	        // 訂單基本資訊
	        content.append("<h3>訂單明細</h3>");
	        content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
	        content.append("<tr><td><strong>訂單編號</strong></td><td>#").append(order.getOrderNo()).append("</td></tr>");
	        content.append("<tr><td><strong>付款時間</strong></td><td>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
	        content.append("<tr><td><strong>總金額</strong></td><td>NT$ ").append(order.getOrderTotal()).append("</td></tr>");
	        content.append("</table>");
	        
	        // 商品清單
	        content.append("<h3>產品清單</h3>");
	        content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
	        content.append("<tr><th>產品名稱</th><th>價格</th><th>狀態</th><th>序號</th></tr>");
	        
	        int serialIndex = 0;
	        for (OrderItemDTO item : order.getOrderItems()) {
	            content.append("<tr>");
	            content.append("<td>").append(item.getProductName()).append("</td>");
	            content.append("<td>NT$ ").append(item.getProPrice()).append("</td>");
	            
	            // 根據序號庫存判斷商品狀態
	            if (isProductInStock(item.getProNo()) && serialIndex < inStockSerials.size()) {
	                content.append("<td>現貨</td>");
	                content.append("<td><strong>").append(inStockSerials.get(serialIndex++)).append("</strong></td>");
	            } else {
	                content.append("<td>預購中</td>");
	                content.append("<td>到貨後發送</td>");
	            }
	            content.append("</tr>");
	        }
	        
	        content.append("</table>");
	        
	        // 預購說明
	        if (!preOrderItems.isEmpty()) {
	            content.append("<h3>預購產品說明</h3>");
	            content.append("<p>您有 <strong>").append(preOrderItems.size()).append("</strong> 項預購產品</p>");
	            content.append("<p>預購產品將於到貨後立即為您發放序號，屆時會再發送郵件通知</p>");
	            content.append("<p>我們會在產品上架後的 <strong>1個工作天</strong> 自動發送序號給您</p>");
	        }
	        
	        content.append("<hr>");
	        content.append("<p>感謝您的支持！<br><strong>像素部落商城</strong></p>");
	        content.append("</body></html>");
	        
	        return content.toString();
	    }
	    
	    
	    /* 
	   	 1. 建立預購到貨郵件內容
	   	 2. @param order 訂單
	   	 3. @param proName 產品名稱
	   	 4. @param productSn 序號
	   	 5. @return HTML 郵件內容
	   	 */
	    private String buildPreOrderDeliveryEmailContent(OrderDTO order, String proName, String productSn) {
	        StringBuilder content = new StringBuilder();
	        
	        content.append("<html><head><meta charset='UTF-8'></head><body>");
	        content.append("<h2>預購產品到貨通知</h2>");
	        content.append("<p>親愛的客戶您好，您預購的產品已到貨！</p>");
	        
	        content.append("<h3>產品資訊</h3>");
	        content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
	        content.append("<tr><td><strong>訂單編號</strong></td><td>#").append(order.getOrderNo()).append("</td></tr>");
	        content.append("<tr><td><strong>產品名稱</strong></td><td>").append(proName).append("</td></tr>");
	        content.append("<tr><td><strong>到貨時間</strong></td><td>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
	        content.append("</table>");
	        
	        content.append("<h3>遊戲序號</h3>");
	        content.append("<div style='background: #f0f8ff; padding: 20px; margin: 10px 0; border: 2px solid #4169e1; border-radius: 8px; text-align: center;'>");
	        content.append("<p style='margin: 0; font-size: 14px; color: #666;'>請複製以下序號到遊戲中啟用</p>");
	        content.append("<div style='font-size: 24px; font-weight: bold; color: #4169e1; margin: 10px 0; font-family: monospace;'>");
	        content.append(productSn);
	        content.append("</div>");
	        content.append("</div>");
	        
	        content.append("<h3>使用說明</h3>");
	        content.append("<p>請儘快使用序號啟用遊戲</p>");
	        content.append("<p>如有問題請聯繫客服</p>");
	        content.append("<p>序號僅能使用一次，請妥善保存</p>");
	        
	        content.append("<hr>");
	        content.append("<p>感謝您的耐心等待！<br><strong>像素部落商城</strong></p>");
	        content.append("</body></html>");
	        
	        return content.toString();
	    }
	    
	    // ========== 付款成功/失敗處理 ========== //
	    /* 
	   	 1. 處理付款成功
	   	 2. @param tradeNo 交易編號
	   	 3. @param ecpayResponse 綠界回應參數
	   	 */
	    private void handlePaymentSuccess(String tradeNo, Map<String, String> ecpayResponse) {
	        try {
	            // 從 Redis 找到對應的訂單
	            Integer orderNo = findOrderNoByTradeNo(tradeNo);
	            if (orderNo == null) {
	                log.error("找不到對應的訂單：tradeNo={}", tradeNo);
	                return;
	            }
	            
	            // 檢查訂單狀態
	            OrderDTO order = orderService.getOrderDetail(orderNo);
	            if (!"PAYING".equals(order.getOrderStatus())) {
	                log.warn("訂單狀態異常：orderNo={}, status={}", orderNo, order.getOrderStatus());
	                return;
	            }
	            
	            // 更新訂單狀態為處理中
	            orderService.updateOrderStatus(orderNo, "PROCESSING");
	            log.info("訂單狀態更新為：PROCESSING");
	            
	            // 更新 Redis 付款狀態
	            updatePaymentStatusInRedis(orderNo, "SUCCESS", ecpayResponse);
	            
	            // 觸發後續處理 (序號發放、郵件發送)
	            triggerOrderProcessing(order);
	            
	            log.info("付款成功處理完成：orderNo={}, tradeNo={}", orderNo, tradeNo);
	            
	        } catch (Exception e) {
	            log.error("處理付款成功失敗：tradeNo={}", tradeNo, e);
	        }
	    }
	    
	    /* 
	   	 1. 處理付款失敗
	   	 2. @param tradeNo 交易編號
	   	 3. @param reason 失敗原因
	   	 */
	    private void handlePaymentFailure(String tradeNo, String reason) {
	        try {
	            Integer orderNo = findOrderNoByTradeNo(tradeNo);
	            if (orderNo == null) {
	                log.error("找不到對應的訂單：tradeNo={}", tradeNo);
	                return;
	            }
	            
	            // 更新訂單狀態為付款失敗
	            orderService.updateOrderStatus(orderNo, "FAILED");
	            log.info("訂單狀態更新為：FAILED");
	            
	            // 更新 Redis 狀態
	            Map<String, String> failureInfo = new HashMap<>();
	            failureInfo.put("reason", reason);
	            failureInfo.put("failureTime", LocalDateTime.now().toString());
	            updatePaymentStatusInRedis(orderNo, "FAILED", failureInfo);
	            
	            log.info("付款失敗處理完成：orderNo={}, reason={}", orderNo, reason);
	            
	        } catch (Exception e) {
	            log.error("處理付款失敗失敗：tradeNo={}", tradeNo, e);
	        }
	    }
	    
	    
	    // ========== 工具方法 ========== //
	    /* 
	   	 1. 取得付款狀態
	   	 2. @param orderNo 訂單編號
	   	 3. @return 付款狀態資訊
	   	 */
	    public Map<String, Object> getPaymentStatus(Integer orderNo) {
	        String redisKey = "payment:" + orderNo;
	        Map<Object, Object> paymentInfo = redisTemplate.opsForHash().entries(redisKey);
	        
	        if (paymentInfo.isEmpty()) {
	            log.debug("找不到付款資訊：orderNo={}", orderNo);
	            return null;
	        }
	        
	        Map<String, Object> result = new HashMap<>();
	        result.put("orderNo", paymentInfo.get("orderNo"));
	        result.put("tradeNo", paymentInfo.get("tradeNo"));
	        result.put("status", paymentInfo.get("status"));
	        result.put("amount", paymentInfo.get("amount"));
	        result.put("createdAt", paymentInfo.get("createdAt"));
	        result.put("updatedAt", paymentInfo.get("updatedAt"));
	        
	        log.debug("付款狀態查詢：orderNo={}, status={}", orderNo, result.get("status"));
	        return result;
	    }
	    
	    /* 
	   	 1. 取得所有預購商品的等待狀況
	   	 2. @return 預購商品資訊
	   	 */
	    public Map<String, Object> getAllPreOrderWaitingInfo() {
	        Map<String, Object> result = new HashMap<>();
	        Set<String> preOrderKeys = redisTemplate.keys("preorder:*");
	        
	        if (preOrderKeys == null || preOrderKeys.isEmpty()) {
	            result.put("totalProducts", 0);
	            result.put("totalOrders", 0);
	            result.put("products", new ArrayList<>());
	            return result;
	        }
	        
	        List<Map<String, Object>> products = new ArrayList<>();
	        int totalOrders = 0;
	        
	        for (String key : preOrderKeys) {
	            try {
	                Integer productId = extractProNoFromKey(key);
	                Set<Object> waitingOrders = redisTemplate.opsForSet().members(key);
	                Map<String, Object> stockInfo = getProductSerialStock(productId);
	                
	                if (stockInfo != null) {
	                    stockInfo.put("waitingOrders", waitingOrders != null ? waitingOrders.size() : 0);
	                    stockInfo.put("orderList", waitingOrders);
	                    products.add(stockInfo);
	                    totalOrders += (waitingOrders != null ? waitingOrders.size() : 0);
	                }
	                
	            } catch (Exception e) {
	                log.error("處理預購商品資訊失敗：key={}", key, e);
	            }
	        }
	        
	        result.put("totalProducts", products.size());
	        result.put("totalOrders", totalOrders);
	        result.put("products", products);
	        
	        return result;
	    }
	    
	    
	    // ========== 私有工具方法 ========== //
	    /* 
	   	 1. 生成交易編號
	   	 2. @param orderNo 訂單編號
	   	 3. @return 交易編號
	   	 */
	    private String generateTradeNo(Integer orderNo) {
	        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	        return "TXN" + timestamp + String.format("%06d", orderNo);
	    }
	    
	    
	    /* 
	   	 1. 檢查是否有進行中的付款
	   	 2. @param orderNo 訂單編號
	   	 3. @return 是否有進行中的付款
	   	 */
	    private boolean hasOngoingPayment(Integer orderNo) {
	        String redisKey = "payment:" + orderNo;
	        String status = (String) redisTemplate.opsForHash().get(redisKey, "status");
	        return "PAYING".equals(status);
	    }
	    
	    
	    /* 
	   	 1. 存儲付款資訊到 Redis
	   	 2. @param orderNo 訂單編號
	   	 3. @param tradeNo 交易編號
	   	 4. @param order 訂單資訊
	   	 */
	    private void storePaymentInfoToRedis(Integer orderNo, String tradeNo, OrderDTO order) {
	        String redisKey = "payment:" + orderNo;
	        
	        Map<String, Object> paymentInfo = new HashMap<>();
	        paymentInfo.put("orderNo", orderNo);
	        paymentInfo.put("tradeNo", tradeNo);
	        paymentInfo.put("amount", order.getOrderTotal());
	        paymentInfo.put("status", "PAYING");
	        paymentInfo.put("method", "ECPAY");
	        paymentInfo.put("createdAt", System.currentTimeMillis());
	        paymentInfo.put("memNo", order.getMemNo());
	        
	        redisTemplate.opsForHash().putAll(redisKey, paymentInfo);
	        redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES); // 30分鐘過期
	        
	        log.debug("付款資訊已存入 Redis：orderNo={}, tradeNo={}", orderNo, tradeNo);
	    }
	    
	    
	    /* 
	   	 1. 從 Redis key 中提取產品編號
	   	 2. @param key Redis key (格式: preorder:123)
	   	 3. @return 產品編號
	   	 */
	    private Integer extractProNoFromKey(String key) {
	        return Integer.parseInt(key.substring(key.lastIndexOf(":") + 1));
	    }
	    
	    /* 
	   	 1. 查找等待特定商品的預購訂單
	   	 2. @param proNo 產品編號
	   	 3. @return 等待的訂單編號列表
	   	 */
	    private List<Integer> findWaitingPreOrders(Integer proNo) {
	        String redisKey = "preorder:" + proNo;
	        Set<Object> orderNos = redisTemplate.opsForSet().members(redisKey);
	        
	        if (orderNos == null || orderNos.isEmpty()) {
	            return new ArrayList<>();
	        }
	        
	        return orderNos.stream()
	                      .map(obj -> (Integer) obj)
	                      .collect(Collectors.toList());
	    }
	    
	    
	    /* 
	   	 1. 根據交易編號查找訂單編號
	   	 2. @param tradeNo 交易編號
	   	 3. @return 訂單編號
	   	 */
	    private Integer findOrderNoByTradeNo(String tradeNo) {
	        Set<String> keys = redisTemplate.keys("payment:*");
	        if (keys == null) return null;
	        
	        for (String key : keys) {
	            try {
	                String storedTradeNo = (String) redisTemplate.opsForHash().get(key, "tradeNo");
	                if (tradeNo.equals(storedTradeNo)) {
	                    return (Integer) redisTemplate.opsForHash().get(key, "orderNo");
	                }
	            } catch (Exception e) {
	                log.warn("查找交易編號時發生錯誤：key={}", key, e);
	            }
	        }
	        return null;
	    }
	    
	    
	    /* 
	   	 1. 更新 Redis 中的付款狀態
	   	 2. @param orderNo 訂單編號
	   	 3. @param status 狀態
	   	 4. @param response 回應資料
	   	 */
	    private void updatePaymentStatusInRedis(Integer orderNo, String status, Map<String, String> response) {
	        String redisKey = "payment:" + orderNo;
	        
	        redisTemplate.opsForHash().put(redisKey, "status", status);
	        redisTemplate.opsForHash().put(redisKey, "updatedAt", System.currentTimeMillis());
	        
	        if (response != null && !response.isEmpty()) {
	            redisTemplate.opsForHash().put(redisKey, "gatewayResponse", response.toString());
	        }
	        
	        // 如果是最終狀態，延長過期時間到24小時
	        if ("SUCCESS".equals(status) || "FAILED".equals(status) || "TIMEOUT".equals(status)) {
	            redisTemplate.expire(redisKey, 24, TimeUnit.HOURS);
	        }
	        
	        log.debug("付款狀態已更新：orderNo={}, status={}", orderNo, status);
	    }
	    
	    
	    /* 
	   	 1. 清理預購記錄
	   	 2. @param proNo 產品編號
	   	 */
	    private void cleanupPreOrderRecords(Integer proNo) {
	        String redisKey = "preorder:" + proNo;
	        Boolean deleted = redisTemplate.delete(redisKey);
	        log.debug("預購記錄已清理：proNo={}, deleted={}", proNo, deleted);
	    }
	    
	    
	    // ========== 綠界付款表單生成 ========== //
	    /* 
	   	 1. 生成綠界付款表單
	   	 2. @param order 訂單
	   	 3. @param tradeNo 交易編號
	   	 4. @return HTML 表單
	   	 */
	    private String generateECPayForm(OrderDTO order, String tradeNo) {
	        try {
	            log.debug("開始生成綠界付款表單");
	            
	            // 準備綠界參數
	            Map<String, String> params = new TreeMap<>();
	            params.put("MerchantID", merchantId);
	            params.put("MerchantTradeNo", tradeNo);
	            params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
	            params.put("PaymentType", "aio");
	            params.put("TotalAmount", order.getOrderTotal().toString());
	            params.put("TradeDesc", "像素部落商城購物");
	            params.put("ItemName", getItemDescription(order));
	            params.put("ReturnURL", returnUrl);
	            params.put("OrderResultURL", notifyUrl);
	            params.put("ChoosePayment", "ALL");
	            params.put("EncryptType", "1");
	            
	            log.debug("綠界參數準備完成：{}", params);
	            
	            // 生成檢查碼
	            String checkMacValue = generateCheckMacValue(params);
	            params.put("CheckMacValue", checkMacValue);
	            log.debug("檢查碼生成完成");
	            
	            // 生成 HTML 表單
	            String html = generateAutoSubmitForm(params);
	            log.debug("HTML表單生成完成");
	            
	            return html;
	            
	        } catch (Exception e) {
	            log.error("生成綠界付款表單失敗：orderNo={}", order.getOrderNo(), e);
	            throw new RuntimeException("生成付款表單失敗：" + e.getMessage());
	        }
	    }
	        
	        
	        /* 
		   	 1. 取得商品描述
		   	 2. @param order 訂單
		   	 3. @return 商品描述
		   	 */
	        private String getItemDescription(OrderDTO order) {
	            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
	                return "遊戲產品";
	            }
	            
	            if (order.getOrderItems().size() == 1) {
	                return order.getOrderItems().get(0).getProductName();
	            } else {
	                return order.getOrderItems().get(0).getProductName() + " 等 " + order.getOrderItems().size() + " 項產品";
	            }
	        }
	        
	        /* 
		   	 1. 生成綠界檢查碼
		   	 2. @param params 參數
		   	 3. @return 檢查碼
		   	 */
	        private String generateCheckMacValue(Map<String, String> params) throws Exception {
	            // 組合參數字串
	            StringBuilder sb = new StringBuilder();
	            sb.append("HashKey=").append(hashKey).append("&");
	            
	            for (Map.Entry<String, String> entry : params.entrySet()) {
	                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
	            }
	            
	            sb.append("HashIV=").append(hashIV);
	            
	            // URL encode 並轉小寫
	            String encodedStr = URLEncoder.encode(sb.toString(), "UTF-8").toLowerCase();
	            
	            // 替換特殊字符
	            encodedStr = encodedStr.replace("%2d", "-").replace("%5f", "_")
	                                   .replace("%2e", ".").replace("%21", "!")
	                                   .replace("%2a", "*").replace("%28", "(")
	                                   .replace("%29", ")");
	            
	            // SHA256 加密
	            MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] hash = digest.digest(encodedStr.getBytes("UTF-8"));
	            
	            // 轉為大寫十六進位
	            StringBuilder hexString = new StringBuilder();
	            for (byte b : hash) {
	                String hex = Integer.toHexString(0xff & b);
	                if (hex.length() == 1) {
	                    hexString.append('0');
	                }
	                hexString.append(hex);
	            }
	            
	            return hexString.toString().toUpperCase();
	        }
	        
	        
	        /* 
		   	 1. 生成自動提交的 HTML 表單
		   	 2. @param params 參數
		   	 3. @return HTML
		   	 */
	        private String generateAutoSubmitForm(Map<String, String> params) {
	            StringBuilder html = new StringBuilder();
	            html.append("<!DOCTYPE html>");
	            html.append("<html><head>");
	            html.append("<meta charset='UTF-8'>");
	            html.append("<title>跳轉付款頁面</title>");
	            html.append("<style>");
	            html.append("body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }");
	            html.append(".loading { color: #666; }");
	            html.append("</style>");
	            html.append("</head>");
	            html.append("<body>");
	            html.append("<div class='loading'>");
	            html.append("<h3>正在跳轉到付款頁面...</h3>");
	            html.append("<p>請稍候，系統正在為您準備付款環境</p>");
	            html.append("</div>");
	            html.append("<form id='ecpayForm' method='post' action='").append(ecpayUrl).append("'>");
	            
	            for (Map.Entry<String, String> entry : params.entrySet()) {
	                html.append("<input type='hidden' name='").append(entry.getKey())
	                    .append("' value='").append(escapeHtml(entry.getValue())).append("'>");
	            }
	            
	            html.append("</form>");
	            html.append("<script>");
	            html.append("setTimeout(function() { document.getElementById('ecpayForm').submit(); }, 1000);");
	            html.append("</script>");
	            html.append("</body></html>");
	            
	            return html.toString();
	        }
	        
	        
	        /* 
		   	 1. HTML 特殊字符轉義
		   	 2. @param value 原始值
		   	 3. @return 轉義後的值
		   	 */
	        private String escapeHtml(String value) {
	            if (!StringUtils.hasText(value)) {
	                return "";
	            }
	            return value.replace("&", "&amp;")
	                       .replace("<", "&lt;")
	                       .replace(">", "&gt;")
	                       .replace("\"", "&quot;")
	                       .replace("'", "&#x27;");
	        }
	        
	        
	        /* 
		   	 1. 驗證綠界回調
		   	 2. @param params 回調參數
		   	 3. @return 是否驗證通過
		   	 */
	        private boolean validateECPayCallback(Map<String, String> params) {
	            try {
	                String receivedCheckMacValue = params.get("CheckMacValue");
	                if (!StringUtils.hasText(receivedCheckMacValue)) {
	                    log.error("綠界回調缺少 CheckMacValue");
	                    return false;
	                }
	                
	                Map<String, String> paramsForCheck = new TreeMap<>(params);
	                paramsForCheck.remove("CheckMacValue");
	                
	                String calculatedCheckMacValue = generateCheckMacValue(paramsForCheck);
	                
	                boolean isValid = receivedCheckMacValue.equals(calculatedCheckMacValue);
	                if (!isValid) {
	                    log.error("CheckMacValue 驗證失敗：received={}, calculated={}", 
	                             receivedCheckMacValue, calculatedCheckMacValue);
	                }
	                
	                return isValid;
	                
	            } catch (Exception e) {
	                log.error("驗證綠界回調失敗", e);
	                return false;
	            }
	        }
	    
}