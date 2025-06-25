package com.pixeltribe.shopsys.vo;

import com.pixeltribe.membersys.vo.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "favorite_product")
public class FavoriteProduct {
    @Id
    @Column(name = "FAV_PRO_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEM_NO", nullable = false)
    private Member memNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRO_NO", nullable = false)
    private Product proNo;

}