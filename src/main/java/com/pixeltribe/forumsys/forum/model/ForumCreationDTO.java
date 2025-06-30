package com.pixeltribe.forumsys.forum.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumCreationDTO {

    @Size(max = 30)
    @NotEmpty(message="討論區名稱: 請勿空白")
    private String forName;

    @Size(max = 255)
    @NotEmpty(message="討論區描述: 請勿空白")
    private String forDes;

    // 前端只傳送分類ID
    @NotNull(message="必須選擇一個分類")
    private Integer categoryId;


    private Character forStatus;
}
