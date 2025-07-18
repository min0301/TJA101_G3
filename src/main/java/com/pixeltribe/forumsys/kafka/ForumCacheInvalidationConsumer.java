package com.pixeltribe.forumsys.kafka;

import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

//@Component
public class ForumCacheInvalidationConsumer {

    private final CacheManager cacheManager;

    public ForumCacheInvalidationConsumer(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // 監聽我們在 service 中發送訊息的 Topic
    @KafkaListener(topics = "forum-category-update-topic", groupId = "forum-category-cache-group")
    public void handleCategoryCacheInvalidation(Integer categoryId) {
        if (categoryId == null) {
            return;
        }

        System.out.println(">>> Kafka 收到通知，準備清除分類快取，分類 ID: " + categoryId);

        // 從 CacheManager 獲取名為 "forumsByCategory" 的快取區，
        // 並根據收到的 categoryId 清除對應的快取。
        cacheManager.getCache("forumsByCategory").evict(categoryId);
    }
}