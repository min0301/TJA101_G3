package com.pixeltribe.shopsys.order.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {
	
	@Autowired
	private OrderService orderService;
	
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
    
    
    
}