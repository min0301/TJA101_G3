package com.pixeltribe.forumsys.postcollect.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "文章收藏建立DTO")
public class PostCollectUpdateDTO {

    @Schema(description = "文章收藏建立時間")
    private Instant pcollUpdate;

    @Schema(description = "文章編號")
    private Integer postNo;

    @Schema(description = "會員編號")
    private Integer memberNo;
}
