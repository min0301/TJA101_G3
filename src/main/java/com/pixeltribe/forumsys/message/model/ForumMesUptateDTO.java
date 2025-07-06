package com.pixeltribe.forumsys.message.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ForumMesUptateDTO {
    @Schema(description = "文章編號")
    private Integer postId;

    @Schema(description = "ˊ會員編號")
    private Integer memId;

    @NotEmpty(message="留言名稱: 請勿空白")
    @Schema(description = "文章留言")
    private String mesCon;
}
