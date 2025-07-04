package com.pixeltribe.forumsys.forumpost.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // 確保導入 Collectors

import com.pixeltribe.forumsys.forum.model.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.pixeltribe.forumsys.forumpost.model.ForumPostDTO; // **確保導入**

// **移除不必要的導入：FutureOrPresentValidatorForDate, Forum, ForumRepository, ForumTag, ForumTagRepository**


@Service("forumPostService") // 建議保持明確的 Bean 名稱
public class ForumPostService {

    private final ForumPostRepository forumPostRepository; // 使用 final 和建構子注入
    //nick new 假設你還有一個 Forum 服務來獲取 Forum 詳細信息，這裡簡化處理
    @Autowired
    private ForumService forumService;

    // **建構子注入 (推薦)**
    @Autowired
    public ForumPostService(ForumPostRepository forumPostRepository) {
        this.forumPostRepository = forumPostRepository;
    }


    // --- CRUD 操作 (返回 Entity) ---
    @Transactional
    public ForumPost add(ForumPost forumPost) {
        return forumPostRepository.save(forumPost);
    }

    @Transactional
    public ForumPost update(ForumPost forumPost) {
        return forumPostRepository.save(forumPost);
    }

    @Transactional
    public void delete(Integer forumPostId) { // delete 方法通常接收 ID
        forumPostRepository.deleteById(forumPostId);
    }

    @Transactional(readOnly = true)
    public ForumPost getOneForumpost(Integer forumPostId) { // 返回 Entity
        Optional<ForumPost> forumPost = forumPostRepository.findById(forumPostId);
        return forumPost.orElse(null);
    }

    // --- 查詢操作 (返回 DTOs 或計數) ---

    // 獲取所有文章列表 (返回 DTO 列表)
    @Transactional(readOnly = true)
    public List<ForumPostDTO> getAllForumPost() {
        // 呼叫 Repository 的 JOIN FETCH 方法，確保 Forum 和 Member 數據被加載
        List<ForumPost> posts = forumPostRepository.findAllWithForumAndMember();
        // 將 Entity 列表轉換為 DTO 列表
        return posts.stream()
                .map(ForumPostDTO::new) // 呼叫 ForumPostDTO 的建構子進行轉換
                .collect(Collectors.toList());
    }

    // 計算特定討論區的文章數量
    @Transactional(readOnly = true)
    public long countPostsByForumId(Integer forNo) {
        return forumPostRepository.countByForNo_Id(forNo); // 注意 Repository 方法名中的 forNo_Id
    }

    // 獲取特定討論區的文章列表 (返回 DTO 列表)
    @Transactional(readOnly = true)
    public List<ForumPostDTO> getPostsByForumId(Integer forumId) {
        List<ForumPost> posts = forumPostRepository.findByForNo_Id(forumId); // 這個方法也做了 JOIN FETCH
        return posts.stream()
                .map(ForumPostDTO::new)
                .collect(Collectors.toList());
    }

    // 根據文章 ID 和討論區 ID 查詢單篇文章 (返回 Optional<ForumPostDTO>)
    @Transactional(readOnly = true)
    public Optional<ForumPostDTO> getPostByIdAndForumId(Integer postId, Integer forumId) {
        return forumPostRepository.findByIdAndForNoId(postId, forumId) // 注意 Repository 方法名中的 forNo_Id
                .map(ForumPostDTO::new);
    }
    //nick new 假設你還有一個 Forum 服務來獲取 Forum 詳細信息，這裡簡化處理
    public ForumService getForumService() {
        return forumService;
    }

    public void setForumService(ForumService forumService) {
        this.forumService = forumService;
    }
}
