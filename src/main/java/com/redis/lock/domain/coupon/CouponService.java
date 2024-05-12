package com.redis.lock.domain.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

	private final RedissonClient redissonClient;

	private final ConcurrentHashMap<String, Integer> couponData = new ConcurrentHashMap<>();

	//쿠폰 데이터를 set
	public Coupon setCoupon(final String couponId, final Integer couponCount) {
		Coupon coupon = Coupon.createCoupon(couponId, couponCount);
		couponData.put(coupon.couponId(), couponCount);
		return coupon;
	}

	public int getRemainCountCount(final String couponId) {
		return couponData.get(couponId);
	}

	//generateCoupon이 될때마다 해당 쿠폰의 수에서 감소
	public void generateCoupon(final String couponId) {
		int amount = couponData.getOrDefault(couponId, 0);
		if(amount <= 0) {
			log.error("해당 쿠폰은 더이상 발급할 수 없습니다. couponId = %s, amount = %d".formatted(couponId, amount));
			return;
		}
		log.error("쿠폰 발급 couponId = %s, 남은 수량 = %d".formatted(couponId, --amount));
		setCoupon(couponId, amount);
	}

	public void generateCouponWithLock(final String couponId) {
		final RLock lock = redissonClient.getLock(String.format("COUPONLOCK:%s", couponId));

		try {
			lock.tryLock(3, 2, TimeUnit.SECONDS);
			generateCoupon(couponId);
		} catch (InterruptedException e) {
			log.error("lock.tryLock %s".formatted(e));
		} finally {
			lock.unlock();
			log.error("lock unlock");
		}
	}
}
