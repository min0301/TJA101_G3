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
    private String postCoverImageUrl; // 新增圖片 URL 欄位，取代 byte[]
    private Integer forumId;
    private String forumName;
    private Integer memberId;
    private String memberName;
    private String memberNickName;
    private Integer forumTagId;
    private String forumTagName;

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
        // 假設 ForumPost Entity 中存在 getPostCoverImageUrl() 方法來獲取圖片 URL
        // 如果您原本是 byte[]，需要將其轉換為 URL 儲存後才能使用此欄位
        this.postCoverImageUrl = forumPost.getPostImageUrl(); // 請根據實際圖片儲存方式調整

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
//             假設 ForumTag 有 getTagName() 方法
//             this.forumTagName = forumPost.getFtagNo().getTagName();
//             請根據 ForumTag 實際的名稱欄位調整
        }
    }
}