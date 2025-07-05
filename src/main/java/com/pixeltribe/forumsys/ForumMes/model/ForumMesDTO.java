package com.pixeltribe.forumsys.ForumMes.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.membersys.member.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "文章留言 DTO")
@Builder
public class ForumMesDTO {

    @Schema(description = "留言編號")
    private Integer id;
    @Schema(description = "留言內容")
    private String mesCon;
    @Schema(description = "留言建立時間")
    private Instant mesCrdate;
    @Schema(description = "留言更新時間")
    private Instant mesUpdata;
    @Schema(description = "留言狀態")
    private Character mesStatus;
    @Schema(description = "留言讚總數")
    private Integer mesLikeLc;
    @Schema(description = "留言倒讚總數")
    private Integer mesLikeDlc;


    @Schema(description = "文章編號")
    private Integer postId;
    @Schema(description = "會員編號")
    private String memberNo;

    public ForumMesDTO convertEntityToDTO(ForumMes forumMes){
        return ForumMesDTO.builder()
                .id(forumMes.getId())
                .mesCon(forumMes.getMesCon())
                .mesCrdate(forumMes.getMesCrdate())
                .mesUpdata(forumMes.getMesUpdata())
                .mesStatus(forumMes.getMesStatus())
                .mesLikeLc(forumMes.getMesLikeLc())
                .mesLikeDlc(forumMes.getMesLikeDlc())
                .postId(forumMes.getPostNo().getId())
                .build();


    }



}
