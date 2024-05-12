package com.redis.lock.domain.coupon;


import lombok.Builder;

@Builder
public record Coupon(
	String couponId,
	Integer couponCount
) {
	public static Coupon createCoupon(String couponId, Integer couponCount) {
		return Coupon.builder()
				.couponId(couponId)
				.couponCount(couponCount)
				.build();
	}
}
