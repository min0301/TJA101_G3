package com.pixeltribe.forumsys.forumpost.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost; // 確保導入 ForumPost Entity
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant; // 確保導入 Instant

import com.fasterxml.jackson.annotation.JsonProperty; // 確保導入 JsonProperty

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章區list DTO") // 保持 OpenAPI 註解
@ToString
public class ForumPostDTO {

    @Schema(description = "文章編號", example = "1")
    private Integer id;
    @Schema(description = "文章名稱") // 對應 postTitle
    private String postTitle;
    @Schema(description = "文章內容") // 對應 postCon
    private String postCon;
//    @Schema(description = "文章圖片URL") // 從 ForumPost 的 postCoverImage 獲取 URL
//    private String postCoverImageUrl;

    @Schema(description = "建立時間") // 對應 postCrdate
    private Instant postCrdate;
    @Schema(description = "更新時間") // 對應 postUpdate
    private Instant postUpdate;
    @Schema(description = "置頂狀態") // 對應 postPin
    private Character postPin;
    @Schema(description = "文章狀態") // 對應 postStatus
    private Character postStatus;
    @Schema(description = "留言數") // 對應 mesNumbers
    private Integer mesNumbers;
    @Schema(description = "讚數") // 對應 postLikeCount
    private Integer postLikeCount;
    @Schema(description = "倒讚數") // 對應 postLikeDlc
    private Integer postLikeDlc;

    // 關聯物件的名稱和 ID
    @Schema(description = "討論區名稱")
    private String forumName;
    @Schema(description = "討論區編號")
    private Integer forumNo; // 對應 Forum.id

    @Schema(description = "發文會員名稱")
    private String memberName; // 從 Member Entity 獲取名稱
    @Schema(description = "發文會員編號")
    private Integer memNo; // 對應 Member.id


    // 這個建構子用於將 ForumPost Entity 轉換為 ForumPostDTO
    public ForumPostDTO(ForumPost post) {
        this.id = post.getId();
        this.postTitle = post.getPostTitle();
        this.postCon = post.getPostCon();

//        // 圖片 URL 邏輯，確保與 Controller 中的一致
//        if (post.getPostCoverImage() != null && post.getId() != null) {
//            this.postCoverImageUrl = "/api/forumpost/image/" + post.getId();
//        } else {
//            this.postCoverImageUrl = "/static/images/common_default_cover.png"; // 前端靜態資源預設圖片
//        }

        this.postCrdate = post.getPostCrdate();
        this.postUpdate = post.getPostUpdate();
        this.postPin = post.getPostPin();
        this.postStatus = post.getPostStatus();
        this.mesNumbers = post.getMesNumbers();
        this.postLikeCount = post.getPostLikeCount();
        this.postLikeDlc = post.getPostLikeDlc();

        // 關聯名稱和 ID 獲取 (調用 ForumPost Entity 中的 @JsonProperty Getter)
        this.forumName = post.getForumName(); // 這裡調用 ForumPost 的 getForumName() 方法
        this.forumNo = post.getForNo() != null ? post.getForNo().getId() : null; // 從 ForumPost 的 Forum 物件獲取 ID

        this.memberName = post.getMemberName(); // 這裡調用 ForumPost 的 getMemberName() 方法
        this.memNo = post.getMemNo() != null ? post.getMemNo().getId() : null; // 從 ForumPost 的 Member 物件獲取 ID
    }

}
