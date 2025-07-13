package com.pixeltribe.forumsys.message.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixeltribe.forumsys.message.model.ForumMesDTO;
import com.pixeltribe.forumsys.message.model.ForumMesService;
import com.pixeltribe.forumsys.message.model.ForumMesUpdateDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ForumMesProcessor {

    private final RedisTemplate<String, String> redisTemplate;
    private final ForumMesService forumMesSvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MESSAGE_QUEUE_KEY = "forum:message:queue";

    public ForumMesProcessor(RedisTemplate<String, String> redisTemplate, ForumMesService forumMesSvc) {
        this.redisTemplate = redisTemplate;
        this.forumMesSvc = forumMesSvc;
    }

    @Scheduled(fixedDelay = 200)
    public void processMessageQueue() {
        // 使用 left  Pop 的阻塞版本，如果佇列為空，它會等待 5 秒
        String taskJson = redisTemplate.opsForList().leftPop(MESSAGE_QUEUE_KEY, Duration.ofSeconds(5));

        if (taskJson == null) {
            return; // 佇列為空，結束本次執行
        }

        try {
            // 將 JSON 反序列化為任務物件
            ForumMesUpdateDTO task = objectMapper.readValue(taskJson, ForumMesUpdateDTO.class);
            ForumMesDTO createdMessage = forumMesSvc.addMessageFromTask(task);
            System.out.println("成功處理一則留言任務：" + createdMessage);
        } catch (Exception e) {
            System.err.println("處理留言任務失敗: " + taskJson + " | 錯誤: " + e.getMessage());
        }
    }


}
