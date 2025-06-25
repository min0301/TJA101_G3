package com.pixeltribe.shopsys.vo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @Column(name = "ORDER_ITEM_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_NO")
    private Order orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRO_NO")
    private Product proNo;

    @Column(name = "ORDER_AMOUNT")
    private Integer orderAmount;

    @Column(name = "PRO_PRICE")
    private Integer proPrice;

    @Size(max = 30)
    @Column(name = "PRO_NAME", length = 30)
    private String proName;

    @Size(max = 255)
    @Column(name = "PRODUCT_COMMENT")
    private String productComment;

    @Column(name = "PRODUCT_COMMENT_CRDATE")
    private Instant productCommentCrdate;

    @Column(name = "PRO_STAR")
    private Integer proStar;

    @ColumnDefault("'0'")
    @Column(name = "PRO_COM_STATUS")
    private Character proComStatus;

    @OneToMany(mappedBy = "orderItemNo")
    private Set<ProSerialNumber> proSerialNumbers = new LinkedHashSet<>();

}