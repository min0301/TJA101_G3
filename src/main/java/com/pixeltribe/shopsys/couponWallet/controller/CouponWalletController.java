package com.pixeltribe.shopsys.couponWallet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.coupon.model.Coupon;
import com.pixeltribe.shopsys.couponWallet.model.CouponWalletService;

@RestController
@RequestMapping("/api/coupon")
public class CouponWalletController {
	
    @Autowired
    private CouponWalletService couponWalletService;

    @GetMapping("/member/{id}")
    public List<Coupon> getCouponsByMemberId(@PathVariable Integer id) {
        return couponWalletService.getCouponsByMemNo(id);
    }
	
}
