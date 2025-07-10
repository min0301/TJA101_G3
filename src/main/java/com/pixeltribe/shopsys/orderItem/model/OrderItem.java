package com.pixeltribe.shopsys.orderItem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pixeltribe.shopsys.order.model.Order;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumber;
import com.pixeltribe.shopsys.product.model.Product;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "order_item")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderItem {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ITEM_NO", nullable = false)
    private Integer orderItemNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_NO", nullable = false)
    @JsonBackReference
    private Order order;   

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRO_NO", nullable = false)
    private Product proNo;

    @Column(name = "ORDER_AMOUNT", nullable = false)
    private Integer orderAmount;

    @Column(name = "PRO_PRICE", nullable = false)
    private Integer proPrice;

    @Size(max = 30)
    @Column(name = "PRO_NAME", length = 30, nullable = false)
    private String proName;

    @Size(max = 255)
    @Column(name = "PRODUCT_COMMENT", length = 255)
    private String productComment;

    @Column(name = "PRODUCT_COMMENT_CRDATE")
    private Instant productCommentCrdate;

    @Min(value = 1, message = "評分要給1顆星")
    @Max(value = 5, message = "評分最多5顆星")
    @Column(name = "PRO_STAR")
    private Integer proStar;

    @ColumnDefault("'1'")
    @Column(name = "PRO_COM_STATUS", nullable = false)
    private Character proComStatus;

    @OneToMany(mappedBy = "orderItemNo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProSerialNumber> proSerialNumbers = new LinkedHashSet<>();
    
    // 預設建構子
    public OrderItem() {
    	this.proComStatus = '1';  //預設為正常狀態
    }
    
    // 帶參數的建構子
    public OrderItem(Order order, Product proNo, Integer orderAmount) {
    	this.order = order;
    	this.proNo = proNo;
    	this.orderAmount = orderAmount;
    	this.proComStatus = '1';  // 預設為正常狀態
    	
    	// 從product 自動帶入產品資訊
    	if (proNo != null) {
    		this.proName = proNo.getProName();
    		this.proPrice = proNo.getProPrice();
    	}
    }
    
    // ========== 關聯管理方法 雙向關聯：Order <-> OrderItem ========== //
    public void setOrder(Order order) {
    	if (this.order != null) {
    		this.order.removeOrderItem(this);
    	}
    	
    	// 建立新的關聯時
    	this.order = order;
    	
    	if (order != null && !order.getOrderItems().contains(this)) {
    		order.addOrderItem(this);
    	}
    }
    
    // ========== 關聯管理方法 雙向關聯：OrderItem <-> Product ========== //
    public void setProduct(Product proNo) {
    	this.proNo = proNo;
    	
    	if (proNo != null) {
    		this.proName = proNo.getProName();
    		this.proPrice =proNo.getProPrice();
    	}
    }
    
    // 便利方法：取得訂單編號（Integer）
    public Integer getOrderNo() {
        return order != null ? order.getOrderNo() : null;
    }

}