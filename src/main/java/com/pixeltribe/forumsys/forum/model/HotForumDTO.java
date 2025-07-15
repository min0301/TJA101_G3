package com.pixeltribe.forumsys.forum.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "熱門討論區項目 DTO (包含個人化收藏狀態)")
public class HotForumDTO {

    @Schema(description = "討論區編號")
    private Integer id;

    @Schema(description = "討論區名稱")
    private String forName;

    @Schema(description = "討論區圖片 URL")
    private String forImgUrl;

    @Schema(description = "熱度分數")
    private Integer hotScore;

    @Schema(description = "當前使用者是否已收藏")
    private boolean isCollected;

    public HotForumDTO(Integer id, String forName, String forImgUrl, Integer hotScore) {
        this.id = id;
        this.forName = forName;
        this.forImgUrl = forImgUrl;
        this.hotScore = hotScore;
        this.isCollected = false; // 預設為未收藏
    }
}
