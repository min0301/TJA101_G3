package com.pixeltribe.forumsys.messagelike.model;


import com.pixeltribe.forumsys.shared.LikeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "討論區留言喜愛")
public class ForumMesLikeDTO {
    @Schema(description = "留言喜愛編號")
    private Integer id;
    @Schema(description = "留言喜愛更新時間")
    private Instant fmlikeUpdate;

    @Schema(description = "留言喜愛狀態")
    @Enumerated(EnumType.STRING)
    private LikeStatus fmlikeStatus;

    @Schema(description = "留言喜愛建立時間")
    private Instant fmlikeCrdate;

    @Schema(description = "留言編號")
    private Integer messageId;

    @Schema(description = "會員編號")
    private Integer memberId;
    @Schema(description = "會員名稱")
    private String memberName;


    public static ForumMesLikeDTO convertToForumMesLikeDTO(ForumMesLike forumMesLike) {
        return ForumMesLikeDTO.builder()
                .id(forumMesLike.getId())
                .fmlikeUpdate(forumMesLike.getFmlikeUpdate())
                .fmlikeStatus(forumMesLike.getFmlikeStatus())
                .fmlikeCrdate(forumMesLike.getFmlikeCrdate())
                .messageId(forumMesLike.getMesNo().getId() == null ? null : forumMesLike.getMesNo().getId())
                .memberId(forumMesLike.getMesNo().getId() == null ? null : forumMesLike.getMesNo().getId())
                .memberName(forumMesLike.getMemNo().getMemName() == null ? null : forumMesLike.getMemNo().getMemName())
                .build();
    }


}
