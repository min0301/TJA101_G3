package com.pixeltribe.forumsys.forumtag.model; // 確保這個 package 名稱正確

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema; // 如果你有用到 Swagger/OpenAPI

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章類別 DTO") // 保持 OpenAPI 註解
public class ForumTagDTO {

    @Schema(description = "類別編號", example = "1")
    private Integer id;

    @Schema(description = "類別名稱", example = "Java教學")
    private String ftagName;

    // 【重要】這個建構子用於將 ForumTag Entity 轉換為 ForumTagDTO
    public ForumTagDTO(ForumTag forumTag) { // 確保這裡接收的是 ForumTag Entity
        this.id = forumTag.getId();        // 假設 ForumTag 的 ID 欄位是 `id`
        this.ftagName = forumTag.getFtagName(); // 假設 ForumTag 有 `getFtagName()` 方法
    }
}