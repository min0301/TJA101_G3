package com.pixeltribe.shopsys.couponWallet.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixeltribe.shopsys.coupon.model.Coupon;

@Service
public class CouponWalletService {
	
	@Autowired
    private CouponWalletRepository couponWalletRepository;

    public List<Coupon> getCouponsByMemNo(Integer memNo) {
        List<CouponWallet> wallets = couponWalletRepository.findByMemNo_IdWithCoupon(memNo);

        if (wallets.isEmpty()) {
            return List.of();
        }

        // 從每個wallet取得coupon物件，並收集成list
        return wallets.stream()
                      .map(CouponWallet::getCouNo)
                      .filter(Objects::nonNull)
                      .collect(Collectors.toList());
    }
}
