package com.pixeltribe.shopsys.coupon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "coupon")
public class Coupon {
    @Id
    @Column(name = "COU_NO", nullable = false)
    private Integer couNo;

    @Size(max = 25)
    @Column(name = "COU_NAME", length = 25)
    private String couName;

    @Column(name = "COU_DISCOUNT")
    private Integer couDiscount;

    @ColumnDefault("'0'")
    @Column(name = "COU_STATUS")
    private Character couStatus;

    @Column(name = "COU_USE_START")
    private Instant couUseStart;

    @Column(name = "COU_USE_END")
    private Instant couUseEnd;

    @Column(name = "COU_TOTAL")
    private Integer couTotal;

    @ColumnDefault("0")
    @Column(name = "COU_USED")
    private Integer couUsed;

    @Size(max = 30)
    @Column(name = "COU_CODE", length = 30)
    private String couCode;

    @Column(name = "COU_DTR_START")
    private Instant couDtrStart;

    @Column(name = "COU_DTR_END")
    private Instant couDtrEnd;

    @ColumnDefault("0")
    @Column(name = "COU_RECEIVE_NUM")
    private Integer couReceiveNum;

}