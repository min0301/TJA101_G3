package com.pixeltribe.shopsys.proSerialNumber.model;

import com.pixeltribe.shopsys.orderItem.model.OrderItem;
import com.pixeltribe.shopsys.product.model.Product;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pro_serial_numbers")
public class ProSerialNumber {
    @Id
    @Column(name = "PRODUCT_SN_NO", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 100)
    @Column(name = "PRODUCT_SN", length = 100)
    private String productSn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ITEM_NO")
    private OrderItem orderItemNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRO_NO")
    private Product proNo;

}