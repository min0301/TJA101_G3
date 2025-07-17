package com.pixeltribe.forumsys.forumtag.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map; // 新增導入
import java.util.HashMap; // 新增導入

@Service
public class ForumTagService {

    private final ForumTagRepository forumTagRepository;

    // 【模擬資料】這個 Map 模擬了每個 ForumTag ID 對應的「預設圖片 URL」。
    // 這些 URL 應該是你的 Spring Boot 靜態資源路徑，例如：
    // src/main/resources/static/images/forumposttag_img/01.jpg
    private static final Map<Integer, String> DEFAULT_TAG_POST_IMAGES = new HashMap<>();

    static {
        // 這些 URL 應與你在 forum_post.POSTIMAGE_URL 中使用的路徑一致
        // 請根據你的實際 ForumTag ID 和圖片路徑來配置這些值
        DEFAULT_TAG_POST_IMAGES.put(1, "http://localhost:8080/images/forumposttag_img/01.jpg"); // 趣味
        DEFAULT_TAG_POST_IMAGES.put(2, "http://localhost:8080/images/forumposttag_img/02.jpg"); // 問題
        DEFAULT_TAG_POST_IMAGES.put(3, "http://localhost:8080/images/forumposttag_img/03.jpg"); // 情報
        DEFAULT_TAG_POST_IMAGES.put(4, "http://localhost:8080/images/forumposttag_img/04.jpg"); // 心得
        DEFAULT_TAG_POST_IMAGES.put(5, "http://localhost:8080/images/forumposttag_img/05.jpg"); // 討論
        DEFAULT_TAG_POST_IMAGES.put(6, "http://localhost:8080/images/forumposttag_img/06.jpg"); // 攻略
        DEFAULT_TAG_POST_IMAGES.put(7, "http://localhost:8080/images/forumposttag_img/07.jpg"); // 密技
        DEFAULT_TAG_POST_IMAGES.put(8, "http://localhost:8080/images/forumposttag_img/08.jpg"); // 閒聊
        DEFAULT_TAG_POST_IMAGES.put(9, "http://localhost:8080/images/forumposttag_img/09.jpg"); // 其他 (假設有這張圖)
        DEFAULT_TAG_POST_IMAGES.put(10, "http://localhost:8080/images/forumposttag_img/10.jpg"); // 招募
        DEFAULT_TAG_POST_IMAGES.put(11, "http://localhost:8080/images/forumposttag_img/11.jpg"); // 官方訊息
        // 確保你的圖片檔案存在於 src/main/resources/static/images/forumposttag_img/ 下
    }

    @Autowired
    public ForumTagService(ForumTagRepository forumTagRepository) {
        this.forumTagRepository = forumTagRepository;
    }

    @Transactional(readOnly = true)
    public List<ForumTagDTO> getAllForumTags() {
        return forumTagRepository.findAll().stream()
                .map(ForumTagDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * @description 根據文章類別 ID 獲取其「預設文章封面圖片」的 URL。
     * 這個方法從 Service 層的靜態 Map 中獲取，而不是從 ForumTag Entity 讀取。
     *
     * @param ftagNoId 文章類別 ID
     * @return 預設文章封面圖片的 URL 字串，如果沒有對應的圖片則返回 null。
     */
    @Transactional(readOnly = true)
    public String getDefaultImageUrl(Integer ftagNoId) { // `ftagNoId` 參數名可變
        return DEFAULT_TAG_POST_IMAGES.get(ftagNoId); // 【不可變】直接從模擬 Map 獲取
    }

    // 您也可以保留 getForumTagById 方法，如果前端也需要獲取完整的 ForumTagDTO
    @Transactional(readOnly = true)
    public ForumTagDTO getForumTagById(Integer ftagNoId) {
        return forumTagRepository.findById(ftagNoId)
                .map(ForumTagDTO::new)
                .orElse(null);
    }
}