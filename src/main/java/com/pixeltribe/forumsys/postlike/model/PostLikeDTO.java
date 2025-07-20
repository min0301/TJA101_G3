package com.pixeltribe.forumsys.postlike.model;

import com.pixeltribe.forumsys.shared.LikeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "文章喜愛")
public class PostLikeDTO {
    @Schema(description = "文章喜愛編號")
    private Integer id;

    @Schema(description = "文章喜愛更新時間")
    private Instant pLikeUpdate;

    @Schema(description = "文章喜愛狀態")
    @Enumerated(EnumType.STRING)
    private LikeStatus pLikeStatus;

    @Schema(description = "文章喜愛建立時間")
    private Instant pLikeCrdate;

    @Schema(description = "文章編號")
    private Integer postId;

    @Schema(description = "會員編號")
    private Integer memberId;

    @Schema(description = "會員名稱")
    private String memberName;



    public static PostLikeDTO convertToPostLikeDTO(PostLike postLike) {
        return PostLikeDTO.builder()
                .id(postLike.getId())
                .pLikeUpdate(postLike.getPlikeUpdate())
                .pLikeStatus(postLike.getPlikeStatus())
                .pLikeCrdate(postLike.getPlikeCrdate())
                .postId(postLike.getPostNo() != null ? postLike.getPostNo().getId() : null)
                .memberId(postLike.getMemNo() != null ? postLike.getMemNo().getId() : null)
                .memberName(postLike.getMemNo() != null ? postLike.getMemNo().getMemName() : null)
                .build();
    }
}
