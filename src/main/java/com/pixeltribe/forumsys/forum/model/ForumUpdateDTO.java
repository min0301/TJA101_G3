package com.pixeltribe.forumsys.forum.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumUpdateDTO {

    @Size(max = 30)
    @NotEmpty(message = "討論區名稱: 請勿空白")
    @Schema(description = "討論區名稱")
    private String forName;

    @Size(max = 255)
    @NotEmpty(message = "討論區描述: 請勿空白")
    @Schema(description = "討論區描述")
    private String forDes;

    @Schema(description = "狀態")
    private Character forStatus;

    @NotNull(message = "必須選擇一個分類")
    @Schema(description = "分類編號")
    private Integer categoryId;


}
