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

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pixeltribe.shopsys.cart.model.CartService;
import com.pixeltribe.shopsys.cart.model.StockInfoResponse;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Component
@Transactional
public class PaymentService {
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private CartService cartService;
	
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
    	
    	System.out.println("🔥🔥🔥 PaymentService.initiatePayment 開始執行！orderNo=" + orderNo + " 🔥🔥🔥");
    	
    	try {
    		log.info("開始付款:orderNo={}", orderNo);
    		
    		// ✅ 加入每個步驟的除錯
            System.out.println("=== 步驟1：檢查訂單狀態 ===");
            OrderDTO order = orderService.getOrderDetail(orderNo);
            System.out.println("訂單狀態：" + order.getOrderStatus());
            
            System.out.println("=== 步驟2：驗證訂單狀態 ===");
            if (!"PENDING".equals(order.getOrderStatus())) {
                System.out.println("❌ 訂單狀態不是 PENDING：" + order.getOrderStatus());
                throw new RuntimeException("訂單狀態錯誤，無法付款");
            }
            
            System.out.println("=== 步驟3：準備綠界付款參數 ===");
    		
    		
    		// 1. 檢查訂單狀態
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
            log.info("🔍 開始執行付款超時檢查 - {}", LocalDateTime.now());
            
            Set<String> paymentKeys = redisTemplate.keys("payment:*");
            if (paymentKeys == null || paymentKeys.isEmpty()) {
                log.info("📝 沒有找到付款記錄");
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            int timeoutCount = 0;
            int totalChecked = 0;
            
            log.info("📊 找到 {} 筆付款記錄需要檢查", paymentKeys.size());
            
            for (String key : paymentKeys) {
                try {
                    totalChecked++;
                    Map<Object, Object> paymentInfo = redisTemplate.opsForHash().entries(key);
                    if (paymentInfo.isEmpty()) continue;
                    
                    String status = (String) paymentInfo.get("status");
                    Long createdAt = (Long) paymentInfo.get("createdAt");
                    Integer orderNo = (Integer) paymentInfo.get("orderNo");

                    // ✅ 增加詳細日誌
                    if (createdAt != null) {
                        long ageMinutes = (currentTime - createdAt) / 60000;
                        log.debug("檢查付款記錄：orderNo={}, status={}, ageMinutes={}", 
                                 orderNo, status, ageMinutes);
                    }

                    // ✅ 檢查是否超過30分鐘且狀態為 PAYING
                    if ("PAYING".equals(status) && 
                            createdAt != null && 
                            (currentTime - createdAt) > 1800000) { // 30分鐘
                            
                        long ageMinutes = (currentTime - createdAt) / 60000;
                        log.warn("⏰ 發現超時付款：orderNo={}, 超時時間={}分鐘", orderNo, ageMinutes);
                            
                        try {
                            // ✅ 更新訂單狀態為失敗
                            orderService.updateOrderStatus(orderNo, "FAILED");        
                            log.info("✅ 訂單狀態已更新為 FAILED：orderNo={}", orderNo);
                            
                            // ✅ 更新Redis狀態
                            Map<String, String> timeoutInfo = new HashMap<>();
                            timeoutInfo.put("reason", "付款超時");
                            timeoutInfo.put("timeoutTime", LocalDateTime.now().toString());
                            timeoutInfo.put("originalCreatedAt", createdAt.toString());
                            timeoutInfo.put("ageMinutes", String.valueOf(ageMinutes));
                            
                            updatePaymentStatusInRedis(orderNo, "TIMEOUT", timeoutInfo);
                            
                            timeoutCount++;
                            log.info("✅ 訂單付款超時處理完成：orderNo={}", orderNo);
                            
                        } catch (Exception updateEx) {
                            log.error("❌ 更新超時訂單失敗：orderNo={}", orderNo, updateEx);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("❌ 處理單個付款記錄失敗：key={}", key, e);
                }
            }
            
            if (timeoutCount > 0) {
                log.warn("📈 本次處理了 {} 筆超時付款，總檢查 {} 筆", timeoutCount, totalChecked);
            } else {
                log.info("✅ 本次檢查 {} 筆記錄，無超時付款", totalChecked);
            }
            
        } catch (Exception e) {
            log.error("❌ 執行付款超時檢查失敗", e);
        }
    }
    
 // ✅ 新增：手動檢查超時付款的方法（用於調試）
    public Map<String, Object> manualCheckTimeout() {
        try {
            log.info("🔧 手動執行付款超時檢查");
            
            // 直接調用定時任務方法
            handlePaymentTimeout();
            
            // 返回當前所有付款狀態
            Set<String> paymentKeys = redisTemplate.keys("payment:*");
            Map<String, Object> result = new HashMap<>();
            
            if (paymentKeys != null) {
                List<Map<String, Object>> payments = new ArrayList<>();
                
                for (String key : paymentKeys) {
                    Map<Object, Object> paymentInfo = redisTemplate.opsForHash().entries(key);
                    if (!paymentInfo.isEmpty()) {
                        Map<String, Object> payment = new HashMap<>();
                        payment.put("orderNo", paymentInfo.get("orderNo"));
                        payment.put("status", paymentInfo.get("status"));
                        payment.put("createdAt", paymentInfo.get("createdAt"));
                        
                        // 計算時間差
                        Long createdAt = (Long) paymentInfo.get("createdAt");
                        if (createdAt != null) {
                            long diffMinutes = (System.currentTimeMillis() - createdAt) / 60000;
                            payment.put("ageMinutes", diffMinutes);
                            payment.put("isTimeout", diffMinutes > 30);
                        }
                        
                        payments.add(payment);
                    }
                }
                
                result.put("totalPayments", payments.size());
                result.put("payments", payments);
            } else {
                result.put("totalPayments", 0);
                result.put("payments", new ArrayList<>());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("手動檢查超時失敗", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
    

    
    	// ****定時任務：檢查預購商品是否上架 (每天晚上8點檢查) 透過檢查 pro_serial_numbers 表中的序號庫存判斷商品是否可發貨 **** //
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
	            emailService.sendPreOrderDeliveryEmail(order, preOrderItem.getProductName(), serialNumber);
	            
	            log.info("預購訂單處理完成：orderNo={}, orderItemNo={}, serial={}", 
	                    orderNo, preOrderItem.getOrderItemNo(), serialNumber);
	            
	        } catch (Exception e) {
	            log.error("處理預購訂單交貨失敗：orderNo={}, proNo={}", orderNo, proNo, e);
	            throw e;
	        }
	    }
	   
	    // ========== 訂單處理流程 ========== //
	    // *** 觸發訂單後續處理 (付款成功後調用) *** //
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
	            sendCompletionEmailWithSerials(order, inStockSerials, preOrderItems);
	            
	            // 5. 更新訂單狀態為已完成
	            orderService.updateOrderStatus(order.getOrderNo(), "COMPLETED");
	            log.info("訂單處理完成：orderNo={}, 現貨序號數={}, 預購商品數={}", 
	                    order.getOrderNo(), inStockSerials.size(), preOrderItems.size());
	            
	        } catch (Exception e) {
	            log.error("觸發訂單後續處理失敗：orderNo={}", order.getOrderNo(), e);
	        }
	    }
	    
	    // *** 分離現貨和預購商品 *** // 
	    private Map<String, List<OrderItemDTO>> separateOrderItems(OrderDTO order) {
	        List<OrderItemDTO> inStockItems = new ArrayList<>();
	        List<OrderItemDTO> preOrderItems = new ArrayList<>();
	        
	        for (OrderItemDTO item : order.getOrderItems()) {
	        	try {
	                // 🔥 使用 CartService 的庫存判斷邏輯
	                StockInfoResponse stockInfo = cartService.getStockInfo(item.getProNo());
	                
	                if (stockInfo != null) {
	                    // 使用我們修正過的智能判斷方法
	                    if (stockInfo.isDefinitelyInStock()) {
	                        inStockItems.add(item);
	                        log.debug("分類為現貨：proNo={}, productName={}, stockSource={}", 
	                                 item.getProNo(), item.getProName(), stockInfo.getStockSource());
	                    } else {
	                        preOrderItems.add(item);
	                        log.debug("分類為預購：proNo={}, productName={}, stockSource={}", 
	                                 item.getProNo(), item.getProName(), stockInfo.getStockSource());
	                    }
	                } else {
	                    // 如果無法取得庫存資訊，預設為預購（安全起見）
	                    preOrderItems.add(item);
	                    log.warn("無法取得庫存資訊，預設為預購：proNo={}", item.getProNo());
	                }
	                
	            } catch (Exception e) {
	                log.error("分類商品時發生錯誤：proNo={}", item.getProNo(), e);
	                // 發生錯誤時預設為預購（安全起見）
	                preOrderItems.add(item);
	            }
	        }
	        
	        Map<String, List<OrderItemDTO>> result = new HashMap<>();
	        result.put("inStock", inStockItems);
	        result.put("preOrder", preOrderItems);
	        
	        log.info("商品分類完成：現貨={}項，預購={}項", inStockItems.size(), preOrderItems.size());
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
	                    log.info("現貨序號發放成功：proNo={}, productName={}, serial={}", 
	                            item.getProNo(), item.getProName(), serialNumber);
	                } else {
	                    // 如果沒有可用序號，記錄錯誤但不中斷流程
	                    serialNumbers.add("序號發放中，請聯繫客服");
	                    log.error("現貨商品序號發放失敗：proNo={}, orderItemNo={}, productName={}", 
	                             item.getProNo(), item.getOrderItemNo(), item.getProName());
	                }
	                
	            } catch (Exception e) {
	                log.error("處理現貨序號時發生錯誤：proNo={}, productName={}", 
	                         item.getProNo(), item.getProName(), e);
	                serialNumbers.add("序號處理中，請聯繫客服");
	            }
	        }
	        
	        log.info("現貨序號處理完成：成功分配={}個", serialNumbers.size());
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
	            
	            // 發送付款失敗郵件
	            try {
	                OrderDTO order = orderService.getOrderDetail(orderNo);
	                emailService.sendPaymentFailedEmail(order, reason);
	                log.info("付款失敗郵件已發送：orderNo={}", orderNo);
	            } catch (Exception emailEx) {
	                log.error("發送付款失敗郵件失敗：orderNo={}", orderNo, emailEx);
	            }
	            
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

	    // *** 生成交易編號 ***//
	    private String generateTradeNo(Integer orderNo) {
	    	// 只取年月日時分 (10位) + 訂單號後6位
	        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm")); // 10位
	        String orderSuffix = String.valueOf(orderNo).substring(Math.max(0, String.valueOf(orderNo).length() - 6)); // 最多6位
	        return "O" + timestamp + orderSuffix; // 1+10+6=17字元 ✅

	    }
	    
	    
	    // *** 檢查是否有進行中的付款 ***//
	   	
	    private boolean hasOngoingPayment(Integer orderNo) {
	        String redisKey = "payment:" + orderNo;
	        String status = (String) redisTemplate.opsForHash().get(redisKey, "status");
	        return "PAYING".equals(status);
	    }
	    
	    
	    // *** 存儲付款資訊到 Redis *** //
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
	            
	            // ✅ 加入除錯
	            String itemName = getItemDescription(order);
	            System.out.println("🔥 ItemName 除錯：" + itemName);
	            System.out.println("🔥 OrderItems 數量：" + (order.getOrderItems() != null ? order.getOrderItems().size() : "null"));
	            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
	                System.out.println("🔥 第一個商品名稱：" + order.getOrderItems().get(0).getProductName());
	            }
	            
	            // ✅ 強制確保不是 null
	            if (itemName == null || itemName.trim().isEmpty()) {
	                itemName = "像素部落商城商品";
	                System.out.println("🔥 強制設定 ItemName：" + itemName);
	            }	
	            
	            
	            
	            // 準備綠界參數
	            Map<String, String> params = new TreeMap<>();
	            params.put("MerchantID", merchantId);
	            params.put("MerchantTradeNo", tradeNo);
	            params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
	            params.put("PaymentType", "aio");
	            params.put("TotalAmount", order.getOrderTotal().toString());
	            params.put("TradeDesc", "像素部落商城購物");
	            params.put("ItemName", itemName);
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
	        
	        
	        // *** 取得商品描述 *** // 
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
	        
	        
	        // *** 驗證綠界回調 *** //
	        private boolean validateECPayCallback(Map<String, String> params) {
	            try {
	                String receivedCheckMacValue = params.get("CheckMacValue");
	                String tradeNo = params.get("MerchantTradeNo");
	                
	                // 🔥 測試模式：檢查多種測試條件
	                if ((tradeNo != null && tradeNo.startsWith("TEST_")) ||
	                    "TEST_CHECKSUM".equals(receivedCheckMacValue)) {
	                    log.info("🧪 測試模式：跳過 CheckMacValue 驗證，tradeNo={}, checkMacValue={}", 
	                             tradeNo, receivedCheckMacValue);
	                    return true;
	                }
	                
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
	        
	        
	        // ========== <<前台使用的增刪改查>> ========== //
	        // **** 用戶點擊付款 (權限驗證) *****//
	        public String createPayment(Integer orderNo, Integer memNo) {
	            try {
	                log.info("用戶發起付款：orderNo={}, memNo={}", orderNo, memNo);
	                
	                // 驗證訂單所有權
	                OrderDTO order = orderService.getOrderDetail(orderNo);
	                if (!order.getMemNo().equals(memNo)) {
	                    throw new IllegalArgumentException("無權限操作此訂單");
	                }
	                
	                // 重用現有的付款邏輯
	                return initiatePayment(orderNo);
	                
	            } catch (Exception e) {
	                log.error("用戶發起付款失敗：orderNo={}, memNo={}", orderNo, memNo, e);
	                throw new RuntimeException("發起付款失敗：" + e.getMessage());
	            }
	        }
	        
	        
		    // **** 會員查付款紀錄 *****//
	        public Map<String, Object> getMemberPaymentRecords(Integer memNo, Integer page, Integer size) {
	            try {
	                log.debug("查詢會員付款記錄：memNo={}", memNo);
	                
	                // 1. 查詢會員訂單 (重用 orderService)
	                List<OrderDTO> allOrders = orderService.getmemOrders(memNo);
	                
	                // 2. 簡單分頁
	                int startIndex = page * size;
	                int endIndex = Math.min(startIndex + size, allOrders.size());
	                List<OrderDTO> pagedOrders = allOrders.subList(startIndex, endIndex);
	                
	                // 3. 組裝付款記錄
	                List<Map<String, Object>> paymentRecords = new ArrayList<>();
	                for (OrderDTO order : pagedOrders) {
	                    Map<String, Object> record = new HashMap<>();
	                    record.put("orderNo", order.getOrderNo());
	                    record.put("orderTotal", order.getOrderTotal());
	                    record.put("orderStatus", order.getOrderStatus());
	                    record.put("orderStatusInfo", order.getOrderStatusInfo());
	                    record.put("orderDatetime", order.getOrderDatetime());
	                    record.put("canRetryPayment", order.canRetryPayment());
	                    record.put("canBeCancelled", order.canBeCancelled());
	                    
	                    // 從 Redis 查詢付款狀態 (重用現有方法)
	                    Map<String, Object> paymentStatus = getPaymentStatus(order.getOrderNo());
	                    if (paymentStatus != null) {
	                        record.put("paymentInfo", paymentStatus);
	                    } else {
	                        record.put("paymentInfo", createDefaultPaymentInfo(order));
	                    }
	                    
	                    paymentRecords.add(record);
	                }
	                
	                // 4. 分頁資訊
	                Map<String, Object> result = new HashMap<>();
	                result.put("records", paymentRecords);
	                result.put("currentPage", page);
	                result.put("totalRecords", allOrders.size());
	                result.put("totalPages", (int) Math.ceil((double) allOrders.size() / size));
	                result.put("hasNext", endIndex < allOrders.size());
	                result.put("hasPrevious", page > 0);
	                
	                return result;
	                
	            } catch (Exception e) {
	                log.error("查詢會員付款記錄失敗：memNo={}", memNo, e);
	                throw new RuntimeException("查詢付款記錄失敗：" + e.getMessage());
	            }
	        }
	        
	        
	        // **** 前台查詢單筆訂單付款詳情(READ-詳細版) *****//
	        public Map<String, Object> getOrderPaymentDetail(Integer orderNo, Integer memNo) {
	            try {
	                log.debug("查詢訂單付款詳情：orderNo={}, memNo={}", orderNo, memNo);
	                
	                // 1. 驗證權限
	                OrderDTO order = orderService.getOrderDetail(orderNo);
	                if (!order.getMemNo().equals(memNo)) {
	                    throw new IllegalArgumentException("無權限查看此訂單");
	                }
	                
	                // 2. 組裝詳細付款資訊
	                Map<String, Object> detail = new HashMap<>();
	                detail.put("orderInfo", order);
	                detail.put("paymentStatus", getPaymentStatus(orderNo));
	                detail.put("availableActions", getAvailablePaymentActions(order));
	                
	                // 3. 序號發放狀況 (如果有的話)
	                if ("COMPLETED".equals(order.getOrderStatus()) || "SHIPPED".equals(order.getOrderStatus())) {
	                    detail.put("serialInfo", getOrderSerialInfo(orderNo));
	                }
	                
	                return detail;
	                
	            } catch (Exception e) {
	                log.error("查詢訂單付款詳情失敗：orderNo={}, memNo={}", orderNo, memNo, e);
	                throw new RuntimeException("查詢付款詳情失敗：" + e.getMessage());
	            }
	        }
	        
	        
	        // **** 前台查詢付款進度(READ-即時狀態) *****//
	        public Map<String, Object> getPaymentProgress(Integer orderNo, Integer memNo) {
	            try {
	                // 1. 驗證權限
	                OrderDTO order = orderService.getOrderDetail(orderNo);
	                if (!order.getMemNo().equals(memNo)) {
	                    throw new IllegalArgumentException("無權限查看此訂單");
	                }
	                
	                // 2. 組裝進度資訊
	                Map<String, Object> progress = new HashMap<>();
	                progress.put("orderNo", orderNo);
	                progress.put("currentStatus", order.getOrderStatus());
	                progress.put("statusInfo", order.getOrderStatusInfo());
	                progress.put("paymentInfo", getPaymentStatus(orderNo));
	                
	                // 3. 進度步驟
	                progress.put("progressSteps", createProgressSteps(order));
	                
	                // 4. 下一步操作
	                progress.put("nextActions", getAvailablePaymentActions(order));
	                
	                return progress;
	                
	            } catch (Exception e) {
	                log.error("查詢付款進度失敗：orderNo={}, memNo={}", orderNo, memNo, e);
	                throw new RuntimeException("查詢付款進度失敗：" + e.getMessage());
	            }
	        }
	        
		    // **** 會員要重新付款 *****//
	        public String retryPayment(Integer orderNo, Integer memNo) {
	            try {
	                log.info("用戶重新發起付款：orderNo={}, memNo={}", orderNo, memNo);
	                return createPayment(orderNo, memNo); // 重用驗證邏輯
	            } catch (Exception e) {
	                log.error("重新發起付款失敗：orderNo={}, memNo={}", orderNo, memNo, e);
	                throw new RuntimeException("重新付款失敗：" + e.getMessage());
	            }
	        }
	        
		    // **** 會員要取消付款 *****//
	        public boolean cancelPayment(Integer orderNo, Integer memNo, String reason) {
	            try {
	                log.info("用戶取消付款：orderNo={}, memNo={}, reason={}", orderNo, memNo, reason);
	                
	                // 驗證權限
	                OrderDTO order = orderService.getOrderDetail(orderNo);
	                if (!order.getMemNo().equals(memNo)) {
	                    throw new IllegalArgumentException("無權限操作此訂單");
	                }
	                
	                // 檢查是否可以取消
	                if (!order.canBeCancelled() && !"PAYING".equals(order.getOrderStatus())) {
	                    log.warn("訂單狀態不允許取消：orderNo={}, status={}", orderNo, order.getOrderStatus());
	                    return false;
	                }
	                
	                // 如果正在付款中，先清理 Redis
	                if ("PAYING".equals(order.getOrderStatus())) {
	                    String redisKey = "payment:" + orderNo;
	                    redisTemplate.delete(redisKey);
	                    log.info("已清理付款中的 Redis 記錄：orderNo={}", orderNo);
	                }
	                
	                // 重用 orderService 的取消邏輯
	                return orderService.cancelOrder(orderNo, memNo, reason);
	                
	            } catch (Exception e) {
	                log.error("取消付款失敗：orderNo={}, memNo={}", orderNo, memNo, e);
	                return false;
	            }
	        }
	        
	        
	        
	        
	        // ========== <<後台使用的增刪改查>> ========== //
	        // **** 管理員查詢付款的<<統計>> *****//
	        public Map<String, Object> getPaymentStatistics() {
	            try {
	                log.debug("計算付款統計資料");
	                
	                Map<String, Object> stats = new HashMap<>();
	                
	                // 今日訂單統計
	                String todayOrdersSql = "SELECT COUNT(*) as count, COALESCE(SUM(order_total), 0) as total " +
	                                       "FROM `order` WHERE DATE(order_datetime) = CURDATE()";
	                Map<String, Object> todayOrders = jdbcTemplate.queryForMap(todayOrdersSql);
	                
	                // 今日成功付款統計
	                String todaySuccessSql = "SELECT COUNT(*) as count, COALESCE(SUM(order_total), 0) as total " +
	                                        "FROM `order` WHERE DATE(order_datetime) = CURDATE() " +
	                                        "AND order_status IN ('COMPLETED', 'SHIPPED', 'PROCESSING')";
	                Map<String, Object> todaySuccess = jdbcTemplate.queryForMap(todaySuccessSql);
	                
	                // 訂單狀態分布
	                String statusSql = "SELECT order_status, COUNT(*) as count FROM `order` " +
	                                  "WHERE order_datetime >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
	                                  "GROUP BY order_status";
	                List<Map<String, Object>> statusStats = jdbcTemplate.queryForList(statusSql);
	                
	                // 計算成功率
	                Long totalToday = ((Number) todayOrders.get("count")).longValue();
	                Long successToday = ((Number) todaySuccess.get("count")).longValue();
	                Double successRate = totalToday > 0 ? (double) successToday / totalToday * 100 : 0.0;
	                
	                stats.put("todayOrders", todayOrders);
	                stats.put("todaySuccess", todaySuccess);
	                stats.put("statusDistribution", statusStats);
	                stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
	                stats.put("generatedAt", LocalDateTime.now());
	                
	                return stats;
	                
	            } catch (Exception e) {
	                log.error("計算付款統計失敗", e);
	                throw new RuntimeException("計算付款統計失敗：" + e.getMessage());
	            }
	        }
	        
	        
	        // **** 管理員重製卡單付款 *****//
	        public boolean resetStuckPayment(Integer orderNo, String adminReason) {
	            try {
	                log.info("管理員重置卡單付款：orderNo={}, reason={}", orderNo, adminReason);
	                
	                // 檢查訂單是否在付款中
	                OrderDTO order = orderService.getOrderDetail(orderNo);
	                if (!"PAYING".equals(order.getOrderStatus())) {
	                    log.warn("訂單不在付款中狀態：orderNo={}, status={}", orderNo, order.getOrderStatus());
	                    return false;
	                }
	                
	                // 重置為待付款狀態
	                orderService.updateOrderStatus(orderNo, "PENDING");
	                
	                // 清理 Redis 付款狀態
	                String redisKey = "payment:" + orderNo;
	                redisTemplate.delete(redisKey);
	                
	                // 記錄管理員操作
	                log.info("ADMIN_RESET_PAYMENT|orderNo={}|reason={}|timestamp={}", 
	                        orderNo, adminReason, System.currentTimeMillis());
	                
	                return true;
	                
	            } catch (Exception e) {
	                log.error("重置卡單付款失敗：orderNo={}", orderNo, e);
	                return false;
	            }
	        }
	        
	        
		    // **** 管理員查詢所有的付款紀錄 *****//
	        public Map<String, Object> getAllPaymentRecords(Map<String, Object> filters, Integer page, Integer size) {
	            try {
	                log.debug("管理員查詢付款記錄：filters={}", filters);
	                
	                // 查詢所有訂單 (可加篩選)
	                String sql = "SELECT o.*, m.mem_name, m.mem_email FROM `order` o " +
	                            "LEFT JOIN member m ON o.mem_no = m.mem_no " +
	                            "WHERE 1=1 ";
	                
	                List<Object> params = new ArrayList<>();
	                
	                // 動態添加篩選條件
	                if (filters.get("orderStatus") != null) {
	                    sql += "AND o.order_status = ? ";
	                    params.add(filters.get("orderStatus"));
	                }
	                
	                if (filters.get("startDate") != null) {
	                    sql += "AND DATE(o.order_datetime) >= ? ";
	                    params.add(filters.get("startDate"));
	                }
	                
	                if (filters.get("endDate") != null) {
	                    sql += "AND DATE(o.order_datetime) <= ? ";
	                    params.add(filters.get("endDate"));
	                }
	                
	                // 計算總數
	                String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS temp";
	                Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
	                
	                // 添加排序和分頁
	                sql += "ORDER BY o.order_datetime DESC LIMIT ? OFFSET ?";
	                params.add(size);
	                params.add(page * size);
	                
	                // 執行查詢
	                List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql, params.toArray());
	                
	                // 為每個訂單添加付款狀態
	                for (Map<String, Object> order : orders) {
	                    Integer orderNo = (Integer) order.get("order_no");
	                    Map<String, Object> paymentStatus = getPaymentStatus(orderNo);
	                    order.put("paymentInfo", paymentStatus != null ? paymentStatus : createDefaultPaymentInfo(order));
	                }
	                
	                // 組裝結果
	                Map<String, Object> result = new HashMap<>();
	                result.put("records", orders);
	                result.put("currentPage", page);
	                result.put("totalCount", totalCount);
	                result.put("totalPages", (int) Math.ceil((double) totalCount / size));
	                result.put("hasNext", (page + 1) * size < totalCount);
	                result.put("hasPrevious", page > 0);
	                
	                return result;
	                
	            } catch (Exception e) {
	                log.error("管理員查詢付款記錄失敗", e);
	                throw new RuntimeException("查詢付款記錄失敗：" + e.getMessage());
	            }
	        }
		    
	        
		    // **** 管理員批量清理過期付款紀錄 *****//
	        public Map<String, Object> cleanupExpiredPayments(String adminId) {
	            try {
	                log.info("管理員清理過期付款記錄：admin={}", adminId);
	                
	                Set<String> paymentKeys = redisTemplate.keys("payment:*");
	                if (paymentKeys == null || paymentKeys.isEmpty()) {
	                    Map<String, Object> result = new HashMap<>();
	                    result.put("clearedCount", 0);
	                    result.put("message", "沒有找到付款記錄");
	                    return result;
	                }
	                
	                int clearedCount = 0;
	                long currentTime = System.currentTimeMillis();
	                
	                for (String key : paymentKeys) {
	                    try {
	                        Map<Object, Object> paymentInfo = redisTemplate.opsForHash().entries(key);
	                        if (paymentInfo.isEmpty()) continue;
	                        
	                        String status = (String) paymentInfo.get("status");
	                        Long createdAt = (Long) paymentInfo.get("createdAt");
	                        
	                        // 清理 24 小時以上的非成功付款記錄
	                        if (!"SUCCESS".equals(status) && 
	                            createdAt != null && 
	                            (currentTime - createdAt) > 86400000) { // 24小時
	                            
	                            redisTemplate.delete(key);
	                            clearedCount++;
	                        }
	                        
	                    } catch (Exception e) {
	                        log.error("清理單個付款記錄失敗：key={}", key, e);
	                    }
	                }
	                
	                // 記錄管理員操作
	                log.info("ADMIN_CLEANUP_PAYMENTS|admin={}|clearedCount={}|timestamp={}", 
	                        adminId, clearedCount, System.currentTimeMillis());
	                
	                Map<String, Object> result = new HashMap<>();
	                result.put("clearedCount", clearedCount);
	                result.put("message", "批量清理完成");
	                
	                return result;
	                
	            } catch (Exception e) {
	                log.error("管理員清理過期付款記錄失敗", e);
	                throw new RuntimeException("批量清理失敗：" + e.getMessage());
	            }
	        }
	        
	        
	        // **** 後台手動標記付款成功 (CREATE/UPDATE_特殊情況處理) *****//
	        public boolean adminMarkPaymentSuccess(Integer orderNo, String adminId, String reason) {
	            try {
	                log.info("管理員手動標記付款成功：orderNo={}, admin={}, reason={}", orderNo, adminId, reason);
	                
	                // 1. 檢查訂單
	                OrderDTO order = orderService.getOrderDetail(orderNo);
	                if (order == null) {
	                    throw new IllegalArgumentException("訂單不存在：" + orderNo);
	                }
	                
	                // 2. 生成交易編號
	                String tradeNo = "ADMIN_" + generateTradeNo(orderNo);
	                
	                // 3. 存儲到 Redis (模擬付款成功)
	                Map<String, Object> paymentInfo = new HashMap<>();
	                paymentInfo.put("orderNo", orderNo);
	                paymentInfo.put("tradeNo", tradeNo);              //綠界的資訊
	                paymentInfo.put("amount", order.getOrderTotal());
	                paymentInfo.put("status", "SUCCESS");
	                paymentInfo.put("method", "ADMIN_MANUAL");
	                paymentInfo.put("createdAt", System.currentTimeMillis());
	                paymentInfo.put("memNo", order.getMemNo());
	                paymentInfo.put("adminCreated", true);
	                paymentInfo.put("adminId", adminId);
	                paymentInfo.put("adminReason", reason);
	                
	                String redisKey = "payment:" + orderNo;
	                redisTemplate.opsForHash().putAll(redisKey, paymentInfo);
	                redisTemplate.expire(redisKey, 24, TimeUnit.HOURS);    //一天清一次
	                
	                // 4. 更新訂單狀態為處理中
	                orderService.updateOrderStatus(orderNo, "PROCESSING");
	                
	                // 5. 觸發後續處理 (序號發放等)
	                triggerOrderProcessing(order);
	                
	                // 6. 記錄管理員操作
	                log.info("ADMIN_MANUAL_PAYMENT_SUCCESS|orderNo={}|admin={}|reason={}|timestamp={}", 
	                        orderNo, adminId, reason, System.currentTimeMillis());
	                
	                return true;
	                
	            } catch (Exception e) {
	                log.error("管理員手動標記付款成功失敗：orderNo={}", orderNo, e);
	                return false;
	            }
	        }
	        
	        
	        // **** 後台查詢付款趨勢 (READ - 圖表數據) *****//
	        public List<Map<String, Object>> getPaymentTrends(Integer days) {
	            try {
	                log.debug("查詢付款趨勢：days={}", days);
	                
	                String sql = "SELECT " +
	                            "DATE(order_datetime) as payment_date, " +
	                            "COUNT(*) as total_orders, " +
	                            "SUM(CASE WHEN order_status IN ('COMPLETED', 'SHIPPED', 'PROCESSING') THEN 1 ELSE 0 END) as success_orders, " +
	                            "SUM(CASE WHEN order_status IN ('COMPLETED', 'SHIPPED', 'PROCESSING') THEN order_total ELSE 0 END) as success_amount, " +
	                            "SUM(CASE WHEN order_status = 'FAILED' THEN 1 ELSE 0 END) as failed_orders " +
	                            "FROM `order` " +
	                            "WHERE order_datetime >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
	                            "GROUP BY DATE(order_datetime) " +
	                            "ORDER BY payment_date DESC";
	                
	                List<Map<String, Object>> trends = jdbcTemplate.queryForList(sql, days != null ? days : 30);
	                
	                // 計算每日成功率
	                for (Map<String, Object> trend : trends) {
	                    Long totalOrders = ((Number) trend.get("total_orders")).longValue();
	                    Long successOrders = ((Number) trend.get("success_orders")).longValue();
	                    Double successRate = totalOrders > 0 ? (double) successOrders / totalOrders * 100 : 0.0;
	                    trend.put("success_rate", Math.round(successRate * 100.0) / 100.0);
	                }
	                
	                return trends;
	                
	            } catch (Exception e) {
	                log.error("查詢付款趨勢失敗", e);
	                return new ArrayList<>();
	            }
	        }
	        
	        
	        // **** 後台更新付款方式狀態 (UPDATE - 系統配置) *****//
	        public boolean updatePaymentMethodStatus(String method, boolean enabled, String adminId) {
	            try {
	                log.info("管理員更新付款方式狀態：method={}, enabled={}, admin={}", method, enabled, adminId);
	                
	                // 這裡可以存到 Redis 或配置表
	                String configKey = "payment:config:" + method;
	                Map<String, Object> config = new HashMap<>();
	                config.put("method", method);
	                config.put("enabled", enabled);
	                config.put("updatedBy", adminId);
	                config.put("updatedAt", System.currentTimeMillis());
	                
	                redisTemplate.opsForHash().putAll(configKey, config);
	                
	                // 記錄管理員操作
	                log.info("ADMIN_UPDATE_PAYMENT_METHOD|method={}|enabled={}|admin={}|timestamp={}", 
	                        method, enabled, adminId, System.currentTimeMillis());
	                
	                return true;
	                
	            } catch (Exception e) {
	                log.error("更新付款方式狀態失敗：method={}", method, e);
	                return false;
	            }
	        }
	        
	        // **** 後台批量處理異常訂單 (UPDATE - 批量操作) *****//
	        public Map<String, Object> batchProcessAbnormalOrders(List<Integer> orderNos, String action, String adminId) {
	            try {
	                log.info("管理員批量處理異常訂單：orderNos={}, action={}, admin={}", orderNos, action, adminId);
	                
	                int successCount = 0;
	                int failureCount = 0;
	                List<String> errors = new ArrayList<>();
	                
	                for (Integer orderNo : orderNos) {
	                    try {
	                        boolean success = false;
	                        
	                        switch (action) {
	                            case "RESET_PAYMENT":
	                                success = resetStuckPayment(orderNo, "批量重置 - " + adminId);
	                                break;
	                            case "MARK_SUCCESS":
	                                success = adminMarkPaymentSuccess(orderNo, adminId, "批量標記成功");
	                                break;
	                            case "CLEANUP":
	                                String redisKey = "payment:" + orderNo;
	                                success = Boolean.TRUE.equals(redisTemplate.delete(redisKey));
	                                break;
	                            default:
	                                errors.add("訂單 " + orderNo + " - 未知操作：" + action);
	                                continue;
	                        }
	                        
	                        if (success) {
	                            successCount++;
	                        } else {
	                            failureCount++;
	                            errors.add("訂單 " + orderNo + " - 操作失敗");
	                        }
	                        
	                    } catch (Exception e) {
	                        failureCount++;
	                        errors.add("訂單 " + orderNo + " - 處理異常：" + e.getMessage());
	                        log.error("批量處理單個訂單失敗：orderNo={}", orderNo, e);
	                    }
	                }
	                
	                // 記錄批量操作
	                log.info("ADMIN_BATCH_PROCESS|action={}|admin={}|success={}|failure={}|timestamp={}", 
	                        action, adminId, successCount, failureCount, System.currentTimeMillis());
	                
	                Map<String, Object> result = new HashMap<>();
	                result.put("totalCount", orderNos.size());
	                result.put("successCount", successCount);
	                result.put("failureCount", failureCount);
	                result.put("errors", errors);
	                result.put("success", failureCount == 0);
	                
	                return result;
	                
	            } catch (Exception e) {
	                log.error("批量處理異常訂單失敗：orderNos={}", orderNos, e);
	                throw new RuntimeException("批量處理失敗：" + e.getMessage());
	            }
	        }
	        

	        
	        // ========== 輔助方法 ========== //
	        // ****** << 為 OrderDTO 建立預設付款資訊 >> ****** //
	        private Map<String, Object> createDefaultPaymentInfo(OrderDTO order) {
	            Map<String, Object> paymentInfo = new HashMap<>();
	            paymentInfo.put("status", getPaymentStatusFromOrder(order.getOrderStatus()));
	            paymentInfo.put("amount", order.getOrderTotal());
	            paymentInfo.put("method", "N/A");
	            paymentInfo.put("tradeNo", "N/A");
	            return paymentInfo;
	        }
	        
	        // ****** << 為 Map 建立預設付款資訊 >> ****** //
	        private Map<String, Object> createDefaultPaymentInfo(Map<String, Object> orderMap) {
	            Map<String, Object> paymentInfo = new HashMap<>();
	            paymentInfo.put("status", getPaymentStatusFromOrder((String) orderMap.get("order_status")));
	            paymentInfo.put("amount", (Integer) orderMap.get("order_total"));
	            paymentInfo.put("method", "N/A");
	            paymentInfo.put("tradeNo", "N/A");
	            return paymentInfo;
	        }
	        
	        
	        // *** 根據訂單狀態推斷付款狀態 *** //
	        private String getPaymentStatusFromOrder(String orderStatus) {
	            switch (orderStatus) {
	                case "PENDING":
	                    return "UNPAID";
	                case "PAYING":
	                    return "PAYING";
	                case "PROCESSING":
	                case "SHIPPED":
	                case "COMPLETED":
	                    return "SUCCESS";
	                case "FAILED":
	                    return "FAILED";
	                case "CANCELLED":
	                    return "CANCELLED";
	                default:
	                    return "UNKNOWN";
	            }
	        }
	        
	        // *** 取得可用的付款操作 *** //
	        private List<String> getAvailablePaymentActions(OrderDTO order) {
	            List<String> actions = new ArrayList<>();
	            
	            if (order.canRetryPayment()) {
	                actions.add("RETRY_PAYMENT");
	            }
	            
	            if (order.canBeCancelled() || "PAYING".equals(order.getOrderStatus())) {
	                actions.add("CANCEL_PAYMENT");
	            }
	            
	            if ("PENDING".equals(order.getOrderStatus()) || "FAILED".equals(order.getOrderStatus())) {
	                actions.add("VIEW_PAYMENT_OPTIONS");
	            }
	            
	            return actions;
	        }
	        
	        
	        // *** 建立進度步驟 *** //
	        private List<Map<String, Object>> createProgressSteps(OrderDTO order) {
	            List<Map<String, Object>> steps = new ArrayList<>();
	            
	            // 步驟 1: 訂單建立
	            Map<String, Object> step1 = new HashMap<>();
	            step1.put("name", "訂單建立");
	            step1.put("status", "completed");
	            step1.put("time", order.getOrderDatetime());
	            steps.add(step1);
	            
	            // 步驟 2: 付款處理
	            Map<String, Object> step2 = new HashMap<>();
	            step2.put("name", "付款處理");
	            if ("PENDING".equals(order.getOrderStatus())) {
	                step2.put("status", "waiting");
	            } else if ("PAYING".equals(order.getOrderStatus())) {
	                step2.put("status", "processing");
	            } else if ("FAILED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus())) {
	                step2.put("status", "failed");
	            } else {
	                step2.put("status", "completed");
	            }
	            steps.add(step2);
	            
	            // 步驟 3: 訂單處理
	            Map<String, Object> step3 = new HashMap<>();
	            step3.put("name", "訂單處理");
	            if ("PROCESSING".equals(order.getOrderStatus())) {
	                step3.put("status", "processing");
	            } else if ("SHIPPED".equals(order.getOrderStatus()) || "COMPLETED".equals(order.getOrderStatus())) {
	                step3.put("status", "completed");
	            } else {
	                step3.put("status", "waiting");
	            }
	            steps.add(step3);
	            
	            // 步驟 4: 完成
	            Map<String, Object> step4 = new HashMap<>();
	            step4.put("name", "交易完成");
	            step4.put("status", "COMPLETED".equals(order.getOrderStatus()) ? "completed" : "waiting");
	            steps.add(step4);
	            
	            return steps;
	        }
	        
	        
	        // *** 取得訂單序號資訊 *** //
	        private Map<String, Object> getOrderSerialInfo(Integer orderNo) {
	            try {
	                String sql = "SELECT oi.pro_name, psn.product_sn " +
	                            "FROM order_item oi " +
	                            "LEFT JOIN pro_serial_numbers psn ON oi.order_item_no = psn.order_item_no " +
	                            "WHERE oi.order_no = ?";
	                
	                List<Map<String, Object>> serialInfo = jdbcTemplate.queryForList(sql, orderNo);
	                
	                Map<String, Object> result = new HashMap<>();
	                result.put("serialNumbers", serialInfo);
	                result.put("hasSerials", !serialInfo.isEmpty());
	                
	                return result;
	                
	            } catch (Exception e) {
	                log.error("查詢訂單序號資訊失敗：orderNo={}", orderNo, e);
	                return new HashMap<>();
	            }
	        }
	        
	        
	        
	        // ****** 收集序號資訊並發送付款成功郵件 ****** //
	        private void sendCompletionEmailWithSerials(OrderDTO orderDetail, List<String> inStockSerials, List<OrderItemDTO> preOrderItems) {
	            try {
	                log.info("📧 準備發送付款成功郵件：orderNo={}", orderDetail.getOrderNo());
	                
	                // 🔥 不依賴傳入的參數，直接查詢每個訂單項目的序號
	                List<String> actualSerials = new ArrayList<>();
	                Map<String, String> productSerialMap = new HashMap<>();
	                
	                for (OrderItemDTO item : orderDetail.getOrderItems()) {
	                    if (item.getOrderItemNo() != null) {
	                        String serial = getSerialNumberForOrderItem(item.getOrderItemNo());
	                        if (serial != null) {
	                            actualSerials.add(serial);
	                            productSerialMap.put(item.getProductName(), serial);
	                            log.info("✅ 找到序號：商品={}, orderItemNo={}, serial={}", 
	                                    item.getProductName(), item.getOrderItemNo(), serial);
	                        } else {
	                            log.info("⚠️ 無序號：商品={}, orderItemNo={}", 
	                                    item.getProductName(), item.getOrderItemNo());
	                        }
	                    } else {
	                        log.warn("❌ orderItemNo 是 null：商品={}", item.getProductName());
	                    }
	                }
	                
	                log.info("📊 實際找到的序號數量：{}", actualSerials.size());
	                log.info("📊 商品序號對應：{}", productSerialMap);
	                
	                // 🔥 使用實際查到的序號發送郵件
	                boolean emailSent = emailService.sendPaymentSuccessEmail(orderDetail, actualSerials, preOrderItems);
	                
	                if (emailSent) {
	                    log.info("✅ 付款成功郵件發送成功：orderNo={}, 實際序號數={}個", 
	                            orderDetail.getOrderNo(), actualSerials.size());
	                } else {
	                    log.warn("❌ 付款成功郵件發送失敗：orderNo={}", orderDetail.getOrderNo());
	                }
	                
	            } catch (Exception e) {
	                log.error("💥 發送付款成功郵件時發生錯誤：orderNo={}", orderDetail.getOrderNo(), e);
	            }
	        }
	        
	        
	        
	        
	        
	        // ****** 查詢訂單項目的序號 ****** //
	        private String getSerialNumberForOrderItem(Integer orderItemNo) {
	            try {
	                String sql = "SELECT product_sn FROM pro_serial_numbers WHERE order_item_no = ?";
	                List<String> results = jdbcTemplate.queryForList(sql, String.class, orderItemNo);
	                
	                if (!results.isEmpty()) {
	                    String serialNumber = results.get(0);
	                    log.debug("查詢到序號：orderItemNo={}, serial={}", orderItemNo, serialNumber);
	                    return serialNumber;
	                } else {
	                    log.debug("未找到序號：orderItemNo={} (可能為預購商品)", orderItemNo);
	                    return null;
	                }
	                
	            } catch (Exception e) {
	                log.error("查詢序號失敗：orderItemNo={}", orderItemNo, e);
	                return null;
	            }
	        }
	        
	        
	    
}