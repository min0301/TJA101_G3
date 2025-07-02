package com.pixeltribe.forumsys.forum.model;

import io.swagger.v3.oas.annotations.media.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "討論區list DTO")
public class ForumDetailDTO {

    @Schema(description = "討論區編號", example = "1")
    private Integer id;
    @Schema(description = "討論區名稱")
    private String forName;
    @Schema(description = "討論區敘述")
    private String forDes;
    @Schema(description = "討論區圖片URL")
    private String forImgUrl;
    @Schema(description = "創建時間")
    private Instant forDate;
    @Schema(description = "更新時間")
    private Instant forUpdate;
    @Schema(description = "狀態")
    private Character forStatus;

    // 用一個 String 來接收分類名稱，而不是整個 ForumCategory 物件
    @Schema(description = "類別名稱")
    private String categoryName;
    @Schema(description = "類別編號")
    private Integer categoryId;

}
