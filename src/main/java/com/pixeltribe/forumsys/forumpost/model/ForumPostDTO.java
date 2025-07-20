package com.pixeltribe.forumsys.forumpost.model;

import lombok.Data;

import java.time.Instant;

@Data // Lombok 註解，自動生成 Getter, Setter, equals, hashCode, toString
public class ForumPostDTO {

    private Integer id;
    private String postTitle;
    private String postCon;
    private Instant postCrdate;
    private Instant postUpdate;
    private Character postPin;
    private Character postStatus;
    private Integer mesNumbers;
    private Integer postLikeCount;
    private Integer postLikeDlc;
    private String postImageUrl; // 新增圖片 URL 欄位，取代 byte[]
    private Integer forumId;
    private String forumName;
    private Integer memberId;
    private String memberName;
    private String memberNickName;
    private Integer forumTagId;
    private String forumTagName;
    private boolean isCollected; // 新增：表示文章是否被當前使用者收藏

    // 建構子：將 ForumPost Entity 轉換為 ForumPostDTO
    public ForumPostDTO(ForumPost forumPost) {
        this.id = forumPost.getId();
        this.postTitle = forumPost.getPostTitle();
        this.postCon = forumPost.getPostCon();
        this.postCrdate = forumPost.getPostCrdate();
        this.postUpdate = forumPost.getPostUpdate();
        this.postPin = forumPost.getPostPin();
        this.postStatus = forumPost.getPostStatus();
        this.mesNumbers = forumPost.getMesNumbers();
        this.postLikeCount = forumPost.getPostLikeCount();
        this.postLikeDlc = forumPost.getPostLikeDlc();
        this.postImageUrl = forumPost.getPostImageUrl();


        // 處理關聯實體，避免 N+1 問題及 LazyInitializationException
        if (forumPost.getForNo() != null) {
            this.forumId = forumPost.getForNo().getId();
            this.forumName = forumPost.getForNo().getForName();
        }
        if (forumPost.getMemNo() != null) {
            this.memberId = forumPost.getMemNo().getId();
            this.memberName = forumPost.getMemNo().getMemName();
            this.memberNickName = forumPost.getMemNo().getMemNickName();
        }
        if (forumPost.getFtagNo() != null) {
            this.forumTagId = forumPost.getFtagNo().getId();
            // 假設 ForumTag 有 getTagName() 方法
            // this.forumTagName = forumPost.getFtagNo().getTagName();
            // 請根據 ForumTag 實際的名稱欄位調整
        }
        // isCollected 欄位將在 Service 層設定，因為這需要會員登入資訊
    }
}
