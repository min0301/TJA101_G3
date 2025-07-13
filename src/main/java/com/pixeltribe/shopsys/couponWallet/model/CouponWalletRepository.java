package com.pixeltribe.shopsys.couponWallet.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponWalletRepository extends JpaRepository<CouponWallet, Integer> {
	
    @Query("SELECT cw FROM CouponWallet cw JOIN FETCH cw.couNo WHERE cw.memNo.id = :memNo")
    List<CouponWallet> findByMemNo_IdWithCoupon(@Param("memNo") Integer memNo);
}
