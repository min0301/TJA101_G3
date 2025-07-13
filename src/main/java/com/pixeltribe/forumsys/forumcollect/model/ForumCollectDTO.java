package com.pixeltribe.forumsys.forumcollect.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
@Data
@Builder
@Schema(description = "討論區收藏DTO")
public class ForumCollectDTO {

    @Schema(description = "討論區收藏編號")
    private Integer id;

    @Schema(description = "討論區收藏建立時間")
    private Instant fcollUpdate;

    @Schema(description = "討論區收藏狀態")
    private String collectStatus;

    @Schema(description = "討論區編號")
    private Integer forumNo;

    @Schema(description = "會員編號")
    private Integer memberNo;



    public static ForumCollectDTO convertToForumCollectDTO(ForumCollect forumCollect) {
        return ForumCollectDTO.builder()
                .id(forumCollect.getId())
                .fcollUpdate(forumCollect.getFcollUpdate())
                .forumNo(forumCollect.getForNo().getId())
                .memberNo(forumCollect.getMemNo().getId())
                .collectStatus(forumCollect.getCollectStatus().name())
                .build();
    }

}
