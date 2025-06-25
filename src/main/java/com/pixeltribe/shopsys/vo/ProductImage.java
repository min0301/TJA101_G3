package com.pixeltribe.shopsys.vo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_image")
public class ProductImage {
    @Id
    @Column(name = "PRO_IMG_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRO_NO", nullable = false)
    private Product proNo;

    @Column(name = "PRO_IMG_DATA")
    private byte[] proImgData;

    @Size(max = 100)
    @Column(name = "PRO_IMG_TYPE", length = 100)
    private String proImgType;

}