package com.pixeltribe.shopsys.order.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.shopsys.couponWallet.model.CouponWallet;
import com.pixeltribe.shopsys.orderItem.model.OrderItem;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "`order`")
public class Order {
	
	// 要使用MySQL觸發器自動生成的格式，設定格式：YYYYMM0001；所以不用@GeneratedValue
    @Id
    @Column(name = "ORDER_NO", nullable = false)
    private Integer orderNo;   

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEM_NO", nullable = false)
    @JsonIgnore
    private Member memNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPON_WALLET_NO")
    @JsonIgnore
    private CouponWallet couponWalletNo;

    //要使用MySQL的DEFAULT CURRENT_TIMESTAMP；所以不用@ColumnDefault
    @Column(name = "ORDER_DATETIME", nullable = false, updatable = false, insertable = false)
    private Instant orderDatetime;

    @NotNull
    @Size(max = 10)
    @Column(name = "ORDER_STATUS", length = 10, nullable = false)
    private String orderStatus;

    @NotNull
    @Min(value = 0)
    @Column(name = "ORDER_TOTAL", nullable = false)
    private Integer orderTotal;

    // ====== 此功能暫時不開放 ======//
//    @Column(name = "POINT_USED")
//    private Integer pointUsed;

    
    // ====== 關聯映射 ====== //
    /**
     * 與訂單項目的一對多關聯
     * mappedBy = "order" 對應 OrderItem 中的 order 屬性
     * cascade = ALL: 保存/刪除Order時連帶處理OrderItem
     * fetch = LAZY: 需要時才載入OrderItem
     * orphanRemoval = true: 從集合移除時自動刪除OrderItem
     */
    @OneToMany(mappedBy = "orderNo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> orderItems = new ArrayList<>();
    
    
    //建構子
    
    public Order() {
    	this.orderStatus = "PENDING";  //預設狀態
    	this.orderTotal = 0;           //先預設為0
    	this.orderItems = new ArrayList<>();
    }
    
    
    public Order(Member member) {
    	this();
    	this.memNo = member;
    }
    
    public Order(Member member, String orderStatus) {
    	this(member);  //先設定為預設值
    	if (orderStatus != null && !orderStatus.trim().isEmpty()) {
    		this.orderStatus = orderStatus;
    	}
    }
    
    
   // ========== 關聯管理方法 雙向關聯：Order <-> OrderItem ========== //
   // ***** 處理新增 ***** //
    public void addOrderItem(OrderItem orderItem) {
    	if (orderItems == null) {
    		orderItems = new ArrayList<>();
    	}
    	orderItems.add(orderItem);   //加入Order的集合
//    	orderItem.setOrder(this);   //設定OrderItem的反向關聯 <<orderItem.java完成後才會打開>>
    }
   // ***** 處理移除 ***** // 
    public void removeOrderItem(OrderItem orderItem) {
    	if (orderItem != null) {
    		orderItems.remove(orderItem);          // 從Order的集合移除
//    		orderItem.setOrderAmount(null);        // 清除OrderItem的反向關聯 <<orderItem.java完成後才會打開>>
    	}
    }
    
    
    
    
}

