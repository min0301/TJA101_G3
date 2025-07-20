package com.pixeltribe.forumsys.forum.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumUpdateDTO {

    @Schema(description = "討論區名稱")
    private String forName;

    @Schema(description = "討論區描述")
    private String forDes;

    @Schema(description = "狀態")
    private Character forStatus;

    @Schema(description = "分類編號")
    private Integer categoryId;


}
