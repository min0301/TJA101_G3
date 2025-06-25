package com.pixeltribe.shopsys.vo;

import com.pixeltribe.membersys.vo.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "`order`")
public class Order {
    @Id
    @Column(name = "ORDER_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEM_NO", nullable = false)
    private Member memNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPON_WALLET_NO")
    private CouponWallet couponWalletNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ORDER_DATETIME")
    private Instant orderDatetime;

    @Size(max = 20)
    @Column(name = "ORDER_STATUS", length = 20)
    private String orderStatus;

    @Column(name = "ORDER_TOTAL")
    private Integer orderTotal;

    @Column(name = "POINT_USED")
    private Integer pointUsed;

    @OneToMany(mappedBy = "orderNo")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

}