package com.pixeltribe.forumsys.messagelike.model;

import com.pixeltribe.forumsys.shared.LikeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "討論區留言喜愛")
public class ForumMesLikeUpdateDTO {


    @Schema(description = "會員編號")
    private Integer memberId;

    @Schema(description = "喜愛狀態")
    @Enumerated(EnumType.STRING)
    private LikeStatus fmlikeStatus;

}
