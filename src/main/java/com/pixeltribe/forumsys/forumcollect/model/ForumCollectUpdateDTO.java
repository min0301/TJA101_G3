package com.pixeltribe.forumsys.forumcollect.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "討論區收藏建立DTO")
public class ForumCollectUpdateDTO {

    @Schema(description = "討論區收藏建立時間")
    private Instant fcollUpdate;

    @Schema(description = "討論區編號")
    private Integer forumNo;

    @Schema(description = "會員編號")
    private Integer memberNo;


}
