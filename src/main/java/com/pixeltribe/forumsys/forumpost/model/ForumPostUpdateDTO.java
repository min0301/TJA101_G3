package com.pixeltribe.forumsys.forumpost.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data // Lombok 註解，自動生成 Getter, Setter, equals, hashCode, toString
public class ForumPostUpdateDTO {

    @NotNull(message = "討論區編號: 請選擇一個討論區編號")
    private Integer forNoId;

    @NotNull(message = "類別編號: 請選擇您的類別")
    private Integer ftagNoId;

    @NotEmpty(message = "文章標題: 請勿空白")
    @Size(max = 50, message = "文章標題長度不能超過50")
    private String postTitle;

    @NotEmpty(message = "文章內容: 請勿空白(最少十個字) ")
    @Size(min = 10, max = 5000, message = "文章內容長度必需在10到5000之間")
    private String postCon;

    private Character postPin;
    private Character postStatus;

    // 不直接放在 DTO 中處理，而是作為 @RequestPart 處理
    // private MultipartFile postCoverImageFile; // 此欄位會作為 @RequestPart 單獨處理
}