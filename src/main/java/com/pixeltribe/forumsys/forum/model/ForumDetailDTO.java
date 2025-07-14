package com.pixeltribe.forumsys.forum.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Schema(description = "討論區list DTO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Schema(description = "熱度分數 (例如：近7日留言數)", example = "120")
    private Long hotScore;

    @Schema(description = "最後留言時間")
    private Instant lastMessageTime;

    @Schema(description = "當前用戶是否已追蹤")
    private boolean isCollected;

    public static ForumDetailDTO convertToForumDetailDTO(Forum forum) {
        return ForumDetailDTO.builder()
                .id(forum.getId())
                .forName(forum.getForName())
                .forDes(forum.getForDes())
                .forImgUrl(forum.getForImgUrl())
                .forDate(forum.getForDate())
                .forUpdate(forum.getForUpdate())
                .forStatus(forum.getForStatus())
                .categoryName(forum.getCatNo() == null ? null : forum.getCatNo().getCatName())
                .categoryId(forum.getCatNo() == null ? null : forum.getCatNo().getId())
                .build();
    }

}
