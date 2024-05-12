package com.redis.lock.domain;

import com.redis.lock.domain.coupon.CouponService;
import com.redis.lock.domain.user.UserExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CouponServiceTest {

	@Autowired
	private CouponService couponService;

	String couponId;

	@BeforeEach
	void testStart() {
		couponId = "PIZZA_50PER";
		couponService.setCoupon(couponId, 50);
	}

	@Test
	@DisplayName("락이 없는 상황에서 동시 쿠폰 발급 처리를 진행할 경우 실패")
	void generateCoupon_Test_Fail() throws InterruptedException {
		final int people = 100;
		final CountDownLatch countDownLatch = new CountDownLatch(people);

		List<Thread> userThread = Stream
				.generate(() -> new Thread(new UserExecutor(couponId, countDownLatch, o -> couponService.generateCoupon(couponId))))
				.limit(people)
				.collect(Collectors.toList());
		userThread.forEach(Thread::start);
		countDownLatch.await();

		final int remainCouponCount = couponService.getRemainCountCount(couponId);
		assertEquals(remainCouponCount, 0);
	}

	@Test
	@DisplayName("분산락을 통한 동시 쿠폰 발급 처리를 진행할 경우 성공")
	void generateCouponWithLock_Test_Success() throws InterruptedException {
		final int people = 100;
		final CountDownLatch countDownLatch = new CountDownLatch(people);

		List<Thread> userThread = Stream
				.generate(() -> new Thread(new UserExecutor(couponId, countDownLatch, o -> couponService.generateCouponWithLock(couponId))))
				.limit(people)
				.collect(Collectors.toList());
		userThread.forEach(Thread::start);
		countDownLatch.await();

		final int remainCouponCount = couponService.getRemainCountCount(couponId);

		assertEquals(remainCouponCount, 0);
	}
}