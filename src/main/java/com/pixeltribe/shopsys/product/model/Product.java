package com.pixeltribe.shopsys.product.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.shopsys.favoriteProduct.model.FavoriteProduct;
import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.orderItem.model.OrderItem;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumber;
import com.pixeltribe.shopsys.productImage.model.ProductImage;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @Column(name = "PRO_NO", nullable = false)
    private Integer id;

    @Size(max = 30)
    @NotNull
    @Column(name = "PRO_NAME", nullable = false, length = 30)
    private String proName;

    @NotNull
    @Column(name = "PRO_PRICE", nullable = false)
    private Integer proPrice;

    @Size(max = 10)
    @NotNull
    @Column(name = "PRO_STATUS", nullable = false, length = 10)
    private String proStatus;

    @Size(max = 30)
    @NotNull
    @Column(name = "PRO_VERSION", nullable = false, length = 30)
    private String proVersion;

    @Column(name = "PRO_DATE")
    private LocalDate proDate;

    @Column(name = "PRO_COVER")
    private byte[] proCover;

    @Size(max = 500)
    @Column(name = "PRO_DETAILS", length = 500)
    private String proDetails;

    @Size(max = 255)
    @Column(name = "PRO_INCLUDE")
    private String proInclude;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MALL_TAG_NO", nullable = false)
    private MallTag mallTagNo;

    @NotNull
    @Column(name = "PRO_ISMARKET", nullable = false)
    private Character proIsmarket;

    @Column(name = "PRO_TOTAL_COMMIT")
    private Integer proTotalCommit;

    @Column(name = "PRO_TOTAL_STAR", precision = 1, scale = 1)
    private BigDecimal proTotalStar;

    @OneToMany(mappedBy = "proNo")
    private Set<FavoriteProduct> favoriteProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "proNo")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "proNo")
    private Set<ProSerialNumber> proSerialNumbers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "proNo")
    private Set<ProductImage> productImages = new LinkedHashSet<>();

}