package com.pixeltribe.forumsys.postcollect.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost; // 確保有這個 import
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "文章收藏DTO")
public class PostCollectDTO {

    @Schema(description = "文章收藏編號")
    private Integer id;

    @Schema(description = "文章收藏建立時間")
    private Instant pcollUpdate;

    @Schema(description = "文章收藏狀態")
    private String postCollectStatus;

    @Schema(description = "文章編號")
    private Integer postNo;

    @Schema(description = "會員編號")
    private Integer memberNo;

    // 新增文章相關欄位
    @Schema(description = "文章標題")
    private String postTitle;

    @Schema(description = "文章內容摘要")
    private String postCon;

    @Schema(description = "文章圖片URL")
    private String postImageUrl;

    @Schema(description = "文章作者暱稱")
    private String memberNickName;

    @Schema(description = "留言數")
    private Integer mesNumbers;

    @Schema(description = "喜歡數")
    private Integer postLikeCount;

    @Schema(description = "不喜歡數")
    private Integer postLikeDlc;


    /**
     * 將 PostCollect 轉換為 PostCollectDTO
     *
     * @param postCollect PostCollect 實體
     * @return PostCollectDTO
     */
    public static PostCollectDTO convertToPostCollectDTO(PostCollect postCollect) {
        ForumPost post = postCollect.getPostNo(); // 取得關聯的 ForumPost
        return PostCollectDTO.builder()
                .id(postCollect.getId())
                .pcollUpdate(postCollect.getPcollUpdate())
                .postNo(post.getId())
                .memberNo(postCollect.getMemNo().getId())
                .postCollectStatus(postCollect.getPostCollectStatus().name())
                // 填充文章相關欄位
                .postTitle(post.getPostTitle())
                .postCon(post.getPostCon())
                .postImageUrl(post.getPostImageUrl())
                .memberNickName(post.getMemNo().getMemNickName()) // 假設 ForumPost 有 getMember().getMemNickName()
                .mesNumbers(post.getMesNumbers()) // 假設 ForumPost 有 getMesNumbers()
                .postLikeCount(post.getPostLikeCount()) // 假設 ForumPost 有 getPostLikeCount()
                .postLikeDlc(post.getPostLikeDlc()) // 假設 ForumPost 有 getPostLikeDlc()
                .build();
    }
}