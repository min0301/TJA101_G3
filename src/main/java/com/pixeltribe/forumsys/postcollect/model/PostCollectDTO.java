package com.pixeltribe.forumsys.postcollect.model;

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

    public static PostCollectDTO convertToPostCollectDTO(PostCollect postCollect) {
        return PostCollectDTO.builder()
                .id(postCollect.getId())
                .pcollUpdate(postCollect.getPcollUpdate())
                .postNo(postCollect.getPostNo().getId())
                .memberNo(postCollect.getMemNo().getId())
                .postCollectStatus(postCollect.getPostCollectStatus().name())
                .build();


    }
}
