package com.pixeltribe.forumsys.postlike.model;

import com.pixeltribe.forumsys.shared.LikeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
@Schema(description = "文章喜愛")
public class PostLikeUpdateDTO {

    @Schema(description = "會員編號")
    private Integer memberId;

    @Schema(description = "喜愛狀態")
    @Enumerated(EnumType.STRING)
    private LikeStatus pLikeStatus;

}
