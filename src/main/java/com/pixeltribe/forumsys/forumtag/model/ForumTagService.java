package com.pixeltribe.forumsys.forumtag.model; // 確保這個 package 名稱正確

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 確保有這個導入

import java.util.List;
import java.util.stream.Collectors; // 確保有這個導入

@Service // 標註這是一個 Service 層元件，會被 Spring 管理
public class ForumTagService {

    private final ForumTagRepository forumTagRepository; // 【私有 final 屬性】你的 Repository

    // 【建構子注入】 Repository
    @Autowired
    public ForumTagService(ForumTagRepository forumTagRepository) {
        this.forumTagRepository = forumTagRepository;
    }

    /**
     * 獲取所有文章類別的 ForumTagDTO 列表。
     *
     * @return ForumTagDTO 的列表
     */
    @Transactional(readOnly = true) // 標註為只讀事務，提高性能
    public List<ForumTagDTO> getAllForumTags() { // `getAllForumTags` 是方法名，可變
        // 從 Repository 獲取所有 ForumTag Entity，然後使用 Stream API 轉換為 ForumTagDTO
        return forumTagRepository.findAll().stream()
                .map(ForumTagDTO::new) // 【重要】這裡假設 ForumTagDTO 有一個建構子可以接收 ForumTag Entity 作為參數
                .collect(Collectors.toList());
    }

    // 如果你有其他業務邏輯，例如根據 ID 獲取、新增、修改、刪除等，可以在這裡添加方法
}
