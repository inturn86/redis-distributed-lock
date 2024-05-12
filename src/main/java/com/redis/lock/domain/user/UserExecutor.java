package com.redis.lock.domain.user;

import com.redis.lock.domain.coupon.CouponService;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class UserExecutor implements Runnable{

	private String couponId;
	private CountDownLatch countDownLatch;
	private Consumer<String> consumer;

	public UserExecutor(String couponId, CountDownLatch countDownLatch, Consumer<String> consumer) {
		this.couponId = couponId;
		this.countDownLatch = countDownLatch;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		consumer.accept(couponId);
		countDownLatch.countDown();
	}
}
