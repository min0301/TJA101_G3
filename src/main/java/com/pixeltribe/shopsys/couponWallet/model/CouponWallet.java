package com.pixeltribe.shopsys.couponWallet.model;


import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.order.model.Order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.order.model.Order;


import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "coupon_wallet")
public class CouponWallet {
    @Id
    @Column(name = "COUPON_WALLET_NO", nullable = false)
    private Integer id;

    @Column(name = "COU_NO")
    private Integer couNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "COUPON_RELEASE_TIME")
    private Instant couponReleaseTime;

    @Column(name = "COU_WALLET_USED")
    private Character couWalletUsed;

    @OneToMany(mappedBy = "couponWalletNo")
    private Set<Order> orders = new LinkedHashSet<>();

}