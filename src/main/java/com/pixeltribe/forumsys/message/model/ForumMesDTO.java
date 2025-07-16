package com.pixeltribe.forumsys.message.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "文章留言")
public class ForumMesDTO {
    @Schema(description = "文章留言編號")
    private Integer id;
    @Schema(description = "留言內容")
    private String mesCon;
    @Schema(description = "留言建立時間")
    private Instant mesCrdate;
    @Schema(description = "留言更新時間")
    private Instant mesUpdata;
    @Schema(description = "留言狀態")
    private Character mesStatus;
    @Schema(description = "留言讚數")
    private Integer mesLikeLc;
    @Schema(description = "留言倒讚數")
    private Integer mesLikeDlc;

    @Schema(description = "文章編號")
    private Integer postId;
    @Schema(description = "會員編號")
    private Integer memberId;
    @Schema(description = "會員名稱")
    private String memberName;
    @Schema(description = "會員暱稱")
    private String memberNickName;

    public static ForumMesDTO convertToForumMesDTO(ForumMes forumMes) {
        return ForumMesDTO.builder()
                .id(forumMes.getId())
                .mesCon(forumMes.getMesCon())
                .mesCrdate(forumMes.getMesCrdate())
                .mesUpdata(forumMes.getMesUpdata())
                .mesStatus(forumMes.getMesStatus())
                .mesLikeLc(forumMes.getMesLikeLc())
                .mesLikeDlc(forumMes.getMesLikeDlc())
                .postId(forumMes.getPostNo().getId() == null ? null : forumMes.getPostNo().getId())
                .memberId(forumMes.getMemNo().getId() == null ? null : forumMes.getMemNo().getId())
                .memberName(forumMes.getMemNo().getMemName() == null ? null : forumMes.getMemNo().getMemName())
                .memberNickName(forumMes.getMemNo().getMemNickName() == null ? null : forumMes.getMemNo().getMemNickName())
                .build();


    }

}
