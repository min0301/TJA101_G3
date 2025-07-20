package com.pixeltribe.shopsys.product.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProductScheduler {
	
		@Autowired
	    private ProductPreorderService productPreorderService;
	    
	    //每日凌晨 00:00 更新商品狀態
	    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Taipei")
	    public void updateProductStatus() {
	    	productPreorderService.updaeProductStatus();
	    }
	    
	    //每日凌晨 02:00 清理 Redis 資料
	    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Taipei")
	    public void cleanupRedisData() {
	    	productPreorderService.cleanupRedis();
	    }
}
